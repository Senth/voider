package com.spiddekauga.voider.repo.resource;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;
import com.spiddekauga.net.IDownloadProgressListener;
import com.spiddekauga.net.IOutstreamProgressListener;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.IMethodEntity;
import com.spiddekauga.voider.network.misc.BlobDownloadMethod;
import com.spiddekauga.voider.network.resource.BulletDefEntity;
import com.spiddekauga.voider.network.resource.BulletFetchMethod;
import com.spiddekauga.voider.network.resource.BulletFetchResponse;
import com.spiddekauga.voider.network.resource.CommentFetchMethod;
import com.spiddekauga.voider.network.resource.CommentFetchResponse;
import com.spiddekauga.voider.network.resource.DefEntity;
import com.spiddekauga.voider.network.resource.DownloadSyncMethod;
import com.spiddekauga.voider.network.resource.DownloadSyncResponse;
import com.spiddekauga.voider.network.resource.EnemyDefEntity;
import com.spiddekauga.voider.network.resource.EnemyFetchMethod;
import com.spiddekauga.voider.network.resource.EnemyFetchResponse;
import com.spiddekauga.voider.network.resource.FetchMethod;
import com.spiddekauga.voider.network.resource.FetchResponse;
import com.spiddekauga.voider.network.resource.FetchStatuses;
import com.spiddekauga.voider.network.resource.LevelFetchMethod;
import com.spiddekauga.voider.network.resource.LevelFetchResponse;
import com.spiddekauga.voider.network.resource.PublishMethod;
import com.spiddekauga.voider.network.resource.PublishResponse;
import com.spiddekauga.voider.network.resource.PublishResponse.Statuses;
import com.spiddekauga.voider.network.resource.ResourceBlobEntity;
import com.spiddekauga.voider.network.resource.ResourceConflictEntity;
import com.spiddekauga.voider.network.resource.ResourceDownloadMethod;
import com.spiddekauga.voider.network.resource.ResourceDownloadResponse;
import com.spiddekauga.voider.network.resource.ResourceRevisionBlobEntity;
import com.spiddekauga.voider.network.resource.ResourceRevisionEntity;
import com.spiddekauga.voider.network.resource.UserResourceSyncMethod;
import com.spiddekauga.voider.network.resource.UserResourceSyncResponse;
import com.spiddekauga.voider.network.stat.CommentEntity;
import com.spiddekauga.voider.network.stat.LevelInfoEntity;
import com.spiddekauga.voider.repo.Cache;
import com.spiddekauga.voider.repo.CacheEntity;
import com.spiddekauga.voider.repo.IResponseListener;
import com.spiddekauga.voider.repo.WebGateway.FieldNameFileWrapper;
import com.spiddekauga.voider.repo.WebRepo;
import com.spiddekauga.voider.resources.Def;
import com.spiddekauga.voider.resources.IResource;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

/**
 * Web repository for resources
 */
public class ResourceWebRepo extends WebRepo {
private static ResourceWebRepo mInstance = null;
private Cache<UUID, CommentCacheEntity> mCommentCache = new Cache<>();
private Cache<EnemyFetchMethod, EnemyCache> mEnemyCache = new Cache<>();
private Cache<String, BulletCache> mBulletCache = new Cache<>();
private Cache<LevelFetchMethod, LevelCache> mLevelCache = new Cache<>();
/** Last progress listener for sync download */
private IDownloadProgressListener mSyncDownloadProgressListener = null;

/**
 * Private constructor to enforce singleton usage
 */
private ResourceWebRepo() {
	// Does nothing
}

/**
 * @return singleton instance of ResourceWebRepo
 */
static public ResourceWebRepo getInstance() {
	if (mInstance == null) {
		mInstance = new ResourceWebRepo();
	}

	return mInstance;
}

/**
 * Sync all downloaded levels. I.e. download all publish levels that have been downloaded on other
 * devices
 * @param lastSync last synchronized date
 * @param progressListener listens to downloaded status
 * @param responseListeners listens to the web response
 */
void syncDownloaded(Date lastSync, IDownloadProgressListener progressListener, IResponseListener... responseListeners) {
	DownloadSyncMethod method = new DownloadSyncMethod();
	method.lastSync = lastSync;

	mSyncDownloadProgressListener = progressListener;

	sendInNewThread(method, responseListeners);
}

/**
 * Sync all user resource revisions, both upload and download
 * @param uploadResources all resources that should be uploaded
 * @param removeResources these will be deleted on the server
 * @param lastSync last synchronized date
 * @param conflicts optional conflicts to resolve (null if not used)
 * @param keepLocal optional how to resolve conflicts. True keeps local versions, false server
 * gameVersion, null returns the conflicts.
 * @param responseListeners listens to the web response
 */
void syncUserResources(HashMap<UUID, ResourceRevisionEntity> uploadResources, ArrayList<UUID> removeResources, Date lastSync,
					   HashMap<UUID, ResourceConflictEntity> conflicts, Boolean keepLocal, IResponseListener... responseListeners) {
	UserResourceSyncMethod method = new UserResourceSyncMethod();
	method.lastSync = lastSync;
	method.resourceToRemove = removeResources;
	method.conflictKeepLocal = keepLocal;
	method.conflictsToFix = conflicts;

	for (Entry<UUID, ResourceRevisionEntity> entry : uploadResources.entrySet()) {
		method.resources.add(entry.getValue());
	}

	ArrayList<FieldNameFileWrapper> files = createFieldNameFiles(uploadResources);

	sendInNewThread(method, files, null, responseListeners);
}

/**
 * Download or redownload the specified resource
 * @param method download method
 * @param responseListeners listens to the web response
 */
void download(ResourceDownloadMethod method, IResponseListener... responseListeners) {
	sendInNewThread(method, responseListeners);
}

/**
 * Publish all specified resources
 * @param resources all resource to publish
 * @param progressListener send upload progress to this listener
 * @param responseListeners listens to the web response
 */
void publish(ArrayList<IResource> resources, IOutstreamProgressListener progressListener, IResponseListener... responseListeners) {
	PublishMethod method = createPublishMethod(resources);
	ArrayList<FieldNameFileWrapper> files = createFieldNameFiles(resources);

	// Upload the actual files
	sendInNewThread(method, files, progressListener, responseListeners);
}

/**
 * Creates publish method from definitions
 * @param resources all resources (may include non definitions)
 * @return all entities that should be "published"
 */
private static PublishMethod createPublishMethod(ArrayList<IResource> resources) {
	PublishMethod method = new PublishMethod();

	for (IResource resource : resources) {
		if (resource instanceof Def) {
			DefEntity defEntity = ((Def) resource).toDefEntity(true);

			if (defEntity != null) {
				method.defs.add(defEntity);
			}
		}
	}

	return method;
}

@Override
protected void handleResponse(IMethodEntity methodEntity, IEntity response, IResponseListener[] callerResponseListeners) {
	IEntity responseToSend = null;

	// Publish
	if (methodEntity instanceof PublishMethod) {
		responseToSend = handlePublishResponse(response);
	}

	// Fetch Levels
	else if (methodEntity instanceof LevelFetchMethod) {
		responseToSend = handleLevelFetchResponse((LevelFetchMethod) methodEntity, response);
	}

	// Fetch Enemies
	else if (methodEntity instanceof EnemyFetchMethod) {
		responseToSend = handleEnemyFetchResponse((EnemyFetchMethod) methodEntity, response);
	}

	// Fetch Bullets
	else if (methodEntity instanceof BulletFetchMethod) {
		responseToSend = handleBulletFetchResponse((BulletFetchMethod) methodEntity, response);
	}

	// Download resources
	else if (methodEntity instanceof ResourceDownloadMethod) {
		responseToSend = handleResourceDownloadResponse(response);
	}

	// Sync downloaded
	else if (methodEntity instanceof DownloadSyncMethod) {
		responseToSend = handleDownloadSyncResponse(response);
	}

	// Sync user resources
	else if (methodEntity instanceof UserResourceSyncMethod) {
		responseToSend = handleUserResourcesSyncResponse(response);
	}

	// Fetch comments
	else if (methodEntity instanceof CommentFetchMethod) {
		responseToSend = handleCommentFetchResponse((CommentFetchMethod) methodEntity, response);
	}

	sendResponseToListeners(methodEntity, responseToSend, callerResponseListeners);
}

/**
 * Handle response from publishing a resource
 * @param response server response, null if not valid
 * @return a correct response for publishing a resource
 */
private IEntity handlePublishResponse(IEntity response) {
	if (response instanceof PublishResponse) {
		return response;
	} else {
		PublishResponse publishMethodResponse = new PublishResponse();
		publishMethodResponse.status = Statuses.FAILED_SERVER_CONNECTION;
		return publishMethodResponse;
	}
}

/**
 * Handle response from getting levels (as in in)
 * @param methodEntity method with parameters that was called on the server
 * @param response server response, null if not valid
 * @return a correct response for getting levels
 */
private IEntity handleLevelFetchResponse(LevelFetchMethod methodEntity, IEntity response) {
	// Update cache
	if (response instanceof LevelFetchResponse) {
		if (((LevelFetchResponse) response).status.isSuccessful()) {
			cacheLevels(methodEntity, (LevelFetchResponse) response);
		}

		return response;
	} else {
		LevelFetchResponse levelGetAllMethodResponse = new LevelFetchResponse();
		levelGetAllMethodResponse.status = FetchStatuses.FAILED_SERVER_CONNECTION;
		return levelGetAllMethodResponse;
	}
}

/**
 * Handle response from fetching enemies
 * @param method parameters to the server
 * @param response server response, null if not valid
 * @return a correct server response
 */
private IEntity handleEnemyFetchResponse(EnemyFetchMethod method, IEntity response) {
	// Update cache
	if (response instanceof EnemyFetchResponse) {
		cacheEnemies(method, (EnemyFetchResponse) response);
		return response;
	} else {
		EnemyFetchResponse fixedResponse = new EnemyFetchResponse();
		fixedResponse.status = FetchStatuses.FAILED_SERVER_CONNECTION;
		return fixedResponse;
	}
}

/**
 * Handle response from fetching bullets
 * @param method parameters to the server
 * @param response server response, null if not valid
 * @return a correct server response
 */
private IEntity handleBulletFetchResponse(BulletFetchMethod method, IEntity response) {
	// Update cache
	if (response instanceof BulletFetchResponse) {
		cacheBullets(method.searchString, (BulletFetchResponse) response);
		return response;
	} else {
		BulletFetchResponse fixedResponse = new BulletFetchResponse();
		fixedResponse.status = FetchStatuses.FAILED_SERVER_CONNECTION;
		return fixedResponse;
	}
}

/**
 * Handle response from downloading a resource
 * @param response the response from the server
 * @return a correct response for downloading a resource and its dependencies
 */
private IEntity handleResourceDownloadResponse(IEntity response) {
	// Download all resources
	if (response instanceof ResourceDownloadResponse) {
		ResourceDownloadResponse resourceDownloadResponse = (ResourceDownloadResponse) response;

		for (ResourceBlobEntity resourceBlobEntity : resourceDownloadResponse.resources) {
			Gdx.app.debug(ResourceWebRepo.class.getSimpleName(),
					"Resource: " + resourceBlobEntity.resourceId + ", BlobKey: " + resourceBlobEntity.blobKey);
		}

		boolean success = downloadResources(resourceDownloadResponse.resources, null);

		if (!success) {
			resourceDownloadResponse.status = ResourceDownloadResponse.Statuses.FAILED_DOWNLOAD;
		}

		return response;
	}
	// Error connecting to server
	else {
		ResourceDownloadResponse methodResponse = new ResourceDownloadResponse();
		methodResponse.status = ResourceDownloadResponse.Statuses.FAILED_CONNECTION;
		return methodResponse;
	}
}

/**
 * Handle response from sync downloaded
 * @param response the response from the server
 * @return a correct response for syncing downloaded resources
 */
private IEntity handleDownloadSyncResponse(IEntity response) {
	// Download all resources
	if (response instanceof DownloadSyncResponse) {

		boolean success = downloadResources(((DownloadSyncResponse) response).resources, mSyncDownloadProgressListener);

		if (!success) {
			((DownloadSyncResponse) response).status = DownloadSyncResponse.Statuses.FAILED_DOWNLOAD;
		}

		return response;
	} else {
		DownloadSyncResponse methodResponse = new DownloadSyncResponse();
		methodResponse.status = DownloadSyncResponse.Statuses.FAILED_CONNECTION;
		return methodResponse;
	}
}

/**
 * Handle response from sync user resource revisions
 * @param response the response from the server
 * @return a correct response for syncing user resource revisions
 */
private IEntity handleUserResourcesSyncResponse(IEntity response) {
	if (response instanceof UserResourceSyncResponse) {
		return response;
	} else {
		UserResourceSyncResponse methodResponse = new UserResourceSyncResponse();
		methodResponse.uploadStatus = UserResourceSyncResponse.UploadStatuses.FAILED_CONNECTION;
		return methodResponse;
	}
}

/**
 * Handle get resource comments response
 * @param method
 * @param response
 * @return a correct response for getting comments
 */
private IEntity handleCommentFetchResponse(CommentFetchMethod method, IEntity response) {
	if (response instanceof CommentFetchResponse) {
		if (((CommentFetchResponse) response).isSuccessful()) {
			cacheComments(method.resourceId, (CommentFetchResponse) response);
		}

		return response;
	} else {
		CommentFetchResponse validResponse = new CommentFetchResponse();
		validResponse.status = FetchStatuses.FAILED_SERVER_CONNECTION;
		return validResponse;
	}
}

/**
 * Cache a level
 * @param method parameters to the server (or search parameters)
 * @param response server response
 */
private void cacheLevels(LevelFetchMethod method, LevelFetchResponse response) {
	LevelCache cache = mLevelCache.get(method);

	boolean newCache = false;

	// Create new cache
	if (cache == null) {
		cache = new LevelCache();
		newCache = true;
	}

	updateLevelCache(cache, response);

	if (newCache) {
		mLevelCache.add(method, cache);
	}
}

/**
 * Cache new enemies
 * @param method search criteria
 * @param response response from the server
 */
private void cacheEnemies(EnemyFetchMethod method, EnemyFetchResponse response) {
	EnemyCache cache = mEnemyCache.get(method);

	boolean newCache = false;

	// Create new cache
	if (cache == null) {
		cache = new EnemyCache();
		newCache = true;
	}

	cache.enemies.addAll(response.enemies);
	updateServerCache(cache, response);

	if (newCache) {
		mEnemyCache.add(method, cache);
	}
}

/**
 * Cache new bullets
 * @param searchString search criteria
 * @param response server response
 */
private void cacheBullets(String searchString, BulletFetchResponse response) {
	BulletCache cache = mBulletCache.get(searchString);

	boolean newCache = false;

	// Create new cache
	if (cache == null) {
		cache = new BulletCache();
		newCache = true;
	}

	cache.bullets.addAll(response.bullets);
	updateServerCache(cache, response);

	if (newCache) {
		mBulletCache.add(searchString, cache);
	}
}

/**
 * Download all specified resources
 * @param resources all resources to download.
 * @param progressListener optional progress listener
 * @return true if all resources were downloaded.
 */
boolean downloadResources(ArrayList<? extends ResourceBlobEntity> resources, IDownloadProgressListener progressListener) {
	// Add all resources to download
	ArrayList<DownloadResourceWrapper> toDownload = new ArrayList<>();


	for (ResourceBlobEntity resourceInfo : resources) {
		String resourceFileName = null;
		if (resourceInfo instanceof ResourceRevisionBlobEntity) {
			resourceFileName = ResourceLocalRepo.getRevisionFilepath(resourceInfo.resourceId,
					((ResourceRevisionBlobEntity) resourceInfo).revision);
		} else {
			resourceFileName = ResourceLocalRepo.getFilepath(resourceInfo.resourceId);
		}

		// Create directories
		FileHandle file = Gdx.files.external(resourceFileName);
		if (!file.exists()) {
			// Create revision directory if it doesn't exist
			if (resourceInfo instanceof ResourceRevisionBlobEntity) {
				FileHandle parentDir = file.parent();

				if (!parentDir.exists()) {
					parentDir.mkdirs();
				}
			}
		}

		toDownload.add(new DownloadResourceWrapper(resourceFileName, resourceInfo));
	}

	downloadInThreads(toDownload, progressListener);

	for (ResourceBlobEntity resourceInfo : resources) {
		if (!resourceInfo.downloaded) {
			return false;
		}
	}

	return true;
}

/**
 * Cache comments
 * @param resourceId
 * @param response
 */
private void cacheComments(UUID resourceId, CommentFetchResponse response) {
	// Does the cache exist?
	CommentCacheEntity cache = mCommentCache.getCopy(resourceId);

	// Create new cache
	if (cache == null) {
		cache = new CommentCacheEntity();
	}

	if (response.userComment != null) {
		cache.userComment = response.userComment;
	}

	cache.comments.addAll(response.comments);
	updateServerCache(cache, response);

	mCommentCache.add(resourceId, cache);
}

/**
 * Update existing LevelCache from response
 * @param levelCache
 * @param response the response from the server
 */
private static void updateLevelCache(LevelCache levelCache, LevelFetchResponse response) {
	levelCache.addLevels(response.levels);
	updateServerCache(levelCache, response);
}

/**
 * Update existing server cache
 * @param cache server cache
 * @param fetchResponse
 */
private static void updateServerCache(ServerCache<?> cache, FetchResponse fetchResponse) {
	cache.serverCursor = fetchResponse.cursor;
	cache.fetchedAll = fetchResponse.status == FetchStatuses.SUCCESS_FETCHED_ALL;
}

/**
 * Get comments for a resource. If cached comments exists those will be returned directly if
 * fetchMore is set to false.
 * @param resourceId id of the resource to get comments from
 * @param fetchMore set to true to always fetch more levels if possible.
 * @param responseListeners listens to the web response
 */
public void getComments(UUID resourceId, boolean fetchMore, IResponseListener... responseListeners) {
	CommentFetchMethod method = new CommentFetchMethod();
	method.resourceId = resourceId;
	CommentCacheEntity cache = mCommentCache.getCopy(resourceId);
	CommentFetchResponse response = new CommentFetchResponse();
	if (cache != null) {
		response.comments = cache.comments;
		response.userComment = cache.userComment;
	}
	fetch(method, cache, fetchMore, responseListeners, response);
}

/**
 * Get any resource
 * @param method the method to send
 * @param cache the cache to maybe use
 * @param fetchMore true if we should fetch more, false to use the cache (if it exists)
 * @param responseListeners callers to send the response to
 * @param response response to use if cache was used
 */
private void fetch(FetchMethod method, ServerCache<?> cache, boolean fetchMore, IResponseListener[] responseListeners, FetchResponse response) {
	// Fetch more from server
	if (cache == null || (fetchMore && !cache.fetchedAll)) {
		if (cache != null) {
			method.nextCursor = cache.serverCursor;
		}
		sendInNewThread(method, responseListeners);
	}
	// Use cache
	else {
		response.status = cache.fetchedAll ? FetchStatuses.SUCCESS_FETCHED_ALL : FetchStatuses.SUCCESS_MORE_EXISTS;
		sendResponseToListeners(method, response, responseListeners);
	}
}

/**
 * Check if more comments can be fetched for the specified resource.
 * @param resourceId
 * @return true if more comments can be fetched
 */
public boolean hasMoreComments(UUID resourceId) {
	CommentCacheEntity cache = mCommentCache.get(resourceId);
	return cache == null || !cache.fetchedAll;
}

/**
 * Get levels either by sorting or searching
 * @param method parameters to the server
 * @param fetchMore set to true to always fetch more levels from the server (if available)
 * @param responseListeners listens to the server response
 */
public void getLevels(LevelFetchMethod method, boolean fetchMore, IResponseListener... responseListeners) {
	LevelFetchMethod levelFetchMethod = method.copy();
	LevelFetchResponse response = new LevelFetchResponse();
	LevelCache cache = mLevelCache.getCopy(levelFetchMethod);
	if (cache != null) {
		response.levels = cache.levels;
	}
	fetch(levelFetchMethod, cache, fetchMore, responseListeners, response);
}

/**
 * Check if the server has more levels
 * @param method parameters to the server
 * @return true if the server has more levels
 */
public boolean hasMoreLevels(LevelFetchMethod method) {
	return hasMore(mLevelCache.get(method));
}

/**
 * Check if more things can be fetched from the server
 * @param cache the cache to check
 * @return true if the server has more things to fetch
 */
private boolean hasMore(ServerCache<?> cache) {
	return cache != null && !cache.fetchedAll;
}

/**
 * Get enemies from the server or cache
 * @param method fetch enemy method with all parameters what to search for
 * @param fetchMore true if we should fetch more, false to use cache if the cache exists
 * @param responseListeners callers to send the response to
 */
public void getEnemies(EnemyFetchMethod method, boolean fetchMore, IResponseListener... responseListeners) {
	EnemyFetchMethod fetchMethod = method.copy();
	EnemyCache cache = mEnemyCache.getCopy(fetchMethod);
	EnemyFetchResponse response = new EnemyFetchResponse();
	if (cache != null) {
		response.enemies = cache.enemies;
	}
	fetch(fetchMethod, cache, fetchMore, responseListeners, response);
}

/**
 * Check if the server has more enemies to fetch for this search criteria
 * @param method search criteria
 * @return true if the server has more enemies to fetch
 */
public boolean hasMoreEnemies(EnemyFetchMethod method) {
	return hasMore(mEnemyCache.get(method));
}

/**
 * Get bullets from the server or cache
 * @param searchString the string to search for
 * @param fetchMore true if we should fetch more, false to use cache if the cache exists
 * @param responseListeners callers to send the response to
 */
public void getBullets(String searchString, boolean fetchMore, IResponseListener... responseListeners) {
	BulletFetchMethod method = new BulletFetchMethod();
	method.searchString = searchString;
	BulletCache cache = mBulletCache.getCopy(searchString);
	BulletFetchResponse response = new BulletFetchResponse();
	if (cache != null) {
		response.bullets = cache.bullets;
	}
	fetch(method, cache, fetchMore, responseListeners, response);
}

/**
 * Check if the server has more bullets to fetch for this search criteria
 * @param searchString what to search for
 * @return true if the server has more bullets to fetch
 */
public boolean hasMoreBullets(String searchString) {
	return hasMore(mBulletCache.get(searchString));
}

/**
 * Resource Blob download wrapper
 */
private class DownloadResourceWrapper extends DownloadBlobWrapper {
	private ResourceBlobEntity mResourceInfo;

	/**
	 * Sets the resource blob information
	 * @param filepath
	 * @param resourceInfo
	 */
	public DownloadResourceWrapper(String filepath, ResourceBlobEntity resourceInfo) {
		super(new BlobDownloadMethod(resourceInfo.blobKey), filepath);
		mResourceInfo = resourceInfo;
	}

	@Override
	protected synchronized void setDownloaded(boolean downloaded) {
		super.setDownloaded(downloaded);

		mResourceInfo.downloaded = downloaded;
	}
}

/**
 * Common cache class
 * @param <EntityType>
 */
private abstract class ServerCache<EntityType extends ServerCache<?>> extends CacheEntity<EntityType> {
	boolean fetchedAll = false;
	String serverCursor = null;

	/**
	 * @param outdated how long time the cache should be used
	 */
	protected ServerCache(long outdated) {
		super(outdated);
	}

	@Override
	public void copy(EntityType copy) {
		super.copy(copy);
		copy.fetchedAll = fetchedAll;
		copy.serverCursor = serverCursor;
	}
}

/**
 * Common cache for actors
 * @param <EntityType>
 */
private abstract class ActorCache<EntityType extends ActorCache<?>> extends ServerCache<EntityType> implements Disposable {
	/**
	 * Create actor cache with default cache time
	 */
	protected ActorCache() {
		super(Config.Cache.RESOURCE_BROWSE_TIME);
	}

	@Override
	public void dispose() {
		List<? extends DefEntity> actors = getActors();
		if (actors != null) {
			for (DefEntity actor : actors) {
				if (actor.drawable instanceof TextureRegionDrawable) {
					((TextureRegionDrawable) actor.drawable).getRegion().getTexture().dispose();
				}
			}
		}
	}

	protected abstract List<? extends DefEntity> getActors();
}

/**
 * Enemy cache when getting enemies
 */
private class EnemyCache extends ActorCache<EnemyCache> {
	ArrayList<EnemyDefEntity> enemies = new ArrayList<>();

	@Override
	public EnemyCache copy() {
		EnemyCache copy = new EnemyCache();
		copy(copy);
		return copy;
	}

	@Override
	public synchronized void copy(EnemyCache copy) {
		super.copy(copy);
		copy.enemies.addAll(enemies);
	}

	@Override
	protected List<? extends DefEntity> getActors() {
		return enemies;
	}
}

/**
 * Bullet cache when getting bullets
 */
private class BulletCache extends ActorCache<BulletCache> {
	ArrayList<BulletDefEntity> bullets = new ArrayList<>();

	@Override
	public BulletCache copy() {
		BulletCache copy = new BulletCache();
		copy(copy);
		return copy;
	}

	@Override
	public void copy(BulletCache copy) {
		super.copy(copy);
		copy.bullets.addAll(bullets);
	}

	@Override
	protected List<? extends DefEntity> getActors() {
		return bullets;
	}

}

/**
 * Level cache when getting levels
 */
private class LevelCache extends ServerCache<LevelCache> implements Disposable {
	ArrayList<LevelInfoEntity> levels = new ArrayList<>();

	/**
	 * Create level cache with default cache time
	 */
	LevelCache() {
		super(Config.Cache.RESOURCE_BROWSE_TIME);
	}

	/**
	 * Add levels to existing cache
	 * @param levels
	 */
	public synchronized void addLevels(ArrayList<LevelInfoEntity> levels) {
		this.levels.addAll(levels);
	}

	@Override
	public LevelCache copy() {
		LevelCache copy = new LevelCache();
		copy(copy);
		return copy;
	}


	@Override
	public synchronized void copy(LevelCache copy) {
		super.copy(copy);
		copy.levels.addAll(levels);
	}

	@Override
	public void dispose() {
		if (levels != null) {
			// Dispose drawables
			for (LevelInfoEntity levelInfoEntity : levels) {
				if (levelInfoEntity.defEntity.drawable instanceof TextureRegionDrawable) {
					((TextureRegionDrawable) levelInfoEntity.defEntity.drawable).getRegion().getTexture().dispose();
				}
				levelInfoEntity.defEntity.drawable = null;
			}
		}
	}


}

/**
 * Comment cache
 */
private class CommentCacheEntity extends ServerCache<CommentCacheEntity> {
	ArrayList<CommentEntity> comments = new ArrayList<>();
	CommentEntity userComment = null;

	/**
	 * Create cache with default cache time
	 */
	public CommentCacheEntity() {
		super(Config.Cache.COMMENT_TIME);
	}

	@Override
	public CommentCacheEntity copy() {
		CommentCacheEntity copy = new CommentCacheEntity();
		copy(copy);
		return copy;
	}


	@Override
	public void copy(CommentCacheEntity copy) {
		super.copy(copy);
		copy.userComment = userComment;
		copy.comments.addAll(comments);
	}

}
}

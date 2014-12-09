package com.spiddekauga.voider.repo.resource;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;
import com.spiddekauga.net.IDownloadProgressListener;
import com.spiddekauga.net.IOutstreamProgressListener;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.IMethodEntity;
import com.spiddekauga.voider.network.entities.misc.BlobDownloadMethod;
import com.spiddekauga.voider.network.entities.resource.BulletDefEntity;
import com.spiddekauga.voider.network.entities.resource.BulletFetchMethod;
import com.spiddekauga.voider.network.entities.resource.BulletFetchMethodResponse;
import com.spiddekauga.voider.network.entities.resource.CommentFetchMethod;
import com.spiddekauga.voider.network.entities.resource.CommentFetchMethodResponse;
import com.spiddekauga.voider.network.entities.resource.DefEntity;
import com.spiddekauga.voider.network.entities.resource.DownloadSyncMethod;
import com.spiddekauga.voider.network.entities.resource.DownloadSyncMethodResponse;
import com.spiddekauga.voider.network.entities.resource.EnemyDefEntity;
import com.spiddekauga.voider.network.entities.resource.EnemyFetchMethod;
import com.spiddekauga.voider.network.entities.resource.EnemyFetchMethodResponse;
import com.spiddekauga.voider.network.entities.resource.FetchMethod;
import com.spiddekauga.voider.network.entities.resource.FetchMethodResponse;
import com.spiddekauga.voider.network.entities.resource.FetchStatuses;
import com.spiddekauga.voider.network.entities.resource.LevelFetchMethod;
import com.spiddekauga.voider.network.entities.resource.LevelFetchMethodResponse;
import com.spiddekauga.voider.network.entities.resource.PublishMethod;
import com.spiddekauga.voider.network.entities.resource.PublishMethodResponse;
import com.spiddekauga.voider.network.entities.resource.PublishMethodResponse.Statuses;
import com.spiddekauga.voider.network.entities.resource.ResourceBlobEntity;
import com.spiddekauga.voider.network.entities.resource.ResourceConflictEntity;
import com.spiddekauga.voider.network.entities.resource.ResourceDownloadMethod;
import com.spiddekauga.voider.network.entities.resource.ResourceDownloadMethodResponse;
import com.spiddekauga.voider.network.entities.resource.ResourceRevisionBlobEntity;
import com.spiddekauga.voider.network.entities.resource.ResourceRevisionEntity;
import com.spiddekauga.voider.network.entities.resource.UserResourceSyncMethod;
import com.spiddekauga.voider.network.entities.resource.UserResourceSyncMethodResponse;
import com.spiddekauga.voider.network.entities.stat.CommentEntity;
import com.spiddekauga.voider.network.entities.stat.LevelInfoEntity;
import com.spiddekauga.voider.repo.Cache;
import com.spiddekauga.voider.repo.CacheEntity;
import com.spiddekauga.voider.repo.IResponseListener;
import com.spiddekauga.voider.repo.WebGateway.FieldNameFileWrapper;
import com.spiddekauga.voider.repo.WebRepo;
import com.spiddekauga.voider.resources.Def;
import com.spiddekauga.voider.resources.IResource;

/**
 * Web repository for resources
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class ResourceWebRepo extends WebRepo {
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
	 * Sync all downloaded levels. I.e. download all publish levels that have been
	 * downloaded on other devices
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
	 * @param keepLocal optional how to resolve conflicts. True keeps local versions,
	 *        false server version, null returns the conflicts.
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
	 * Downloads all the specified resources
	 * @param resourceId id of the resource to download
	 * @param responseListeners listens to the web response
	 */
	void download(UUID resourceId, IResponseListener... responseListeners) {
		ResourceDownloadMethod method = new ResourceDownloadMethod();
		method.resourceId = resourceId;

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
	 * Handle get resource comments response
	 * @param method
	 * @param response
	 * @return a correct response for getting comments
	 */
	private IEntity handleCommentFetchResponse(CommentFetchMethod method, IEntity response) {
		if (response instanceof CommentFetchMethodResponse) {
			if (((CommentFetchMethodResponse) response).isSuccessful()) {
				cacheComments(method.resourceId, (CommentFetchMethodResponse) response);
			}

			return response;
		} else {
			CommentFetchMethodResponse validResponse = new CommentFetchMethodResponse();
			validResponse.status = FetchStatuses.FAILED_SERVER_CONNECTION;
			return validResponse;
		}
	}

	/**
	 * Cache comments
	 * @param resourceId
	 * @param response
	 */
	private void cacheComments(UUID resourceId, CommentFetchMethodResponse response) {
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
	 * Handle response from sync user resource revisions
	 * @param response the response from the server
	 * @return a correct response for syncing user resource revisions
	 */
	private IEntity handleUserResourcesSyncResponse(IEntity response) {
		if (response instanceof UserResourceSyncMethodResponse) {
			return response;
		} else {
			UserResourceSyncMethodResponse methodResponse = new UserResourceSyncMethodResponse();
			methodResponse.uploadStatus = UserResourceSyncMethodResponse.UploadStatuses.FAILED_CONNECTION;
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
		if (response instanceof DownloadSyncMethodResponse) {

			boolean success = downloadResources(((DownloadSyncMethodResponse) response).resources, mSyncDownloadProgressListener);

			if (!success) {
				((DownloadSyncMethodResponse) response).status = DownloadSyncMethodResponse.Statuses.FAILED_DOWNLOAD;
			}

			return response;
		} else {
			DownloadSyncMethodResponse methodResponse = new DownloadSyncMethodResponse();
			methodResponse.status = DownloadSyncMethodResponse.Statuses.FAILED_CONNECTION;
			return methodResponse;
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

			String filePath = Gdx.files.getExternalStoragePath() + resourceFileName;
			toDownload.add(new DownloadResourceWrapper(filePath, resourceInfo));
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
	 * Handle response from downloading a resource
	 * @param response the response from the server
	 * @return a correct response for downloading a resource and its dependencies
	 */
	private IEntity handleResourceDownloadResponse(IEntity response) {
		// Download all resources
		if (response instanceof ResourceDownloadMethodResponse) {

			boolean success = downloadResources(((ResourceDownloadMethodResponse) response).resources, null);

			if (!success) {
				((ResourceDownloadMethodResponse) response).status = ResourceDownloadMethodResponse.Statuses.FAILED_DOWNLOAD;
			}

			return response;
		}
		// Error connecting to server
		else {
			ResourceDownloadMethodResponse methodResponse = new ResourceDownloadMethodResponse();
			methodResponse.status = ResourceDownloadMethodResponse.Statuses.FAILED_CONNECTION;
			return methodResponse;
		}
	}

	/**
	 * Handle response from publishing a resource
	 * @param response server response, null if not valid
	 * @return a correct response for publishing a resource
	 */
	private IEntity handlePublishResponse(IEntity response) {
		if (response instanceof PublishMethodResponse) {
			return response;
		} else {
			PublishMethodResponse publishMethodResponse = new PublishMethodResponse();
			publishMethodResponse.status = Statuses.FAILED_SERVER_CONNECTION;
			return publishMethodResponse;
		}
	}

	/**
	 * Cache a level
	 * @param method parameters to the server (or search parameters)
	 * @param response server response
	 */
	private void cacheLevels(LevelFetchMethod method, LevelFetchMethodResponse response) {
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
	 * Handle response from getting levels (as in in)
	 * @param methodEntity method with parameters that was called on the server
	 * @param response server response, null if not valid
	 * @return a correct response for getting levels
	 */
	private IEntity handleLevelFetchResponse(LevelFetchMethod methodEntity, IEntity response) {
		// Update cache
		if (response instanceof LevelFetchMethodResponse) {
			if (((LevelFetchMethodResponse) response).status.isSuccessful()) {
				cacheLevels(methodEntity, (LevelFetchMethodResponse) response);
			}

			return response;
		} else {
			LevelFetchMethodResponse levelGetAllMethodResponse = new LevelFetchMethodResponse();
			levelGetAllMethodResponse.status = FetchStatuses.FAILED_SERVER_CONNECTION;
			return levelGetAllMethodResponse;
		}
	}

	/**
	 * Update existing LevelCache from response
	 * @param levelCache
	 * @param response the response from the server
	 */
	private static void updateLevelCache(LevelCache levelCache, LevelFetchMethodResponse response) {
		levelCache.addLevels(response.levels);
		updateServerCache(levelCache, response);
	}

	/**
	 * Update existing server cache
	 * @param cache server cache
	 * @param fetchResponse
	 */
	private static void updateServerCache(ServerCache<?> cache, FetchMethodResponse fetchResponse) {
		cache.serverCursor = fetchResponse.cursor;
		cache.fetchedAll = fetchResponse.status == FetchStatuses.SUCCESS_FETCHED_ALL;
	}

	/**
	 * Handle response from fetching enemies
	 * @param method parameters to the server
	 * @param response server response, null if not valid
	 * @return a correct server response
	 */
	private IEntity handleEnemyFetchResponse(EnemyFetchMethod method, IEntity response) {
		// Update cache
		if (response instanceof EnemyFetchMethodResponse) {
			cacheEnemies(method, (EnemyFetchMethodResponse) response);
			return response;
		} else {
			EnemyFetchMethodResponse fixedResponse = new EnemyFetchMethodResponse();
			fixedResponse.status = FetchStatuses.FAILED_SERVER_CONNECTION;
			return fixedResponse;
		}
	}

	/**
	 * Cache new enemies
	 * @param method search criteria
	 * @param response response from the server
	 */
	private void cacheEnemies(EnemyFetchMethod method, EnemyFetchMethodResponse response) {
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
	 * Handle response from fetching bullets
	 * @param method parameters to the server
	 * @param response server response, null if not valid
	 * @return a correct server response
	 */
	private IEntity handleBulletFetchResponse(BulletFetchMethod method, IEntity response) {
		// Update cache
		if (response instanceof BulletFetchMethodResponse) {
			cacheBullets(method.searchString, (BulletFetchMethodResponse) response);
			return response;
		} else {
			BulletFetchMethodResponse fixedResponse = new BulletFetchMethodResponse();
			fixedResponse.status = FetchStatuses.FAILED_SERVER_CONNECTION;
			return fixedResponse;
		}
	}

	/**
	 * Cache new bullets
	 * @param searchString search criteria
	 * @param response server response
	 */
	private void cacheBullets(String searchString, BulletFetchMethodResponse response) {
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
	 * Get comments for a resource. If cached comments exists those will be returned
	 * directly if fetchMore is set to false.
	 * @param resourceId id of the resource to get comments from
	 * @param fetchMore set to true to always fetch more levels if possible.
	 * @param responseListeners listens to the web response
	 */
	public void getComments(UUID resourceId, boolean fetchMore, IResponseListener... responseListeners) {
		CommentFetchMethod method = new CommentFetchMethod();
		method.resourceId = resourceId;
		CommentCacheEntity cache = mCommentCache.getCopy(resourceId);
		CommentFetchMethodResponse response = new CommentFetchMethodResponse();
		if (cache != null) {
			response.comments = cache.comments;
			response.userComment = cache.userComment;
		}
		fetch(method, cache, fetchMore, responseListeners, response);
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
	 * @param fetchMore set to true to always fetch more levels from the server (if
	 *        available)
	 * @param responseListeners listens to the server response
	 */
	public void getLevels(LevelFetchMethod method, boolean fetchMore, IResponseListener... responseListeners) {
		LevelFetchMethod levelFetchMethod = method.copy();
		LevelFetchMethodResponse response = new LevelFetchMethodResponse();
		LevelCache cache = mLevelCache.getCopy(levelFetchMethod);
		if (cache != null) {
			response.levels = cache.levels;
		}
		fetch(levelFetchMethod, null, fetchMore, responseListeners, response);
	}

	/**
	 * Get any resource
	 * @param method the method to send
	 * @param cache the cache to maybe use
	 * @param fetchMore true if we should fetch more, false to use the cache (if it
	 *        exists)
	 * @param responseListeners callers to send the response to
	 * @param response response to use if cache was used
	 */
	private void fetch(FetchMethod method, ServerCache<?> cache, boolean fetchMore, IResponseListener[] responseListeners,
			FetchMethodResponse response) {
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
	 * @param fetchMore true if we should fetch more, false to use cache if the cache
	 *        exists
	 * @param responseListeners callers to send the response to
	 */
	public void getEnemies(EnemyFetchMethod method, boolean fetchMore, IResponseListener... responseListeners) {
		EnemyFetchMethod fetchMethod = method.copy();
		EnemyCache cache = mEnemyCache.getCopy(fetchMethod);
		EnemyFetchMethodResponse response = new EnemyFetchMethodResponse();
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
	 * @param fetchMore true if we should fetch more, false to use cache if the cache
	 *        exists
	 * @param responseListeners callers to send the response to
	 */
	public void getBullets(String searchString, boolean fetchMore, IResponseListener... responseListeners) {
		BulletFetchMethod method = new BulletFetchMethod();
		method.searchString = searchString;
		BulletCache cache = mBulletCache.getCopy(searchString);
		BulletFetchMethodResponse response = new BulletFetchMethodResponse();
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

	private static ResourceWebRepo mInstance = null;

	private Cache<UUID, CommentCacheEntity> mCommentCache = new Cache<>();
	private Cache<EnemyFetchMethod, EnemyCache> mEnemyCache = new Cache<>();
	private Cache<String, BulletCache> mBulletCache = new Cache<>();
	private Cache<LevelFetchMethod, LevelCache> mLevelCache = new Cache<>();

	/** Last progress listener for sync download */
	private IDownloadProgressListener mSyncDownloadProgressListener = null;

	/**
	 * Resource Blob download wrapper
	 */
	private class DownloadResourceWrapper extends DownloadBlobWrapper {
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

		private ResourceBlobEntity mResourceInfo;
	}

	/**
	 * Common cache class
	 * @param <EntityType>
	 */
	private abstract class ServerCache<EntityType extends ServerCache<?>> extends CacheEntity<EntityType> {
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

		boolean fetchedAll = false;
		String serverCursor = null;
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

		ArrayList<EnemyDefEntity> enemies = new ArrayList<>();
	}

	/**
	 * Bullet cache when getting bullets
	 */
	private class BulletCache extends ActorCache<BulletCache> {
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

		ArrayList<BulletDefEntity> bullets = new ArrayList<>();
	}

	/**
	 * Level cache when getting levels
	 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
	 */
	private class LevelCache extends ServerCache<LevelCache> implements Disposable {
		/**
		 * Create level cache with default cache time
		 */
		LevelCache() {
			super(Config.Cache.RESOURCE_BROWSE_TIME);
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

		/**
		 * Add levels to existing cache
		 * @param levels
		 */
		public synchronized void addLevels(ArrayList<LevelInfoEntity> levels) {
			this.levels.addAll(levels);
		}

		ArrayList<LevelInfoEntity> levels = new ArrayList<>();
	}

	/**
	 * Comment cache
	 */
	private class CommentCacheEntity extends ServerCache<CommentCacheEntity> {
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

		/** All comments */
		ArrayList<CommentEntity> comments = new ArrayList<>();
		/** User comment */
		CommentEntity userComment = null;
	}
}

package com.spiddekauga.voider.repo.resource;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;
import com.spiddekauga.net.IDownloadProgressListener;
import com.spiddekauga.net.IOutstreamProgressListener;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.game.LevelDef;
import com.spiddekauga.voider.game.actors.BulletActorDef;
import com.spiddekauga.voider.game.actors.EnemyActorDef;
import com.spiddekauga.voider.game.actors.MovementTypes;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.IMethodEntity;
import com.spiddekauga.voider.network.entities.misc.BlobDownloadMethod;
import com.spiddekauga.voider.network.entities.resource.BulletDefEntity;
import com.spiddekauga.voider.network.entities.resource.DefEntity;
import com.spiddekauga.voider.network.entities.resource.DownloadSyncMethod;
import com.spiddekauga.voider.network.entities.resource.DownloadSyncMethodResponse;
import com.spiddekauga.voider.network.entities.resource.EnemyDefEntity;
import com.spiddekauga.voider.network.entities.resource.FetchStatuses;
import com.spiddekauga.voider.network.entities.resource.LevelDefEntity;
import com.spiddekauga.voider.network.entities.resource.LevelFetchMethod;
import com.spiddekauga.voider.network.entities.resource.LevelFetchMethod.SortOrders;
import com.spiddekauga.voider.network.entities.resource.LevelFetchMethodResponse;
import com.spiddekauga.voider.network.entities.resource.PublishMethod;
import com.spiddekauga.voider.network.entities.resource.PublishMethodResponse;
import com.spiddekauga.voider.network.entities.resource.PublishMethodResponse.Statuses;
import com.spiddekauga.voider.network.entities.resource.ResourceBlobEntity;
import com.spiddekauga.voider.network.entities.resource.ResourceCommentGetMethod;
import com.spiddekauga.voider.network.entities.resource.ResourceCommentGetMethodResponse;
import com.spiddekauga.voider.network.entities.resource.ResourceConflictEntity;
import com.spiddekauga.voider.network.entities.resource.ResourceDownloadMethod;
import com.spiddekauga.voider.network.entities.resource.ResourceDownloadMethodResponse;
import com.spiddekauga.voider.network.entities.resource.ResourceRevisionBlobEntity;
import com.spiddekauga.voider.network.entities.resource.ResourceRevisionEntity;
import com.spiddekauga.voider.network.entities.resource.UserResourceSyncMethod;
import com.spiddekauga.voider.network.entities.resource.UserResourceSyncMethodResponse;
import com.spiddekauga.voider.network.entities.stat.LevelInfoEntity;
import com.spiddekauga.voider.network.entities.stat.ResourceCommentEntity;
import com.spiddekauga.voider.network.entities.stat.Tags;
import com.spiddekauga.voider.repo.Cache;
import com.spiddekauga.voider.repo.CacheEntity;
import com.spiddekauga.voider.repo.IResponseListener;
import com.spiddekauga.voider.repo.WebGateway.FieldNameFileWrapper;
import com.spiddekauga.voider.repo.WebRepo;
import com.spiddekauga.voider.resources.Def;
import com.spiddekauga.voider.resources.IResource;
import com.spiddekauga.voider.resources.IResourcePng;

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
				DefEntity defEntity = createDefEntity((Def) resource);

				if (defEntity != null) {
					method.defs.add(defEntity);
				}
			}
		}

		return method;
	}

	/**
	 * Creates Entities from definitions
	 * @param def the definition to convert from an entity
	 * @return DefEntity for the specified definition
	 */
	private static DefEntity createDefEntity(Def def) {
		DefEntity entity = null;

		// Bullet
		if (def instanceof BulletActorDef) {
			BulletDefEntity bulletEntity = new BulletDefEntity();
			setBulletDefEntity((BulletActorDef) def, bulletEntity);
			entity = bulletEntity;
		}
		// Enemy
		else if (def instanceof EnemyActorDef) {
			EnemyDefEntity enemyEntity = new EnemyDefEntity();
			setEnemyDefEntity((EnemyActorDef) def, enemyEntity);
			entity = enemyEntity;
		}
		// Level
		else if (def instanceof LevelDef) {
			LevelDefEntity levelEntity = new LevelDefEntity();
			setLevelDefEntity((LevelDef) def, levelEntity);
			entity = levelEntity;
		}
		// TODO Campaign
		// Else unknown
		else {
			Gdx.app.error("ResourceWebRepo", "Unknown Def type: " + def.getClass().getSimpleName());
		}

		return entity;
	}

	/**
	 * Converts an EnemyActorDef to EnemyDefEntity
	 * @param enemyDef the enemy definition to convert
	 * @param enemyEntity the enemy entity to set
	 */
	private static void setEnemyDefEntity(EnemyActorDef enemyDef, EnemyDefEntity enemyEntity) {
		setDefEntity(enemyDef, enemyEntity);

		// Weapon stuff
		enemyEntity.hasWeapon = enemyDef.hasWeapon() && enemyDef.getWeaponDef().getBulletActorDef() != null;
		if (enemyEntity.hasWeapon) {
			enemyEntity.bulletSpeed = enemyDef.getWeaponDef().getBulletSpeed();
			enemyEntity.bulletDamage = enemyDef.getWeaponDef().getDamage();
			enemyEntity.aimType = enemyDef.getAimType();
		}

		enemyEntity.movementType = enemyDef.getMovementType();
		if (enemyEntity.movementType != MovementTypes.STATIONARY) {
			enemyEntity.movementSpeed = enemyDef.getSpeed();
		}
	}

	/**
	 * Converts a BulletActorDef to BulletDefEntity
	 * @param bulletDef bullet definition to convert from
	 * @param bulletEntity bullet entity to set
	 */
	private static void setBulletDefEntity(BulletActorDef bulletDef, BulletDefEntity bulletEntity) {
		setDefEntity(bulletDef, bulletEntity);
	}

	/**
	 * Converts a LevelDef to LevelDefEntity
	 * @param levelDef the level definition to convert
	 * @param levelEntity level entity to set
	 */
	private static void setLevelDefEntity(LevelDef levelDef, LevelDefEntity levelEntity) {
		setDefEntity(levelDef, levelEntity);

		levelEntity.levelLength = levelDef.getLengthInTime();
		levelEntity.levelSpeed = levelDef.getBaseSpeed();
		levelEntity.levelId = levelDef.getLevelId();
	}

	/**
	 * Converts a Def to DefEntity
	 * @param def the definition to convert
	 * @param defEntity the entity to set
	 */
	private static void setDefEntity(Def def, DefEntity defEntity) {
		defEntity.name = def.getName();
		defEntity.creator = def.getRevisedBy();
		defEntity.originalCreator = def.getOriginalCreator();
		defEntity.description = def.getDescription();
		defEntity.copyParentId = def.getCopyParentId();
		defEntity.resourceId = def.getId();
		defEntity.date = def.getDate();
		defEntity.creatorKey = def.getRevisedByKey();
		defEntity.originalCreatorKey = def.getOriginalCreatorKey();

		if (def instanceof IResourcePng) {
			defEntity.png = ((IResourcePng) def).getPngImage();
		}

		// Set dependencies
		for (Entry<UUID, AtomicInteger> entry : def.getExternalDependencies().entrySet()) {
			defEntity.dependencies.add(entry.getKey());
		}
	}

	@Override
	protected void handleResponse(IMethodEntity methodEntity, IEntity response, IResponseListener[] callerResponseListeners) {
		IEntity responseToSend = null;

		// Publish
		if (methodEntity instanceof PublishMethod) {
			responseToSend = handlePublishResponse(response);
		}

		// Get Levels
		else if (methodEntity instanceof LevelFetchMethod) {
			responseToSend = handleLevelGetResponse((LevelFetchMethod) methodEntity, response);
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

		// Get comments
		else if (methodEntity instanceof ResourceCommentGetMethod) {
			responseToSend = handleResourceCommentGetResponse((ResourceCommentGetMethod) methodEntity, response);
		}

		sendResponseToListeners(methodEntity, responseToSend, callerResponseListeners);
	}

	/**
	 * Handle get resource comments response
	 * @param method
	 * @param response
	 * @return a correct response for getting comments
	 */
	private IEntity handleResourceCommentGetResponse(ResourceCommentGetMethod method, IEntity response) {
		if (response instanceof ResourceCommentGetMethodResponse) {
			if (((ResourceCommentGetMethodResponse) response).isSuccessful()) {
				cacheComments(method.resourceId, (ResourceCommentGetMethodResponse) response);
			}

			return response;
		} else {
			ResourceCommentGetMethodResponse validResponse = new ResourceCommentGetMethodResponse();
			validResponse.status = ResourceCommentGetMethodResponse.Statuses.FAILED_CONNECTION;
			return validResponse;
		}
	}

	/**
	 * Cache comments
	 * @param resourceId
	 * @param response
	 */
	private void cacheComments(UUID resourceId, ResourceCommentGetMethodResponse response) {
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
		cache.fetchedAll = response.status == ResourceCommentGetMethodResponse.Statuses.SUCCESS_FETCHED_ALL;
		cache.serverCursor = response.cursor;

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
	 * Cache a new search level
	 * @param searchString
	 * @param response
	 */
	private void cacheLevels(String searchString, LevelFetchMethodResponse response) {
		LevelCache cache = mSearchCache.get(searchString);

		boolean newCache = false;

		// Create new cache
		if (cache == null) {
			cache = new LevelCache();
			newCache = true;
		}

		updateLevelCache(cache, response);

		if (newCache) {
			mSearchCache.add(searchString, cache);
		}
	}

	/**
	 * Cache a new sort level
	 * @param sort
	 * @param tags
	 * @param response
	 */
	private void cacheLevels(SortOrders sort, ArrayList<Tags> tags, LevelFetchMethodResponse response) {
		SortWrapper sortCacheKey = new SortWrapper(sort, tags);
		LevelCache cache = mSortCache.get(sortCacheKey);

		boolean newCache = false;

		if (cache == null) {
			cache = new LevelCache();
			newCache = true;
		}

		updateLevelCache(cache, response);

		if (newCache) {
			mSortCache.add(sortCacheKey, cache);
		}
	}

	/**
	 * Handle response from getting levels (as in in)
	 * @param methodEntity method with parameters that was called on the server
	 * @param response server response, null if not valid
	 * @return a correct response for getting levels
	 */
	private IEntity handleLevelGetResponse(LevelFetchMethod methodEntity, IEntity response) {
		// Update cache
		if (response instanceof LevelFetchMethodResponse) {
			if (((LevelFetchMethodResponse) response).status.isSuccessful()) {
				// Search string
				if (methodEntity.searchString != null && !methodEntity.searchString.equals("")) {
					cacheLevels(methodEntity.searchString, (LevelFetchMethodResponse) response);
				}
				// Sorting with or without tags
				else {
					cacheLevels(methodEntity.sort, methodEntity.tagFilter, (LevelFetchMethodResponse) response);
				}
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
		levelCache.serverCursor = response.cursor;
		levelCache.addLevels(response.levels);
		levelCache.fetchedAll = response.status == FetchStatuses.SUCCESS_FETCHED_ALL;
	}

	/**
	 * Get comments for a resource. If cached comments exists those will be returned
	 * directly if fetchMore is set to false.
	 * @param resourceId id of the resource to get comments from
	 * @param fetchMore set to true to always fetch more levels if possible.
	 * @param responseListeners listens to the web response
	 */
	public void getComments(UUID resourceId, boolean fetchMore, IResponseListener... responseListeners) {
		ResourceCommentGetMethod method = new ResourceCommentGetMethod();
		method.resourceId = resourceId;

		CommentCacheEntity commentCache = mCommentCache.getCopy(resourceId);

		if (commentCache != null) {
			method.cursor = commentCache.serverCursor;
		}

		// Fetch more from server
		if (commentCache == null || (fetchMore && !commentCache.fetchedAll)) {
			sendInNewThread(method, responseListeners);
		}
		// Use cache
		else {
			ResourceCommentGetMethodResponse response = new ResourceCommentGetMethodResponse();
			response.comments = commentCache.comments;
			response.userComment = commentCache.userComment;
			response.status = ResourceCommentGetMethodResponse.Statuses.SUCCESS_FETCHED_ALL;
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
	 * Get levels by sorting and specified tags (only definitions)
	 * @param sort sorting order of the levels to get
	 * @param tags all tags the levels have to have
	 * @param fetchMore set to true to always fetch more levels from the server (if
	 *        available)
	 * @param responseListeners listens to the web response
	 */
	public void getLevels(SortOrders sort, ArrayList<Tags> tags, boolean fetchMore, IResponseListener... responseListeners) {
		LevelFetchMethod method = new LevelFetchMethod();
		method.sort = sort;
		method.tagFilter = tags;

		// Get cache
		SortWrapper sortCacheKey = new SortWrapper(sort, tags);
		LevelCache cache = mSortCache.get(sortCacheKey);


		// Fetch more from server
		if (cache == null || (fetchMore && !cache.fetchedAll)) {
			if (cache != null) {
				method.nextCursor = cache.serverCursor;
			}
			sendInNewThread(method, responseListeners);
		}
		// Use cache
		else {
			LevelFetchMethodResponse response = new LevelFetchMethodResponse();
			response.levels = cache.levels;
			response.status = FetchStatuses.SUCCESS_FETCHED_ALL;
			sendResponseToListeners(method, response, responseListeners);
		}
	}

	/**
	 * Get levels by text search
	 * @param searchString the string to search for in the levels
	 * @param fetchMore set to true to always fetch more levels
	 * @param responseListeners the caller to send the response to
	 */
	public void getLevels(String searchString, boolean fetchMore, IResponseListener... responseListeners) {
		LevelFetchMethod method = new LevelFetchMethod();
		method.searchString = searchString;

		// Get cache
		LevelCache cache = mSearchCache.getCopy(searchString);


		// Fetch more from server
		if (cache == null || (fetchMore && !cache.fetchedAll)) {
			if (cache != null) {
				method.nextCursor = cache.serverCursor;
			}
			sendInNewThread(method, responseListeners);
		}
		// Use cache
		else {
			LevelFetchMethodResponse response = new LevelFetchMethodResponse();
			response.levels = cache.levels;
			response.status = FetchStatuses.SUCCESS_FETCHED_ALL;
			sendResponseToListeners(method, response, responseListeners);
		}
	}

	/**
	 * Check if the server has more levels for this sort order and these tags.
	 * @param sort sort order to get cached levels from
	 * @param tags all tags that are checked
	 * @return true if the server has more levels
	 */
	public boolean hasMoreLevels(SortOrders sort, ArrayList<Tags> tags) {
		LevelCache levelCache = mSortCache.get(new SortWrapper(sort, tags));
		return levelCache != null && !levelCache.fetchedAll;
	}

	/**
	 * Check if the server has more levels for this search string
	 * @param searchString the string to search for
	 * @return true if the server has more levels
	 */
	public boolean hasMoreLevels(String searchString) {
		LevelCache levelCache = mSearchCache.get(searchString);
		return levelCache != null && !levelCache.fetchedAll;
	}

	/** Instance of this class */
	private static ResourceWebRepo mInstance = null;

	/** Comment cache */
	private Cache<UUID, CommentCacheEntity> mCommentCache = new Cache<>();
	/** Level search cache */
	private Cache<String, LevelCache> mSearchCache = new Cache<>();
	/** Level sort cache */
	private Cache<SortWrapper, LevelCache> mSortCache = new Cache<>();

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
	 * The key for LevelCache when getting by sort order and tags.
	 */
	private class SortWrapper {
		private SortWrapper(SortOrders sort, ArrayList<Tags> tags) {
			this.sort = sort;
			this.tags = tags;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			SortWrapper other = (SortWrapper) obj;
			return sort == other.sort && tags.equals(other.tags);
		}

		@Override
		public int hashCode() {
			return sort.hashCode() + tags.hashCode();
		}

		private SortOrders sort;
		private ArrayList<Tags> tags;
	}

	/**
	 * Level cache when getting levels
	 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
	 */
	private class LevelCache extends CacheEntity<LevelCache> implements Disposable {
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
			copy.fetchedAll = fetchedAll;
			copy.serverCursor = serverCursor;
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
		String serverCursor = null;
		boolean fetchedAll = false;
	}

	/**
	 * Comment cache
	 */
	private class CommentCacheEntity extends CacheEntity<CommentCacheEntity> {
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
			copy.fetchedAll = fetchedAll;
			copy.userComment = userComment;
			copy.serverCursor = serverCursor;
			copy.comments.addAll(comments);
		}

		/** All comments */
		ArrayList<ResourceCommentEntity> comments = new ArrayList<>();
		/** User comment */
		ResourceCommentEntity userComment = null;
		/** Server cursor to continue the cache with */
		String serverCursor = null;
		/** True if we have fetched all */
		boolean fetchedAll = false;
	}
}

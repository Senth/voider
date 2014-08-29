package com.spiddekauga.voider.repo;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;
import com.spiddekauga.utils.IOutstreamProgressListener;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.game.LevelDef;
import com.spiddekauga.voider.game.actors.BulletActorDef;
import com.spiddekauga.voider.game.actors.EnemyActorDef;
import com.spiddekauga.voider.network.entities.BulletDefEntity;
import com.spiddekauga.voider.network.entities.DefEntity;
import com.spiddekauga.voider.network.entities.EnemyDefEntity;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.LevelDefEntity;
import com.spiddekauga.voider.network.entities.LevelInfoEntity;
import com.spiddekauga.voider.network.entities.ResourceBlobEntity;
import com.spiddekauga.voider.network.entities.ResourceRevisionBlobEntity;
import com.spiddekauga.voider.network.entities.ResourceRevisionEntity;
import com.spiddekauga.voider.network.entities.Tags;
import com.spiddekauga.voider.network.entities.UploadTypes;
import com.spiddekauga.voider.network.entities.method.BlobDownloadMethod;
import com.spiddekauga.voider.network.entities.method.DownloadSyncMethod;
import com.spiddekauga.voider.network.entities.method.DownloadSyncMethodResponse;
import com.spiddekauga.voider.network.entities.method.IMethodEntity;
import com.spiddekauga.voider.network.entities.method.LevelGetAllMethod;
import com.spiddekauga.voider.network.entities.method.LevelGetAllMethod.SortOrders;
import com.spiddekauga.voider.network.entities.method.LevelGetAllMethodResponse;
import com.spiddekauga.voider.network.entities.method.PublishMethod;
import com.spiddekauga.voider.network.entities.method.PublishMethodResponse;
import com.spiddekauga.voider.network.entities.method.PublishMethodResponse.Statuses;
import com.spiddekauga.voider.network.entities.method.ResourceDownloadMethod;
import com.spiddekauga.voider.network.entities.method.ResourceDownloadMethodResponse;
import com.spiddekauga.voider.network.entities.method.UserResourcesSyncMethod;
import com.spiddekauga.voider.network.entities.method.UserResourcesSyncMethodResponse;
import com.spiddekauga.voider.repo.WebGateway.FieldNameFileWrapper;
import com.spiddekauga.voider.resources.Def;
import com.spiddekauga.voider.resources.IResource;
import com.spiddekauga.voider.resources.IResourcePng;
import com.spiddekauga.voider.utils.Pools;

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
	 * @param responseListeners listens to the web response.
	 */
	void syncDownloaded(Date lastSync, IResponseListener... responseListeners) {
		DownloadSyncMethod method = new DownloadSyncMethod();
		method.lastSync = lastSync;

		sendInNewThread(method, responseListeners);
	}

	/**
	 * Sync all user resource revisions, both upload and download
	 * @param uploadResources all resources that should be uploaded
	 * @param removeResources these will be deleted on the server
	 * @param lastSync last synchronized date
	 * @param responseListeners listens to the web response
	 */
	void syncUserResources(HashMap<UUID, ResourceRevisionEntity> uploadResources, ArrayList<UUID> removeResources, Date lastSync,
			IResponseListener... responseListeners) {
		UserResourcesSyncMethod method = new UserResourcesSyncMethod();
		method.lastSync = lastSync;
		method.resourceToRemove = removeResources;

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

		enemyEntity.enemyHasWeapon = enemyDef.hasWeapon();
		enemyEntity.enemyMovementType = enemyDef.getMovementType();
		enemyEntity.type = UploadTypes.ENEMY_DEF;
	}

	/**
	 * Converts a BulletActorDef to BulletDefEntity
	 * @param bulletDef bullet definition to convert from
	 * @param bulletEntity bullet entity to set
	 */
	private static void setBulletDefEntity(BulletActorDef bulletDef, BulletDefEntity bulletEntity) {
		setDefEntity(bulletDef, bulletEntity);

		bulletEntity.type = UploadTypes.BULLET_DEF;
	}

	/**
	 * Converts a LevelDef to LevelDefEntity
	 * @param levelDef the level definition to convert
	 * @param levelEntity level entity to set
	 */
	private static void setLevelDefEntity(LevelDef levelDef, LevelDefEntity levelEntity) {
		setDefEntity(levelDef, levelEntity);

		levelEntity.levelLength = levelDef.getLengthInTime();
		levelEntity.levelId = levelDef.getLevelId();
		levelEntity.type = UploadTypes.LEVEL_DEF;
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
		else if (methodEntity instanceof LevelGetAllMethod) {
			responseToSend = handleLevelGetResponse((LevelGetAllMethod) methodEntity, response);
		}

		// Download resources
		else if (methodEntity instanceof ResourceDownloadMethod) {
			responseToSend = handleResourceDownloadResponse(response);
		}

		// Sync downloaded
		else if (methodEntity instanceof DownloadSyncMethod) {
			responseToSend = handleSyncDownloadResponse(response);
		}

		else if (methodEntity instanceof UserResourcesSyncMethod) {
			responseToSend = handleSyncUserResourcesResponse(response);
		}


		sendResponseToListeners(methodEntity, responseToSend, callerResponseListeners);
	}

	/**
	 * Handle response from sync user resource revisions
	 * @param response the response from the server
	 * @return a correct response for syncing user resource revisions
	 */
	private IEntity handleSyncUserResourcesResponse(IEntity response) {
		if (response instanceof UserResourcesSyncMethodResponse) {
			return response;
		} else {
			UserResourcesSyncMethodResponse methodResponse = new UserResourcesSyncMethodResponse();
			methodResponse.uploadStatus = UserResourcesSyncMethodResponse.UploadStatuses.FAILED_CONNECTION;
			return methodResponse;
		}
	}

	/**
	 * Handle response from sync downloaded
	 * @param response the response from the server
	 * @return a correct response for syncing downloaded resources
	 */
	private IEntity handleSyncDownloadResponse(IEntity response) {
		// Download all resources
		if (response instanceof DownloadSyncMethodResponse) {

			boolean success = downloadResources(((DownloadSyncMethodResponse) response).resources);

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
	 * @return true if all resources were downloaded.
	 */
	boolean downloadResources(ArrayList<ResourceBlobEntity> resources) {
		for (ResourceBlobEntity resourceInfo : resources) {
			BlobDownloadMethod blobDownloadMethod = new BlobDownloadMethod();
			blobDownloadMethod.blobKey = resourceInfo.blobKey;

			String resourceFileName = null;
			if (resourceInfo instanceof ResourceRevisionBlobEntity) {
				resourceFileName = ResourceLocalRepo.getRevisionFilepath(resourceInfo.resourceId,
						((ResourceRevisionBlobEntity) resourceInfo).revision);
			} else {
				resourceFileName = ResourceLocalRepo.getFilepath(resourceInfo.resourceId);
			}

			// Only download if we don't have it
			FileHandle file = Gdx.files.external(resourceFileName);
			if (!file.exists()) {
				// Create revision directory if it doesn't exist
				if (resourceInfo instanceof ResourceRevisionBlobEntity) {
					FileHandle parentDir = file.parent();

					if (!parentDir.exists()) {
						parentDir.mkdirs();
					}
				}

				String filePath = Gdx.files.getExternalStoragePath() + resourceFileName;

				resourceInfo.downloaded = serializeAndDownload(blobDownloadMethod, filePath);

				if (!resourceInfo.downloaded) {
					return false;
				}
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

			boolean success = downloadResources(((ResourceDownloadMethodResponse) response).resources);

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
	 * Handle response from getting levels (as in in)
	 * @param methodEntity method with parameters that was called on the server
	 * @param response server response, null if not valid
	 * @return a correct response for getting levels
	 */
	@SuppressWarnings("unchecked")
	private synchronized IEntity handleLevelGetResponse(LevelGetAllMethod methodEntity, IEntity response) {
		// Update cache
		if (response instanceof LevelGetAllMethodResponse) {
			if (((LevelGetAllMethodResponse) response).status.isSuccessful()) {
				// Get or create cache
				LevelCache levelCache = null;
				boolean newCache = false;

				// Search string
				if (methodEntity.searchString != null && !methodEntity.searchString.equals("")) {
					levelCache = mSearchCache.get(methodEntity.searchString);

					// Create new
					if (levelCache == null) {
						levelCache = createNewLevelCache((LevelGetAllMethodResponse) response);
						((LevelGetAllMethodResponse) response).levels = (ArrayList<LevelInfoEntity>) levelCache.levels.clone();
						mSearchCache.put(methodEntity.searchString, levelCache);
						newCache = true;
					}
				}
				// Sorting with or without tags
				else {
					// Tag list
					HashMap<ArrayList<Tags>, LevelCache> tagCaches = mSortCache.get(methodEntity.sort);

					// Create new tag caches
					if (tagCaches == null) {
						tagCaches = new HashMap<>();
						mSortCache.put(methodEntity.sort, tagCaches);
					}

					// Level cache
					levelCache = tagCaches.get(methodEntity.tagFilter);

					if (levelCache == null) {
						levelCache = createNewLevelCache((LevelGetAllMethodResponse) response);
						tagCaches.put(methodEntity.tagFilter, levelCache);
						newCache = true;
					}
				}

				// Fetched all?
				if (((LevelGetAllMethodResponse) response).status == LevelGetAllMethodResponse.Statuses.SUCCESS_FETCHED_ALL) {
					levelCache.fetchedAll = true;
				}

				// Update cursor
				levelCache.serverCursor = ((LevelGetAllMethodResponse) response).cursor;

				// Add to cache
				if (!newCache) {
					levelCache.levels.addAll(((LevelGetAllMethodResponse) response).levels);
				}
			}

			return response;
		} else {
			LevelGetAllMethodResponse levelGetAllMethodResponse = new LevelGetAllMethodResponse();
			levelGetAllMethodResponse.status = LevelGetAllMethodResponse.Statuses.FAILED_SERVER_CONNECTION;
			return levelGetAllMethodResponse;
		}
	}

	/**
	 * Create new LevelCache from response
	 * @param response the response from the server
	 * @return new LevelCache instance
	 */
	private LevelCache createNewLevelCache(LevelGetAllMethodResponse response) {
		LevelCache levelCache = new LevelCache();
		levelCache.serverCursor = response.cursor;
		levelCache.levels = response.levels;
		return levelCache;
	}

	/**
	 * Get level cache from the a sort order
	 * @param sort sort order
	 * @param tags selected tags
	 * @return level cache for this sort order, null if none exists
	 */
	private LevelCache getLevelCache(SortOrders sort, ArrayList<Tags> tags) {
		HashMap<ArrayList<Tags>, LevelCache> tagCaches = mSortCache.get(sort);
		if (tagCaches != null) {
			return tagCaches.get(tags);
		}

		return null;
	}

	/**
	 * Get level cache from the a sort order
	 * @param searchString the text to search for
	 * @return level cache for this search, null if none exists
	 */
	private LevelCache getLevelCache(String searchString) {
		return mSearchCache.get(searchString);
	}

	/**
	 * Get levels by sorting and specified tags (only definitions)
	 * @param callerResponseListener the caller to send the response to
	 * @param sort sorting order of the levels to get
	 * @param tags all tags the levels have to have
	 * @return method that was created and sent
	 */
	public synchronized LevelGetAllMethod getLevels(IResponseListener callerResponseListener, SortOrders sort, ArrayList<Tags> tags) {
		LevelGetAllMethod method = new LevelGetAllMethod();
		method.sort = sort;
		method.tagFilter = tags;


		// Continue from cursor?
		HashMap<ArrayList<Tags>, LevelCache> tagCaches = mSortCache.get(sort);
		if (tagCaches != null) {
			LevelCache levelCache = tagCaches.get(method.tagFilter);
			if (levelCache != null) {
				// Remove cache if outdated
				if (levelCache.isOutdated()) {
					levelCache.dispose();
					tagCaches.remove(method.tagFilter);
				}
				// Fetched all, no need to query server, just return
				else if (levelCache.fetchedAll) {
					return method;
				}
				// Continue from cursor
				else {
					method.nextCursor = levelCache.serverCursor;
				}
			}
		}

		sendInNewThread(method, callerResponseListener);
		return method;
	}

	/**
	 * Get levels by text search
	 * @param callerResponseListener the caller to send the response to
	 * @param searchString the string to search for in the levels
	 * @return method that was created and sent
	 */
	public synchronized LevelGetAllMethod getLevels(IResponseListener callerResponseListener, String searchString) {
		LevelGetAllMethod method = new LevelGetAllMethod();
		method.searchString = searchString;
		method.tagFilter = new ArrayList<>();

		// Continue from cursor?
		LevelCache levelCache = mSearchCache.get(searchString);
		if (levelCache != null) {
			// Remove cache if outdated
			if (levelCache.isOutdated()) {
				levelCache.dispose();
				mSearchCache.remove(searchString);
			}
			// Fetched all, no need to query server
			else if (levelCache.fetchedAll) {
				return method;
			}
			// Continue from cursor
			else {
				method.nextCursor = levelCache.serverCursor;
			}
		}

		sendInNewThread(method, callerResponseListener);
		return method;
	}

	/**
	 * Get cached levels
	 * @param method the method that was sent
	 * @return all cached levels this method has
	 */
	public ArrayList<LevelInfoEntity> getCachedLevels(LevelGetAllMethod method) {
		// Search
		if (method.searchString != null && !method.searchString.equals("")) {
			return getCachedLevels(method.searchString);
		}
		// Sort
		else if (method.sort != null) {
			return getCachedLevels(method.sort, method.tagFilter);
		}
		// None
		else {
			return new ArrayList<>();
		}
	}

	/**
	 * Get cached levels
	 * @param sort sort order to get cached levels from
	 * @param tags all tags that are checked
	 * @return all cached levels that match the above criteria, empty if no cache was
	 *         found
	 */
	@SuppressWarnings("unchecked")
	public synchronized ArrayList<LevelInfoEntity> getCachedLevels(SortOrders sort, ArrayList<Tags> tags) {
		LevelCache levelCache = getLevelCache(sort, tags);

		if (levelCache != null) {
			ArrayList<LevelInfoEntity> copy = Pools.arrayList.obtain();
			copy.addAll(levelCache.levels);
			return copy;
		} else {
			return Pools.arrayList.obtain();
		}
	}

	/**
	 * Get cached levels
	 * @param searchString the string to search for
	 * @return all cached levels that match the above criteria, empty if no cache was
	 *         found
	 */
	@SuppressWarnings("unchecked")
	public synchronized ArrayList<LevelInfoEntity> getCachedLevels(String searchString) {
		LevelCache levelCache = getLevelCache(searchString);

		if (levelCache != null) {
			ArrayList<LevelInfoEntity> copy = Pools.arrayList.obtain();
			copy.addAll(levelCache.levels);
			return copy;
		} else {
			return Pools.arrayList.obtain();
		}
	}

	/**
	 * Check if the server has more levels
	 * @param method the method that was sent
	 * @return true if the server has more levels
	 */
	public boolean hasMoreLevels(LevelGetAllMethod method) {
		// Search
		if (method.searchString != null && !method.searchString.equals("")) {
			return hasMoreLevels(method.searchString);
		}
		// Sort
		else if (method.sort != null) {
			return hasMoreLevels(method.sort, method.tagFilter);
		}
		// None
		else {
			return false;
		}
	}

	/**
	 * Check if the server has more levels for this sort order and these tags.
	 * @param sort sort order to get cached levels from
	 * @param tags all tags that are checked
	 * @return true if the server has more levels
	 */
	public synchronized boolean hasMoreLevels(SortOrders sort, ArrayList<Tags> tags) {
		LevelCache levelCache = getLevelCache(sort, tags);

		return levelCache != null && !levelCache.fetchedAll;
	}

	/**
	 * Check if the server has more levels for this search string
	 * @param searchString the string to search for
	 * @return true if the server has more levels
	 */
	public synchronized boolean hasMoreLevels(String searchString) {
		LevelCache levelCache = getLevelCache(searchString);

		return levelCache != null && !levelCache.fetchedAll;
	}

	/**
	 * Check if we have cached levels
	 * @param method the method that was sent
	 * @return true if we have cached levels
	 */
	public boolean hasCachedLevels(LevelGetAllMethod method) {
		// Search
		if (method.searchString != null && !method.searchString.equals("")) {
			return hasCachedLevels(method.searchString);
		}
		// Sort
		else if (method.sort != null) {
			return hasCachedLevels(method.sort, method.tagFilter);
		}
		// None
		else {
			return false;
		}
	}

	/**
	 * Check if we have cached levels for this sort order and these tags.
	 * @param sort sort order to get cached levels from
	 * @param tags all tags that are checked
	 * @return true if we have cached levels
	 */
	public synchronized boolean hasCachedLevels(SortOrders sort, ArrayList<Tags> tags) {
		LevelCache levelCache = getLevelCache(sort, tags);

		return levelCache != null;
	}

	/**
	 * Check if we have cached levels for this search string
	 * @param searchString the string to search for
	 * @return true if we have cached levels
	 */
	public synchronized boolean hasCachedLevels(String searchString) {
		LevelCache levelCache = getLevelCache(searchString);

		return levelCache != null;
	}

	/** Instance of this class */
	private static ResourceWebRepo mInstance = null;

	// CACHE
	/** String search cache */
	private Map<String, LevelCache> mSearchCache = new HashMap<>();
	/** Sort cache */
	private Map<SortOrders, HashMap<ArrayList<Tags>, LevelCache>> mSortCache = new HashMap<>();

	/**
	 * Level cache when getting levels
	 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
	 */
	private class LevelCache extends CacheEntity implements Disposable {
		/**
		 * Create level cache with default cache time
		 */
		LevelCache() {
			super(Config.Cache.RESOURCE_BROWSE_TIME);
		}

		/** All the levels in the cache */
		@SuppressWarnings("unchecked") ArrayList<LevelInfoEntity> levels = Pools.arrayList.obtain();
		/** Server cursor to continue the cache with */
		String serverCursor = null;
		/** True if we have fetched all */
		boolean fetchedAll = false;

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


				Pools.arrayList.free(levels);
			}
		}
	}
}

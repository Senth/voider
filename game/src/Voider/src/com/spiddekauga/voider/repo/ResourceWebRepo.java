package com.spiddekauga.voider.repo;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.spiddekauga.utils.IOutstreamProgressListener;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.game.LevelDef;
import com.spiddekauga.voider.game.actors.BulletActorDef;
import com.spiddekauga.voider.game.actors.EnemyActorDef;
import com.spiddekauga.voider.network.entities.BulletDefEntity;
import com.spiddekauga.voider.network.entities.DefEntity;
import com.spiddekauga.voider.network.entities.DefTypes;
import com.spiddekauga.voider.network.entities.EnemyDefEntity;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.LevelDefEntity;
import com.spiddekauga.voider.network.entities.Tags;
import com.spiddekauga.voider.network.entities.method.IMethodEntity;
import com.spiddekauga.voider.network.entities.method.LevelGetAllMethod;
import com.spiddekauga.voider.network.entities.method.LevelGetAllMethod.SortOrders;
import com.spiddekauga.voider.network.entities.method.LevelGetAllMethodResponse;
import com.spiddekauga.voider.network.entities.method.PublishMethod;
import com.spiddekauga.voider.network.entities.method.PublishMethodResponse;
import com.spiddekauga.voider.network.entities.method.PublishMethodResponse.Statuses;
import com.spiddekauga.voider.repo.WebGateway.FieldNameFileWrapper;
import com.spiddekauga.voider.resources.Def;
import com.spiddekauga.voider.resources.IResource;
import com.spiddekauga.voider.resources.IResourcePng;

/**
 * Web repository for resources
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class ResourceWebRepo extends WebRepo {
	/**
	 * Protected constructor to enforce singleton usage
	 */
	protected ResourceWebRepo() {
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
	 * Publish all specified resources
	 * @param resources all resource to publish
	 * @param progressListener send upload progress to this listener
	 * @param responseListeners listens to the web response
	 */
	void publish(ArrayList<IResource> resources, IOutstreamProgressListener progressListener, ICallerResponseListener... responseListeners) {
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
		enemyEntity.type = DefTypes.ENEMY;
	}

	/**
	 * Converts a BulletActorDef to BulletDefEntity
	 * @param bulletDef bullet definition to convert from
	 * @param bulletEntity bullet entity to set
	 */
	private static void setBulletDefEntity(BulletActorDef bulletDef, BulletDefEntity bulletEntity) {
		setDefEntity(bulletDef, bulletEntity);

		bulletEntity.type = DefTypes.BULLET;
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
		levelEntity.type = DefTypes.LEVEL;
	}

	/**
	 * Converts a Def to DefEntity
	 * @param def the definition to convert
	 * @param defEntity the entity to set
	 */
	private static void setDefEntity(Def def, DefEntity defEntity) {
		defEntity.name = def.getName();
		defEntity.creator = def.getCreator();
		defEntity.originalCreator = def.getOriginalCreator();
		defEntity.description = def.getDescription();
		defEntity.copyParentId = def.getCopyParentId();
		defEntity.resourceId = def.getId();
		defEntity.date = def.getDate();
		defEntity.creatorKey = def.getCreatorKey();
		defEntity.originalCreatorKey = def.getOriginalCreatorKey();

		if (def instanceof IResourcePng) {
			defEntity.png = ((IResourcePng) def).getPngImage();
		}
	}

	@Override
	protected void handleResponse(IMethodEntity methodEntity, IEntity response, ICallerResponseListener[] callerResponseListeners) {
		IEntity responseToSend = null;

		// Publish
		if (methodEntity instanceof PublishMethod) {
			responseToSend = handlePublishResponse(response);
		}

		// Get Levels
		if (methodEntity instanceof LevelGetAllMethod) {
			responseToSend = handleLevelGetResponse((LevelGetAllMethod) methodEntity, responseToSend);
		}

		// Send the actual response
		if (response != null) {
			for (ICallerResponseListener responseListener : callerResponseListeners) {
				responseListener.handleWebResponse(methodEntity, responseToSend);
			}
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
	private IEntity handleLevelGetResponse(LevelGetAllMethod methodEntity, IEntity response) {
		// Update cache
		if (response instanceof LevelGetAllMethodResponse) {
			// Get or create cache
			LevelCache levelCache = null;

			// Search string
			if (methodEntity.searchString != null && !methodEntity.searchString.equals("")) {
				levelCache = mSearchCache.get(methodEntity.searchString);

				// Create new
				if (levelCache == null) {
					levelCache = createNewLevelCache((LevelGetAllMethodResponse) response);
					mSearchCache.put(methodEntity.searchString, levelCache);
				}
			}
			// Sorting with or without tags
			else {
				// Tag list
				HashMap<ArrayList<Tags>, LevelCache> tagCaches = mSortCache.get(methodEntity.sort);

				// Create new tag caches
				if (tagCaches == null) {
					tagCaches = new HashMap<>();
				}

				// Level cache
				levelCache = tagCaches.get(methodEntity.tagFilter);

				if (levelCache == null) {
					levelCache = createNewLevelCache((LevelGetAllMethodResponse) response);
					tagCaches.put(methodEntity.tagFilter, levelCache);
				}
			}

			// Update cursor
			levelCache.serverCursor = ((LevelGetAllMethodResponse) response).cursor;


			// Set response to include cached levels
			levelCache.levels.addAll(((LevelGetAllMethodResponse) response).levels);
			((LevelGetAllMethodResponse) response).levels = levelCache.levels;

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
	 * Get levels by sorting and specified tags (only definitions)
	 * @param callerResponseListener the caller to send the response to
	 * @param sort sorting order of the levels to get
	 * @param tags all tags the levels have to have
	 */
	public void getLevels(ICallerResponseListener callerResponseListener, SortOrders sort, ArrayList<Tags> tags) {
		LevelGetAllMethod method = new LevelGetAllMethod();
		method.sort = sort;
		method.tagFilter = tags;


		// Continue from cursor?
		HashMap<ArrayList<Tags>, LevelCache> tagCaches = mSortCache.get(sort);
		if (tagCaches != null) {
			LevelCache levelCache = tagCaches.get(method.tagFilter);
			if (levelCache != null) {
				// Remove cache if outdated
				if (isCacheOutdated(levelCache)) {
					levelCache.dispose();
					tagCaches.remove(method.tagFilter);
				} else {
					method.nextCursor = levelCache.serverCursor;
				}
			}
		}
	}

	/**
	 * Get levels by text search
	 * @param callerResponseListener the caller to send the response to
	 * @param searchString the string to search for in the levels
	 */
	public void getLevels(ICallerResponseListener callerResponseListener, String searchString) {
		LevelGetAllMethod method = new LevelGetAllMethod();
		method.searchString = searchString;

		// Continue from cursor?
		LevelCache levelCache = mSearchCache.get(searchString);
		if (levelCache != null) {
			// Remove cache if outdated
			if (isCacheOutdated(levelCache)) {
				levelCache.dispose();
				mSearchCache.remove(searchString);
			} else {
				method.nextCursor = levelCache.serverCursor;
			}
		}

		sendInNewThread(method, callerResponseListener);
	}

	/**
	 * Checks if a cache is outdated
	 * @param cache check if this cache is outdated
	 * @return true if the cache is outdated
	 */
	private boolean isCacheOutdated(Cache cache) {
		long currentTime = new Date().getTime();
		long createdTime = cache.created.getTime();
		return createdTime + Config.Cache.RESOURCE_BROWSE_TIME * 1000 < currentTime;
	}

	/** Instance of this class */
	private static ResourceWebRepo mInstance = null;

	// CACHE
	/** String search cache */
	Map<String, LevelCache> mSearchCache = new HashMap<>();
	/** Sort cache */
	Map<SortOrders, HashMap<ArrayList<Tags>, LevelCache>> mSortCache = new HashMap<>();
}

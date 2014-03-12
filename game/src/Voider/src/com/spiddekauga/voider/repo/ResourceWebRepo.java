package com.spiddekauga.voider.repo;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.spiddekauga.voider.game.LevelDef;
import com.spiddekauga.voider.game.actors.BulletActorDef;
import com.spiddekauga.voider.game.actors.EnemyActorDef;
import com.spiddekauga.voider.network.entities.BulletDefEntity;
import com.spiddekauga.voider.network.entities.DefEntity;
import com.spiddekauga.voider.network.entities.DefTypes;
import com.spiddekauga.voider.network.entities.EnemyDefEntity;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.LevelDefEntity;
import com.spiddekauga.voider.network.entities.method.IMethodEntity;
import com.spiddekauga.voider.network.entities.method.PublishMethod;
import com.spiddekauga.voider.network.entities.method.PublishMethodResponse;
import com.spiddekauga.voider.network.entities.method.PublishMethodResponse.Statuses;
import com.spiddekauga.voider.repo.WebGateway.FieldNameFileWrapper;
import com.spiddekauga.voider.resources.Def;
import com.spiddekauga.voider.resources.IResource;
import com.spiddekauga.voider.resources.IResourcePng;
import com.spiddekauga.voider.utils.Pools;

/**
 * Web repository for resources
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
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
	static ResourceWebRepo getInstance() {
		if (mInstance == null) {
			mInstance = new ResourceWebRepo();
		}

		return mInstance;
	}

	/**
	 * Publish all specified resources
	 * @param resources all resource to publish
	 * @param responseListeners listens to the web response
	 */
	void publish(ArrayList<IResource> resources, ICallerResponseListener... responseListeners) {
		PublishMethod method = createPublishMethod(resources);
		ArrayList<FieldNameFileWrapper> files = createFieldNameFiles(resources);

		// Upload the actual files
		sendInNewThread(method, files, responseListeners);
		Pools.arrayList.free(files);
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
			if (response instanceof PublishMethodResponse) {
				responseToSend = response;
			} else {
				PublishMethodResponse publishMethodResponse = new PublishMethodResponse();
				publishMethodResponse.status = Statuses.FAILED_SERVER_CONNECTION;
				responseToSend = publishMethodResponse;
			}
		}


		// Send the actual response
		if (response != null) {
			for (ICallerResponseListener responseListener : callerResponseListeners) {
				responseListener.handleWebResponse(methodEntity, responseToSend);
			}
		}
	}


	/** Instance of this class */
	private static ResourceWebRepo mInstance = null;
}

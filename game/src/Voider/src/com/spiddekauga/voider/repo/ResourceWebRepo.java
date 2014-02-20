package com.spiddekauga.voider.repo;

import java.io.File;
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
import com.spiddekauga.voider.network.entities.method.GetUploadUrlMethod;
import com.spiddekauga.voider.network.entities.method.GetUploadUrlMethodResponse;
import com.spiddekauga.voider.network.entities.method.IMethodEntity;
import com.spiddekauga.voider.network.entities.method.NetworkEntitySerializer;
import com.spiddekauga.voider.network.entities.method.PublishMethod;
import com.spiddekauga.voider.network.entities.method.PublishMethodResponse;
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
public class ResourceWebRepo {
	/**
	 * Send all methods that should upload files via this method
	 * @param method the method that should "called" on the server
	 * when the upload is finished
	 * @param files all files that should be uploaded
	 * @return server method response, null if something went wrong
	 */
	private static IEntity upload(IMethodEntity method, ArrayList<FieldNameFileWrapper> files) {
		// Get upload URL
		GetUploadUrlMethod uploadMethod = new GetUploadUrlMethod();
		uploadMethod.redirectMethod = method.getMethodName();
		byte[] uploadBytes = NetworkEntitySerializer.serializeEntity(uploadMethod);

		byte[] uploadResponseBytes = WebGateway.sendRequest(uploadMethod.getMethodName(), uploadBytes);
		IEntity uploadResponse = NetworkEntitySerializer.deserializeEntity(uploadResponseBytes);


		// Upload files
		if (uploadResponse instanceof GetUploadUrlMethodResponse) {
			String uploadUrl = ((GetUploadUrlMethodResponse) uploadResponse).uploadUrl;
			if (uploadUrl != null) {
				byte[] methodBytes = NetworkEntitySerializer.serializeEntity(method);
				byte[] responseBytes = WebGateway.sendUploadRequest(uploadUrl, methodBytes, files);
				return NetworkEntitySerializer.deserializeEntity(responseBytes);
			}
		}

		return null;
	}

	/**
	 * Sets field names and files to upload
	 * @param resources all the resources to upload
	 * @return list with all field names and files to upload
	 */
	private static ArrayList<FieldNameFileWrapper> createFieldNameFiles(ArrayList<IResource> resources) {
		@SuppressWarnings("unchecked")
		ArrayList<FieldNameFileWrapper> files = Pools.arrayList.obtain();

		for (IResource resource : resources) {
			// Get file
			String filepath = Gdx.files.getExternalStoragePath();
			filepath += ResourceLocalRepo.getFilepath(resource);
			File file = new File(filepath);

			if (file.exists()) {
				FieldNameFileWrapper fieldNameFile = new FieldNameFileWrapper();
				fieldNameFile.fieldName = resource.getId().toString();
				fieldNameFile.file = file;

				files.add(fieldNameFile);
			} else {
				Gdx.app.error("ResourceWebRepo", "File does not exist: " + filepath);
			}
		}

		return files;
	}

	/**
	 * Publish all specified resources
	 * @param resources all resource to publish
	 * @return true if successful, false otherwise
	 */
	static boolean  publish(ArrayList<IResource> resources) {
		PublishMethod method = createPublishMethod(resources);
		ArrayList<FieldNameFileWrapper> files = createFieldNameFiles(resources);

		// Upload the actual files
		IEntity response = upload(method, files);
		Pools.arrayList.free(files);

		if (response instanceof PublishMethodResponse) {
			return ((PublishMethodResponse) response).success;
		}

		return false;
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

		if (def instanceof IResourcePng) {
			defEntity.png = ((IResourcePng) def).getPngImage();
		}
	}
}

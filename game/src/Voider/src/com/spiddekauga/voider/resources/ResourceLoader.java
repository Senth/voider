package com.spiddekauga.voider.resources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.UUID;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.SkinLoader;
import com.badlogic.gdx.assets.loaders.resolvers.ExternalFileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.game.GameSave;
import com.spiddekauga.voider.game.GameSaveDef;
import com.spiddekauga.voider.game.Level;
import com.spiddekauga.voider.game.LevelDef;
import com.spiddekauga.voider.game.PlayerStats;
import com.spiddekauga.voider.game.actors.BulletActorDef;
import com.spiddekauga.voider.game.actors.EnemyActorDef;
import com.spiddekauga.voider.game.actors.PickupActorDef;
import com.spiddekauga.voider.game.actors.PlayerActorDef;
import com.spiddekauga.voider.game.actors.StaticTerrainActorDef;
import com.spiddekauga.voider.repo.ResourceLocalRepo;
import com.spiddekauga.voider.scene.Scene;
import com.spiddekauga.voider.utils.AbsoluteFileHandleResolver;
import com.spiddekauga.voider.utils.Pools;

/**
 * Handles loading resources
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
class ResourceLoader {
	/**
	 * Initializes the resource loader
	 */
	ResourceLoader() {
		if (Config.File.USE_EXTERNAL_RESOURCES) {
			mAssetManager = new AssetManager(new AbsoluteFileHandleResolver());
		} else {
			mAssetManager = new AssetManager();
		}

		// External
		mAssetManager.setLoader(BulletActorDef.class, new KryoLoaderAsync<BulletActorDef>(new ExternalFileHandleResolver(), BulletActorDef.class));
		mAssetManager.setLoader(EnemyActorDef.class, new KryoLoaderAsync<EnemyActorDef>(new ExternalFileHandleResolver(), EnemyActorDef.class));
		mAssetManager.setLoader(PickupActorDef.class, new KryoLoaderAsync<PickupActorDef>(new ExternalFileHandleResolver(), PickupActorDef.class));
		mAssetManager.setLoader(PlayerActorDef.class, new KryoLoaderAsync<PlayerActorDef>(new ExternalFileHandleResolver(), PlayerActorDef.class));
		mAssetManager.setLoader(StaticTerrainActorDef.class, new KryoLoaderAsync<StaticTerrainActorDef>(new ExternalFileHandleResolver(), StaticTerrainActorDef.class));
		mAssetManager.setLoader(LevelDef.class, new KryoLoaderAsync<LevelDef>(new ExternalFileHandleResolver(), LevelDef.class));
		mAssetManager.setLoader(Level.class, new KryoLoaderAsync<Level>(new ExternalFileHandleResolver(), Level.class));
		mAssetManager.setLoader(GameSave.class, new KryoLoaderSync<GameSave>(new ExternalFileHandleResolver(), GameSave.class));
		mAssetManager.setLoader(GameSaveDef.class, new KryoLoaderAsync<GameSaveDef>(new ExternalFileHandleResolver(), GameSaveDef.class));
		mAssetManager.setLoader(PlayerStats.class, new KryoLoaderAsync<PlayerStats>(new ExternalFileHandleResolver(), PlayerStats.class));

		// Internal
		if (Config.File.USE_EXTERNAL_RESOURCES) {
			mAssetManager.setLoader(ShaderProgram.class, new ShaderLoader(new AbsoluteFileHandleResolver()));
			mAssetManager.setLoader(Skin.class, new SkinLoader(new AbsoluteFileHandleResolver()));
		} else {
			mAssetManager.setLoader(ShaderProgram.class, new ShaderLoader(new InternalFileHandleResolver()));
			mAssetManager.setLoader(Skin.class, new SkinLoader(new InternalFileHandleResolver()));
		}
	}

	/**
	 * Loads the specified resource. If the resource has been loaded already
	 * it does nothing.
	 * @param scene the scene which is loading the resource
	 * @param resourceId the resource that should be loaded
	 * @param revision the revision to load. Set to -1 if the resource doesn't
	 * have revisions or if you want to load the latest revision.
	 * Note that if the resource already has been loaded, this variable
	 * will have no effect. I.e. it will not load
	 * another revision of the resource as only one revision is allowed.
	 */
	void load(Scene scene, UUID resourceId, int revision) {
		// If loaded or loading -> do nothing
		if (mLoadedResources.containsKey(resourceId) || mLoadingQueue.containsKey(resourceId)) {
			return;
		}


		LoadedResource loadedResource = new LoadedResource();
		if (revision > 0) {
			loadedResource.filepath = ResourceLocalRepo.getRevisionFilepath(resourceId, revision);
		} else {
			loadedResource.filepath = ResourceLocalRepo.getFilepath(resourceId);
		}

		loadedResource.scene = scene;

		mAssetManager.load(loadedResource.filepath, ResourceLocalRepo.getType(resourceId).getType());

		mLoadingQueue.put(resourceId, loadedResource);
	}

	/**
	 * @param resourceId the resource to check if it has been loaded
	 * @return true if the resource has been loaded
	 */
	boolean isResourceLoaded(UUID resourceId) {
		return mLoadedResources.containsKey(resourceId);
	}

	/**
	 * @return true if loading
	 */
	boolean isLoading() {
		return !mLoadingQueue.isEmpty();
	}

	/**
	 * @param <ResourceType> the resource type
	 * @param resourceId id of the resource to return
	 * @return the loaded resource with the specified id, null if not loaded
	 */
	@SuppressWarnings("unchecked")
	<ResourceType extends IResource> ResourceType getLoadedResource(UUID resourceId) {
		LoadedResource loadedResource = mLoadedResources.get(resourceId);
		if (loadedResource != null) {
			return (ResourceType) loadedResource.resource;
		} else {
			return null;
		}
	}

	/**
	 * @param <ResourceType> Type of resources to get
	 * @param type the type of resources to return
	 * @return all loaded resources of the specified type
	 */
	@SuppressWarnings("unchecked")
	<ResourceType extends IResource> ArrayList<ResourceType> getAllLoadedResourcesOf(ExternalTypes type) {
		ArrayList<ResourceType> resources = Pools.arrayList.obtain();

		for (Entry<UUID, LoadedResource> entry : mLoadedResources.entrySet()) {
			if (type.getType().isAssignableFrom(entry.getValue().resource.getClass())) {
				resources.add((ResourceType) entry.getValue().resource);
			}
		}

		return resources;
	}

	/**
	 * Unloads all resources allocated in the specified scene
	 * @param scene unload all resources in this scene
	 */
	void unload(Scene scene) {
		Iterator<Entry<UUID, LoadedResource>> iterator = mLoadedResources.entrySet().iterator();

		while (iterator.hasNext()) {
			LoadedResource loadedResource = iterator.next().getValue();

			if (loadedResource.scene == scene) {
				mAssetManager.unload(loadedResource.filepath);
				iterator.remove();
			}
		}
	}

	/**
	 * Updates the resource loader. This will set the loaded resources correctly
	 * @return true if all resources has been loaded
	 */
	boolean update() {
		Iterator<Entry<UUID, LoadedResource>> iterator = mLoadingQueue.entrySet().iterator();

		mAssetManager.update();

		while (iterator.hasNext()) {
			LoadedResource loadingResource = iterator.next().getValue();

			// Add resource if it has been loaded
			if (mAssetManager.isLoaded(loadingResource.filepath)) {
				loadingResource.resource = mAssetManager.get(loadingResource.filepath);

				mLoadedResources.put(loadingResource.resource.getId(), loadingResource);

				iterator.remove();
			}
		}

		return mLoadingQueue.isEmpty();
	}

	/**
	 * @return asset manager used for loading resources
	 */
	AssetManager getAssetManager() {
		return mAssetManager;
	}

	/**
	 * Loaded or loading resources
	 */
	private static class LoadedResource {
		/** File path of the resource */
		String filepath = null;
		/** Which scene this resource should be loaded into */
		Scene scene = null;
		/** The actual resource that was loaded */
		IResource resource = null;
	}

	/** All loaded resources */
	private HashMap<UUID, LoadedResource> mLoadedResources = new HashMap<>();
	/** All resources that are currently loading */
	private HashMap<UUID, LoadedResource> mLoadingQueue = new HashMap<>();
	/** The asset manager used for actually loading resources */
	private AssetManager mAssetManager;
}

package com.spiddekauga.voider.resources;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.UUID;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.SkinLoader;
import com.badlogic.gdx.assets.loaders.resolvers.ExternalFileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
import com.spiddekauga.voider.Config.Debug;
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
import com.spiddekauga.voider.scene.Scene;
import com.spiddekauga.voider.scene.SceneSwitcher;
import com.spiddekauga.voider.utils.Pools;


/**
 * This class is responsible for caching assets/resources, or rather act as
 * a facade to other cashes. First the resource needs to be loaded (from file)
 * into the cache before it can be used. This can be done with the various load()
 * methods. To read (and get an object) from the cache, use one of the
 * get() methods. To unload cache use one of the appropriate unload() methods.
 * 
 * @see ResourceSaver for how to save files
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class ResourceCacheFacade {
	/**
	 * Initializes the ResourceCacheFacade. This needs to be called before using any other
	 * method
	 */
	public static void init() {
		mAssetManager = new AssetManager();

		// External
		mAssetManager.setLoader(BulletActorDef.class, new JsonLoaderAsync<BulletActorDef>(new ExternalFileHandleResolver(), BulletActorDef.class));
		mAssetManager.setLoader(EnemyActorDef.class, new JsonLoaderAsync<EnemyActorDef>(new ExternalFileHandleResolver(), EnemyActorDef.class));
		mAssetManager.setLoader(PickupActorDef.class, new JsonLoaderAsync<PickupActorDef>(new ExternalFileHandleResolver(), PickupActorDef.class));
		mAssetManager.setLoader(PlayerActorDef.class, new JsonLoaderAsync<PlayerActorDef>(new ExternalFileHandleResolver(), PlayerActorDef.class));
		mAssetManager.setLoader(StaticTerrainActorDef.class, new JsonLoaderAsync<StaticTerrainActorDef>(new ExternalFileHandleResolver(), StaticTerrainActorDef.class));
		mAssetManager.setLoader(LevelDef.class, new JsonLoaderAsync<LevelDef>(new ExternalFileHandleResolver(), LevelDef.class));
		mAssetManager.setLoader(Level.class, new JsonLoaderAsync<Level>(new ExternalFileHandleResolver(), Level.class));
		mAssetManager.setLoader(GameSave.class, new JsonLoaderSync<GameSave>(new ExternalFileHandleResolver(), GameSave.class));
		mAssetManager.setLoader(GameSaveDef.class, new JsonLoaderAsync<GameSaveDef>(new ExternalFileHandleResolver(), GameSaveDef.class));
		mAssetManager.setLoader(PlayerStats.class, new JsonLoaderAsync<PlayerStats>(new ExternalFileHandleResolver(), PlayerStats.class));

		// Internal
		mAssetManager.setLoader(ShaderProgram.class, new ShaderLoader(new InternalFileHandleResolver()));
		mAssetManager.setLoader(Skin.class, new SkinLoader(new InternalFileHandleResolver()));

		mDependencyLoader = new ResourceDependencyLoader(mAssetManager);
		ResourceDatabase.init(mAssetManager);
	}

	/**
	 * Loads all latest revision for the resources of the specified type. Used in conjunction with
	 * {@link #unloadAllOf(Scene, Class, boolean)}.
	 * @param scene the scene to load all resources for
	 * @param type the type of resource to load
	 * @param loadDependencies Set to true ot load all file dependencies of the resource. E.g. some
	 * ActorDef might have textures, particle effects, sound, bullets as dependencies. In this case
	 * these will also be loaded.
	 */
	public static void loadAllOf(Scene scene, Class<? extends IResource> type, boolean loadDependencies) {
		// Get all resources of this type
		ArrayList<ResourceItem> resources = ResourceDatabase.getAllExistingResource(type);


		// Load dependencies
		if (loadDependencies) {
			for (ResourceItem resourceItem : resources) {
				mDependencyLoader.load(scene, resourceItem.id, type, resourceItem.revision);
			}
		}
		// Only load them, no dependencies
		else {
			for (ResourceItem resourceItem : resources) {
				ResourceDatabase.load(scene, resourceItem.id, type, resourceItem.revision);
			}
		}


		// Free
		Pools.resourceItem.freeAll(resources);
		Pools.arrayList.free(resources);
	}

	/**
	 * Loads all latest revision for the resource of the specified type, but has the ability
	 * to override the revision loaded. I.e. not all resource need to load the latest revision.
	 * This can be useful if you know that you want to load certain revision instead of the latest.
	 * Used in conjunction with {@link #unloadAllOf(Scene, Class, boolean, ObjectMap)}.
	 * @param scene the scene to load all resource for
	 * @param type the type of resources to load
	 * @param loadDependencies Set to true ot load all file dependencies of the resource. E.g. some
	 * ActorDef might have textures, particle effects, sound, bullets as dependencies. In this case
	 * these will also be loaded.
	 * @param revisionsToLoad an object map with a specific revision to use for the specified resource.
	 */
	public static void loadAllOf(Scene scene, Class<? extends IResource> type, boolean loadDependencies, ObjectMap<UUID, Integer> revisionsToLoad) {
		// Get all resources of this type
		ArrayList<ResourceItem> resources = ResourceDatabase.getAllExistingResource(type);


		// Load dependencies
		if (loadDependencies) {
			for (ResourceItem resourceItem : resources) {
				Integer overridingRevision = revisionsToLoad.get(resourceItem.id);
				if (overridingRevision == null) {
					overridingRevision = resourceItem.revision;
				}
				mDependencyLoader.load(scene, resourceItem.id, type, overridingRevision);
			}
		}
		// Only load them, no dependencies
		else {
			for (ResourceItem resourceItem : resources) {
				Integer overridingRevision = revisionsToLoad.get(resourceItem.id);
				if (overridingRevision == null) {
					overridingRevision = resourceItem.revision;
				}
				ResourceDatabase.load(scene, resourceItem.id, type, overridingRevision);
			}
		}


		// Free
		Pools.resourceItem.freeAll(resources);
		Pools.arrayList.free(resources);
	}

	/**
	 * Unloads all of the specified resources, but only the latest revisions. Used in conjunction
	 * with {@link #loadAllOf(Scene, Class, boolean)}.
	 * @param scene the scene to unload all the resource from
	 * @param type the type of resources to unload
	 * @param unloadDependencies true if the cache shall unload all the dependencies.
	 */
	@SuppressWarnings("unchecked")
	public static void unloadAllOf(Scene scene, Class<? extends IResource> type, boolean unloadDependencies) {
		// Get all loaded resources of this type
		ArrayList<IResource> resources = (ArrayList<IResource>) getAll(scene, type);


		// Unload dependencies
		if (unloadDependencies) {
			if (IResourceDependency.class.isAssignableFrom(type)) {
				for (IResource resource : resources) {
					mDependencyLoader.unload(scene, (IResourceDependency) resource);
				}
			} else {
				Gdx.app.error("ResourceCacheFacade", "Tried to unload dependencies of (" + type.getSimpleName() + "), but this class cannot hold dependencies!");
			}
		}
		// Only unload the resource, no dependencies
		else {
			for (IResource resource : resources) {
				ResourceDatabase.unload(scene, resource);
			}
		}


		// Free
		Pools.arrayList.free(resources);
	}

	/**
	 * Unloads all of the specified resources, usually this means the latest but this
	 * method has the ability to unload specific revisions of a resource instead of the latest.
	 * Used in conjunction with {@link #loadAllOf(Scene, Class, boolean, ObjectMap)}.
	 * @param scene the scene to unload all the resources from
	 * @param type the type of resources to unload
	 * @param unloadDependencies true if the cache shall unload all dependencies of the resources
	 * @param revisionsToUnload these resources will override the unload of latest revision to use
	 * this revision for the corresponding resource.
	 */
	public static void unloadAllOf(Scene scene, Class<? extends IResource> type, boolean unloadDependencies, ObjectMap<UUID, Integer> revisionsToUnload) {
		// Get all resources of this type
		ArrayList<ResourceItem> resources = ResourceDatabase.getAllExistingResource(type);


		// Load dependencies
		if (unloadDependencies) {
			for (ResourceItem resourceItem : resources) {
				Integer overridingRevision = revisionsToUnload.get(resourceItem.id);
				if (overridingRevision == null) {
					overridingRevision = resourceItem.revision;
				}
				// Get the resource first
				IResourceDependency resource = ResourceDatabase.getLoadedResource(scene, resourceItem.id, overridingRevision);
				mDependencyLoader.unload(scene, resource);
			}
		}
		// Only load them, no dependencies
		else {
			for (ResourceItem resourceItem : resources) {
				Integer overridingRevision = revisionsToUnload.get(resourceItem.id);
				if (overridingRevision == null) {
					overridingRevision = resourceItem.revision;
				}
				ResourceDatabase.unload(scene, resourceItem.id, type, overridingRevision);
			}
		}


		// Free
		Pools.resourceItem.freeAll(resources);
		Pools.arrayList.free(resources);
	}

	/**
	 * @return true if the resource cache facade is currently loading
	 */
	public static boolean isLoading() {
		return mAssetManager.getQueuedAssets() > 0 || mDependencyLoader.isLoading() || !ResourceDatabase.isAllResourcesLoaded();
	}

	/**
	 * Loads the resource, definition and all dependencies.
	 * @param scene the scene to load the resource to
	 * @param resourceId the id of the resource we're loading (i.e. not the
	 * definition's id).
	 * @param resourceType the class of the resource to load
	 * @param defId the definition of the resource we're loading
	 * @param defType the class of the definition
	 * @param revision the revision of the resource to load
	 */
	public static void load(Scene scene, UUID resourceId, Class<? extends IResource> resourceType, UUID defId, Class<? extends Def> defType, int revision) {
		// Load definition dependencies first
		load(scene, defId, defType, revision, true);

		// Add the resource to the queue. Load this resource once all dependencies are loaded
		mLoadQueue.add(new ResourceItem(scene, resourceId, resourceType, revision));
	}

	//	/**
	//	 * Loads an external definition. Included in these are in general resources
	//	 * that the user can add and remove. E.g. ActorDef, WeaponDef, etc. Loads
	//	 * latest revision
	//	 * @param scene the scene to load the resource to
	//	 * @param resource the resource we want to load. This includes a unique id
	//	 * as well as dependencies.
	//	 * @param loadDependencies if we also shall load the dependencies
	//	 * @throws UndefinedResourceTypeException
	//	 */
	//	public static void load(Scene scene, IResource resource, boolean loadDependencies) throws UndefinedResourceTypeException {
	//		load(scene, resource.getId(), resource.getClass(), loadDependencies, -1);
	//	}

	//	/**
	//	 * Loads an external definition. Included in these are in general resources
	//	 * that the user can add and remove. E.g. ActorDef, WeaponDef, etc.
	//	 * @param scene the scene to load the resource to
	//	 * @param resource the resource we want to load. This includes a unique id
	//	 * as well as dependencies.
	//	 * @param loadDependencies if we also shall load the dependencies
	//	 * @param revision the revision to load the resource of
	//	 */
	//	private static void load(Scene scene, IResource resource, boolean loadDependencies, int revision) {
	//		load(scene, resource.getId(), resource.getClass(), loadDependencies, revision);
	//	}

	//	/**
	//	 * Loads a resource. Included in these are in general resources
	//	 * that the user can add and remove. E.g. all actor, definitions, levels, etc.
	//	 * Loads the latest revision
	//	 * @param scene the scene to load the resource to
	//	 * @param resourceId the unique id of the resource we want to load
	//	 * @param type the class type of the resource
	//	 * @param loadDependencies if we also shall load the dependencies
	//	 * @throws UndefinedResourceTypeException
	//	 */
	//	public static void load(Scene scene, UUID resourceId, Class<?> type, boolean loadDependencies) throws UndefinedResourceTypeException {
	//		load(scene, resourceId, type, loadDependencies, -1);
	//	}

	/**
	 * Loads a resource. Included in these are in general resources
	 * that the user can add and remove. E.g. all actor, definitions, levels, etc.
	 * @param scene the scene to load the resource to
	 * @param resourceId the unique id of the resource we want to load
	 * @param type the class type of the resource
	 * @param revision loads the specific revision of the resource
	 * @param loadDependencies if we also shall load the dependencies
	 */
	public static void load(Scene scene, UUID resourceId, Class<? extends IResource> type, int revision, boolean loadDependencies) {
		if (loadDependencies) {
			// Type need to implement the IResourceDependency interface to load resources
			if (IResourceDependency.class.isAssignableFrom(type)) {
				mDependencyLoader.load(scene, resourceId, type, revision);
			} else {
				Gdx.app.error("ResourceCacheFacade", "Tried to load dependencies of a class that doesn't hold dependencies!");
			}
		} else {
			ResourceDatabase.load(scene, resourceId, type, revision);
		}
	}

	/**
	 * Reloads a loaded resource. This reloads the resource directly by calling {@link #finishLoading()}
	 * Useful when a resource is saved (and thus the revision increased, but we want the loaded resource to
	 * use the real revision (i.e. not the changed one). This method reloads the resource for all scenes.
	 * Does nothing if the resource isn't loaded. Only applicable on resources that has revisions...
	 * @param resourceId resource id to reload
	 * @param revision specific revision of the resource to reload.
	 */
	public static void reload(UUID resourceId, int revision) {
		ResourceDatabase.reload(resourceId, revision);
	}

	//	/**
	//	 * Reloads a resource to the specified resource
	//	 * @param scene the scene to reload the resource in
	//	 * @param resourceId id of the resource to reload to another revision
	//	 * @param revision the revision to reload the resource to
	//	 * @param reloadDependencies if all dependencies should be reloaded
	//	 */
	//	public static void reload(Scene scene, UUID resourceId, int revision, boolean reloadDependencies) {
	//		// reload
	//	}

	/**
	 * !!!NOT IMPLEMENTED!!! Unloads all <b>External</b> resources for the specified scene.
	 * @param scene the scene to unload all external resources from
	 */
	public static void unloadAllSceneResources(Scene scene) {
		/** @todo implement unload all scene resources, but to do this all ResourceNames needs to be
		 * saved into scenes too... Why? Because some resources will load dependencies, thus they will not
		 * unload ResourceNames resource */
		Gdx.app.error("ResourceCacheFacade", "unloadAllSceneResources() not implemented!");
		throw new GdxRuntimeException("unloadAllSceneResources() not implemented!");
	}

	/**
	 * Unloads a definition. This will automatically unload the correct revision
	 * @param scene the scene the resource was loaded into
	 * @param resource the resource to unload
	 * @param unloadDependencies if we shall unload the dependencies
	 */
	public static void unload(Scene scene, IResource resource, boolean unloadDependencies) {
		if (unloadDependencies) {
			// Type need to implement the IResourceDependency interface to load resources
			if (resource instanceof IResourceDependency) {
				mDependencyLoader.unload(scene, (IResourceDependency) resource);
			} else {
				Gdx.app.error("ResourceCacheFacade", "Tried to unload dependencies of a class that doesn't hold dependencies!");
			}
		} else {
			ResourceDatabase.unload(scene, resource);
		}
	}

	/**
	 * Unloads a resource (including its definition and dependencies)
	 * @param scene the scene the resource was loaded into
	 * @param resource the resource to unload
	 * @param resourceDef the resource definition
	 */
	public static void unload(Scene scene, Resource resource, Def resourceDef) {
		ResourceDatabase.unload(scene, resource);
		mDependencyLoader.unload(scene, resourceDef);
	}

	/**
	 * Get a resource based on the id and class of resource. Always gets the latest revision
	 * @param <ResourceType> type of resource that will be returned
	 * @param scene the scene the resource was loaded in, if null it will use the
	 * current active scene.
	 * @param resourceId id of the resource, can be both def and instance resource
	 * @param revision the revision of the resource to get
	 * @return the actual resource, null if not found
	 */
	public static <ResourceType extends IResource> ResourceType get(Scene scene, UUID resourceId, int revision) {
		Scene sceneToUse = scene;
		if (scene == null) {
			sceneToUse = SceneSwitcher.getActiveScene(true);
		}
		return ResourceDatabase.getLoadedResource(sceneToUse, resourceId, revision);
	}

	/**
	 * Returns all of the specified resource type
	 * @Precondition the resources have been loaded
	 * @param <ResourceType> the resource type that will be returned
	 * @param scene the scene the resources was loaded in, if null it will use the
	 * current active scene.
	 * @param type resource type that will be returned
	 * @return array with all the resources of that type. Don't forget to free the arraylist
	 * using Pools.arrayList.free(resources).
	 */
	public static <ResourceType extends IResource> ArrayList<ResourceType> getAll(Scene scene, Class<ResourceType> type) {
		Scene sceneToUse = scene;
		if (scene == null) {
			sceneToUse = SceneSwitcher.getActiveScene(true);
		}
		return ResourceDatabase.getAllLoadedSceneResourceOf(sceneToUse, type);
	}

	/**
	 * Checks whether a resource has been loaded or not
	 * @param scene the scene which is has been loaded in, if null it will use the
	 * current active scene.
	 * @param resourceId unique id of the object to test if it's loaded
	 * @param revision the revision to check if it's loaded. If the resource doesn't use a revision
	 * this parameter won't be used.
	 * @return true if the object has been loaded
	 */
	public static boolean isLoaded(Scene scene, UUID resourceId, int revision) {
		Scene sceneToUse = scene;
		if (scene == null) {
			sceneToUse = SceneSwitcher.getActiveScene(true);
		}
		return ResourceDatabase.isResourceLoaded(sceneToUse, resourceId, revision);
	}

	// -----------------------------
	// Resource names
	// -----------------------------
	/**
	 * Unloads a regular resource
	 * @param resourceName the name of the resource
	 */
	public static void unload(ResourceNames resourceName) {
		mAssetManager.unload(resourceName.getFilePath());
	}

	/**
	 * Loads a resources of static type. Usually those in internal assets,
	 * such as textures, music, etc.
	 * @param resource the name of the resource to load
	 * Texture, Music, etc.
	 */
	public static void load(ResourceNames resource) {
		String fullPath = null;
		try {
			fullPath = resource.getFilePath();
		} catch (UndefinedResourceTypeException e) {
			Gdx.app.error("UndefinedType", "Undefined resource type for a resource name. This should NEVER happen");
		}
		mAssetManager.load(fullPath, resource.type);
	}

	/**
	 * Get an internal resource of the specified type
	 * @param <ResourceType> the type to be returned
	 * @param resource the resource to return
	 * @return the actual resource
	 */
	@SuppressWarnings("unchecked")
	public static <ResourceType> ResourceType get(ResourceNames resource) {
		String fullPath = null;
		try {
			fullPath = resource.getFilePath();
		} catch (UndefinedResourceTypeException e) {
			Gdx.app.error("UndefinedType", "Undefined resource type for a resource name. This should NEVER happen");
		}
		return (ResourceType) mAssetManager.get(fullPath, resource.type);
	}

	/**
	 * Checks whether a resource has been loaded or not
	 * @param resource the resource to check if it has been loaded
	 * @return true if the resource has been loaded
	 */
	public static boolean isLoaded(ResourceNames resource) {
		String fullPath = null;
		try {
			fullPath = resource.getFilePath();
		} catch (UndefinedResourceTypeException e) {
			Gdx.app.error("ResourceCacheFacade", "Undefined resource type for a resource name. This should NEVER happen");
		}
		return mAssetManager.isLoaded(fullPath, resource.type);
	}

	/**
	 * Checks if everything has loaded and can be used.
	 * @return true if everything has been loaded
	 * @throws UndefinedResourceTypeException
	 */
	public static boolean update() throws UndefinedResourceTypeException {
		boolean fullyLoaded = true;
		try {
			try {
				if (!ResourceDatabase.update()) {
					fullyLoaded = false;
				}
				if (!mDependencyLoader.update()) {
					fullyLoaded = false;
				}
				if (!mAssetManager.update()) {
					fullyLoaded = false;
				}
				if (fullyLoaded && !mLoadQueue.isEmpty()) {
					fullyLoaded = false;
					ResourceItem toLoad = mLoadQueue.removeFirst();
					ResourceDatabase.load(toLoad.scene, toLoad.id, toLoad.type, toLoad.revision);
				}
			} catch (UndefinedResourceTypeException e) {
				mLoadQueue.clear();
				throw e;
			}
		} catch (GdxRuntimeException e) {
			if (e.getCause() != null && e.getCause().getCause() != null && e.getCause().getCause().getCause() != null) {
				Throwable source = e.getCause().getCause().getCause();
				Class<?> type = source.getClass();
				if (type == ResourceNotFoundException.class) {
					throw new ResourceNotFoundException(source.getMessage());
				} else if (type == ResourceCorruptException.class) {
					throw new ResourceCorruptException(source.getMessage());
				} else {
					throw e;
				}
			} else {
				throw e;
			}
		}

		return fullyLoaded;
	}

	/**
	 * Waits for the cache to finish loading all files into the cache. I.e.
	 * blocks this thread
	 * @throws UndefinedResourceTypeException
	 */
	public static void finishLoading() throws UndefinedResourceTypeException {
		while (!update()) {
			// Does nothing
		}
	}

	/**
	 * Checks how many resources are loaded
	 * @return number of loaded resources
	 */
	public static int getLoadedCount() {
		return mAssetManager.getLoadedAssets();
	}

	/**
	 * Checks how many of the specific resource there exists
	 * @param resourceType type of resource to count
	 * @return number of existing resources. Note the resources don't have to be
	 * loaded. It checks all resources of the type, whether loaded or not.
	 */
	public static int getExistingResourcesCount(Class<? extends IResource> resourceType) {
		try {
			String resourceDirPath = ResourceNames.getDirPath(resourceType);

			FileHandle resourceDir = Gdx.files.external(resourceDirPath);

			if (resourceDir.exists() && resourceDir.isDirectory()) {
				return resourceDir.list().length;
			}

		} catch (UndefinedResourceTypeException e) {
			Gdx.app.error("ResourceCacheFacade", e.toString());
		}

		return 0;
	}

	/**
	 * Disposes all the resources allocated.
	 */
	public static void dispose() {
		if (Debug.DEBUG_TESTS) {
			// All assets should be unloaded by now
			if (mAssetManager.getLoadedAssets() > 0) {
				Gdx.app.error("Assets", "All assets have not been unloaded!");
				Gdx.app.error("LoadedResource", "\n" + ResourceDatabase.getAllLoadedResourcesString());
				Gdx.app.error("Assets", "\n" + mAssetManager.getDiagnostics());
			}
		}

		mDependencyLoader.dispose();
		mAssetManager.dispose();
		mLoadQueue.clear();
	}

	/**
	 * Private constructor to enforce that no instance can be created from this
	 * class.
	 */
	private ResourceCacheFacade() {}

	/**
	 * Resource loader. This directly takes care of internal resources� it can load
	 * directly, no need to go through ResourceDependencyLoader. All Defs needs
	 * to be loaded via the ResourceDependencyLoader. A level is a special case
	 * and is also loaded directly via this manager, but still needs to load
	 * its LevelDef through ResourceDependencyLoader first. All resources
	 * can be directly accessed through this manager once loaded.
	 */
	private static AssetManager mAssetManager = null;

	/**
	 * Handles loading all dependencies.
	 */
	private static ResourceDependencyLoader mDependencyLoader = null;

	/**
	 * This queue is for loading resources (or rather instances of defs). However
	 * all defs needs to be loaded first which is done recursively while loading.
	 * The current only way to know if all resources have been loaded for a instance
	 * is to wait until the asset manager have loaded everything and then load the
	 * instances from the queue.
	 */
	private static LinkedList<ResourceItem> mLoadQueue = new LinkedList<ResourceItem>();
}

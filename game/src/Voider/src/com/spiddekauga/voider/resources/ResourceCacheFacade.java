package com.spiddekauga.voider.resources;

import java.util.LinkedList;
import java.util.List;
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
import com.spiddekauga.voider.scene.Scene;


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
	 * Loads all latest revision for the resources of the specified type
	 * @param scene the scene to load all resources for
	 * @param type the type of resource to load
	 * @param loadDependencies Set to true if the cache shall load all the files
	 * dependencies. E.g. For ActorDef it has some textures, maybe particle
	 * effects, etc. If loadDependencies are set to true the cache will
	 * load these dependencies too.
	 * @throws UndefinedResourceTypeException
	 */
	public static void loadAllOf(Scene scene, Class<?> type, boolean loadDependencies) throws UndefinedResourceTypeException {
		// TODO
	}

	/**
	 * Unloads all of the specified resources
	 * @param scene the scene to unload all the resource from
	 * @param type the type of resource to unload
	 * @param unloadDependencies true if the cache shall unload all the dependencies.
	 * @throws UndefinedResourceTypeException
	 */
	public static void unloadAllOf(Scene scene, Class<?> type, boolean unloadDependencies) throws UndefinedResourceTypeException {
		// TODO
		//		String dirPath = ResourceNames.getDirPath(type);
		//
		//		// Get all resource files
		//		FileHandle dir = Gdx.files.external(dirPath);
		//		if (!dir.exists() || !dir.isDirectory()) {
		//			return;
		//		}
		//
		//		FileHandle[] files = dir.list();
		//		// Only unload the resource, no dependencies
		//		if (!unloadDependencies) {
		//			for (FileHandle file : files) {
		//				if (mAssetManager.isLoaded(file.path())) {
		//					mAssetManager.unload(file.path());
		//				}
		//			}
		//		}
		//		// Unload dependencies too
		//		else {
		//			for (FileHandle file : files) {
		//				if (mAssetManager.isLoaded(file.path())) {
		//					mDependencyLoader.unload((Def) mAssetManager.get(file.path(), type));
		//				}
		//			}
		//		}
	}

	/**
	 * @return true if the resource cache facade is currently loading
	 */
	public static boolean isLoading() {
		return mAssetManager.getQueuedAssets() > 0 || mDependencyLoader.isLoading();
	}

	/**
	 * Loads the resource, definition and all dependencies.
	 * @param scene the scene to load the resource to
	 * @param resourceId the id of the resource we're loading (i.e. not the
	 * definition's id).
	 * @param resourceType the class of the resource to load
	 * @param def the definition of the resource we're loading
	 * @param revision the revision of the resource to load
	 * @throws UndefinedResourceTypeException
	 */
	public static void load(Scene scene, UUID resourceId, Class<?> resourceType, Def def, int revision) throws UndefinedResourceTypeException {
		load(scene, def, true, revision);

		// Add the resource to the queue. Load this resource once all dependencies are loaded
		mLoadQueue.add(new ResourceItem(resourceId, resourceType, revision));
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

	/**
	 * Loads an external definition. Included in these are in general resources
	 * that the user can add and remove. E.g. ActorDef, WeaponDef, etc.
	 * @param scene the scene to load the resource to
	 * @param resource the resource we want to load. This includes a unique id
	 * as well as dependencies.
	 * @param loadDependencies if we also shall load the dependencies
	 * @param revision the revision to load the resource of
	 * @throws UndefinedResourceTypeException
	 */
	private static void load(Scene scene, IResource resource, boolean loadDependencies, int revision) throws UndefinedResourceTypeException {
		load(scene, resource.getId(), resource.getClass(), loadDependencies, revision);
	}

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
	 * @param loadDependencies if we also shall load the dependencies
	 * @param revision loads the specific revision of the resource
	 * @throws UndefinedResourceTypeException
	 */
	public static void load(Scene scene, UUID resourceId, Class<?> type, boolean loadDependencies, int revision) throws UndefinedResourceTypeException {
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
	 * Unloads all <b>External</b> resources for the specified scene
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
	 * @param scene the scene the resource was loaded in
	 * @param resourceId id of the resource, can be both def and instance resource
	 * @param revision the revision of the resource to get
	 * @return the actual resource, null if not found
	 */
	public static <ResourceType> ResourceType get(Scene scene, UUID resourceId, int revision) {
		return ResourceDatabase.getLoadedResource(scene, resourceId, revision);
	}

	/**
	 * Returns all of the specified resource type
	 * @Precondition the resources have been loaded
	 * @param <ResourceType> the resource type that will be returned
	 * @param scene the scene the resources was loaded in
	 * @param type resource type that will be returned
	 * @return array with all the resources of that type
	 */
	public static <ResourceType> List<ResourceType> getAll(Scene scene, Class<ResourceType> type) {
		// TODO get all loaded resources of the specified type
		return null;
	}

	/**
	 * Checks whether a resource has been loaded or not
	 * @param scene the scene which is has been loaded in
	 * @param resourceId unique id of the object to test if it's loaded
	 * @param revision the revision to check if it's loaded. If the resource doesn't use a revision
	 * this parameter won't be used.
	 * @return true if the object has been loaded
	 */
	public static boolean isLoaded(Scene scene, UUID resourceId, int revision) {
		return ResourceDatabase.isResourceLoaded(scene, resourceId, revision);
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
				else if (!mLoadQueue.isEmpty()) {
					fullyLoaded = false;
					ResourceItem toLoad = mLoadQueue.removeFirst();
					mAssetManager.load(toLoad.fullName, toLoad.resourceType);
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
		if (Config.DEBUG_TESTS) {
			// All assets should be unloaded by now
			if (mAssetManager.getLoadedAssets() > 0) {
				Gdx.app.error("Assets", "All assets have not been unloaded!");
				Gdx.app.error("Assets", mAssetManager.getDiagnostics());
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

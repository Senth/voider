package com.spiddekauga.voider.resources;

import java.util.ArrayList;
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
import com.spiddekauga.voider.game.actors.BossActorDef;
import com.spiddekauga.voider.game.actors.BulletActorDef;
import com.spiddekauga.voider.game.actors.EnemyActorDef;
import com.spiddekauga.voider.game.actors.PickupActorDef;
import com.spiddekauga.voider.game.actors.PlayerActorDef;
import com.spiddekauga.voider.game.actors.StaticTerrainActorDef;


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

		// Own loaders
		mAssetManager.setLoader(BossActorDef.class, new JsonLoaderAsync<BossActorDef>(new ExternalFileHandleResolver(), BossActorDef.class));
		mAssetManager.setLoader(BulletActorDef.class, new JsonLoaderAsync<BulletActorDef>(new ExternalFileHandleResolver(), BulletActorDef.class));
		mAssetManager.setLoader(EnemyActorDef.class, new JsonLoaderAsync<EnemyActorDef>(new ExternalFileHandleResolver(), EnemyActorDef.class));
		mAssetManager.setLoader(PickupActorDef.class, new JsonLoaderAsync<PickupActorDef>(new ExternalFileHandleResolver(), PickupActorDef.class));
		mAssetManager.setLoader(PlayerActorDef.class, new JsonLoaderAsync<PlayerActorDef>(new ExternalFileHandleResolver(), PlayerActorDef.class));
		mAssetManager.setLoader(StaticTerrainActorDef.class, new JsonLoaderAsync<StaticTerrainActorDef>(new ExternalFileHandleResolver(), StaticTerrainActorDef.class));
		mAssetManager.setLoader(LevelDef.class, new JsonLoaderAsync<LevelDef>(new ExternalFileHandleResolver(), LevelDef.class));
		mAssetManager.setLoader(Level.class, new JsonLoaderAsync<Level>(new ExternalFileHandleResolver(), Level.class));
		mAssetManager.setLoader(ShaderProgram.class, new ShaderLoader(new InternalFileHandleResolver()));
		mAssetManager.setLoader(GameSave.class, new JsonLoaderSync<GameSave>(new ExternalFileHandleResolver(), GameSave.class));
		mAssetManager.setLoader(GameSaveDef.class, new JsonLoaderAsync<GameSaveDef>(new ExternalFileHandleResolver(), GameSaveDef.class));

		// Existing loaders
		mAssetManager.setLoader(Skin.class, new SkinLoader(new InternalFileHandleResolver()));

		mDependencyLoader = new ResourceDependencyLoader(mAssetManager);
	}

	/**
	 * Loads all resources of the specified type
	 * @param <ResourceType> the type of resource to load
	 * @param type the type of resource to load
	 * @param loadDependencies Set to true if the cache shall load all the files
	 * dependencies. E.g. For ActorDef it has some textures, maybe particle
	 * effects, etc. If loadDependencies are set to true the cache will
	 * load these dependencies too.
	 * @throws UndefinedResourceTypeException
	 */
	public static <ResourceType> void loadAllOf(Class<ResourceType> type, boolean loadDependencies) throws UndefinedResourceTypeException {
		String dirPath = ResourceNames.getDirPath(type);

		// Get all resource files
		FileHandle dir = Gdx.files.external(dirPath);
		if (!dir.exists() || !dir.isDirectory()) {
			return;
		}

		FileHandle[] files = dir.list();
		// Only load the resource, no dependencies
		// Skip filenames that aren't fully UUIDs, e.g. backup files
		if (!loadDependencies) {
			for (FileHandle file : files) {
				// Skip files with extension (these can be backup files etc)
				if (file.extension().length() == 0) {
					// Is it really an UUID?
					try {
						UUID.fromString(file.name());
						mAssetManager.load(file.path(), type);
					} catch (IllegalArgumentException e) {
						Gdx.app.error("ResourceCacheFacade", "File is not resource: " + file.path());
					}
				}
			}
		}
		// Load dependencies too, note dependency loader will load this resource too. Thus
		// No call to asset manager should be done from this class.
		else {
			for (FileHandle file : files) {
				// Skif files with extension (these can be backup files etc)
				if (file.extension().length() == 0) {
					try {
						UUID uuid = UUID.fromString(file.name());
						mDependencyLoader.load(uuid, type);
					} catch (IllegalArgumentException e) {
						Gdx.app.error("ResourceCacheFacade", "File is not resource: " + file.path());
					}
				}
			}
		}
	}

	/**
	 * Unloads all of the specified resources
	 * @param <ResourceType> the type of resource to unload
	 * @param type the type of resource to unload
	 * @param unloadDependencies true if the cache shall unload all the dependencies.
	 * @throws UndefinedResourceTypeException
	 */
	public static <ResourceType> void unloadAllOf(Class<ResourceType> type, boolean unloadDependencies) throws UndefinedResourceTypeException {
		String dirPath = ResourceNames.getDirPath(type);

		// Get all resource files
		FileHandle dir = Gdx.files.external(dirPath);
		if (!dir.exists() || !dir.isDirectory()) {
			return;
		}

		FileHandle[] files = dir.list();
		// Only load the resource, no dependencies
		if (!unloadDependencies) {
			for (FileHandle file : files) {
				if (mAssetManager.isLoaded(file.path())) {
					mAssetManager.unload(file.path());
				}
			}
		}
		// Load dependencies too
		else {
			for (FileHandle file : files) {
				if (mAssetManager.isLoaded(file.path())) {
					mDependencyLoader.unload((Def) mAssetManager.get(file.path(), type));
				}
			}
		}
	}

	/**
	 * @return true if the resource cache facade is currently loading
	 */
	public static boolean isLoading() {
		return mAssetManager.getQueuedAssets() > 0 || mDependencyLoader.isLoading();
	}

	/**
	 * Loads the resource, definition and all dependencies.
	 * @param <ResourceType> Class of the resource that shall be loaded.
	 * @param resourceId the id of the resource we're loading (i.e. not the
	 * definition's id).
	 * @param resourceType the class of the resource to load
	 * @param def the definition of the resource we're loading
	 * @throws UndefinedResourceTypeException
	 */
	public static <ResourceType> void load(UUID resourceId, Class<ResourceType> resourceType, Def def) throws UndefinedResourceTypeException {
		load(def, true);

		// Add the level to the queue. Load this level once all dependencies are loaded
		mLoadQueue.add(new ResourceItem(resourceId, resourceType));
	}

	/**
	 * Loads an external definition. Included in these are in general resources
	 * that the user can add and remove. E.g. ActorDef, WeaponDef, etc.
	 * @param resource the resource we want to load. This includes a unique id
	 * as well as dependencies.
	 * @param loadDependencies if we also shall load the dependencies
	 * @throws UndefinedResourceTypeException
	 */
	public static void load(IResource resource, boolean loadDependencies) throws UndefinedResourceTypeException {
		load(resource.getId(), resource.getClass(), loadDependencies);
	}

	/**
	 * Loads a definition. Included in these are in general resources
	 * that the user can add and remove. E.g. all actor, definitions, levels, etc.
	 * @param <DefClass> Class of the definition to load
	 * @param defId the unique id of the resource we want to load
	 * @param type the class type of the resource
	 * @param loadDependencies if we also shall load the dependencies
	 * @throws UndefinedResourceTypeException
	 */
	public static <DefClass> void load(UUID defId, Class<DefClass> type, boolean loadDependencies) throws UndefinedResourceTypeException {
		if (loadDependencies) {
			// Type need to implement the IResourceDependency interface to load resources
			if (IResourceDependency.class.isAssignableFrom(type)) {
				mDependencyLoader.load(defId, type);
			} else {
				Gdx.app.error("ResourceCacheFacade", "Tried to load dependencies of a class that don't hold dependencies!");
			}
		} else {
			final String fullName = ResourceNames.getDirPath(type) + defId.toString();
			mAssetManager.load(fullName, type);
		}
	}

	/**
	 * Unloads a definition.
	 * @param resource the resource to unload
	 * @param unloadDependencies if we shall unload the dependencies
	 */
	public static void unload(IResource resource, boolean unloadDependencies) {
		if (unloadDependencies) {
			if (resource instanceof IResourceDependency) {
				mDependencyLoader.unload((IResourceDependency) resource);
			} else {
				Gdx.app.error("ResourceCacheFacade", "Tried to load dependencies of a class that don't hold dependencies!");
			}
		} else {
			try {
				final String fullName = ResourceNames.getDirPath(resource.getClass()) + resource.getId().toString();
				mAssetManager.unload(fullName);
			} catch (UndefinedResourceTypeException e) {
				Gdx.app.error("Unknown resource type", "Should never happen when unloading");
			}
		}
	}

	/**
	 * Unloads a resource (its definition and dependencies)
	 * @param resource the resource to unload
	 * @param resourceDef the resource definition
	 */
	public static void unload(Resource resource, Def resourceDef) {
		try {
			final String fullName = ResourceNames.getDirPath(resource.getClass()) + resource.getId().toString();
			unload(resourceDef, true);
			mAssetManager.unload(fullName);
		} catch (UndefinedResourceTypeException e) {
			Gdx.app.error("Unknown resource type", "Should never happen when unloading");
		}
	}

	/**
	 * Unloads a regular resource
	 * @param resourceName the name of the resource
	 */
	public static void unload(ResourceNames resourceName) {
		mAssetManager.unload(resourceName.fullName);
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
			fullPath = ResourceNames.getDirPath(resource.type) + resource.filename;
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
			fullPath = ResourceNames.getDirPath(resource.type) + resource.filename;
		} catch (UndefinedResourceTypeException e) {
			Gdx.app.error("UndefinedType", "Undefined resource type for a resource name. This should NEVER happen");
		}
		return (ResourceType) mAssetManager.get(fullPath, resource.type);
	}

	/**
	 * Get a resource based on the id and class of resource
	 * @param <ResourceType> type of resource that will be returned
	 * @param resourceId id of the resource, can be both def and instance resource
	 * @param resourceType the class of the resource
	 * @return the actual resource
	 * @throws UndefinedResourceTypeException
	 */
	public static <ResourceType> ResourceType get(UUID resourceId, Class<ResourceType> resourceType) throws UndefinedResourceTypeException {
		final String fullPath = ResourceNames.getDirPath(resourceType) + resourceId.toString();
		return mAssetManager.get(fullPath, resourceType);
	}

	/**
	 * Returns all of the specified resource type
	 * @Precondition the resources have been loaded
	 * @param <ResourceType> the resource type that will be returned
	 * @param type resource type that will be returned
	 * @return array with all the resources of that type
	 * @throws UndefinedResourceTypeException
	 */
	public static <ResourceType> List<ResourceType> get(Class<ResourceType> type) throws UndefinedResourceTypeException {
		String dirPath = ResourceNames.getDirPath(type);

		ArrayList<ResourceType> resources = new ArrayList<ResourceType>();

		// Get all resource files
		FileHandle dir = Gdx.files.external(dirPath);
		if (dir.exists() && dir.isDirectory()) {
			FileHandle[] files = dir.list();

			for (FileHandle file : files) {
				if (mAssetManager.isLoaded(file.path())) {
					resources.add(mAssetManager.get(file.path(), type));
				}
			}
		}

		return resources;
	}

	/**
	 * Checks whether a resource has been loaded or not
	 * @param <ResourceType> type of the resource to check if it has been loaded
	 * @param uuid unique id of the object to test if it's loaded
	 * @param type the type of resource
	 * @return true if the object has been loaded
	 */
	public static <ResourceType> boolean isLoaded(UUID uuid, Class<ResourceType> type) {
		try {
			String fullPath = ResourceNames.getDirPath(type) + uuid.toString();
			return mAssetManager.isLoaded(fullPath, type);
		} catch (UndefinedResourceTypeException e) {
			return false;
		}
	}

	/**
	 * Checks whether a resource has been loaded or not
	 * @param resource the resource to check if it has been loaded
	 * @return true if the resource has been loaded
	 */
	public static boolean isLoaded(ResourceNames resource) {
		String fullPath = null;
		try {
			fullPath = ResourceNames.getDirPath(resource.type) + resource.filename;
		} catch (UndefinedResourceTypeException e) {
			Gdx.app.error("UndefinedType", "Undefined resource type for a resource name. This should NEVER happen");
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
	 * The currently only way to know if all resources have been loaded for a instance
	 * is to wait until the asset manager have loaded everything and then load the
	 * instances from the queue.
	 */
	private static LinkedList<ResourceItem> mLoadQueue = new LinkedList<ResourceItem>();
}

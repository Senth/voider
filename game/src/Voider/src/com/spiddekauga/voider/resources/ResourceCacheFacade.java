package com.spiddekauga.voider.resources;

import java.util.LinkedList;
import java.util.UUID;

import com.badlogic.gdx.assets.AssetManager;


/**
 * This class is responsible for caching assets/resources, or rather act as
 * a facade to other cashes. First the resource needs to be loaded (from file)
 * into the cache before it can be used. This can be done with the various load()
 * methods. To read (and get an object) from the cache, use one of the
 * get() methods. To unload cache use one of the appropriate unload() methods.
 * 
 * @see #ResourceSaver for how to save files
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class ResourceCacheFacade {
	/**
	 * Initializes the ResourceCacheFacade. This needs to be called before using any other
	 * method
	 */
	public void init() {
		mDependencyLoader = new ResourceDependencyLoader(mAssetManager);
	}

	/**
	 * Loads all resources of the specified type
	 * @param <ResourceType> Class of the type to load
	 * @param type the type of resource to load
	 * @param loadDependencies Set to true if the cache shall load all the files
	 * dependencies. E.g. For ActorDef it has some textures, maybe particle
	 * effects, etc. If loadDependencies are set to true the cache will
	 * load these dependencies too.
	 */
	public static <ResourceType> void loadAllOf(Class<ResourceType> type, boolean loadDependencies) {
		// TODO
	}

	/**
	 * Loads the resource including all dependencies.
	 * @param <ResourceType> Class of the resource that shall be loaded.
	 * @param resourceId the id of the resource we're loading (i.e. not the
	 * definition's id).
	 * @param resourceType the class of the resource to load
	 * @param def the definition of the resource we're loading
	 */
	public static <ResourceType> void load(UUID resourceId, Class<ResourceType> resourceType, Def def) {
		load(def, true);

		// Add the level to the queue. Load this level once all dependencies are loaded
		mLoadQueue.add(new QueueItem(resourceId, resourceType));
	}

	/**
	 * Loads an external definition. Included in these are in general resources
	 * that the user can add and remove. E.g. ActorDef, WeaponDef, etc.
	 * @param def the resource we want to load. This includes a unique id
	 * as well as dependencies.
	 * @param loadDependencies if we also shall load the dependencies
	 */
	public static void load(Def def, boolean loadDependencies) {
		load(def.getId(), def.getClass(), loadDependencies);
	}

	/**
	 * Loads a resource of dynamic type. Included in these are in general resources
	 * that the user can add and remove. E.g. all actor, definitions, levels, etc.
	 * @param <DefClass> Class of the definition to load
	 * @param defId the unique id of the resource we want to load
	 * @param type the class type of the resource
	 * @param loadDependencies if we also shall load the dependencies
	 */
	public static <DefClass> void load(UUID defId, Class<DefClass> type, boolean loadDependencies) {
		if (loadDependencies) {
			mDependencyLoader.load(defId, type);
		} else {
			final String fullName = ResourceNames.getDirPath(type) + defId.toString();
			mAssetManager.load(fullName, type);
		}
	}

	/**
	 * Loads a resources of static type. Usually those in internal assets,
	 * such as textures, music, etc.
	 * @param resource the name of the resource to load
	 * Texture, Music, etc.
	 */
	public static void load(ResourceNames resource) {
		final String fullPath = ResourceNames.getDirPath(resource.type) + resource.filename;
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
		final String fullPath = ResourceNames.getDirPath(resource.type) + resource.filename;
		return (ResourceType) mAssetManager.get(fullPath, resource.type);
	}

	/**
	 * Get a resource based on the id and class of resource
	 * @param <ResourceType> type of resource that will be returned
	 * @param resourceId id of the resource, can be both def and instance resource
	 * @param resourceType the class of the resource
	 * @return the actual resource
	 */
	public static <ResourceType> ResourceType get(UUID resourceId, Class<ResourceType> resourceType) {
		final String fullPath = ResourceNames.getDirPath(resourceType) + resourceId.toString();
		return mAssetManager.get(fullPath, resourceType);
	}

	/**
	 * Checks if everything has loaded and can be used.
	 * @return true if everything has been loaded
	 */
	public static boolean update() {
		boolean fullyLoaded = true;
		if (!mDependencyLoader.update()) {
			fullyLoaded = false;
		}
		if (!mAssetManager.update()) {
			fullyLoaded = false;
		}
		return fullyLoaded;
	}

	/**
	 * Waits for the cache to finish loading all files into the cache. I.e.
	 * blocks this thread
	 */
	public static void finishLoading() {
		while (!update()) {
			// Does nothing
		}
	}


	/**
	 * Private constructor to enforce that no instance can be created from this
	 * class.
	 */
	private ResourceCacheFacade() {}

	/**
	 * Resource loader. This directly takes care of internal resourcesâ it can load
	 * directly, no need to go through ResourceDependencyLoader. All Defs needs
	 * to be loaded via the ResourceDependencyLoader. A level is a special case
	 * and is also loaded directly via this manager, but still needs to load
	 * its LevelDef through ResourceDependencyLoader first. All resources
	 * can be directly accessed through this manager once loaded.
	 */
	private static AssetManager mAssetManager = new AssetManager();

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
	private static LinkedList<QueueItem> mLoadQueue = new LinkedList<QueueItem>();
}

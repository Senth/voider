package com.spiddekauga.voider.repo.resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.spiddekauga.voider.Config.Debug;
import com.spiddekauga.voider.resources.IResource;
import com.spiddekauga.voider.resources.InternalDeps;
import com.spiddekauga.voider.resources.Resource;
import com.spiddekauga.voider.resources.ResourceException;
import com.spiddekauga.voider.resources.ResourceItem;
import com.spiddekauga.voider.scene.Scene;


/**
 * This class is responsible for caching assets/resources, or rather act as a facade to
 * other cashes. First the resource needs to be loaded (from file) into the cache before
 * it can be used. This can be done with the various load() methods. To read (and get an
 * object) from the cache, use one of the get() methods. To unload cache use one of the
 * appropriate unload() methods.
 * @see ResourceRepo for how to save files
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class ResourceCacheFacade {
	/**
	 * Loads all latest revision for the resources of the specified type. Can also be used
	 * to load all non-loaded resources if called again. E.g. if a resource has been
	 * created after the first call to loadAllOf()...
	 * @param scene the scene to load all resources for
	 * @param type the type of resource to load
	 * @param loadDependencies Set to true ot load all file dependencies of the resource.
	 *        E.g. some ActorDef might have textures, particle effects, sound, bullets as
	 *        dependencies. In this case these will also be loaded.
	 */
	public static void loadAllOf(Scene scene, ExternalTypes type, boolean loadDependencies) {
		// Get all resources of this type
		ArrayList<UUID> resources = ResourceLocalRepo.getAll(type);


		// Load dependencies
		if (loadDependencies) {
			for (UUID resourceId : resources) {
				mDependencyLoader.load(scene, resourceId, -1);
			}
		}
		// Only load them, no dependencies
		else {
			for (UUID resourceId : resources) {
				mResourceLoader.load(scene, resourceId, -1);
			}
		}
	}

	/**
	 * Unload all resources that were loaded into the specified scene
	 * @param scene the scene the resources were loaded into
	 */
	public static void unload(Scene scene) {
		mResourceLoader.unload(scene);
	}

	/**
	 * @return true if the resource cache facade is currently loading
	 */
	public static boolean isLoading() {
		return mAssetManager.getQueuedAssets() > 0 || mDependencyLoader.isLoading() || mResourceLoader.isLoading();
	}

	/**
	 * Loads the resource, definition and all dependencies.
	 * @param scene the scene to load the resource to
	 * @param resourceId the id of the resource we're loading (i.e. not the definition's
	 *        id).
	 * @param defId the definition of the resource we're loading
	 * @param revision the revision of the resource to load
	 */
	public static void load(Scene scene, UUID resourceId, UUID defId, int revision) {
		// Load definition dependencies first
		load(scene, defId, true, revision);

		// Add the resource to the queue. Load this resource once all dependencies are
		// loaded
		mLoadQueue.add(new ResourceItem(scene, resourceId, revision));
	}

	/**
	 * Loads the latest revision of both resource, definition and all dependencies.
	 * @param scene the scene to load the resource to
	 * @param resourceId the id of the resource we're loading (i.e. not the definition's
	 *        id).
	 * @param defId the definition of the resource we're loading
	 */
	public static void load(Scene scene, UUID resourceId, UUID defId) {
		load(scene, resourceId, defId, -1);
	}

	/**
	 * Loads a resource. Included in these are in general resources that the user can add
	 * and remove. E.g. all actor, definitions, levels, etc.
	 * @param scene the scene to load the resource to
	 * @param resourceId the unique id of the resource we want to load
	 * @param loadDependencies if we also shall load the dependencies
	 * @param revision loads the specific revision of the resource
	 */
	public static void load(Scene scene, UUID resourceId, boolean loadDependencies, int revision) {
		if (loadDependencies) {
			mDependencyLoader.load(scene, resourceId, revision);
		} else {
			mResourceLoader.load(scene, resourceId, revision);
		}
	}

	/**
	 * Loads the latest revision of this resource. Included in these are in general
	 * resources that the user can add and remove. E.g. all actor, definitions, levels,
	 * etc.
	 * @param scene the scene to load the resource to
	 * @param resourceId the unique id of the resource we want to load
	 * @param loadDependencies if we also shall load the dependencies
	 */
	public static void load(Scene scene, UUID resourceId, boolean loadDependencies) {
		load(scene, resourceId, loadDependencies, -1);
	}

	/**
	 * Sets the latest resource to the specified resource
	 * @param resource the resource to be set as latest resource
	 * @param oldRevision old revision that the resource was loaded into
	 */
	public static void setLatestResource(Resource resource, int oldRevision) {
		mResourceLoader.setLatestResource(resource, oldRevision);
	}

	/**
	 * Reloads a resource. Useful when changing updating resources and we don't want to
	 * restart the program to test them.
	 * @param resource the resource to reload
	 * @note <b>Use with care!</b> This will simply reload the resource, meaning all other
	 *       previous instances that uses the old resource won't work.
	 */
	public static void reload(InternalNames resource) {
		String filepath = resource.getFilePath();

		// Reload the actual asset
		if (filepath != null && mAssetManager.isLoaded(filepath)) {
			int cRefs = mAssetManager.getReferenceCount(filepath);
			mAssetManager.setReferenceCount(filepath, 1);
			mAssetManager.unload(filepath);
			mAssetManager.load(filepath, resource.getType());
			mAssetManager.finishLoading();
			mAssetManager.setReferenceCount(filepath, cRefs);
		}
	}

	/**
	 * Reloads a resource. Useful when changing updating resources and we don't want to
	 * restart the program to test them.
	 * @param resource the resource to reload
	 * @note <b>Use with care!</b> This will simply reload the resource, meaning all other
	 *       previous instances that uses the old resource won't work.
	 */
	public static void reload(InternalDeps resource) {
		InternalNames[] names = resource.getDependencies();

		for (int i = 0; i < names.length; i++) {
			reload(names[i]);
		}
	}

	/**
	 * Reloads the latest resource. Useful when a new revision has been added of the
	 * resource during sync.
	 * @param resourceId id of the resource to reload
	 */
	public static void reload(UUID resourceId) {
		mResourceLoader.reload(resourceId);
	}

	/**
	 * Get a resource based on the id and class of resource. Always gets the latest
	 * revision
	 * @param resourceId id of the resource, can be both def and instance resource
	 * @param revision the specific revision of the resource to get
	 * @param <ResourceType> type of resource that will be returned
	 * @return the actual resource, null if not found
	 */
	public static <ResourceType extends IResource> ResourceType get(UUID resourceId, int revision) {
		return mResourceLoader.getLoadedResource(resourceId, revision);
	}

	/**
	 * Get a resource based on the id and class of resource. Always gets the latest
	 * revision
	 * @param resourceId id of the resource, can be both def and instance resource
	 * @param <ResourceType> type of resource that will be returned
	 * @return the actual resource, null if not found
	 */
	public static <ResourceType extends IResource> ResourceType get(UUID resourceId) {
		return mResourceLoader.getLoadedResource(resourceId);
	}

	/**
	 * Returns all of the specified resource type
	 * @Precondition the resources have been loaded
	 * @param <ResourceType> the resource type that will be returned
	 * @param type resource type that will be returned
	 * @return array with all the resources of that type. Don't forget to free the
	 *         arraylist using Pools.arrayList.free(resources).
	 */
	public static <ResourceType extends IResource> ArrayList<ResourceType> getAll(ExternalTypes type) {
		return mResourceLoader.getAllLoadedResourcesOf(type);
	}

	/**
	 * Checks whether a resource has been loaded into the specified scene. Uses the latest
	 * revision
	 * @param resourceId unique id of the object to test if it's loaded
	 * @param scene if the resource has been loaded into this scene
	 * @return true if the object has been loaded
	 */
	public static boolean isLoaded(UUID resourceId, Scene scene) {
		return mResourceLoader.isResourceLoaded(resourceId, scene);
	}

	/**
	 * Checks whether a resource has been loaded or not. Uses the latest revision
	 * @param resourceId unique id of the object to test if it's loaded
	 * @return true if the object has been loaded
	 */
	public static boolean isLoaded(UUID resourceId) {
		return mResourceLoader.isResourceLoaded(resourceId);
	}

	/**
	 * Checks whether a resource has been loaded or not
	 * @param resourceId unique id of the object to test if it's loaded
	 * @param revision the revision to test if it's loaded
	 * @return true if the object has been loaded
	 */
	public static boolean isLoaded(UUID resourceId, int revision) {
		return mResourceLoader.isResourceLoaded(resourceId, revision);
	}

	/**
	 * Unload a specific resource. Does nothing if the resource isn't loaded
	 * @param resourceId unique id of the object to unload
	 */
	public static void unload(UUID resourceId) {
		mResourceLoader.unload(resourceId);
	}

	/**
	 * Unload a specific resource. Does nothing if the resource isn't loaded
	 * @param resourceId unique id of the object to unload
	 * @param revision the specific revision to unload
	 */
	public static void unload(UUID resourceId, int revision) {
		mResourceLoader.unload(resourceId, revision);
	}

	// -----------------------------
	// Resource names
	// -----------------------------
	/**
	 * Unload all internal dependencies
	 * @param internalDeps
	 */
	public static void unload(InternalDeps... internalDeps) {
		for (InternalDeps internalDep : internalDeps) {
			for (InternalNames internalName : internalDep.getDependencies()) {
				unload(internalName);
			}
		}
	}

	/**
	 * Unloads a regular resource
	 * @param resourceName the name of the resource
	 */
	public static void unload(InternalNames resourceName) {
		// Is this resource currently used?
		IResourceUnloadReady unloadReadyMethod = mUnloadReadyMethods.get(resourceName.getType());
		if (unloadReadyMethod != null) {
			mUnloadList.add(resourceName);
		}
		// Unload directly
		else {
			mAssetManager.unload(resourceName.getFilePath());
		}
	}

	/**
	 * Unload all of the specified resource dependency. This will only work correctly if
	 * the resources loaded by internalDep was loaded through this method. If any of them
	 * were loaded with {@link #load(InternalNames)} the program will eventually crash.
	 * @param internalDeps the internal dependencies to unload
	 * @return number of times the internalDep was loaded
	 */
	public static int[] unloadAll(InternalDeps... internalDeps) {
		int[] count = new int[internalDeps.length];
		for (int i = 0; i < internalDeps.length; i++) {
			count[i] = unloadAll(internalDeps[i]);
		}

		return count;
	}

	/**
	 * Unload all of the specified resource dependency. This will only work correctly if
	 * the resources loaded by internalDep was loaded through this method. If any of them
	 * were loaded with {@link #load(InternalNames)} the program will eventually crash.
	 * @param internalDeps the internal dependencies to unload
	 * @return number of times the internalDep was loaded
	 */
	public static int unloadAll(InternalDeps internalDeps) {
		int count = 0;
		for (InternalNames resourceName : internalDeps.getDependencies()) {
			count = unloadAll(resourceName);
		}

		return count;
	}

	/**
	 * Unload all of the specified resource
	 * @param resourceName name of the resource
	 * @return number of instance of resourceName
	 */
	public static int unloadAll(InternalNames resourceName) {
		int count = 0;

		if (isLoaded(resourceName)) {
			count = mAssetManager.getReferenceCount(resourceName.getFilePath());

			for (int i = 0; i < count; ++i) {
				// Is this resource currently used?
				IResourceUnloadReady unloadReadyMethod = mUnloadReadyMethods.get(resourceName.getType());
				if (unloadReadyMethod != null) {
					mUnloadList.add(resourceName);
				}
				// Unload directly
				else {
					mAssetManager.unload(resourceName.getFilePath());
				}
			}
		}

		return count;
	}

	/**
	 * Loads all internal dependencies
	 * @param internalDeps
	 */
	public static void load(InternalDeps... internalDeps) {
		for (InternalDeps internalDep : internalDeps) {
			for (InternalNames internalName : internalDep.getDependencies()) {
				load(internalName);
			}
		}
	}

	/**
	 * Loads a resources of static type. Usually those in internal assets, such as
	 * textures, music, etc.
	 * @param resource the name of the resource to load Texture, Music, etc.
	 */
	@SuppressWarnings("unchecked")
	public static void load(InternalNames resource) {
		// Remove from unload list if we're loading it again before it was unloaded
		if (mUnloadList.contains(resource)) {
			mUnloadList.remove(resource);
		}
		// Load it
		else {
			String fullPath = resource.getFilePath();
			mAssetManager.load(fullPath, resource.getType(), resource.getParameters());
		}
	}

	/**
	 * Load all internal dependencies cLoad number of times.
	 * @param internalDeps the dependencies to load
	 * @param cLoad how many times we will load the resource
	 */
	public static void load(InternalDeps[] internalDeps, int[] cLoad) {
		for (int i = 0; i < internalDeps.length; i++) {
			load(internalDeps[i], cLoad[i]);
		}
	}

	/**
	 * Load all internal dependencies cLoad number of times.
	 * @param internalDep the dependencies to load
	 * @param cLoad how many times we will load the resource
	 */
	public static void load(InternalDeps internalDep, int cLoad) {
		for (InternalNames resourceName : internalDep.getDependencies()) {
			load(resourceName, cLoad);
		}
	}

	/**
	 * Load the resource X number of times.
	 * @param resourceName the resource to load
	 * @param cLoad how many times we will load the resource
	 */
	public static void load(InternalNames resourceName, int cLoad) {
		for (int i = 0; i < cLoad; ++i) {
			load(resourceName);
		}
	}

	/**
	 * Get an internal resource of the specified type
	 * @param <ResourceType> the type to be returned
	 * @param resource the resource to return
	 * @return the actual resource, null if not loaded
	 */
	@SuppressWarnings("unchecked")
	public static <ResourceType> ResourceType get(InternalNames resource) {
		if (isLoaded(resource)) {
			String fullPath = resource.getFilePath();
			return (ResourceType) mAssetManager.get(fullPath, resource.getType());
		} else {
			return null;
		}
	}

	/**
	 * Get the first resource of the specified type
	 * @param <ResourceType> the type to be returned
	 * @param resource the resource to return
	 * @return the actual resource, null if not loaded
	 */
	public static <ResourceType> ResourceType get(InternalDeps resource) {
		InternalNames[] names = resource.getDependencies();

		if (names.length >= 1) {
			return get(names[0]);
		} else {
			return null;
		}
	}

	/**
	 * Checks whether a resource has been loaded or not
	 * @param resource the resource to check if it has been loaded
	 * @return true if the resource has been loaded
	 */
	public static boolean isLoaded(InternalNames resource) {
		String fullPath = resource.getFilePath();
		return mAssetManager.isLoaded(fullPath, resource.getType());
	}

	/**
	 * Checks all resource has been loaded or not
	 * @param resource the resource to check if it has been loaded
	 * @return true if the resource has been loaded
	 */
	public static boolean isLoaded(InternalDeps resource) {
		InternalNames[] names = resource.getDependencies();

		for (int i = 0; i < names.length; i++) {
			if (!isLoaded(names[i])) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Checks if everything has loaded and can be used.
	 * @return true if everything has been loaded
	 */
	public static boolean update() {
		boolean fullyLoaded = true;
		try {
			if (!mDependencyLoader.update()) {
				fullyLoaded = false;
			}
			if (fullyLoaded && !mLoadQueue.isEmpty()) {
				fullyLoaded = false;
				ResourceItem toLoad = mLoadQueue.removeFirst();
				mResourceLoader.load(toLoad.scene, toLoad.id, toLoad.revision);
			}
		} catch (ResourceException e) {

		}

		// Try to unload stuff
		Iterator<InternalNames> unloadIt = mUnloadList.iterator();
		while (unloadIt.hasNext()) {
			InternalNames name = unloadIt.next();
			String filepath = name.getFilePath();
			Object resource = mAssetManager.get(filepath);
			IResourceUnloadReady unloadReadyMethod = mUnloadReadyMethods.get(name.getType());
			if (unloadReadyMethod.isReadyToUnload(resource)) {
				mAssetManager.unload(filepath);
				unloadIt.remove();
			}
		}

		return fullyLoaded;
	}

	/**
	 * Waits for the cache to finish loading all files into the cache. I.e. blocks this
	 * thread
	 */
	public static void finishLoading() {
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
	 * @return loading progress in percentage of completion [0,100]
	 */
	public static float getProgress() {
		return mAssetManager.getProgress();
	}

	/**
	 * Disposes all the resources allocated.
	 */
	public static void dispose() {
		if (Debug.DEBUG_TESTS) {
			// All assets should be unloaded by now
			if (mAssetManager.getLoadedAssets() > 0) {
				Gdx.app.error("Assets", "All assets have not been unloaded!");
				Gdx.app.error("Assets", "\n" + mAssetManager.getDiagnostics());
			}
		}

		mDependencyLoader.dispose();
		mAssetManager.dispose();
		mLoadQueue.clear();
	}

	/**
	 * Private constructor to enforce that no instance can be created from this class.
	 */
	private ResourceCacheFacade() {
	}

	/** Resources that should be unloaded when they aren't used */
	private static ArrayList<InternalNames> mUnloadList = new ArrayList<>();
	/**
	 * List of all methods for checking if a resource is still being used and should be
	 * placed in the unload list
	 */
	private static Map<Class<?>, IResourceUnloadReady> mUnloadReadyMethods = new HashMap<>();
	/** Handles loading all dependencies. */
	private static ResourceDependencyLoader mDependencyLoader = new ResourceDependencyLoader();
	/** Resource Loader */
	private static ResourceLoader mResourceLoader = mDependencyLoader.getResourceLoader();
	/**
	 * Resource loader. This directly takes care of internal resources� it can load
	 * directly, no need to go through ResourceDependencyLoader. All Defs needs to be
	 * loaded via the ResourceDependencyLoader. A level is a special case and is also
	 * loaded directly via this manager, but still needs to load its LevelDef through
	 * ResourceDependencyLoader first. All resources can be directly accessed through this
	 * manager once loaded.
	 */
	private static AssetManager mAssetManager = mResourceLoader.getAssetManager();

	/**
	 * This queue is for loading resources (or rather instances of defs). However all defs
	 * needs to be loaded first which is done recursively while loading. The current only
	 * way to know if all resources have been loaded for a instance is to wait until the
	 * asset manager have loaded everything and then load the instances from the queue.
	 */
	private static LinkedList<ResourceItem> mLoadQueue = new LinkedList<ResourceItem>();

	// Create all unload ready methods
	static {
		mUnloadReadyMethods.put(Music.class, new IResourceUnloadReady() {
			@Override
			public boolean isReadyToUnload(Object resource) {
				if (resource instanceof Music) {
					return !((Music) resource).isPlaying();
				}
				return true;
			};
		});
	}
}

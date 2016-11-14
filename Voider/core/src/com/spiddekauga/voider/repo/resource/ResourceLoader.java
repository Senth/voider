package com.spiddekauga.voider.repo.resource;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.VoiderGame;
import com.spiddekauga.voider.scene.Scene;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Common class for loading resources
 * @param <Identifier> how the resource is identified
 * @param <Resource> the actual resource that is loaded
 */
abstract class ResourceLoader<Identifier, Resource> {
private static Map<Class<?>, IResourceUnloadReady> mUnloadReadyMethods = new HashMap<>();

static {
	mUnloadReadyMethods.put(Music.class, new IResourceUnloadReady() {
		@Override
		public boolean isReadyToUnload(Object resource) {
			if (resource instanceof Music) {
				return !((Music) resource).isPlaying();
			}
			return true;
		}

		;
	});
}

protected AssetManager mAssetManager;
protected HashMap<Identifier, LoadedResource> mLoadedResources = new HashMap<>();
protected HashMap<Identifier, LoadedResource> mLoadingQueue = new HashMap<>();
private ArrayList<Identifier> mUnloadQueue = new ArrayList<>();
private ArrayList<ReloadResource> mReloadQueue = new ArrayList<>();

/**
 * Initializes the resource loader with the an asset manager
 * @param assetManager
 */
protected ResourceLoader(AssetManager assetManager) {
	mAssetManager = assetManager;

	if (Config.Debug.Messages.LOAD_UNLOAD) {
		mAssetManager.getLogger().setLevel(Config.Debug.LOG_VERBOSITY);
	} else {
		mAssetManager.getLogger().setLevel(Application.LOG_NONE);
	}
}

/**
 * Loads the specified resource. A) If the resource has been loaded already to the specified scene
 * nothing happens; b) if the resource has been loaded into another scene it adds it to the
 * specified scene; c) if the resource hasn't been loaded it adds it to the resource queue.
 * @param scene the scene the resource is loaded into
 * @param identifier the resource identifier
 */
@SuppressWarnings("unchecked")
protected synchronized void load(Scene scene, Identifier identifier) {

	// Get the loaded resource
	LoadedResource loadedResource = mLoadedResources.get(identifier);
	if (loadedResource == null) {
		loadedResource = mLoadingQueue.get(identifier);
	}

	// Add scene to resource if it hasn't been
	if (loadedResource != null) {
		if (!loadedResource.scenes.contains(scene)) {
			loadedResource.scenes.add(scene);

			log("load(" + getClassName(scene) + ", " + loadedResource.identifier + "): add scene. Scene count: " + loadedResource.scenes.size());
		}
	}
	// Else no resource loaded create new
	else {
		loadedResource = new LoadedResource(identifier, scene);
		mAssetManager.load(loadedResource.filepath, getType(identifier), getParameters(identifier));
		mLoadingQueue.put(identifier, loadedResource);

		log("load(" + getClassName(scene) + ", " + loadedResource.identifier + "): new resource");
	}
}

/**
 * Log a message if it should
 * @param message the message to display
 */
private void log(String message) {
	log(ResourceLoader.class, message);
	;
}

/**
 * @param instance
 * @return get the correct class name for an instance
 */
protected String getClassName(Object instance) {
	if (instance == null) {
		return "null";
	} else {
		return instance.getClass().getSimpleName();
	}
}

/**
 * Get the type of the resource
 * @param identifier resource identifier
 * @return class type of the resource
 */
protected abstract Class<?> getType(Identifier identifier);

/**
 * @param identifier resource identifier
 * @return additional parameters when loading the resources. Should return null if no parameters are
 * necessary
 */
@SuppressWarnings("rawtypes")
protected AssetLoaderParameters getParameters(Identifier identifier) {
	return null;
}

/**
 * Log a message if it should
 * @param clazz the log message is from
 * @param message the message to display
 */
protected void log(Class<?> clazz, String message) {
	if (Config.Debug.Messages.LOAD_UNLOAD) {
		Gdx.app.debug(clazz.getSimpleName(), message);
	}
}

/**
 * Check if the resource is being loaded into the specified scene
 * @param scene the scene to check in, null to use any scene
 * @param identifier the resource identifier
 * @return true if the resource is being loaded into the specified scene
 */
protected synchronized boolean isLoading(Scene scene, Identifier identifier) {
	boolean found = false;
	LoadedResource loadedResource = mLoadingQueue.get(identifier);
	if (loadedResource != null) {
		if (scene != null) {
			found = loadedResource.scenes.contains(scene);
		} else {
			found = true;
		}
	}

	return found;
}

/**
 * Unload all resources allocated in the specified scene
 * @param scene unload all resources in this scene
 */
protected synchronized void unload(Scene scene) {
	Set<Entry<Identifier, LoadedResource>> entrySet = mLoadedResources.entrySet();
	Iterator<Entry<Identifier, LoadedResource>> iterator = entrySet.iterator();

	while (iterator.hasNext()) {
		Entry<Identifier, LoadedResource> entry = iterator.next();

		LoadedResource loadedResource = entry.getValue();
		if (loadedResource.scenes.contains(scene)) {
			loadedResource.scenes.remove(scene);

			// Unload the resource fully
			if (loadedResource.scenes.isEmpty()) {
				mAssetManager.unload(loadedResource.filepath);
				iterator.remove();
				onUnload(entry.getKey());

				log("unload(" + getClassName(scene) + "): Fully removed " + loadedResource.identifier);
			} else {
				log("unload(" + getClassName(scene) + "): Removed " + loadedResource.identifier + " from this scene. Scene count: "
						+ loadedResource.scenes.size());
			}
		}
	}
}

/**
 * Called when an loaded resource has been fully unloaded
 * @param identifier the identifier of the resource that was unloaded
 */
protected void onUnload(Identifier identifier) {
	// Does nothing
}

/**
 * Unload the specified resource (fully)
 * @param identifier the resource identifier
 */
protected synchronized void unload(Identifier identifier) {
	if (isLoaded(identifier)) {
		mLoadedResources.remove(identifier);
		mUnloadQueue.add(identifier);

		// Unload directly if main thread
		if (VoiderGame.isMainThread()) {
			do {
				update();
			} while (isUnloading());
		} else {
			waitTillUnloadingIsDone();
		}
	}
}

/**
 * Check if the resource has been loaded
 * @param identifier the resource identifier
 * @return true if the resource has been loaded
 */
protected synchronized boolean isLoaded(Identifier identifier) {
	return isLoaded(null, identifier);
}

/**
 * Updates the resource loader. This will set the loaded resources correctly
 * @return true if all resources has been loaded
 */
protected synchronized boolean update() {
	try {
		mAssetManager.update();
	} catch (GdxRuntimeException e) {
		handleException(e);
	}

	// Unload queue
	Iterator<Identifier> unloadIt = mUnloadQueue.iterator();
	while (unloadIt.hasNext()) {
		Identifier identifier = unloadIt.next();
		String filepath = getFilepath(identifier);

		// Unload if the resource is ready to be unloaded
		IResourceUnloadReady unloadReady = mUnloadReadyMethods.get(getType(identifier));
		if (unloadReady == null || unloadReady.isReadyToUnload(getResource(identifier))) {
			if (mAssetManager.isLoaded(filepath)) {
				mAssetManager.unload(filepath);
			}
			onUnload(identifier);

			unloadIt.remove();
		}
	}

	// Reload queue
	Iterator<ReloadResource> reloadIt = mReloadQueue.iterator();
	while (reloadIt.hasNext()) {
		ReloadResource reloadResource = reloadIt.next();

		// Check if it has been loaded and set the new resource
		if (reloadResource.unloaded) {
			if (mAssetManager.isLoaded(reloadResource.filepath)) {

				Resource newResource = mAssetManager.get(reloadResource.filepath);
				onReloaded(reloadResource.oldResource, newResource);

				// Update the resources
				LoadedResource loadedResource = mLoadedResources.get(reloadResource.identifier);
				loadedResource.resource = newResource;

				reloadIt.remove();
			}
		}
		// Unload the resource
		else {
			mAssetManager.unload(reloadResource.filepath);
			mAssetManager.load(reloadResource.filepath, reloadResource.oldResource.getClass());
			reloadResource.unloaded = true;
		}
	}

	// Loading queue
	Iterator<Entry<Identifier, LoadedResource>> loadIt = mLoadingQueue.entrySet().iterator();

	while (loadIt.hasNext()) {
		Entry<Identifier, LoadedResource> entry = loadIt.next();
		LoadedResource loadingResource = entry.getValue();

		// Add resource if it has been loaded
		if (mAssetManager.isLoaded(loadingResource.filepath)) {
			loadingResource.resource = mAssetManager.get(loadingResource.filepath);

			mLoadedResources.put(entry.getKey(), loadingResource);

			loadIt.remove();
		}
	}

	return !isLoading();
}

/**
 * @return true if unloading
 */
protected synchronized boolean isUnloading() {
	return !mUnloadQueue.isEmpty();
}

/**
 * Wait until unloading is done
 */
protected void waitTillUnloadingIsDone() {
	while (isUnloading()) {
		try {
			Thread.sleep(20);
		} catch (InterruptedException e) {
			// Do nothing
		}
	}
}

/**
 * Check if the resource has been loaded into the specified scene
 * @param scene if this resource has been loaded into this scene, null to use any
 * @param identifier the resource identifier
 * @return true if the resource has been loaded into the specified scene
 */
protected synchronized boolean isLoaded(Scene scene, Identifier identifier) {
	boolean found = false;
	LoadedResource loadedResource = mLoadedResources.get(identifier);
	if (loadedResource != null) {
		if (scene != null) {
			found = loadedResource.scenes.contains(scene);
		} else {
			found = true;
		}
	}

	return found;
}

/**
 * Override this method to handle the exception, by default this just rethrows the exception
 * @param exception the exception to handle
 */
void handleException(GdxRuntimeException exception) {
	throw exception;
}

/**
 * Get the filepath for the resource
 * @param identifier resource identifier
 * @return filepath for the resource
 */
protected abstract String getFilepath(Identifier identifier);

/**
 * @param <ResourceType> the resource type
 * @param identifier the resource to get
 * @return the loaded resource with the specified id, null if not loaded
 */
@SuppressWarnings("unchecked")
protected synchronized <ResourceType extends Resource> ResourceType getResource(Identifier identifier) {
	LoadedResource loadedResource = mLoadedResources.get(identifier);
	if (loadedResource != null) {
		return (ResourceType) loadedResource.resource;
	} else {
		return null;
	}
}

/**
 * Called when a resource has been updated
 * @param oldResource the old resource
 * @param newResource the new resource
 */
protected void onReloaded(Resource oldResource, Resource newResource) {
	// Does nothing
}

/**
 * @return true if loading or reloading
 */
protected synchronized boolean isLoading() {
	return !mLoadingQueue.isEmpty() || !mReloadQueue.isEmpty() || mAssetManager.getQueuedAssets() > 0;
}

/**
 * Reload the resource. Useful when the resource has been updated
 * @param identifier the resource to be reloaded
 */
protected synchronized void reload(Identifier identifier) {
	final Resource oldResource = getResource(identifier);

	if (oldResource != null) {
		mReloadQueue.add(new ReloadResource(identifier, oldResource));

		// Reload directly if main thread
		if (VoiderGame.isMainThread()) {
			do {
				update();
			} while (isLoading());
		} else {
			waitTillLoadingIsDone();
		}
	} else {
		Gdx.app.error(ResourceLoader.class.getSimpleName(), "Latest resource was not loaded while trying to reload it");
	}
}

/**
 * Waits until the loading is done
 */
protected void waitTillLoadingIsDone() {
	while (isLoading()) {
		try {
			Thread.sleep(20);
		} catch (InterruptedException e) {
			// Do nothing
		}
	}
}

/**
 * @param <ResourceType> Type of resources to get
 * @param clazz get all resources that can be assigned to this type
 * @return all loaded resources of the specified type
 */
@SuppressWarnings("unchecked")
synchronized <ResourceType extends Resource> ArrayList<ResourceType> getResourcesOf(Class<?> clazz) {
	ArrayList<ResourceType> resources = new ArrayList<>();

	for (Entry<Identifier, LoadedResource> entry : mLoadedResources.entrySet()) {
		if (clazz.isAssignableFrom(entry.getValue().resource.getClass())) {
			resources.add((ResourceType) entry.getValue().resource);
		}
	}

	return resources;
}

/**
 * Loaded or loading resources
 */
protected class LoadedResource {
	/** File path of the resource */
	String filepath;
	/** Identifier of the loaded resource */
	Identifier identifier;
	/** Which scene this resource should be loaded into */
	HashSet<Scene> scenes = new HashSet<>();
	/** The actual resource that was loaded */
	Resource resource = null;

	/**
	 * Constructor
	 * @param identifier resource identifier
	 * @param scene the first scene this resource will be loaded into
	 */
	public LoadedResource(Identifier identifier, Scene scene) {
		this.identifier = identifier;
		this.filepath = getFilepath(identifier);
		this.scenes.add(scene);
	}
}

/**
 * Reload queue class
 */
protected class ReloadResource {
	String filepath;
	Identifier identifier;
	Resource oldResource;
	boolean unloaded = false;

	/**
	 * Constructor
	 * @param identifier the identifier of the resource
	 * @param oldResource the old loaded resource
	 */
	ReloadResource(Identifier identifier, Resource oldResource) {
		this.identifier = identifier;
		this.filepath = getFilepath(identifier);
		this.oldResource = oldResource;
	}
}
}

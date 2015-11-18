package com.spiddekauga.voider.repo.resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.VoiderGame;
import com.spiddekauga.voider.scene.Scene;

/**
 * Common class for loading resources
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 * @param <Identifier> how the resource is identified
 * @param <Resource> the actual resource that is loaded
 */
abstract class ResourceLoader<Identifier, Resource> {

	/**
	 * Initializes the resource loader with the an asset manager
	 * @param assetManager
	 */
	protected ResourceLoader(AssetManager assetManager) {
		mAssetManager = assetManager;
		mAssetManager.getLogger().setLevel(Config.Debug.LOG_VERBOSITY);
	}

	/**
	 * Loads the specified resource. A) If the resource has been loaded already to the
	 * specified scene nothing happens; b) if the resource has been loaded into another
	 * scene it adds it to the specified scene; c) if the resource hasn't been loaded it
	 * adds it to the resource queue.
	 * @param scene the scene the resource is loaded into
	 * @param identifier the resource identifier
	 */
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
			}
		}
		// Else no resource loaded create new
		else {
			loadedResource = new LoadedResource();
			loadedResource.scenes.add(scene);
			loadedResource.filepath = getFilepath(identifier);
			mAssetManager.load(loadedResource.filepath, getClass());
			mLoadingQueue.put(identifier, loadedResource);
		}
	}

	/**
	 * Get the filepath for the resource
	 * @param identifier resource identifier
	 * @return filepath for the resource
	 */
	protected abstract String getFilepath(Identifier identifier);

	/**
	 * Get the type of the resource
	 * @param identifier resource identifier
	 * @return class type of the resource
	 */
	protected abstract Class<?> getType(Identifier identifier);

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
	 * Check if the resource has been loaded
	 * @param identifier the resource identifier
	 * @return true if the resource has been loaded
	 */
	protected synchronized boolean isLoaded(Identifier identifier) {
		return isLoaded(null, identifier);
	}

	/**
	 * @return true if loading, unloading, or reloading
	 */
	protected synchronized boolean isLoading() {
		return !mLoadingQueue.isEmpty() || !mUnloadQueue.isEmpty() || !mReloadQueue.isEmpty() || mAssetManager.getQueuedAssets() > 0;
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

				if (loadedResource.scenes.isEmpty()) {
					mAssetManager.unload(loadedResource.filepath);
					iterator.remove();
					onUnload(entry.getKey());
				}
			}
		}
	}

	/**
	 * Unload the specified resource (fully)
	 * @param identifier the resource identifier
	 */
	protected synchronized void unload(Identifier identifier) {
		LoadedResource loadedResource = mLoadedResources.get(identifier);

		// Unload if the resource is loaded
		if (loadedResource != null) {

			mUnloadQueue.add(loadedResource.filepath);

			// Unload directly if main thread
			if (VoiderGame.isMainThread()) {
				do {
					update();
				} while (isLoading());
			} else {
				waitTillLoadingIsDone();
			}

			mLoadedResources.remove(identifier);
			onUnload(identifier);
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
	 * Reload the resource. Useful when the resource has been updated
	 * @param identifier the resource to be reloaded
	 */
	protected synchronized void reload(Identifier identifier) {
		final Resource oldResource = getLoadedResource(identifier);

		if (oldResource != null) {
			final String latestFilepath = getFilepath(identifier);
			mReloadQueue.add(new ReloadResource(latestFilepath, oldResource));

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
		Iterator<String> unloadIt = mUnloadQueue.iterator();
		while (unloadIt.hasNext()) {
			String filepath = unloadIt.next();
			if (mAssetManager.isLoaded(filepath)) {
				mAssetManager.unload(filepath);
			}

			unloadIt.remove();
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
	 * Called when a resource has been updated
	 * @param oldResource the old resource
	 * @param newResource the new resource
	 */
	protected void onReloaded(Resource oldResource, Resource newResource) {
		// Does nothing
	}

	/**
	 * Override this method to handle the exception, by default this just rethrows the
	 * exception
	 * @param exception the exception to handle
	 */
	void handleException(GdxRuntimeException exception) {
		throw exception;
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
	 * @param <ResourceType> the resource type
	 * @param identifier the resource to get
	 * @return the loaded resource with the specified id, null if not loaded
	 */
	@SuppressWarnings("unchecked")
	protected synchronized <ResourceType extends Resource> ResourceType getLoadedResource(Identifier identifier) {
		LoadedResource loadedResource = mLoadedResources.get(identifier);
		if (loadedResource != null) {
			return (ResourceType) loadedResource.resource;
		} else {
			return null;
		}
	}

	/**
	 * @param <ResourceType> Type of resources to get
	 * @param clazz get all resources that can be assigned to this type
	 * @return all loaded resources of the specified type
	 */
	@SuppressWarnings("unchecked")
	synchronized <ResourceType extends Resource> ArrayList<ResourceType> getAllLoadedResourcesOf(Class<?> clazz) {
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
		String filepath = null;
		/** Which scene this resource should be loaded into */
		HashSet<Scene> scenes = new HashSet<>();
		/** The actual resource that was loaded */
		Resource resource = null;
	}

	/**
	 * Reload queue class
	 */
	protected class ReloadResource {
		/**
		 * Constructor
		 * @param filepath the file path of the resource to unload
		 * @param oldResource the old loaded resource
		 */
		ReloadResource(String filepath, Resource oldResource) {
			this.filepath = filepath;
			this.oldResource = oldResource;
		}

		/** File path of the resource */
		String filepath;
		/** Old loaded resource */
		Resource oldResource;
		/** True if the resource has been unloaded */
		boolean unloaded = false;
	}

	/** Asset manager */
	protected AssetManager mAssetManager;
	/** All loaded resources */
	protected HashMap<Identifier, LoadedResource> mLoadedResources = new HashMap<>();
	/** All resources that are currently loading */
	protected HashMap<Identifier, LoadedResource> mLoadingQueue = new HashMap<>();
	/** Unload queue */
	private ArrayList<String> mUnloadQueue = new ArrayList<>();
	/** Unload queue */
	private ArrayList<ReloadResource> mReloadQueue = new ArrayList<>();
}

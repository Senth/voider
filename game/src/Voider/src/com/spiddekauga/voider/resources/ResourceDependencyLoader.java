package com.spiddekauga.voider.resources;

import java.util.Map;
import java.util.UUID;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.spiddekauga.voider.scene.Scene;

/**
 * Makes sure that all dependencies to the specified resource is loaded
 * and unloaded.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
class ResourceDependencyLoader implements Disposable {
	/**
	 * Constructor that takes an asset manager that is used to load
	 * the resources.
	 * @param assetManager used for loading/unloading dependencies
	 */
	ResourceDependencyLoader(AssetManager assetManager) {
		mAssetManager = assetManager;
	}

	/**
	 * Loads the definition including all its dependencies.
	 * @note it will load the definition first, as it needs to read
	 * its dependencies which is in the file.
	 * @param <ResourceType> class of the resourceId to load
	 * @param scene the scene to load this resource into
	 * @param resourceId the id of the resource which we want to load, including its dependencies
	 * @param revision the revision of the resource, -1 to use latest revision
	 * @throws UndefinedResourceTypeException thrown when type is an undefined resource type
	 */
	<ResourceType> void load(Scene scene, UUID resourceId, int revision) throws UndefinedResourceTypeException {
		// Add definition to wait queue
		mLoadingDefs.add(new ResourceItem(scene, resourceId, revision));

		// Load the resource
		ResourceDatabase.load(scene, resourceId, revision);
	}

	@Override
	public void dispose() {
		mLoadingDefs.clear();
	}

	/**
	 * Unloads the definition including all its dependencies.
	 * @param scene the scene to unload the resource from
	 * @param resource the definition to unload
	 */
	void unload(Scene scene, IResourceDependency resource) {
		// Recursive, unload all dependencies first
		// Internal
		for (ResourceNames dependency : resource.getInternalDependencies()) {
			mAssetManager.unload(dependency.getFilePath());
		}

		// External, if the dependency has dependencies, unload its dependencies too
		for (Map.Entry<UUID, Integer> entry : resource.getExternalDependencies().entrySet()) {
			UUID dependencyId = entry.getKey();
			IResource dependency = ResourceDatabase.getLoadedResource(scene, dependencyId, -1);

			if (dependency instanceof IResourceDependency) {
				unload(scene, (IResourceDependency) dependency);
			} else {
				ResourceDatabase.unload(scene, dependency);
			}
		}

		// unload this resource
		ResourceDatabase.unload(scene, resource);
	}

	/**
	 * Checks whether the resources have been loaded and then if it
	 * has any dependencies that shall be loaded. If so it will add
	 * these to the queue.
	 * @return true if it has finished all the loading
	 * @throws UndefinedResourceTypeException thrown when a resource has an invalid type
	 */
	boolean update() throws UndefinedResourceTypeException {
		// Skip update if we aren't waiting for any definitions to be done loading
		// I.e. not loading anything
		if (mLoadingDefs.size == 0) {
			return true;
		}

		ResourceDatabase.update();

		// If any of the resources we're waiting for been loaded ->
		// Check for its dependencies and remove from load
		for (int i = 0; i < mLoadingDefs.size; ++i) {
			ResourceItem queueItem = mLoadingDefs.get(i);

			if (ResourceDatabase.isResourceLoaded(queueItem.scene, queueItem.id, queueItem.revision)) {
				IResourceDependency def = (IResourceDependency) ResourceDatabase.getLoadedResource(queueItem.scene, queueItem.id, queueItem.revision);

				// Load dependencies
				// External
				for (Map.Entry<UUID, Integer> entry : def.getExternalDependencies().entrySet()) {
					UUID dependencyId = entry.getKey();

					try {
						load(queueItem.scene, dependencyId, -1);
					} catch (UndefinedResourceTypeException e) {
						// Reset entire loading queue
						mLoadingDefs.clear();
						throw e;
					} catch (GdxRuntimeException e) {
						mLoadingDefs.clear();
						throw e;
					}
				}

				// Internal
				for (ResourceNames dependency : def.getInternalDependencies()) {
					mAssetManager.load(dependency.getFilePath(), dependency.type);
				}

				// Remove element
				mLoadingDefs.removeIndex(i);
				--i;
			}
		}

		return mLoadingDefs.size == 0;
	}

	/**
	 * @return true if this class is loading
	 */
	boolean isLoading() {
		return mLoadingDefs.size != 0;
	}

	/** The load queue which we're loading the resources */
	private Array<ResourceItem> mLoadingDefs = new Array<ResourceItem>();
	/** The class actually loading the resources */
	private AssetManager mAssetManager;
}

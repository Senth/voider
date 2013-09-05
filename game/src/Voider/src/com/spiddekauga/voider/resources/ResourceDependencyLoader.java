package com.spiddekauga.voider.resources;

import java.util.UUID;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
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
	 * @param type the class type of resourceId
	 * @param revision the revision of the resource, -1 to use latest revision
	 * @throws UndefinedResourceTypeException thrown when type is an undefined resource type
	 */
	<ResourceType> void load(Scene scene, UUID resourceId, Class<ResourceType> type, int revision) throws UndefinedResourceTypeException {
		// Add definition to wait queue
		mLoadingDefs.add(new ResourceItem(scene, resourceId, type, revision));

		// Load the resource
		ResourceDatabase.load(scene, resourceId, type, revision);
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

		// External
		for (ObjectMap.Entry<UUID, ResourceItem> entry : resource.getExternalDependencies().entries()) {
			ResourceItem dependencyInformation = entry.value;
			IResource dependency = ResourceDatabase.getLoadedResource(scene, dependencyInformation.resourceId, dependencyInformation.revision);
			ResourceDatabase.unload(scene, dependency);
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

		mAssetManager.update();

		// If any of the resources we're waiting for been loaded ->
		// Check for its dependencies and remove from load
		for (int i = 0; i < mLoadingDefs.size; ++i) {
			ResourceItem queueItem = mLoadingDefs.get(i);

			if (mAssetManager.isLoaded(queueItem.fullName)) {
				IResourceDependency def = (IResourceDependency) mAssetManager.get(queueItem.fullName, queueItem.resourceType);


				// Load dependencies
				// External
				for (ObjectMap.Entry<UUID, ResourceItem> entry : def.getExternalDependencies().entries()) {
					ResourceItem dependency = entry.value;

					// Propagate scene so that we always know which scene to load into
					dependency.scene = queueItem.scene;
					try {
						load(queueItem.scene, dependency.resourceId, dependency.resourceType, dependency.revision);
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

package com.spiddekauga.voider.resources;

import java.util.UUID;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.ExternalFileHandleResolver;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
import com.spiddekauga.voider.game.actors.ActorDef;

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
	 * @param assetManager used for loading (and then getting) the
	 * resources. This asset manager is used #ResourceCacheFacade.
	 */
	ResourceDependencyLoader(AssetManager assetManager) {
		mAssetManager = assetManager;
		mAssetManager.setLoader(ActorDef.class, new JsonLoaderAsync<ActorDef>(new ExternalFileHandleResolver(), ActorDef.class));
	}

	/**
	 * Loads the definition including all its dependencies.
	 * @note it will load the definition first, as it needs to read
	 * its dependencies which is in the file.
	 * @param <ResourceType> class of the resourceId to load
	 * @param resourceId the id of the resource which we want to load, including its dependencies
	 * @param type the class type of resourceId
	 * @throws UndefinedResourceTypeException thrown when type is an undefined resource type
	 */
	<ResourceType> void load(UUID resourceId, Class<ResourceType> type) throws UndefinedResourceTypeException {
		// Add definition to wait queue
		mLoadingDefs.add(new ResourceItem(resourceId, type));

		// Load the resource
		final String fullPath = ResourceNames.getDirPath(type) + resourceId.toString();
		mAssetManager.load(fullPath, type);
	}

	@Override
	public void dispose() {
		mLoadingDefs.clear();
	}

	/**
	 * Unloads the definition including all its dependencies.
	 * @param resource the definition to unload
	 */
	void unload(IResourceDependency resource) {
		// Recursive, unload all dependencies first
		// Internal
		for (ResourceNames dependency : resource.getInternalDependencies()) {
			mAssetManager.unload(dependency.fullName);
		}

		// External
		for (ObjectMap.Entry<UUID, ResourceItem> entry : resource.getExternalDependencies().entries()) {
			ResourceItem dependency = entry.value;
			Def externalDef = (Def) mAssetManager.get(dependency.fullName, dependency.resourceType);
			unload(externalDef);
			// DO NOT USE externalDef AFTER THIS TIME, IT HAS BEEN UNLOADED!
		}

		// unload this def
		try {
			String fullName = ResourceNames.getDirPath(resource.getClass()) + resource.getId().toString();
			mAssetManager.unload(fullName);
		} catch (UndefinedResourceTypeException e) {
			Gdx.app.error("UndefinedResourceType", e.toString());
		}

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
					try {
						load(dependency.resourceId, dependency.resourceType);
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
					mAssetManager.load(dependency.fullName, dependency.type);
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

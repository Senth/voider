package com.spiddekauga.voider.resources;

import java.util.UUID;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.utils.Array;

/**
 * Makes sure that all dependencies to the specified resource is loaded
 * and unloaded.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
class ResourceDependencyLoader {
	/**
	 * Constructor that takes an asset manager that is used to load
	 * the resources.
	 * @param assetManager used for loading (and then getting) the
	 * resources. This asset manager is used #ResourceCacheFacade.
	 */
	ResourceDependencyLoader(AssetManager assetManager) {
		mAssetManager = assetManager;
	}

	/**
	 * Loads the definition including all its dependencies.
	 * @note it will load the definition first, as it needs to read
	 * its dependencies which is in the file.
	 * @param <DefType> class of the defId to load
	 * @param defId the definition and its dependencies to load
	 * @param type the class type of defId
	 */
	<DefType> void load(UUID defId, Class<DefType> type) {
		// Add definition to wait queue
		mLoadingDefs.add(new DefItem(defId, type));

		// Load the resource
		/** @note by loading again, this might cause problems in the future */
		final String fullPath = ResourceNames.getDirPath(type) + defId.toString();
		mAssetManager.load(fullPath, type);
	}

	/**
	 * Unloads the definition including all its dependencies.
	 * @param def the definition to unload
	 */
	void unload(Def def) {
		// Recursive, unload all dependencies first
		// Internal
		for (ResourceNames dependency : def.getInternalDependencies()) {
			mAssetManager.unload(dependency.fullName);
		}

		// External
		for (DefItem dependency : def.getExternalDependencies()) {
			Def externalDef = (Def) mAssetManager.get(dependency.fullName, dependency.resourceType);
			unload(externalDef);
			// DO NOT USE externalDef AFTER THIS TIME, IT HAS BEEN UNLOADED!
		}

		// unload this def
		final String fullName = ResourceNames.getDirPath(def.getClass()) + def.getId().toString();
		mAssetManager.unload(fullName);
	}

	/**
	 * Checks whether the resources have been loaded and then if it
	 * has any dependencies that shall be loaded. If so it will add
	 * these to the queue.
	 * @return true if it has finished all the loading
	 */
	boolean update() {
		// Skip update if we aren't waiting for any definitions to be done loading
		// I.e. not loading anything
		if (mLoadingDefs.size == 0) {
			return true;
		}

		// If any of the resources we're waiting for been loaded ->
		// Check for its dependencies and remove from load
		for (int i = 0; i < mLoadingDefs.size; ++i) {
			DefItem queueItem = mLoadingDefs.get(i);

			if (mAssetManager.isLoaded(queueItem.fullName)) {
				Def def = (Def) mAssetManager.get(queueItem.fullName, queueItem.resourceType);


				// Load dependencies
				// External
				for (DefItem dependency : def.getExternalDependencies()) {
					load(dependency.resourceId, dependency.resourceType);
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

	/** The load queue which we're loading the resources */
	private Array<DefItem> mLoadingDefs = new Array<DefItem>();
	/** The class actually loading the resources */
	private AssetManager mAssetManager;
}

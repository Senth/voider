package com.spiddekauga.voider.repo.resource;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.resources.IResource;
import com.spiddekauga.voider.resources.IResourceDependency;
import com.spiddekauga.voider.resources.ResourceException;
import com.spiddekauga.voider.resources.ResourceItem;
import com.spiddekauga.voider.scene.Scene;

/**
 * Makes sure that all dependencies to the specified resource is loaded and unloaded.
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
class ResourceDependencyLoader implements Disposable {
	/**
	 * Default constructor
	 */
	ResourceDependencyLoader() {
		mAssetManager = mResourceLoader.getAssetManager();
	}

	/**
	 * @return the resource loader
	 */
	ResourceLoader getResourceLoader() {
		return mResourceLoader;
	}

	/**
	 * Loads the definition including all its dependencies.
	 * @note it will load the definition first, as it needs to read its dependencies which
	 *       is in the file.
	 * @param <ResourceType> class of the resourceId to load
	 * @param scene the scene to load this resource into
	 * @param resourceId the id of the resource which we want to load, including its
	 *        dependencies
	 * @param revision the revision of the resource, -1 to use latest revision
	 */
	synchronized <ResourceType> void load(Scene scene, UUID resourceId, int revision) {
		// Load the resource
		mResourceLoader.load(scene, resourceId, revision);

		// Add definition to wait queue
		mLoadingDefs.add(new ResourceItem(scene, resourceId, revision));
	}

	@Override
	public void dispose() {
		mLoadingDefs.clear();
	}

	/**
	 * Checks whether the resources have been loaded and then if it has any dependencies
	 * that shall be loaded. If so it will add these to the queue.
	 * @return true if it has finished all the loading
	 */
	synchronized boolean update() {
		try {
			mResourceLoader.update();
		} catch (ResourceException e) {
			// Remove the queue item
			ResourceItem searchResourceItem = new ResourceItem();
			searchResourceItem.id = e.getId();
			mLoadingDefs.remove(searchResourceItem);

			throw e;
		}

		// Skip update if we aren't waiting for any definitions to be done loading
		// I.e. not loading anything
		if (mLoadingDefs.size() == 0) {
			return mLoadingDefs.size() == 0 && !mResourceLoader.isLoading();
		}

		// If any of the resources we're waiting for been loaded ->
		// Check for its dependencies and remove from load
		for (int i = 0; i < mLoadingDefs.size(); ++i) {
			ResourceItem queueItem = mLoadingDefs.get(i);

			if (mResourceLoader.isResourceLoaded(queueItem.id, queueItem.revision)) {
				IResource resource = mResourceLoader.getLoadedResource(queueItem.id, queueItem.revision);

				if (resource instanceof IResourceDependency) {
					IResourceDependency def = (IResourceDependency) resource;

					debugLoadUnload(queueItem.scene, def, true);

					// Load dependencies
					// External
					for (Map.Entry<UUID, AtomicInteger> entry : def.getExternalDependencies().entrySet()) {
						UUID dependencyId = entry.getKey();

						try {
							load(queueItem.scene, dependencyId, -1);
						} catch (GdxRuntimeException e) {
							mLoadingDefs.clear();
							throw e;
						}
					}

					// Internal
					for (InternalNames dependency : def.getInternalDependencies()) {
						mAssetManager.load(dependency.getFilePath(), dependency.getType());
					}
				} else {
					Gdx.app.debug("ResourceDependency", "Loaded resource " + resource.getClass().getSimpleName() + " does not have any dependencies.");
				}

				// Remove element
				mLoadingDefs.remove(i);
				--i;
			}
		}

		return mLoadingDefs.size() == 0 && !mResourceLoader.isLoading();
	}

	/**
	 * Debug output for when loading/unloading dependencies
	 * @param scene the scene the dependency is loaded/unloaded to
	 * @param resource the resource to load/unload dependencies from
	 * @param load true if loading
	 */
	private static void debugLoadUnload(Scene scene, IResourceDependency resource, boolean load) {
		if (Config.Debug.Messages.LOAD_UNLOAD && Config.Debug.Messages.LOAD_UNLOAD_DEPENDENCIES) {
			// if (resource.getExternalDependenciesCount() > 0) {
			//
			// String resourceName = "";
			// if (resource instanceof Def) {
			// resourceName = ((Def) resource).getName();
			// }
			//
			// String resourceType = resource.getClass().getSimpleName();
			//
			// String loadUnloadString = "";
			// if (load) {
			// loadUnloadString = "+++";
			// } else {
			// loadUnloadString = "---";
			// }
			//
			// String message = Strings.padLeft(loadUnloadString, 6) + "  " +
			// Strings.padRight(scene.getClass().getSimpleName(), 15) +
			// Strings.padRight(resourceType, 18) +
			// Strings.padRight(resourceName, Config.Editor.NAME_LENGTH_MAX + 2) +
			// "---------->>>";
			//
			//
			// for (Map.Entry<UUID, AtomicInteger> entry :
			// resource.getExternalDependencies().entrySet()) {
			// ResourceInfo resourceInfo =
			// ResourceDatabaseOld.getResourceInfo(entry.getKey());
			//
			// message += "\n" + Strings.padLeft("", 49) +
			// Strings.padRight(resourceInfo.type.getSimpleName(), 16) + entry.getKey();
			// }
			//
			// Gdx.app.debug("ResourceDependencyLoader", message);
			// }
		}
	}

	/**
	 * @return true if this class is loading
	 */
	synchronized boolean isLoading() {
		return mLoadingDefs.size() != 0;
	}

	/** The load queue which we're loading the resources */
	private ArrayList<ResourceItem> mLoadingDefs = new ArrayList<ResourceItem>();
	/** The class actually loading the resources */
	private AssetManager mAssetManager;
	/** Resource loader, this actually loads all the external dependencies */
	private static ResourceLoader mResourceLoader = new ResourceLoader();
}

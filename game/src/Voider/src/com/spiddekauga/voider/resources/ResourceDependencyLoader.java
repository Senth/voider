package com.spiddekauga.voider.resources;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.spiddekauga.utils.Strings;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.resources.ResourceDatabase.ResourceInfo;
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
		debugLoadUnload(scene, resource, false);

		// Recursive, unload all dependencies first
		// Internal
		for (ResourceNames dependency : resource.getInternalDependencies()) {
			mAssetManager.unload(dependency.getFilePath());
		}

		// External, if the dependency has dependencies, unload its dependencies too
		for (Map.Entry<UUID, AtomicInteger> entry : resource.getExternalDependencies().entrySet()) {
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
				IResource resource = ResourceDatabase.getLoadedResource(queueItem.scene, queueItem.id, queueItem.revision);

				if (resource instanceof IResourceDependency) {
					IResourceDependency def = (IResourceDependency) resource;

					debugLoadUnload(queueItem.scene, def, true);

					// Load dependencies
					// External
					for (Map.Entry<UUID, AtomicInteger> entry : def.getExternalDependencies().entrySet()) {
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
				} else {
					Gdx.app.debug("ResourceDependency", "Loaded resource " + resource.getClass().getSimpleName() + " does not have any dependencies.");
				}

				// Remove element
				mLoadingDefs.removeIndex(i);
				--i;
			}
		}

		return mLoadingDefs.size == 0;
	}

	/**
	 * Debug output for when loading/unloading dependencies
	 * @param scene the scene the dependency is loaded/unloaded to
	 * @param resource the resource to load/unload dependencies from
	 * @param load true if loading
	 */
	private static void debugLoadUnload(Scene scene, IResourceDependency resource, boolean load) {
		if (Config.Debug.Messages.LOAD_UNLOAD && Config.Debug.Messages.LOAD_UNLOAD_DEPENDENCIES) {
			if (resource.getExternalDependenciesCount() > 0) {

				String resourceName = "";
				if (resource instanceof Def) {
					resourceName = ((Def) resource).getName();
				}

				String resourceType = resource.getClass().getSimpleName();

				String loadUnloadString = "";
				if (load) {
					loadUnloadString = "+++";
				} else {
					loadUnloadString = "---";
				}

				String message = Strings.padLeft(loadUnloadString, 6) + "  " +
						Strings.padRight(scene.getClass().getSimpleName(), 15) +
						Strings.padRight(resourceType, 18) +
						Strings.padRight(resourceName, Config.Editor.NAME_LENGTH_MAX + 2) +
						"---------->>>";


				for (Map.Entry<UUID, AtomicInteger> entry : resource.getExternalDependencies().entrySet()) {
					ResourceInfo resourceInfo = ResourceDatabase.getResourceInfo(entry.getKey());

					message += "\n" + Strings.padLeft("", 49) + Strings.padRight(resourceInfo.type.getSimpleName(), 16) + entry.getKey();
				}

				Gdx.app.debug("ResourceDependencyLoader", message);
			}
		}
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

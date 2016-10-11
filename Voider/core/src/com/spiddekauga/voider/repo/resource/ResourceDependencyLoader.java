package com.spiddekauga.voider.repo.resource;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import com.badlogic.gdx.Gdx;
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
	 * @param internalLoader
	 * @param externalLoader
	 */
	ResourceDependencyLoader(ResourceInternalLoader internalLoader, ResourceExternalLoader externalLoader) {
		mInternalLoader = internalLoader;
		mExternalLoader = externalLoader;
	}

	/**
	 * @return the resource loader
	 */
	ResourceExternalLoader getExternalLoader() {
		return mExternalLoader;
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
		mExternalLoader.load(scene, resourceId, revision);

		// Add definition to wait queue
		mLoadingDefs.add(new ResourceItem(scene, resourceId, revision));
	}

	@Override
	public void dispose() {
		mLoadingDefs.clear();
	}

	/**
	 * Remove a loading resource
	 * @param resourceId the resource to skip loading
	 */
	private void removeFromLoadingQueue(UUID resourceId) {
		ResourceItem searchResourceItem = new ResourceItem();
		searchResourceItem.id = resourceId;
		mLoadingDefs.remove(searchResourceItem);
	}

	/**
	 * Checks whether the resources have been loaded and then if it has any dependencies
	 * that shall be loaded. If so it will add these to the queue.
	 * @return true if it has finished all the loading
	 */
	synchronized boolean update() {
		try {
			mExternalLoader.update();
		} catch (ResourceException e) {
			// Remove the queue item
			removeFromLoadingQueue(e.getId());

			throw e;
		}

		// Check if some resources failed to be loaded
		BlockingQueue<UserResourceIdentifier> failedResources = mExternalLoader.getFailedDownloaded();
		for (UserResourceIdentifier uuidRevision : failedResources) {
			removeFromLoadingQueue(uuidRevision.resourceId);
		}

		// Skip update if we aren't waiting for any definitions to be done loading
		// I.e. not loading anything
		if (mLoadingDefs.size() == 0) {
			return mLoadingDefs.size() == 0 && !mExternalLoader.isLoading();
		}

		// If any of the resources we're waiting for been loaded ->
		// Check for its dependencies and remove from load
		for (int i = 0; i < mLoadingDefs.size(); ++i) {
			ResourceItem queueItem = mLoadingDefs.get(i);

			if (mExternalLoader.isLoaded(queueItem.id, queueItem.revision)) {
				IResource resource = mExternalLoader.getResource(queueItem.id, queueItem.revision);

				if (resource instanceof IResourceDependency) {
					IResourceDependency def = (IResourceDependency) resource;

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
						mInternalLoader.load(queueItem.scene, dependency);
					}
				} else {
					log("Loaded resource " + resource.getClass().getSimpleName() + " does not have any dependencies.");
				}

				// Remove element
				mLoadingDefs.remove(i);
				--i;
			}
		}

		return mLoadingDefs.size() == 0 && !mExternalLoader.isLoading();
	}

	/**
	 * Internal logging
	 * @param message
	 */
	private void log(String message) {
		if (Config.Debug.Messages.LOAD_UNLOAD) {
			Gdx.app.debug(ResourceDependencyLoader.class.getSimpleName(), message);
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
	/** Resource loader, this actually loads all the external dependencies */
	private ResourceExternalLoader mExternalLoader;
	/** Loads all internal resources */
	private ResourceInternalLoader mInternalLoader;

}

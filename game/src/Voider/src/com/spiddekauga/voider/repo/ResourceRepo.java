package com.spiddekauga.voider.repo;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import com.badlogic.gdx.Gdx;
import com.spiddekauga.utils.IOutstreamProgressListener;
import com.spiddekauga.voider.game.Level;
import com.spiddekauga.voider.game.actors.ActorDef;
import com.spiddekauga.voider.network.entities.DefEntity;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.LevelDefEntity;
import com.spiddekauga.voider.network.entities.ResourceBlobEntity;
import com.spiddekauga.voider.network.entities.ResourceRevisionBlobEntity;
import com.spiddekauga.voider.network.entities.ResourceRevisionEntity;
import com.spiddekauga.voider.network.entities.RevisionEntity;
import com.spiddekauga.voider.network.entities.method.IMethodEntity;
import com.spiddekauga.voider.network.entities.method.PublishMethod;
import com.spiddekauga.voider.network.entities.method.PublishMethodResponse;
import com.spiddekauga.voider.network.entities.method.ResourceDownloadMethod;
import com.spiddekauga.voider.network.entities.method.ResourceDownloadMethodResponse;
import com.spiddekauga.voider.network.entities.method.SyncDownloadMethod;
import com.spiddekauga.voider.network.entities.method.SyncDownloadMethodResponse;
import com.spiddekauga.voider.network.entities.method.SyncUserResourcesMethod;
import com.spiddekauga.voider.network.entities.method.SyncUserResourcesMethodResponse;
import com.spiddekauga.voider.resources.Def;
import com.spiddekauga.voider.resources.IResource;
import com.spiddekauga.voider.resources.IResourceRevision;
import com.spiddekauga.voider.utils.Pools;
import com.spiddekauga.voider.utils.Synchronizer;
import com.spiddekauga.voider.utils.User;

/**
 * Common resource repository for both web and local. Handles requests that affects both.
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class ResourceRepo implements ICallerResponseListener {
	/**
	 * Protected constructor to enforce singleton usage
	 */
	protected ResourceRepo() {
		// Does nothing
	}

	/**
	 * @return singleton instance of ResourceRepo
	 */
	public static ResourceRepo getInstance() {
		if (mInstance == null) {
			mInstance = new ResourceRepo();
		}

		return mInstance;
	}

	/**
	 * Synchronize downloaded/published resources
	 * @param responseListeners listens to the web response (when syncing is done)
	 */
	public void syncDownload(ICallerResponseListener... responseListeners) {
		Date lastSync = ResourceLocalRepo.getSyncDownloadDate();
		mWebRepo.syncDownloaded(lastSync, addToFront(responseListeners, this));
	}

	/**
	 * Save the specified resources. If the resource contains revisions it will try to
	 * upload it to the server if the user is currently online.
	 * @param resources all the resource to save.
	 */
	public void save(IResource... resources) {
		save(null, resources);
	}

	/**
	 * Save the specified resources. If the resource contains revisions it will try to
	 * upload it to the server if the user is currently online.
	 * @param responseListener listens to the sync web response, may be null
	 * @param resources all the resource to save.
	 */
	public void save(ICallerResponseListener responseListener, IResource... resources) {

		boolean upload = false;

		for (IResource resource : resources) {
			boolean success = ResourceLocalRepo.save(resource);

			if (success && resource instanceof IResourceRevision) {
				upload = true;
			}
		}

		if (upload) {
			if (User.getGlobalUser().isOnline()) {
				// Don't sync via the synchronizer as a wait window will appear, instead
				// just add the synchronizer as a listener
				if (responseListener != null) {
					syncUserResources(Synchronizer.getInstance(), responseListener);
				} else {
					syncUserResources(Synchronizer.getInstance());
				}
			}
		}
	}

	/**
	 * Removes the specified resource, will unload it if it is currently loaded
	 * @param resourceId the resource to remove
	 */
	public void remove(UUID resourceId) {
		// Unload first
		ResourceCacheFacade.unload(resourceId);

		// Remove it from the database and physically
		ResourceLocalRepo.remove(resourceId);
	}

	/**
	 * Synchronizes the user resource revisions, both upload and download
	 * @param responseListeners listens to the web response (when syncing is done)
	 */
	public void syncUserResources(ICallerResponseListener... responseListeners) {
		mWebRepo.syncUserResources(ResourceLocalRepo.getUnsyncedUserResources(), ResourceLocalRepo.getSyncUserResourceDate(),
				addToFront(responseListeners, this));
	}

	/**
	 * Add add an element to the front of an array
	 * @param array the original array
	 * @param element the element to add
	 * @return new array with the element at the front
	 */
	private static ICallerResponseListener[] addToFront(ICallerResponseListener[] array, ICallerResponseListener element) {
		ICallerResponseListener[] newArray = new ICallerResponseListener[array.length + 1];
		newArray[0] = element;
		System.arraycopy(array, 0, newArray, 1, array.length);
		return newArray;
	}


	/**
	 * Publish an actor (and its unpublished dependencies) to the server
	 * @param responseListener listens to the web response
	 * @param progressListener listen to upload writing
	 * @param actorDef the actor to publish
	 */
	public void publish(ICallerResponseListener responseListener, IOutstreamProgressListener progressListener, ActorDef actorDef) {
		@SuppressWarnings("unchecked")
		ArrayList<IResource> resources = Pools.arrayList.obtain();

		resources.addAll(getNonPublishedDependencies(actorDef));
		resources.add(actorDef);

		publish(responseListener, progressListener, resources);
	}

	/**
	 * Publish a level (and its unpublished dependencies) to the server
	 * @param responseListener listens to the web response
	 * @param progressListener listen to upload writing
	 * @param level the level to publish
	 */
	public void publish(ICallerResponseListener responseListener, IOutstreamProgressListener progressListener, Level level) {
		@SuppressWarnings("unchecked")
		ArrayList<IResource> resources = Pools.arrayList.obtain();

		resources.addAll(getNonPublishedDependencies(level.getDef()));
		resources.add(level);
		resources.add(level.getDef());

		publish(responseListener, progressListener, resources);
	}

	/**
	 * Publish all the resources. In addition frees the ArrayList
	 * @param responseListener listens to the web response
	 * @param progressListener listen to upload writing
	 * @param resources all resources to publish
	 */
	private void publish(ICallerResponseListener responseListener, IOutstreamProgressListener progressListener, ArrayList<IResource> resources) {
		mWebRepo.publish(resources, progressListener, this, responseListener);
	}

	@Override
	public void handleWebResponse(IMethodEntity method, IEntity response) {
		if (response instanceof PublishMethodResponse) {
			handlePublishResponse((PublishMethod) method, (PublishMethodResponse) response);
		} else if (response instanceof ResourceDownloadMethodResponse) {
			handleDownloadResponse((ResourceDownloadMethod) method, (ResourceDownloadMethodResponse) response);
		} else if (response instanceof SyncDownloadMethodResponse) {
			handleSyncDownloadResponse((SyncDownloadMethod) method, (SyncDownloadMethodResponse) response);
		} else if (response instanceof SyncUserResourcesMethodResponse) {
			handleSyncUserResourcesResponse((SyncUserResourcesMethod) method, (SyncUserResourcesMethodResponse) response);
		}
	}

	/**
	 * Handles sync user resource revisions response.
	 * @param method
	 * @param response
	 */
	private void handleSyncUserResourcesResponse(SyncUserResourcesMethod method, SyncUserResourcesMethodResponse response) {
		// Set the successful revisions as uploaded/synced
		if (response.uploadStatus.isSuccessful()) {
			for (ResourceRevisionEntity resource : method.resources) {
				if (!response.conflicts.containsKey(resource.resourceId) && !resource.revisions.isEmpty()) {
					int fromRevision = resource.revisions.get(0).revision;
					int toRevision = resource.revisions.get(resource.revisions.size() - 1).revision;

					// Same amount of revisions
					assert (toRevision - fromRevision == resource.revisions.size() - 1);

					ResourceLocalRepo.setSyncedUserResource(resource.resourceId, fromRevision, toRevision);
				}
			}
		}

		// Download resources, but remove local first if there exists any revision of
		// those.
		// I.e. the server's sync was replaced, thus the local should also be
		response.downloadStatus = true;
		for (Entry<UUID, ArrayList<ResourceBlobEntity>> entry : response.blobsToDownload.entrySet()) {
			UUID resourceId = entry.getKey();
			ArrayList<ResourceBlobEntity> revisions = entry.getValue();

			// Remove existing revisions
			int firstRevision = ((ResourceRevisionBlobEntity) revisions.get(0)).revision;
			ResourceLocalRepo.removeRevisions(resourceId, firstRevision);

			// Download resources
			response.downloadStatus = mWebRepo.downloadResources(revisions);

			if (!response.downloadStatus) {
				break;
			}
		}

		// Add resource locally
		if (response.downloadStatus) {
			@SuppressWarnings("unchecked")
			ArrayList<RevisionEntity> revisions = Pools.arrayList.obtain();

			for (Entry<UUID, ArrayList<ResourceBlobEntity>> entry : response.blobsToDownload.entrySet()) {
				UUID resourceId = entry.getKey();
				ExternalTypes type = ExternalTypes.fromUploadType(entry.getValue().get(0).uploadType);
				revisions.clear();

				for (ResourceBlobEntity blobEntity : entry.getValue()) {
					if (blobEntity instanceof ResourceRevisionBlobEntity) {
						ResourceRevisionBlobEntity revisionBlobEntity = (ResourceRevisionBlobEntity) blobEntity;
						RevisionEntity revisionEntity = new RevisionEntity();
						revisionEntity.date = revisionBlobEntity.created;
						revisionEntity.revision = revisionBlobEntity.revision;
						revisions.add(revisionEntity);
					}
				}

				ResourceLocalRepo.addRevisions(resourceId, type, revisions);


				// Reload resource if latest already has been loaded
				if (ResourceCacheFacade.isLoaded(resourceId)) {
					ResourceCacheFacade.reload(resourceId);
				}
			}

			Pools.arrayList.free(revisions);

			// Set sync time
			ResourceLocalRepo.setSyncUserResourceDate(response.syncTime);
		}
	}

	/**
	 * Handles sync downloaded response
	 * @param method the sync download method
	 * @param response sync download response
	 */
	private void handleSyncDownloadResponse(SyncDownloadMethod method, SyncDownloadMethodResponse response) {
		if (response.status.isSuccessful()) {
			addDownloaded(response.resources);

			ResourceLocalRepo.setSyncDownloadDate(response.syncTime);
		}
	}

	/**
	 * Handles a download response
	 * @param method the download method
	 * @param response the download response
	 */
	private void handleDownloadResponse(ResourceDownloadMethod method, ResourceDownloadMethodResponse response) {
		// Add all downloaded resources to the local database
		if (response.status.isSuccessful()) {
			addDownloaded(response.resources);
		}
	}

	/**
	 * Add downloaded resources to the database
	 * @param resources all resources that could've been downloaded
	 */
	private void addDownloaded(ArrayList<ResourceBlobEntity> resources) {
		for (ResourceBlobEntity resourceInfo : resources) {
			ResourceLocalRepo.addDownloaded(resourceInfo.resourceId, ExternalTypes.fromUploadType(resourceInfo.uploadType));
		}
	}

	/**
	 * Handles a publish response
	 * @param method the publish method
	 * @param response the publish response
	 */
	private void handlePublishResponse(PublishMethod method, PublishMethodResponse response) {
		if (response.status.isSuccessful()) {
			for (DefEntity defEntity : method.defs) {
				ResourceLocalRepo.removeRevisions(defEntity.resourceId);
				ResourceLocalRepo.setPublished(defEntity.resourceId, true);

				// If level set level as published too
				if (defEntity instanceof LevelDefEntity) {
					ResourceLocalRepo.removeRevisions(((LevelDefEntity) defEntity).levelId);
					ResourceLocalRepo.setPublished(((LevelDefEntity) defEntity).levelId, true);
				}
			}
		}
	}

	/**
	 * Download a resource and its dependencies
	 * @param responseListener listens to the web response
	 * @param resourceId the resource to download
	 */
	public void download(ICallerResponseListener responseListener, UUID resourceId) {
		// Download if we don't have the resource already
		if (!ResourceLocalRepo.exists(resourceId)) {
			mWebRepo.download(resourceId, this, responseListener);
		}
		// Already downloaded -> send response
		else {
			ResourceDownloadMethodResponse response = new ResourceDownloadMethodResponse();
			response.status = ResourceDownloadMethodResponse.Statuses.SUCCESS;
			ResourceDownloadMethod method = new ResourceDownloadMethod();
			method.resourceId = resourceId;
			responseListener.handleWebResponse(method, response);
		}
	}

	/**
	 * Get all non published dependencies from the specified definition
	 * @param def the definition to get all non published dependencies from
	 * @return all non published dependencies of def
	 */
	public static ArrayList<Def> getNonPublishedDependencies(Def def) {
		if (def != null) {
			@SuppressWarnings("unchecked")
			HashSet<UUID> uuidDeps = Pools.hashSet.obtain();
			@SuppressWarnings("unchecked")
			ArrayList<Def> dependencies = Pools.arrayList.obtain();

			getNonPublishedDependencies(def, uuidDeps, dependencies);

			Pools.hashSet.free(uuidDeps);
			return dependencies;
		}
		return null;
	}

	/**
	 * Gets all the non-published def dependencies of the the specified definition
	 * @param def the definition to get the external dependencies from
	 * @param foundUuids all the found dependencies' UUID
	 * @param dependencies all non-published dependencies
	 */
	private static final void getNonPublishedDependencies(Def def, Set<UUID> foundUuids, ArrayList<Def> dependencies) {
		for (Entry<UUID, AtomicInteger> entry : def.getExternalDependencies().entrySet()) {
			if (!foundUuids.contains(entry.getKey()) && !ResourceLocalRepo.isPublished(def.getId())) {
				foundUuids.add(entry.getKey());

				Def dependency = ResourceCacheFacade.get(entry.getKey());
				if (dependency != null) {
					dependencies.add(dependency);

					getNonPublishedDependencies(dependency, foundUuids, dependencies);
				} else {
					Gdx.app.error("ResourceRepo", "Could not find dependency when publishing...");
				}
			}
		}
	}


	/** Instance of this class */
	private static ResourceRepo mInstance = null;
	/** ResourceWebRepo */
	protected ResourceWebRepo mWebRepo = ResourceWebRepo.getInstance();
}

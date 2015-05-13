package com.spiddekauga.voider.repo.resource;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import com.badlogic.gdx.Gdx;
import com.spiddekauga.net.IDownloadProgressListener;
import com.spiddekauga.net.IOutstreamProgressListener;
import com.spiddekauga.voider.game.Level;
import com.spiddekauga.voider.game.actors.ActorDef;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.IMethodEntity;
import com.spiddekauga.voider.network.resource.DefEntity;
import com.spiddekauga.voider.network.resource.DownloadSyncMethod;
import com.spiddekauga.voider.network.resource.DownloadSyncResponse;
import com.spiddekauga.voider.network.resource.LevelDefEntity;
import com.spiddekauga.voider.network.resource.PublishMethod;
import com.spiddekauga.voider.network.resource.PublishResponse;
import com.spiddekauga.voider.network.resource.ResourceBlobEntity;
import com.spiddekauga.voider.network.resource.ResourceConflictEntity;
import com.spiddekauga.voider.network.resource.ResourceDownloadMethod;
import com.spiddekauga.voider.network.resource.ResourceDownloadResponse;
import com.spiddekauga.voider.network.resource.ResourceDownloadResponse.Statuses;
import com.spiddekauga.voider.network.resource.ResourceRevisionBlobEntity;
import com.spiddekauga.voider.network.resource.ResourceRevisionEntity;
import com.spiddekauga.voider.network.resource.RevisionEntity;
import com.spiddekauga.voider.network.resource.UserResourceSyncMethod;
import com.spiddekauga.voider.network.resource.UserResourceSyncResponse;
import com.spiddekauga.voider.repo.IResponseListener;
import com.spiddekauga.voider.repo.Repo;
import com.spiddekauga.voider.resources.Def;
import com.spiddekauga.voider.resources.IResource;
import com.spiddekauga.voider.resources.IResourceRevision;
import com.spiddekauga.voider.utils.Synchronizer;
import com.spiddekauga.voider.utils.Synchronizer.SyncTypes;
import com.spiddekauga.voider.utils.User;

/**
 * Common resource repository for both web and local. Handles requests that affects both.
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class ResourceRepo extends Repo {
	/**
	 * Private constructor to enforce singleton usage
	 */
	private ResourceRepo() {
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
	 * @param progressListener listen to the download progress
	 * @param responseListeners listens to the web response (when syncing is done)
	 */
	public void syncDownload(IDownloadProgressListener progressListener, IResponseListener... responseListeners) {
		Date lastSync = ResourceLocalRepo.getSyncDownloadDate();
		mWebRepo.syncDownloaded(lastSync, progressListener, addToFront(responseListeners, this));
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
	public void save(IResponseListener responseListener, IResource... resources) {

		boolean upload = false;

		for (IResource resource : resources) {
			boolean success = ResourceLocalRepo.save(resource);

			if (success && resource instanceof IResourceRevision) {
				upload = true;
			}
		}

		if (upload) {
			if (User.getGlobalUser().isOnline()) {
				Synchronizer.getInstance().synchronize(SyncTypes.USER_RESOURCES, responseListener);
			}
		}
	}

	/**
	 * Removes the specified resource, will unload it if it is currently loaded
	 * @param resourceId the resource to remove
	 */
	public void remove(UUID resourceId) {
		// Remove it from the database and physically
		ResourceLocalRepo.remove(resourceId);

		// TODO send sync ?
	}

	/**
	 * Synchronizes the user resource revisions, both upload and download
	 * @param progressListener download progress listener
	 * @param responseListeners listens to the web response (when syncing is done)
	 */
	public void syncUserResources(IDownloadProgressListener progressListener, IResponseListener... responseListeners) {
		mSyncUserResourcesProgressListener = progressListener;
		mWebRepo.syncUserResources(ResourceLocalRepo.getUnsyncedUserResources(), ResourceLocalRepo.getRemovedResources(),
				ResourceLocalRepo.getSyncUserResourceDate(), null, null, addToFront(responseListeners, this));
	}

	/**
	 * Synchronizes the user resource revisions, both upload and download and fixes
	 * conflicts
	 * @param conflicts all conflicts to resolve
	 * @param keepLocal how to resolve the conflicts. True if keep local versions, false
	 *        if use server versions.
	 * @param progressListener download progress listener
	 * @param responseListeners listens to the web response (when syncing is done)
	 */
	public void fixUserResourceConflict(HashMap<UUID, ResourceConflictEntity> conflicts, boolean keepLocal,
			IDownloadProgressListener progressListener, IResponseListener... responseListeners) {
		mSyncUserResourcesProgressListener = progressListener;

		HashMap<UUID, ResourceRevisionEntity> unsyncedResources = ResourceLocalRepo.getUnsyncedUserResources();

		// Remove conflicted unsynced resources when keeping server version
		if (!keepLocal) {
			for (UUID conflictId : conflicts.keySet()) {
				unsyncedResources.remove(conflictId);
			}
		}

		mWebRepo.syncUserResources(unsyncedResources, ResourceLocalRepo.getRemovedResources(), ResourceLocalRepo.getSyncUserResourceDate(),
				conflicts, keepLocal, addToFront(responseListeners, this));
	}

	/**
	 * Publish an actor (and its unpublished dependencies) to the server
	 * @param responseListener listens to the web response
	 * @param progressListener listen to upload writing
	 * @param actorDef the actor to publish
	 */
	public void publish(IResponseListener responseListener, IOutstreamProgressListener progressListener, ActorDef actorDef) {
		ArrayList<IResource> resources = new ArrayList<>();

		resources.addAll(getNonPublishedDependencies(actorDef));

		publish(responseListener, progressListener, resources);
	}

	/**
	 * Publish a level (and its unpublished dependencies) to the server
	 * @param responseListener listens to the web response
	 * @param progressListener listen to upload writing
	 * @param level the level to publish
	 */
	public void publish(IResponseListener responseListener, IOutstreamProgressListener progressListener, Level level) {
		ArrayList<IResource> resources = new ArrayList<>();

		resources.addAll(getNonPublishedDependencies(level.getDef()));
		resources.add(level);

		publish(responseListener, progressListener, resources);
	}

	/**
	 * Publish all the resources. In addition frees the ArrayList
	 * @param responseListener listens to the web response
	 * @param progressListener listen to upload writing
	 * @param resources all resources to publish
	 */
	private void publish(IResponseListener responseListener, IOutstreamProgressListener progressListener, ArrayList<IResource> resources) {
		mWebRepo.publish(resources, progressListener, this, responseListener);
	}

	@Override
	public void handleWebResponse(IMethodEntity method, IEntity response) {
		if (response instanceof PublishResponse) {
			handlePublishResponse((PublishMethod) method, (PublishResponse) response);
		} else if (response instanceof ResourceDownloadResponse) {
			handleDownloadResponse((ResourceDownloadMethod) method, (ResourceDownloadResponse) response);
		} else if (response instanceof DownloadSyncResponse) {
			handleSyncDownloadResponse((DownloadSyncMethod) method, (DownloadSyncResponse) response);
		} else if (response instanceof UserResourceSyncResponse) {
			handleSyncUserResourcesResponse((UserResourceSyncMethod) method, (UserResourceSyncResponse) response);
		}
	}

	/**
	 * Set successful user resource revisions as synced
	 * @param method
	 * @param response
	 */
	private void setSuccessfulAsSynced(UserResourceSyncMethod method, UserResourceSyncResponse response) {
		// Set the successful revisions as uploaded/synced
		for (ResourceRevisionEntity resource : method.resources) {
			if (!resource.revisions.isEmpty()) {
				int fromRevision = resource.revisions.get(0).revision;
				int toRevision = resource.revisions.get(resource.revisions.size() - 1).revision;

				// Same amount of revisions
				assert (toRevision - fromRevision == resource.revisions.size() - 1);

				// All revisions were uploaded correctly
				boolean allUploaded = !response.conflicts.containsKey(resource.resourceId)
						&& !response.failedUploads.containsKey(resource.resourceId);
				if (allUploaded) {
					ResourceLocalRepo.setSyncedUserResource(resource.resourceId, fromRevision, toRevision);
				}
				// Set sync for revisions that were successfully updated
				else if (!response.conflicts.containsKey(resource.resourceId)) {
					Set<Integer> failedRevisions = response.failedUploads.get(resource.resourceId);

					// If failed revisions isn't set all failed
					if (failedRevisions != null) {
						for (int revision = fromRevision; revision <= toRevision; ++revision) {
							if (!failedRevisions.contains(revision)) {
								ResourceLocalRepo.setSyncedUserResource(resource.resourceId, revision);
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Handles sync user resource revisions response.
	 * @param method
	 * @param response
	 */
	private void handleSyncUserResourcesResponse(UserResourceSyncMethod method, UserResourceSyncResponse response) {
		if (response.uploadStatus.isSuccessful()) {
			setSuccessfulAsSynced(method, response);


			// Delete resources that should be deleted
			for (UUID removedId : response.resourcesToRemove) {
				// If the resource was published it shouldn't be removed here, only
				// revisions should be removed then...
				if (ResourceLocalRepo.isPublished(removedId)) {
					ResourceLocalRepo.removeRevisions(removedId);
				}
				// Remove everything
				else {
					ResourceLocalRepo.remove(removedId);
				}
			}


			// Remove resources that were synced as removed from this client to the server
			for (UUID removedId : method.resourceToRemove) {
				ResourceLocalRepo.removeFromRemoved(removedId);
			}
		}


		// Delete conflicted resources locally
		if (method.keepServerConflicts() && method.conflictsToFix != null) {
			for (Entry<UUID, ResourceConflictEntity> entry : method.conflictsToFix.entrySet()) {
				ResourceConflictEntity conflictEntity = entry.getValue();
				ResourceLocalRepo.removeRevisions(conflictEntity.resourceId, conflictEntity.fromRevision);
			}
		}


		// Download resources, but remove local first if there exists any revision of
		// those. I.e. the server's sync was replaced, thus the local should also be
		ArrayList<ResourceRevisionBlobEntity> toDownload = new ArrayList<>();
		for (Entry<UUID, ArrayList<ResourceRevisionBlobEntity>> entry : response.blobsToDownload.entrySet()) {
			UUID resourceId = entry.getKey();
			ArrayList<ResourceRevisionBlobEntity> revisions = entry.getValue();

			// Remove existing revisions
			int firstRevision = revisions.get(0).revision;
			ResourceLocalRepo.removeRevisions(resourceId, firstRevision);

			// Download resources
			toDownload.addAll(revisions);
		}

		response.downloadStatus = mWebRepo.downloadResources(toDownload, mSyncUserResourcesProgressListener);


		// Add resource locally
		if (response.downloadStatus) {
			ArrayList<RevisionEntity> revisions = new ArrayList<>();

			for (Entry<UUID, ArrayList<ResourceRevisionBlobEntity>> entry : response.blobsToDownload.entrySet()) {
				UUID resourceId = entry.getKey();
				ExternalTypes type = ExternalTypes.fromUploadType(entry.getValue().get(0).uploadType);
				revisions.clear();

				for (ResourceRevisionBlobEntity blobEntity : entry.getValue()) {
					ResourceRevisionBlobEntity revisionBlobEntity = blobEntity;
					RevisionEntity revisionEntity = new RevisionEntity();
					revisionEntity.date = revisionBlobEntity.created;
					revisionEntity.revision = revisionBlobEntity.revision;
					revisions.add(revisionEntity);
				}

				ResourceLocalRepo.addRevisions(resourceId, type, revisions);


				// Reload resource if latest already has been loaded
				if (ResourceCacheFacade.isLoaded(resourceId)) {
					ResourceCacheFacade.reload(resourceId);
				}
			}
		}

		// Set sync time
		if (response.isSuccessful()) {
			ResourceLocalRepo.setSyncUserResourceDate(response.syncTime);
		}
	}

	/**
	 * Handles sync downloaded response
	 * @param method the sync download method
	 * @param response sync download response
	 */
	private void handleSyncDownloadResponse(DownloadSyncMethod method, DownloadSyncResponse response) {
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
	private void handleDownloadResponse(ResourceDownloadMethod method, ResourceDownloadResponse response) {
		// Add all downloaded resources to the local database
		if (response.isSuccessful() && !method.redownload) {
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
	private void handlePublishResponse(PublishMethod method, PublishResponse response) {
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
	 * Redownload an existing published resource that has become corrupt or is missing
	 * @param resourceId the resource to download again
	 * @param responseListener listens to the web response
	 */
	public void redownload(UUID resourceId, IResponseListener responseListener) {
		redownload(resourceId, -1, responseListener);
	}

	/**
	 * Redownload an existing user resource that has become corrupt or is missing
	 * @param resourceId the resource to download again
	 * @param revision specified revision of the user resource, if -1 it's a published
	 *        resource
	 * @param responseListener listens to the web response
	 */
	public void redownload(UUID resourceId, int revision, IResponseListener responseListener) {
		ResourceDownloadMethod method = new ResourceDownloadMethod();
		method.redownload = true;
		method.resourceId = resourceId;
		method.revision = revision;

		// Only try to redownload resource we have downloaded
		if (ResourceLocalRepo.exists(resourceId)) {
			mWebRepo.download(method, this, responseListener);
		}
		// Does not exists
		else {
			ResourceDownloadResponse response = new ResourceDownloadResponse();
			response.status = Statuses.FAILED_DOWNLOAD;
			responseListener.handleWebResponse(method, response);
		}
	}

	/**
	 * Download a resource and its dependencies
	 * @param resourceId the resource to download
	 * @param responseListener listens to the web response
	 */
	public void download(UUID resourceId, IResponseListener responseListener) {
		ResourceDownloadMethod method = new ResourceDownloadMethod();
		method.resourceId = resourceId;

		// Download if we don't have the resource already
		if (!ResourceLocalRepo.exists(resourceId)) {
			mWebRepo.download(method, this, responseListener);
		}
		// Already downloaded -> send response
		else {
			ResourceDownloadResponse response = new ResourceDownloadResponse();
			response.status = ResourceDownloadResponse.Statuses.SUCCESS;
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
			HashSet<UUID> uuidDeps = new HashSet<>();
			ArrayList<Def> dependencies = new ArrayList<>();

			getNonPublishedDependencies(def, uuidDeps, dependencies);
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
		// Add to found IDs
		if (!foundUuids.contains(def.getId())) {
			foundUuids.add(def.getId());

			// Add if this resource isn't published
			if (!ResourceLocalRepo.isPublished(def.getId())) {
				dependencies.add(def);

				for (Entry<UUID, AtomicInteger> entry : def.getExternalDependencies().entrySet()) {
					Def dependency = ResourceCacheFacade.get(entry.getKey());
					if (dependency != null) {
						getNonPublishedDependencies(dependency, foundUuids, dependencies);
					} else {
						Gdx.app.error("ResourceRepo", "Could not find dependency when publishing...");
					}
				}
			}

		}
	}

	/** Last progress listener for sync user resources */
	private IDownloadProgressListener mSyncUserResourcesProgressListener = null;

	/** Instance of this class */
	private static ResourceRepo mInstance = null;
	/** ResourceWebRepo */
	protected ResourceWebRepo mWebRepo = ResourceWebRepo.getInstance();
}

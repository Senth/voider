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
import com.spiddekauga.voider.network.entities.method.IMethodEntity;
import com.spiddekauga.voider.network.entities.method.PublishMethod;
import com.spiddekauga.voider.network.entities.method.PublishMethodResponse;
import com.spiddekauga.voider.network.entities.method.ResourceDownloadMethod;
import com.spiddekauga.voider.network.entities.method.ResourceDownloadMethodResponse;
import com.spiddekauga.voider.network.entities.method.SyncDownloadMethod;
import com.spiddekauga.voider.network.entities.method.SyncDownloadMethodResponse;
import com.spiddekauga.voider.resources.Def;
import com.spiddekauga.voider.resources.ExternalTypes;
import com.spiddekauga.voider.resources.IResource;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.utils.Pools;

/**
 * Common resource repository for both web and local. Handles requests that affects
 * both.
 * 
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
	 * @param responseListener listens to the web response (when syncing is done)
	 */
	public void syncDownload(ICallerResponseListener responseListener) {
		Date lastSync = ResourceLocalRepo.getSyncDownloadDate();
		mWebRepo.syncDownloaded(lastSync, this, responseListener);
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
	 * Publish a campaign (and its levels and unpublished dependencies) to the server
	 * @param campaignDef the campaign to publish
	 * @param responseListener listens to the web response
	 * @return true if publish was successful
	 */
	// TODO

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
		}
	}

	/**
	 * Handles sync downloaded response
	 * @param method the sync download method
	 * @param response sync download response
	 */
	private void handleSyncDownloadResponse(SyncDownloadMethod method, SyncDownloadMethodResponse response) {
		if (response.status.isSuccessful()) {
			for (ResourceBlobEntity resourceInfo : response.resources) {
				ResourceLocalRepo.addDownloaded(resourceInfo.resourceId, ExternalTypes.fromUploadType(resourceInfo.uploadType));
			}

			ResourceLocalRepo.setDownloadSyncDate(response.syncTime);
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
			for (ResourceBlobEntity resourceInfo : response.resources) {
				ResourceLocalRepo.addDownloaded(resourceInfo.resourceId, ExternalTypes.fromUploadType(resourceInfo.uploadType));
			}
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

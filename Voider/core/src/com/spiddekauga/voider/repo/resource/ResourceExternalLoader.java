package com.spiddekauga.voider.repo.resource;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.ExternalFileHandleResolver;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.spiddekauga.utils.scene.ui.NotificationShower;
import com.spiddekauga.voider.game.GameSave;
import com.spiddekauga.voider.game.GameSaveDef;
import com.spiddekauga.voider.game.Level;
import com.spiddekauga.voider.game.LevelDef;
import com.spiddekauga.voider.game.actors.BulletActorDef;
import com.spiddekauga.voider.game.actors.EnemyActorDef;
import com.spiddekauga.voider.game.actors.PickupActorDef;
import com.spiddekauga.voider.game.actors.PlayerActorDef;
import com.spiddekauga.voider.game.actors.StaticTerrainActorDef;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.IMethodEntity;
import com.spiddekauga.voider.network.resource.ResourceDownloadMethod;
import com.spiddekauga.voider.network.resource.ResourceDownloadResponse;
import com.spiddekauga.voider.network.resource.RevisionEntity;
import com.spiddekauga.voider.repo.IResponseListener;
import com.spiddekauga.voider.repo.user.User;
import com.spiddekauga.voider.resources.BugReportDef;
import com.spiddekauga.voider.resources.Resource;
import com.spiddekauga.voider.resources.ResourceException;
import com.spiddekauga.voider.scene.Scene;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Handles loading user resources
 */
class ResourceExternalLoader extends ResourceLoader<UserResourceIdentifier, Resource> {
/** All resources that are being redownloaded */
private BlockingQueue<UserResourceIdentifier> mRedownloading = new LinkedBlockingQueue<>();
/** Resources that failed to load and redownload */
private BlockingQueue<UserResourceIdentifier> mFailed = new LinkedBlockingQueue<>();
private IResponseListener mRedownloadListener = new IResponseListener() {
	@Override
	public void handleWebResponse(IMethodEntity method, IEntity response) {
		if (method instanceof ResourceDownloadMethod && response instanceof ResourceDownloadResponse) {
			handle((ResourceDownloadMethod) method, (ResourceDownloadResponse) response);
		}
	}

	private void handle(ResourceDownloadMethod method, ResourceDownloadResponse response) {
		UserResourceIdentifier identifier = new UserResourceIdentifier(method.resourceId, method.revision);
		if (response.isSuccessful()) {

			// Find resource
			boolean removed = mRedownloading.remove(identifier);
			if (removed) {
				loadAgain(identifier);
			} else {
				Gdx.app.error("ResourceLoader", "Didn't find the specified resource that was redownloading");
			}
			NotificationShower.getInstance().showSuccess("Redownload successful!");
		}
		// Remove from redownload list
		else {
			mFailed.add(identifier);
			mLoadingQueue.remove(identifier);
			mRedownloading.remove(identifier);
			NotificationShower.getInstance().showError("Failed to redownload resource!");
		}
	}

	/**
	 * Try to load the specified resource again
	 * @param identifier the resource to try to load
	 */
	private void loadAgain(UserResourceIdentifier identifier) {
		// Load the resource again
		LoadedResource loadedResource = mLoadingQueue.get(identifier);

		ExternalTypes type = ResourceLocalRepo.getType(identifier.resourceId);
		if (type != null) {
			mAssetManager.load(loadedResource.filepath, type.getClassType());
		}
	}
};

/**
 * Initializes the resource loader
 * @param assetManager the asset manager
 */
ResourceExternalLoader(AssetManager assetManager) {
	super(assetManager);

	FileHandleResolver externalFileHandleResolver = new ExternalFileHandleResolver();
	mAssetManager.setLoader(BulletActorDef.class, new KryoLoaderAsync<>(externalFileHandleResolver, BulletActorDef.class));
	mAssetManager.setLoader(EnemyActorDef.class, new KryoLoaderAsync<>(externalFileHandleResolver, EnemyActorDef.class));
	mAssetManager.setLoader(PickupActorDef.class, new KryoLoaderAsync<>(externalFileHandleResolver, PickupActorDef.class));
	mAssetManager.setLoader(PlayerActorDef.class, new KryoLoaderAsync<>(externalFileHandleResolver, PlayerActorDef.class));
	mAssetManager.setLoader(StaticTerrainActorDef.class, new KryoLoaderAsync<>(externalFileHandleResolver, StaticTerrainActorDef.class));
	mAssetManager.setLoader(LevelDef.class, new KryoLoaderAsync<>(externalFileHandleResolver, LevelDef.class));
	mAssetManager.setLoader(Level.class, new KryoLoaderAsync<>(externalFileHandleResolver, Level.class));
	mAssetManager.setLoader(GameSave.class, new KryoLoaderSync<>(externalFileHandleResolver, GameSave.class));
	mAssetManager.setLoader(GameSaveDef.class, new KryoLoaderAsync<>(externalFileHandleResolver, GameSaveDef.class));
	mAssetManager.setLoader(BugReportDef.class, new KryoLoaderAsync<>(externalFileHandleResolver, BugReportDef.class));
}

/**
 * Loads the specified resource. If the resource has been loaded already it does nothing.
 * @param scene the scene which is loading the resource
 * @param resourceId the resource that should be loaded
 * @param revision the revision to load. Set to -1 if the resource doesn't have revisions or if you
 * want to load the latest revision. Note that if the resource already has been loaded, this
 * variable will have no effect. I.e. it will not load another revision of the resource as only one
 * revision is allowed.
 */
synchronized void load(Scene scene, UUID resourceId, int revision) {
	UserResourceIdentifier identifier = new UserResourceIdentifier();
	identifier.resourceId = resourceId;
	identifier.revision = getCorrectRevision(resourceId, revision);

	try {
		load(scene, identifier);
	} catch (ResourceNotFoundException e) {
		throw e;
	}
}

/**
 * @param resourceId the resource id to get a correct revision for
 * @param revision the revision to check
 * @return a correct id for the resource, i.e. if it's the latest revision it will return the
 * special number LATEST_REVISION instead
 */
private int getCorrectRevision(UUID resourceId, int revision) {
	if (revision > 0) {
		RevisionEntity latestRevision = ResourceLocalRepo.getRevisionLatest(resourceId);
		if (latestRevision != null && revision < latestRevision.revision) {
			return revision;
		}
	}

	return UserResourceIdentifier.LATEST_REVISION;
}

@Override
protected Class<?> getType(UserResourceIdentifier identifier) {
	ExternalTypes type = ResourceLocalRepo.getType(identifier.resourceId);
	if (type != null) {
		return type.getClassType();
	} else {
		throw new ResourceNotFoundException(identifier.resourceId);
	}
}

@Override
void handleException(GdxRuntimeException exception) {
	RuntimeException throwException = exception;

	if (exception.getCause() != null && exception.getCause().getCause() != null && exception.getCause().getCause().getCause() != null) {
		Throwable source = exception.getCause().getCause().getCause();
		Class<?> type = source.getClass();
		ResourceException resourceException = null;

		if (type == ResourceNotFoundException.class) {
			resourceException = new ResourceNotFoundException(source.getMessage());
		} else if (type == ResourceCorruptException.class) {
			resourceException = new ResourceCorruptException(source.getMessage());
		}

		NotificationShower notification = NotificationShower.getInstance();
		if (User.getGlobalUser().isOnline()) {
			notification.showError("Found a corrupt or missing file, redownloading...");
			notification.showHighlight("This may take a while");
		} else {
			notification.showError("Found a corrupt or missing file, aborting load");
			notification.showHighlight("Go online and it will redownload itself next time its loaded");
		}

		// Redownload the resource
		if (resourceException != null) {
			UserResourceIdentifier uuidRevision = new UserResourceIdentifier().set(resourceException.getId(), resourceException.getRevision());
			if (User.getGlobalUser().isOnline()) {
				mRedownloading.add(uuidRevision);
				ResourceRepo.getInstance().redownload(uuidRevision.resourceId, uuidRevision.revision, mRedownloadListener);
				throwException = null;
			} else {
				mLoadingQueue.remove(uuidRevision);
				throwException = resourceException;
			}
		}
	}

	if (throwException != null) {
		throw throwException;
	}
}

@Override
protected String getFilepath(UserResourceIdentifier identifier) {
	if (identifier.revision > 0) {
		return ResourceLocalRepo.getRevisionFilepath(identifier.resourceId, identifier.revision);
	} else {
		return ResourceLocalRepo.getFilepath(identifier.resourceId);
	}
}

@Override
protected void onReloaded(Resource oldResource, Resource newResource) {
	oldResource.set(newResource);
}

/**
 * Check if the resource is being loaded into the specified scene
 * @param scene if this resource has been loaded into this scene, null to use any
 * @param resourceId if this resource has been loaded
 * @param revision if this revision has been loaded
 * @return true if the resource is being loaded into the specified scene
 */
synchronized boolean isLoading(Scene scene, UUID resourceId, int revision) {
	int correctRevision = getCorrectRevision(resourceId, revision);
	UserResourceIdentifier identifier = new UserResourceIdentifier(resourceId, correctRevision);

	return isLoading(scene, identifier);
}

/**
 * Check if the resource has been loaded into the specified scene
 * @param scene if this resource has been loaded into this scene
 * @param resourceId if this resource has been loaded
 * @return true if the resource has been loaded into the specified scene
 */
synchronized boolean isLoaded(Scene scene, UUID resourceId) {
	return isLoaded(scene, resourceId, UserResourceIdentifier.LATEST_REVISION);
}

/**
 * Check if the resource has been loaded into the specified scene
 * @param scene if this resource has been loaded into this scene, null to use any
 * @param resourceId if this resource has been loaded
 * @param revision if this revision has been loaded
 * @return true if the resource has been loaded into the specified scene
 */
synchronized boolean isLoaded(Scene scene, UUID resourceId, int revision) {
	int correctRevision = getCorrectRevision(resourceId, revision);
	UserResourceIdentifier identifier = new UserResourceIdentifier(resourceId, correctRevision);

	return isLoaded(scene, identifier);
}

/**
 * @param resourceId the resource to check if it has been loaded
 * @return true if the resource has been loaded
 */
synchronized boolean isResourceLoaded(UUID resourceId) {
	return isLoaded(resourceId, UserResourceIdentifier.LATEST_REVISION);
}

/**
 * @param resourceId the resource to check if it has been loaded
 * @param revision if the specified revision is loaded
 * @return true if the resource has been loaded
 */
synchronized boolean isLoaded(UUID resourceId, int revision) {
	return isLoaded(null, resourceId, revision);
}

/**
 * @param <ResourceType> Type of resources to get
 * @param type the type of resources to return
 * @return all loaded resources of the specified type
 */
synchronized <ResourceType extends Resource> ArrayList<ResourceType> getResourcesOf(ExternalTypes type) {
	return getResourcesOf(type.getClassType());
}

/**
 * Unloads the resource, does nothing if the resource isn't loaded.
 * @param resourceId the resource to unload
 */
void unload(UUID resourceId) {
	unload(resourceId, UserResourceIdentifier.LATEST_REVISION);
}

/**
 * Unloads the specified resource, does nothing if the resource isn't loaded.
 * @param resourceId the resource to unload
 * @param revision the revision of the resource to unload
 */
synchronized void unload(UUID resourceId, int revision) {
	UserResourceIdentifier identifier = new UserResourceIdentifier(resourceId, revision);
	unload(identifier);
}

/**
 * Sets the latest resource to the specified resource
 * @param resource the resource to be set as latest resource
 * @param oldRevision old revision that the resource was loaded into
 */
synchronized void setLatestResource(Resource resource, int oldRevision) {
	Resource latestResource = getResource(resource.getId());

	if (latestResource != null) {
		// Unload old revision
		String oldRevisionFilepath = ResourceLocalRepo.getRevisionFilepath(resource.getId(), oldRevision);
		mAssetManager.unload(oldRevisionFilepath);

		// Remove old revision from loaded
		UserResourceIdentifier identifier = new UserResourceIdentifier(resource.getId(), oldRevision);
		mLoadedResources.remove(identifier);

		// Reload latest revision
		String latestFilepath = ResourceLocalRepo.getFilepath(resource);
		mAssetManager.unload(latestFilepath);
		mAssetManager.load(latestFilepath, resource.getClass());
		mAssetManager.finishLoading();

		// Update existing resource to use the new latest resource
		Resource newLatestResource = mAssetManager.get(latestFilepath);
		latestResource.set(newLatestResource);
		resource.set(newLatestResource);
	} else {
		Gdx.app.error("ResourceLoader", "Could not find latest resource when setting latest resource");
	}
}

/**
 * Get the latest revision of the resource, or simple the resource if it doesn't have any revisions
 * @param <ResourceType> the resource type
 * @param resourceId id of the resource to return
 * @return the loaded resource with the specified id, null if not loaded
 */
synchronized <ResourceType extends Resource> ResourceType getResource(UUID resourceId) {
	return getResource(resourceId, UserResourceIdentifier.LATEST_REVISION);
}

/**
 * @param <ResourceType> the resource type
 * @param resourceId id of the resource to return
 * @param revision the resource revision to return
 * @return the loaded resource with the specified id, null if not loaded
 */
synchronized <ResourceType extends Resource> ResourceType getResource(UUID resourceId, int revision) {
	int correctRevision = getCorrectRevision(resourceId, revision);
	UserResourceIdentifier identifier = new UserResourceIdentifier(resourceId, correctRevision);

	return getResource(identifier);
}

/**
 * Reloads the latest resource. Useful when a new revision has been added of the resource during
 * sync.
 * @param resourceId id of the resource to reload
 */
synchronized void reload(UUID resourceId) {
	UserResourceIdentifier identifier = new UserResourceIdentifier(resourceId, UserResourceIdentifier.LATEST_REVISION);
	reload(identifier);
}

/**
 * @return all resources that failed to be downloaded
 */
BlockingQueue<UserResourceIdentifier> getFailedDownloaded() {
	return mFailed;
}


}

package com.spiddekauga.voider.repo.resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.ini4j.Ini;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.MusicLoader;
import com.badlogic.gdx.assets.loaders.SkinLoader;
import com.badlogic.gdx.assets.loaders.SoundLoader;
import com.badlogic.gdx.assets.loaders.TextureAtlasLoader;
import com.badlogic.gdx.assets.loaders.resolvers.ExternalFileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Logger;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.VoiderGame;
import com.spiddekauga.voider.game.GameSave;
import com.spiddekauga.voider.game.GameSaveDef;
import com.spiddekauga.voider.game.Level;
import com.spiddekauga.voider.game.LevelDef;
import com.spiddekauga.voider.game.PlayerStats;
import com.spiddekauga.voider.game.actors.BulletActorDef;
import com.spiddekauga.voider.game.actors.EnemyActorDef;
import com.spiddekauga.voider.game.actors.PickupActorDef;
import com.spiddekauga.voider.game.actors.PlayerActorDef;
import com.spiddekauga.voider.game.actors.StaticTerrainActorDef;
import com.spiddekauga.voider.network.resource.RevisionEntity;
import com.spiddekauga.voider.resources.BugReportDef;
import com.spiddekauga.voider.resources.IResource;
import com.spiddekauga.voider.resources.Resource;
import com.spiddekauga.voider.resources.ResourceException;
import com.spiddekauga.voider.scene.Scene;
import com.spiddekauga.voider.utils.AbsoluteFileHandleResolver;
import com.spiddekauga.voider.utils.Pool;

/**
 * Handles loading resources
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
class ResourceLoader {
	/**
	 * Initializes the resource loader
	 */
	ResourceLoader() {
		if (Config.File.USE_EXTERNAL_RESOURCES) {
			mAssetManager = new AssetManager(new AbsoluteFileHandleResolver());
		} else {
			mAssetManager = new AssetManager();
		}
		mAssetManager.getLogger().setLevel(Logger.DEBUG);

		// External
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
		mAssetManager.setLoader(PlayerStats.class, new KryoLoaderAsync<>(externalFileHandleResolver, PlayerStats.class));
		mAssetManager.setLoader(BugReportDef.class, new KryoLoaderAsync<>(externalFileHandleResolver, BugReportDef.class));


		// Internal
		FileHandleResolver internalFileHandleResolver = null;
		if (Config.File.USE_EXTERNAL_RESOURCES) {
			internalFileHandleResolver = new AbsoluteFileHandleResolver();
		} else {
			internalFileHandleResolver = new InternalFileHandleResolver();
		}
		mAssetManager.setLoader(ShaderProgram.class, new ShaderLoader(internalFileHandleResolver));
		mAssetManager.setLoader(Skin.class, new SkinLoader(internalFileHandleResolver));
		mAssetManager.setLoader(Ini.class, new IniLoader(internalFileHandleResolver));
		mAssetManager.setLoader(Music.class, new MusicLoader(internalFileHandleResolver));
		mAssetManager.setLoader(Sound.class, new SoundLoader(internalFileHandleResolver));
		mAssetManager.setLoader(TextureAtlas.class, new TextureAtlasLoader(internalFileHandleResolver));
		mAssetManager.setLoader(String.class, new TextLoader(internalFileHandleResolver));
	}

	/**
	 * @param resourceId the resource id to get a correct revision for
	 * @param revision the revision to check
	 * @return a correct id for the resource, i.e. if it's the latest revision it will
	 *         return the special number LATEST_REVISION instead
	 */
	private int getCorrectRevision(UUID resourceId, int revision) {
		if (revision > 0) {
			RevisionEntity latestRevision = ResourceLocalRepo.getRevisionLatest(resourceId);
			if (latestRevision != null && revision < latestRevision.revision) {
				return revision;
			}
		}

		return LATEST_REVISION;
	}

	/**
	 * Loads the specified resource. If the resource has been loaded already it does
	 * nothing.
	 * @param scene the scene which is loading the resource
	 * @param resourceId the resource that should be loaded
	 * @param revision the revision to load. Set to -1 if the resource doesn't have
	 *        revisions or if you want to load the latest revision. Note that if the
	 *        resource already has been loaded, this variable will have no effect. I.e. it
	 *        will not load another revision of the resource as only one revision is
	 *        allowed.
	 */
	void load(Scene scene, UUID resourceId, int revision) {
		UuidRevision uuidRevision = mUuidRevisionPool.obtain();
		uuidRevision.resourceId = resourceId;
		uuidRevision.revision = getCorrectRevision(resourceId, revision);

		// If loaded or loading -> do nothing
		if (mLoadedResources.containsKey(uuidRevision) || mLoadingQueue.containsKey(uuidRevision)) {
			return;
		}


		LoadedResource loadedResource = new LoadedResource();
		loadedResource.scene = scene;


		// Set filepath
		if (uuidRevision.revision > 0) {
			loadedResource.filepath = ResourceLocalRepo.getRevisionFilepath(resourceId, revision);
		} else {
			loadedResource.filepath = ResourceLocalRepo.getFilepath(resourceId);
		}


		ExternalTypes type = ResourceLocalRepo.getType(resourceId);
		if (type != null) {
			mAssetManager.load(loadedResource.filepath, type.getClassType());
		} else {
			mUuidRevisionPool.free(uuidRevision);
			throw new ResourceNotFoundException(loadedResource.filepath, resourceId);
		}

		mLoadingQueue.put(uuidRevision, loadedResource);
	}

	/**
	 * @param resourceId the resource to check if it has been loaded
	 * @param revision if the specified revision is loaded
	 * @return true if the resource has been loaded
	 */
	boolean isResourceLoaded(UUID resourceId, int revision) {
		int correctRevision = getCorrectRevision(resourceId, revision);
		UuidRevision uuidRevision = mUuidRevisionPool.obtain().set(resourceId, correctRevision);
		boolean found = mLoadedResources.containsKey(uuidRevision);
		mUuidRevisionPool.free(uuidRevision);
		return found;
	}

	/**
	 * @param resourceId the resource to check if it has been loaded
	 * @return true if the resource has been loaded
	 */
	boolean isResourceLoaded(UUID resourceId) {
		return isResourceLoaded(resourceId, LATEST_REVISION);
	}

	/**
	 * @return true if loading, unloading, or reloading
	 */
	synchronized boolean isLoading() {
		return !mLoadingQueue.isEmpty() || !mReloadQueue.isEmpty() || !mUnloadQueue.isEmpty() || mAssetManager.getQueuedAssets() > 0;
	}

	/**
	 * Get the latest revision of the resource, or simple the resource if it doesn't have
	 * any revisions
	 * @param <ResourceType> the resource type
	 * @param resourceId id of the resource to return
	 * @return the loaded resource with the specified id, null if not loaded
	 */
	<ResourceType extends IResource> ResourceType getLoadedResource(UUID resourceId) {
		return getLoadedResource(resourceId, LATEST_REVISION);
	}

	/**
	 * @param <ResourceType> the resource type
	 * @param resourceId id of the resource to return
	 * @param revision the resource revision to return
	 * @return the loaded resource with the specified id, null if not loaded
	 */
	@SuppressWarnings("unchecked")
	<ResourceType extends IResource> ResourceType getLoadedResource(UUID resourceId, int revision) {
		int correctRevision = getCorrectRevision(resourceId, revision);
		UuidRevision uuidRevision = mUuidRevisionPool.obtain().set(resourceId, correctRevision);
		LoadedResource loadedResource = mLoadedResources.get(uuidRevision);
		mUuidRevisionPool.free(uuidRevision);
		if (loadedResource != null) {
			return (ResourceType) loadedResource.resource;
		} else {
			return null;
		}
	}

	/**
	 * @param <ResourceType> Type of resources to get
	 * @param type the type of resources to return
	 * @return all loaded resources of the specified type
	 */
	@SuppressWarnings("unchecked")
	<ResourceType extends IResource> ArrayList<ResourceType> getAllLoadedResourcesOf(ExternalTypes type) {
		ArrayList<ResourceType> resources = new ArrayList<>();

		for (Entry<UuidRevision, LoadedResource> entry : mLoadedResources.entrySet()) {
			if (type.getClassType().isAssignableFrom(entry.getValue().resource.getClass())) {
				resources.add((ResourceType) entry.getValue().resource);
			}
		}

		return resources;
	}

	/**
	 * Unloads all resources allocated in the specified scene
	 * @param scene unload all resources in this scene
	 */
	void unload(Scene scene) {
		Set<Entry<UuidRevision, LoadedResource>> entrySet = mLoadedResources.entrySet();
		Iterator<Entry<UuidRevision, LoadedResource>> iterator = entrySet.iterator();

		while (iterator.hasNext()) {
			Entry<UuidRevision, LoadedResource> entry = iterator.next();

			LoadedResource loadedResource = entry.getValue();
			if (loadedResource.scene == scene) {
				mAssetManager.unload(loadedResource.filepath);
				iterator.remove();
				mUuidRevisionPool.free(entry.getKey());
			}
		}
	}

	/**
	 * Unloads the resource, does nothing if the resource isn't loaded.
	 * @param resourceId the resource to unload
	 */
	void unload(UUID resourceId) {
		unload(resourceId, LATEST_REVISION);
	}

	/**
	 * Unloads the specified resource, does nothing if the resource isn't loaded.
	 * @param resourceId the resource to unload
	 * @param revision the revision of the resource to unload
	 */
	void unload(UUID resourceId, int revision) {
		UuidRevision uuidRevision = mUuidRevisionPool.obtain().set(resourceId, revision);
		LoadedResource loadedResource = mLoadedResources.get(uuidRevision);

		// Unload if the resource is loaded
		if (loadedResource != null) {

			mUnloadQueue.add(loadedResource.filepath);

			// Unload directly if main thread
			if (VoiderGame.isMainThread()) {
				do {
					update();
				} while (isLoading());
			} else {
				waitTillLoadingIsDone();
			}

			mLoadedResources.remove(uuidRevision);
		}
	}

	/**
	 * Sets the latest resource to the specified resource
	 * @param resource the resource to be set as latest resource
	 * @param oldRevision old revision that the resource was loaded into
	 */
	void setLatestResource(Resource resource, int oldRevision) {
		Resource latestResource = getLoadedResource(resource.getId());

		if (latestResource != null) {
			// Unload old revision
			String oldRevisionFilepath = ResourceLocalRepo.getRevisionFilepath(resource.getId(), oldRevision);
			mAssetManager.unload(oldRevisionFilepath);

			// Remove old revision from loaded
			UuidRevision uuidRevision = mUuidRevisionPool.obtain().set(resource.getId(), oldRevision);
			mLoadedResources.remove(uuidRevision);
			mUuidRevisionPool.free(uuidRevision);

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
	 * Reloads the latest resource. Useful when a new revision has been added of the
	 * resource during sync.
	 * @param resourceId id of the resource to reload
	 */
	void reload(UUID resourceId) {
		final Resource oldResource = getLoadedResource(resourceId);

		if (oldResource != null) {
			// Reload latest revision
			final String latestFilepath = ResourceLocalRepo.getFilepath(resourceId);
			mReloadQueue.add(new ReloadResource(latestFilepath, oldResource));

			// Reload directly if main thread
			if (VoiderGame.isMainThread()) {
				do {
					update();
				} while (isLoading());
			} else {
				waitTillLoadingIsDone();
			}
		} else {
			Gdx.app.error("ResourceLoader", "Latest resource was not loaded while trying to reload it");
		}
	}

	/**
	 * Waits until the loading is done
	 */
	void waitTillLoadingIsDone() {
		while (isLoading()) {
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				// Do nothing
			}
		}
	}

	/**
	 * Handle exception
	 * @param exception the exception to handle
	 */
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

			// Remove the loading resource
			if (resourceException != null) {
				UuidRevision uuidRevision = mUuidRevisionPool.obtain().set(resourceException.getId(), resourceException.getRevision());
				mLoadingQueue.remove(uuidRevision);
				mUuidRevisionPool.free(uuidRevision);
				throwException = resourceException;
			}
		}

		throw throwException;
	}

	/**
	 * Updates the resource loader. This will set the loaded resources correctly
	 * @return true if all resources has been loaded
	 */
	synchronized boolean update() {
		try {
			mAssetManager.update();
		} catch (GdxRuntimeException e) {
			handleException(e);
		}

		// Unload queue
		Iterator<String> unloadIt = mUnloadQueue.iterator();
		while (unloadIt.hasNext()) {
			String filepath = unloadIt.next();
			if (mAssetManager.isLoaded(filepath)) {
				mAssetManager.unload(filepath);
			}

			unloadIt.remove();
		}

		// Reload queue
		Iterator<ReloadResource> reloadIt = mReloadQueue.iterator();
		while (reloadIt.hasNext()) {
			ReloadResource reloadResource = reloadIt.next();

			// Check if it has been loaded and set the new resource
			if (reloadResource.unloaded) {
				if (mAssetManager.isLoaded(reloadResource.filepath)) {

					Resource newResource = mAssetManager.get(reloadResource.filepath);
					reloadResource.oldResource.set(newResource);

					reloadIt.remove();
				}
			}
			// Unload the resource
			else {
				mAssetManager.unload(reloadResource.filepath);
				mAssetManager.load(reloadResource.filepath, reloadResource.oldResource.getClass());
				reloadResource.unloaded = true;
			}
		}

		// Loading queue
		Iterator<Entry<UuidRevision, LoadedResource>> loadIt = mLoadingQueue.entrySet().iterator();

		while (loadIt.hasNext()) {
			Entry<UuidRevision, LoadedResource> entry = loadIt.next();
			LoadedResource loadingResource = entry.getValue();

			// Add resource if it has been loaded
			if (mAssetManager.isLoaded(loadingResource.filepath)) {
				loadingResource.resource = mAssetManager.get(loadingResource.filepath);

				mLoadedResources.put(entry.getKey(), loadingResource);

				loadIt.remove();
			}
		}

		return !isLoading();
	}

	/**
	 * @return asset manager used for loading resources
	 */
	AssetManager getAssetManager() {
		return mAssetManager;
	}

	/**
	 * Wrapper for unique UUID together with a revision
	 */
	private static class UuidRevision implements Poolable {
		/**
		 * Sets the resource and revision
		 * @param resourceId the resource id
		 * @param revision the revision to use
		 * @return this object for chaining
		 */
		public UuidRevision set(UUID resourceId, int revision) {
			this.resourceId = resourceId;
			this.revision = revision;
			return this;
		}

		/** UUID of the resource */
		UUID resourceId = null;
		/** revision of the resource */
		int revision = LATEST_REVISION;

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((resourceId == null) ? 0 : resourceId.hashCode());
			result = prime * result + revision;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			UuidRevision other = (UuidRevision) obj;
			if (resourceId == null) {
				if (other.resourceId != null) {
					return false;
				}
			} else if (!resourceId.equals(other.resourceId)) {
				return false;
			}
			if (revision != other.revision) {
				return false;
			}
			return true;
		}

		@Override
		public void reset() {
			resourceId = null;
			revision = LATEST_REVISION;
		}
	}

	/**
	 * Loaded or loading resources
	 */
	private static class LoadedResource {
		/** File path of the resource */
		String filepath = null;
		/** Which scene this resource should be loaded into */
		Scene scene = null;
		/** The actual resource that was loaded */
		IResource resource = null;
	}

	/**
	 * Reload queue class
	 */
	private static class ReloadResource {
		/**
		 * Constructor
		 * @param filepath the file path of the resource to unload
		 * @param oldResource the old loaded resource
		 */
		ReloadResource(String filepath, Resource oldResource) {
			this.filepath = filepath;
			this.oldResource = oldResource;
		}

		/** File path of the resource */
		String filepath;
		/** Old loaded resource */
		Resource oldResource;
		/** True if the resource has been unloaded */
		boolean unloaded = false;
	}

	/** Latest resource */
	private static final int LATEST_REVISION = -1;
	/** Pool for UuidRevision */
	private Pool<UuidRevision> mUuidRevisionPool = new Pool<>(UuidRevision.class, 16);
	/** All loaded resources */
	private HashMap<UuidRevision, LoadedResource> mLoadedResources = new HashMap<>();
	/** All resources that are currently loading */
	private HashMap<UuidRevision, LoadedResource> mLoadingQueue = new HashMap<>();
	/** Unload queue */
	private ArrayList<ReloadResource> mReloadQueue = new ArrayList<>();
	/** Unload queue */
	private ArrayList<String> mUnloadQueue = new ArrayList<>();

	/** The asset manager used for actually loading resources */
	private AssetManager mAssetManager;
}

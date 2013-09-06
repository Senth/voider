package com.spiddekauga.voider.resources;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.UUID;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectMap.Entry;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.scene.Scene;
import com.spiddekauga.voider.utils.Pool;
import com.spiddekauga.voider.utils.Pools;

/**
 * Contains all current resources, revisions and loaded resources
 * 
 * @todo ability to remove saved resources and revisions
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
class ResourceDatabase {
	/**
	 * Builds the resource database
	 * @param assetManager the asset manager to load files with
	 */
	static void init(AssetManager assetManager) {
		mAssetManager = assetManager;

		ObjectMap<Class<?>, String> resourcePaths = ResourceNames.getResourcePaths();

		for (ObjectMap.Entry<Class<?>, String> entry : resourcePaths.entries()) {
			FileHandle dir = Gdx.files.external(entry.value);

			if (!dir.exists() || !dir.isDirectory()) {
				continue;
			}

			// Resource revision
			if (IResourceRevision.class.isAssignableFrom(entry.key)) {
				buildResourceRevisionDb(entry.key, dir);
			}
			// Simple resource
			else if (IResource.class.isAssignableFrom(entry.key)) {
				buildResourceDb(entry.key, dir);
			}
		}
	}

	/**
	 * Iterates through a resource with revision and builds its database
	 * @param type what type of class the resource is
	 * @param dir the directory where the resources of this type is located
	 */
	private static void buildResourceRevisionDb(Class<?> type, FileHandle dir) {
		FileHandle[] resourceDirs = dir.list();

		for (FileHandle revisionDir : resourceDirs) {
			if (!revisionDir.isDirectory()) {
				continue;
			}

			// Test if directory is a UUID
			try {
				UUID resourceId = UUID.fromString(revisionDir.name());

				ResourceInfo resourceDb = new ResourceInfo(type);

				// Add all revisions, the file can contain both revision and
				// date in that case split the string and store both
				for (FileHandle revisionFile : revisionDir.list()) {
					int splitPos = revisionFile.name().indexOf('_');

					String revisionString = null;
					String dateString = null;

					// Get revision and date substrings
					if (splitPos != -1) {
						revisionString = revisionFile.name().substring(0, splitPos);
						dateString = revisionFile.name().substring(splitPos + 1);
					} else {
						revisionString = revisionFile.name();
					}

					if (revisionString.length() != Config.File.REVISION_LENGTH) {
						Gdx.app.debug("ResourceDatabase", "Revision is not the correct length!" + revisionFile.path());
					}

					// Convert revision string to integer
					int revision = Integer.parseInt(revisionString);

					resourceDb.addRevision(revision, dateString);
				}

				mResources.put(resourceId, resourceDb);
			} catch (IllegalArgumentException e) {
				Gdx.app.debug("ResourceDatabase", "Directory was not a resource! " + revisionDir.path());
			}
		}
	}

	/**
	 * Iterates through a simple resource and builds its database
	 * @param type what type of class the resource is
	 * @param dir the directory where the resources of this type is located
	 */
	private static void buildResourceDb(Class<?> type, FileHandle dir) {
		FileHandle[] resources = dir.list();

		for (FileHandle resource : resources) {
			// Test if file is UUID
			try {
				UUID resourceId = UUID.fromString(resource.name());

				// Save resource
				ResourceInfo resourceDb = new ResourceInfo(type);
				mResources.put(resourceId, resourceDb);

			} catch (IllegalArgumentException e) {
				Gdx.app.debug("ResourceDatabase", "File was not a resource! " + resource.path());
			}
		}
	}

	/**
	 * Gets the fully qualified file path name for the resource
	 * @param resource the resource to get the file path for
	 * @return file path of the resource
	 */
	static String getFilePath(IResource resource) {
		String filePath = ResourceNames.getDirPath(resource.getClass()) + resource.getId().toString();

		if (resource instanceof IResourceRevision) {
			filePath += "/" + getRevisionFormat(((IResourceRevision) resource).getRevision());

			if (resource instanceof Def) {
				String date = ((Def) resource).getDateString();
				filePath += "_" + date;
			}
		}

		return filePath;
	}

	//	/**
	//	 * Gets the fully qualified file path name for the resource
	//	 * @param type the type of resource
	//	 * @param uuid the id of the resource
	//	 * @return file path of the resource
	//	 */
	//	static String getFilePath(Class<?> type, UUID uuid) {
	//		return ResourceNames.getDirPath(type) + uuid.toString();
	//	}

	/**
	 * @param revision the revision to get the format of
	 * @return revision file format from the revision
	 */
	private static String getRevisionFormat(int revision) {
		return String.format("%010d", revision);
	}

	/**
	 * Gets the fully qualified file path name for the resource
	 * @param resourceId id of the resource
	 * @param revision the revision to get, -1 to use latest revision, i.e.
	 * regular path
	 * @return file path of the resource
	 */
	static String getFilePath(UUID resourceId, int revision) {
		ResourceInfo resourceDb = mResources.get(resourceId);

		if (resourceDb != null) {
			if (resourceDb.revisionDates != null) {
				if (revision > 0) {
					String date = resourceDb.revisionDates.get(revision);

					if (date != null) {
						String filePath = ResourceNames.getDirPath(resourceDb.type) + resourceId.toString() + "/";
						filePath += getRevisionFormat(revision) + "_" + date;
						return filePath;
					} else {
						Gdx.app.error("ResourceDatabase", "Could not find revision (" + revision + ") for this resource in getFilePath");
					}

				} else {
					Gdx.app.error("ResourceDatabase", "Invalid revision (" + revision + "for resource database.");
				}
			} else {
				return ResourceNames.getDirPath(resourceDb.type) + resourceId.toString();
			}
		} else {
			Gdx.app.error("ResourceDatabase", "Could not find resource when getting file path!");
		}

		return null;
	}

	/**
	 * Updates the resource database. This will set the loaded resources correctly
	 * @return true if all resources has been loaded
	 */
	static boolean update() {
		Iterator<LoadingQueueItem> iterator = mLoadingQueue.iterator();

		while (iterator.hasNext()) {
			LoadingQueueItem loadingQueueItem = iterator.next();

			// Add resource if it has been loaded
			if (mAssetManager.isLoaded(loadingQueueItem.filePath)) {
				IResource resource = mAssetManager.get(loadingQueueItem.filePath);
				mLoadedResources.setLoadedResource(loadingQueueItem.scene, resource);
				iterator.remove();
			}
		}

		return mLoadingQueue.isEmpty();
	}

	/**
	 * @return true if all resources has been loaded
	 */
	static boolean isAllResourcesLoaded() {
		return mLoadingQueue.isEmpty();
	}

	/**
	 * Checks whether the resource has been loaded or not
	 * @param scene the scene to check if the resource has been loaded in
	 * @param resourceId the resource to check if it has been loaded
	 * @param revision the revision that has been loaded. If the resource doesn't have a resource, set
	 * this to -1.
	 * @return true if the resource has been loaded in the specified scene, false if not.
	 */
	static boolean isResourceLoaded(Scene scene, UUID resourceId, int revision) {
		return mLoadedResources.getLoadedResource(scene, resourceId, revision) != null;
	}

	/**
	 * Load the specified resource. If the resource already has been loaded it will
	 * not try to load the resource
	 * @param scene the scene which is loading
	 * @param resourceId the resource that is currently being loaded
	 * @param type the type of the resource to load
	 * @param revision the revision to load. Note that if the resource already has
	 * been loaded, this variable will have no effect. I.e. it will not load another
	 * revision of the resource as only one revision per scene and resource is allowed.
	 */
	static void load(Scene scene, UUID resourceId, Class<?> type, int revision) {
		boolean alreadyIsLoading = mLoadedResources.addLoadingResource(scene, resourceId, type, revision);

		if (!alreadyIsLoading) {
			int revisionToUse = -1;
			if (IResourceRevision.class.isAssignableFrom(type)) {
				revisionToUse = revision;
			}

			String filePath = getFilePath(resourceId, revisionToUse);
			mAssetManager.load(filePath, type);

			LoadingQueueItem loadingQueueItem = mLoadingQueuePool.obtain();
			loadingQueueItem.scene = scene;
			loadingQueueItem.filePath = filePath;
			mLoadingQueue.add(loadingQueueItem);
		}
	}

	/**
	 * Removes the loaded resource. This has to be called the same number of
	 * times the resource was loaded.
	 * @param scene the scene the resource was loaded in
	 * @param resource the resource to remove
	 * @see #clearLoadedSceneResources(Scene)
	 * @see #unload(Scene, UUID, Class, int) is a bit slower as it must lookup the filePath.
	 */
	static void unload(Scene scene, IResource resource) {
		int revisionToUse = -1;
		if (resource instanceof IResourceRevision) {
			revisionToUse = ((IResourceRevision) resource).getRevision();
		}

		boolean fullUnloaded = mLoadedResources.removeLoadedResource(scene, resource.getId(), resource.getClass(), revisionToUse);

		if (fullUnloaded) {
			mAssetManager.unload(getFilePath(resource));
		}
	}

	/**
	 * Removes the loaded resource. This has to be called the same number of
	 * times the resource was loaded.
	 * @param scene the scene the resource was loaded in
	 * @param resourceId id of the resource to be unloaded
	 * @param type the resource type of there resource
	 * @param revision revision of the resource to be unloaded, if the resource doesn't
	 * use a revision this parameter will not be used...
	 * @see #clearLoadedSceneResources(Scene)
	 * @see #unload(Scene, IResource) is a bit faster.
	 */
	static void unload(Scene scene, UUID resourceId, Class<?> type, int revision) {
		int revisionToUse = -1;
		if (IResourceRevision.class.isAssignableFrom(type)) {
			revisionToUse = revision;
		}

		boolean fullyUnloaded = mLoadedResources.removeLoadedResource(scene, resourceId, type, revisionToUse);

		if (fullyUnloaded) {
			mAssetManager.unload(getFilePath(resourceId, revisionToUse));
		}
	}

	/**
	 * Get all existing resources of a specified type, this will always return the latest resource
	 * @param type the type of resource to return
	 * @return all existing resources of the specified type, usually these aren't loaded, so be sure to load
	 * them first. Don't forget to free the all resource items with Pools.resourceItem.freeAll(resources) and
	 * the arraylist with Pools.arrayList(resources).
	 */
	static ArrayList<ResourceItem> getAllExistingResource(Class<?> type) {
		@SuppressWarnings("unchecked")
		ArrayList<ResourceItem> resources = Pools.arrayList.obtain();
		resources.clear();

		for (Entry<UUID, ResourceInfo> entry : mResources.entries()) {
			if (entry.value.type == type) {
				ResourceItem resourceItem = Pools.resourceItem.obtain();
				resourceItem.resourceId = entry.key;
				resourceItem.revision = entry.value.latestRevision;
				resources.add(resourceItem);
			}
		}

		return resources;
	}

	/**
	 * Get all resources loaded in this scene
	 * @param scene the scene to get all loaded resources from
	 * @return all loaded resources in this scene. Don't forget to use Pool.arraylist.free(resources) once
	 * you have used it!
	 */
	static ArrayList<IResource> getAllLoadedSceneResources(Scene scene) {
		return mLoadedResources.getAllLoadedSceneResources(scene);
	}

	/**
	 * Get all loaded resources of the specified type for this scene
	 * @param <ResourceType> type of the resource to get
	 * @param scene the scene to get all loaded resources from
	 * @param type the type of resource to get
	 * @return all loaded resource of the specified type in this scene. Don't forget to use Pool.arrayList.free(resources)
	 * once you have used it!
	 */
	static <ResourceType extends IResource> ArrayList<ResourceType> getAllLoadedSceneResourceOf(Scene scene, Class<ResourceType> type) {
		return mLoadedResources.getAllLoadedSceneResourceOf(scene, type);
	}

	/**
	 * Clears all loaded resource for a specified scene
	 * @param scene the scene to clear all loaded resource from
	 */
	static void clearLoadedSceneResources(Scene scene) {
		mLoadedResources.clearLoadedSceneResources(scene);
	}

	/**
	 * Gets a loaded resources. This method automatically gets the correct revision
	 * for this scene.
	 * @param scene scene which the resource was loaded in
	 * @param resourceId UUID of the resource
	 * @param revision the revision of the resource to get
	 * @param <ResourceType> the resource type
	 * @return A loaded resource. Null if not found
	 */
	static <ResourceType> ResourceType getLoadedResource(Scene scene, UUID resourceId, int revision) {
		return mLoadedResources.getLoadedResource(scene, resourceId, revision);
	}

	/**
	 * Add a new saved resource
	 * @param resource the resource that has been saved
	 */
	static void addSavedResource(IResource resource) {
		ResourceInfo resourceDb = mResources.get(resource.getId());

		// Resource already exist, just add revision
		if (resourceDb != null) {
			if (resource instanceof IResourceRevision) {
				resourceDb.addRevision((IResourceRevision) resource);
			}
		}
		// Create new resource db with revision
		else {
			resourceDb = new ResourceInfo(resource);
			mResources.put(resource.getId(), resourceDb);
		}
	}

	/**
	 * Private constructor to enforce singleton usage
	 */
	ResourceDatabase() {
		// Does nothing
	}

	/**
	 * Container for all existing resources and revisions
	 * 
	 * @author Matteus Magnusson <senth.wallace@gmail.com>
	 */
	private static class ResourceInfo {
		/**
		 * Creates a new resource DB type with no revision
		 * @param type the type of the resource
		 */
		ResourceInfo(Class<?> type) {
			this.type = type;
		}

		/**
		 * Creates a new resource DB type, automatically adds a revision if the
		 * resource uses revisions
		 * @param resource
		 */
		ResourceInfo(IResource resource) {
			type = resource.getClass();

			if (resource instanceof IResourceRevision) {
				addRevision((IResourceRevision)resource);
			}
		}

		/**
		 * Adds a new revision to the resource
		 * @param revision the revision to add
		 * @param date creation date of the revision, may be null
		 */
		void addRevision(int revision, String date) {
			if (revisionDates == null) {
				revisionDates = new ObjectMap<Integer, String>();
			}

			if (revision > latestRevision) {
				latestRevision = revision;
			}

			revisionDates.put(revision, date);
		}

		/**
		 * Adds a new revision to the resource
		 * @param resource the resource to add the revision
		 */
		void addRevision(IResourceRevision resource) {
			// Add date if the resource is a definition
			if (resource instanceof Def) {
				addRevision(resource.getRevision(), ((Def)resource).getDateString());
			} else {
				addRevision(resource.getRevision(), null);
			}
		}

		/** Type of resource */
		private Class<?> type;
		/** Latest revision of the resource */
		private int latestRevision = -1;
		/** all available including the date */
		private ObjectMap<Integer, String> revisionDates = null;
	}

	/**
	 * Container for currently loading resources (and waiting to be set)
	 */
	private static class LoadingQueueItem {
		/** File path of the resource */
		String filePath = null;
		/** For this scene */
		Scene scene = null;
	}

	/** Loaded resources */
	private static LoadedDb mLoadedResources = new LoadedDb();
	/** All resources and revisions */
	private static ObjectMap<UUID, ResourceInfo> mResources = new ObjectMap<UUID, ResourceDatabase.ResourceInfo>();
	/** Pool for LoadingQueue */
	private static Pool<LoadingQueueItem> mLoadingQueuePool = new Pool<ResourceDatabase.LoadingQueueItem>(LoadingQueueItem.class, 30, 300);
	/** Asset manager */
	private static AssetManager mAssetManager = null;
	/** Queue for currently loading assets */
	private static HashSet<LoadingQueueItem> mLoadingQueue = new HashSet<ResourceDatabase.LoadingQueueItem>();
}

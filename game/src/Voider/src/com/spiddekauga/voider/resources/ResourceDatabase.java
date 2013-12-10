package com.spiddekauga.voider.resources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.esotericsoftware.minlog.Log;
import com.spiddekauga.utils.Strings;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.Config.Debug;
import com.spiddekauga.voider.Config.Debug.Messages;
import com.spiddekauga.voider.scene.Scene;
import com.spiddekauga.voider.scene.SceneSwitcher;
import com.spiddekauga.voider.utils.Pool;
import com.spiddekauga.voider.utils.Pools;

/**
 * Contains all current resources, revisions and loaded resources
 * 
 * @todo ability to remove saved resources and revisions
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class ResourceDatabase {
	/**
	 * Builds the resource database
	 * @param assetManager the asset manager to load files with
	 */
	@SuppressWarnings("unchecked")
	static void init(AssetManager assetManager) {
		mAssetManager = assetManager;

		Map<Class<?>, String> resourcePaths = ResourceNames.getResourcePaths();

		for (Map.Entry<Class<?>, String> entry : resourcePaths.entrySet()) {
			FileHandle dir = Gdx.files.external(entry.getValue());

			if (!dir.exists() || !dir.isDirectory()) {
				continue;
			}

			// Resource revision
			if (IResourceRevision.class.isAssignableFrom(entry.getKey())) {
				buildResourceRevisionDb((Class<? extends IResource>) entry.getKey(), dir);
			}
			// Simple resource
			else if (IResource.class.isAssignableFrom(entry.getKey())) {
				buildResourceDb((Class<? extends IResource>) entry.getKey(), dir);
			}
		}
	}

	/**
	 * Returns resource information if the resource exists
	 * @param resourceId id of the resource
	 * @return resource information
	 */
	static ResourceInfo getResourceInfo(UUID resourceId) {
		return mResources.get(resourceId);
	}

	/**
	 * Returns all existing revision with dates of the resource
	 * @param resourceId id of the resource
	 * @return all existing revisions with dates of the resource. null if the
	 * resource either wasn't found, it doesn't have any revisions, or it has
	 * been published (meaning all revisions have been removed)
	 */
	public static Map<Integer, String> getResourceRevisionsWithDate(UUID resourceId) {
		ResourceInfo resourceInfo = mResources.get(resourceId);
		if (resourceInfo != null) {
			return resourceInfo.revisionDates;
		}
		return null;
	}

	/**
	 * Iterates through a resource with revision and builds its database
	 * @param type what type of class the resource is
	 * @param dir the directory where the resources of this type is located
	 */
	private static void buildResourceRevisionDb(Class<? extends IResource> type, FileHandle dir) {
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
					String[] splitStrings = revisionFile.name().split("_");


					String revisionString = null;
					String dateString = null;


					// Get revision and date substrings
					if (splitStrings.length == 2) {
						revisionString = splitStrings[0];
						dateString = splitStrings[1];
					} else {
						revisionString = revisionFile.name();
					}

					if (revisionString.length() != Config.File.REVISION_LENGTH) {
						if (!revisionString.equals(Config.File.REVISION_LATEST_NAME)) {
							Gdx.app.debug("ResourceDatabase", "Revision is not the correct length!" + revisionFile.path());
						}
						continue;
					}

					// Convert revision string to integer
					int revision = Integer.parseInt(revisionString);

					resourceDb.addRevision(revision, dateString);
				}

				// Only add resource if there actually were any revisions and not just an empty directory...
				if (resourceDb.revisionDates != null) {
					mResources.put(resourceId, resourceDb);
				}
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
	private static void buildResourceDb(Class<? extends IResource> type, FileHandle dir) {
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
	 * @param revision the revision to get, -1 to use latest revision.
	 * @return file path of the resource
	 */
	@SuppressWarnings("unused")
	static String getFilePath(UUID resourceId, int revision) {
		ResourceInfo resourceDb = mResources.get(resourceId);

		String filePath = ResourceNames.getDirPath(resourceDb.type) + resourceId.toString();

		if (resourceDb != null) {
			if (resourceDb.revisionDates != null) {
				if (revision > 0) {
					filePath += "/" + getRevisionFormat(revision);

					// Add date for definitions
					if (Def.class.isAssignableFrom(resourceDb.type)) {
						String date = resourceDb.revisionDates.get(revision);

						if (date != null) {
							filePath += "_" + date;
						} else {
							Gdx.app.error("ResourceDatabase", "Could not find revision (" + revision + ") for (" + resourceId + ") in getFilePath");
							return null;
						}
					}
				} else {
					filePath += "/" + Config.File.REVISION_LATEST_NAME;
				}
			}
		} else {
			Gdx.app.error("ResourceDatabase", "Could not find resource when getting file path!");
			return null;
		}

		return filePath;
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

				if (!ResourceDatabase.isResourceLoaded(loadingQueueItem.scene, resource.getId(), loadingQueueItem.revision)) {

					mLoadedResources.setLoadedResource(loadingQueueItem.scene, resource, loadingQueueItem.revision);

					String name = "";
					if (resource instanceof Def) {
						name = ((Def) resource).getName();
					}

					debugOutputLoadedUnloaded(loadingQueueItem.scene, 1, true, resource.getClass(), name, loadingQueueItem.revision, loadingQueueItem.filePath);
				}

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
		return mLoadedResources.getLoadedResource(scene, resourceId, getRevisionToUse(resourceId, revision)) != null;
	}

	/**
	 * Load the specified resource. If the resource already has been loaded it will
	 * not try to load the resource
	 * @param scene the scene which is loading
	 * @param resourceId the resource that is currently being loaded
	 * @param revision the revision to load. Note that if the resource already has
	 * been loaded, this variable will have no effect. I.e. it will not load another
	 * revision of the resource as only one revision per scene and resource is allowed.
	 */
	static void load(Scene scene, UUID resourceId, int revision) {
		ResourceInfo resourceInfo = mResources.get(resourceId);

		if (resourceInfo != null) {
			int revisionToUse = getRevisionToUse(resourceId, revision);

			int cLoad = mLoadedResources.addLoadingResource(scene, resourceId, revisionToUse, resourceInfo.type);

			String filePath = getFilePath(resourceId, revisionToUse);

			if (cLoad == 1) {
				mAssetManager.load(filePath, resourceInfo.type);

				LoadingQueueItem loadingQueueItem = mLoadingQueuePool.obtain();
				loadingQueueItem.scene = scene;
				loadingQueueItem.filePath = filePath;
				loadingQueueItem.revision = revisionToUse;
				mLoadingQueue.add(loadingQueueItem);
			} else {
				String name = "";
				if (Def.class.isAssignableFrom(resourceInfo.type)) {
					if (mAssetManager.isLoaded(filePath)) {
						IResource resource = mAssetManager.get(filePath);
						name = ((Def) resource).getName();
					}
				}

				if (resourceInfo.latestRevision == revisionToUse) {
					revisionToUse = -1;
				}

				debugOutputLoadedUnloaded(scene, cLoad, true, resourceInfo.type, name, revisionToUse, filePath);
			}
		} else {
			Gdx.app.error("ResourceDatabase", "Could not find the resource you tried to load!");
		}
	}

	/**
	 * Gets the latest revision number of the specified resource
	 * @param resourceId the resource to get the latest revision from
	 * @return the latest revision from the specified resource. 0 if the resource doesn't exist yet. -1 if
	 * the resource doesn't use revisions
	 */
	static int getLatestRevisionNumber(UUID resourceId) {
		int revision = 0;
		ResourceInfo resourceInfo = mResources.get(resourceId);

		if (resourceInfo != null) {
			revision = resourceInfo.latestRevision;
		}

		return revision;
	}

	/**
	 * Sets the latest resource to the specified resource
	 * @param resource the resource to be set as latest resource
	 * @param oldRevision old revision that the resource was loaded into
	 */
	static void setLatestResource(Resource resource, int oldRevision) {
		Resource latestResource = getLoadedResource(SceneSwitcher.getActiveScene(true), resource.getId(), -1);
		if (latestResource != null) {
			// Unload old revision and reload latest resource.
			String oldFilepath = getFilePath(resource.getId(), oldRevision);
			mAssetManager.unload(oldFilepath);
			mLoadedResources.removeLoadedResource(SceneSwitcher.getActiveScene(true), resource.getId(), oldRevision);

			String latestFilepath = getFilePath(resource.getId(), -1);
			int cReferences = mAssetManager.getReferenceCount(latestFilepath);
			mAssetManager.setReferenceCount(latestFilepath, 1);
			mAssetManager.unload(latestFilepath);
			mAssetManager.load(latestFilepath, resource.getClass());
			mAssetManager.finishLoading();
			mAssetManager.setReferenceCount(latestFilepath, cReferences);

			Resource newLatestResource = mAssetManager.get(latestFilepath);

			// Update old latest resource
			latestResource.set(newLatestResource);
			resource.set(newLatestResource);
		} else {
			Gdx.app.error("ResourceDatabase", "Could not find latest resource");
		}
	}

	/**
	 * Reloads a loaded resource from latest revision to another revision.
	 * Useful when reverting back to an older revision of the resource. This methods will set the
	 * latest resource to contain the specified revision instead of the actual revision. This
	 * will have the effect of "updating" all existing references to this resource so they don't
	 * have to reload an older revision of the resource.
	 * @param resourceId resource id to reload
	 * @param revision specific revision of the resource to reload to
	 */
	static void reload(UUID resourceId, int revision) {
		//		int revisionToUse = getRevisionToUse(resourceId, revision);
		//		String filepath = getFilePath(resourceId, revision);
		//		String latestFilepath = getFilePath(resourceId, -1);
		//
		//		if (filepath != null && latestFilepath != null) {
		//			ResourceInfo resourceInfo = mResources.get(resourceId);
		//			mAssetManager.load(filepath, resourceInfo.type);
		//			mAssetManager.finishLoading();
		//
		//
		//			// Unload all internal dependencies first
		//			for (ResourceNames dependency : resou)
		//
		//
		//			ResourceCacheFacade.load(null, resourceId, true, revision);
		//			ResourceCacheFacade.finishLoading();
		//
		//			Resource latestResource = ResourceCacheFacade.get(null, resourceId);
		//			Resource loadedResource = ResourceCacheFacade.get(null, resourceId, revision);
		//
		//			latestResource.set(loadedResource);
		//
		//			Scene sceneToUse = SceneSwitcher.getActiveScene(true);
		//			mLoadedResources.removeLoadedResource(null, resourceId, revision);
		//			mAssetManager.setReferenceCount(filepath, refCount);
		//		}
		//
		//		if (filepath != null && mAssetManager.isLoaded(filepath)) {
		//			// Get scenes the resource is loaded into
		//			ArrayList<Scene> scenes = mLoadedResources.getResourceScenes(resourceId, revisionToUse);
		//
		//			// Reload the actual asset
		//			ResourceInfo resourceInfo = mResources.get(resourceId);
		//			int cRefs = mAssetManager.getReferenceCount(filepath);
		//			mAssetManager.setReferenceCount(filepath, 1);
		//			mAssetManager.unload(filepath);
		//			mAssetManager.load(filepath, resourceInfo.type);
		//			mAssetManager.finishLoading();
		//			mAssetManager.setReferenceCount(filepath, cRefs);
		//
		//			// Update the scene resource to contain the new reference of the resource
		//			IResource resource = mAssetManager.get(filepath, resourceInfo.type);
		//			for (Scene scene : scenes) {
		//				mLoadedResources.setLoadedResource(scene, resource, revisionToUse);
		//			}
		//
		//			Pools.arrayList.free(scenes);
		//			scenes = null;
		//		}
	}

	/**
	 * Removes the loaded resource. This has to be called the same number of
	 * times the resource was loaded.
	 * @param scene the scene the resource was loaded in
	 * @param resource the resource to remove
	 * @see #clearLoadedSceneResources(Scene)
	 */
	static void unload(Scene scene, IResource resource) {
		int revisionToUse = -1;
		if (resource instanceof IResourceRevision) {
			revisionToUse = ((IResourceRevision) resource).getRevision();
		}

		unload(scene, resource.getId(), revisionToUse);
	}

	/**
	 * Removes the loaded resource. This has to be called the same number of
	 * times the resource was loaded.
	 * @param scene the scene the resource was loaded in
	 * @param resourceId id of the resource to be unloaded
	 * @param revision revision of the resource to be unloaded, if the resource doesn't
	 * use a revision this parameter will not be used...
	 * @see #clearLoadedSceneResources(Scene)
	 */
	static void unload(Scene scene, UUID resourceId, int revision) {
		ResourceInfo resourceInfo = mResources.get(resourceId);

		int revisionToUse = getRevisionToUse(resourceId, revision);

		int cLoad = mLoadedResources.removeLoadedResource(scene, resourceId, revisionToUse);

		String filePath = getFilePath(resourceId, revisionToUse);

		String name = "";
		if (Def.class.isAssignableFrom(resourceInfo.type)) {
			IResource resource = mAssetManager.get(filePath);
			name = ((Def) resource).getName();
		}

		if (cLoad == 0) {
			mAssetManager.unload(filePath);
		}

		debugOutputLoadedUnloaded(scene, cLoad, false, resourceInfo.type, name, revisionToUse, filePath);
	}

	/**
	 * Outputs a debug message when a file is loaded/unloaded
	 * @param scene the scene the resources was loaded/unloaded
	 * @param cRefsInScene number of references for the references in the current scene.
	 * @param loaded true if the resource was loaded, false if unloaded
	 * @param type resource type
	 * @param name name of the resource
	 * @param revision of the resource
	 * @param filepath of the resource
	 */
	@SuppressWarnings("unused")
	private static void debugOutputLoadedUnloaded(Scene scene, int cRefsInScene, boolean loaded, Class<?> type, String name, int revision, String filepath) {
		if (Messages.LOAD_UNLOAD) {
			if (Messages.LOAD_UNLOAD_EVERY_TIME || ((loaded && cRefsInScene == 1) || (!loaded && cRefsInScene == 0))) {
				int cRefsInAssetManager = 0;
				if (mAssetManager.isLoaded(filepath)) {
					cRefsInAssetManager = mAssetManager.getReferenceCount(filepath);
				}

				// Revision
				String revisionString = "";
				if (IResourceRevision.class.isAssignableFrom(type)) {
					if (revision > 0) {
						revisionString = "r." + revision;
					} else {
						revisionString = Config.File.REVISION_LATEST_NAME;
					}
				}


				if (name.equals("(Unnamed)")) {
					name = "";
				}

				String loadUnloadString = loaded ? "+++" : "---";

				String message = loadUnloadString + "  a:" + cRefsInAssetManager + "  " +
						Strings.padRight("s:" + cRefsInScene, 4) + "  " +
						Strings.padRight(scene.getClass().getSimpleName(), 15) + " " +
						Strings.padRight(type.getSimpleName(), 18) + " " +
						Strings.padRight(name, 16) + " " +
						Strings.padRight(revisionString, 8) + " " +
						filepath;

				if (Gdx.app != null) {
					Gdx.app.log("ResourceDatabase", message);
				} else {
					Log.debug(message);
				}
			}
		}
	}

	/**
	 * Get all existing resources of a specified type, this will always return the latest resource
	 * @param type the type of resource to return
	 * @return all existing resources of the specified type, usually these aren't loaded, so be sure to load
	 * them first. Don't forget to free the all resource items with Pools.resourceItem.freeAll(resources) and
	 * the ArrayList with Pools.arrayList(resources).
	 */
	static ArrayList<ResourceItem> getAllExistingResource(Class<?> type) {
		@SuppressWarnings("unchecked")
		ArrayList<ResourceItem> resources = Pools.arrayList.obtain();

		for (Entry<UUID, ResourceInfo> entry : mResources.entrySet()) {
			if (entry.getValue().type == type) {
				ResourceItem resourceItem = Pools.resourceItem.obtain();
				resourceItem.id = entry.getKey();
				resourceItem.revision = entry.getValue().latestRevision;
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
	 * @return string with all the current loaded resources
	 */
	static String getAllLoadedResourcesString() {
		return mLoadedResources.getAllLoadedResourcesString();
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
		return mLoadedResources.getLoadedResource(scene, resourceId, getRevisionToUse(resourceId, revision));
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
	 * Removes the saved resource from the database (and the file if it exists)
	 * @param resource the resource that is to be removed
	 */
	static void removeSavedResource(IResource resource) {
		ResourceInfo resourceInfo = mResources.get(resource.getId());

		if (resourceInfo != null) {
			String filepath = getFilePath(resource);

			// Remove file
			FileHandle file = Gdx.files.external(filepath);
			if (file.exists()) {
				file.delete();
			}

			boolean removeResource = false;

			if (resource instanceof IResourceRevision) {
				// Remove folder if no more revisions are available
				resourceInfo.removeRevision((IResourceRevision) resource);
				if (resourceInfo.revisionDates == null || resourceInfo.revisionDates.isEmpty()) {
					FileHandle dir = file.parent();
					if (dir.exists() && dir.isDirectory()) {
						dir.deleteDirectory();
					}

					removeResource = true;
				}
			} else {
				removeResource = true;
			}

			if (removeResource) {
				mResources.remove(resource.getId());
			}
		} else {
			Gdx.app.error("ResourceDatabase", "Could not find the resource you tried to remove: " + resource.getClass().getSimpleName());
		}
	}

	/**
	 * Removes all resources of the specified type. This includes all revisions!
	 * @per All the resources of these types should be fully unloaded
	 * before calling this method!
	 * @param type the type of resource to remove
	 */
	static void removeAllOf(Class<? extends IResource> type) {
		// Delete from disk
		String path = ResourceNames.getDirPath(type);
		FileHandle folder = Gdx.files.external(path);

		if (folder.exists()) {
			folder.deleteDirectory();
		}


		// Delete from database
		if (!Debug.DEBUG_TESTS) {
			Iterator<Entry<UUID, ResourceInfo>> iterator = mResources.entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<UUID, ResourceInfo> entry = iterator.next();

				if (entry.getValue().type == type) {
					iterator.remove();
				}
			}
		}
		// Debug test delete from database
		else {
			Iterator<Entry<UUID, ResourceInfo>> iterator = mResources.entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<UUID, ResourceInfo> entry = iterator.next();

				if (entry.getValue().type == type) {
					iterator.remove();

					// Check if there is any scene that has this resource loaded!
					ArrayList<Scene> scenesWithResource = mLoadedResources.getResourceScenes(entry.getKey(), -1);

					if (!scenesWithResource.isEmpty()) {
						Gdx.app.error("ResourceDatabase", "A resource of (" + type.getSimpleName() + ") was " +
								"loaded into (" + scenesWithResource.get(0).getClass().getSimpleName() +
								") when removing all resources of this type!");
					}

					Pools.arrayList.free(scenesWithResource);
					scenesWithResource = null;
				}
			}
		}
	}

	/**
	 * Calculates the revision to use for the resource
	 * @param resourceId id of the resource
	 * @param requestedRevision the requested revision to use.
	 * @return If the resource can use revisions the requested revision will be used unless -1 is requested.
	 * In that case the latest revision for that resource will be used instead. If the resource cannot hold
	 * revisions this method will always return -1.
	 */
	private static int getRevisionToUse(UUID resourceId, int requestedRevision) {
		int revisionToUse = -1;
		ResourceInfo resourceInfo = mResources.get(resourceId);
		if (resourceInfo != null) {
			if (IResourceRevision.class.isAssignableFrom(resourceInfo.type)) {
				if (resourceInfo.latestRevision == requestedRevision) {
					revisionToUse = -1;
				} else {
					revisionToUse = requestedRevision;
				}
			}
		} else {
			Gdx.app.debug("ResourceDatabase", "Could not find resource! " + resourceId);
		}
		return revisionToUse;
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
	static class ResourceInfo {
		/**
		 * Creates a new resource DB type with no revision
		 * @param type the type of the resource
		 */
		ResourceInfo(Class<? extends IResource> type) {
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
				revisionDates = new HashMap<Integer, String>();
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

		/**
		 * Removes a revision from the resource. Only removes the active revision
		 * @param resource the revision to remove
		 */
		void removeRevision(IResourceRevision resource) {
			if (revisionDates != null) {
				revisionDates.remove(resource.getRevision());
			}
		}

		/** Type of resource */
		Class<? extends IResource> type;
		/** Latest revision of the resource */
		int latestRevision = -1;
		/** all available including the date */
		private Map<Integer, String> revisionDates = null;
	}

	/**
	 * Container for currently loading resources (and waiting to be set)
	 */
	private static class LoadingQueueItem {
		/** File path of the resource */
		String filePath = null;
		/** For this scene */
		Scene scene = null;
		/** Revision of the loading file */
		int revision = -1;
	}

	/** Loaded resources */
	private static LoadedDb mLoadedResources = new LoadedDb();
	/** All resources and revisions */
	private static Map<UUID, ResourceInfo> mResources = new HashMap<UUID, ResourceDatabase.ResourceInfo>();
	/** Pool for LoadingQueue */
	private static Pool<LoadingQueueItem> mLoadingQueuePool = new Pool<ResourceDatabase.LoadingQueueItem>(LoadingQueueItem.class, 30, 300);
	/** Asset manager */
	private static AssetManager mAssetManager = null;
	/** Queue for currently loading assets */
	private static HashSet<LoadingQueueItem> mLoadingQueue = new HashSet<ResourceDatabase.LoadingQueueItem>();
}

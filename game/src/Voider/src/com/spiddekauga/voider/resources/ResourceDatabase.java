package com.spiddekauga.voider.resources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.ObjectMap;
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
	 */
	static void init() {
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

				ResourceDb resourceDb = new ResourceDb(type);

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
				ResourceDb resourceDb = new ResourceDb(type);
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
	 * Gets the fully qualified file path name for the resource
	 * @param type the type of resource
	 * @param uuid the id of the resource
	 * @return file path of the resource
	 */
	static String getFilePath(Class<?> type, UUID uuid) {
		return ResourceNames.getDirPath(type) + uuid.toString();
	}

	/**
	 * Gets the fully qualified file path name for a resource with revision
	 * @param type the type of the resource
	 * @param uuid id of the resource
	 * @param revision revision of the resource to use
	 * @return file path of the resource
	 */
	static String getFilePath(Class<?> type, UUID uuid, int revision) {
		String filePath = getFilePath(type, uuid);

		if (IResourceRevision.class.isAssignableFrom(type)) {
			filePath += "/" + getRevisionFormat(revision);
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
	 * @param revision the revision to get, -1 to use latest revision, i.e.
	 * regular path
	 * @return file path of the resource
	 */
	static String getFilePath(UUID resourceId, int revision) {
		ResourceDb resourceDb = mResources.get(resourceId);

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
	 * Checks whether the resource has been loaded or not
	 * @param scene the scene to check if the resource has been loaded in
	 * @param resourceId the resource to check if it has been loaded
	 * @return true if the resource has been loaded in the specified scene, false if not.
	 */
	static boolean isResourceLoaded(Scene scene, UUID resourceId) {
		return getLoadedResource(scene, resourceId) != null;
	}

	/**
	 * Adds another resources that has been loaded
	 * @param scene which scene the resource was loaded in
	 * @param resource the resource that has been loaded
	 */
	static void addLoadedResource(Scene scene, IResource resource) {
		HashMap<UUID, LoadedDb> sceneResources = mLoadedResources.get(scene);

		// No existing scene resources, add scene
		if (sceneResources == null) {
			sceneResources = new HashMap<UUID, ResourceDatabase.LoadedDb>();
			mLoadedResources.put(scene, sceneResources);
		}


		LoadedDb loadedResource = sceneResources.get(resource.getId());

		// Resource has not been loaded, add it
		if (loadedResource == null) {
			loadedResource = mLoadedDbPool.obtain();
			loadedResource.count = 1;
			loadedResource.resource = resource;

			if (resource instanceof IResourceRevision) {
				loadedResource.revision = ((IResourceRevision) resource).getRevision();
			} else {
				loadedResource.revision = -1;
			}

			sceneResources.put(resource.getId(), loadedResource);
		}
		// Resource already loaded, just increase count
		else {
			loadedResource.count++;
		}
	}

	/**
	 * Removes the loaded resource. This has to be called the same number of
	 * times the resource was loaded.
	 * @param scene the scene the resource was loaded in
	 * @param resource the resource to remove
	 * @return true if the resource was fully unloaded
	 * @see #clearLoadedSceneResources(Scene)
	 */
	static boolean removeLoadedResource(Scene scene, IResource resource) {
		HashMap<UUID, LoadedDb> sceneResources = mLoadedResources.get(scene);

		boolean fullyUnloaded = false;

		if (sceneResources  !=  null) {
			LoadedDb loadedResource = sceneResources.get(resource.getId());

			if (loadedResource != null) {
				loadedResource.count--;

				if (loadedResource.count == 0) {
					mLoadedDbPool.free(loadedResource);
					sceneResources.remove(resource.getId());
					fullyUnloaded = true;
				}

			} else {
				Gdx.app.error("ResourceDatabase", "Resource to remove doesn't exist in scene reosurce");
			}
		} else {
			Gdx.app.error("ResourceDatabase", "Scene resources does not exist when trying to remove a resource");
		}

		return fullyUnloaded;
	}

	/**
	 * Get all resources loaded in this scene
	 * @param scene the scene to get all loaded resources from
	 * @return all loaded resources in this scene. Don't forget to use Pool.arraylist.free(resources) once
	 * you have used it!
	 */
	static ArrayList<IResource> getAllLoadedSceneResources(Scene scene) {
		@SuppressWarnings("unchecked")
		ArrayList<IResource> resources = Pools.arrayList.obtain();
		resources.clear();

		HashMap<UUID, LoadedDb> sceneResources = mLoadedResources.get(scene);
		if (sceneResources != null) {
			for (Entry<UUID, LoadedDb> entry : sceneResources.entrySet()) {
				resources.add(entry.getValue().resource);
			}
		}

		return resources;
	}

	/**
	 * Clears all loaded resource for a specified scene
	 * @param scene the scene to clear all loaded resource from
	 */
	static void clearLoadedSceneResources(Scene scene) {
		HashMap<UUID, LoadedDb> sceneResources = mLoadedResources.get(scene);
		if (sceneResources != null) {
			for (Entry<UUID, LoadedDb> entry : sceneResources.entrySet()) {
				mLoadedDbPool.free(entry.getValue());
			}

			mLoadedResources.remove(scene);
		}
	}

	/**
	 * Gets a loaded resources. This method automatically gets the correct revision
	 * for this scene.
	 * @param scene scene which the resource was loaded in
	 * @param resourceId UUID of the resource
	 * @param <ResourceType> the resource type
	 * @return A loaded resource. Null if not found
	 */
	@SuppressWarnings("unchecked")
	static <ResourceType> ResourceType getLoadedResource(Scene scene, UUID resourceId) {
		HashMap<UUID, LoadedDb> loadedSceneResources = mLoadedResources.get(scene);
		if (loadedSceneResources != null) {
			LoadedDb loadedResource = loadedSceneResources.get(resourceId);

			if (loadedResource != null) {
				return (ResourceType) loadedResource.resource;
			}
		}

		return null;
	}

	/**
	 * Add a new saved resource
	 * @param resource the resource that has been saved
	 */
	static void addSavedResource(IResource resource) {
		ResourceDb resourceDb = mResources.get(resource.getId());

		// Resource already exist, just add revision
		if (resourceDb != null) {
			if (resource instanceof IResourceRevision) {
				resourceDb.addRevision((IResourceRevision) resource);
			}
		}
		// Create new resource db with revision
		else {
			resourceDb = new ResourceDb(resource);
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
	static class ResourceDb {
		/**
		 * Creates a new resource DB type with no revision
		 * @param type the type of the resource
		 */
		ResourceDb(Class<?> type) {
			this.type = type;
		}

		/**
		 * Creates a new resource DB type, automatically adds a revision if the
		 * resource uses revisions
		 * @param resource
		 */
		ResourceDb(IResource resource) {
			type = resource.getClass();

			if (resource instanceof IResourceRevision) {
				addRevision((IResourceRevision)resource);
			}
		}

		/**
		 * Creates a new resource DB type with revision information
		 * @param type the type of the resource
		 * @param revision the revision to initially add
		 * @param date creation date of the revision, may be null
		 */
		ResourceDb(Class<?> type, int revision, String date) {
			this.type = type;

			addRevision(revision, date);
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
		Class<?> type;
		/** all available including the date */
		ObjectMap<Integer, String> revisionDates = null;
	}

	/**
	 * Container for all loaded resources
	 */
	static class LoadedDb {
		/** Loaded revision, -1 if the resource doesn't have any revisions */
		int revision = -1;
		/** How many times it has been loaded */
		int count = 0;
		/** The actual resource */
		IResource resource = null;
	}

	/** All resources and revisions */
	static ObjectMap<UUID, ResourceDb> mResources = new ObjectMap<UUID, ResourceDatabase.ResourceDb>();
	/** All loaded resources */
	static ObjectMap<Scene, HashMap<UUID, LoadedDb>> mLoadedResources = new ObjectMap<Scene, HashMap<UUID,LoadedDb>>();
	/** Pool for LoadedDb */
	static Pool<LoadedDb> mLoadedDbPool = new Pool<ResourceDatabase.LoadedDb>(LoadedDb.class, 30, 300);
}

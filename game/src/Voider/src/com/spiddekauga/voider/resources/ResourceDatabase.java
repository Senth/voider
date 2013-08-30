package com.spiddekauga.voider.resources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

import com.badlogic.gdx.Gdx;
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
	 * @param resourceId the resource to check if it has been loaded
	 * @param scene the scene to check if the resource has been loaded in
	 * @return true if the resource has been loaded in the specified scene, false if not.
	 */
	static boolean isResourceLoaded(UUID resourceId, Scene scene) {
		return getLoadedResource(resourceId, scene) != null;
	}

	/**
	 * Adds another resources that has been loaded
	 * @param resource the resource that has been loaded
	 * @param scene which scene the resource was loaded in
	 */
	static void addLoadedResource(IResource resource, Scene scene) {
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
	 * @param resource the resource to remove
	 * @param scene the scene the resource was loaded in
	 * @see #clearLoadedSceneResources(Scene)
	 */
	static void removeLoadedResource(IResource resource, Scene scene) {
		HashMap<UUID, LoadedDb> sceneResources = mLoadedResources.get(scene);

		if (sceneResources  !=  null) {
			LoadedDb loadedResource = sceneResources.get(resource.getId());

			if (loadedResource != null) {
				loadedResource.count--;

				if (loadedResource.count == 0) {
					mLoadedDbPool.free(loadedResource);
					sceneResources.remove(resource.getId());
				}

			} else {
				Gdx.app.error("ResourceDatabase", "Resource to remove doesn't exist in scene reosurce");
			}
		} else {
			Gdx.app.error("ResourceDatabase", "Scene resources does not exist when trying to remove a resource");
			return;
		}
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
	 * @param <ResourceType> the resource type
	 * @param resourceId UUID of the resource
	 * @param scene scene which the resource was loaded in
	 * @return A loaded resource. Null if not found
	 */
	@SuppressWarnings("unchecked")
	static <ResourceType> ResourceType getLoadedResource(UUID resourceId, Scene scene) {
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
		 * Creates a new resource DB type
		 * @param resource the initial resource to be added
		 */
		ResourceDb(IResource resource) {
			type = resource.getClass();

			if (resource instanceof IResourceRevision) {
				revisionDates = new HashMap<Integer, String>();
			}
		}

		/**
		 * Adds a new revision to the resource
		 * @param resource the resource to add
		 */
		void addRevision(IResourceRevision resource) {
			// Add date if the resource is a definition
			if (resource instanceof Def) {
				revisionDates.put(resource.getRevision(), ((Def) resource).getDateString());
			} else {
				revisionDates.put(resource.getRevision(), null);
			}
		}

		/** Type of resource */
		Class<?> type;
		/** all available including the date */
		HashMap<Integer, String> revisionDates = null;
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
	static HashMap<UUID, ResourceDb> mResources = new HashMap<UUID, ResourceDatabase.ResourceDb>();
	/** All loaded resources */
	static HashMap<Scene, HashMap<UUID, LoadedDb>> mLoadedResources = new HashMap<Scene, HashMap<UUID,LoadedDb>>();
	/** Pool for LoadedDb */
	static Pool<LoadedDb> mLoadedDbPool = new Pool<ResourceDatabase.LoadedDb>(LoadedDb.class, 30, 300);
}

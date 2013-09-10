package com.spiddekauga.voider.resources;

import java.util.ArrayList;
import java.util.UUID;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectMap.Entry;
import com.spiddekauga.voider.scene.Scene;
import com.spiddekauga.voider.utils.Pool;
import com.spiddekauga.voider.utils.Pools;

/**
 * Container for all the loaded resources
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
class LoadedDb {
	/**
	 * Adds a resource that is currently being loaded
	 * @param scene the scene the resource is loading into
	 * @param resourceId id of the resource
	 * @param type the type of the resource
	 * @param revision the current revision that is being loaded. If the resource doesn't use a
	 * revision this variable won't be used.
	 * @return number of times this resource has been added onto this scene. If 1 this was the
	 * first time it was added to this scene.
	 */
	int addLoadingResource(Scene scene, UUID resourceId, Class<?> type, int revision) {
		ObjectMap<UUID, LoadedResource> sceneResources = mLoadedResources.get(scene);

		// No existing scene, add scene
		if (sceneResources == null) {
			sceneResources = new ObjectMap<UUID, LoadedDb.LoadedResource>();
			mLoadedResources.put(scene, sceneResources);
		}


		LoadedResource loadedResource = sceneResources.get(resourceId);

		// Resource has never been loaded, add it
		if (loadedResource == null) {
			loadedResource = mLoadedResourcePool.obtain();
			loadedResource.type = type;
			loadedResource.revisions.clear();
			sceneResources.put(resourceId, loadedResource);
		}


		int revisionToUse = -1;
		if (IResourceRevision.class.isAssignableFrom(type)) {
			revisionToUse = revision;
		}

		LoadedRevision loadedRevision = loadedResource.revisions.get(revisionToUse);

		// Revision has not been loaded before, add it
		if (loadedRevision == null) {
			loadedRevision = mLoadedRevisionPool.obtain();
			loadedRevision.count = 1;
			loadedResource.revisions.put(revisionToUse, loadedRevision);
		}
		// Resource already loaded, just increase count
		else {
			loadedRevision.count++;
		}

		return loadedRevision.count;
	}

	/**
	 * Sets the loaded resource
	 * @param scene the scene the resource was loaded into
	 * @param resource the resource that was loaded
	 */
	void setLoadedResource(Scene scene, IResource resource) {
		LoadedRevision loadedRevision = getLoadedRevision(scene, resource);

		if (loadedRevision != null) {
			loadedRevision.resource = resource;
		}
	}

	/**
	 * Removes a loaded resource from the specified scene
	 * @param scene the scene the resource was loaded into
	 * @param resourceId the resource that was loaded
	 * @param type of the resource that was loaded
	 * @param revision the revision of the resource
	 * @return number of instances the scene has of this resource after it has been unloaded. If
	 * it returns 0, it means it has been fully unloaded from this scene. -1 if the resource
	 * wasn't found
	 */
	int removeLoadedResource(Scene scene, UUID resourceId, Class<?> type, int revision) {
		ObjectMap<UUID, LoadedResource> sceneResources = mLoadedResources.get(scene);

		int cLoad = -1;

		if (sceneResources != null) {
			LoadedResource loadedResource = sceneResources.get(resourceId);

			if (loadedResource != null) {
				int revisionToUse = -1;
				if (IResourceRevision.class.isAssignableFrom(type)) {
					revisionToUse = revision;
				}

				LoadedRevision loadedRevision = loadedResource.revisions.get(revisionToUse);
				if (loadedRevision != null) {
					loadedRevision.count--;
					cLoad = loadedRevision.count;

					// Remove and free revision if this was the last of it
					if (loadedRevision.count == 0) {
						mLoadedRevisionPool.free(loadedRevision);
						loadedResource.revisions.remove(revisionToUse);

						// Remove and free resource if this was the last revision
						if (loadedResource.revisions.size == 0) {
							sceneResources.remove(resourceId);
							mLoadedResourcePool.free(loadedResource);

							// Remove the scene if this was the last resource
							if (sceneResources.size == 0) {
								mLoadedResources.remove(scene);
							}
						}
					}
				} else {
					Gdx.app.error("LoadedDb", "Could not find the revision (" + revision + ") for (" + type.getSimpleName() + ": " + resourceId + ")");
				}
			} else {
				Gdx.app.error("LoadedDb", "Could not find " + type.getSimpleName() + ": " + resourceId);
			}
		} else {
			Gdx.app.error("LoadedDb", "Could not find scene (" + scene.getClass().getSimpleName() + ") for the resource!");
		}

		return cLoad;
	}

	/**
	 * Gets an array with all the scenes the specified resource exists in
	 * @param resourceId the resource to check if it's loaded
	 * @param revision the revision of the resource, set as -1 if the resource doesn't use
	 * revisions OR if you don't care which revision the scene has loaded.
	 * @return ArrayList with all scenes the resource with the specified revision is
	 * loaded into. Don't forget to free the arraylist using Pool.arrayList.free(scenes)
	 */
	ArrayList<Scene> getResourceScenes(UUID resourceId, int revision) {
		@SuppressWarnings("unchecked")
		ArrayList<Scene> scenes = Pools.arrayList.obtain();
		scenes.clear();

		for (Entry<Scene, ObjectMap<UUID, LoadedResource>> sceneEntry : mLoadedResources.entries()) {
			LoadedResource foundLoadedResource = sceneEntry.value.get(resourceId);

			if (foundLoadedResource != null) {
				if (revision == -1 || foundLoadedResource.revisions.containsKey(revision)) {
					scenes.add(sceneEntry.key);
				}
			}
		}

		return scenes;
	}

	/**
	 * Gets a loaded or loading revision
	 * @param scene the scene which the resource and revision is loaded into
	 * @param resource the resource to get it revision from
	 * @return loaded revision of the resource, null if revision wasn't found.
	 */
	private LoadedRevision getLoadedRevision(Scene scene, IResource resource) {
		int revisionToUse = -1;
		if (resource instanceof IResourceRevision) {
			revisionToUse = ((IResourceRevision) resource).getRevision();
		}

		return getLoadedRevision(scene, resource.getId(), revisionToUse);
	}

	/**
	 * Gets a loaded or loading revision
	 * @param scene the scene which teh resource and revision is loaded into
	 * @param resourceId id of the resource
	 * @param revision revision of the resource
	 * @return loaded revision of the resource, null if revision wasn't found.
	 */
	private LoadedRevision getLoadedRevision(Scene scene, UUID resourceId, int revision) {
		ObjectMap<UUID, LoadedResource> sceneResources = mLoadedResources.get(scene);

		if (sceneResources != null) {
			LoadedResource loadedResource = sceneResources.get(resourceId);

			if (loadedResource != null) {
				int revisionToUse = -1;
				if (IResourceRevision.class.isAssignableFrom(loadedResource.type)) {
					revisionToUse = revision;
				}

				LoadedRevision loadedRevision = loadedResource.revisions.get(revisionToUse);
				if (loadedRevision != null) {
					return loadedRevision;
				} else {
					Gdx.app.debug("LoadedDb", "Could not find the revision (" + revision + ") for (" + loadedResource.type.getSimpleName() + ": " + resourceId + ")");
				}
			} else {
				Gdx.app.debug("LoadedDb", "Could not find " + resourceId);
			}
		} else {
			Gdx.app.error("LoadedDb", "Could not find scene (" + scene.getClass().getSimpleName() + ") for the resource!");
		}

		return null;
	}

	/**
	 * Gets a loaded resource with the specific revision.
	 * @param <ResourceType> automatically converts the resource to this type.
	 * @param scene the scene the resource was loaded into.
	 * @param resourceId the id of the resource
	 * @param revision what revision of the resource to get. If the resource doesn't use
	 * any revisions this variable won't be used.
	 * @return the specified resource revision, or just the resource if it doesn't have any
	 * revisions.
	 */
	@SuppressWarnings("unchecked")
	<ResourceType> ResourceType getLoadedResource(Scene scene, UUID resourceId, int revision) {
		LoadedRevision loadedRevision = getLoadedRevision(scene, resourceId, revision);

		if (loadedRevision != null) {
			return (ResourceType) loadedRevision.resource;
		}

		return null;
	}

	/**
	 * Get all resources loaded in this scene
	 * @param scene the scene to get all loaded resources from
	 * @return all loaded resources in this scene. Don't forget to use Pool.arraylist.free(resources) once
	 * you have used it!
	 */
	ArrayList<IResource> getAllLoadedSceneResources(Scene scene) {
		@SuppressWarnings("unchecked")
		ArrayList<IResource> resources = Pools.arrayList.obtain();
		resources.clear();

		ObjectMap<UUID, LoadedResource> sceneResources = mLoadedResources.get(scene);
		if (sceneResources != null) {
			for (ObjectMap.Entry<UUID, LoadedResource> resourceEntry : sceneResources.entries()) {
				for (ObjectMap.Entry<Integer, LoadedRevision> revisionEntry : resourceEntry.value.revisions.entries()) {
					resources.add(revisionEntry.value.resource);
				}
			}
		}

		return resources;
	}

	/**
	 * Get all loaded resources of the specified type for this scene
	 * @param <ResourceType> Type of the resource to get
	 * @param scene the scene to get all loaded resources from
	 * @param type the type of resource to get
	 * @return all loaded resource of the specified type in this scene. Don't forget to use Pool.arrayList.free(resources)
	 * once you have used it!
	 */
	@SuppressWarnings("unchecked")
	<ResourceType extends IResource> ArrayList<ResourceType> getAllLoadedSceneResourceOf(Scene scene, Class<ResourceType> type) {
		ArrayList<ResourceType> resources = Pools.arrayList.obtain();
		resources.clear();

		ObjectMap<UUID, LoadedResource> sceneResources = mLoadedResources.get(scene);
		if (sceneResources != null) {
			for (ObjectMap.Entry<UUID, LoadedResource> resourceEntry : sceneResources.entries()) {
				if (resourceEntry.value.type == type) {
					for (ObjectMap.Entry<Integer, LoadedRevision> revisionEntry : resourceEntry.value.revisions.entries()) {
						resources.add((ResourceType)revisionEntry.value.resource);
					}
				}
			}
		}

		return resources;
	}

	/**
	 * Clears all loaded resource for a specified scene
	 * @param scene the scene to clear all loaded resource from
	 */
	void clearLoadedSceneResources(Scene scene) {
		ObjectMap<UUID, LoadedResource> sceneResources = mLoadedResources.get(scene);
		if (sceneResources != null) {
			for (ObjectMap.Entry<UUID, LoadedResource> resourceEntry : sceneResources.entries()) {
				for (ObjectMap.Entry<Integer, LoadedRevision> revisionEntry : resourceEntry.value.revisions.entries()) {
					mLoadedRevisionPool.free(revisionEntry.value);
				}
				mLoadedResourcePool.free(resourceEntry.value);
			}

			mLoadedResources.remove(scene);
		}
	}

	/**
	 * @return string with all the current loaded resources
	 */
	String getAllLoadedResourcesString() {
		String message = "";

		// Add all loaded resources
		for (Entry<Scene, ObjectMap<UUID, LoadedResource>> sceneEntry : mLoadedResources.entries()) {
			String sceneString = sceneEntry.key.getClass().getSimpleName();

			for (Entry<UUID, LoadedResource> resourceEntry : sceneEntry.value.entries()) {
				for(Entry<Integer, LoadedRevision> revisionEntry : resourceEntry.value.revisions.entries()) {
					if (revisionEntry.value.resource != null) {
						String filePath = ResourceDatabase.getFilePath(revisionEntry.value.resource);

						message += sceneString + ": " + filePath + ", refs: " + revisionEntry.value.count + "\n";
					}
				}
			}
		}

		return message;
	}

	/**
	 * A loaded resource
	 */
	private static class LoadedResource {
		/** Resource type */
		Class<?> type;
		/** All resource revisions, if the resource doesn't have any revisions the information
		 * will be found under revision -1 */
		ObjectMap<Integer, LoadedRevision> revisions = new ObjectMap<Integer, LoadedDb.LoadedRevision>();
	}

	/**
	 * Revision information
	 */
	private static class LoadedRevision {
		/** How many times it has been loaded */
		int count = 0;
		/** The actual resource */
		IResource resource = null;
	}

	/** All scene resources */
	private ObjectMap<Scene, ObjectMap<UUID, LoadedResource>> mLoadedResources = new ObjectMap<Scene, ObjectMap<UUID,LoadedResource>>();
	/** Pool for loaded resources */
	private Pool<LoadedResource> mLoadedResourcePool = new Pool<LoadedDb.LoadedResource>(LoadedResource.class, 30, 300);
	/** Pool for loaded revisions */
	private Pool<LoadedRevision> mLoadedRevisionPool = new Pool<LoadedRevision>(LoadedRevision.class, 40, 600);
}

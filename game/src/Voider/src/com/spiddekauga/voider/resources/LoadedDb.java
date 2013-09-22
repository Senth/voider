package com.spiddekauga.voider.resources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import com.badlogic.gdx.Gdx;
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
	 * @param revision the current revision that is being loaded. If the resource doesn't use a
	 * revision this variable won't be used.
	 * @param type the type of the resource
	 * @return number of times this resource has been added onto this scene. If 1 this was the
	 * first time it was added to this scene.
	 */
	int addLoadingResource(Scene scene, UUID resourceId, int revision, Class<? extends IResource> type) {
		Map<UUID, LoadedResource> sceneResources = mLoadedResources.get(scene);

		// No existing scene, add scene
		if (sceneResources == null) {
			sceneResources = new HashMap<UUID, LoadedDb.LoadedResource>();
			mLoadedResources.put(scene, sceneResources);
		}


		LoadedResource loadedResource = sceneResources.get(resourceId);

		// Resource has never been loaded, add it
		if (loadedResource == null) {
			loadedResource = mLoadedResourcePool.obtain();
			loadedResource.revisions.clear();
			loadedResource.type = type;
			sceneResources.put(resourceId, loadedResource);
		}

		LoadedRevision loadedRevision = loadedResource.revisions.get(revision);

		// Revision has not been loaded before, add it
		if (loadedRevision == null) {
			loadedRevision = mLoadedRevisionPool.obtain();
			loadedRevision.count = 1;
			loadedResource.revisions.put(revision, loadedRevision);
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
	 * @param revision the revision of the resource
	 * @return number of instances the scene has of this resource after it has been unloaded. If
	 * it returns 0, it means it has been fully unloaded from this scene. -1 if the resource
	 * wasn't found
	 */
	int removeLoadedResource(Scene scene, UUID resourceId, int revision) {
		Map<UUID, LoadedResource> sceneResources = mLoadedResources.get(scene);

		int cLoad = -1;

		if (sceneResources != null) {
			LoadedResource loadedResource = sceneResources.get(resourceId);

			if (loadedResource != null) {

				LoadedRevision loadedRevision = loadedResource.revisions.get(revision);
				if (loadedRevision != null) {
					loadedRevision.count--;
					cLoad = loadedRevision.count;

					// Remove and free revision if this was the last of it
					if (loadedRevision.count == 0) {
						mLoadedRevisionPool.free(loadedRevision);
						loadedResource.revisions.remove(revision);

						// Remove and free resource if this was the last revision
						if (loadedResource.revisions.isEmpty()) {
							sceneResources.remove(resourceId);
							mLoadedResourcePool.free(loadedResource);

							// Remove the scene if this was the last resource
							if (sceneResources.isEmpty()) {
								mLoadedResources.remove(scene);
							}
						}
					}
				} else {
					Gdx.app.error("LoadedDb", "Could not find the revision (" + revision + ") for (" + resourceId + ")");
				}
			} else {
				Gdx.app.error("LoadedDb", "Could not find " + resourceId);
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

		for (Entry<Scene, Map<UUID, LoadedResource>> sceneEntry : mLoadedResources.entrySet()) {
			LoadedResource foundLoadedResource = sceneEntry.getValue().get(resourceId);

			if (foundLoadedResource != null) {
				if (revision == -1 || foundLoadedResource.revisions.containsKey(revision)) {
					scenes.add(sceneEntry.getKey());
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
		Map<UUID, LoadedResource> sceneResources = mLoadedResources.get(scene);

		if (sceneResources != null) {
			LoadedResource loadedResource = sceneResources.get(resourceId);

			if (loadedResource != null) {
				LoadedRevision loadedRevision = loadedResource.revisions.get(revision);
				if (loadedRevision != null) {
					return loadedRevision;
				} else if (Gdx.app != null) {
					Gdx.app.debug("LoadedDb", "Could not find the revision (" + revision + ") for (" + loadedResource.type.getSimpleName() + ": " + resourceId + ")");
				}
			} else if (Gdx.app != null) {

				Gdx.app.debug("LoadedDb", "Could not find " + resourceId);
			}
		} else if (Gdx.app != null) {
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

		Map<UUID, LoadedResource> sceneResources = mLoadedResources.get(scene);
		if (sceneResources != null) {
			for (Map.Entry<UUID, LoadedResource> resourceEntry : sceneResources.entrySet()) {
				for (Map.Entry<Integer, LoadedRevision> revisionEntry : resourceEntry.getValue().revisions.entrySet()) {
					resources.add(revisionEntry.getValue().resource);
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

		Map<UUID, LoadedResource> sceneResources = mLoadedResources.get(scene);
		if (sceneResources != null) {
			for (Map.Entry<UUID, LoadedResource> resourceEntry : sceneResources.entrySet()) {
				if (resourceEntry.getValue().type == type) {
					for (Map.Entry<Integer, LoadedRevision> revisionEntry : resourceEntry.getValue().revisions.entrySet()) {
						resources.add((ResourceType)revisionEntry.getValue().resource);
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
		Map<UUID, LoadedResource> sceneResources = mLoadedResources.get(scene);
		if (sceneResources != null) {
			for (Map.Entry<UUID, LoadedResource> resourceEntry : sceneResources.entrySet()) {
				for (Map.Entry<Integer, LoadedRevision> revisionEntry : resourceEntry.getValue().revisions.entrySet()) {
					mLoadedRevisionPool.free(revisionEntry.getValue());
				}
				mLoadedResourcePool.free(resourceEntry.getValue());
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
		for (Entry<Scene, Map<UUID, LoadedResource>> sceneEntry : mLoadedResources.entrySet()) {
			String sceneString = sceneEntry.getKey().getClass().getSimpleName();

			for (Entry<UUID, LoadedResource> resourceEntry : sceneEntry.getValue().entrySet()) {
				for(Entry<Integer, LoadedRevision> revisionEntry : resourceEntry.getValue().revisions.entrySet()) {
					if (revisionEntry.getValue().resource != null) {
						String filePath = ResourceDatabase.getFilePath(revisionEntry.getValue().resource);

						message += sceneString + ": " + filePath + ", refs: " + revisionEntry.getValue().count + "\n";
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
		Map<Integer, LoadedRevision> revisions = new HashMap<Integer, LoadedDb.LoadedRevision>();
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
	private Map<Scene, Map<UUID, LoadedResource>> mLoadedResources = new HashMap<Scene, Map<UUID,LoadedResource>>();
	/** Pool for loaded resources */
	private Pool<LoadedResource> mLoadedResourcePool = new Pool<LoadedDb.LoadedResource>(LoadedResource.class, 30, 300);
	/** Pool for loaded revisions */
	private Pool<LoadedRevision> mLoadedRevisionPool = new Pool<LoadedRevision>(LoadedRevision.class, 40, 600);
}

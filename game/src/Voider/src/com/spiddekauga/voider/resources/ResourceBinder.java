package com.spiddekauga.voider.resources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.spiddekauga.utils.Invoker;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.editor.commands.CResourceBoundRemove;
import com.spiddekauga.voider.game.Level;
import com.spiddekauga.voider.scene.SceneSwitcher;
import com.spiddekauga.voider.utils.Pools;

/**
 * Contains all the resources. Used for example in levels to later bind all
 * resources that have been loaded.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class ResourceBinder implements Json.Serializable {

	/**
	 * Adds a resource to add and keep track of (and load its resources)
	 * @param resource the resource
	 */
	public void addResource(IResource resource) {
		mResources.put(resource.getId(), resource);
	}

	/**
	 * Removes the specified resource
	 * @param resourceId the resource to remove
	 * @return resource that was removed
	 */
	public IResource removeResource(UUID resourceId) {
		IResource removedResource = mResources.remove(resourceId);

		// Skip removing bound resource for level
		if (!(removedResource instanceof Level)) {

			// Find all other resources that uses the removed resource
			// Unbind/Remove the removed resource from those
			if (removedResource != null) {
				Invoker invoker = SceneSwitcher.getInvoker();
				for (Map.Entry<UUID, IResource> entry : mResources.entrySet()) {
					IResource resource = entry.getValue();

					if (isResourceBoundIn(resource, resourceId)) {
						if (invoker != null) {
							invoker.execute(new CResourceBoundRemove(resource, removedResource), true);
						} else {
							boolean success = resource.removeBoundResource(removedResource);

							if (!success) {
								Gdx.app.error("ResourceBinder", "Failed to remove bound resource: " + removedResource.toString() + ", from: " + resource.toString());
							}
						}
					}
				}
			}
		}

		return removedResource;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((mResources == null) ? 0 : mResources.hashCode());
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
		ResourceBinder other = (ResourceBinder) obj;
		if (mResources == null) {
			if (other.mResources != null) {
				return false;
			}
		}
		else if (!mResources.equals(other.mResources)) {
			return false;
		}
		return true;
	}

	/**
	 * To easily get all the resources of a specific type after they have
	 * been read.
	 * @param <ResourceType> type of resources to return
	 * @param resourceType the resource type (including derived) to return
	 * @return a list of resources that are instances of the specified type.
	 * Don't forget to free the ArrayList once it has been used using
	 * Pools.arraylist.free(resources);
	 */
	@SuppressWarnings("unchecked")
	public <ResourceType> ArrayList<ResourceType> getResources(Class<ResourceType> resourceType) {
		ArrayList<ResourceType> resources = Pools.arrayList.obtain();
		resources.clear();

		for (Map.Entry<UUID, IResource> entry : mResources.entrySet()) {
			IResource resource = entry.getValue();

			if (resourceType.isInstance(resource)) {
				resources.add((ResourceType) resource);
			}
		}

		return resources;
	}

	/**
	 * Checks for all bound resources that uses  the specified parameter resource.
	 * @param usesResource resource to check for in all other resources
	 * @param foundResources list with all resources that uses
	 */
	public void usesResource(IResource usesResource, ArrayList<IResource> foundResources) {
		for (Map.Entry<UUID, IResource> entry : mResources.entrySet()) {
			IResource resource = entry.getValue();

			if (isResourceBoundIn(resource, usesResource.getId())) {
				foundResources.add(resource);
			}
		}
	}

	@Override
	public void write(Json json) {
		json.writeValue("Config.REVISION", Config.REVISION);
		json.writeValue("mResources", mResources);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void read(Json json, JsonValue jsonValue) {
		mResources = json.readValue("mResources", Map.class, jsonValue);
	}

	/**
	 * Binds all the resources
	 */
	public void bindResources() {
		for (Map.Entry<UUID, IResource> entry : mResources.entrySet()) {
			IResource resource = entry.getValue();

			mDependencies.clear();
			resource.getReferences(mDependencies);
			for (UUID dependencyId : mDependencies) {
				IResource foundDependency = mResources.get(dependencyId);

				if (foundDependency != null) {
					boolean success = resource.bindReference(foundDependency);

					if (!success) {
						Gdx.app.error("ResourceBinder", "Failed to bind this reference: " + foundDependency.toString() + " to: " + resource.toString());
					}
				} else {
					Gdx.app.error("ResourceBinder", "Could not find resource for " + resource.toString() + ", dependency: " + dependencyId);
				}
			}
		}
	}

	/**
	 * Replaces the specified resource with another one. This will update all
	 * resources that uses the old resource to use the new resource instead.
	 * @param oldId old resource id to be replaced
	 * @param newResource the new resource that replaces the old resource
	 */
	public void replaceResource(UUID oldId, IResource newResource) {
		// Remove and add resource
		IResource removedResource = mResources.remove(oldId);
		mResources.put(newResource.getId(), newResource);

		// Find dependencies of the old, and replace them to use the new one
		if (removedResource != null) {
			for (Map.Entry<UUID, IResource> entry : mResources.entrySet()) {
				IResource resource = entry.getValue();

				if (isResourceBoundIn(resource, oldId)) {
					boolean removedSuccess = resource.removeBoundResource(removedResource);

					if (removedSuccess) {
						boolean addedSuccess = resource.addBoundResource(newResource);

						if (!addedSuccess) {
							Gdx.app.error("ResourceBinder", "Failed to replace resource, could not add the resource " + newResource.toString());
						}
					} else {
						Gdx.app.error("ResourceBinder", "Failed to replace resource, could not remove bound resource " + newResource.toString());
					}
				}
			}
		} else {
			Gdx.app.error("ResourceBinder", "Failed to replace resource, did not find resource to replace");
		}
	}

	/**
	 * Checks if a resource is bound for the selected resource
	 * @param insideThis the resource to check if boundResourceId is in
	 * @param boundResourceId the resource to check if resourceToCheckIn uses.
	 * @return true if resourceToCheckIn uses boundResourceId
	 */
	private boolean isResourceBoundIn(IResource insideThis, UUID boundResourceId) {
		mDependencies.clear();
		insideThis.getReferences(mDependencies);

		return mDependencies.contains(boundResourceId);
	}

	/** All the resources */
	@Tag(102) private Map<UUID, IResource> mResources = new HashMap<UUID, IResource>();
	/** Temporary array for getting dependencies */
	private static ArrayList<UUID> mDependencies = new ArrayList<UUID>();
}

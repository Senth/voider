package com.spiddekauga.voider.resources;

import java.util.ArrayList;
import java.util.UUID;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.OrderedMap;
import com.spiddekauga.utils.Json;
import com.spiddekauga.voider.Config;

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
	 */
	public void removeResource(UUID resourceId) {
		mResources.remove(resourceId);
	}

	/**
	 * To easily get all the resources of a specific type after they have
	 * been read.
	 * @param <ResourceType> type of resources to return
	 * @param resourceType the resource type (including derived) to return
	 * @return a list of resources that are instances of the specified type.
	 */
	@SuppressWarnings("unchecked")
	public <ResourceType> ArrayList<ResourceType> getResources(Class<ResourceType> resourceType) {
		ArrayList<ResourceType> resources = new ArrayList<ResourceType>();

		for (ObjectMap.Entry<UUID, IResource> entry : mResources.entries()) {
			IResource resource = entry.value;

			if (resourceType.isInstance(resource)) {
				resources.add((ResourceType) resource);
			}
		}

		return resources;
	}

	@Override
	public void write(Json json) {
		json.writeValue("REVISION", Config.REVISION);
		json.writeValue("mResources", mResources);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void read(Json json, OrderedMap<String, Object> jsonData) {
		mResources = json.readValue("mResources", ObjectMap.class, jsonData);

		bindResources();
	}

	/**
	 * Binds all the resources
	 */
	private void bindResources() {
		for (ObjectMap.Entry<UUID, IResource> entry : mResources.entries()) {
			IResource resource = entry.value;

			ArrayList<UUID> dependencies = resource.getReferences();
			if (dependencies != null) {
				for (UUID dependencyId : dependencies) {
					IResource foundDependency = mResources.get(dependencyId);

					if (foundDependency != null) {
						resource.bindReference(foundDependency);
					} else {
						Gdx.app.error("ResourceBinder", "Could not find resource for " + resource.getId() + ", dependency: " + dependencyId);
					}
				}
			}
		}
	}

	/** All the resources */
	private ObjectMap<UUID, IResource> mResources = new ObjectMap<UUID, IResource>();
}

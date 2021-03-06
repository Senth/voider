package com.spiddekauga.voider.resources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.spiddekauga.utils.commands.Command;
import com.spiddekauga.utils.commands.Invoker;
import com.spiddekauga.voider.game.Level;
import com.spiddekauga.utils.scene.ui.SceneSwitcher;

/**
 * Contains all the resources. Used for example in levels to later bind all resources that
 * have been loaded.

 */
public class ResourceContainer {

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
	 * @param addRemoveBoundResourceToInvoker if the removed bound resources should be
	 *        added to the invoker. If the removed resource should be undone via the
	 *        undo() command this variable should be true, otherwise the resource won't be
	 *        bound to the previously bound resources.
	 * @return resource that was removed
	 */
	public IResource removeResource(UUID resourceId, boolean addRemoveBoundResourceToInvoker) {
		IResource removedResource = mResources.remove(resourceId);

		// Skip removing bound resource for level
		if (!(removedResource instanceof Level)) {

			// Find all other resources that uses the removed resource
			// Unbind/Remove the removed resource from those
			if (removedResource != null) {
				ArrayList<Command> commands = new ArrayList<>();
				for (Map.Entry<UUID, IResource> entry : mResources.entrySet()) {
					IResource resource = entry.getValue();
					resource.removeBoundResource(removedResource, commands);
				}

				// Unbind the resources
				// Ability to undo the bind
				if (addRemoveBoundResourceToInvoker) {
					Invoker invoker = SceneSwitcher.getInvoker();
					invoker.execute(commands, true, true);
				}
				// Just unbind the resource
				else {
					for (Command command : commands) {
						command.execute();
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
		ResourceContainer other = (ResourceContainer) obj;
		if (mResources == null) {
			if (other.mResources != null) {
				return false;
			}
		} else if (!mResources.equals(other.mResources)) {
			return false;
		}
		return true;
	}

	/**
	 * To easily get all the resources of a specific type after they have been read.
	 * @param <ResourceType> type of resources to return
	 * @param resourceType the resource type (including derived) to return
	 * @return a list of resources that are instances of the specified type. Don't forget
	 *         to free the ArrayList once it has been used using
	 *         Pools.arraylist.free(resources);
	 */
	@SuppressWarnings("unchecked")
	public <ResourceType> ArrayList<ResourceType> getResources(Class<ResourceType> resourceType) {
		ArrayList<ResourceType> resources = new ArrayList<>();

		for (Map.Entry<UUID, IResource> entry : mResources.entrySet()) {
			IResource resource = entry.getValue();

			if (resourceType.isInstance(resource)) {
				resources.add((ResourceType) resource);
			}
		}

		return resources;
	}

	/** All the resources */
	@Tag(102) private Map<UUID, IResource> mResources = new HashMap<UUID, IResource>();
}

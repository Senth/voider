package com.spiddekauga.voider.resources;

import java.util.UUID;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.scene.Scene;

/**
 * Wrapper to simplify the queue. Only available for this pacakage
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class ResourceItem implements Json.Serializable {
	/**
	 * Checks whether the resources are the same
	 * @param object another queue item
	 * @return true if the parameter is a queue item and they have the same resourceId.
	 */
	@Override
	public boolean equals(Object object) {
		if (object == this) {
			return true;
		} else if (object == null) {
			return false;
		} else if (object.getClass() == this.getClass()) {
			return ((ResourceItem) object).resourceId.equals(resourceId);
		} else if (object instanceof UUID) {
			return resourceId.equals(object);
		} else {
			return false;
		}
	}

	/**
	 * Constructor that sets the resource id and type
	 * @param resourceId id of the resource
	 * @param resourceType class of the resource
	 * @param revision the revision of the resourc
	 */
	public ResourceItem(UUID resourceId, Class<?> resourceType, int revision) {
		this(null, resourceId, resourceType, revision);
	}

	/**
	 * Constructor that sets the resource id and type
	 * @param scene to use for the resource
	 * @param resourceId id of the resource
	 * @param resourceType class of the resource
	 * @param revision the revision of the resourc
	 */
	public ResourceItem(Scene scene, UUID resourceId, Class<?> resourceType, int revision) {
		this.scene = scene;
		this.resourceId = resourceId;
		this.resourceType = resourceType;
		this.revision = revision;
		if (resourceType != null) {
			try {
				fullName = ResourceNames.getDirPath(resourceType) + resourceId.toString();
			} catch (UndefinedResourceTypeException e) {
				Gdx.app.error("UndefinedResourceType", e.toString());
			}
		}
		count = 1;
	}

	/**
	 * Default constructor for queue item, used for when reading instances from json
	 */
	public ResourceItem() {
		// Does nothing
	}

	@Override
	public int hashCode() {
		return resourceId.hashCode();
	}

	/** Number of references */
	public int count = 0;
	/** Scene */
	public Scene scene = null;
	/** Unique id */
	public UUID resourceId = null;
	/** Resource Type */
	public Class<?> resourceType = null;
	/** The full file path to this resource */
	public String fullName = null;
	/** revision of the resource */
	public int revision = -1;

	@Override
	public void write(Json json) {
		json.writeValue("REVISION", Config.REVISION);
		json.writeValue("resourceId", resourceId.toString());
		json.writeValue("resourceType", resourceType.getName());
		json.writeValue("count", count);
		json.writeValue("revision", revision);

		// We don't write the fullName, it's derived when reading the file again
	}

	@Override
	public void read(Json json, JsonValue jsonData) {
		resourceId = UUID.fromString(json.readValue("resourceId", String.class, jsonData));
		count = json.readValue("count", int.class, jsonData);

		if (jsonData.get("revision") != null) {
			revision = json.readValue("revision", int.class, jsonData);
		}

		// resourceType
		String className = json.readValue("resourceType", String.class, jsonData);
		try {
			resourceType = Class.forName(className);
		} catch (ClassNotFoundException e) {
			Gdx.app.error("JsonRead", "Class not found for class: " + className);
		}


		// Derive the fullName from the resource id and type
		try {
			fullName = ResourceNames.getDirPath(resourceType) + resourceId.toString();
		} catch (UndefinedResourceTypeException e) {
			Gdx.app.error("UndefinedResourceType", e.toString());
		}
	}
}

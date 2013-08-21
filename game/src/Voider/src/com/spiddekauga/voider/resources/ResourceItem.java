package com.spiddekauga.voider.resources;

import java.util.UUID;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

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
	 */
	public ResourceItem(UUID resourceId, Class<?> resourceType) {
		this.resourceId = resourceId;
		this.resourceType = resourceType;
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
	/** Unique id */
	UUID resourceId = null;
	/** Resource Type */
	Class<?> resourceType = null;
	/** The full file path to this resource */
	String fullName = null;

	@Override
	public void write(Json json) {
		json.writeValue("resourceId", resourceId.toString());
		json.writeValue("resourceType", resourceType.getName());
		json.writeValue("count", count);

		// We don't write the fullName, it's derived when reading the file again
	}

	@Override
	public void read(Json json, JsonValue jsonValue) {
		resourceId = UUID.fromString(json.readValue("resourceId", String.class, jsonValue));
		count = json.readValue("count", int.class, jsonValue);

		// resourceType
		String className = json.readValue("resourceType", String.class, jsonValue);
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

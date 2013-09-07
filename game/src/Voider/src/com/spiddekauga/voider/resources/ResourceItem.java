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
			return ((ResourceItem) object).id.equals(id);
		} else if (object instanceof UUID) {
			return id.equals(object);
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
		this.id = resourceId;
		this.type = resourceType;
		this.revision = revision;
	}

	/**
	 * Default constructor for queue item, used for when reading instances from json
	 */
	public ResourceItem() {
		// Does nothing
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	/** Number of references */
	public int count = 1;
	/** Scene */
	public Scene scene = null;
	/** Unique id */
	public UUID id = null;
	/** Resource Type */
	public Class<?> type = null;
	/** revision of the resource */
	public int revision = -1;

	@Override
	public void write(Json json) {
		json.writeValue("REVISION", Config.REVISION);
		json.writeValue("resourceId", id.toString());
		json.writeValue("resourceType", type.getName());
		json.writeValue("count", count);
		json.writeValue("revision", revision);
	}

	@Override
	public void read(Json json, JsonValue jsonData) {
		id = UUID.fromString(json.readValue("resourceId", String.class, jsonData));
		count = json.readValue("count", int.class, jsonData);

		if (jsonData.get("revision") != null) {
			revision = json.readValue("revision", int.class, jsonData);
		}

		// resourceType
		String className = json.readValue("resourceType", String.class, jsonData);
		try {
			type = Class.forName(className);
		} catch (ClassNotFoundException e) {
			Gdx.app.error("JsonRead", "Class not found for class: " + className);
		}
	}
}

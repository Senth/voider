package com.spiddekauga.voider.resources;

import java.util.UUID;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.OrderedMap;

/**
 * Wrapper to simplify the queue. Only available for this pacakage
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
class DefinitionItem implements Json.Serializable {
	/**
	 * Checks whether the resources are the same
	 * @param queueItem another queue item
	 * @return true if the parameter is a queue item and they have the same resourceId.
	 */
	@Override
	public boolean equals(Object queueItem) {
		if (queueItem.getClass() == this.getClass()) {
			return ((DefinitionItem) queueItem).resourceId == resourceId;
		} else {
			return false;
		}
	}

	/**
	 * Constructor that sets the resource id and type
	 * @param resourceId id of the resource
	 * @param resourceType class of the resource
	 */
	DefinitionItem(UUID resourceId, Class<?> resourceType) {
		this.resourceId = resourceId;
		this.resourceType = resourceType;
		fullName = ResourceNames.getDirPath(resourceType) + resourceId.toString();
	}

	/**
	 * Default constructor for queue item, used for when reading instances from json
	 */
	DefinitionItem() {
		// Does nothing
	}

	/** Unique id */
	UUID resourceId = null;;
	/** Resource Type */
	Class<?> resourceType = null;;
	/** The full file path to this resource */
	String fullName = null;;

	/* (non-Javadoc)
	 * @see com.badlogic.gdx.utils.Json.Serializable#write(com.badlogic.gdx.utils.Json)
	 */
	@Override
	public void write(Json json) {
		json.writeValue("resourceId", resourceId.toString());
		json.writeValue("resourceType", resourceType.getName());

		// We don't write the fullName, it's derived when reading the file again
	}

	/* (non-Javadoc)
	 * @see com.badlogic.gdx.utils.Json.Serializable#read(com.badlogic.gdx.utils.Json, com.badlogic.gdx.utils.OrderedMap)
	 */
	@Override
	public void read(Json json, OrderedMap<String, Object> jsonData) {
		resourceId = UUID.fromString(json.readValue("resourceId", String.class, jsonData));

		// resourceType
		String className = json.readValue("resourceType", String.class, jsonData);
		try {
			resourceType = Class.forName(className);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		// Derive the fullName from the resource id and type
		fullName = ResourceNames.getDirPath(resourceType) + resourceId.toString();
	}
}

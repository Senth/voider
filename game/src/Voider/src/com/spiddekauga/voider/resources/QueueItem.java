package com.spiddekauga.voider.resources;

import java.util.UUID;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.OrderedMap;

/**
 * Wrapper to simplify the queue. Only available for this pacakage
 * @param <ResourceType> the resource class
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
class QueueItem implements Json.Serializable {
	/**
	 * Checks whether the resources are the same
	 * @param queueItem another queue item
	 * @return true if the parameter is a queue item and they have the same resourceId.
	 */
	@Override
	public boolean equals(Object queueItem) {
		if (queueItem.getClass() == this.getClass()) {
			return ((QueueItem) queueItem).resourceId == resourceId;
		} else {
			return false;
		}
	}

	/**
	 * Constructor that sets the resource id and type
	 * @param resourceId id of the resource
	 * @param resourceType class of the resource
	 */
	QueueItem(UUID resourceId, Class<?> resourceType) {
		this.resourceId = resourceId;
		this.resourceType = resourceType;
		fullName = ResourceNames.getDirPath(resourceType) + resourceId.toString();
	}

	/** Unique id */
	UUID resourceId;
	/** Resource Type */
	Class<?> resourceType;
	/** The full file path to this resource */
	String fullName;

	/* (non-Javadoc)
	 * @see com.badlogic.gdx.utils.Json.Serializable#write(com.badlogic.gdx.utils.Json)
	 */
	@Override
	public void write(Json json) {
		json.writeValue("resourceId", resourceId);
		json.writeValue("resourceType", resourceType.getName());

		// TODO how do we save the resource type?


		// We don't write the fullName.
	}

	/* (non-Javadoc)
	 * @see com.badlogic.gdx.utils.Json.Serializable#read(com.badlogic.gdx.utils.Json, com.badlogic.gdx.utils.OrderedMap)
	 */
	@Override
	public void read(Json json, OrderedMap<String, Object> jsonData) {
		resourceId = json.readValue("resourceId", UUID.class, jsonData);
		String className = json.readValue("resourceType", String.class, jsonData);

		try {
			resourceType = Class.forName(className);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// TODO read resource type
		// TODO derive the fullName from the resource id and type
	}
}

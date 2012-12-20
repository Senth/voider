package com.spiddekauga.voider.resources;

import java.util.UUID;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.OrderedMap;

/**
 * Base class for all resources
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class Resource implements IResource, Json.Serializable {

	/* (non-Javadoc)
	 * @see com.spiddekauga.voider.resources.IResource#getId()
	 */
	@Override
	public UUID getId() {
		return mUniqueId;
	}

	/** Unique id of the resource */
	protected UUID mUniqueId = null;

	@Override
	public boolean equals(Object object) {
		if (object == null) {
			return false;
		} else if (object.getClass() == this.getClass()) {
			return mUniqueId.equals(((IResource)object).getId());
		} else if (object instanceof UUID) {
			return mUniqueId.equals(object);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return mUniqueId.hashCode();
	}

	/* (non-Javadoc)
	 * @see com.badlogic.gdx.utils.Json.Serializable#write(com.badlogic.gdx.utils.Json)
	 */
	@Override
	public void write(Json json) {
		json.writeValue("mUniqueId", mUniqueId.toString());
	}

	/* (non-Javadoc)
	 * @see com.badlogic.gdx.utils.Json.Serializable#read(com.badlogic.gdx.utils.Json, com.badlogic.gdx.utils.OrderedMap)
	 */
	@Override
	public void read(Json json, OrderedMap<String, Object> jsonData) {
		mUniqueId = UUID.fromString(json.readValue("mUniqueId", String.class, jsonData));
	}
}

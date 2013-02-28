package com.spiddekauga.voider.resources;

import java.util.ArrayList;
import java.util.UUID;

import com.badlogic.gdx.utils.OrderedMap;
import com.spiddekauga.utils.Json;
import com.spiddekauga.voider.Config;

/**
 * Base class for all resources
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public abstract class Resource implements IResource, Json.Serializable {
	@Override
	public UUID getId() {
		return mUniqueId;
	}

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

	/**
	 * Creates a copy of this resource, but with a different unique id
	 * @param <ResourceType> the type of this resource
	 * @return copy of this resource
	 */
	@SuppressWarnings("unchecked")
	public <ResourceType> ResourceType copy() {
		Class<?> derivedClass = getClass();

		Json json = new Json();
		String jsonString = json.toJson(this);
		Resource copy = (Resource) json.fromJson(derivedClass, jsonString);

		copy.mUniqueId = UUID.randomUUID();

		return (ResourceType) copy;
	}

	@Override
	public int hashCode() {
		return mUniqueId.hashCode();
	}

	@Override
	public void getReferences(ArrayList<UUID> references) {
		// Does nothing here
	}

	@Override
	public boolean bindReference(IResource resource) {
		// Does nothing here
		return false;
	}

	@Override
	public boolean addBoundResource(IResource boundResource)  {
		// Does nothing here
		return false;
	}

	@Override
	public boolean removeBoundResource(IResource boundResource) {
		// Does nothing here
		return false;
	}

	@Override
	public void write(Json json) {
		json.writeValue("REVISION", Config.REVISION);
		json.writeValue("mUniqueId", mUniqueId);
	}

	@Override
	public void read(Json json, OrderedMap<String, Object> jsonData) {
		mUniqueId = json.readValue("mUniqueId", UUID.class, jsonData);
	}

	/** Unique id of the resource */
	protected UUID mUniqueId = null;
}

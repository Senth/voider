package com.spiddekauga.voider.resources;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.ObjectMap.Entry;
import com.badlogic.gdx.utils.OrderedMap;

/**
 * Base class for all "definitions", e.g. ActorDef, WeaponDef. All definitions
 * shall derive from this class.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class Def {
	/**
	 * Default constructor for the resource.
	 */
	public Def() {
		mUniqueId = UUID.randomUUID();

		// We don't initialize the external and internal dependencies
		// here because:
		// 1. We don't want them to take up extra space if they aren't needed.
		// 2. When loading from file they would first be initialized with an
		// empty map/set, then initialized with the proper one.
	}

	/**
	 * Checks if two definitions are equal to each other. Note that these only test
	 * the unique id and nothing else.
	 * @param object the other object to test if it is the same definition
	 * @return true if the object is a definition and has the same unique id as this
	 */
	@Override
	public boolean equals(Object object) {
		if (object == this) {
			return true;
		} else if (object != null && object.getClass() == this.getClass()) {
			return ((Def) object).mUniqueId.equals(mUniqueId);
		} else {
			return false;
		}
	}

	/**
	 * @return the unique id of the resource
	 */
	public final UUID getId() {
		return mUniqueId;
	}

	/**
	 * @return all external dependencies
	 */
	public final Set<DefItem> getExternalDependencies() {
		return mExternalDependencies;
	}

	/**
	 * @return all internal dependencies
	 */
	public final Set<ResourceNames> getInternalDependencies() {
		return mInternalDependencies;
	}

	/**
	 * Writes this class to a json object. This method is only required to
	 * be called when a derived class implements the Json.Serializable interface
	 * @note This class does not implement this interface, the method just looks
	 * the same for simplicity; this allows derived classes to skip implementing
	 * (and overriding) the write method.
	 * @param json the object which we write all this classe's variables to.
	 */
	protected void write(Json json) {
		json.writeValue("mUniqueId", mUniqueId.toString());

		if (mInternalDependencies == null || mInternalDependencies.isEmpty()) {
			json.writeValue("mInternalDependencies", (OrderedMap<?,?>) null);
		} else {
			json.writeObjectStart("mInternalDependencies");
			for (ResourceNames item : mInternalDependencies) {
				json.writeValue(item.toString(), item);
			}
			json.writeObjectEnd();
		}

		if (mExternalDependencies == null || mExternalDependencies.isEmpty()) {
			json.writeValue("mExternalDependencies", (OrderedMap<?,?>) null);
		} else {
			json.writeObjectStart("mExternalDependencies");
			int i= 0;
			for (DefItem item : mExternalDependencies) {
				json.writeValue(Integer.toString(i), item);
				++i;
			}
			json.writeObjectEnd();
		}
	}

	/**
	 * Reads this class as a json object. This method is only required to be
	 * called when a derived class implements the Json.Serializable interface.
	 * @note This class does not implement this interface, the method just look
	 * the same for simplicity; this allows derived classes to skip implementing
	 * (and overriding) the read method.
	 * @param json the json to read the value from
	 * @param jsonData this is where all the json variables have been loaded
	 */
	@SuppressWarnings("unchecked")
	protected void read(Json json, OrderedMap<String, Object> jsonData) {
		mUniqueId = UUID.fromString(json.readValue("mUniqueId", String.class, jsonData));

		OrderedMap<?,?> internalMap = json.readValue("mInternalDependencies", OrderedMap.class, jsonData);
		if (internalMap != null) {
			mInternalDependencies = new HashSet<ResourceNames>(internalMap.size);
			for (Entry<?,?> entry : internalMap.entries()) {
				mInternalDependencies.add(ResourceNames.valueOf((String)entry.value));
			}
		}

		OrderedMap<?,?> externalMap = json.readValue("mExternalDependencies", OrderedMap.class, jsonData);
		if (externalMap != null) {
			mExternalDependencies = new HashSet<DefItem>(externalMap.size);
			for (int i = 0; i < externalMap.size; ++i) {
				DefItem def = json.readValue(Integer.toString(i), DefItem.class, externalMap);
				mExternalDependencies.add(def);
			}
		}
	}

	/**
	 * Adds an external dependency to the resource
	 * @param dependency the resource dependency
	 * @see #addDependency(ResourceNames)
	 */
	protected void addDependency(Def dependency) {
		if (mExternalDependencies == null) {
			mExternalDependencies = new HashSet<DefItem>();
		}
		mExternalDependencies.add(new DefItem(dependency.getId(), dependency.getClass()));
	}

	/**
	 * Adds an internal dependency to the resource
	 * @param dependency the resource dependency
	 * @see #addDependency(Def)
	 */
	protected void addDependency(ResourceNames dependency) {
		if (mInternalDependencies == null) {
			mInternalDependencies = new HashSet<ResourceNames>();
		}
		mInternalDependencies.add(dependency);
	}

	/**
	 * Removes an external dependency from the resource
	 * @param dependency the id of the dependency to remove
	 */
	protected void removeDependency(UUID dependency) {
		if (mExternalDependencies == null) {
			return;
		}
		mExternalDependencies.remove(new DefItem(dependency, null));
	}

	/**
	 * Removes an internal dependency from the resource
	 * @param dependency the name of the dependency to remove
	 */
	protected void removeDependency(ResourceNames dependency) {
		if (mInternalDependencies == null) {
			return;
		}
		mInternalDependencies.remove(dependency);
	}

	/** A unique id for the resource */
	private UUID mUniqueId;
	/** Dependencies for the resource */
	private Set<DefItem> mExternalDependencies = null;
	/** Internal dependencies, such as textures, sound, particle effects */
	private Set<ResourceNames> mInternalDependencies = null;
}

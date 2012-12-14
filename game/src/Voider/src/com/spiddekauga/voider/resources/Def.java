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
public abstract class Def implements IResource, Json.Serializable {
	/**
	 * Default constructor for the resource.
	 */
	public Def() {
		mUniqueId = UUID.randomUUID();

		/** @TODO Set creator and original creator somehow */
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
	@Override
	public final UUID getId() {
		return mUniqueId;
	}

	/**
	 * @return number of external dependencies
	 */
	public int getExternalDependenciesCount() {
		return mExternalDependencies.size();
	}

	/**
	 * @return number of internal dependencies
	 */
	public int getInternalDependenciesCount() {
		return mInternalDependencies.size();
	}

	/**
	 * @return the name of the definition
	 */
	public final String getName() {
		return mName;
	}

	/**
	 * Sets the name of the definition
	 * @param name the new name of the definition
	 */
	public void setName(String name) {
		mName = name;
	}

	/**
	 * @return the name of the creator
	 */
	public final String getCreator() {
		return mCreator;
	}

	/**
	 * @return the original creator of this definition
	 */
	public final String getOriginalCreator() {
		return mOriginalCreator;
	}

	/**
	 * @return the comment of the definition
	 */
	public final String getComment() {
		return mComment;
	}

	/**
	 * Sets the comment of the definition
	 * @param comment the new comment
	 */
	public void setComment(String comment) {
		mComment = comment;
	}

	/**
	 * Writes this class to a json object.
	 * @param json the object which we write all this classe's variables to.
	 */
	@Override
	public void write(Json json) {
		json.writeValue("mUniqueId", mUniqueId.toString());
		json.writeValue("mName", mName);
		json.writeValue("mComment", mComment);
		json.writeValue("mCreator", mCreator);
		json.writeValue("mOriginalCreator", mOriginalCreator);

		if (mInternalDependencies.isEmpty()) {
			json.writeValue("mInternalDependencies", (OrderedMap<?,?>) null);
		} else {
			json.writeObjectStart("mInternalDependencies");
			for (ResourceNames item : mInternalDependencies) {
				json.writeValue(item.toString(), item);
			}
			json.writeObjectEnd();
		}

		if (mExternalDependencies.isEmpty()) {
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
	 * Reads this class as a json object.
	 * @param json the json to read the value from
	 * @param jsonData this is where all the json variables have been loaded
	 */
	@Override
	public void read(Json json, OrderedMap<String, Object> jsonData) {
		mUniqueId = UUID.fromString(json.readValue("mUniqueId", String.class, jsonData));
		mName = json.readValue("mName", String.class, jsonData);
		mCreator = json.readValue("mCreator", String.class, jsonData);
		mOriginalCreator = json.readValue("mOriginalCreator", String.class, jsonData);
		mComment = json.readValue("mComment", String.class, jsonData);

		OrderedMap<?,?> internalMap = json.readValue("mInternalDependencies", OrderedMap.class, jsonData);
		if (internalMap != null) {
			for (Entry<?,?> entry : internalMap.entries()) {
				mInternalDependencies.add(ResourceNames.valueOf((String)entry.value));
			}
		}

		OrderedMap<?,?> externalMap = json.readValue("mExternalDependencies", OrderedMap.class, jsonData);
		if (externalMap != null) {
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
		mExternalDependencies.add(new DefItem(dependency.getId(), dependency.getClass()));
	}

	/**
	 * Adds an external dependency to the resource
	 * @param uuid the unique id of the dependency
	 * @param type the type of dependency
	 */
	protected void addDependency(UUID uuid, Class<?> type) {
		mExternalDependencies.add(new DefItem(uuid, type));
	}

	/**
	 * Adds an internal dependency to the resource
	 * @param dependency the resource dependency
	 * @see #addDependency(Def)
	 */
	protected void addDependency(ResourceNames dependency) {
		mInternalDependencies.add(dependency);
	}

	/**
	 * Removes an external dependency from the resource
	 * @param dependency the id of the dependency to remove
	 */
	protected void removeDependency(UUID dependency) {
		mExternalDependencies.remove(new DefItem(dependency, null));
	}

	/**
	 * Removes an internal dependency from the resource
	 * @param dependency the name of the dependency to remove
	 */
	protected void removeDependency(ResourceNames dependency) {
		mInternalDependencies.remove(dependency);
	}

	/**
	 * @return all external dependencies
	 */
	final Set<DefItem> getExternalDependencies() {
		return mExternalDependencies;
	}

	/**
	 * @return all internal dependencies
	 */
	final Set<ResourceNames> getInternalDependencies() {
		return mInternalDependencies;
	}

	/**
	 * Sets the creator, only for JUNIT testing
	 * @param creator the new creator name
	 */
	void setCreator(String creator) {
		mCreator = creator;
	}

	/**
	 * Sets the original creator, only for JUNIT testing
	 * @param creator the original creator
	 */
	void setOriginalCreator(String creator) {
		mOriginalCreator = creator;
	}

	/** A unique id for the resource */
	private UUID mUniqueId;
	/** Dependencies for the resource */
	private Set<DefItem> mExternalDependencies = new HashSet<DefItem>();
	/** Internal dependencies, such as textures, sound, particle effects */
	private Set<ResourceNames> mInternalDependencies = new HashSet<ResourceNames>();
	/** Name of the definition */
	private String mName = "Unnamed";
	/** Original creator name */
	private String mOriginalCreator = "Unnamed";
	/** Creator name */
	private String mCreator = "Unnamed";
	/** Comment of the definition */
	private String mComment = null;
}

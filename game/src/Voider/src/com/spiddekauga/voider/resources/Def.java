package com.spiddekauga.voider.resources;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.badlogic.gdx.utils.ObjectMap.Entry;
import com.badlogic.gdx.utils.OrderedMap;
import com.spiddekauga.utils.Json;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.User;

/**
 * Base class for all "definitions", e.g. ActorDef, WeaponDef. All definitions
 * shall derive from this class.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public abstract class Def extends Resource implements Json.Serializable {
	/**
	 * Default constructor for the resource.
	 */
	public Def() {
		mUniqueId = UUID.randomUUID();
	}

	/**
	 * Creates a copy of this definition, automatically resets revision
	 * creator, unique id.
	 * @return copy of this definition.
	 */
	public Def copy() {
		Class<?> derivedClass = getClass();

		Json json = new Json();
		String defString = json.toJson(this);
		Def copy = (Def) json.fromJson(derivedClass, defString);

		copy.mCreator = User.getNickName();
		copy.mName = copy.mName + " (copy)";
		copy.mUniqueId = UUID.randomUUID();

		return copy;
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
	 * @return the comment/description of the definition
	 */
	public final String getDescription() {
		return mDescription;
	}

	/**
	 * Sets the comment/description of the definition
	 * @param description the new comment
	 */
	public void setDescription(String description) {
		mDescription = description;
	}

	/**
	 * Writes this class to a json object.
	 * @param json the object which we write all this classe's variables to.
	 */
	@Override
	public void write(Json json) {
		super.write(json);

		json.writeValue("REVISION", Config.REVISION);
		json.writeValue("mName", mName);
		json.writeValue("mDescription", mDescription);
		json.writeValue("mCreator", mCreator);
		json.writeValue("mOriginalCreator", mOriginalCreator);

		if (mInternalDependencies.isEmpty()) {
			json.writeValue("mInternalDependencies", (Set<?>) null);
		} else {
			json.writeObjectStart("mInternalDependencies");
			for (ResourceNames item : mInternalDependencies) {
				json.writeValue(item.toString(), item);
			}
			json.writeObjectEnd();
		}

		if (mExternalDependencies.isEmpty()) {
			json.writeValue("mExternalDependencies", (Set<?>) null);
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
		super.read(json, jsonData);

		mName = json.readValue("mName", String.class, jsonData);
		mCreator = json.readValue("mCreator", String.class, jsonData);
		mOriginalCreator = json.readValue("mOriginalCreator", String.class, jsonData);
		mDescription = json.readValue("mDescription", String.class, jsonData);

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
	public void addDependency(Def dependency) {
		mExternalDependencies.add(new DefItem(dependency.getId(), dependency.getClass()));
	}

	/**
	 * Adds an external dependency to the resource
	 * @param uuid the unique id of the dependency
	 * @param type the type of dependency
	 */
	public void addDependency(UUID uuid, Class<?> type) {
		mExternalDependencies.add(new DefItem(uuid, type));
	}

	/**
	 * Adds an internal dependency to the resource
	 * @param dependency the resource dependency
	 * @see #addDependency(Def)
	 */
	public void addDependency(ResourceNames dependency) {
		mInternalDependencies.add(dependency);
	}

	/**
	 * Removes an external dependency from the resource
	 * @param dependency the id of the dependency to remove
	 */
	public void removeDependency(UUID dependency) {
		mExternalDependencies.remove(new DefItem(dependency, null));
	}

	/**
	 * Removes an internal dependency from the resource
	 * @param dependency the name of the dependency to remove
	 */
	public void removeDependency(ResourceNames dependency) {
		mInternalDependencies.remove(dependency);
	}

	/**
	 * @return all external dependencies
	 */
	Set<DefItem> getExternalDependencies() {
		return mExternalDependencies;
	}

	/**
	 * @return all internal dependencies
	 */
	Set<ResourceNames> getInternalDependencies() {
		return mInternalDependencies;
	}

	/** Dependencies for the resource */
	private Set<DefItem> mExternalDependencies = new HashSet<DefItem>();
	/** Internal dependencies, such as textures, sound, particle effects */
	private Set<ResourceNames> mInternalDependencies = new HashSet<ResourceNames>();
	/** Name of the definition */
	private String mName = "Unnamed";
	/** Original creator name */
	private String mOriginalCreator = User.getNickName();
	/** Creator name */
	private String mCreator = User.getNickName();
	/** Comment of the definition */
	private String mDescription = "";
}

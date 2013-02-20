package com.spiddekauga.voider.resources;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectMap.Entry;
import com.badlogic.gdx.utils.OrderedMap;
import com.spiddekauga.utils.Json;
import com.spiddekauga.voider.User;

/**
 * Base class for all "definitions", e.g. ActorDef, WeaponDef. All definitions
 * shall derive from this class.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
@SuppressWarnings("unchecked")
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
	@Override
	public <ResourceType> ResourceType copy() {
		Class<?> derivedClass = getClass();

		Json json = new Json();
		String defString = json.toJson(this);
		Def copy = (Def) json.fromJson(derivedClass, defString);

		copy.mCreator = User.getNickName();
		copy.mName = copy.mName + " (copy)";
		copy.mUniqueId = UUID.randomUUID();

		return (ResourceType) copy;
	}

	/**
	 * @return number of external dependencies
	 */
	public int getExternalDependenciesCount() {
		return mExternalDependencies.size;
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

		if (mExternalDependencies.size == 0) {
			json.writeValue("mExternalDependencies", (Object) null);
		} else {
			json.writeValue("mExternalDependencies", mExternalDependencies);
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

		ObjectMap<UUID, DefItem> externalDependencies = json.readValue("externalDependencies", ObjectMap.class, jsonData);
		if (externalDependencies != null) {
			mExternalDependencies = externalDependencies;
		}
	}

	/**
	 * Adds an external dependency to the resource
	 * @param dependency the resource dependency
	 * @see #addDependency(ResourceNames)
	 */
	public void addDependency(Def dependency) {
		addDependency(dependency.getId(), dependency.getClass());
	}

	/**
	 * Adds an external dependency to the resource
	 * @param uuid the unique id of the dependency
	 * @param type the type of dependency
	 */
	public void addDependency(UUID uuid, Class<?> type) {
		// Increment value of old one if one exist...
		DefItem oldDefItem = mExternalDependencies.get(uuid);

		if (oldDefItem != null) {
			oldDefItem.count++;
		} else {
			mExternalDependencies.put(uuid, new DefItem(uuid, type));
		}
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
		// Decrement the value, remove if only one was left...

		DefItem oldDefItem = mExternalDependencies.get(dependency);
		if (oldDefItem != null) {
			oldDefItem.count--;
			if (oldDefItem.count == 0) {
				mExternalDependencies.remove(dependency);
			}
		} else {
			Gdx.app.error("Def", "No dependency found to remove for the current definition");
		}
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
	ObjectMap<UUID, DefItem> getExternalDependencies() {
		return mExternalDependencies;
	}

	/**
	 * @return all internal dependencies
	 */
	Set<ResourceNames> getInternalDependencies() {
		return mInternalDependencies;
	}

	/** Dependencies for the resource */
	private ObjectMap<UUID, DefItem> mExternalDependencies = new ObjectMap<UUID, DefItem>();
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

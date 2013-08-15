package com.spiddekauga.voider.resources;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.spiddekauga.utils.JsonWrapper;
import com.spiddekauga.voider.User;

/**
 * Base class for all "definitions", e.g. ActorDef, WeaponDef. All definitions
 * shall derive from this class.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
@SuppressWarnings("unchecked")
public abstract class Def extends Resource implements Json.Serializable, IResourceDependency {
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

		Json json = new JsonWrapper();
		String defString = json.toJson(this);
		Def copy = (Def) json.fromJson(derivedClass, defString);

		copy.mCreator = User.getNickName();
		copy.mName = copy.mName + " (copy)";
		copy.mUniqueId = UUID.randomUUID();

		return (ResourceType) copy;
	}

	@Override
	public int getExternalDependenciesCount() {
		return mExternalDependencies.size;
	}

	@Override
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
		json.writeValue("mRevision", mRevision);
		json.writeValue("mVersion", getVersionString());
		json.writeValue("mOriginalCreator", mOriginalCreator);

		if (mInternalDependencies.isEmpty()) {
			json.writeValue("mInternalDependencies", (Set<?>) null);
		} else {
			json.writeValue("mInternalDependencies", mInternalDependencies);
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
	public void read(Json json, JsonValue jsonData) {
		super.read(json, jsonData);

		mName = json.readValue("mName", String.class, jsonData);
		mCreator = json.readValue("mCreator", String.class, jsonData);
		mOriginalCreator = json.readValue("mOriginalCreator", String.class, jsonData);
		mRevision = json.readValue("mRevision", long.class, jsonData);
		mDescription = json.readValue("mDescription", String.class, jsonData);

		HashSet<ResourceNames> internalMap = json.readValue("mInternalDependencies", HashSet.class, jsonData);
		if (internalMap != null) {
			mInternalDependencies.addAll(internalMap);
		}

		ObjectMap<UUID, ResourceItem> externalDependencies = json.readValue("mExternalDependencies", ObjectMap.class, jsonData);
		if (externalDependencies != null) {
			mExternalDependencies = externalDependencies;
		}

		// Version
		String stringVersion = json.readValue("mVersion", String.class, jsonData);
		String[] stringVersions = stringVersion.split("\\.");
		mVersionFirst = Integer.parseInt(stringVersions[0]);
		mVersionSecond = Integer.parseInt(stringVersions[1]);
		mVersionThird = Integer.parseInt(stringVersions[2]);
	}

	@Override
	public void addDependency(IResource dependency) {
		addDependency(dependency.getId(), dependency.getClass());
	}

	@Override
	public void addDependency(UUID uuid, Class<?> type) {
		// Increment value of old one if one exist...
		ResourceItem oldDefItem = mExternalDependencies.get(uuid);

		if (oldDefItem != null) {
			oldDefItem.count++;
		} else {
			mExternalDependencies.put(uuid, new ResourceItem(uuid, type));
		}
	}

	@Override
	public void addDependency(ResourceNames dependency) {
		mInternalDependencies.add(dependency);
	}

	@Override
	public void removeDependency(UUID dependency) {
		// Decrement the value, remove if only one was left...

		ResourceItem oldDefItem = mExternalDependencies.get(dependency);
		if (oldDefItem != null) {
			oldDefItem.count--;
			if (oldDefItem.count == 0) {
				mExternalDependencies.remove(dependency);
			}
		} else {
			Gdx.app.error("Def", "No dependency found to remove for the current definition");
		}
	}

	@Override
	public void removeDependency(ResourceNames dependency) {
		mInternalDependencies.remove(dependency);
	}

	/**
	 * Sets the version of the level
	 * @param first the first number (i.e. 1 in 1.0.13)
	 * @param second the second number (i.e. 0 in 1.0.13)
	 * @param third the third number (i.e. 13 in 1.0.13)
	 */
	public void setVersion(int first, int second, int third) {
		mVersionFirst = first;
		mVersionSecond = second;
		mVersionThird = third;
	}

	/**
	 * @return the first number in the version (i.e. 1 in 1.0.13)
	 */
	public int getVersionFirst() {
		return mVersionFirst;
	}

	/**
	 * @return the second number in the version (i.e. 0 in 1.0.13)
	 */
	public int getVersionSecond() {
		return mVersionSecond;
	}

	/**
	 * @return the third number in the version (i.e. 0 in 1.0.13)
	 */
	public int getVersionThird() {
		return mVersionThird;
	}

	/**
	 * Updates the first number in the version and resets the other counters
	 */
	public void increaseVersionFirst() {
		mVersionFirst++;
		mVersionSecond = 0;
		mVersionThird = 0;
	}

	/**
	 * Updates the second number in the version and resets the third counter.
	 * The first number is unchanged
	 */
	public void increaseVersionSecond() {
		mVersionSecond++;
		mVersionThird = 0;
	}

	/**
	 * Updates the third number in the version. The first and second number
	 * is unchanged
	 */
	public void increaseVersionThird() {
		mVersionThird++;
	}

	/**
	 * @return the version number as a string
	 */
	public String getVersionString() {
		return Integer.toString(mVersionFirst) + "." +
				Integer.toString(mVersionSecond) + "." +
				Integer.toString(mVersionThird);
	}

	/**
	 * @return the revision of the level
	 */
	public long getRevision() {
		return mRevision;
	}

	/**
	 * Increases the revision count by one
	 */
	public void increaseRevision() {
		++mRevision;
	}

	@Override
	public ObjectMap<UUID, ResourceItem> getExternalDependencies() {
		return mExternalDependencies;
	}

	@Override
	public Set<ResourceNames> getInternalDependencies() {
		return mInternalDependencies;
	}

	/** Dependencies for the resource */
	private ObjectMap<UUID, ResourceItem> mExternalDependencies = new ObjectMap<UUID, ResourceItem>();
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
	/** The revision of the definition, this increases after each save */
	private long mRevision = 1;
	/** Main version (1 in 1.0.13) */
	private int mVersionFirst = 0;
	/** Minor version (0 in 1.0.13) */
	private int mVersionSecond = 0;
	/** Third small version (13 in 1.0.13) */
	private int mVersionThird = 0;
}

package com.spiddekauga.voider.resources;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.spiddekauga.voider.User;

/**
 * Base class for all "definitions", e.g. ActorDef, WeaponDef. All definitions
 * shall derive from this class.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
@SuppressWarnings("unchecked")
public abstract class Def extends Resource implements Json.Serializable, IResourceDependency, IResourceRevision {
	/**
	 * Default constructor for the resource.
	 */
	public Def() {
		mUniqueId = UUID.randomUUID();
	}

	@Override
	public <ResourceType> ResourceType copyNewResource() {
		ResourceType copy = super.copyNewResource();

		Def defCopy = (Def)copy;
		defCopy.mCopyParentId = mUniqueId;
		defCopy.mCreator = User.getNickName();
		// TODO create numbering of copy name if already a copy
		defCopy.mName = defCopy.mName + " (copy)";

		return copy;
	}

	@Override
	public int getExternalDependenciesCount() {
		return mExternalDependencies.size();
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
		json.writeValue("mDate", mDate);
		json.writeValue("mDescription", mDescription);
		json.writeValue("mCreator", mCreator);
		json.writeValue("mRevision", mRevision);
		json.writeValue("mOriginalCreator", mOriginalCreator);

		if (mInternalDependencies.isEmpty()) {
			json.writeValue("mInternalDependencies", (Set<?>) null);
		} else {
			json.writeValue("mInternalDependencies", mInternalDependencies);
		}

		if (mExternalDependencies.size() == 0) {
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
		mDate = json.readValue("mDate", Date.class, jsonData);
		mCreator = json.readValue("mCreator", String.class, jsonData);
		mOriginalCreator = json.readValue("mOriginalCreator", String.class, jsonData);
		mRevision = json.readValue("mRevision", int.class, jsonData);
		mDescription = json.readValue("mDescription", String.class, jsonData);

		HashSet<ResourceNames> internalMap = json.readValue("mInternalDependencies", HashSet.class, jsonData);
		if (internalMap != null) {
			mInternalDependencies.addAll(internalMap);
		}

		Map<UUID, ResourceItem> externalDependencies = json.readValue("mExternalDependencies", Map.class, jsonData);
		if (externalDependencies != null) {
			mExternalDependencies = externalDependencies;
		}
	}

	@Override
	public void addDependency(IResource dependency) {
		ResourceItem oldDefItem = mExternalDependencies.get(dependency.getId());

		if (oldDefItem != null) {
			oldDefItem.count++;
		} else {
			int revision = -1;
			if (dependency instanceof IResourceRevision) {
				revision = ((IResourceRevision) dependency).getRevision();
			}

			ResourceItem newDepItem = new ResourceItem(dependency.getId(), dependency.getClass(), revision);
			mExternalDependencies.put(dependency.getId(), newDepItem);
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

	@Override
	public void updateDependencyRevision(UUID dependency, int revision) {
		ResourceItem depItem = mExternalDependencies.get(dependency);
		if (depItem != null) {
			depItem.revision = revision;
		} else {
			Gdx.app.error("Def", "No dependency found to update the revision of");
		}
	}

	/**
	 * Update the date to the current date
	 */
	public void updateDate() {
		mDate = new Date();
	}

	/**
	 * @return date of the definition
	 */
	public Date getDate() {
		return mDate;
	}

	/**
	 * @return date string
	 */
	public String getDateString() {
		// Format date
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
		String dateString = simpleDateFormat.format(mDate);
		return dateString;
	}

	/**
	 * @return the revision of the level
	 */
	public int getRevision() {
		return mRevision;
	}
	/**
	 * Sets the revision of the resource
	 * @param revision the new revision of the resource
	 */
	public void setRevision(int revision) {
		mRevision = revision;
	}

	@Override
	public Map<UUID, ResourceItem> getExternalDependencies() {
		return mExternalDependencies;
	}

	@Override
	public Set<ResourceNames> getInternalDependencies() {
		return mInternalDependencies;
	}

	/**
	 * @return if this object is a copy it will return the parent id which it was
	 * copied from, null this is the original.
	 */
	public UUID getCopyParentId() {
		return mCopyParentId;
	}

	/** Dependencies for the resource */
	@Tag(43) private Map<UUID, ResourceItem> mExternalDependencies = new HashMap<UUID, ResourceItem>();
	/** Internal dependencies, such as textures, sound, particle effects */
	@Tag(42) private Set<ResourceNames> mInternalDependencies = new HashSet<ResourceNames>();
	/** Name of the definition */
	@Tag(36) private String mName = "(Unnamed)";
	/** Original creator name */
	@Tag(41) private String mOriginalCreator = User.getNickName();
	/** Creator name */
	@Tag(39) private String mCreator = User.getNickName();
	/** Comment of the definition */
	@Tag(38) private String mDescription = "";
	/** Saved date for the definition */
	@Tag(37) private Date mDate = null;
	/** The revision of the definition, this increases after each save */
	@Tag(40) private int mRevision = 0;
	/** When copied, this is the id of the resource we copied */
	@Tag(87) private UUID mCopyParentId = null;


	// Don't forget to add to DefTest!
}

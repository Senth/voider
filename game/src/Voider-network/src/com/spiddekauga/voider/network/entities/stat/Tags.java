package com.spiddekauga.voider.network.entities.stat;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * All tags
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("javadoc")
public enum Tags {
	EASY(1),
	/** Challenging to play */
	CHALLENGING(2),
	/** Contains some sort of story */
	STORY(3),
	COMPLICATED(4),
	/** Artistically made */
	ARTISTIC(5),
	BULLET_STORM(6),
	ENEMYLESS(7, "Enemy-less"),
	SHORT(8),
	LONG(9),
	COLORFUL(10),
	NARROW(11),
	/** Surprises */
	SURPRISE(12, "Surprise!"),
	WEIRD(13),
	BORING(14),
	FUN(15),
	/** Unfinished level */
	UNFINISHED(16),


	// NEXT ID TO USE => 17

	;
	/**
	 * @param id unique id of the tag, used for storing in datastore
	 */
	Tags(int id) {
		mId = id;

		// Convert name to lower case except first letter
		mName = String.valueOf(name().charAt(0));
		mName += name().substring(1).toLowerCase();

		// Replace underscore with space
		mName = mName.replace('_', ' ');
	}

	/**
	 * @param id unique id of the tag, used for storing in datastore
	 * @param name Human readable name of the tag
	 */
	Tags(int id, String name) {
		mName = name;
		mId = id;
	}

	/**
	 * @return id used for storing in datastore, i.e. not same as ordinal.
	 */
	public int getId() {
		return mId;
	}

	@Override
	public String toString() {
		return mName;
	}

	/**
	 * Converts an integer id to an enumeration
	 * @param id tag id of the enumeration to get the actual enumeration fro
	 * @return enumeration of the tag id
	 */
	public static Tags getEnumFromId(int id) {
		return mIdToEnum.get(id);
	}

	/**
	 * Convert a list of tags into a list of tag ids
	 * @param tags all tags to convert
	 * @return list of tag ids
	 */
	public static ArrayList<Integer> toIdList(ArrayList<Tags> tags) {
		ArrayList<Integer> tagIds = new ArrayList<>();
		for (Tags tag : tags) {
			tagIds.add(tag.getId());
		}
		return tagIds;
	}

	/**
	 * Convert a list of tag ids back to a list of tags
	 * @param tagIds all tag ids to convert
	 * @return list of tags
	 */
	public static ArrayList<Tags> toTagList(ArrayList<? extends Number> tagIds) {
		ArrayList<Tags> tags = new ArrayList<>();
		if (tagIds != null) {
			for (Number tagId : tagIds) {
				tags.add(getEnumFromId(tagId.intValue()));
			}
		}
		return tags;
	}

	/** Human readable name */
	private String mName;
	/** Id used for storing in datastore */
	private int mId;
	/** From id to enum */
	private static HashMap<Integer, Tags> mIdToEnum = new HashMap<>();

	static {
		for (Tags tag : Tags.values()) {
			mIdToEnum.put(tag.mId, tag);
		}
	}
}

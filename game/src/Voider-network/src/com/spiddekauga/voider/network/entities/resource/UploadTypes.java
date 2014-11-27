package com.spiddekauga.voider.network.entities.resource;

import java.util.HashMap;


/**
 * All definition types that can be uploaded
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("javadoc")
public enum UploadTypes {
	BULLET_DEF(1),
	ENEMY_DEF(2),
	PICKUP_DEF(3),
	PLAYER_DEF(4),
	LEVEL_DEF(5),
	LEVEL(6),
	GAME_SAVE(7),
	GAME_SAVE_DEF(8),
	BUG_REPORT(9),
	CAMPAIGN_DEF(10),

	// NEXT ID: 11

	;
	/**
	 * Id for saving in datastore
	 * @param id the id saved in the datastore
	 */
	private UploadTypes(int id) {
		mId = id;
		mName = name().toLowerCase();
	}

	@Override
	public String toString() {
		return mName;
	}

	/**
	 * @return id of the movement type
	 */
	public int getId() {
		return mId;
	}

	/**
	 * Get the correct def type from the id
	 * @param id the id to get the def type of
	 * @return the correct def type for this id, null if invalid id
	 */
	public static UploadTypes fromId(int id) {
		return mIdToEnum.get(id);
	}

	/** Id for saving in datastore */
	private int mId;
	/** Human readable name for the type */
	private String mName;
	/** Map from id to def types */
	private static HashMap<Integer, UploadTypes> mIdToEnum = new HashMap<>();

	static {
		for (UploadTypes type : UploadTypes.values()) {
			mIdToEnum.put(type.mId, type);
		}
	}

}

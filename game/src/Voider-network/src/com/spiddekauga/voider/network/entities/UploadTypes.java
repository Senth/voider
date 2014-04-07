package com.spiddekauga.voider.network.entities;

import java.util.HashMap;


/**
 * All definition types that can be uploaded
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public enum UploadTypes {
	/** BulletDef */
	BULLET_DEF(1),
	/** EnemyDef */
	ENEMY_DEF(2),
	/** LevelDef */
	LEVEL_DEF(3),
	/** CampaignDef */
	CAMPAIG_DEF(4),
	/** Level */
	LEVEL(5),

	// NEXT ID TO USE => 6

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
		return mIdToDefType.get(id);
	}

	/** Id for saving in datastore */
	private int mId;
	/** Human readable name for the type */
	private String mName;
	/** Map from id to def types */
	private static HashMap<Integer, UploadTypes> mIdToDefType = new HashMap<>();

	static {
		for (UploadTypes defType : UploadTypes.values()) {
			mIdToDefType.put(defType.mId, defType);
		}
	}

}

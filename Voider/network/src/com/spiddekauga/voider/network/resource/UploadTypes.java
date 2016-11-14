package com.spiddekauga.voider.network.resource;

import com.spiddekauga.utils.IIdStore;

import java.util.HashMap;


/**
 * All definition types that can be uploaded
 */
@SuppressWarnings("javadoc")
public enum UploadTypes implements IIdStore {
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

/** Map from id to def types */
private static HashMap<Integer, UploadTypes> mIdToEnum = new HashMap<>();

static {
	for (UploadTypes type : UploadTypes.values()) {
		mIdToEnum.put(type.mId, type);
	}
}

/** Id for saving in datastore */
private int mId;
/** Human readable name for the type */
private String mName;

/**
 * Id for saving in datastore
 * @param id the id saved in the datastore
 */
private UploadTypes(int id) {
	mId = id;
	mName = name().toLowerCase();
}

/**
 * Get the correct def type from the id
 * @param id the id to get the def type of
 * @return the correct def type for this id, null if invalid id
 */
public static UploadTypes fromId(int id) {
	return mIdToEnum.get(id);
}

@Override
public String toString() {
	return mName;
}

@Override
public int toId() {
	return mId;
}

}

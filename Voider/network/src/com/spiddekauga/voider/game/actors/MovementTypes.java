package com.spiddekauga.voider.game.actors;

import com.spiddekauga.utils.IIdStore;
import com.spiddekauga.utils.ISearchStore;

import java.util.HashMap;

/**
 * Enumeration for all enemy movement types
 */
public enum MovementTypes implements ISearchStore, IIdStore {
// NEVER EVER remove or change the order of these!
	/** Uses variable values to behave in a certain manner */
	AI(1, "AI"),
	/** Follows a path */
	PATH(2, "Path"),
	/** Stationary, cannot move */
	STATIONARY(3, "Stationary"),;

private static HashMap<Integer, MovementTypes> mIdToEnum = new HashMap<>();

static {
	for (MovementTypes type : MovementTypes.values()) {
		mIdToEnum.put(type.mId, type);
	}
}

/** Id for saving in datastore */
private int mId;
private String mDisplayName = null;

/**
 * Id for saving in datastore
 * @param id the id saved in datastore
 * @param displayName what to show when {@link #toString()} is called
 */
private MovementTypes(int id, String displayName) {
	mId = id;
	mDisplayName = displayName;
}

/**
 * Get the enum with the specified id
 * @param id id of the enum to get
 * @return enum with the specified id, null if not found
 */
public static MovementTypes fromId(int id) {
	return mIdToEnum.get(id);
}

/**
 * Get the enum with the specified id
 * @param id id of the enum to get
 * @return enum with the specified id, null if not found
 */
public static MovementTypes fromId(String id) {
	try {
		return mIdToEnum.get(Integer.valueOf(id));
	} catch (NumberFormatException e) {
		return null;
	}
}

@Override
public String toString() {
	return mDisplayName;
}

@Override
public int toId() {
	return mId;
}

/**
 * @return id as a string
 */
@Override
public String toSearchId() {
	return String.valueOf(mId);
}
}

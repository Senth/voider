package com.spiddekauga.voider.game.actors;

import java.util.HashMap;

import com.spiddekauga.voider.network.util.IIdStore;
import com.spiddekauga.voider.network.util.ISearchStore;

/**
 * Enumeration for all enemy movement types
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public enum MovementTypes implements ISearchStore, IIdStore {
	// NEVER EVER remove or change the order of these!
	/** Uses variable values to behave in a certain manner */
	AI(1, "AI"),
	/** Follows a path */
	PATH(2, "Path"),
	/** Stationary, cannot move */
	STATIONARY(3, "Stationary"),

	;
	/**
	 * Id for saving in datastore
	 * @param id the id saved in datastore
	 * @param displayName what to show when {@link #toString()} is called
	 */
	private MovementTypes(int id, String displayName) {
		mId = id;
		mDisplayName = displayName;
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


	/** Id for saving in datastore */
	private int mId;
	private String mDisplayName = null;
	private static HashMap<Integer, MovementTypes> mIdToEnum = new HashMap<>();

	static {
		for (MovementTypes type : MovementTypes.values()) {
			mIdToEnum.put(type.mId, type);
		}
	}
}

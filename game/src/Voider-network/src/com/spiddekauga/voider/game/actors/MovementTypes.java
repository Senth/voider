package com.spiddekauga.voider.game.actors;

import java.util.HashMap;

import com.spiddekauga.voider.network.util.ISearchStore;

/**
 * Enumeration for all enemy movement types
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public enum MovementTypes implements ISearchStore {
	// NEVER EVER remove or change the order of these!
	/** Uses variable values to behave in a certain manner */
	AI(1),
	/** Follows a path */
	PATH(2),
	/** Stationary, cannot move */
	STATIONARY(3),

	;
	/**
	 * Id for saving in datastore
	 * @param id the id saved in datastore
	 */
	private MovementTypes(int id) {
		mId = id;
	}

	/**
	 * @return id of the movement type
	 */
	public int getId() {
		return mId;
	}

	/**
	 * @return id as a string
	 */
	@Override
	public String getSearchId() {
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
	private static HashMap<Integer, MovementTypes> mIdToEnum = new HashMap<>();

	static {
		for (MovementTypes type : MovementTypes.values()) {
			mIdToEnum.put(type.mId, type);
		}
	}
}

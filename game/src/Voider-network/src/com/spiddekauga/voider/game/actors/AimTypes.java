package com.spiddekauga.voider.game.actors;

import java.util.HashMap;

import com.spiddekauga.voider.network.util.ISearchStore;

/**
 * Enumeration for how to aim
 */
public enum AimTypes implements ISearchStore {
	// !!!NEVER EVER remove or change order of these!!!
	/** On the player */
	ON_PLAYER(1),
	/** In front of the player */
	IN_FRONT_OF_PLAYER(2),
	/** In the moving direction */
	MOVE_DIRECTION(3),
	/** Rotates */
	ROTATE(4),
	/** In a specific direction */
	DIRECTION(5),

	;
	/**
	 * Id for saving in datastore
	 * @param id the id saved in datastore
	 */
	private AimTypes(int id) {
		mId = id;
	}

	/**
	 * @return datastore save id
	 */
	public int getId() {
		return mId;
	}

	/**
	 * @return search save id
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
	public static AimTypes fromId(int id) {
		return mIdToEnum.get(id);
	}

	/**
	 * Get the enum with the specified id
	 * @param id id of the enum to get
	 * @return enum with the specified id, null if not found
	 */
	public static AimTypes fromId(String id) {
		try {
			return mIdToEnum.get(Integer.valueOf(id));
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/** Id for saving in datastore */
	private int mId;
	private static HashMap<Integer, AimTypes> mIdToEnum = new HashMap<>();

	static {
		for (AimTypes type : AimTypes.values()) {
			mIdToEnum.put(type.mId, type);
		}
	}
}
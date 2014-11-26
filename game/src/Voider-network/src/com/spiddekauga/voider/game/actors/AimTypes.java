package com.spiddekauga.voider.game.actors;

/**
 * Enumeration for how to aim
 */
public enum AimTypes {
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
	public String getStringId() {
		return String.valueOf(mId);
	}

	private int mId;
}
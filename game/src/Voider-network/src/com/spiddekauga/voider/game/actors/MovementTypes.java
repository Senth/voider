package com.spiddekauga.voider.game.actors;

/**
 * Enumeration for all enemy movement types
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public enum MovementTypes {
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
	public String getStringId() {
		return String.valueOf(mId);
	}

	/** Id for saving in datastore */
	private int mId;
}

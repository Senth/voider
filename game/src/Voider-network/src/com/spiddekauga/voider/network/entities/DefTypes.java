package com.spiddekauga.voider.network.entities;


/**
 * All definition types that can be uploaded
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public enum DefTypes {
	/** BulletDef */
	BULLET(1),
	/** EnemyDef */
	ENEMY(2),
	/** LevelDef */
	LEVEL(3),
	/** CampaignDef */
	CAMPAIGN(4),

	// NEXT ID TO USE => 5

	;
	/**
	 * Id for saving in datastore
	 * @param id the id saved in the datastore
	 */
	private DefTypes(int id) {
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

	/** Id for saving in datastore */
	private int mId;
	/** Human readable name for the type */
	private String mName;
}

package com.spiddekauga.voider.network.entities;


/**
 * All definition types that can be uploaded
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
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

	;
	/**
	 * Id for saving in datastore
	 * @param id the id saved in datastores
	 */
	private DefTypes(int id) {
		mId = id;
	}

	/**
	 * @return id of the movement type
	 */
	public int getId() {
		return mId;
	}

	/** Id for saving in datastore */
	private int mId;
}

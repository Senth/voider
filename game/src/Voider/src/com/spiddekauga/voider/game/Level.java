package com.spiddekauga.voider.game;

import java.util.UUID;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.OrderedMap;
import com.spiddekauga.voider.resources.IUniqueId;

/**
 * A game level
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class Level implements Json.Serializable, IUniqueId {
	/**
	 * Default constructor. Creates an empty level with a unique id
	 */
	public Level() {
		mUniqueId = UUID.randomUUID();
	}

	/**
	 * Tests whether two levels are equal. This is done by the unique id
	 * @param object the object to test if it's equal
	 */
	@Override
	public boolean equals(Object object) {
		if (object == this) {
			return true;
		} else if (object == null) {
			return false;
		} else if (object instanceof Level) {
			return ((Level)object).mUniqueId.equals(mUniqueId);
		} else {
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see com.spiddekauga.voider.resources.IUniqueId#getId()
	 */
	@Override
	public UUID getId() {
		return mUniqueId;
	}



	/** Unique id for the level */
	private UUID mUniqueId = null;


	/* (non-Javadoc)
	 * @see com.badlogic.gdx.utils.Json.Serializable#write(com.badlogic.gdx.utils.Json)
	 */
	@Override
	public void write(Json json) {
		// TODO Auto-generated method stub

	}


	/* (non-Javadoc)
	 * @see com.badlogic.gdx.utils.Json.Serializable#read(com.badlogic.gdx.utils.Json, com.badlogic.gdx.utils.OrderedMap)
	 */
	@Override
	public void read(Json json, OrderedMap<String, Object> jsonData) {
		// TODO Auto-generated method stub

	}
}

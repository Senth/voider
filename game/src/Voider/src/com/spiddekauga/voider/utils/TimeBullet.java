package com.spiddekauga.voider.utils;

import com.badlogic.gdx.utils.OrderedMap;
import com.spiddekauga.utils.Json;
import com.spiddekauga.voider.game.actors.BulletActor;

/**
 * Container class for bullet and time
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class TimeBullet implements Json.Serializable {

	@Override
	public void write(Json json) {
		json.writeValue(bulletActor);
		// Don't save the time
	}

	@Override
	public void read(Json json, OrderedMap<String, Object> jsonData) {
		bulletActor = json.readValue(BulletActor.class, jsonData);
	}

	/** The bound bullet actor to this time */
	public BulletActor bulletActor = null;
	/** Time bound to the bullet actor */
	public float time = 0;
}

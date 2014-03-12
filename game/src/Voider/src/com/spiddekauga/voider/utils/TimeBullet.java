package com.spiddekauga.voider.utils;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.spiddekauga.voider.game.actors.BulletActor;

/**
 * Container class for bullet and time
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class TimeBullet implements Json.Serializable {

	@Override
	public void write(Json json) {
		json.writeValue("bulletActor", bulletActor);
		// Don't save the time
	}

	@Override
	public void read(Json json, JsonValue jsonValue) {
		bulletActor = json.readValue("bulletActor", BulletActor.class, jsonValue);
	}

	/** The bound bullet actor to this time */
	@Tag(86) public BulletActor bulletActor = null;
	/** Time bound to the bullet actor */
	public float time = 0;
}

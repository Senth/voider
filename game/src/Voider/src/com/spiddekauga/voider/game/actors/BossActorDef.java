package com.spiddekauga.voider.game.actors;

import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.utils.OrderedMap;
import com.spiddekauga.utils.Json;
import com.spiddekauga.voider.Config;

/**
 * Boss actor definition, does nothing more than specify that the actor
 * is a boss.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class BossActorDef extends ActorDef {
	/**
	 * Constructor that sets all variables
	 * @param maxLife maximum life of the actor, also starting amount of life
	 * @param name name of the actor
	 * @param fixtureDef physical representation of the object
	 * @TODO just a stub for now
	 */
	public BossActorDef(
			float maxLife,
			String name,
			FixtureDef fixtureDef
			)
	{
		super(maxLife, name, fixtureDef);
	}

	/**
	 * Empty constructor that does nothing. Used for JSON.
	 */
	protected BossActorDef() {
		super(ActorTypes.BOSS);
	}

	@Override
	protected FixtureDef getDefaultFixtureDef() {
		return new FixtureDef();
	}

	@Override
	public void write(Json json) {
		json.writeObjectStart("ActorDef");
		super.write(json);
		json.writeObjectEnd();

		json.writeValue("REVISION", Config.REVISION);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void read(Json json, OrderedMap<String, Object> jsonData) {
		OrderedMap<String, Object> actorMap = json.readValue("ActorDef", OrderedMap.class, jsonData);
		super.read(json, actorMap);
	}
}

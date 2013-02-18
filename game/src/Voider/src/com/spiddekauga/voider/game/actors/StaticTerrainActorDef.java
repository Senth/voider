package com.spiddekauga.voider.game.actors;

import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.utils.OrderedMap;
import com.spiddekauga.utils.Json;
import com.spiddekauga.voider.Config;

/**
 * Static terrain actor definition, does nothing more than specify that
 * the actor is a static terrain.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class StaticTerrainActorDef extends ActorDef {
	/**
	 * Constructor that sets all variables
	 */
	public StaticTerrainActorDef()
	{
		super(ActorTypes.STATIC_TERRAIN);
		setMaxLife(-1);
		addFixtureDef(new FixtureDef());
		setCollisionDamage(20);
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

package com.spiddekauga.voider.game.actors;

import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.utils.OrderedMap;
import com.spiddekauga.utils.Json;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.Config.Editor.Bullet;

/**
 * Bullet actor definition, does nothing more than specify that
 * the actor is a bullet
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class BulletActorDef extends ActorDef {
	/**
	 * Default constructor
	 */
	public BulletActorDef() {
		super(ActorTypes.BULLET);

		getBodyDef().type = BodyType.KinematicBody;

		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.friction = 0.0f;
		fixtureDef.restitution = 0.1f;
		fixtureDef.density = 0.001f;
		addFixtureDef(fixtureDef);

		setShapeType(Bullet.Visual.SHAPE_DEFAULT);
	}

	@Override
	public void write(Json json) {
		json.writeValue("REVISION", Config.REVISION);

		json.writeObjectStart("ActorDef");
		super.write(json);
		json.writeObjectEnd();
	}

	@Override
	public void read(Json json, OrderedMap<String, Object> jsonData) {
		@SuppressWarnings("unchecked")
		OrderedMap<String, Object> superMap = json.readValue("ActorDef", OrderedMap.class, jsonData);
		super.read(json, superMap);
	}
}

package com.spiddekauga.voider.game.actors;

import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.utils.OrderedMap;
import com.spiddekauga.utils.Json;
import com.spiddekauga.voider.Config;

/**
 * Player actor definition, does nothing more than specify
 * that the actor is a player
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class PlayerActorDef extends ActorDef {
	/**
	 * Default constructor
	 */
	public PlayerActorDef() {
		super(ActorTypes.PLAYER);
		setName("Default");
		setMaxLife(100f);
		getBodyDef().type = BodyType.DynamicBody;
		getBodyDef().fixedRotation = true;

		setShapeType(ActorShapeTypes.CIRCLE);
	}

	@Override
	protected FixtureDef getDefaultFixtureDef() {
		FixtureDef fixtureDef = new FixtureDef();
		CircleShape circleShape = new CircleShape();
		circleShape.setRadius(1.0f);
		fixtureDef.friction = 0.0f;
		fixtureDef.restitution = 0.1f;
		fixtureDef.density = 0.001f;
		fixtureDef.shape = circleShape;
		return fixtureDef;
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

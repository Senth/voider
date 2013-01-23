package com.spiddekauga.voider.game.actors;

import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.spiddekauga.voider.game.ActorDef;

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
		setName("Default");
		setMaxLife(100f);
		getBodyDef().type = BodyType.DynamicBody;
		getBodyDef().fixedRotation = true;
		FixtureDef fixtureDef = new FixtureDef();
		CircleShape circleShape = new CircleShape();
		circleShape.setRadius(1.0f);
		fixtureDef.friction = 0.0f;
		fixtureDef.restitution = 0.1f;
		fixtureDef.density = 0.001f;
		fixtureDef.shape = circleShape;
		addFixtureDef(fixtureDef);
	}

	/**
	 * @return player filter category
	 */
	@Override
	protected short getFilterCategory() {
		return FixtureFilterCategories.PLAYER;
	}

	/**
	 * Can collide with everything except player and player bullets
	 * @return colliding categories
	 */
	@Override
	protected short getFilterCollidingCategories() {
		return (short) (FixtureFilterCategories.ENEMY | FixtureFilterCategories.PICKUP | FixtureFilterCategories.STATIC_TERRAIN | FixtureFilterCategories.SCREEN_BORDER);
	}
}

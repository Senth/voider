package com.spiddekauga.voider.game.actors;

import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
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
	 * Constructor that sets all variables
	 * @param maxLife maximum life of the actor, also starting amount of life
	 * @param name name of the actor
	 * @param fixtureDef physical representation of the object
	 */
	public PlayerActorDef(
			float maxLife,
			String name,
			FixtureDef fixtureDef
			)
	{
		super(maxLife, name, fixtureDef);
		getBodyDef().type = BodyType.DynamicBody;
		getBodyDef().fixedRotation = true;
	}

	/**
	 * Protected default constructor used for JSON
	 */
	protected PlayerActorDef() {

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

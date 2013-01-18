package com.spiddekauga.voider.game.actors;

import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.spiddekauga.voider.game.ActorDef;

/**
 * Pickup actor definition, does nothing more than specify that the actor
 * is a pickup object
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class PickupActorDef extends ActorDef {
	/**
	 * Constructor that sets all variables
	 * @param maxLife maximum life of the actor, also starting amount of life
	 * @param name name of the actor
	 * @param fixtureDef physical representation of the object
	 */
	public PickupActorDef(
			float maxLife,
			String name,
			FixtureDef fixtureDef
			)
	{
		super(maxLife, name, fixtureDef);
	}

	/**
	 * Private default constructor, used for JSON.
	 */
	@SuppressWarnings("unused")
	private PickupActorDef() {

	}

	/**
	 * @return pickup filter category
	 */
	@Override
	protected short getFilterCategory() {
		return FixtureFilterCategories.PICKUP;
	}

	/**
	 * Can collide only with other players
	 * @return colliding categories
	 */
	@Override
	protected short getFilterCollidingCategories() {
		return FixtureFilterCategories.PLAYER;
	}
}

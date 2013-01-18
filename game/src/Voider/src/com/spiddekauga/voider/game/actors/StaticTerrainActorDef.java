package com.spiddekauga.voider.game.actors;

import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.spiddekauga.voider.game.ActorDef;

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
		super(-1, "StaticTerrain", new FixtureDef());
		setCollisionDamage(20);
	}

	/**
	 * @return static terrain filter category
	 */
	@Override
	protected short getFilterCategory() {
		return FixtureFilterCategories.STATIC_TERRAIN;
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

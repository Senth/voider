package com.spiddekauga.voider.game.actors;

import com.badlogic.gdx.physics.box2d.FixtureDef;

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
}

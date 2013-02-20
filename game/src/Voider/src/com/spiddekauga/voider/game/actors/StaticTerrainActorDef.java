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
		super(ActorTypes.STATIC_TERRAIN);
		setMaxLife(-1);
		addFixtureDef(new FixtureDef());
		setCollisionDamage(20);
	}

	@Override
	protected FixtureDef getDefaultFixtureDef() {
		return new FixtureDef();
	}
}

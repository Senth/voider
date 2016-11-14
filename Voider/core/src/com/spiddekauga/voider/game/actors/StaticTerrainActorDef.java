package com.spiddekauga.voider.game.actors;


/**
 * Static terrain actor definition, does nothing more than specify that the actor is a static
 * terrain.
 */
public class StaticTerrainActorDef extends ActorDef {
/**
 * Constructor that sets all variables
 */
public StaticTerrainActorDef() {
	super(ActorTypes.STATIC_TERRAIN);
	setHealthMax(-1);
	setCollisionDamage(20);
}
}

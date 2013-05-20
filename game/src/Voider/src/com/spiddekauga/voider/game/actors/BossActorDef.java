package com.spiddekauga.voider.game.actors;


/**
 * Boss actor definition, does nothing more than specify that the actor
 * is a boss.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class BossActorDef extends ActorDef {
	/**
	 * Empty constructor that does nothing. Used for JSON.
	 */
	public BossActorDef() {
		super(ActorTypes.BOSS);
	}
}

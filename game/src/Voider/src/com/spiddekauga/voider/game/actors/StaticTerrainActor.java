package com.spiddekauga.voider.game.actors;


/**
 * Static terrain actor. This terrain will not move, and cannot be destroyed.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class StaticTerrainActor extends Actor {

	/**
	 * Default constructor, creates a new definition for the actor
	 */
	public StaticTerrainActor() {
		super(new StaticTerrainActorDef());
	}

	@Override
	public boolean savesDef() {
		return true;
	}

	/**
	 * @return static terrain filter category
	 */
	@Override
	protected short getFilterCategory() {
		return ActorFilterCategories.STATIC_TERRAIN;
	}

	/**
	 * Can collide only with other players
	 * @return colliding categories
	 */
	@Override
	protected short getFilterCollidingCategories() {
		return ActorFilterCategories.PLAYER;
	}
}

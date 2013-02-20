package com.spiddekauga.voider.game.actors;



/**
 * Pickup actors contains a collectible that will be transfered to the player
 * once the player collides with this pickup.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class PickupActor extends Actor {
	/**
	 * @return pickup filter category
	 */
	@Override
	protected short getFilterCategory() {
		return ActorFilterCategories.PICKUP;
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

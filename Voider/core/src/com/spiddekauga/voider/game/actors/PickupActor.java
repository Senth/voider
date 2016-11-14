package com.spiddekauga.voider.game.actors;

import com.spiddekauga.voider.Config.Graphics.RenderOrders;


/**
 * Pickup actors contains a collectible that will be transfered to the player once the player
 * collides with this pickup.
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

@Override
public RenderOrders getRenderOrder() {
	return RenderOrders.PICKUP;
}
}

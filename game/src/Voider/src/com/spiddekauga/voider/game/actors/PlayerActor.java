package com.spiddekauga.voider.game.actors;

import com.spiddekauga.voider.game.Collectibles;




/**
 * The ship the player controls
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class PlayerActor extends com.spiddekauga.voider.game.actors.Actor {
	/**
	 * Creates a default player
	 */
	public PlayerActor() {
		this(new PlayerActorDef());
	}

	/**
	 * Creates a player from the specified actor definition
	 * @param playerActorDef player actor definition to create the player from
	 */
	public PlayerActor(PlayerActorDef playerActorDef) {
		super(playerActorDef);
		setDef(playerActorDef);
	}

	/**
	 * Adds a collectible to the player
	 * @param collectible the collectible to add to the player
	 */
	public void addCollectible(Collectibles collectible) {
		switch (collectible) {
		case HEALTH_25:
			mLife += 25;
			break;

		case HEALTH_50:
			mLife += 50;
			break;
		}
	}

	/**
	 * @return player filter category
	 */
	@Override
	protected short getFilterCategory() {
		return ActorFilterCategories.PLAYER;
	}

	/**
	 * Can collide with everything except player and player bullets
	 * @return colliding categories
	 */
	@Override
	protected short getFilterCollidingCategories() {
		return (short) (ActorFilterCategories.ENEMY | ActorFilterCategories.PICKUP | ActorFilterCategories.STATIC_TERRAIN | ActorFilterCategories.SCREEN_BORDER);
	}
}

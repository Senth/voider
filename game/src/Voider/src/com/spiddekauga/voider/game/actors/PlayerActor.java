package com.spiddekauga.voider.game.actors;

import com.spiddekauga.voider.game.Collectibles;




/**
 * The ship the player controls
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class PlayerActor extends com.spiddekauga.voider.game.Actor {
	/**
	 * Creates a default player
	 */
	public PlayerActor() {
		super(new PlayerActorDef());
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
}

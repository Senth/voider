package com.spiddekauga.voider.game.actors;

import com.spiddekauga.voider.game.Collectibles;
import com.spiddekauga.voider.game.TriggerAction;




/**
 * The ship the player controls
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class PlayerActor extends com.spiddekauga.voider.game.Actor {
	/**
	 * Player constructor
	 * @param playerDef the player definition
	 */
	public PlayerActor(PlayerActorDef playerDef) {
		super(playerDef);
	}

	@Override
	public void onTriggered(TriggerAction action) {
		// TODO Auto-generated method stub
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
	 * protected constructor, used for JSON
	 */
	protected PlayerActor() {
		// Does nothing
	}
}

package com.spiddekauga.voider.game.actors;

import com.spiddekauga.voider.game.ActorDef;

/**
 * The ship the player controls
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class PlayerActor extends com.spiddekauga.voider.game.Actor {
	/**
	 * Player constructor
	 */
	public PlayerActor() {
		super(new ActorDef(100.0f, ActorTypes.PLAYER, null, "Player", null));
	}

	/* (non-Javadoc)
	 * @see com.spiddekauga.voider.game.ITriggerListener#onTriggered(java.lang.String)
	 */
	@Override
	public void onTriggered(String action) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.spiddekauga.voider.game.Actor#update(float)
	 */
	@Override
	public void update(float deltaTime) {
		// TODO Auto-generated method stub

	}


}

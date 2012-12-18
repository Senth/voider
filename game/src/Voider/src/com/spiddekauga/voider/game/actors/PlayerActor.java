package com.spiddekauga.voider.game.actors;



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

	/**
	 * Private constructor, used for JSON
	 */
	@SuppressWarnings("unused")
	private PlayerActor() {
		// Does notihng
	}
}

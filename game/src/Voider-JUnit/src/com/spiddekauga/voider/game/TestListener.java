package com.spiddekauga.voider.game;

import java.util.UUID;

import com.spiddekauga.voider.resources.Def;

/**
 * Just a test listener
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class TestListener extends Def implements ITriggerListener {

	/**
	 * Default constructor
	 */
	TestListener() {
		mUniqueId = UUID.randomUUID();
	}

	/* (non-Javadoc)
	 * @see com.spiddekauga.voider.game.ITriggerListener#onTriggered(java.lang.String)
	 */
	@Override
	public void onTriggered(TriggerAction action) {
		// TODO Auto-generated method stub

	}

}

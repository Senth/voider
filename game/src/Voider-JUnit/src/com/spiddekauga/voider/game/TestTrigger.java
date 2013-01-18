package com.spiddekauga.voider.game;

import com.spiddekauga.voider.game.TriggerAction.Reasons;

/**
 * Test trigger that essentially does nothing
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class TestTrigger extends Trigger {
	@Override
	protected boolean isTriggered() {
		return true;
	}

	@Override
	protected Reasons getReason() {
		return null;
	}

	@Override
	protected Object getCauseObject() {
		// TODO Auto-generated method stub
		return null;
	}
}

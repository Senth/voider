package com.spiddekauga.voider.game;

/**
 * Test trigger that essentially does nothing
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class TestTrigger extends Trigger {

	/* (non-Javadoc)
	 * @see com.spiddekauga.voider.game.Trigger#isTriggered()
	 */
	@Override
	protected boolean isTriggered() {
		return true;
	}


}

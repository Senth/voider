package com.spiddekauga.voider.prototype.entities;

import java.util.ArrayList;

/**
 * Response from highscore get method
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class HighscoreGetMethodResponse implements IEntity {
	/** If the call was successful */
	public boolean success = false;
	/**
	 * All highscores for the level that was specified, ordered from highscore to lowest
	 * score
	 */
	public ArrayList<HighscoreEntity> highscores = new ArrayList<>();
}

package com.spiddekauga.voider.prototype.entities;

/**
 * Contains highscore information
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class HighscoreEntity implements IEntity {
	/** Player name */
	public String playerName = null;
	/** Score */
	public int score;
}

package com.spiddekauga.voider.network.stat;

import com.spiddekauga.voider.network.entities.IEntity;

/**
 * Contains highscore information
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class HighscoreEntity implements IEntity {
	private static final long serialVersionUID = 1L;
	/** Player name */
	public String playerName = null;
	/** Score */
	public int score;
}

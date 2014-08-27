package com.spiddekauga.voider.prototype.entities;

import java.util.UUID;

/**
 * Method for getting highscores
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class HighscoreGetMethod implements IMethodEntity {
	/** Id of the level to get highscores for */
	public UUID levelId = null;
	/** Fetch all users in one batch */
	public boolean oneBatch;

	@Override
	public String getMethodName() {
		return "highscores-get";
	}
}

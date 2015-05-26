package com.spiddekauga.voider.network.stat;

import java.util.Date;
import java.util.UUID;

import com.spiddekauga.voider.network.entities.IEntity;

/**
 * Highscore of a player
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class HighscoreSyncEntity implements IEntity {
	private static final long serialVersionUID = 1L;
	/** Level id */
	public UUID levelId;
	/** Score */
	public int score;
	/** Created date */
	public Date created = null;
}

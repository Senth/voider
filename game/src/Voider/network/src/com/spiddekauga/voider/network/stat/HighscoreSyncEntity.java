package com.spiddekauga.voider.network.stat;

import java.util.Date;
import java.util.UUID;

import com.spiddekauga.voider.network.entities.IEntity;

/**
 * Highscore of a player
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class HighscoreSyncEntity implements IEntity {
	/** Level id */
	public UUID levelId;
	/** Score */
	public int score;
	/** Created date */
	public Date created = null;
}

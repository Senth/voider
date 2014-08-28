package com.spiddekauga.voider.network.entities.method;

import java.util.ArrayList;
import java.util.Date;

import com.spiddekauga.voider.network.entities.HighscoreSyncEntity;

/**
 * Method for syncronizing highscore
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class SyncHighscoreMethod implements IMethodEntity {
	/** Highscores to syncronize */
	public ArrayList<HighscoreSyncEntity> highscores = new ArrayList<>();
	/** Last sync time */
	public Date lastSync;

	@Override
	public String getMethodName() {
		return "sync-highscore";
	}
}

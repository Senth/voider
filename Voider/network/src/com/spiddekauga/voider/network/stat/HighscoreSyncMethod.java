package com.spiddekauga.voider.network.stat;

import java.util.ArrayList;
import java.util.Date;

import com.spiddekauga.voider.network.entities.IMethodEntity;

/**
 * Method for syncronizing highscore

 */
public class HighscoreSyncMethod implements IMethodEntity {
	/** Highscores to syncronize */
	public ArrayList<HighscoreSyncEntity> highscores = new ArrayList<>();
	/** Last sync time */
	public Date lastSync;

	@Override
	public MethodNames getMethodName() {
		return MethodNames.HIGHSCORE_SYNC;
	}
}

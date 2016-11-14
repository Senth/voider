package com.spiddekauga.voider.network.stat;

import java.util.ArrayList;
import java.util.Date;

import com.spiddekauga.voider.network.entities.GeneralResponseStatuses;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.ISuccessStatuses;

/**
 * Response from synchronizing highscores

 */
public class HighscoreSyncResponse implements IEntity, ISuccessStatuses {
	/** Upload status */
	public GeneralResponseStatuses status = null;
	/** Highscores to update/set */
	public ArrayList<HighscoreSyncEntity> highscores = new ArrayList<>();
	/** Latest sync time (to set) */
	public Date syncTime;


	@Override
	public boolean isSuccessful() {
		return status != null && status.isSuccessful();
	}
}

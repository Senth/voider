package com.spiddekauga.voider.network.entities.stat;

import java.util.ArrayList;
import java.util.Date;

import com.spiddekauga.voider.network.entities.GeneralResponseStatuses;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.ISuccessStatuses;

/**
 * Response from syncronizing highscores
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class HighscoreSyncMethodResponse implements IEntity, ISuccessStatuses {
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

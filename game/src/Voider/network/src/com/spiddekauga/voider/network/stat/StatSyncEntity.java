package com.spiddekauga.voider.network.stat;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import com.spiddekauga.voider.network.entities.IEntity;

/**
 * All statistics that should be synced
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class StatSyncEntity implements IEntity {
	/** Level/Campaign stats to sync */
	public ArrayList<LevelStat> levelStats = new ArrayList<>();
	/** Last sync date */
	public Date syncDate = null;

	/**
	 * Level/Campaign stats
	 */
	public static class LevelStat implements IEntity {
		/** Level/Campaign id */
		public UUID id = null;
		/** If the level/campaign is bookmarked */
		public boolean bookmark = false;
		/** Play count (incl. plays to sync) */
		public int cPlayed = 0;
		/** Play count to sync (only from client) */
		public int cPlaysToSync = 0;
		/** Clear count (incl. clears to sync) */
		public int cCleared = 0;
		/** Clear count to sync (only from client) */
		public int cClearsToSync = 0;
		/** Total death count (incl. deaths to sync) */
		public int cDeaths = 0;
		/** Death count to sync (only from client) */
		public int cDeathsToSync = 0;
		/** Rating rating of the level/campaign */
		public int rating = 0;
		/** User comment */
		public String comment = "";
		/** Last played date */
		public Date lastPlayed = null;
		/** Last updated (only from server) */
		public Date updated = null;
		/** Tags */
		public ArrayList<Tags> tags = new ArrayList<>();
	}
}

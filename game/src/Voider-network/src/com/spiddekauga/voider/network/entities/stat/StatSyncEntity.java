package com.spiddekauga.voider.network.entities.stat;

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
	public ArrayList<LevelStats> levelStats = new ArrayList<>();
	/** Last sync date */
	public Date syncDate = null;

	/**
	 * Level/Campaign stats
	 */
	public static class LevelStats {
		/** Level/Campaign id */
		public UUID id;
		/** If the level/campaign is bookmarked */
		public boolean bookmark;
		/** Play count (incl. plays to sync) */
		public int cPlayed;
		/** Play count to sync (only from client) */
		public int cPlaysToSync = 0;
		/** Clear count (incl. clears to sync) */
		public int cCleared;
		/** Clear count to sync (only from client) */
		public int cClearsToSync = 0;
		/** Rating rating of the level/campaign */
		public int rating;
		/** Last played date */
		public Date lastPlayed;
		/** Last updated (only from server) */
		public Date updated = null;
		/** Tags */
		public ArrayList<Tags> tags = new ArrayList<>();
	}
}

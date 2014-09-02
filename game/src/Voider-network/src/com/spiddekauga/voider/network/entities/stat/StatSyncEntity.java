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
		/** Play count to sync */
		public int cPlaysToSync;
		/** Clear count (incl. clears to sync) */
		public int cCleared;
		/** Clear count to sync */
		public int cClearsToSync;
		/** Rating rating of the level/campaign */
		public int rating;
		/** Last played date */
		public Date lastPlayed;
		/** Tags */
		public ArrayList<Tags> tags = new ArrayList<>();
	}
}

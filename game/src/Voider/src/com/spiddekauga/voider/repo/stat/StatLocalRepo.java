package com.spiddekauga.voider.repo.stat;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import com.spiddekauga.voider.network.entities.stat.StatSyncEntity;
import com.spiddekauga.voider.network.entities.stat.Tags;

/**
 * Local repository for stats
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class StatLocalRepo {
	/**
	 * Private constructor to enforce singleton pattern
	 */
	private StatLocalRepo() {
		// TODO auto-generated method
	}

	/**
	 * @return instance of this class
	 */
	public static StatLocalRepo getInstance() {
		if (mInstance == null) {
			mInstance = new StatLocalRepo();
		}
		return mInstance;
	}

	/**
	 * Increases the play count of a level, automatically updates last played date. If no
	 * statistics exist for the level it will be created.
	 * @param id level/campaign id
	 * @param cleared true if the level/campaign was cleared
	 */
	public void increasePlayCount(UUID id, boolean cleared) {
		mSqliteGateway.increasePlayCount(id, cleared);
	}

	/**
	 * Sets the rating of a level/campaign.
	 * @param id level/campaign id
	 * @param rating in range [0,5]
	 */
	public void setRating(UUID id, int rating) {
		mSqliteGateway.setRating(id, rating);
	}

	/**
	 * Set the level/ćampaign as bookmarked
	 * @param id level/campaign id
	 * @param bookmark true if bookmarked
	 */
	public void setBookmark(UUID id, boolean bookmark) {
		mSqliteGateway.setBookmark(id, bookmark);
	}

	/**
	 * Sets when the level/ćampaign was last played
	 * @param id level/ćampaign id
	 * @param date last played date
	 */
	public void setLastPlayed(UUID id, Date date) {
		mSqliteGateway.setLastPlayed(id, date);
	}

	/**
	 * Set tags for the level/campaign
	 * @param id level/campaign id
	 * @param tags all tags
	 */
	public void setTags(UUID id, ArrayList<Tags> tags) {
		mSqliteGateway.setTags(id, tags);
	}

	/**
	 * Get level/campaign statistics
	 * @param id level/campaign id
	 * @return statistics for the level, null if no statistics exists
	 */
	public UserLevelStat getLevelStats(UUID id) {
		return mSqliteGateway.getLevelStats(id);
	}

	/**
	 * @return all statistics that should be synced to the server
	 */
	public StatSyncEntity getUnsynced() {
		StatSyncEntity syncEntity = mSqliteGateway.getUnsynced();
		syncEntity.syncDate = mPrefsGateway.getSyncDate();

		return syncEntity;
	}

	/** Preferences gateway */
	private StatPrefsGateway mPrefsGateway = new StatPrefsGateway();
	/** Sqlite gateway */
	private StatSqliteGateway mSqliteGateway = new StatSqliteGateway();

	/** Instance of this class */
	private static StatLocalRepo mInstance = null;
}

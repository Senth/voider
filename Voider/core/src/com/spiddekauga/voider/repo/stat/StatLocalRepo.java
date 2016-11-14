package com.spiddekauga.voider.repo.stat;

import com.spiddekauga.voider.network.stat.StatSyncEntity;
import com.spiddekauga.voider.network.stat.Tags;

import java.util.Date;
import java.util.UUID;

/**
 * Local repository for stats
 */
public class StatLocalRepo {
/** Instance of this class */
private static StatLocalRepo mInstance = null;
/** Preferences gateway */
private StatPrefsGateway mPrefsGateway = new StatPrefsGateway();
/** Sqlite gateway */
private StatSqliteGateway mSqliteGateway = new StatSqliteGateway();

/**
 * Private constructor to enforce singleton pattern
 */
private StatLocalRepo() {
	// Does nothing
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
 * Increases the play count of a level, automatically updates last played date. If no statistics
 * exist for the level it will be created.
 * @param id level/campaign id
 */
public void increasePlayCount(UUID id) {
	mSqliteGateway.increasePlayCount(id);
}

/**
 * Increase clear count
 * @param id level id
 */
public void increaseClearCount(UUID id) {
	mSqliteGateway.increaseClearCount(id);
}

/**
 * Increase the death count on a specific level
 * @param id level id
 */
public void increaseDeathCount(UUID id) {
	mSqliteGateway.increaseDeathCount(id);
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
 * Set the level/campaign as bookmarked
 * @param id level/campaign id
 * @param bookmark true if bookmarked
 */
public void setBookmark(UUID id, boolean bookmark) {
	mSqliteGateway.setBookmark(id, bookmark);
}

/**
 * Set a comment for the level/campaign
 * @param id level/campaign id
 * @param comment
 */
public void setComment(UUID id, String comment) {
	mSqliteGateway.setComment(id, comment);
}

/**
 * Sets when the level/campaign was last played to current time
 * @param id level/campaign id
 */
public void updateLastPlayed(UUID id) {
	mSqliteGateway.setLastPlayed(id, new Date());
}

/**
 * Add tag for the level/campaign
 * @param id level/campaign id
 * @param tag the tag to add
 */
public void addTag(UUID id, Tags tag) {
	if (isTaggable(id)) {
		mSqliteGateway.addTag(id, tag);
	}
}

/**
 * Checks if a level/campaign is available for tagging.
 * @param id level/campaign id
 * @return true if the level/campaign is available for tagging
 */
public boolean isTaggable(UUID id) {
	return mSqliteGateway.isTaggable(id);
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

/**
 * Set all stats as synced
 * @param syncDate the new sync date
 */
void setAsSynced(Date syncDate) {
	mSqliteGateway.setAsSynced();
	mPrefsGateway.setSyncDate(syncDate);
}
}

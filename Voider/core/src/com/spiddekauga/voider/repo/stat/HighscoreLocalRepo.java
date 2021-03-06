package com.spiddekauga.voider.repo.stat;

import com.spiddekauga.voider.network.stat.HighscoreSyncEntity;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

/**
 * Local repository of highscores
 */
class HighscoreLocalRepo {
/** Instance of this class */
private static HighscoreLocalRepo mInstance = null;
/** Sqlite gateway */
private HighscoreSqliteGateway mSqliteGateway = new HighscoreSqliteGateway();
/** Preferences gateway */
private HighscorePrefsGateway mPrefsGateway = new HighscorePrefsGateway();

/**
 * Private constructor to enforce singleton pattern
 */
private HighscoreLocalRepo() {
	// Does nothing
}

/**
 * @return instance of this class
 */
public static HighscoreLocalRepo getInstance() {
	if (mInstance == null) {
		mInstance = new HighscoreLocalRepo();
	}
	return mInstance;
}

/**
 * Sets a new highscore (as unsynced)
 * @param levelId id of the level
 * @param score new highscore
 */
void setHighscore(UUID levelId, int score) {
	mSqliteGateway.set(levelId, score, new Date(), false);
}

/**
 * Get highscore from the specified level
 * @param levelId id of the level to get highscore from
 * @return highscore of the specified level id, null if no highscore exists for this level
 */
HighscoreSyncEntity getHighscore(UUID levelId) {
	return mSqliteGateway.get(levelId);
}

/**
 * Sets a new highscore as synced
 * @param highscores all the highscore to set as synced
 */
void setHighscoresFromServer(ArrayList<HighscoreSyncEntity> highscores) {
	for (HighscoreSyncEntity highscore : highscores) {
		setHighscoreAndSynced(highscore);
	}
}

/**
 * Sets a new highscore as synced
 * @param highscore the highscore to set as synced
 */
void setHighscoreAndSynced(HighscoreSyncEntity highscore) {
	mSqliteGateway.set(highscore.levelId, highscore.score, highscore.created, true);
}

/**
 * Sets all specified highscores as synced
 * @param highscores all highscores to set as synced
 */
void setSynced(ArrayList<HighscoreSyncEntity> highscores) {
	for (HighscoreSyncEntity highscore : highscores) {
		setSynced(highscore.levelId);
	}
}

/**
 * Sets a highscore as synced
 * @param levelId set the highscore as synced
 */
void setSynced(UUID levelId) {
	mSqliteGateway.setSynced(levelId);
}

/**
 * Checks if the new score is a highscore
 * @param levelId id of the level
 * @param score test if this score is higher than the current highscore
 * @return true if it's a new highscore or no highscore existst for this level; false otherwise.
 */
boolean isNewHighscore(UUID levelId, int score) {
	return mSqliteGateway.isNewHighscore(levelId, score);
}

/**
 * @return all unsynced highscores
 */
ArrayList<HighscoreSyncEntity> getUnsynced() {
	return mSqliteGateway.getUnsynced();
}

/**
 * @return last sync date of highscores
 */
Date getSyncDate() {
	return mPrefsGateway.getSyncDate();
}

/**
 * Update the last sync date
 * @param lastSync date when synced highscores the last time
 */
void setSyncDate(Date lastSync) {
	mPrefsGateway.setSyncDate(lastSync);
}
}

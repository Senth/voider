package com.spiddekauga.voider.repo.stat;

import com.badlogic.gdx.sql.DatabaseCursor;
import com.spiddekauga.voider.network.stat.HighscoreSyncEntity;
import com.spiddekauga.voider.repo.SqliteGateway;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

/**
 * Sqlite gateway for highscores
 */
class HighscoreSqliteGateway extends SqliteGateway {
/**
 * Set or create a new highscore.
 * @param levelId id of the level
 * @param score player score
 * @param date when the score was made
 * @param synced if the score has been synced or not
 */
void set(UUID levelId, int score, Date date, boolean synced) {
	String syncedString = synced ? "1" : "0";

	// Update existing highscore
	if (exists(levelId)) {
		execSQL("UPDATE highscore SET score=" + score + ", date=" + dateToString(date) + ", synced=" + syncedString + " WHERE level_id='"
				+ levelId + "';");
	}
	// Create new highscore
	else {
		execSQL("INSERT INTO highscore VALUES ('" + levelId + "',  " + score + ", " + dateToString(date) + ", " + syncedString + ");");
	}
}

/**
 * @param levelId test if a highscore exist for this level
 * @return true if a highscore exists for the specified level
 */
private boolean exists(UUID levelId) {
	DatabaseCursor cursor = rawQuery("SELECT NULL FROM highscore WHERE level_id='" + levelId + "';");

	boolean exists = cursor.next();
	cursor.close();
	return exists;
}

/**
 * Get existing highscore
 * @param levelId id of the level
 * @return player highscore of this level, null if no highscore exists for this level
 */
HighscoreSyncEntity get(UUID levelId) {
	DatabaseCursor cursor = rawQuery("SELECT score, date FROM highscore WHERE level_id='" + levelId + "';");

	HighscoreSyncEntity highscore = null;

	if (cursor.next()) {
		highscore = new HighscoreSyncEntity();
		highscore.levelId = levelId;
		highscore.score = cursor.getInt(0);
		highscore.created = new Date(cursor.getLong(1));
	}

	cursor.close();

	return highscore;
}

/**
 * @return all unsynced highscores
 */
ArrayList<HighscoreSyncEntity> getUnsynced() {
	ArrayList<HighscoreSyncEntity> highscores = new ArrayList<>();

	DatabaseCursor cursor = rawQuery("SELECT level_id, score, date FROM highscore WHERE synced=0;");

	while (cursor.next()) {
		HighscoreSyncEntity highscore = new HighscoreSyncEntity();
		highscore.levelId = UUID.fromString(cursor.getString(0));
		highscore.score = cursor.getInt(1);
		highscore.created = new Date(cursor.getLong(2));

		highscores.add(highscore);
	}

	cursor.close();

	return highscores;
}

/**
 * Checks if it's a new highscore
 * @param levelId id of the level
 * @param score new score to check if it's higher than the current score
 * @return true if it's a new highscore or no highscore exists for this level; false otherwise.
 */
boolean isNewHighscore(UUID levelId, int score) {
	DatabaseCursor cursor = rawQuery("SELECT score FROM highscore WHERE level_id='" + levelId + "';");

	boolean newHighscore = true;

	if (cursor.next()) {
		int currentHighscore = cursor.getInt(0);
		newHighscore = score > currentHighscore;
	}

	cursor.close();

	return newHighscore;
}

/**
 * Set highscore as synced
 * @param levelId id of the level to set as synced
 */
void setSynced(UUID levelId) {
	execSQL("UPDATE highscore SET synced=1 WHERE level_id='" + levelId + "';");
}
}

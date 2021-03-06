package com.spiddekauga.voider.repo.stat;

import com.badlogic.gdx.sql.DatabaseCursor;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.network.stat.StatSyncEntity;
import com.spiddekauga.voider.network.stat.StatSyncEntity.LevelStat;
import com.spiddekauga.voider.network.stat.Tags;
import com.spiddekauga.voider.repo.SqliteGateway;

import java.util.Date;
import java.util.UUID;

/**
 * Database gateway for statistics
 */
class StatSqliteGateway extends SqliteGateway {
/**
 * Increases the play count of a level. If no statistics exist for the level it will
 * be created.
 * @param id level/campaign id
 */
void increasePlayCount(UUID id) {
	String sql = "UPDATE level_stat SET synced=0, play_count=play_count+1, plays_to_sync=plays_to_sync+1 WHERE uuid='" + id + "';";
	execSQL(sql);
}

/**
 * Increase clear count
 * @param id level id
 */
void increaseClearCount(UUID id) {
	String sql = "UPDATE level_stat SET synced=0, clear_count=clear_count+1, clears_to_sync=clears_to_sync+1 WHERE uuid='" + id + "';";
	execSQL(sql);
}

/**
 * Increase the death count on the specific level
 * @param id level id
 */
void increaseDeathCount(UUID id) {
	String sql = "UPDATE level_stat SET synced=0, death_count=death_count+1, deaths_to_sync=deaths_to_sync+1 WHERE uuid='" + id + "';";
	execSQL(sql);
}

/**
 * Sets the rating of a level/campaign.
 * @param id level/campaign id
 * @param rating in range [0,5]
 */
void setRating(UUID id, int rating) {
	update(id, "rating", rating);
}

/**
 * Update a specific thing
 * @param id level/campaign id
 * @param column the column to update
 * @param value what to set the column to
 */
private void update(UUID id, String column, Object value) {
	String valueString;
	if (value instanceof String) {
		valueString = "'" + ((String) value) + "'";
	} else {
		valueString = value.toString();
	}

	execSQL("UPDATE level_stat SET " + column + "=" + valueString + ", synced=0 WHERE uuid='" + id + "';");
}

/**
 * Set the level/ćampaign as bookmarked
 * @param id level/campaign id
 * @param bookmark true if bookmarked
 */
void setBookmark(UUID id, boolean bookmark) {
	int sqlBookmark = bookmark ? 1 : 0;
	update(id, "bookmark", sqlBookmark);
}

/**
 * Set a comment for the level/campaign
 * @param id level/campaign id
 * @param comment
 */
void setComment(UUID id, String comment) {
	update(id, "comment", comment);
}

/**
 * Sets when the level/campaign was last played
 * @param id level/campaign id
 * @param date last played date
 */
void setLastPlayed(UUID id, Date date) {
	// Update
	if (statExists(id)) {
		update(id, "last_played", date.getTime());
	}
	// Create new
	else {
		execSQL("INSERT INTO level_stat (uuid, last_played) VALUES ('" + id + "', " + date.getTime() + ");");
	}
}

/**
 * Check if stats exists for the specified level/campaign
 * @param id level/campaign id
 * @return true if stats exists for the level/campaign
 */
boolean statExists(UUID id) {
	DatabaseCursor cursor = rawQuery("SELECT NULL FROM level_stat WHERE uuid='" + id + "' LIMIT 1;");
	return cursor.getCount() == 1;
}

/**
 * Add a tag for the level/campaign
 * @param id level/campaign id
 * @param tag
 */
void addTag(UUID id, Tags tag) {
	execSQL("INSERT INTO level_tag (uuid, tag, date) VALUES ('" + id + "', " + tag.getId() + ", " + new Date().getTime() + ");");
}

/**
 * Checks if a level/campaign is available for tagging.
 * @param id level/campaign id
 * @return true if the level/campaign is available for tagging
 */
boolean isTaggable(UUID id) {
	Date tagDate = getOldDate();
	DatabaseCursor cursor = rawQuery("SELECT NULL FROM level_tag WHERE uuid='" + id + "' AND date>" + tagDate.getTime() + ";");

	// Date OK, check tag amount
	if (cursor.getCount() == 0) {
		cursor = rawQuery("SELECT NULL FROM level_tag WHERE uuid='" + id + "';");

		return cursor.getCount() < Config.Community.TAGS_MAX_PER_RESOURCE;
	}

	return false;
}

/**
 * @return old tagging date
 */
private static Date getOldDate() {
	return new Date(System.currentTimeMillis() - Config.Community.TAGGABLE_DELAY_MS);
}

/**
 * Set all stats as synced
 */
void setAsSynced() {
	// Level stat
	execSQL("UPDATE level_stat SET plays_to_sync=0, clears_to_sync=0, synced=1;");

	// Level tag
	// Prune old tags
	Date deleteDate = getOldDate();
	execSQL("DELETE FROM level_tag WHERE date<" + deleteDate.getTime());

	// Set rest as synced
	execSQL("UPDATE level_tag SET synced=1;");
}

/**
 * Get level/campaign statistics
 * @param id level/campaign id
 * @return statistics for the level, null if no statistics exists
 */
UserLevelStat getLevelStats(UUID id) {
	// @formatter:off
		DatabaseCursor cursor = rawQuery("SELECT "
				+ "bookmark, "
				+ "play_count, "
				+ "clear_count, "
				+ "rating, "
				+ "last_played,"
				+ "comment "
				+ "FROM level_stat "
				+ "WHERE "
				+ "uuid='" + id + "';");
		// @formatter:on

	UserLevelStat userLevelStat = null;

	if (cursor.next()) {
		userLevelStat = new UserLevelStat();
		userLevelStat.bookmarked = cursor.getInt(0) == 1 ? true : false;
		userLevelStat.cPlayed = cursor.getInt(1);
		userLevelStat.cCleared = cursor.getInt(2);
		userLevelStat.rating = cursor.getInt(3);
		userLevelStat.lastPlayed = new Date(cursor.getLong(4));
		userLevelStat.comment = cursor.getString(5);
	}

	cursor.close();

	return userLevelStat;
}

/**
 * @return all statistics that should be synced to the server
 */
StatSyncEntity getUnsynced() {
	StatSyncEntity syncEntity = new StatSyncEntity();

	// Level stats
	DatabaseCursor cursor = rawQuery("SELECT * FROM level_stat WHERE synced=0");

	while (cursor.next()) {
		LevelStat levelStats = new LevelStat();
		levelStats.id = UUID.fromString(cursor.getString(0));
		levelStats.bookmark = cursor.getInt(1) == 1 ? true : false;
		levelStats.cPlayed = cursor.getInt(2);
		levelStats.cPlaysToSync = cursor.getInt(3);
		levelStats.cCleared = cursor.getInt(4);
		levelStats.cClearsToSync = cursor.getInt(5);
		levelStats.rating = cursor.getInt(6);
		levelStats.lastPlayed = new Date(cursor.getLong(7));
		// synced is column 8
		levelStats.comment = cursor.getString(9);
		levelStats.cDeaths = cursor.getInt(10);
		levelStats.cDeathsToSync = cursor.getInt(11);

		// Add tags
		DatabaseCursor tagCursor = rawQuery("SELECT tag FROM level_tag WHERE uuid='" + levelStats.id + "' AND synced=0;");
		while (tagCursor.next()) {
			Tags tag = Tags.fromId(tagCursor.getInt(0));
			levelStats.tags.add(tag);
		}

		tagCursor.close();


		syncEntity.levelStats.add(levelStats);
	}
	cursor.close();

	return syncEntity;
}
}

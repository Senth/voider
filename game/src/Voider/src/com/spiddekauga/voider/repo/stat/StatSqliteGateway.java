package com.spiddekauga.voider.repo.stat;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;

import com.badlogic.gdx.sql.DatabaseCursor;
import com.spiddekauga.voider.network.entities.stat.StatSyncEntity;
import com.spiddekauga.voider.network.entities.stat.StatSyncEntity.LevelStats;
import com.spiddekauga.voider.network.entities.stat.Tags;
import com.spiddekauga.voider.repo.SqliteGateway;
import com.spiddekauga.voider.utils.Pools;

/**
 * Database gateway for statistics
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
class StatSqliteGateway extends SqliteGateway {
	/**
	 * Increases the play count of a level, automatically updates last played date. If no
	 * statistics exist for the level it will be created.
	 * @param id level/campaign id
	 * @param cleared true if the level/campaign was cleared
	 */
	void increasePlayCount(UUID id, boolean cleared) {
		PlaysWrapper playCount = getPlayCount(id);

		long lastPlayed = new Date().getTime();

		// Update existing stats
		if (playCount != null) {
			playCount.cPlayed++;
			playCount.cClearsToSync++;

			if (cleared) {
				playCount.cCleared++;
				playCount.cClearsToSync++;
			}

			// @formatter:off
			execSQL("UPDATE level_stat SET "
					+ "play_count=" + playCount.cPlayed
					+ ", plays_to_sync=" + playCount.cPlaysToSync
					+ ", clear_count=" + playCount.cCleared
					+ ", clears_to_sync=" + playCount.cClearsToSync
					+ ", last_played=" + lastPlayed
					+ ", synced=0"
					+ "WHERE"
					+ "uuid='" + id + "';");
			// @formatter:on
		}
		// Create new stats for level/campaign
		else {
			int clearCount = cleared ? 1 : 0;

			// @formatter:off
			execSQL("INSERT INTO level_stat ("
					+ "uuid, "
					+ "clear_count, "
					+ "clears_to_sync, "
					+ "last_played) "
					+ "VALUES ("
					+ "'" + id + "', "
					+ clearCount + ", "
					+ clearCount + ", "
					+ lastPlayed + ");");
			// @formatter:on
		}
	}

	/**
	 * Get number of plays from the specified id
	 * @param id level/campaign id
	 * @return number of plays, null if no statistics for the level/campaign exists
	 */
	private PlaysWrapper getPlayCount(UUID id) {
		DatabaseCursor cursor = rawQuery("SELECT play_count, plays_to_sync, clear_count, clears_to_sync FROM level_stats WHERE uuid='" + id + "';");

		PlaysWrapper playsWrapper = null;

		if (cursor.next()) {
			playsWrapper = new PlaysWrapper();
			playsWrapper.cPlayed = cursor.getInt(0);
			playsWrapper.cPlaysToSync = cursor.getInt(1);
			playsWrapper.cCleared = cursor.getInt(2);
			playsWrapper.cClearsToSync = cursor.getInt(3);
		}

		cursor.close();

		return playsWrapper;
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
	 * Set the level/ćampaign as bookmarked
	 * @param id level/campaign id
	 * @param bookmark true if bookmarked
	 */
	void setBookmark(UUID id, boolean bookmark) {
		int sqlBookmark = bookmark ? 1 : 0;
		update(id, "bookmark", sqlBookmark);
	}

	/**
	 * Sets when the level/ćampaign was last played
	 * @param id level/ćampaign id
	 * @param date last played date
	 */
	void setLastPlayed(UUID id, Date date) {
		update(id, "last_played", date.getTime());
	}

	/**
	 * Set tags for the level/campaign
	 * @param id level/campaign id
	 * @param tags all tags
	 */
	void setTags(UUID id, ArrayList<Tags> tags) {
		String tagString = tagsToString(tags);
		update(id, "tags", tagString);
	}

	/**
	 * Convert tags to string list
	 * @param tags all tags to convert to string list
	 * @return a string with all tag ids
	 */
	private static String tagsToString(ArrayList<Tags> tags) {
		@SuppressWarnings("unchecked")
		ArrayList<Integer> tagIds = Pools.arrayList.obtain();

		for (Tags tag : tags) {
			tagIds.add(tag.getId());
		}

		return StringUtils.join(tagIds, SEPARATOR);
	}

	/**
	 * Convert a string tag list to array list of tags
	 * @param tagString string of all tags to be converted to a list of tags
	 * @return list of tags
	 */
	private static ArrayList<Tags> stringToTags(String tagString) {
		String[] tagIds = StringUtils.split(tagString, SEPARATOR);
		ArrayList<Tags> tags = new ArrayList<>();

		for (String tagId : tagIds) {
			tags.add(Tags.getEnumFromId(Integer.parseInt(tagId)));
		}

		return tags;
	}

	/**
	 * Set all stats as synced
	 */
	void setAsSynced() {
		// Level stats
		execSQL("UPDATE level_stat SET synced=1;");
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
	 * Get level/campaign statistics
	 * @param id level/campaign id
	 * @return statistics for the level, null if no statistics exists
	 */
	UserLevelStat getLevelStats(UUID id) {
		// @formatter:off
		DatabaseCursor cursor = rawQuery("SELECT"
				+ "bookmark,"
				+ "play_count,"
				+ "clear_count,"
				+ "rating,"
				+ "last_played,"
				+ "tags"
				+ "FROM level_stat"
				+ "WHERE"
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
			userLevelStat.tags = stringToTags(cursor.getString(5));
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
			LevelStats levelStats = new LevelStats();
			levelStats.id = UUID.fromString(cursor.getString(0));
			levelStats.bookmark = cursor.getInt(1) == 1 ? true : false;
			levelStats.cPlayed = cursor.getInt(2);
			levelStats.cPlaysToSync = cursor.getInt(3);
			levelStats.cCleared = cursor.getInt(4);
			levelStats.cClearsToSync = cursor.getInt(5);
			levelStats.rating = cursor.getInt(6);
			levelStats.lastPlayed = new Date(cursor.getLong(7));
			levelStats.tags = stringToTags(cursor.getString(8));

			syncEntity.levelStats.add(levelStats);
		}
		cursor.close();

		return syncEntity;
	}

	/**
	 * Wrapper for number of plays
	 */
	@SuppressWarnings("javadoc")
	private class PlaysWrapper {
		int cPlayed;
		int cPlaysToSync;
		int cCleared;
		int cClearsToSync;
	}

	/** Separator */
	private static final String SEPARATOR = " ";
}

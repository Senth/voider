package com.spiddekauga.voider.repo;

import com.badlogic.gdx.sql.Database;
import com.badlogic.gdx.sql.DatabaseCursor;
import com.badlogic.gdx.sql.SQLiteGdxException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Upgrades or clears the SQLite database to the latest gameVersion
 */
class SqliteUpgrader {
/** Version table column */
private static final int VERSION__VERSION = 0;
/** Version gameVersion column */
private static final int VERSION__TABLE_NAME = 1;
/** DB gameVersion */
private static final int DB_VERSION = 9;
/** Create gameVersion table */
private static final String TABLE_VERSION_CREATE = "CREATE TABLE IF NOT EXISTS gameVersion (gameVersion INTEGER, table_name TEXT);";
/** Database to do the upgrading on */
private Database mDatabase;
/** If the database has been upgraded or deleted */
private boolean mUpgradedOrDeleted = false;
/** Tables that weren't found in the gameVersion table */
private Set<String> mNotFoundTables = new HashSet<String>();
/** Create table queries for all tables */
private Map<String, String> mCreateTableQueries = new HashMap<String, String>();

/**
 * @param database the database to do upgrading on
 */
SqliteUpgrader(Database database) {
	mDatabase = database;

	fillTables();
}

/**
 * Fill tables variable
 */
private void fillTables() {
	// @formatter:off

		// !!! DON'T add table 'gameVersion' to this list

		// revisions
		mNotFoundTables.add("resource_revision");
		mCreateTableQueries.put("resource_revision", "CREATE TABLE IF NOT EXISTS resource_revision ("
				+ "uuid TEXT, "
				+ "revision INTEGER, "
				+ "date INTEGER, "
				+ "uploaded INTEGER DEFAULT 0);");

		// resources
		mNotFoundTables.add("resource");
		mCreateTableQueries.put("resource", "CREATE TABLE IF NOT EXISTS resource ("
				+ "uuid TEXT PRIMARY KEY, "
				+ "type INTEGER, "
				+ "published INTEGER DEFAULT 0);");

		// Removed resources
		mNotFoundTables.add("resource_removed");
		mCreateTableQueries.put("resource_removed", "CREATE TABLE IF NOT EXISTS resource_removed ("
				+ "uuid TEXT PRIMARY KEY);");

		// Highscores
		mNotFoundTables.add("highscore");
		mCreateTableQueries.put("highscore", "CREATE TABLE IF NOT EXISTS highscore ("
				+ "level_id TEXT PRIMARY KEY, "
				+ "score INTEGER, date INTEGER, "
				+ "synced INTEGER DEFAULT 0);");

		// Level stats
		mNotFoundTables.add("level_stat");
		mCreateTableQueries.put("level_stat", "CREATE TABLE IF NOT EXISTS level_stat ("
				+ "uuid TEXT PRIMARY KEY, "
				+ "bookmark INTEGER DEFAULT 0, "
				+ "play_count INTEGER DEFAULT 0, "
				+ "plays_to_sync INTEGER DEFAULT 0, "
				+ "clear_count INTEGER DEFAULT 0, "
				+ "clears_to_sync INTEGER DEFAULT 0, "
				+ "rating INTEGER DEFAULT 0, "
				+ "last_played INTEGER, "
				+ "synced INTEGER DEFAULT 0, "
				+ "comment TEXT DEFAULT '',"
				+ "death_count INTEGER DEFAULT 0,"
				+ "deaths_to_sync INTEGER DEFAULT 0);");

		// Level tags to sync
		mNotFoundTables.add("level_tag");
		mCreateTableQueries.put("level_tag", "CREATE TABLE IF NOT EXISTS level_tag ("
				+ "uuid TEXT,"
				+ "tag INTEGER,"
				+ "date INTEGER,"
				+ "synced INTEGER DEFAULT 0);");


		// Analytics
		// Analytics - Session
		mNotFoundTables.add("analytics_session");
		mCreateTableQueries.put("analytics_session", "CREATE TABLE IF NOT EXISTS analytics_session ("
				+ "session_id TEXT,"
				+ "start_time INTEGER,"
				+ "end_time INTEGER DEFAULT 0,"
				+ "screen_size TEXT);");

		// Analytics - Scene
		mNotFoundTables.add("analytics_scene");
		mCreateTableQueries.put("analytics_scene", "CREATE TABLE IF NOT EXISTS analytics_scene ("
				+ "session_id TEXT,"
				+ "scene_id TEXT,"
				+ "start_time INTEGER,"
				+ "end_time INTEGER DEFAULT 0,"
				+ "name TEXT,"
				+ "load_time REAL);");

		// Analytics - Event
		mNotFoundTables.add("analytics_event");
		mCreateTableQueries.put("analytics_event", "CREATE TABLE IF NOT EXISTS analytics_event ("
				+ "scene_id TEXT,"
				+ "time INTEGER,"
				+ "name TEXT,"
				+ "data TEXT,"
				+ "type INTEGER);");

		// @formatter:on
}

/**
 * @return get DB gameVersion
 */
static int getDbVersion() {
	return DB_VERSION;
}

/**
 * Initializes and upgrade the SQLite database to the latest gameVersion
 * @throws SQLiteGdxException thrown when an error occurs
 */
void initAndUpgrade() throws SQLiteGdxException {
	if (!mUpgradedOrDeleted) {
		// Create gameVersion table
		mDatabase.execSQL(TABLE_VERSION_CREATE);

		createOrUpgradeTables();

		mUpgradedOrDeleted = true;
	}
}

/**
 * Checks for all tables in the database and either creates or updates them
 * @throws SQLiteGdxException
 */
private void createOrUpgradeTables() throws SQLiteGdxException {
	DatabaseCursor cursor = mDatabase.rawQuery("SELECT * FROM gameVersion");

	while (cursor.next()) {
		int version = cursor.getInt(VERSION__VERSION);
		String tableName = cursor.getString(VERSION__TABLE_NAME);

		if (version < DB_VERSION) {
			upgradeTable(tableName, version);
		}

		mNotFoundTables.remove(tableName);
	}

	// Update gameVersion for all tables
	mDatabase.execSQL("UPDATE gameVersion SET gameVersion=" + DB_VERSION + ";");

	// Create tables that weren't found
	createTables();
}

/**
 * Upgrade a table
 * @param table name of the table to upgrade
 * @param fromVersion the gameVersion to upgrade from
 * @throws SQLiteGdxException
 */
private void upgradeTable(String table, int fromVersion) throws SQLiteGdxException {
	// Resource revision
	if (table.equals("resource_revision")) {
		// 2 — Added uploaded status
		if (fromVersion < 2) {
			mDatabase.execSQL("ALTER TABLE resource_revision ADD COLUMN uploaded INTEGER DEFAULT 0;");
		}
	}


	// Level stat
	if (table.equals("level_stat")) {
		// 6 — Added comment to level stats
		if (fromVersion < 6) {
			mDatabase.execSQL("ALTER TABLE level_stat ADD COLUMN comment TEXT DEFAULT '';");
		}
		// 9 - Added death count to level stats
		if (fromVersion < 9) {
			mDatabase.execSQL("ALTER TABLE level_stat ADD COLUMN death_count INTEGER DEFAULT 0;");
			mDatabase.execSQL("ALTER TABLE level_stat ADD COLUMN deaths_to_sync INTEGER DEFAULT 0;");
		}
	}


	// Analytics event
	if (table.equals("analytics_event")) {
		// 8 - Added column type
		if (fromVersion < 8) {
			mDatabase.execSQL("ALTER TABLE analytics_event ADD COLUMN type INTEGER");
			mDatabase.execSQL("UPDATE analytics_event SET type=0 WHERE type IS NULL");
		}
	}
}

/**
 * Creates tables that weren't found
 * @throws SQLiteGdxException
 */
private void createTables() throws SQLiteGdxException {
	for (String table : mNotFoundTables) {
		mDatabase.execSQL(mCreateTableQueries.get(table));

		// Add to gameVersion table
		mDatabase.execSQL("INSERT INTO gameVersion VALUES (" + DB_VERSION + ", '" + table + "');");
	}
}

/**
 * Clear all tables.
 * @throws SQLiteGdxException
 */
void clearTables() throws SQLiteGdxException {
	if (!mUpgradedOrDeleted) {
		for (String table : mNotFoundTables) {
			mDatabase.execSQL("DELETE FROM " + table + ";");
		}

		mUpgradedOrDeleted = true;
	}
}
}

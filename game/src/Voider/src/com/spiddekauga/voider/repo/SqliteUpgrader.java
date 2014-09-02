package com.spiddekauga.voider.repo;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.badlogic.gdx.sql.Database;
import com.badlogic.gdx.sql.DatabaseCursor;
import com.badlogic.gdx.sql.SQLiteGdxException;

/**
 * Upgrades the SQLite database to the latest version
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
class SqliteUpgrader {
	/**
	 * @param database the database to do upgrading on
	 */
	SqliteUpgrader(Database database) {
		mDatabase = database;

		fillTables();
	}

	/**
	 * Initializes and upgrade the SQLite database to the latest version
	 * @throws SQLiteGdxException thrown when an error occurs
	 */
	void initAndUpgrade() throws SQLiteGdxException {
		if (!mUpgraded) {
			// Create version table
			mDatabase.execSQL(TABLE_VERSION_CREATE);

			createOrUpgradeTables();

			mUpgraded = true;
		}
	}

	/**
	 * Checks for all tables in the database and either creates or updates them
	 * @throws SQLiteGdxException
	 */
	private void createOrUpgradeTables() throws SQLiteGdxException {
		DatabaseCursor cursor = mDatabase.rawQuery("SELECT * FROM version");

		while (cursor.next()) {
			int version = cursor.getInt(VERSION__VERSION);
			String tableName = cursor.getString(VERSION__TABLE_NAME);

			if (version < DB_VERSION) {
				upgradeTable(tableName, version);
			}

			mNotFoundTables.remove(tableName);
		}

		// Update version for all tables
		mDatabase.execSQL("UPDATE version SET version=" + DB_VERSION + ";");

		// Create tables that weren't found
		createTables();
	}

	/**
	 * Upgrade a table
	 * @param table name of the table to upgrade
	 * @param fromVersion the version to upgrade from
	 * @throws SQLiteGdxException
	 */
	private void upgradeTable(String table, int fromVersion) throws SQLiteGdxException {
		// 1 -> 2
		if (fromVersion == 1) {
			if (table.equals("resource_revision")) {
				mDatabase.execSQL("ALTER TABLE resource_revision ADD COLUMN uploaded INTEGER DEFAULT 0;");
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

			// Add to version table
			mDatabase.execSQL("INSERT INTO version VALUES (" + DB_VERSION + ", '" + table + "');");
		}
	}

	/**
	 * Fill tables variable
	 */
	private void fillTables() {
		// @formatter:off

		// !!! DON'T add table 'version' to this list

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
				+ "play_count INTEGER DEFAULT 1, "
				+ "plays_to_sync INTEGER DEFAULT 1, "
				+ "clear_count INTEGER DEFAULT 0, "
				+ "clears_to_sync INTEGER DEFAULT 0, "
				+ "rating INTEGER DEFAULT 0, "
				+ "last_played INTEGER, "
				+ "tags TEXT DEFAULT '', "
				+ "synced INTEGER DEFAULT 0);");


		// @formatter:on
	}

	/**
	 * @return get DB version
	 */
	static int getDbVersion() {
		return DB_VERSION;
	}

	/** Database to do the upgrading on */
	private Database mDatabase;

	/** Version table column */
	private static final int VERSION__VERSION = 0;
	/** Version version column */
	private static final int VERSION__TABLE_NAME = 1;

	/** If the database has been upgraded */
	private boolean mUpgraded = false;
	/** Tables that weren't found in the version table */
	private Set<String> mNotFoundTables = new HashSet<String>();
	/** Create table queries for all tables */
	private Map<String, String> mCreateTableQueries = new HashMap<String, String>();
	/** DB version */
	private static final int DB_VERSION = 5;
	/** Create version table */
	private static final String TABLE_VERSION_CREATE = "CREATE TABLE IF NOT EXISTS version (version INTEGER, table_name TEXT);";
}

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
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
class SqliteUpgrader {
	/**
	 * Initializes and upgrade the SQLite database to the latest version
	 * @param database the database to upgrade
	 * @throws SQLiteGdxException thrown when an error occurs
	 */
	static void initAndUpgrade(Database database) throws SQLiteGdxException {
		if (!mUpgraded) {
			fillTables();

			// Create version table
			database.execSQL(TABLE_VERSION_CREATE);

			createOrUpgradeTables(database);

			mUpgraded = true;
		}
	}

	/**
	 * Checks for all tables in the database and either creates or updates them
	 * @param database
	 * @throws SQLiteGdxException
	 */
	private static void createOrUpgradeTables(Database database) throws SQLiteGdxException {
		DatabaseCursor cursor = database.rawQuery("SELECT * FROM version");

		while (cursor.next()) {
			int version = cursor.getInt(VERSION__VERSION);
			String tableName = cursor.getString(VERSION__TABLE_NAME);

			if (version < DB_VERSION) {
				upgradeTable(database, tableName, version);
			}

			mNotFoundTables.remove(tableName);
		}

		// Update version for all tables
		database.execSQL("UPDATE version SET version=" + DB_VERSION + ";");

		// Create tables that weren't found
		createTables(database);
	}

	/**
	 * Upgrade a table
	 * @param database the database to upgrade from
	 * @param table name of the table to upgrade
	 * @param fromVersion the version to upgrade from
	 */
	private static void upgradeTable(Database database, String table, int fromVersion) {
		// TODO not yet implemented
	}

	/**
	 * Creates tables that weren't found
	 * @param database
	 * @throws SQLiteGdxException
	 */
	private static void createTables(Database database) throws SQLiteGdxException {
		for (String table : mNotFoundTables) {
			database.execSQL(mCreateTableQueries.get(table));

			// Add to version table
			database.execSQL("INSERT INTO version VALUES (" + DB_VERSION + ", '" + table + "');");
		}
	}

	/**
	 * Fill tables variable
	 */
	private static void fillTables() {
		// DON'T add VERSION to these tables

		// new_user
		mNotFoundTables.add("new_user");
		mCreateTableQueries.put("new_user", "CREATE TABLE IF NOT EXISTS new_user (username TEXT, password TEXT, email TEXT);");

		// revisions
		mNotFoundTables.add("resource_revision");
		mCreateTableQueries.put("resource_revision", "CREATE TABLE IF NOT EXISTS resource_revision (uuid TEXT PRIMARY KEY, revision INTEGER, date INTEGER);");

		// resources
		mNotFoundTables.add("resource");
		mCreateTableQueries.put("resource", "CREATE TABLE IF NOT EXISTS resource (uuid TEXT PRIMARY KEY, type INTEGER);");
	}

	/**
	 * @return get DB version
	 */
	static int getDbVersion() {
		return DB_VERSION;
	}

	/** Version table column */
	private static final int VERSION__VERSION = 0;
	/** Version version column */
	private static final int VERSION__TABLE_NAME = 1;

	/** If the database has been upgraded */
	private static boolean mUpgraded = false;
	/** Tables that weren't found in the version table */
	private static Set<String> mNotFoundTables = new HashSet<String>();
	/** Create table queries for all tables */
	private static Map<String, String> mCreateTableQueries = new HashMap<String, String>();
	/** DB version */
	private static final int DB_VERSION = 1;
	/** Create version table */
	private static final String TABLE_VERSION_CREATE = "CREATE TABLE IF NOT EXISTS version (version INTEGER, table_name TEXT);";
}

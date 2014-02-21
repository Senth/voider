package com.spiddekauga.voider.repo;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.sql.Database;
import com.badlogic.gdx.sql.DatabaseFactory;
import com.badlogic.gdx.sql.SQLiteGdxException;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.spiddekauga.voider.Config;

/**
 * SQLite gateway
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
class SqliteGateway implements Disposable {
	/**
	 * Closes the database connection to SQLite
	 */
	@Override
	public void dispose() {
		if (mDatabase != null) {
			try {
				mDatabase.closeDatabase();
			} catch (SQLiteGdxException e) {
				e.printStackTrace();
			}
			mDatabase = null;
		}
	}

	/**
	 * @return filename/location of Voider database
	 */
	private String getDatabaseLocation() {
		switch (Gdx.app.getType()) {
		// Get external file location
		case Desktop:
			return Gdx.files.getExternalStoragePath() + Config.File.DB_FILEPATH;

		case Android:
			return Config.File.DB_FILENAME;

		default:
			throw new GdxRuntimeException("Unsupported application type: " + Gdx.app.getType());
		}
	}

	/**
	 * Default constructor
	 */
	protected SqliteGateway() {
		if (mDatabase == null) {
			mDatabase = DatabaseFactory.getNewDatabase(getDatabaseLocation(), SqliteUpgrader.getDbVersion(), null, null);

			// Does nothing
			mDatabase.setupDatabase();
			try {
				mDatabase.openOrCreateDatabase();

				SqliteUpgrader upgrader = new SqliteUpgrader(mDatabase);
				upgrader.initAndUpgrade();
			} catch (SQLiteGdxException e) {
				mDatabase = null;
				e.printStackTrace();
			}
		}
	}

	/** The database */
	protected static Database mDatabase = null;
}

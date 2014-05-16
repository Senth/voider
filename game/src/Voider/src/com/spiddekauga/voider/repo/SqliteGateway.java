package com.spiddekauga.voider.repo;

import java.util.Observable;
import java.util.Observer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.sql.Database;
import com.badlogic.gdx.sql.DatabaseFactory;
import com.badlogic.gdx.sql.SQLiteGdxException;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.utils.User;
import com.spiddekauga.voider.utils.User.UserEvents;

/**
 * SQLite gateway
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
abstract class SqliteGateway implements Disposable, Observer {
	/**
	 * Closes the database connection to SQLite
	 */
	@Override
	public synchronized void dispose() {
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
			return Gdx.files.getExternalStoragePath() + Config.File.getUserStorage() + Config.File.DB_FILENAME;

		case Android:
			return User.getGlobalUser().getUsername() + "_" + Config.File.DB_FILENAME;

		default:
			throw new GdxRuntimeException("Unsupported application type: " + Gdx.app.getType());
		}
	}

	/**
	 * Default constructor
	 */
	protected SqliteGateway() {
		User.getGlobalUser().addObserver(this);

		// Connect if logged in already
		if (User.getGlobalUser().isLoggedIn()) {
			connect();
		}
	}

	/**
	 * Connects to the database
	 */
	private synchronized void connect() {
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

	@Override
	public void update(Observable object, Object arg) {
		if (object instanceof User) {
			if (arg instanceof UserEvents) {
				switch ((UserEvents)arg) {
				case LOGIN:
					connect();
					break;

				case LOGOUT:
					dispose();
					break;
				}
			}
		}
	}

	/** The database */
	protected static Database mDatabase = null;
}

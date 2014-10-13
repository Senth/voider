package com.spiddekauga.voider.repo;

import java.util.Date;
import java.util.Observable;
import java.util.Observer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.sql.Database;
import com.badlogic.gdx.sql.DatabaseCursor;
import com.badlogic.gdx.sql.DatabaseFactory;
import com.badlogic.gdx.sql.SQLiteGdxException;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.utils.GameEvent;
import com.spiddekauga.voider.utils.User;

/**
 * SQLite gateway
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public abstract class SqliteGateway implements Disposable, Observer {
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
				throw new GdxRuntimeException(e);
			}
		}
	}

	@Override
	public void update(Observable object, Object arg) {
		if (object instanceof User) {
			if (arg instanceof GameEvent) {
				switch (((GameEvent) arg).type) {
				case USER_LOGIN:
					connect();
					break;

				case USER_LOGOUT:
					dispose();
					break;

				default:
					break;
				}
			}
		}
	}

	/**
	 * Execute a single SQL statement that is NOT a SELECT or any other SQL statement that
	 * returns data.
	 * @param sql the SQL statement to be executed. Multiple statements separated by
	 *        semicolons are not supported.
	 */
	protected static synchronized void execSQL(String sql) {
		try {
			mDatabase.execSQL(sql);
		} catch (SQLiteGdxException e) {
			e.printStackTrace();
			throw new GdxRuntimeException(e);
		}
	}

	/**
	 * Runs the provided SQL and returns a {@link DatabaseCursor} over the result set.
	 * @param sql the SQL query. The SQL string must not be ; terminated
	 * @return {@link DatabaseCursor}
	 */
	protected static synchronized DatabaseCursor rawQuery(String sql) {
		try {
			return mDatabase.rawQuery(sql);
		} catch (SQLiteGdxException e) {
			e.printStackTrace();
			throw new GdxRuntimeException(e);
		}
	}

	/**
	 * Runs the provided SQL and returns the same {@link DatabaseCursor} that was passed
	 * to this method. Use this method when you want to avoid reallocation of
	 * {@link DatabaseCursor} object. Note that you shall only pass the
	 * {@link DatabaseCursor} object that was previously returned by a rawQuery method.
	 * Creating your own {@link DatabaseCursor} and then passing it as an object will not
	 * work.
	 * @param cursor existing {@link DatabaseCursor} object
	 * @param sql the SQL query. The SQL string must not be ; terminated
	 * @return the passed {@link DatabaseCursor}.
	 */
	protected static synchronized DatabaseCursor rawQuery(DatabaseCursor cursor, String sql) {
		try {
			return mDatabase.rawQuery(cursor, sql);
		} catch (SQLiteGdxException e) {
			e.printStackTrace();
			throw new GdxRuntimeException(e);
		}
	}

	/**
	 * Date to string
	 * @param date the date to string.
	 * @return date in string format, 0 if date is null.
	 */
	protected static String dateToString(Date date) {
		return date == null ? "0" : String.valueOf(date.getTime());
	}

	/**
	 * @return the database
	 */
	protected static Database getDatabase() {
		return mDatabase;
	}

	/** The database */
	private static Database mDatabase = null;
}

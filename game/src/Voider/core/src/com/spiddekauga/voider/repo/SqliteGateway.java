package com.spiddekauga.voider.repo;

import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.sql.Database;
import com.badlogic.gdx.sql.DatabaseCursor;
import com.badlogic.gdx.sql.DatabaseFactory;
import com.badlogic.gdx.sql.SQLiteGdxException;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.repo.user.User;
import com.spiddekauga.voider.utils.event.EventDispatcher;
import com.spiddekauga.voider.utils.event.EventTypes;
import com.spiddekauga.voider.utils.event.GameEvent;
import com.spiddekauga.voider.utils.event.IEventListener;

/**
 * SQLite gateway
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public abstract class SqliteGateway implements Disposable, IEventListener {
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
		EventDispatcher.getInstance().connect(EventTypes.USER_LOGIN, this);
		EventDispatcher.getInstance().connect(EventTypes.USER_LOGOUT, this);

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
	public void handleEvent(GameEvent event) {
		switch (event.type) {
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

	/**
	 * Set if this instance should queue failed SQL statements when calling
	 * {@link #execSQL(String)}.
	 * @param queue true if we should queue. Default is false.
	 */
	protected void setQueueFailedSqlStatements(boolean queue) {
		mQueueIfFail = queue;
	}

	/**
	 * Execute a single SQL statement that is NOT a SELECT or any other SQL statement that
	 * returns data. If {@link #setQueueFailedSqlStatements(boolean)} has been called with
	 * true and if the database isn't yet connect the SQL statement will be pushed onto a
	 * queue; when this method is called again it will retry to execute the queue messages
	 * before the new SQL statement.
	 * @param sql the SQL statement to be executed. Multiple statements separated by
	 *        semicolons are not supported.
	 */
	protected void execSQL(String sql) {
		// Try to execute failed SQL statements
		if (mQueueIfFail) {
			boolean failed = false;
			while (!mFailQueue.isEmpty() && !failed) {
				try {
					execSQLPrivate(mFailQueue.peek());
					mFailQueue.remove();
				} catch (SQLiteGdxException e) {
					failed = true;
				}
			}

			// Execute new SQL statement
			if (!failed) {
				try {
					execSQLPrivate(sql);
				} catch (SQLiteGdxException e) {
					mFailQueue.add(sql);
				}
			}
			// Add new SQL statement to the fail queue
			else {
				mFailQueue.add(sql);
			}
		}
		// Regular execution
		else {
			try {
				execSQLPrivate(sql);
			} catch (SQLiteGdxException e) {
				e.printStackTrace();
				throw new GdxRuntimeException(e);
			}
		}
	}

	/**
	 * Execute a single SQL statement that is NOT a SELECT or any other SQL statement that
	 * returns data. If {@link #setQueueFailedSqlStatements(boolean)} has been called with
	 * true and if the database isn't yet connect the SQL statement will be pushed onto a
	 * queue; when this method is called again it will retry to execute the queue messages
	 * before the new SQL statement.
	 * @param sql the SQL statement to be executed. Multiple statements separated by
	 *        semicolons are not supported.
	 * @throws SQLiteGdxException
	 */
	private static synchronized void execSQLPrivate(String sql) throws SQLiteGdxException {
		if (mDatabase == null) {
			throw new SQLiteGdxException("Database not connected!");
		}

		mDatabase.execSQL(sql);
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

	/**
	 * Delete everything in the database
	 */
	public static void clearDatabase() {
		if (mDatabase != null) {
			SqliteUpgrader sqliteUpgrader = new SqliteUpgrader(mDatabase);
			try {
				sqliteUpgrader.clearTables();
			} catch (SQLiteGdxException e) {
				e.printStackTrace();
			}
		}
	}

	private Queue<String> mFailQueue = new LinkedList<>();
	/** Queue exec SQL statements if they fail, and retry at a later point */
	private boolean mQueueIfFail = false;
	private static Database mDatabase = null;
}

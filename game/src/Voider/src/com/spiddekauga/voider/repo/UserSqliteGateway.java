package com.spiddekauga.voider.repo;

import java.util.ArrayList;

import com.badlogic.gdx.sql.DatabaseCursor;
import com.badlogic.gdx.sql.SQLiteGdxException;
import com.spiddekauga.voider.utils.Pools;
import com.spiddekauga.voider.utils.UserInfo;


/**
 * SQLite gateway for user repository
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
class UserSqliteGateway extends SqliteGateway {
	/**
	 * Default constructor.
	 */
	UserSqliteGateway() {
		// Does nothing
	}

	/**
	 * Removes a temporary user from the database
	 * @param username the user with the specified username to remove
	 */
	void removeTempUser(String username) {
		try {
			mDatabase.execSQL("DELETE FROM new_user WHERE username LIKE '" + username + "';");
		} catch (SQLiteGdxException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Creates a new temporary offline user until Voider gets goes online
	 * @param username new username
	 * @param password the password for the user
	 * @param email the email of the user
	 * @return true if the user was created successfully, false if email or username already exists
	 */
	boolean createTempUser(String username, String password, String email) {
		boolean success = false;

		try {
			DatabaseCursor cursor = mDatabase.rawQuery("SELECT username FROM new_user WHERE username LIKE '" + username + "' OR "
					+ "email LIKE '" + email + "';");


			// No user with that email or name exists -> continue creating
			if (!cursor.next()) {
				mDatabase.execSQL("INSERT INTO new_user VALUES ( '" + username + "', '" + password + "', '" + email + "' );");
				return true;
			}
		} catch (SQLiteGdxException e) {
			e.printStackTrace();
		}

		return success;
	}

	/**
	 * @return all temporary created users
	 */
	ArrayList<UserInfo> getTempUsers() {
		@SuppressWarnings("unchecked")
		ArrayList<UserInfo> users = Pools.arrayList.obtain();

		try {
			DatabaseCursor cursor = mDatabase.rawQuery("SELECT * FROM new_user");

			while (cursor.next()) {
				UserInfo userInfo = new UserInfo();
				userInfo.username = cursor.getString(NEW_USER__USERNAME);
				userInfo.password = cursor.getString(NEW_USER__PASSWORD);
				userInfo.email = cursor.getString(NEW_USER__EMAIL);

				users.add(userInfo);
			}
		} catch (SQLiteGdxException e) {
			e.printStackTrace();
		}

		return users;
	}

	/**
	 * Search for a temporary user with the specified username/email
	 * @param username either username or email
	 * @return user information for the found user, null if not found
	 */
	UserInfo getTempUser(String username) {
		try {
			DatabaseCursor cursor = mDatabase.rawQuery("SELECT * FROM new_user WHERE username LIKE '" + username + "' OR email LIKE '" + username + "';");

			if (cursor.next()) {
				UserInfo userInfo = new UserInfo();
				userInfo.username = cursor.getString(NEW_USER__USERNAME);
				userInfo.password = cursor.getString(NEW_USER__PASSWORD);
				userInfo.email = cursor.getString(NEW_USER__EMAIL);

				return userInfo;
			}
		} catch (SQLiteGdxException e) {
			e.printStackTrace();
		}

		return null;
	}

	/** Column for new_user username */
	private static final int NEW_USER__USERNAME = 0;
	/** Column for new_user password */
	private static final int NEW_USER__PASSWORD = 1;
	/** Column for new_user email */
	private static final int NEW_USER__EMAIL = 2;
}

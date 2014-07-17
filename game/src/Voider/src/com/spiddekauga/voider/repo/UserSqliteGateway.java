package com.spiddekauga.voider.repo;

import java.util.ArrayList;

import com.badlogic.gdx.sql.DatabaseCursor;
import com.spiddekauga.voider.utils.Pools;
import com.spiddekauga.voider.utils.User;


/**
 * SQLite gateway for user repository
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
		execSQL("DELETE FROM new_user WHERE username LIKE '" + username + "';");
	}

	/**
	 * Creates a new temporary offline user until Voider gets goes online
	 * @param username new username
	 * @param password the password for the user
	 * @param email the email of the user
	 * @return true if the user was created successfully, false if email or username
	 *         already exists
	 */
	boolean createTempUser(String username, String password, String email) {
		boolean success = false;

		DatabaseCursor cursor = rawQuery("SELECT username FROM new_user WHERE username LIKE '" + username + "' OR " + "email LIKE '" + email + "';");


		// No user with that email or name exists -> continue creating
		if (!cursor.next()) {
			execSQL("INSERT INTO new_user VALUES ( '" + username + "', '" + password + "', '" + email + "' );");
			return true;
		}

		return success;
	}

	/**
	 * @return all temporary created users
	 */
	ArrayList<User> getTempUsers() {
		@SuppressWarnings("unchecked")
		ArrayList<User> users = Pools.arrayList.obtain();

		DatabaseCursor cursor = rawQuery("SELECT * FROM new_user");

		while (cursor.next()) {
			User userInfo = new User();
			userInfo.setUsername(cursor.getString(NEW_USER__USERNAME));
			userInfo.setPassword(cursor.getString(NEW_USER__PASSWORD));
			userInfo.setEmail(cursor.getString(NEW_USER__EMAIL));

			users.add(userInfo);
		}

		return users;
	}

	/**
	 * Search for a temporary user with the specified username/email
	 * @param username either username or email
	 * @return user information for the found user, null if not found
	 */
	User getTempUser(String username) {
		DatabaseCursor cursor = rawQuery("SELECT * FROM new_user WHERE username LIKE '" + username + "' OR email LIKE '" + username + "';");

		if (cursor.next()) {
			User userInfo = new User();
			userInfo.setUsername(cursor.getString(NEW_USER__USERNAME));
			userInfo.setPassword(cursor.getString(NEW_USER__PASSWORD));
			userInfo.setEmail(cursor.getString(NEW_USER__EMAIL));

			return userInfo;
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

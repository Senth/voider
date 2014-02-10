package com.spiddekauga.voider.repo;

import java.util.ArrayList;
import java.util.UUID;

import com.spiddekauga.voider.utils.Pools;
import com.spiddekauga.voider.utils.UserInfo;

/**
 * Local repository for users
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class UserLocalRepo {
	/**
	 * Set last logged in user
	 * @param username username or email of the user that was logged in
	 * @param privateKey the private key which will be used for automatic login
	 * in the future.
	 */
	public static void setLastUser(String username, UUID privateKey) {
		mPrefsGateway.setLastUser(username, privateKey);
	}

	/**
	 * Removes the last logged in user
	 */
	public static void removeLastUser() {
		mPrefsGateway.removeLastUser();
	}

	/**
	 * Get information of the last user that was logged in
	 * @return last user logged in, null if not found
	 */
	public static UserInfo getLastUser() {
		return mPrefsGateway.getLastUser();
	}

	/**
	 * Creates a new temporary offline user until Voider gets goes online
	 * @param username new username
	 * @param password the password for the user
	 * @param email the email of the user
	 */
	public static void createTempUser(String username, String password, String email) {
		// TODO
	}

	/**
	 * @return all temporary created users
	 */
	public static ArrayList<UserInfo> getTempUsers() {
		@SuppressWarnings("unchecked")
		ArrayList<UserInfo> users = Pools.arrayList.obtain();

		// TODO

		return users;
	}

	/** Preferences gateway */
	private static UserPrefsGateway mPrefsGateway = new UserPrefsGateway();
	/** SQLite gateway */

}

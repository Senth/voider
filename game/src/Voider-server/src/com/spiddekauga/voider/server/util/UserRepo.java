package com.spiddekauga.voider.server.util;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.spiddekauga.appengine.DatastoreUtils;

/**
 * Helper methods for users
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class UserRepo {
	/**
	 * Gets the username of a user with the specified key
	 * @param userKey key of the user
	 * @return username of the user, null if not found
	 */
	public static String getUsername(Key userKey) {
		Entity userEntity = DatastoreUtils.getItemByKey(userKey);
		if (userEntity != null) {
			return (String) userEntity.getProperty("username");
		} else {
			return null;
		}
	}
}

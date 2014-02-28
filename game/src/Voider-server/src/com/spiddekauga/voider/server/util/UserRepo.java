package com.spiddekauga.voider.server.util;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.spiddekauga.appengine.DatastoreUtils;

/**
 * Helper methods for users
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class UserRepo {
	/**
	 * Gets the username of a user with the specified key
	 * @param userKey key of the user
	 * @return username of the user, null if not found
	 */
	public static String getUsername(Key userKey) {
		try {
			Entity userEntity = DatastoreUtils.mDatastore.get(userKey);
			return (String) userEntity.getProperty("username");
		} catch (EntityNotFoundException e) {
			return null;
		}
	}
}

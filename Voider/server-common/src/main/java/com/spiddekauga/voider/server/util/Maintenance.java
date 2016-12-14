package com.spiddekauga.voider.server.util;

import com.google.appengine.api.datastore.Entity;
import com.spiddekauga.appengine.DatastoreUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Maintenance helper
 */
public class Maintenance {
/**
 * @return current maintenance mode
 */
public static Modes getMaintenanceMode() {
	Entity entity = DatastoreUtils.getSingleEntity(DatastoreTables.MAINTENANCE);

	Modes mode = Modes.UP;

	if (entity != null) {
		String modeString = (String) entity.getProperty(DatastoreTables.CMaintenance.MODE);
		if (modeString != null) {
			mode = Modes.fromString(modeString);
		}
	}

	return mode;
}

/**
 * All maintenance modes
 */
public enum Modes {
	/** Up and running */
	UP,
	/** Server is down for maintenance */
	DOWN,;


	private static Map<String, Modes> mStringToEnum = new HashMap<>();

	static {
		for (Modes mode : Modes.values()) {
			mStringToEnum.put(mode.toString(), mode);
		}
	}

	/**
	 * Convert a string back to a maintenance mode
	 * @return the enumeration of mode, null if not found
	 */
	public static Modes fromString(String mode) {
		return mStringToEnum.get(mode);
	}
}
}

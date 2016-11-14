package com.spiddekauga.voider.network.analytics;

import java.util.HashMap;

/**
 * Different event types for analytics

 */
public enum AnalyticsEventTypes {
	/** Unknown event type */
	UNKNOWN(0),
	/** Buttons */
	BUTTON(1),
	/** Keyboard/hotkey */
	KEY(2),
	/** Slider */
	SLIDER(3),

	;

	/**
	 * @param id the id saved in datastore
	 */
	private AnalyticsEventTypes(int id) {
		mId = id;
	}

	/**
	 * @return id of the movement type
	 */
	public int toId() {
		return mId;
	}

	/**
	 * Get the enum with the specified id
	 * @param id id of the enum to get
	 * @return enum with the specified id, null if not found
	 */
	public static AnalyticsEventTypes fromId(int id) {
		return mIdToEnum.get(id);
	}

	private int mId;
	private static HashMap<Integer, AnalyticsEventTypes> mIdToEnum = new HashMap<>();

	static {
		for (AnalyticsEventTypes type : AnalyticsEventTypes.values()) {
			mIdToEnum.put(type.mId, type);
		}
	}
}

package com.spiddekauga.voider.network.misc;

import com.spiddekauga.utils.IIdStore;
import com.spiddekauga.voider.network.analytics.AnalyticsSessionEntity;
import com.spiddekauga.voider.network.entities.IEntity;

import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

/**
 * Bug report information
 */
public class BugReportEntity implements IEntity {
public String userKey = null;
public String subject;
public String description;
public Date date;
/** Additional information such as exception, optional */
public String exception = "";
/** Analytics information (events, scenes, last actions...), optional */
public AnalyticsSessionEntity analyticsSession = null;
public String os;
public String gameVersion;
public String buildType;
public String resolution;
/** id of the report, only used locally */
public UUID id = UUID.randomUUID();
public BugReportTypes type;

/**
 * Bug report types
 */
public enum BugReportTypes implements IIdStore {
	/** Unknown/Invalid */
	UNKNOWN(0),
	/** Bug report from an exception */
	BUG_EXCEPTION(1),
	/** Custom bug report */
	BUG_CUSTOM(2),
	/** Feature request */
	FEATURE(3),;

	private static HashMap<Integer, BugReportTypes> mIdToEnum = new HashMap<>();

	static {
		for (BugReportTypes type : BugReportTypes.values()) {
			mIdToEnum.put(type.mId, type);
		}
	}

	private int mId;


	private BugReportTypes(int id) {
		mId = id;
	}

	/**
	 * Get the enum from an id
	 * @param id
	 * @return enum for the specified id, unknown if not found
	 */
	public static BugReportTypes fromId(int id) {
		BugReportTypes type = mIdToEnum.get(id);
		if (type == null) {
			type = UNKNOWN;
		}
		return type;
	}

	@Override
	public int toId() {
		return mId;
	}
}
}

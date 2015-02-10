package com.spiddekauga.voider.network.misc;

import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.util.IIdStore;

/**
 * Bug report information
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class BugReportEntity implements IEntity {
	/** User that's reporting, null if anonymous */
	public String userKey = null;
	/** Subject */
	public String subject;
	/** Additional description */
	public String description;
	/** Date of the report */
	public Date date;
	/** Additional information such as exception or last actions, optional */
	public String additionalInformation = "";
	/** System information */
	public String systemInformation;
	/** id of the report, only used locally */
	public UUID id = UUID.randomUUID();
	/** Bug report type */
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
		FEATURE(3),

		;

		private BugReportTypes(int id) {
			mId = id;
		}

		@Override
		public int toId() {
			return mId;
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


		private int mId;
		private static HashMap<Integer, BugReportTypes> mIdToEnum = new HashMap<>();

		static {
			for (BugReportTypes type : BugReportTypes.values()) {
				mIdToEnum.put(type.mId, type);
			}
		}
	}
}

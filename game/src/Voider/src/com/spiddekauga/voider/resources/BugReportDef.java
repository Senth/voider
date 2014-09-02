package com.spiddekauga.voider.resources;

import java.util.Date;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.spiddekauga.voider.network.entities.misc.BugReportEntity;


/**
 * Bug report to store locally
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class BugReportDef extends Resource {
	/**
	 * Creates a bug report resource from a network entity and store it locally
	 * @param bugReportEntity entity to set the resource from
	 */
	public BugReportDef(
			BugReportEntity bugReportEntity) {
		mUserKey = bugReportEntity.userKey;
		mSubject = bugReportEntity.subject;
		mLastAction = bugReportEntity.lastAction;
		mSecondLastAction = bugReportEntity.secondLastAction;
		mDescription = bugReportEntity.description;
		mDate = bugReportEntity.date;
		mException = bugReportEntity.exception;
		mSystemInformation = bugReportEntity.systemInformation;
		mUniqueId = bugReportEntity.id;
	}

	/**
	 * Default constructor for kryo
	 */
	@SuppressWarnings("unused")
	private BugReportDef() {
		// For kryo
	}

	/**
	 * @return a new network entity from this Bug Report resource
	 */
	public BugReportEntity toNetworkEntity() {
		BugReportEntity entity = new BugReportEntity();

		entity.userKey = mUserKey;
		entity.subject = mSubject;
		entity.lastAction = mLastAction;
		entity.secondLastAction = mSecondLastAction;
		entity.description = mDescription;
		entity.exception = mException;
		entity.date = mDate;
		entity.systemInformation = mSystemInformation;
		entity.id = mUniqueId;

		return entity;
	}

	/** User that's reporting */
	@Tag(113)
	private String mUserKey;
	/** Last action */
	@Tag(114)
	private String mLastAction;
	/** Second last action */
	@Tag(115)
	private String mSecondLastAction;
	/** Additional description */
	@Tag(119)
	private String mDescription;
	/** Date of the report */
	@Tag(120)
	private Date mDate = new Date();
	/** The exception that was thrown, optional */
	@Tag(121)
	private String mException = null;
	/** Subject */
	@Tag(122)
	private String mSubject;
	/** System information */
	@Tag(123)
	private String mSystemInformation;
}

package com.spiddekauga.voider.resources;

import java.util.Date;
import java.util.UUID;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.spiddekauga.voider.network.entities.BugReportEntity;


/**
 * Bug report to store locally
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class BugReportDef extends Resource {
	/**
	 * Creates a bug report resource to store locally
	 * @param userKey the user's server key that created the bug report
	 * @param lastAction text field for the last action before the bug occurred
	 * @param secondLastAction text field for the second last action
	 * @param thirdLastAction text field for the third last action
	 * @param expectedOutcome text field for the expected outcome
	 * @param actualOutcome text field for the actual outcome
	 * @param description text field for the description
	 * @param exception the exception that was thrown
	 * 
	 */
	public BugReportDef(
			String userKey,
			String lastAction,
			String secondLastAction,
			String thirdLastAction,
			String expectedOutcome,
			String actualOutcome,
			String description,
			Exception exception
			) {
		this();

		mUserKey = userKey;
		mLastAction = lastAction;
		mSecondLastAction = secondLastAction;
		mThirdLastAction = thirdLastAction;
		mExpectedOutcome = expectedOutcome;
		mActualOutcome = actualOutcome;
		mDescription = description;
		mException = exception;
	}

	/**
	 * Creates a bug report resource from a network entity and store it locally
	 * @param bugReportEntity entity to set the resource from
	 */
	public BugReportDef(BugReportEntity bugReportEntity) {
		this();

		mUserKey = bugReportEntity.userKey;
		mLastAction = bugReportEntity.lastAction;
		mSecondLastAction = bugReportEntity.secondLastAction;
		mThirdLastAction = bugReportEntity.thirdLastAction;
		mExpectedOutcome = bugReportEntity.expectedOutcome;
		mActualOutcome = bugReportEntity.actualOutcome;
		mDescription = bugReportEntity.description;
		mDate = bugReportEntity.date;
		mException = bugReportEntity.exception;
	}

	/**
	 * Default constructor, sets UUID
	 */
	private BugReportDef() {
		mUniqueId = UUID.randomUUID();
	}

	/**
	 * @return a new network entity from this Bug Report resource
	 */
	public BugReportEntity toNetworkEntity() {
		BugReportEntity entity = new BugReportEntity();

		entity.userKey = mUserKey;
		entity.lastAction = mLastAction;
		entity.secondLastAction = mSecondLastAction;
		entity.thirdLastAction = mThirdLastAction;
		entity.expectedOutcome = mExpectedOutcome;
		entity.actualOutcome = mActualOutcome;
		entity.description = mDescription;
		entity.exception = mException;
		entity.date = mDate;

		return entity;
	}

	/** User that's reporting */
	@Tag(113) private String mUserKey;
	/** Last action */
	@Tag(114) private String mLastAction;
	/** Second last action */
	@Tag(115) private String mSecondLastAction;
	/** Third last action */
	@Tag(116) private String mThirdLastAction;
	/** Expected outcome */
	@Tag(117) private String mExpectedOutcome;
	/** Actual outcome */
	@Tag(118) private String mActualOutcome;
	/** Additional description */
	@Tag(119) private String mDescription;
	/** Date of the report */
	@Tag(120) private Date mDate = new Date();
	/** The exception that was thrown, optional */
	@Tag(121) private Exception mException = null;
}

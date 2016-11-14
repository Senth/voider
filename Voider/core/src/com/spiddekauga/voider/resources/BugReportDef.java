package com.spiddekauga.voider.resources;

import java.util.Date;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.spiddekauga.voider.network.misc.BugReportEntity;
import com.spiddekauga.voider.network.misc.BugReportEntity.BugReportTypes;


/**
 * Bug report to store locally

 */
public class BugReportDef extends Resource {
	/**
	 * Creates a bug report resource from a network entity and store it locally
	 * @param bugReportEntity entity to set the resource from
	 */
	public BugReportDef(BugReportEntity bugReportEntity) {
		mUserKey = bugReportEntity.userKey;
		mSubject = bugReportEntity.subject;
		mDescription = bugReportEntity.description;
		mDate = bugReportEntity.date;
		mAdditionalInforamtion = bugReportEntity.additionalInformation;
		mSystemInformation = bugReportEntity.systemInformation;
		mUniqueId = bugReportEntity.id;
		mTypeId = bugReportEntity.type.toId();
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
		entity.description = mDescription;
		entity.additionalInformation = mAdditionalInforamtion;
		entity.date = mDate;
		entity.systemInformation = mSystemInformation;
		entity.id = mUniqueId;
		entity.type = BugReportTypes.fromId(mTypeId);

		return entity;
	}

	/** User that's reporting, null if anonymous */
	@Tag(113) private String mUserKey;
	@Tag(114) @Deprecated private String mLastAction;
	@Tag(115) @Deprecated private String mSecondLastAction;
	@Tag(119) private String mDescription;
	@Tag(120) private Date mDate = new Date();
	/** Additional information, such as exception */
	@Tag(121) private String mAdditionalInforamtion = null;
	@Tag(122) private String mSubject;
	@Tag(123) private String mSystemInformation;
	@Tag(130) private int mTypeId;
}

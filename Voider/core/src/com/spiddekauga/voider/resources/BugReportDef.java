package com.spiddekauga.voider.resources;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.spiddekauga.voider.network.misc.BugReportEntity;
import com.spiddekauga.voider.network.misc.BugReportEntity.BugReportTypes;

import java.util.Date;


/**
 * Bug report to store locally
 */
public class BugReportDef extends Resource {
/** User that's reporting, null if anonymous */
@Tag(113) private String mUserKey;
@Tag(114) @Deprecated private String mLastAction;
@Tag(115) @Deprecated private String mSecondLastAction;
@Tag(119) private String mDescription;
@Tag(120) private Date mDate = new Date();
/** Additional information, such as exception */
@Tag(121) private String mException = null;
@Tag(122) private String mSubject;
@Tag(123) private String mOs;
@Tag(130) private int mTypeId;
@Tag(159) private String mBuildType;
@Tag(160) private String mGameVersion;
@Tag(161) private String mResolution;

/**
 * Creates a bug report resource from a network entity and store it locally
 * @param bugReportEntity entity to set the resource from
 */
public BugReportDef(BugReportEntity bugReportEntity) {
	mUserKey = bugReportEntity.userKey;
	mSubject = bugReportEntity.subject;
	mDescription = bugReportEntity.description;
	mDate = bugReportEntity.date;
	mException = bugReportEntity.exception;
	mOs = bugReportEntity.os;
	mUniqueId = bugReportEntity.id;
	mTypeId = bugReportEntity.type.toId();
	mBuildType = bugReportEntity.buildType;
	mGameVersion = bugReportEntity.gameVersion;
	mResolution = bugReportEntity.resolution;
}

/**
 * Default constructor for kryo
 */
@SuppressWarnings("unused")
protected BugReportDef() {
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
	entity.exception = mException;
	entity.date = mDate;
	entity.os = mOs;
	entity.id = mUniqueId;
	entity.type = BugReportTypes.fromId(mTypeId);
	entity.buildType = mBuildType;
	entity.gameVersion = mGameVersion;
	entity.resolution = mResolution;

	return entity;
}

}

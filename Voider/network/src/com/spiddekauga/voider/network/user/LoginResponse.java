package com.spiddekauga.voider.network.user;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.ISuccessStatuses;
import com.spiddekauga.voider.network.misc.Motd;
import com.spiddekauga.voider.version.Version;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;


/**
 * Response from the login method
 */
public class LoginResponse implements IEntity, ISuccessStatuses {
@Tag(6)
public String username;
@Tag(23)
public String email;
@Tag(7)
public Statuses status = Statuses.FAILED_SERVER_CONNECTION;
/** The private key which can be used to login without a password */
@Tag(8)
public UUID privateKey;
@Tag(9)
public String userKey;
/** Restore date, null if the user doesn't have to restore */
@Tag(10)
public RestoreDate restoreDate;
@Tag(11)
public ArrayList<Motd> motds = new ArrayList<>();
@Tag(12)
public VersionInformation versionInfo = new VersionInformation();

@Override
public boolean isSuccessful() {
	return status != null && status.isSuccessful();
}

/**
 * @return true if we should login on the server
 */
public boolean isServerLoginAvailable() {
	return restoreDate == null && versionInfo.status != VersionInformation.Statuses.UPDATE_REQUIRED;
}

/**
 * Response statuses
 */
public enum Statuses implements ISuccessStatuses {
	/** Successfully logged in */
	SUCCESS,
	/** Failed due to username password mismatch */
	FAILED_USERNAME_PASSWORD_MISMATCH,
	/** Failed due to could not connect to server */
	FAILED_SERVER_CONNECTION,
	/** Internal server error */
	FAILED_SERVER_ERROR,
	/** Failed Server Maintenance */
	FAILED_SERVER_MAINTENANCE,;

	@Override
	public boolean isSuccessful() {
		return name().contains("SUCCESS");
	}
}


/**
 * From and to which date the server was restored
 */
public static class RestoreDate implements IEntity {
	/** Restored from this date */
	@Tag(13)
	public Date from;
	/** Restored to this date */
	@Tag(14)
	public Date to;
}

/**
 * Version Information
 */
public static class VersionInformation implements IEntity {
	@Tag(15)
	public Statuses status = Statuses.UNKNOWN;
	/** All new versions, empty if no new versions were available */
	@Tag(25)
	public List<Version> newVersions = new ArrayList<>();
	/** Download location for desktop clients */
	@Tag(26)
	public String downloadLocationDesktop = null;
	@Deprecated
	@Tag(16)
	private String _unused1;
	@Deprecated
	@Tag(17)
	private String _unused2;

	/**
	 * Client status
	 */
	public enum Statuses {
		/** Client is up to date */
		UP_TO_DATE,
		/** A new version is available, update not required */
		NEW_VERSION_AVAILABLE,
		/** A new version is available and required to login online */
		UPDATE_REQUIRED,
		/** Unknown status */
		UNKNOWN,
	}
}
}



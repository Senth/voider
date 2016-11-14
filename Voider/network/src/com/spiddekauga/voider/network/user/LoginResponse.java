package com.spiddekauga.voider.network.user;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.ISuccessStatuses;
import com.spiddekauga.voider.network.misc.Motd;
import com.spiddekauga.voider.version.Version;


/**
 * Response from the login method

 */
public class LoginResponse implements IEntity, ISuccessStatuses {
	/** Username, the user could log in with email, thus reply with the real username */
	@Tag(6) public String username;
	/** Email of the logged in user */
	@Tag(23) public String email;
	/** If the login was successful */
	@Tag(7) public Statuses status = Statuses.FAILED_SERVER_CONNECTION;
	/** The private key which can be used to login without a password */
	@Tag(8) public UUID privateKey;
	/** User key */
	@Tag(9) public String userKey;
	/** Restore date, null if the user doesn't have to restore */
	@Tag(10) public RestoreDate restoreDate;
	/** Messages of the day */
	@Tag(11) public ArrayList<Motd> motds = new ArrayList<>();
	/** Client version status */
	@Tag(12) public VersionInformation versionInfo = new VersionInformation();

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
	 * From and to which date the server was restored
	 */
	public static class RestoreDate implements IEntity {
		/** Restored from this date */
		@Tag(13) public Date from;
		/** Restored to this date */
		@Tag(14) public Date to;
	}

	/**
	 * Version Information
	 */
	public static class VersionInformation implements IEntity {
		/** Client version status */
		@Tag(15) public Statuses status = Statuses.UNKNOWN;
		/** All new versions, empty if no new versions were available */
		@Tag(25) public List<Version> newVersions = new ArrayList<>();
		/** Download location for desktop clients */
		@Tag(26) public String downloadLocationDesktop = null;

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

		@Deprecated @Tag(16) private String _unused1;
		@Deprecated @Tag(17) private String _unused2;
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
		FAILED_SERVER_MAINTENANCE,

		;
		@Override
		public boolean isSuccessful() {
			return name().contains("SUCCESS");
		}
	}
}

package com.spiddekauga.voider.network.user;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.spiddekauga.voider.ClientVersions;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.ISuccessStatuses;
import com.spiddekauga.voider.network.misc.Motd;


/**
 * Response from the login method
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class LoginResponse implements IEntity, ISuccessStatuses {
	/** Username, the user could log in with email, thus reply with the real username */
	@Tag(6) public String username;
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
		/** Latest client version */
		@Tag(16) public String latestVersion = ClientVersions.getLatest().toString();
		/** Change-log for new versions that are available */
		@Tag(17) public String changeLogMessage = null;

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

		;
		@Override
		public boolean isSuccessful() {
			return name().contains("SUCCESS");
		}
	}
}

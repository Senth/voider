package com.spiddekauga.voider.network.user;

import java.util.ArrayList;
import java.util.UUID;

import com.spiddekauga.voider.ClientVersions;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.ISuccessStatuses;
import com.spiddekauga.voider.network.misc.Motd;


/**
 * Response from the login method
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class LoginResponse implements IEntity, ISuccessStatuses {
	/** Username, the user could log in with email, thus reply with the real username */
	public String username = null;
	/** If the login was successful */
	public Statuses status = Statuses.FAILED_SERVER_CONNECTION;
	/** The private key which can be used to login without a password */
	public UUID privateKey = null;
	/** User key */
	public String userKey = null;
	/** Date format */
	@Deprecated public String dateFormat = null;
	/** Client version status */
	public ClientVersionStatuses clientVersionStatus = ClientVersionStatuses.UNKNOWN;
	/** Latest client version */
	public String latestClientVersion = ClientVersions.getLatest().toString();
	/** Change-log for new versions that are available */
	public String changeLogMessage = null;
	/** Message of the Day */
	public ArrayList<Motd> motds = new ArrayList<>();

	@Override
	public boolean isSuccessful() {
		return status != null && status.isSuccessful();
	}

	/**
	 * Client status
	 */
	public enum ClientVersionStatuses {
		/** Client is up to date */
		UP_TO_DATE,
		/** A new version is available, update not required */
		NEW_VERSION_AVAILABLE,
		/** A new version is available and required to login online */
		UPDATE_REQUIRED,
		/** Unknown status */
		UNKNOWN,
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

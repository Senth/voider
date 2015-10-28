package com.spiddekauga.voider.network.user;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

import com.spiddekauga.voider.ClientVersions;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.ISuccessStatuses;
import com.spiddekauga.voider.network.entities.NetworkEntitySerializer;
import com.spiddekauga.voider.network.misc.Motd;


/**
 * Response from the login method
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class LoginResponse implements IEntity, ISuccessStatuses {
	private static final long serialVersionUID = 2L;
	/** Username, the user could log in with email, thus reply with the real username */
	public String username = null;
	/** If the login was successful */
	public Statuses status = Statuses.FAILED_SERVER_CONNECTION;
	/** The private key which can be used to login without a password */
	public UUID privateKey = null;
	/** User key */
	public String userKey = null;

	/** All extra variables in their original form */
	private transient HashMap<String, IEntity> mObjects = new HashMap<>();
	/** All extra variables serialized */
	private HashMap<String, byte[]> mSerializedObjects = new HashMap<>();


	/**
	 * Serialize extra object before serializing this instance
	 * @param out
	 * @throws IOException
	 */
	private void writeObject(ObjectOutputStream out) throws IOException {
		// Write objects to serialized objects variable
		for (Entry<String, IEntity> entry : mObjects.entrySet()) {
			byte[] serializedEntity = NetworkEntitySerializer.serializeEntity(entry.getValue());
			mSerializedObjects.put(entry.getKey(), serializedEntity);
		}

		out.defaultWriteObject();
	}

	/**
	 * Deserialize extra objects after deserializing this instance
	 * @param in
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();

		for (Entry<String, byte[]> entry : mSerializedObjects.entrySet()) {
			IEntity object = NetworkEntitySerializer.deserializeEntity(entry.getValue());
			if (object != null) {
				mObjects.put(entry.getKey(), object);
			}
		}
	}

	@Override
	public boolean isSuccessful() {
		return status != null && status.isSuccessful();
	}

	/**
	 * @return the message of the day object
	 */
	public MotdContainer motd() {
		MotdContainer motd = (MotdContainer) mObjects.get(MotdContainer.class.getSimpleName());

		if (motd == null) {
			motd = new MotdContainer();
			mObjects.put(MotdContainer.class.getSimpleName(), motd);
		}

		return motd;
	}

	/**
	 * @return client version status
	 */
	public ClientVersionStatus clientVersionStatus() {
		ClientVersionStatus clientVersionStatus = (ClientVersionStatus) mObjects.get(ClientVersionStatus.class.getSimpleName());

		if (clientVersionStatus == null) {
			clientVersionStatus = new ClientVersionStatus();
			mObjects.put(ClientVersionStatus.class.getSimpleName(), clientVersionStatus);
		}

		return clientVersionStatus;
	}

	/**
	 * @return restore dates
	 */
	public RestoreDateContainer restoreDate() {
		RestoreDateContainer restoreDateWrapper = (RestoreDateContainer) mObjects.get(RestoreDateContainer.class.getSimpleName());

		if (restoreDateWrapper == null) {
			restoreDateWrapper = new RestoreDateContainer();
			mObjects.put(RestoreDateContainer.class.getSimpleName(), restoreDateWrapper);
		}

		return restoreDateWrapper;
	}

	/**
	 * Restore dates for the server
	 */
	public static class RestoreDateContainer implements IEntity {
		private static final long serialVersionUID = 1L;

		/** All restore dates from the server */
		public ArrayList<RestoreDate> dates = new ArrayList<>();

		/**
		 * From and to which date the server was restored
		 */
		public static class RestoreDate implements IEntity {
			private static final long serialVersionUID = 1L;

			/** Restored from this date */
			public Date from;
			/** Restored to this date */
			public Date to;
		}
	}

	/**
	 * Contains all message of the days
	 */
	public static class MotdContainer implements IEntity {
		private static final long serialVersionUID = 1L;

		/** Message of the Day */
		public ArrayList<Motd> messages = new ArrayList<>();
	}

	/**
	 * Client version status information
	 */
	public static class ClientVersionStatus implements IEntity {
		private static final long serialVersionUID = 2L;

		/** Client version status */
		public ClientVersionStatuses status = ClientVersionStatuses.UNKNOWN;
		/** Latest client version */
		public String latestClientVersion = ClientVersions.getLatest().toString();
		/** Change-log for new versions that are available */
		public String changeLogMessage = null;

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

package com.spiddekauga.voider.servlets.admin;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.spiddekauga.appengine.DatastoreUtils;
import com.spiddekauga.voider.network.misc.Motd.MotdTypes;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables.CMotd;
import com.spiddekauga.voider.server.util.VoiderController;

/**
 * View, edit and generate new MOTDs for Voider
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class Motd extends VoiderController {

	@Override
	protected void onInit() {
		super.onInit();
		mCurrentDate = new Date();
		mCreateValuesDefault = new CreateValuesDefault();
	}

	@Override
	protected void onRequest() {
		// Add a new message
		if (isParameterSet("title")) {
			if (isAddParamatersOk()) {
				createMotd();
			}
		}
		// Expire
		else if (isParameterSet("expire") && isParameterSet("key")) {
			expireMotd();
		}
		// TODO edit

		setDefaultCreateValues();
		displayExistingMotds();
		forwardToHtml();
	}

	/**
	 * Set default values
	 */
	private void setDefaultCreateValues() {
		getRequest().setAttribute("default", mCreateValuesDefault);
	}

	/**
	 * Check if all parameters are set and valid for creating a MOTD
	 * @return true if all parameters are set and valid for creating a new MOTD
	 */
	private boolean isAddParamatersOk() {
		boolean valid = true;

		// Title
		String title = getParameter("title");
		if (title == null) {
			mCreateValuesDefault.setTitle("", "is null!");
			valid = false;
		} else if (title.length() < 3) {
			mCreateValuesDefault.setTitle(title, "Should be minimum 3 characters");
			valid = false;
		}

		// Message
		String message = getParameter("message");
		if (message == null) {
			mCreateValuesDefault.setMessage("", "is null");
			valid = false;
		} else if (message.length() < 5) {
			mCreateValuesDefault.setMessage(message, "Should be minimum 5 characters");
			valid = false;
		}

		// Level
		String level = getParameter("level");
		if (level == null) {
			mCreateValuesDefault.setLevel("", "is null");
			valid = false;
		} else {
			MotdTypes motdType = MotdTypes.valueOf(level);
			if (motdType == null) {
				mCreateValuesDefault.setLevel(level, "Invalid level");
				valid = false;
			}
		}

		// Expires
		String expireString = getParameter("expires");
		if (expireString == null) {
			mCreateValuesDefault.setExpires("", "is null");
			valid = false;
		} else {
			// Check date format
			try {
				mDateTimeFormatter.parse(expireString);
			} catch (ParseException e) {
				mCreateValuesDefault.setExpires(expireString, "invalid format. Use: " + mDateTimeFormatter.toPattern());
				valid = false;
			}
		}


		return valid;
	}

	/**
	 * Expire the MOTD
	 */
	private void expireMotd() {
		String keyString = getParameter("key");
		try {
			Key key = KeyFactory.stringToKey(keyString);
			Entity entity = DatastoreUtils.getEntity(key);
			if (entity != null) {
				entity.setProperty(CMotd.EXPIRES, new Date());
				DatastoreUtils.put(entity);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create a new MOTD
	 */
	private void createMotd() {
		Entity entity = new Entity(DatastoreTables.MOTD);

		entity.setUnindexedProperty(CMotd.TITLE, getParameter("title"));
		entity.setUnindexedProperty(CMotd.CONTENT, getParameter("message"));
		entity.setUnindexedProperty(CMotd.CREATED, new Date());

		// Level
		MotdTypes motdType = MotdTypes.valueOf(getParameter("level"));
		entity.setUnindexedProperty(CMotd.TYPE, motdType.toId());

		// Expires
		try {
			Date expires = mDateTimeFormatter.parse(getParameter("expires"));
			entity.setProperty(CMotd.EXPIRES, expires);
		} catch (ParseException e) {
			return;
		}


		DatastoreUtils.put(entity);
	}

	/**
	 * Display existing MOTDs
	 */
	private void displayExistingMotds() {
		Query query = new Query(DatastoreTables.MOTD);
		query.addSort(CMotd.EXPIRES, SortDirection.DESCENDING);

		PreparedQuery preparedQuery = DatastoreUtils.prepare(query);
		Iterable<Entity> entities = preparedQuery.asIterable();
		ArrayList<Message> messages = new ArrayList<>();
		HttpServletRequest request = getRequest();

		for (Entity entity : entities) {
			messages.add(new Message(entity));
		}

		request.setAttribute("messages", messages);
	}

	/**
	 * Container for default values and error messages
	 */
	public class CreateValuesDefault {
		/**
		 * @return the title
		 */
		public String getTitle() {
			return mTitle;
		}

		/**
		 * @return the expires
		 */
		public String getExpires() {
			return mExpires;
		}

		/**
		 * @return the level
		 */
		public String getLevel() {
			return mLevel;
		}

		/**
		 * @return the message
		 */
		public String getMessage() {
			return mMessage;
		}

		/**
		 * @param title the title to set
		 * @param error error message
		 */
		void setTitle(String title, String error) {
			mTitle = title;
			mTitleError = error;
			mIsUsed = true;
		}

		/**
		 * @param expires the expires to set
		 * @param error error message
		 */
		void setExpires(String expires, String error) {
			mExpires = expires;
			mExpiresError = error;
			mIsUsed = true;
		}

		/**
		 * @param level the level to set
		 * @param error error message
		 */
		void setLevel(String level, String error) {
			mLevel = level;
			mLevelError = error;
			mIsUsed = true;
		}

		/**
		 * @param message the message to set
		 * @param error error message
		 */
		void setMessage(String message, String error) {
			mMessage = message;
			mMessageError = error;
			mIsUsed = true;
		}

		/**
		 * @return true if some error message is set
		 */
		boolean isUsed() {
			return mIsUsed;
		}

		/**
		 * @return the TitleError
		 */
		public String getTitleError() {
			return mTitleError;
		}

		/**
		 * @return the ExpiresError
		 */
		public String getExpiresError() {
			return mExpiresError;
		}


		/**
		 * @return the LevelError
		 */
		public String getLevelError() {
			return mLevelError;
		}

		/**
		 * @return the MessageError
		 */
		public String getMessageError() {
			return mMessageError;
		}

		private boolean mIsUsed = false;
		private String mTitle = "";
		private String mExpires = mDateTimeFormatter.format(new Date());
		private String mLevel = "";
		private String mMessage = "";
		private String mTitleError = "";
		private String mExpiresError = "";
		private String mLevelError = "";
		private String mMessageError = "";
	}

	/**
	 * Container for MOTDs
	 */
	public class Message {
		/**
		 * Create the message from a datastore entity
		 * @param entity
		 */
		public Message(Entity entity) {
			mKey = KeyFactory.keyToString(entity.getKey());
			mTitle = (String) entity.getProperty(CMotd.TITLE);
			mMessage = (String) entity.getProperty(CMotd.CONTENT);
			mType = DatastoreUtils.getPropertyIdStore(entity, CMotd.TYPE, MotdTypes.class);
			mCreateDate = (Date) entity.getProperty(CMotd.CREATED);
			mExpireDate = (Date) entity.getProperty(CMotd.EXPIRES);
			mExpired = mExpireDate.before(mCurrentDate);
		}

		/**
		 * @return datastore key
		 */
		public String getKey() {
			return mKey;
		}

		/**
		 * @return the title
		 */
		public String getTitle() {
			return mTitle;
		}

		/**
		 * @return the message
		 */
		public String getMessage() {
			return mMessage;
		}

		/**
		 * @return the type
		 */
		public String getType() {
			return mType.toString();
		}

		/**
		 * @return the created date
		 */
		public String getCreateDate() {
			return mDateTimeFormatter.format(mCreateDate);
		}

		/**
		 * @return the expired date
		 */
		public String getExpireDate() {
			return mDateTimeFormatter.format(mExpireDate);
		}

		/**
		 * @return true if this message has expired
		 */
		public boolean getExpired() {
			return mExpired;
		}

		private String mKey;
		private String mTitle;
		private String mMessage;
		private MotdTypes mType;
		private Date mCreateDate;
		private Date mExpireDate;
		private boolean mExpired;
	}

	private SimpleDateFormat mDateTimeFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	private CreateValuesDefault mCreateValuesDefault = null;
	private Date mCurrentDate = null;
}

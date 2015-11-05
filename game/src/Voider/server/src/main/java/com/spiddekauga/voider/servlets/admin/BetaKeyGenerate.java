package com.spiddekauga.voider.servlets.admin;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.spiddekauga.appengine.DatastoreUtils;
import com.spiddekauga.appengine.DatastoreUtils.FilterWrapper;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables.CBetaGroup;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables.CBetaKey;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables.CBetaSignUp;
import com.spiddekauga.voider.server.util.VoiderController;

/**
 * Generate beta keys for voider
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class BetaKeyGenerate extends VoiderController {
	@Override
	protected void onRequest() {
		if (isParameterSet("group") && isParameterSet("count")) {
			generateGroupKeys();
		} else if (isParameterSet("delete")) {
			deleteGroup();
		} else if (isParameterSet("email_list")) {
			sendBetaKeysToEmail();
		}
		displayExistingGroups();
	}

	/**
	 * Generate group beta keys
	 */
	private void generateGroupKeys() {
		String groupName = getParameter("group");
		String count = getParameter("count");

		if (groupName.isEmpty()) {
			setResponseMessage("error", "No group name set!");
		} else if (count.isEmpty()) {
			setResponseMessage("error", "No count set!");
		}

		int cKeysToGenerate = 0;
		try {
			cKeysToGenerate = Integer.parseInt(count);
		} catch (NumberFormatException e) {
			return;
		}

		// Does group exist?
		Key groupKey = DatastoreUtils.getSingleKey(DatastoreTables.BETA_GROUP, new FilterWrapper(CBetaGroup.NAME, groupName));

		// Create group if it doesn't exist
		if (groupKey == null) {
			Entity groupEntity = new Entity(DatastoreTables.BETA_GROUP);

			String hash = toBase64(UUID.randomUUID());

			groupEntity.setProperty(CBetaGroup.NAME, groupName);
			groupEntity.setProperty(CBetaGroup.HASH, hash);

			groupKey = DatastoreUtils.put(groupEntity);
			if (groupKey == null) {
				return;
			}
		}


		// Create new beta keys for the group
		ArrayList<Entity> betaKeys = new ArrayList<>();
		for (int i = 0; i < cKeysToGenerate; i++) {
			Entity betaKeyEntity = new Entity(DatastoreTables.BETA_KEY, groupKey);

			betaKeyEntity.setProperty(CBetaKey.KEY, toBase64(UUID.randomUUID()));
			betaKeyEntity.setProperty(CBetaKey.USED, false);

			betaKeys.add(betaKeyEntity);
		}
		DatastoreUtils.put(betaKeys);
	}

	/**
	 * Display existing groups
	 */
	private void displayExistingGroups() {
		Iterable<Entity> entities = DatastoreUtils.getEntities(DatastoreTables.BETA_GROUP);
		ArrayList<Group> groups = new ArrayList<>();
		HttpServletRequest request = getRequest();

		for (Entity entity : entities) {
			String name = (String) entity.getProperty(CBetaGroup.NAME);
			String hash = (String) entity.getProperty(CBetaGroup.HASH);

			if (name != null && hash != null) {
				try {
					name = URLEncoder.encode(name, "UTF-8");
					hash = URLEncoder.encode(hash, "UTF-8");
					groups.add(new Group(name, hash));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
		}

		request.setAttribute("groups", groups);
		try {
			request.getRequestDispatcher("generate-beta-keys.jsp").forward(request, getResponse());
		} catch (ServletException | IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Delete group
	 */
	private void deleteGroup() {
		Key groupKey = DatastoreUtils.getSingleKey(DatastoreTables.BETA_GROUP, new FilterWrapper(CBetaGroup.NAME, getParameter("delete")));

		if (groupKey != null) {
			List<Key> deleteList = DatastoreUtils.getKeys(DatastoreTables.BETA_KEY, groupKey);
			DatastoreUtils.delete(deleteList);
			DatastoreUtils.delete(groupKey);
			setResponseMessage("success", "Deleted group " + getParameter("delete") + " with " + deleteList.size() + " beta keys.");
		} else {
			setResponseMessage("error", "Failed to find group (" + getParameter("delete") + ")");
		}
	}

	/**
	 * Send beta keys to specified emails
	 */
	private void sendBetaKeysToEmail() {
		Key groupKey = getBetaGroupForEmailList();
		if (groupKey != null) {
			ArrayList<String> emailList = getEmailList();

			for (String email : emailList) {
				sendBetaKeyToEmail(groupKey, email);
			}

			setResponseMessage("success", "Sent " + emailList.size() + " beta keys");

		} else {
			setResponseMessage("error", "Couldn't find group key");
		}
	}

	/**
	 * Send beta key to the specified email
	 * @param groupKey beta group key
	 * @param email the email to send beta key to
	 */
	private void sendBetaKeyToEmail(Key groupKey, String email) {
		Entity userSignupEntity = getUserSignupEntity(email);

		boolean sendEmail = true;
		String betaKey = null;

		// User hasn't signed up for beta key, add to list
		if (userSignupEntity == null) {
			userSignupEntity = new Entity(DatastoreTables.BETA_SIGNUP);

			BetaKey wrapBetaKey = createBetaKey(groupKey);
			betaKey = wrapBetaKey.hashKey;
			userSignupEntity.setProperty(CBetaSignUp.BETA_KEY, wrapBetaKey.datastoreKey);
			userSignupEntity.setProperty(CBetaSignUp.DATE, new Date());
			userSignupEntity.setProperty(CBetaSignUp.EMAIL, email);

			DatastoreUtils.put(userSignupEntity);
		}
		// User hasn't gotten a beta key yet
		else if (!userSignupEntity.hasProperty(CBetaSignUp.BETA_KEY)) {
			BetaKey wrapBetaKey = createBetaKey(groupKey);
			betaKey = wrapBetaKey.hashKey;
			userSignupEntity.setProperty(CBetaSignUp.BETA_KEY, wrapBetaKey.datastoreKey);
			userSignupEntity.removeProperty(CBetaSignUp.CONFIRM_EXPIRES);
			userSignupEntity.removeProperty(CBetaSignUp.CONFIRM_KEY);

			DatastoreUtils.put(userSignupEntity);
		}
		// User has already a key. Skip...
		else {
			sendEmail = false;
		}

		if (sendEmail) {
			String body;

			body = "Hi! :D<br/><br/>";
			body += "You've receieved a beta key for Voider. You can use this for yourself or give it to a friend if you like. Note that if you give the beta key to your friend you won't be able to signup for another beta key to this email.<br/><br/>";

			body += "You can find download instructions <a href=\"http://voider-game.com>here</a>.";

			body += "<br /><br />";
			body += "-----------------------------------------<br />";
			body += betaKey;
			body += "-----------------------------------------<br /><br />";

			sendEmail(email, "You've received a Voider beta key!", body);
		}
	}

	/**
	 * Create a beta key for the email list
	 * @param groupKey
	 * @return key of the beta key entity
	 */
	private BetaKey createBetaKey(Key groupKey) {
		Entity betaKey = new Entity(DatastoreTables.BETA_KEY, groupKey);

		String hashKey = toBase64(UUID.randomUUID());
		betaKey.setProperty(CBetaKey.KEY, hashKey);
		betaKey.setProperty(CBetaKey.USED, false);

		Key datastoreKey = DatastoreUtils.put(betaKey);

		return new BetaKey(datastoreKey, hashKey);
	}

	private class BetaKey {
		public BetaKey(Key datastoreKey, String hashKey) {
			this.datastoreKey = datastoreKey;
			this.hashKey = hashKey;
		}

		Key datastoreKey;
		String hashKey;
	}

	/**
	 * Get the user if it has already signed up for beta key
	 * @param email
	 * @return user sign-up datastore entity
	 */
	private Entity getUserSignupEntity(String email) {
		return DatastoreUtils.getSingleEntity(DatastoreTables.BETA_SIGNUP, new FilterWrapper(CBetaSignUp.EMAIL, email.toLowerCase(Locale.ENGLISH)));
	}


	/**
	 * Get Email List beta key group
	 * @return beta group for email list
	 */
	private Key getBetaGroupForEmailList() {
		Key groupKey = DatastoreUtils.getSingleKey(DatastoreTables.BETA_GROUP, new FilterWrapper(CBetaGroup.NAME, EMAIL_LIST_GROUP_NAME));

		if (groupKey == null) {
			Entity entity = new Entity(DatastoreTables.BETA_GROUP);

			entity.setProperty(CBetaGroup.NAME, EMAIL_LIST_GROUP_NAME);
			entity.setProperty(CBetaGroup.HASH, toBase64(UUID.randomUUID()));

			groupKey = DatastoreUtils.put(entity);
		}

		return groupKey;
	}

	/**
	 * Converts the email list parameter into a real list
	 * @return list with various email addresses
	 */
	private ArrayList<String> getEmailList() {
		ArrayList<String> emailList = new ArrayList<>();

		String emailString = getParameter("email_list");
		String[] splittedEmails = emailString.split("\n");

		// Cleanup splitted emails and add to the email list
		for (String splitEmail : splittedEmails) {
			String trimmed = splitEmail.trim().toLowerCase(Locale.ENGLISH);
			if (trimmed.contains("@") && trimmed.contains(".")) {
				emailList.add(trimmed);
			}
		}

		return emailList;
	}

	/**
	 * Beta key group
	 */
	public class Group {
		/**
		 * New group, automatically creates the link
		 * @param name
		 * @param hash
		 */
		public Group(String name, String hash) {
			this.name = name;
			this.hash = hash;
			this.link = getRootUrl() + "beta-keys?group=" + name + "&hash=" + hash;
		}

		/**
		 * @return the name
		 */
		public String getName() {
			return name;
		}

		/**
		 * @return the hash
		 */
		public String getHash() {
			return hash;
		}

		/**
		 * @return the link
		 */
		public String getLink() {
			return link;
		}

		/** Name of the beta key group */
		private String name;
		/** Hash to identify the group */
		private String hash;
		/** Link to the group */
		private String link;
	}

	private final static String EMAIL_LIST_GROUP_NAME = "Email List";
}

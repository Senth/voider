package com.spiddekauga.voider.servlets.admin;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
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
		}
		displayExistingGroups();
	}

	/**
	 * Generate group beta keys
	 */
	private void generateGroupKeys() {
		String groupName = getParameter("group");
		String count = getParameter("count");
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
}

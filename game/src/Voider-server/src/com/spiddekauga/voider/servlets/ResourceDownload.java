package com.spiddekauga.voider.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import javax.servlet.ServletException;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.spiddekauga.appengine.DatastoreUtils;
import com.spiddekauga.appengine.DatastoreUtils.FilterWrapper;
import com.spiddekauga.voider.network.entities.ChatMessage;
import com.spiddekauga.voider.network.entities.ChatMessage.MessageTypes;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.ResourceBlobEntity;
import com.spiddekauga.voider.network.entities.UploadTypes;
import com.spiddekauga.voider.network.entities.method.IMethodEntity;
import com.spiddekauga.voider.network.entities.method.ResourceDownloadMethod;
import com.spiddekauga.voider.network.entities.method.ResourceDownloadMethodResponse;
import com.spiddekauga.voider.network.entities.method.ResourceDownloadMethodResponse.Statuses;
import com.spiddekauga.voider.server.util.ResourceUtils;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables;
import com.spiddekauga.voider.server.util.VoiderServlet;

/**
 * Downloads resources
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class ResourceDownload extends VoiderServlet {
	@Override
	public void init() {
		mResponse.status = Statuses.FAILED_SERVER_INTERAL;
		mResponse.resources.clear();
		mAddedResources.clear();
	}

	@Override
	protected IEntity onRequest(IMethodEntity methodEntity) throws ServletException, IOException {
		init();

		if (mUser.isLoggedIn()) {
			boolean success = false;
			if (methodEntity instanceof ResourceDownloadMethod) {
				success = setInformationAndDependenciesToResponse(((ResourceDownloadMethod) methodEntity).resourceId);
			}

			// Set download date and send sync message
			if (success && !mAddedResources.isEmpty()) {
				setUserDownloadDate();
				mResponse.status = Statuses.SUCCESS;
				sendMessage(new ChatMessage<>(MessageTypes.SYNC_COMMUNITY_DOWNLOAD, mUser.getClientId()));
			}
		} else {
			mResponse.status = Statuses.FAILED_USER_NOT_LOGGED_IN;
		}

		return mResponse;
	}

	/**
	 * Set download date for the specified user so these resources can be synchronized
	 * between devices.
	 */
	private void setUserDownloadDate() {
		for (Key key : mAddedResources) {
			// Create entity if user hasn't downloaded the resource before
			if (!DatastoreUtils.exists("sync_published", mUser.getKey(), new FilterWrapper("published_key", key))) {
				Entity entity = new Entity("sync_published", mUser.getKey());
				entity.setProperty("published_key", key);
				entity.setProperty("download_date", new Date());

				DatastoreUtils.put(entity);
			}
		}
	}

	/**
	 * Get information about the specified resource and its dependencies
	 * @param resourceId id of the resource to get information from and set to the
	 *        response
	 * @return true if successfully added information and dependencies
	 */
	private boolean setInformationAndDependenciesToResponse(UUID resourceId) {
		// Get resource key
		Key resourceKey = DatastoreUtils.getSingleKey(DatastoreTables.PUBLISHED.toString(), new FilterWrapper("resource_id", resourceId));
		if (resourceKey != null) {
			return setInformationAndDependenciesToResponse(resourceKey);
		}
		return false;
	}

	/**
	 * Get information about the specified resource and its dependencies
	 * @param resourceKey key of the resource to get information from and set to the
	 *        response
	 * @return true if successfully added information and dependencies
	 */
	private boolean setInformationAndDependenciesToResponse(Key resourceKey) {
		if (!mAddedResources.contains(resourceKey)) {
			mAddedResources.add(resourceKey);
			Entity resource = DatastoreUtils.getEntityByKey(resourceKey);
			if (resource == null) {
				return false;
			}

			// Information
			ResourceBlobEntity information = ResourceUtils.getBlobInformation(resource);
			if (information == null) {
				return false;
			}
			mResponse.resources.add(information);

			// Level def? Then add level resource
			if (information.uploadType == UploadTypes.LEVEL_DEF) {
				mResponse.resources.add(ResourceUtils.getBlobLevelInformation(resource));
			}

			// Dependencies
			List<Key> dependencies = getDependencies(resourceKey);
			if (dependencies == null) {
				return false;
			}
			// Recursive for all dependencies
			for (Key dependency : dependencies) {
				boolean success = setInformationAndDependenciesToResponse(dependency);

				if (!success) {
					return false;
				}
			}
		}

		return true;
	}


	/**
	 * @param resourceKey get all dependencies of the specified key
	 * @return all dependencies of the specified resource
	 */
	private List<Key> getDependencies(Key resourceKey) {
		Iterable<Entity> dependencies = DatastoreUtils.getEntities(DatastoreTables.DEPENDENCY.toString(), resourceKey);
		ArrayList<Key> keys = new ArrayList<>();

		for (Entity dependency : dependencies) {
			keys.add((Key) dependency.getProperty("dependency"));
		}

		return keys;
	}

	/** Method response */
	private ResourceDownloadMethodResponse mResponse = new ResourceDownloadMethodResponse();
	/** All added resources */
	private HashSet<Key> mAddedResources = new HashSet<>();
}

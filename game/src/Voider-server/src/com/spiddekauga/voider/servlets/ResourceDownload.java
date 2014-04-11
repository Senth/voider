package com.spiddekauga.voider.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.spiddekauga.appengine.DatastoreUtils;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.ResourceBlobEntity;
import com.spiddekauga.voider.network.entities.UploadTypes;
import com.spiddekauga.voider.network.entities.method.NetworkEntitySerializer;
import com.spiddekauga.voider.network.entities.method.ResourceDownloadMethod;
import com.spiddekauga.voider.network.entities.method.ResourceDownloadMethodResponse;
import com.spiddekauga.voider.network.entities.method.ResourceDownloadMethodResponse.Statuses;
import com.spiddekauga.voider.server.util.NetworkGateway;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables;
import com.spiddekauga.voider.server.util.VoiderServlet;

/**
 * Downloads resources
 * 
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
	protected void onRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		byte[] byteEntity = NetworkGateway.getEntity(request);
		IEntity networkEntity = NetworkEntitySerializer.deserializeEntity(byteEntity);

		init();

		if (networkEntity instanceof ResourceDownloadMethod) {
			setInformationAndDependenciesToResponse(((ResourceDownloadMethod) networkEntity).resourceId);
		}

		if (!mResponse.resources.isEmpty()) {
			mResponse.status = Statuses.SUCCESS;
		}


		byte[] byteResponse = NetworkEntitySerializer.serializeEntity(mResponse);
		NetworkGateway.sendResponse(response, byteResponse);
	}

	/**
	 * Get information about the specified resource and its dependencies
	 * @param resourceId id of the resource to get information from and set
	 * to the response
	 * @return true if successfully added information and dependencies
	 */
	private boolean setInformationAndDependenciesToResponse(UUID resourceId) {
		// Get resource key
		Key resourceKey = DatastoreUtils.getSingleKey(DatastoreTables.PUBLISHED.toString(), "resource_id", resourceId);
		if (resourceKey != null) {
			return setInformationAndDependenciesToResponse(resourceKey);
		}
		return false;
	}

	/**
	 * Get information about the specified resource and its dependencies
	 * @param resourceKey key of the resource to get information from and set
	 * to the response
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
			ResourceBlobEntity information = getInformation(resource);
			if (information == null) {
				return false;
			}
			mResponse.resources.add(information);

			// Level def? Then add level resource
			if (information.uploadType == UploadTypes.LEVEL_DEF) {
				mResponse.resources.add(getLevelInformation(resource));
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
	 * @param levelDef the level def to get information from
	 * @return information about the actual level
	 */
	private ResourceBlobEntity getLevelInformation(Entity levelDef) {
		ResourceBlobEntity information = new ResourceBlobEntity();

		information.resourceId = DatastoreUtils.getUuidProperty(levelDef, "level_id");
		information.blobKey = ((BlobKey) levelDef.getProperty("level_blob_key")).getKeyString();
		information.uploadType = UploadTypes.LEVEL;

		return information;
	}

	/**
	 * @param resource get information from the specified resource
	 * @return information about the resource
	 */
	private ResourceBlobEntity getInformation(Entity resource) {
		ResourceBlobEntity information = new ResourceBlobEntity();

		information.resourceId = DatastoreUtils.getUuidProperty(resource, "resource_id");
		information.blobKey = ((BlobKey) resource.getProperty("blob_key")).getKeyString();
		long defTypeId = (long) resource.getProperty("type");
		information.uploadType = UploadTypes.fromId((int) defTypeId);

		return information;
	}

	/**
	 * @param resourceKey get all dependencies of the specified key
	 * @return all dependencies of the specified resource
	 */
	private List<Key> getDependencies(Key resourceKey) {
		List<Entity> dependencies = DatastoreUtils.getEntities(DatastoreTables.DEPENDENCY.toString(), resourceKey);
		ArrayList<Key> keys = new ArrayList<>();

		for (Entity dependency : dependencies) {
			keys.add((Key)dependency.getProperty("dependency"));
		}

		return keys;
	}

	/** Method response */
	private ResourceDownloadMethodResponse mResponse = new ResourceDownloadMethodResponse();
	/** All added resources */
	private HashSet<Key> mAddedResources = new HashSet<>();
}

package com.spiddekauga.voider.repo;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.resources.Def;
import com.spiddekauga.voider.resources.ExternalTypes;
import com.spiddekauga.voider.resources.IResource;
import com.spiddekauga.voider.resources.IResourceRevision;
import com.spiddekauga.voider.resources.ResourceNotFoundException;
import com.spiddekauga.voider.resources.RevisionInfo;

/**
 * Local repository to all resources
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class ResourceLocalRepo {
	/**
	 * Add a resource
	 * @param resource the resource to add
	 */
	public static void add(IResource resource) {
		// Add if resource doesn't exist
		if (!mSqliteGateway.exists(resource.getId())) {
			mSqliteGateway.add(resource.getId(), ExternalTypes.getEnumFromType(resource.getClass()).getId());
		}

		// Add revision
		if (resource instanceof IResourceRevision) {
			Date date = null;
			if (resource instanceof Def) {
				date = ((Def) resource).getDate();
			}

			mSqliteGateway.addRevision(resource.getId(), ((IResourceRevision) resource).getRevision(), date);
		}
	}

	/**
	 * Removes a resource (including all revisions) from the database and physically
	 * @param resourceId the unique id of the resource
	 */
	public static void remove(UUID resourceId) {
		// Remove from database
		mSqliteGateway.remove(resourceId);

		// Remove file
		String filepath = getFilepath(resourceId);
		FileHandle file = Gdx.files.external(filepath);
		if (file.exists()) {
			file.delete();
		}

		// Remove revisions
		removeRevisions(resourceId);
	}

	/**
	 * Removes all resources of the specified type (including all revisions)
	 * @param externalType the resource type to remove
	 */
	void removeAll(ExternalTypes externalType) {
		mSqliteGateway.removeAll(externalType.getId());
	}

	/**
	 * Remove all revisions of a resource
	 * @param resourceId unique id of the resource
	 */
	public static void removeRevisions(UUID resourceId) {
		// Database
		mSqliteGateway.removeRevisions(resourceId);

		// File
		String revisionDir = getRevisionDir(resourceId);
		FileHandle dir = Gdx.files.external(revisionDir);
		if (dir.exists() && dir.isDirectory()) {
			dir.deleteDirectory();
		}
	}

	/**
	 * Get all of the specified type
	 * @param externalType the resource type to get all resource of
	 * @return all resources of the specified type. Don't forget to free the arraylist!
	 */
	public static ArrayList<UUID> getAll(ExternalTypes externalType) {
		return mSqliteGateway.getAll(externalType.getId());
	}

	/**
	 * Get the count of all resources of the specified type
	 * @param externalType resource type to calculate the number of
	 * @return number of resources of the specified type
	 */
	public static int getCount(ExternalTypes externalType) {
		return mSqliteGateway.getCount(externalType.getId());
	}

	/**
	 * Get all revisions of the specified resource
	 * @param resourceId the resource to get the revisions for
	 * @return all revisions
	 */
	public static ArrayList<RevisionInfo> getRevisions(UUID resourceId) {
		return mSqliteGateway.getRevisions(resourceId);
	}

	/**
	 * Get the latest revision of the specified resource
	 * @param uuid the resource to get the revision for
	 * @return latest revision of the specified resource.
	 * @throws ResourceNotFoundException if the resource doesn't got any revisions.
	 * I.e. it either does not exist, does not contain any revisions because it's either not
	 * a revision resource or it has been published.
	 */
	public static RevisionInfo getRevisionLatest(UUID uuid) {
		return mSqliteGateway.getRevisionLatest(uuid);
	}

	/**
	 * @param resource the resource to get the filepath from
	 * @return filepath of the resource
	 */
	public static String getFilepath(IResource resource) {
		return getFilepath(resource.getId());
	}

	/**
	 * @param resourceId the resource to get the filepath from
	 * @return filepath of the resource
	 */
	public static String getFilepath(UUID resourceId) {
		return getDir() + resourceId;
	}

	/**
	 * @param resourceId id of the resource to get resource revision directory for
	 * @return directory where the resource's revisions are located
	 */
	private static String getRevisionDir(UUID resourceId) {
		return getDir() + resourceId + REVISION_DIR_POSTFIX;
	}

	/**
	 * @param resource the resource revision to get the revision filepath from
	 * @return filepath of the specific resource revision
	 */
	public static String getRevisionFilepath(IResourceRevision resource) {
		return getRevisionFilepath(resource.getId(), resource.getRevision());
	}

	/**
	 * @param resourceId id of the resource revision to get the filepath from
	 * @param revision the specific revision file to get
	 * @return filepath to the specific revision
	 */
	public static String getRevisionFilepath(UUID resourceId, int revision) {
		return getRevisionDir(resourceId) + getRevisionFormat(revision);
	}

	/**
	 * @return resource directory
	 */
	private static String getDir() {
		return Config.File.STORAGE + "resources/";
	}

	/**
	 * @param revision the revision to get the format of
	 * @return revision file format from the revision
	 */
	private static String getRevisionFormat(int revision) {
		return String.format("%010d", revision);
	}

	/**
	 * @param resourceId the resource to get the type for
	 * @return type of the resource id
	 */
	public static ExternalTypes getType(UUID resourceId) {
		int type = mSqliteGateway.getType(resourceId);
		if (type != -1) {
			return ExternalTypes.getEnumFromId(type);
		} else {
			return null;
		}
	}

	/**
	 * @param resourceId the resource to test if it's published
	 * @return true if the resource is published
	 * @throws ResourceNotFoundException if the resource wasn't found
	 */
	public static boolean isPublished(UUID resourceId) {
		return mSqliteGateway.isPublished(resourceId);
	}

	/**
	 * Set a resource as published / unpublished
	 * @param resourceId the resource id to set
	 * @param published true if published, false if unpublished
	 */
	static void setPublished(UUID resourceId, boolean published) {
		mSqliteGateway.setPublished(resourceId, published);
	}

	/** SQLite gateway */
	private static ResourceSqliteGateway mSqliteGateway = new ResourceSqliteGateway();
	/** Revision postfix */
	private static final String REVISION_DIR_POSTFIX = "_revs/";
}

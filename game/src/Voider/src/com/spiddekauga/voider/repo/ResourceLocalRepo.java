package com.spiddekauga.voider.repo;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.resources.Def;
import com.spiddekauga.voider.resources.ExternalTypes;
import com.spiddekauga.voider.resources.IResource;
import com.spiddekauga.voider.resources.IResourceRevision;
import com.spiddekauga.voider.resources.RevisionInfo;

/**
 * Local repository to all resources
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
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
	 * Removes a resource (including all revisions) from the database.
	 * @param uuid the unique id of the resource
	 */
	public static void remove(UUID uuid) {
		mSqliteGateway.remove(uuid);
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
	 * @param uuid unique id of the resource
	 */
	public static void removeRevisions(UUID uuid) {
		mSqliteGateway.removeRevisions(uuid);
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
	 * @param uuid the resource to get the revisions for
	 * @return all revisions
	 */
	public static ArrayList<RevisionInfo> getRevisions(UUID uuid) {
		return mSqliteGateway.getRevisions(uuid);
	}

	/**
	 * Get the latest revision of the specified resource
	 * @param uuid the resource to get the revision for
	 * @return latest revision of the specified resource. If the resource
	 * doesn't have any revisions null is returned.
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
		return getDir() + resourceId + "_revs/" + getRevisionFormat(revision);
	}

	/**
	 * @return resource directory
	 */
	private static String getDir() {
		return Config.Debug.JUNIT_TEST ? "Voider-test/resources/" : "Voider/resources/";
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

	/** Sqlite gateway */
	private static ResourceSqliteGateway mSqliteGateway = new ResourceSqliteGateway();
}

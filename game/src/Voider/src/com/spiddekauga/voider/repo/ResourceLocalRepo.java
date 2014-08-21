package com.spiddekauga.voider.repo;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import com.spiddekauga.voider.network.entities.ResourceRevisionEntity;
import com.spiddekauga.voider.network.entities.RevisionEntity;
import com.spiddekauga.voider.resources.Def;
import com.spiddekauga.voider.resources.IResource;
import com.spiddekauga.voider.resources.IResourceHasDef;
import com.spiddekauga.voider.resources.IResourceRevision;
import com.spiddekauga.voider.utils.Pools;

/**
 * Local repository to all resources
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class ResourceLocalRepo {
	/**
	 * Private constructor to enforce singleton usage
	 */
	private ResourceLocalRepo() {
		// Does nothing
	}

	/**
	 * Saves a resource
	 * @param resource the resource to save
	 * @return true if the resource was saved
	 */
	public static boolean save(IResource resource) {
		// Save old values if save is unsuccessful
		Date oldDate = null;
		int oldRevision = -1;

		// Update date
		if (resource instanceof Def) {
			oldDate = ((Def) resource).getDate();
			((Def) resource).updateDate();
		}

		// Update revision
		if (resource instanceof IResourceRevision) {
			oldRevision = ((IResourceRevision) resource).getRevision();

			int nextRevision = 1;
			try {
				RevisionEntity revisionInfo = getRevisionLatest(resource.getId());
				nextRevision = revisionInfo.revision + 1;
			} catch (ResourceNotFoundException e) {
				// Do nothing
			}
			((IResourceRevision) resource).setRevision(nextRevision);
		}


		// File
		boolean success = mFileGateway.save(resource);


		// Add to the database
		if (success) {
			add(resource);
		}
		// Failed -> reset date and revision
		else {
			if (resource instanceof Def) {
				((Def) resource).setDate(oldDate);
			}

			if (resource instanceof IResourceRevision) {
				((IResourceRevision) resource).setRevision(oldRevision);
			}
		}

		return success;
	}

	/**
	 * Add a resource
	 * @param resource the resource to add
	 */
	private static void add(IResource resource) {
		// Add if resource doesn't exist
		if (!mSqliteGateway.exists(resource.getId())) {
			mSqliteGateway.add(resource.getId(), ExternalTypes.fromType(resource.getClass()).getId());
		}

		// Add revision
		if (resource instanceof IResourceRevision) {
			Date date = null;
			if (resource instanceof Def) {
				date = ((Def) resource).getDate();
			} else if (resource instanceof IResourceHasDef) {
				date = ((IResourceHasDef) resource).getDef().getDate();
			}

			mSqliteGateway.addRevision(resource.getId(), ((IResourceRevision) resource).getRevision(), date);
		}
	}

	/**
	 * Add user resource revisions. Automatically updates the latest revision file
	 * location.
	 * @param resourceId id of the user resource revision to add
	 * @param type the resource type
	 * @param revisions new revisions to add
	 */
	static void addRevisions(UUID resourceId, ExternalTypes type, ArrayList<RevisionEntity> revisions) {
		// Add resource if it doesn't exist
		if (!exists(resourceId)) {
			mSqliteGateway.add(resourceId, type.getId());
		}

		// Add revisions
		for (RevisionEntity revisionEntity : revisions) {
			mSqliteGateway.addRevision(resourceId, revisionEntity.revision, revisionEntity.date);
			mSqliteGateway.setSyncedUserResource(resourceId, revisionEntity.revision);
		}

		// Update latest revision file location
		int latestRevision = mSqliteGateway.getRevisionLatest(resourceId).revision;
		mFileGateway.copyFromRevisionToResource(resourceId, latestRevision);
	}

	/**
	 * Add a downloaded / published resource
	 * @param resourceId the id of the downloaded resource
	 * @param type the type of resource
	 */
	static void addDownloaded(UUID resourceId, ExternalTypes type) {
		// Only add the resource if it doesn't exist
		if (!mSqliteGateway.exists(resourceId)) {
			mSqliteGateway.add(resourceId, type.getId());
			mSqliteGateway.setPublished(resourceId, true);
		}
	}

	/**
	 * Removes a resource (including all revisions) from the database and physically.
	 * Equivalent of calling remove(resourceId, false), i.e. does not add the resource to
	 * the removed DB
	 * @param resourceId the unique id of the resource
	 */
	public static void remove(UUID resourceId) {
		remove(resourceId, false);
	}

	/**
	 * Removes a resource (including all revisions) from the database and physically
	 * @param resourceId the unique id of the resource
	 * @param addToRemovedDb set to true to add this resources to the removed DB
	 */
	public static void remove(UUID resourceId, boolean addToRemovedDb) {
		// Unload first
		ResourceCacheFacade.unload(resourceId);

		mSqliteGateway.remove(resourceId);
		mFileGateway.remove(resourceId);

		removeRevisions(resourceId);

		if (addToRemovedDb) {
			mSqliteGateway.addAsRemoved(resourceId);
		}
	}

	/**
	 * @return all resources from the removed resource table. I.e. the table that holds
	 *         all resources that have been removed
	 */
	static ArrayList<UUID> getRemovedResources() {
		return mSqliteGateway.getRemovedResources();
	}

	/**
	 * Remove the resource from the removed resource table. I.e. a table that holds all
	 * resources that have been removed
	 * @param resourceId unique id of the resource to remove
	 */
	static void removeFromRemoved(UUID resourceId) {
		mSqliteGateway.removeFromRemoved(resourceId);
	}

	/**
	 * Removes all resources of the specified type (including all revisions)
	 * @param externalType the resource type to remove
	 */
	public static void removeAll(ExternalTypes externalType) {
		removeAll(externalType, false);
	}

	/**
	 * Removes all resources of the specified type (including all revisions)
	 * @param externalType the resource type to remove
	 * @param addToRemovedDb set to true if the resources should be set as removed
	 */
	public static void removeAll(ExternalTypes externalType, final boolean addToRemovedDb) {
		// File
		ArrayList<UUID> resources = mSqliteGateway.getAll(externalType.getId());
		for (UUID resource : resources) {
			// Unload
			ResourceCacheFacade.unload(resource);

			mFileGateway.remove(resource);
			mFileGateway.removeRevisionDir(resource);
		}
		Pools.arrayList.free(resources);


		// Database
		mSqliteGateway.removeAll(externalType.getId(), addToRemovedDb);
	}

	/**
	 * Remove all revisions of a resource
	 * @param resourceId unique id of the resource
	 */
	static void removeRevisions(UUID resourceId) {
		mSqliteGateway.removeRevisions(resourceId);
		mFileGateway.removeRevisionDir(resourceId);
	}

	/**
	 * Remove the specified revision and all later ones for the specified resource
	 * @param resourceId the resource to remove revisions from
	 * @param fromRevision remove all revisions from this one
	 */
	static void removeRevisions(UUID resourceId, int fromRevision) {
		mSqliteGateway.removeRevisions(resourceId, fromRevision);
		mFileGateway.removeRevisions(resourceId, fromRevision);
	}

	/**
	 * Get all of the specified type
	 * @param externalType the resource type to get all resource of
	 * @return all resources of the specified type. Don't forget to free the ArrayList!
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
	public static ArrayList<RevisionEntity> getRevisions(UUID resourceId) {
		return mSqliteGateway.getRevisions(resourceId);
	}

	/**
	 * Get the latest revision of the specified resource
	 * @param uuid the resource to get the revision for
	 * @return latest revision of the specified resource.
	 * @throws ResourceNotFoundException if the resource doesn't got any revisions. I.e.
	 *         it either does not exist, does not contain any revisions because it's
	 *         either not a revision resource or it has been published.
	 */
	public static RevisionEntity getRevisionLatest(UUID uuid) {
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
		return mFileGateway.getFilepath(resourceId);
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
		return mFileGateway.getRevisionFilepath(resourceId, revision);
	}

	/**
	 * @param resourceId the resource to get the type for
	 * @return type of the resource id
	 */
	public static ExternalTypes getType(UUID resourceId) {
		int type = mSqliteGateway.getType(resourceId);
		if (type != -1) {
			return ExternalTypes.fromId(type);
		} else {
			return null;
		}
	}

	/**
	 * @param resourceId checks if this resource exists
	 * @return true if the resource exists
	 */
	public static boolean exists(UUID resourceId) {
		return mSqliteGateway.exists(resourceId);
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

	/**
	 * Set last sync date of published/downloaded resources
	 * @param lastSync date when synced published/downloaded resources the last time
	 */
	public static void setSyncDownloadDate(Date lastSync) {
		mPrefsGateway.setSyncDownloadDate(lastSync);
	}

	/**
	 * @return last sync date of published/downloaded resources
	 */
	public static Date getSyncDownloadDate() {
		return mPrefsGateway.getSyncDownloadDate();
	}

	/**
	 * Set last sync date of user resource revisions
	 * @param lastSync date when synced user resource revisions
	 */
	public static void setSyncUserResourceDate(Date lastSync) {
		mPrefsGateway.setSyncUserResourceDate(lastSync);
	}

	/**
	 * @return last sync date of user resource revisions
	 */
	public static Date getSyncUserResourceDate() {
		return mPrefsGateway.getSyncUserResourceDate();
	}

	/**
	 * @return all user resource revisions that haven't been uploaded/synced to the
	 *         server.
	 */
	static HashMap<UUID, ResourceRevisionEntity> getUnsyncedUserResources() {
		return mSqliteGateway.getUnsyncedUserResources();
	}

	/**
	 * Set the user resource revision as synced/uploaded
	 * @param resourceId the resource to set
	 * @param revision the specific revision to set as synced/uploaded
	 */
	static void setSyncedUserResource(UUID resourceId, int revision) {
		mSqliteGateway.setSyncedUserResource(resourceId, revision);
	}

	/**
	 * Set the user resource revision as synced/uploaded
	 * @param resourceId the resource to set
	 * @param from from this revision
	 * @param to to and including this revision
	 */
	static void setSyncedUserResource(UUID resourceId, int from, int to) {
		mSqliteGateway.setSyncedUserResource(resourceId, from, to);
	}

	/** File gateway */
	private static ResourceFileGateway mFileGateway = new ResourceFileGateway();
	/** Preferences gateway */
	private static ResourcePrefsGateway mPrefsGateway = new ResourcePrefsGateway();
	/** SQLite gateway */
	private static ResourceSqliteGateway mSqliteGateway = new ResourceSqliteGateway();
}

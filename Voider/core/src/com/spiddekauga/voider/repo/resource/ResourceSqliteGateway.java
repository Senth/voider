package com.spiddekauga.voider.repo.resource;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.sql.DatabaseCursor;
import com.spiddekauga.voider.network.resource.ResourceRevisionEntity;
import com.spiddekauga.voider.network.resource.RevisionEntity;
import com.spiddekauga.voider.network.resource.UploadTypes;
import com.spiddekauga.voider.repo.SqliteGateway;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

/**
 * Gateway for handling resource in an SQLite database
 */
class ResourceSqliteGateway extends SqliteGateway {
/** Column for resource uuid */
private static final int RESOURCE__UUID = 0;
/** Column for resource revision revision */
private static final int RESOURCE_REVISION__REVISION = 1;
/** Column for resource revision date */
private static final int RESOURCE_REVISION__DATE = 2;

/**
 * @param uuid resource to test if it exists
 * @return true if the resource exists
 */
boolean exists(UUID uuid) {
	DatabaseCursor cursor = rawQuery("SELECT NULL FROM resource WHERE uuid='" + uuid + "';");

	boolean exists = cursor.next();
	cursor.close();
	return exists;
}

/**
 * Add a resource to the database
 * @param uuid unique id of the resource
 * @param typeIdentifier the type of resource to add
 */
void add(UUID uuid, int typeIdentifier) {
	execSQL("INSERT INTO resource (uuid, type) VALUES ( '" + uuid + "', " + typeIdentifier + ");");
}

/**
 * Add a revision to an existing resource
 * @param uuid the unique id of the resource
 * @param revision the revision to add
 * @param date the date of the resource
 */
void addRevision(UUID uuid, int revision, Date date) {
	execSQL("INSERT INTO resource_revision (uuid, revision, date) VALUES ( '" + uuid + "', " + revision + ", " + dateToString(date) + ");");
}

/**
 * Removes a resource from the database. Does not delete the resource's revisions!
 * @param uuid the unique id of the resource
 */
void remove(UUID uuid) {
	execSQL("DELETE FROM resource WHERE uuid='" + uuid.toString() + "';");
}

/**
 * Add the resource as removed to the database
 * @param uuid unique id of the resource
 */
void addAsRemoved(UUID uuid) {
	execSQL("INSERT INTO resource_removed VALUES ( '" + uuid + "' )");
}

/**
 * Remove the resource from the removed resource table. I.e. a table that holds all resources that
 * have been removed
 * @param uuid unique id of the resource to remove
 */
void removeFromRemoved(UUID uuid) {
	execSQL("DELETE FROM resource_removed WHERE uuid='" + uuid + "';");
}

/**
 * @return all resources from the removed resource table. I.e. the table that holds all resources
 * that have been removed
 */
ArrayList<UUID> getRemovedResources() {
	ArrayList<UUID> resources = new ArrayList<>();

	DatabaseCursor cursor = rawQuery("SELECT * FROM resource_removed;");

	while (cursor.next()) {
		String stringUuid = cursor.getString(0);
		resources.add(UUID.fromString(stringUuid));
	}
	cursor.close();

	return resources;
}

/**
 * Removes all resources of the specified type (including all revisions)
 * @param typeIdentifier the resource type to remove
 */
void removeAll(int typeIdentifier) {
	removeAll(typeIdentifier, false);
}

/**
 * Removes all resources of the specified type (including all revisions)
 * @param typeIdentifier the resource type to remove
 * @param addToRemovedDb set to true if the resource should be added to the removed DB
 */
void removeAll(int typeIdentifier, final boolean addToRemovedDb) {
	// Delete revisions first
	DatabaseCursor cursor = rawQuery("SELECT uuid FROM resource WHERE type=" + typeIdentifier + ";");
	while (cursor.next()) {
		String uuid = cursor.getString(0);
		execSQL("DELETE FROM resource_revision WHERE uuid='" + uuid + "';");

		// Add as removed
		if (addToRemovedDb) {
			execSQL("INSERT INTO resource_removed VALUES ( '" + uuid + "' );");
		}
	}

	cursor.close();

	// Delete all resources
	execSQL("DELETE FROM resource WHERE type=" + typeIdentifier + ";");
}

/**
 * Remove all revisions of a resource
 * @param uuid unique id of the resource
 */
void removeRevisions(UUID uuid) {
	execSQL("DELETE FROM resource_revision WHERE uuid='" + uuid + "';");
}

/**
 * Remove the specified revision and all later ones for the specified resource
 * @param uuid unique id of the resource
 * @param fromRevision remove all revisions from this one
 */
void removeRevisions(UUID uuid, int fromRevision) {
	execSQL("DELETE FROM resource_revision WHERE uuid='" + uuid + "' AND revision>=" + fromRevision + ";");
}

/**
 * Get all of the specified type
 * @param typeIdentifier the resource type to get all resource of
 * @return all resources of the specified type. Don't forget to free the arraylist!
 */
ArrayList<UUID> getAll(int typeIdentifier) {
	ArrayList<UUID> resources = new ArrayList<>();

	DatabaseCursor cursor = rawQuery("SELECT uuid FROM resource WHERE type=" + typeIdentifier + ";");

	while (cursor.next()) {
		String stringUuid = cursor.getString(RESOURCE__UUID);
		resources.add(UUID.fromString(stringUuid));
	}
	cursor.close();

	return resources;
}

/**
 * Get the count of all resources of the specified type
 * @param typeIdentifier resource type to calculate the number of
 * @return number of resources of the specified type
 */
int getCount(int typeIdentifier) {
	DatabaseCursor cursor = rawQuery("SELECT Count(*) FROM resource WHERE type=" + typeIdentifier + ";");

	if (cursor.next()) {
		return cursor.getInt(0);
	}

	return 0;
}

/**
 * Get all revisions of the specified resource
 * @param uuid the resource to get the revisions for
 * @return all revisions
 */
ArrayList<RevisionEntity> getRevisions(UUID uuid) {
	ArrayList<RevisionEntity> revisions = new ArrayList<>();

	DatabaseCursor cursor = rawQuery("SELECT * FROM resource_revision WHERE uuid='" + uuid + "';");

	while (cursor.next()) {
		RevisionEntity revisionInfo = new RevisionEntity();
		revisionInfo.revision = cursor.getInt(RESOURCE_REVISION__REVISION);
		revisionInfo.date.setTime(cursor.getLong(RESOURCE_REVISION__DATE));
		revisions.add(revisionInfo);
	}
	cursor.close();

	return revisions;
}

/**
 * Get the latest revision of the specified resource
 * @param uuid the resource to get the revision for
 * @return latest revision of the specified resource.
 * @throws ResourceNotFoundException if the resource wasn't found
 */
RevisionEntity getRevisionLatest(UUID uuid) {
	DatabaseCursor cursor = rawQuery("SELECT * FROM resource_revision WHERE uuid='" + uuid + "' ORDER BY revision DESC LIMIT 1;");

	if (cursor.next()) {
		RevisionEntity revisionInfo = new RevisionEntity();
		revisionInfo.revision = cursor.getInt(RESOURCE_REVISION__REVISION);
		revisionInfo.date.setTime(cursor.getLong(RESOURCE_REVISION__DATE));
		cursor.close();
		return revisionInfo;
	} else {
		cursor.close();
		throw new ResourceNotFoundException(uuid);
	}
}

/**
 * @param uuid id of the resource to get the type for
 * @return type of the resource id
 * @throws ResourceNotFoundException if the resource wasn't found
 */
int getType(UUID uuid) {
	DatabaseCursor cursor = rawQuery("SELECT type FROM resource WHERE uuid='" + uuid + "';");

	if (cursor.next()) {
		return cursor.getInt(0);
	} else {
		throw new ResourceNotFoundException(uuid);
	}
}

/**
 * @param uuid id of the resource to get the type for
 * @return true if the resource has been published
 * @throws ResourceNotFoundException if the resource wasn't found
 */
boolean isPublished(UUID uuid) {
	DatabaseCursor cursor = rawQuery("SELECT published FROM resource WHERE uuid='" + uuid + "' LIMIT 1;");

	if (cursor.next()) {
		return cursor.getInt(0) == 1 ? true : false;
	} else {
		throw new ResourceNotFoundException(uuid);
	}
}

/**
 * Set a resource as published / unpublished
 * @param uuid the resource id to set
 * @param published true if published, false if unpublished
 */
void setPublished(UUID uuid, boolean published) {
	execSQL("UPDATE resource SET published=" + (published ? "1" : "0") + " WHERE uuid='" + uuid + "';");
}

/**
 * @return all user resource revisions that haven't been uploaded/synced to the server.
 */
HashMap<UUID, ResourceRevisionEntity> getUnsyncedUserResources() {
	HashMap<UUID, ResourceRevisionEntity> resources = new HashMap<>();

	DatabaseCursor cursor = rawQuery("SELECT uuid, revision, date FROM resource_revision WHERE uploaded=0 ORDER BY revision");

	while (cursor.next()) {
		String uuidString = cursor.getString(0);
		if (uuidString == null) {
			Gdx.app.error("ResourceSqlitGateway", "uuid is null when getting resource revision");
			continue;
		}
		UUID uuid = UUID.fromString(uuidString);

		ResourceRevisionEntity resource = resources.get(uuid);

		if (resource == null) {
			resource = new ResourceRevisionEntity();
			resource.resourceId = uuid;
			resources.put(uuid, resource);
		}

		RevisionEntity revisionEntity = new RevisionEntity();
		revisionEntity.revision = cursor.getInt(1);
		revisionEntity.date = new Date(cursor.getLong(2));
		resource.revisions.add(revisionEntity);
	}

	for (Entry<UUID, ResourceRevisionEntity> entry : resources.entrySet()) {
		cursor = rawQuery("SELECT type FROM resource WHERE uuid='" + entry.getValue().resourceId + "' LIMIT 1;");

		if (cursor.next()) {
			int typeId = cursor.getInt(0);
			entry.getValue().type = UploadTypes.fromId(typeId);
		}
	}

	return resources;
}

/**
 * Set the user resource revision as synced/uploaded
 * @param resourceId the resource to set
 * @param revision the specific revision to set as synced/uploaded
 */
void setSyncedUserResource(UUID resourceId, int revision) {
	execSQL("UPDATE resource_revision SET uploaded=1 WHERE uuid='" + resourceId + "' AND revision=" + revision + ";");
}

/**
 * Set the user resource revision as synced/uploaded
 * @param resourceId the resource to set
 * @param from from this revision
 * @param to to and including this revision
 */
void setSyncedUserResource(UUID resourceId, int from, int to) {
	execSQL("UPDATE resource_revision SET uploaded=1 WHERE uuid='" + resourceId + "' AND revision>=" + from + " AND revision<=" + to + ";");
}
}

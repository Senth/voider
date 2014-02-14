package com.spiddekauga.voider.repo;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import com.badlogic.gdx.sql.DatabaseCursor;
import com.badlogic.gdx.sql.SQLiteGdxException;
import com.spiddekauga.voider.resources.RevisionInfo;
import com.spiddekauga.voider.utils.Pools;

/**
 * Gateway for handling resource in an SQLite database
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
class ResourceSqliteGateway extends SqliteGateway {
	/**
	 * Add a resource to the database
	 * @param uuid unique id of the resource
	 * @param typeIdentifier the type of resource to add
	 */
	void add(UUID uuid, int typeIdentifier) {
		try {
			mDatabase.execSQL("INSERT INTO resource VALUES ( '" + uuid + "', " + typeIdentifier + ");");
		} catch (SQLiteGdxException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Add a revision to an existing resource
	 * @param uuid the unique id of the resource
	 * @param revision the revision to add
	 * @param date the date of the resource
	 */
	void addRevision(UUID uuid, int revision, Date date) {
		try {
			mDatabase.execSQL("INSERT INTO resource VALUES ( '" + uuid + "', " + revision + ", " + date.getTime() + ");");
		} catch (SQLiteGdxException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Removes a resource (including all revisions) from the database.
	 * @param uuid the unique id of the resource
	 */
	void remove(UUID uuid) {
		try {
			mDatabase.execSQL("DELETE FROM resource WHERE uuid='" + uuid.toString() + "';");
			mDatabase.execSQL("DELETE FROM resource_revision WHERE uuid='" + uuid + "';");
		} catch (SQLiteGdxException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Removes all resources of the specified type (including all revisions)
	 * @param typeIdentifier the resource type to remove
	 */
	void removeAll(int typeIdentifier) {
		try {
			// Delete revisions first
			DatabaseCursor cursor = mDatabase.rawQuery("SELECT uuid FROM resource WHERE type=" + typeIdentifier + ";");
			while (cursor.next()) {
				String uuid = cursor.getString(RESOURCE__UUID);
				mDatabase.execSQL("DELETE FROM resource_revision WHERE uuid='" + uuid + "';");
			}

			// Delete all resources
			mDatabase.execSQL("DELETE FROM resource WHERE type=" + typeIdentifier + ";");
		} catch (SQLiteGdxException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Remove all revisions of a resource
	 * @param uuid unique id of the resource
	 */
	void removeRevisions(UUID uuid) {
		try {
			mDatabase.execSQL("DELETE FROM resource_revision WHERE uuid='" + uuid + "';");
		} catch (SQLiteGdxException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Get all of the specified type
	 * @param typeIdentifier the resource type to get all resource of
	 * @return all resources of the specified type. Don't forget to free the arraylist!
	 */
	ArrayList<UUID> getAll(int typeIdentifier) {
		@SuppressWarnings("unchecked")
		ArrayList<UUID> resources = Pools.arrayList.obtain();

		try {
			DatabaseCursor cursor = mDatabase.rawQuery("SELECT uuid FROM resource WHERE type=" + typeIdentifier + ";");

			while (cursor.next()) {
				String stringUuid = cursor.getString(RESOURCE__UUID);
				resources.add(UUID.fromString(stringUuid));
			}
		} catch (SQLiteGdxException e) {
			e.printStackTrace();
		}

		return resources;
	}

	/**
	 * Get the count of all resources of the specified type
	 * @param typeIdentifier resource type to calculate the number of
	 * @return number of resources of the specified type
	 */
	int getCount(int typeIdentifier) {
		try {
			DatabaseCursor cursor = mDatabase.rawQuery("SELECT Count(*) FROM resource WHERE type=" + typeIdentifier + ";");

			if (cursor.next()) {
				return cursor.getInt(0);
			}
		} catch (SQLiteGdxException e) {
			e.printStackTrace();
		}

		return 0;
	}

	/**
	 * Get all revisions of the specified resource
	 * @param uuid the resource to get the revisions for
	 * @return all revisions
	 */
	ArrayList<RevisionInfo> getRevisions(UUID uuid) {
		@SuppressWarnings("unchecked")
		ArrayList<RevisionInfo> revisions = Pools.arrayList.obtain();

		try {
			DatabaseCursor cursor = mDatabase.rawQuery("SELECT * FROM resource_revision WHERE uuid='" + uuid + "';");

			while (cursor.next()) {
				RevisionInfo revisionInfo = Pools.revisionInfo.obtain();
				revisionInfo.revision = cursor.getInt(RESOURCE_REVISION__REVISION);
				revisionInfo.date.setTime(cursor.getLong(RESOURCE_REVISION__DATE));
				revisions.add(revisionInfo);
			}
		} catch (SQLiteGdxException e) {
			e.printStackTrace();
		}

		return revisions;
	}

	/**
	 * Get the latest revision of the specified resource
	 * @param uuid the resource to get the revision for
	 * @return latest revision of the specifed resource. If the resource
	 * doesn't have any revisions null is returned.
	 */
	RevisionInfo getRevisionLatest(UUID uuid) {


		try {
			DatabaseCursor cursor = mDatabase.rawQuery("SELECT * FROM resource_revision WHERE uuid='" + uuid + "' ORDER BY revision DESC LIMIT 1;");

			if (cursor.next()) {
				RevisionInfo revisionInfo = Pools.revisionInfo.obtain();
				revisionInfo.revision = cursor.getInt(RESOURCE_REVISION__REVISION);
				revisionInfo.date.setTime(cursor.getLong(RESOURCE_REVISION__DATE));
				return revisionInfo;
			}
		} catch (SQLiteGdxException e) {
			e.printStackTrace();
		}

		return null;
	}

	/** Column for resource uuid */
	private static final int RESOURCE__UUID = 0;
	/** Column for resource type */
	private static final int RESOURCE__TYPE = 1;
	/** Column for resource revision uuid */
	private static final int RESOURCE_REVISION__UUID = 0;
	/** Column for resource revision revision */
	private static final int RESOURCE_REVISION__REVISION = 1;
	/** Column for resource revision date */
	private static final int RESOURCE_REVISION__DATE = 2;
}

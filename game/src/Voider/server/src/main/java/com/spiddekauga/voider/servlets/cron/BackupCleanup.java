package com.spiddekauga.voider.servlets.cron;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.spiddekauga.appengine.DatastoreUtils;
import com.spiddekauga.appengine.DatastoreUtils.FilterWrapper;
import com.spiddekauga.voider.server.util.ServerConfig;
import com.spiddekauga.voider.server.util.VoiderController;

/**
 * This method deletes backups older than 1 month and newer than 6 months, but keeps all
 * backups made on the 1st of a month.
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class BackupCleanup extends VoiderController {

	@Override
	protected void onRequest() {
		List<Entity> entitiesToDelete = getEntitiesToDelete();
		List<String> backupIds = extractBackupId(entitiesToDelete);

		// Delete all backups
		for (String backupId : backupIds) {
			deleteBackup(backupId);
		}
	}

	/**
	 * Get all backups we should delete. Backups made 1st of the month will be saved.
	 * @return all backups we should delete as entities
	 */
	private List<Entity> getEntitiesToDelete() {
		// Get all backups from 1–6 months.
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MONTH, -1);
		Date oneMonthAgo = calendar.getTime();
		calendar.add(Calendar.MONTH, -5);
		Date sixMonthsAgo = calendar.getTime();

		FilterWrapper olderThan = new FilterWrapper(C_START_TIME, FilterOperator.LESS_THAN, oneMonthAgo);
		FilterWrapper newerThan = new FilterWrapper(C_START_TIME, FilterOperator.GREATER_THAN_OR_EQUAL, sixMonthsAgo);
		Iterable<Entity> backups = DatastoreUtils.getEntities(TABLE, olderThan, newerThan);

		return filterOutFirstInMonth(backups);
	}

	/**
	 * Convert the backups to a list and filter out those that start on the 1st in the
	 * month
	 * @param backups all backup entities
	 * @return all backups to delete
	 */
	private static List<Entity> filterOutFirstInMonth(Iterable<Entity> backups) {
		List<Entity> filteredBackups = new ArrayList<>();

		for (Entity entity : backups) {
			if (isFirstDayOfMonth(entity)) {
				filteredBackups.add(entity);
			}
		}

		return filteredBackups;
	}

	/**
	 * Checks if the date is the first day of the month
	 * @param entity entity containing the date to check
	 * @return true if it's the first day of the month
	 */
	private static boolean isFirstDayOfMonth(Entity entity) {
		Date date = (Date) entity.getProperty(C_START_TIME);

		if (date != null) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(date);

			return calendar.get(Calendar.DAY_OF_MONTH) == 1;
		}

		return false;
	}

	/**
	 * Extract the backup id from all entities
	 * @param entities all backup entities
	 * @return list with names of all entities to delete
	 */
	private static List<String> extractBackupId(List<Entity> entities) {
		List<String> backupIds = new ArrayList<>();

		for (Entity entity : entities) {
			String backupId = extractBackupId(entity);
			if (backupId != null) {
				backupIds.add(backupId);
			}
		}

		return backupIds;
	}

	/**
	 * Extract the backup id from one backup entity
	 * @param entity the backup entity
	 * @return extracted backup id
	 */
	private static String extractBackupId(Entity entity) {
		String gsHandle = (String) entity.getProperty(C_GS_HANDLE);

		// Extract id
		if (gsHandle != null) {
			int from = gsHandle.lastIndexOf("/") + 1;
			int to = gsHandle.lastIndexOf(".");

			String backupId = gsHandle.substring(from, to);
			return backupId;
		} else {
			mLogger.warning("Didn't find any gsHandle for backup entity:\n" + entity);
			return null;
		}
	}

	/**
	 * Delete the specified backup id
	 * @param backupId the backup to delete
	 */
	private static void deleteBackup(String backupId) {
		// Get URL String
		// Create HTTP request
		// Extract token
		// Send HTTP request with token
	}

	/**
	 * Create the correct URL string to delete the specified backup
	 * @param backupId id of the backup to delete
	 * @return URL string for deletion of the specified backup
	 */
	private static String createDeleteUrl(String backupId) {
		String deleteUrl = BASE_URL + "?action";

		return deleteUrl;
	}

	private static final Logger mLogger = Logger.getLogger(BackupCleanup.class.getSimpleName());
	private static final String TABLE = "_AE_Backup_Information Entities";
	private static final String C_START_TIME = "start_time";
	private static final String C_GS_HANDLE = "gs_handle";
	private static final String SUB_DOMAIN = "ah-builtin-python-bundle-dot-";
	private static final String URI = "_ah/datastore_admin";
	private static final String BASE_URL;

	static {
		// Set base url
		String appspotUrl = ServerConfig.Builds.getCurrent().getAppspotUrl();

		// Remove HTTPS
		int removeFrom = appspotUrl.indexOf("//") + 2;
		appspotUrl = appspotUrl.substring(removeFrom);

		BASE_URL = "https://" + SUB_DOMAIN + appspotUrl + URI;
	}
}

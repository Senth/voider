package com.spiddekauga.voider.servlets.cron;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.urlfetch.FetchOptions;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import com.spiddekauga.appengine.DatastoreUtils;
import com.spiddekauga.appengine.DatastoreUtils.FilterWrapper;
import com.spiddekauga.http.HttpPostBuilder;
import com.spiddekauga.http.HttpResponseParser;
import com.spiddekauga.utils.Strings;
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
		String response = deleteBackups(backupIds);

		if (response != null) {
			getResponsePrintWriter().append(response);
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
	 * @param backupIds the backup to delete
	 * @return response from deleting, null if failed
	 */
	private static String deleteBackups(List<String> backupIds) {
		String deleteResponse = sendDeleteRequest(backupIds);
		String token = extractDeleteToken(deleteResponse);
		if (token != null) {
			return sendConfirmRequest(backupIds, token);
		} else {
			return deleteResponse;
		}
	}

	/**
	 * Makes a HTTP request to the server to delete the specified backup
	 * @param backupIds all backups to delete
	 * @return string response from the server
	 */
	private static String sendDeleteRequest(List<String> backupIds) {
		try {
			FetchOptions fetchOptions = FetchOptions.Builder.doNotFollowRedirects();
			HTTPRequest httpRequest = new HTTPRequest(new URL(BASE_URL), HTTPMethod.POST, fetchOptions);

			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append("action=Delete");
			for (String backupId : backupIds) {
				stringBuilder.append("&backup_id=").append(backupId);
			}
			httpRequest.setPayload(stringBuilder.toString().getBytes("UTF-8"));
			HTTPResponse httpResponse = URLFetchServiceFactory.getURLFetchService().fetch(httpRequest);

			String responseText = new String(httpResponse.getContent(), "UTF-8");
			mLogger.info("Response:\n" + responseText);
			return responseText;


			// ArrayList<String> scopes = new ArrayList<>();
			// scopes.add(BASE_URL);
			// AppIdentityService appIdentity =
			// AppIdentityServiceFactory.getAppIdentityService();
			//
			// AppIdentityService.GetAccessTokenResult accessToken =
			// appIdentity.getAccessToken(scopes);
			//
			// HttpPostBuilder builder = new HttpPostBuilder(BASE_URL);
			//
			// HttpURLConnection connection = builder.getHttpURLConnection();
			// connection.setInstanceFollowRedirects(false);
			// connection.setRequestProperty("X-Appengine-Inbound-Appid",
			// SystemProperty.applicationId.get());
			// connection.setRequestProperty("Authorization", "Bearer  " + accessToken);
			// mLogger.info("Appid: " +
			// connection.getRequestProperty("X-Appengine-Inbound-Appid"));
			//
			// builder.addParameter("action", "Delete");
			// for (String backupId : backupIds) {
			// builder.addParameter("backup_id", backupId);
			// }
			//
			// connection = builder.build();
			//
			// String response = HttpResponseParser.getStringResponse(connection);
			// connection.disconnect();
			//
			// mLogger.info("Response:\n" + response);
			//
			// return response;
		} catch (IOException e) {
			mLogger.severe(Strings.exceptionToString(e));
			return null;
		}
	}

	/**
	 * Extract the delete token from the HTTP response
	 * @param response the response from the delete request
	 * @return delete token
	 */
	private static String extractDeleteToken(String response) {
		Matcher matcher = Pattern.compile("xsrf_token\"\\ value=\"(.*?)\">").matcher(response);
		if (matcher.find()) {
			try {
				String token = matcher.group(1);
				mLogger.info("Found token: " + token);
				return token;
			} catch (IndexOutOfBoundsException e) {
				return null;
			}
		} else {
			return null;
		}
	}

	/**
	 * Send delete confirmation request
	 * @param backupIds all backups to delete
	 * @param token the confirmation token for deleting the request
	 * @return response from the delete, null if failed
	 */
	private static String sendConfirmRequest(List<String> backupIds, String token) {
		try {
			HttpPostBuilder builder = new HttpPostBuilder(BASE_URL + "/backup_delete.do");

			for (String backupId : backupIds) {
				builder.addParameter("backup_id", backupId);
			}
			builder.addParameter("namespace");
			builder.addParameter("xsrf_token", token);

			HttpURLConnection connection = builder.build();
			connection.setInstanceFollowRedirects(false);
			String responseText = HttpResponseParser.getStringResponse(connection);
			connection.disconnect();
			return responseText;

		} catch (IOException e) {
			mLogger.severe(Strings.exceptionToString(e));
			return null;
		}
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

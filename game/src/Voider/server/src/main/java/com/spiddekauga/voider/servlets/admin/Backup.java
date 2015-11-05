package com.spiddekauga.voider.servlets.admin;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.spiddekauga.appengine.DatastoreUtils;
import com.spiddekauga.voider.network.misc.Motd.MotdTypes;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables.CBackupInfo;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables.CMaintenance;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables.CMotd;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables.CRestoreDate;
import com.spiddekauga.voider.server.util.ServerConfig.MaintenanceModes;
import com.spiddekauga.voider.server.util.VoiderController;

/**
 * Ability to revert to a restore point.
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class Backup extends VoiderController {

	@Override
	protected void onRequest() {
		// New restore point
		if (isParameterSet(P_BACKUP_ID)) {
			createRestorePoint();
		}
		// Enable/Disable maintenance
		else if (isParameterSet(P_MAINTENANCE_MODE)) {
			changeMaintenanceMode();
		}

		displayBackupPoints();
		displayRestoredDates();
		forwardToHtml();
	}

	/**
	 * Forward to HTML page
	 */
	private void forwardToHtml() {
		try {
			getRequest().getRequestDispatcher("backup.jsp").forward(getRequest(), getResponse());
		} catch (ServletException | IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Change maintenance mode
	 */
	private void changeMaintenanceMode() {
		String modeString = getParameter(P_MAINTENANCE_MODE);
		MaintenanceModes mode = MaintenanceModes.fromString(modeString);

		if (mode != null) {
			switch (mode) {
			case UP:
				disableMaintenance();
				break;

			case DOWN:
				enableMaintenance();
				break;
			}
		}
	}

	/**
	 * Turn on maintenance mode
	 */
	private void enableMaintenance() {
		Key motdKey = createMotdEntity();

		// Update maintenance mode
		Entity entity = DatastoreUtils.getSingleEntity(DatastoreTables.MAINTENANCE);

		// Create new entity
		if (entity == null) {
			entity = new Entity(DatastoreTables.MAINTENANCE);
		}

		// Set mode
		entity.setProperty(CMaintenance.MODE, MaintenanceModes.DOWN.toString());
		entity.setProperty(CMaintenance.REASON, getParameter(P_MAINTENANCE_REASON));
		entity.setProperty(CMaintenance.MOTD_KEY, motdKey);
	}

	/**
	 * Create maintenance MOTD
	 */
	private Key createMotdEntity() {
		Date currentDate = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MONTH, 1);
		Date expireDate = calendar.getTime();
		MotdTypes type = MotdTypes.SEVERE;

		String content = "Server is currently undergoing a maintenance. You will not be able to connect to the server during this time.\n\n";
		content += getParameter(P_MAINTENANCE_REASON);

		Entity entity = new Entity(DatastoreTables.MOTD);
		entity.setProperty(CMotd.TITLE, "Server Maintenance");
		entity.setProperty(CMotd.CREATED, currentDate);
		entity.setProperty(CMotd.EXPIRES, expireDate);
		entity.setProperty(CMotd.TYPE, type.toId());
		entity.setProperty(CMotd.CONTENT, content);

		return DatastoreUtils.put(entity);
	}

	/**
	 * Turn off maintenance mode
	 */
	private void disableMaintenance() {

	}

	/**
	 * Create a new restore point
	 */
	private void createRestorePoint() {
		String backupKeyString = getParameter(P_BACKUP_ID);
		Key backupKey = KeyFactory.stringToKey(backupKeyString);
		Entity backupEntity = DatastoreUtils.getEntity(backupKey);
		Date backupDate = (Date) backupEntity.getProperty(CBackupInfo.START_TIME);

		// Backup from this date
		Entity entity = new Entity(DatastoreTables.RESTORE_DATE);
		entity.setProperty(CRestoreDate.FROM_DATE, new Date());
		entity.setProperty(CRestoreDate.TO_DATE, backupDate);

	}

	/**
	 * Get and display all points the server has restored to
	 */
	private void displayRestoredDates() {
		Iterable<Entity> entities = DatastoreUtils.getEntities(DatastoreTables.RESTORE_DATE);

		// Create Restore Dates
		List<RestoredDate> restoredDates = new ArrayList<>();
		for (Entity entity : entities) {
			Date from = (Date) entity.getProperty(CRestoreDate.FROM_DATE);
			Date to = (Date) entity.getProperty(CRestoreDate.TO_DATE);

			RestoredDate restoredDate = new RestoredDate(from, to);
			restoredDates.add(restoredDate);
		}

		getRequest().setAttribute("restored_dates", restoredDates);
	}

	/**
	 * Get and display all backup points
	 */
	private void displayBackupPoints() {
		Iterable<Entity> entities = DatastoreUtils.getEntities(DatastoreTables.BACKUP_INFO);

		// Create Backup points
		List<BackupPoint> backupPoints = new ArrayList<>();
		for (Entity entity : entities) {
			Date date = (Date) entity.getProperty(CBackupInfo.START_TIME);
			Key key = entity.getKey();

			BackupPoint backupPoint = new BackupPoint(date, key);
			backupPoints.add(backupPoint);
		}

		HttpServletRequest request = getRequest();
		request.setAttribute("backup_points", backupPoints);
	}

	/**
	 * Backup point
	 */
	public class BackupPoint {
		/**
		 * @param date
		 * @param key
		 */
		private BackupPoint(Date date, Key key) {
			this.date = DATE_FORMAT.format(date);
			this.id = KeyFactory.keyToString(key);
		}

		/**
		 * @return date
		 */
		public String getDate() {
			return date;
		}

		/**
		 * @return id
		 */
		public String getId() {
			return id;
		}

		private String date;
		private String id;
	}

	/**
	 * Existing restored points
	 */
	public class RestoredDate {
		/**
		 * New Restored date
		 * @param from which date we restored from
		 * @param to which date we restored to
		 */
		private RestoredDate(Date from, Date to) {
			this.from = DATE_FORMAT.format(from);
			this.to = DATE_FORMAT.format(to);
		}

		/**
		 * @return from
		 */
		public String getFrom() {
			return from;
		}

		/**
		 * @return to
		 */
		public String getTo() {
			return to;
		}

		private String from;
		private String to;
	}


	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:SS z");
	private static final String P_BACKUP_ID = "backup_id";
	private static final String P_MAINTENANCE_MODE = "maintenance_mode";
	private static final String P_MAINTENANCE_REASON = "maintenance_reason";
}

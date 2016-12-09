package com.spiddekauga.voider.servlets.admin;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.spiddekauga.appengine.DatastoreUtils;
import com.spiddekauga.voider.network.misc.Motd;
import com.spiddekauga.voider.network.misc.Motd.MotdTypes;
import com.spiddekauga.voider.network.misc.ServerMessage;
import com.spiddekauga.voider.network.misc.ServerMessage.MessageTypes;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables.CBackupInfo;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables.CMaintenance;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables.CMotd;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables.CRestoreDate;
import com.spiddekauga.voider.server.util.ServerConfig.MaintenanceModes;
import com.spiddekauga.voider.server.util.VoiderController;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Ability to revert to a restore point.
 */
@SuppressWarnings("serial")
public class Maintenance extends VoiderController {
private static final String P_MAINTENANCE_MODE = "maintenance_mode";
private static final String P_MAINTENANCE_REASON = "maintenance_reason";
private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:SS z");
private static final String P_BACKUP_ID = "backup_id";

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

	displayMaintenanceMode();
	displayBackupPoints();
	displayRestoredDates();
	forwardToHtml();
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

	DatastoreUtils.put(entity);
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
 * Get and display the current maintenance mode
 */
private void displayMaintenanceMode() {
	Entity entity = DatastoreUtils.getSingleEntity(DatastoreTables.MAINTENANCE);

	MaintenanceModes mode = MaintenanceModes.UP;

	if (entity != null) {
		String modeString = (String) entity.getProperty(CMaintenance.MODE);
		if (modeString != null) {
			mode = MaintenanceModes.fromString(modeString);
		}
	}

	getRequest().setAttribute("maintenance_mode", mode.toString());
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

	// Sort
	Collections.sort(backupPoints, new Comparator<BackupPoint>() {
		@Override
		public int compare(BackupPoint o1, BackupPoint o2) {
			return o1.date.compareTo(o2.date);
		}
	});
	Collections.reverse(backupPoints);

	getRequest().setAttribute("backup_points", backupPoints);
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

	// Sort
	Collections.sort(restoredDates, new Comparator<RestoredDate>() {
		@Override
		public int compare(RestoredDate o1, RestoredDate o2) {
			return o1.from.compareTo(o2.from);
		}
	});
	Collections.reverse(restoredDates);

	getRequest().setAttribute("restored_dates", restoredDates);
}

/**
 * Turn off maintenance mode
 */
private void disableMaintenance() {
	Entity entity = DatastoreUtils.getSingleEntity(DatastoreTables.MAINTENANCE);

	if (entity != null) {
		entity.setProperty(CMaintenance.MODE, MaintenanceModes.UP.toString());
		entity.removeProperty(CMaintenance.REASON);

		Key motdKey = (Key) entity.getProperty(CMaintenance.MOTD_KEY);
		entity.removeProperty(CMaintenance.MOTD_KEY);
		DatastoreUtils.put(entity);


		// Expire MOTD
		if (motdKey != null) {
			Entity motdEntity = DatastoreUtils.getEntity(motdKey);

			if (motdEntity != null) {
				motdEntity.setProperty(CMotd.EXPIRES, new Date());
				DatastoreUtils.put(motdEntity);
			}
		}
	} else {
		mLogger.severe("There should always be a maintenance entity when disabling!");
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

	DatastoreUtils.put(entity);


}

/**
 * Create maintenance MOTD
 * @return key of the created MOTD
 */
private Key createMotdEntity() {
	Motd motd = new Motd();

	motd.created = new Date();
	motd.title = "Server Maintenance";
	motd.content = "Server is currently undergoing a maintenance. You will not be able to connect to the server during this time.\n\n";
	motd.content += getParameter(P_MAINTENANCE_REASON);
	motd.type = MotdTypes.SEVERE;


	// Expire date
	Calendar calendar = Calendar.getInstance();
	calendar.add(Calendar.MONTH, 1);
	Date expireDate = calendar.getTime();


	Entity entity = new Entity(DatastoreTables.MOTD);
	entity.setProperty(CMotd.TITLE, motd.title);
	entity.setProperty(CMotd.CREATED, motd.created);
	entity.setProperty(CMotd.EXPIRES, expireDate);
	entity.setProperty(CMotd.TYPE, motd.type.toId());
	entity.setProperty(CMotd.CONTENT, motd.content);

	Key key = DatastoreUtils.put(entity);


	// Send message
	ServerMessage<Motd> message = new ServerMessage<>(MessageTypes.SERVER_MAINTENANCE);
	message.data = motd;
	sendMessage(ServerMessageReceivers.ALL, message);


	return key;
}

/**
 * Backup point
 */
public class BackupPoint {
	private String date;
	private String id;

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
}

/**
 * Existing restored points
 */
public class RestoredDate {
	private String from;
	private String to;

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
}
}

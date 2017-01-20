package com.spiddekauga.voider.servlets.admin;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;
import com.google.appengine.tools.cloudstorage.ListItem;
import com.google.appengine.tools.cloudstorage.ListOptions;
import com.google.appengine.tools.cloudstorage.ListResult;
import com.google.appengine.tools.cloudstorage.RetryParams;
import com.spiddekauga.appengine.DatastoreUtils;
import com.spiddekauga.utils.Time;
import com.spiddekauga.voider.network.misc.Motd;
import com.spiddekauga.voider.network.misc.Motd.MotdTypes;
import com.spiddekauga.voider.network.misc.ServerMessage;
import com.spiddekauga.voider.network.misc.ServerMessage.MessageTypes;
import com.spiddekauga.voider.server.util.DatastoreTables;
import com.spiddekauga.voider.server.util.DatastoreTables.CMaintenance;
import com.spiddekauga.voider.server.util.DatastoreTables.CMotd;
import com.spiddekauga.voider.server.util.DatastoreTables.CRestoreDate;
import com.spiddekauga.voider.server.util.MaintenanceHelper;
import com.spiddekauga.voider.server.util.MessageSender;
import com.spiddekauga.voider.server.util.ServerConfig;
import com.spiddekauga.voider.server.util.VoiderController;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;

/**
 * Ability to revert to a restore point.
 */
@SuppressWarnings("serial")
public class MaintenanceServlet extends VoiderController {
private static final String P_MAINTENANCE_MODE = "maintenance_mode";
private static final String P_MAINTENANCE_REASON = "maintenance_reason";
private static final String P_BACKUP_DATE = "backup_date";
private static final Pattern BACKUP_DATE_PATTERN = Pattern.compile("/(.*)/");
private final SimpleDateFormat DATE_FORMAT = Time.createIsoDateFormat();

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

	public String getFrom() {
		return from;
	}

	public String getTo() {
		return to;
	}
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

	String restoreUrl = "backend-dot-" + getRootUrl() + "/restore";
	getRequest().setAttribute("restore_url", restoreUrl);
}


@Override
protected void onPost() throws ServletException, IOException {
	// Enable/Disable maintenance
	if (isParameterSet(P_MAINTENANCE_MODE)) {
		changeMaintenanceMode();
	}

	displayMaintenanceMode();
	displayBackupPoints();
	displayRestoredDates();
	forwardToHtml();
}

@Override
protected void onGet() throws ServletException, IOException {
	onPost();
}

/**
 * Change maintenance mode
 */
private void changeMaintenanceMode() {
	String modeString = getParameter(P_MAINTENANCE_MODE);
	MaintenanceHelper.Modes mode = MaintenanceHelper.Modes.fromString(modeString);

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

	MaintenanceHelper.Modes mode = MaintenanceHelper.Modes.UP;

	if (entity != null) {
		String modeString = (String) entity.getProperty(CMaintenance.MODE);
		if (modeString != null) {
			mode = MaintenanceHelper.Modes.fromString(modeString);
		}
	}

	getRequest().setAttribute("maintenance_mode", mode.toString());
}

/**
 * Get and display all backup points
 */
private void displayBackupPoints() {
	List<String> backupPoints = new ArrayList<>();

	// Get all backup points from GCS
	GcsService gcsService = GcsServiceFactory.createGcsService(RetryParams.getDefaultInstance());
	try {
		ListOptions listOptions = new ListOptions.Builder()
				.setPrefix("datastore_backup/")
				.setRecursive(false)
				.build();
		ListResult listResult = gcsService.list(ServerConfig.GCS_BUCKET, listOptions);

		while (listResult.hasNext()) {
			ListItem listItem = listResult.next();
			String nameDate = listItem.getName();

			// Extract date from full filename
			Matcher matcher = BACKUP_DATE_PATTERN.matcher(nameDate);
			if (matcher.find() && matcher.groupCount() == 1) {
				backupPoints.add(matcher.group(1));
			}
		}
	} catch (IOException e) {
		log("Message", e);
	}

	// Sort
	Collections.sort(backupPoints);
	Collections.reverse(backupPoints);

	getRequest().setAttribute("backup_points", backupPoints);
}


/**
 * Turn off maintenance mode
 */
private void disableMaintenance() {
	Entity entity = DatastoreUtils.getSingleEntity(DatastoreTables.MAINTENANCE);

	if (entity != null) {
		entity.setProperty(CMaintenance.MODE, MaintenanceHelper.Modes.UP.toString());
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
	entity.setProperty(CMaintenance.MODE, MaintenanceHelper.Modes.DOWN.toString());
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
	MessageSender.sendMessage(MessageSender.Receivers.ALL, null, message);


	return key;
}


}

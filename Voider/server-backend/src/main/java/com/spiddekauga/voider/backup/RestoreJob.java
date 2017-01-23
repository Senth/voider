package com.spiddekauga.voider.backup;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.tools.pipeline.FutureValue;
import com.google.appengine.tools.pipeline.Job1;
import com.google.appengine.tools.pipeline.Value;
import com.spiddekauga.appengine.DatastoreUtils;
import com.spiddekauga.appengine.pipeline.DatastoreDeleteJob;
import com.spiddekauga.appengine.pipeline.DatastoreJobConfig;
import com.spiddekauga.appengine.pipeline.backup.DatastoreBackupConfig;
import com.spiddekauga.appengine.pipeline.backup.DatastoreRestoreJob;
import com.spiddekauga.voider.BackendConfig;
import com.spiddekauga.voider.network.misc.Motd;
import com.spiddekauga.voider.network.misc.ServerMessage;
import com.spiddekauga.voider.server.util.DatastoreTables;
import com.spiddekauga.voider.server.util.DatastoreTables.CMaintenance;
import com.spiddekauga.voider.server.util.DatastoreTables.CMotd;
import com.spiddekauga.voider.server.util.MaintenanceHelper;
import com.spiddekauga.voider.server.util.MessageSender;

import java.util.Calendar;
import java.util.Date;

/**
 * Restore to a specific backup job
 */
class RestoreJob extends Job1<Void, Date> {
private static final String MAINTENANCE_REASON = "Server is currently undergoing a maintenance. You will not be able to connect to the server during this time.\n\n"
		+ "We're currently reverting the database. Unfortunately all changes you make will not be saved."
		+ "We're sorry for your inconvenience.";

@Override
public Value<Void> run(Date restoreTo) throws Exception {

	addRestoreDataPoint(restoreTo);
	enableMaintenanceMode();

	// Delete Datastore
	DatastoreJobConfig deleteConfig = new DatastoreJobConfig.Builder()
			.setMapSettings(BackendConfig.getMapSettings())
			.setJobSettings(BackendConfig.getJobSettings())
			.addTables(BackupConfig.BACKUP_TABLES)
			.build();
	FutureValue<Void> deleteDatastore = futureCall(new DatastoreDeleteJob(), immediate(deleteConfig), BackendConfig.getJobSettings());

	// Delete Search
	FutureValue<Void> deleteSearch = futureCall(new DeleteSearchJob(), BackendConfig.getJobSettings());

	// Import Datastore
	DatastoreBackupConfig restoreConfig = new DatastoreBackupConfig.Builder()
			.setMapSettings(BackendConfig.getMapSettings())
			.setJobSettings(BackendConfig.getJobSettings())
			.setDateDirectory(restoreTo)
			.build();
	FutureValue<Void> restoreDatastore = futureCall(new DatastoreRestoreJob(restoreConfig), BackendConfig.getJobSettings(waitFor(deleteDatastore)));

	// TODO Create Search from published resources

	// TODO Check so all resources have blobs

	// TODO Report error if not all resources have blobs

	// TODO Disable maintenance

	return immediate(null);
}

private void addRestoreDataPoint(Date restoreTo) {
	Entity entity = new Entity(DatastoreTables.RESTORE_DATE);
	entity.setProperty(DatastoreTables.CRestoreDate.FROM_DATE, new Date());
	entity.setUnindexedProperty(DatastoreTables.CRestoreDate.TO_DATE, restoreTo);
	DatastoreUtils.put(entity);
}

private void enableMaintenanceMode() {
	Key motdKey = createMotdEntity();

	// Update maintenance mode
	Entity entity = DatastoreUtils.getSingleEntity(DatastoreTables.MAINTENANCE);

	// Create new entity
	if (entity == null) {
		entity = new Entity(DatastoreTables.MAINTENANCE);
	}

	// Set mode
	entity.setProperty(CMaintenance.MODE, MaintenanceHelper.Modes.DOWN.toString());
	entity.setProperty(CMaintenance.REASON, MAINTENANCE_REASON);
	entity.setProperty(CMaintenance.MOTD_KEY, motdKey);

	DatastoreUtils.put(entity);
}

private Key createMotdEntity() {
	Motd motd = new Motd();

	motd.created = new Date();
	motd.title = "Server Maintenance";
	motd.content = MAINTENANCE_REASON;
	motd.type = Motd.MotdTypes.SEVERE;


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
	ServerMessage<Motd> message = new ServerMessage<>(ServerMessage.MessageTypes.SERVER_MAINTENANCE);
	message.data = motd;
	MessageSender.sendMessage(MessageSender.Receivers.ALL, null, message);


	return key;
}
}

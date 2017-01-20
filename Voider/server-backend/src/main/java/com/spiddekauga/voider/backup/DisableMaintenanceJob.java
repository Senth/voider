package com.spiddekauga.voider.backup;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.tools.pipeline.Job0;
import com.google.appengine.tools.pipeline.Value;
import com.spiddekauga.appengine.DatastoreUtils;
import com.spiddekauga.voider.server.util.DatastoreTables;
import com.spiddekauga.voider.server.util.DatastoreTables.CMaintenance;
import com.spiddekauga.voider.server.util.DatastoreTables.CMotd;
import com.spiddekauga.voider.server.util.MaintenanceHelper;

import java.util.Date;
import java.util.logging.Logger;

/**
 * Disables the maintenance mode
 */
public class DisableMaintenanceJob extends Job0<Void> {
@Override
public Value<Void> run() throws Exception {
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
		Logger logger = Logger.getLogger(getClass().getSimpleName());
		logger.severe("There should always be a maintenance entity when disabling!");
	}

	return immediate(null);
}
}

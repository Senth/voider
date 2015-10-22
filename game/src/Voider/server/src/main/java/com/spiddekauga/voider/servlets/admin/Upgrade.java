package com.spiddekauga.voider.servlets.admin;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;

import com.google.appengine.api.datastore.Entity;
import com.spiddekauga.appengine.DatastoreUtils;
import com.spiddekauga.appengine.DatastoreUtils.FilterWrapper;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.IMethodEntity;
import com.spiddekauga.voider.network.resource.UploadTypes;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables.CPublished;
import com.spiddekauga.voider.server.util.VoiderApiServlet;

/**
 * Does an upgrade for the server
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings({ "serial" })
public class Upgrade extends VoiderApiServlet<IMethodEntity> {
	@Override
	protected void onInit() {
		// Does nothing
	}

	@Override
	protected IEntity onRequest(IMethodEntity method) throws ServletException, IOException {
		indexLevelId();
		removeDeletedBlobs();

		getResponse().setContentType("text/html");
		getResponse().getWriter().append("DONE !");

		return null;
	}

	private void indexLevelId() {
		ArrayList<Entity> updateEntities = new ArrayList<>();

		FilterWrapper filterByLevel = new FilterWrapper(CPublished.TYPE, UploadTypes.LEVEL_DEF.toId());
		Iterable<Entity> entities = DatastoreUtils.getEntities(DatastoreTables.PUBLISHED, filterByLevel);
		for (Entity entity : entities) {
			entity.setProperty(CPublished.LEVEL_ID, entity.getProperty(CPublished.LEVEL_ID));
			updateEntities.add(entity);
		}

		DatastoreUtils.put(updateEntities);
	}

	private void removeDeletedBlobs() {

	}
}

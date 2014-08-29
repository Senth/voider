package com.spiddekauga.voider.servlets;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;

import com.google.appengine.api.datastore.Key;
import com.spiddekauga.appengine.DatastoreUtils;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.method.IMethodEntity;
import com.spiddekauga.voider.server.util.VoiderServlet;

/**
 * Does an upgrade for the server
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings("serial")
public class Upgrade extends VoiderServlet {
	@Override
	protected void onInit() {
		// Does nothing
	}

	@Override
	protected IEntity onRequest(IMethodEntity methodEntity) throws ServletException, IOException {
		List<Key> highscores = DatastoreUtils.getKeys("highscore");
		DatastoreUtils.delete(highscores);

		return null;
	}
}

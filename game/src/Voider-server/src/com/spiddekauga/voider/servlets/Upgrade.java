package com.spiddekauga.voider.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;

import javax.servlet.ServletException;

import net._01001111.text.LoremIpsum;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.Field;
import com.spiddekauga.appengine.DatastoreUtils;
import com.spiddekauga.appengine.SearchUtils;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.IMethodEntity;
import com.spiddekauga.voider.network.entities.resource.UploadTypes;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables.CAnalyticsSession;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables.CResourceComment;
import com.spiddekauga.voider.server.util.ServerConfig.TokenSizes;
import com.spiddekauga.voider.server.util.UserRepo;
import com.spiddekauga.voider.server.util.VoiderServlet;

/**
 * Does an upgrade for the server
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
@SuppressWarnings({ "serial", "unused" })
public class Upgrade extends VoiderServlet {
	@Override
	protected void onInit() {
		// Does nothing
	}

	@Override
	protected IEntity onRequest(IMethodEntity methodEntity) throws ServletException, IOException {
		resetAnalyticsSessions();

		getResponse().setContentType("text/html");
		getResponse().getWriter().append("DONE !");

		return null;
	}

	private void resetAnalyticsSessions() {
		Iterable<Entity> sessions = DatastoreUtils.getEntities(DatastoreTables.ANALYTICS_SESSION);
		for (Entity session : sessions) {
			session.setProperty(CAnalyticsSession.EXPORTED, false);
			mLogger.info("Session (" + session.getKey() + ") as exported!");
			DatastoreUtils.put(session);
		}
		DatastoreUtils.put(sessions);
	}

	private void indexDocuments() {
		HashMap<UploadTypes, ArrayList<Document>> documentsToAdd = new HashMap<>();

		for (Entity entity : DatastoreUtils.getEntities("published")) {
			UploadTypes uploadType = UploadTypes.fromId(DatastoreUtils.getIntProperty(entity, "type"));

			ArrayList<Document> documents = documentsToAdd.get(uploadType);

			if (documents == null) {
				documents = new ArrayList<>();
				documentsToAdd.put(uploadType, documents);
			}

			Document.Builder builder = Document.newBuilder();
			builder.setId(KeyFactory.keyToString(entity.getKey()));

			String nameTokens = SearchUtils.tokenizeAutocomplete((String) entity.getProperty("name"), TokenSizes.RESOURCE);
			builder.addField(Field.newBuilder().setName("name").setText(nameTokens).build());

			Date date = (Date) entity.getProperty("date");
			builder.addField(Field.newBuilder().setName("published").setDate(date));

			String creatorName = UserRepo.getUsername(entity.getParent());
			String creatorNameTokens = SearchUtils.tokenizeAutocomplete(creatorName.toLowerCase(), TokenSizes.RESOURCE);
			builder.addField(Field.newBuilder().setName("creator").setText(creatorNameTokens));

			String originalCreatorName = UserRepo.getUsername((Key) entity.getProperty("original_creator_key"));
			String originalCreatorNameTokens = SearchUtils.tokenizeAutocomplete(originalCreatorName.toLowerCase(), TokenSizes.RESOURCE);
			builder.addField(Field.newBuilder().setName("original_creator").setText(originalCreatorNameTokens));

			documents.add(builder.build());
		}

		// Add search documents
		boolean success = true;

		for (Entry<UploadTypes, ArrayList<Document>> entry : documentsToAdd.entrySet()) {
			String typeName = entry.getKey().toString();
			ArrayList<Document> documents = entry.getValue();

			success = SearchUtils.indexDocuments(typeName, documents);

			if (!success) {
				return;
			}
		}

	}

	private void createComments(Key key) {
		// Create 100 comments between today and a year ago
		Date now = new Date();
		final int yearMillis = 365 * 24 * 60 * 60 * 1000;
		Random random = new Random();

		final int COMMENTS = 100;
		LoremIpsum loremIpsum = new LoremIpsum();

		for (int i = 0; i < COMMENTS; ++i) {
			String comment = loremIpsum.words(25);
			long dateTime = random.nextInt(yearMillis);
			Date date = new Date(now.getTime() - dateTime);

			Entity entity = new Entity("resource_comment", key);
			entity.setProperty(CResourceComment.USERNAME, "player_" + i);
			entity.setUnindexedProperty(CResourceComment.COMMENT, comment);
			entity.setProperty(CResourceComment.DATE, date);
			DatastoreUtils.put(entity);
		}
	}

	/**
	 * Create empty level statistics
	 * @param key datastore key of the level entity to add empty statistics for
	 * @return true if successful, false otherwise
	 */
	private boolean createEmptyLevelStatistics(Key key) {
		Entity entity = new Entity(DatastoreTables.LEVEL_STAT.toString(), key);

		entity.setProperty("play_count", 0);
		entity.setProperty("bookmarks", 0);
		entity.setProperty("rating_sum", 0);
		entity.setProperty("ratings", 0);
		entity.setProperty("rating_avg", 0.0);
		entity.setProperty("clear_count", 0);

		Key statKey = DatastoreUtils.put(entity);

		return statKey != null;
	}
}

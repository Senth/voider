package com.spiddekauga.voider.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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
import com.spiddekauga.voider.network.misc.Motd.MotdTypes;
import com.spiddekauga.voider.network.resource.UploadTypes;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables.CAnalyticsEvent;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables.CAnalyticsScene;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables.CAnalyticsSession;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables.CMotd;
import com.spiddekauga.voider.server.util.ServerConfig.DatastoreTables.CPublished;
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
		createMotd();

		getResponse().setContentType("text/html");
		getResponse().getWriter().append("DONE !");

		return null;
	}

	private void createMotd() {
		Entity entity = new Entity(DatastoreTables.MOTD);

		Date nowDate = new Date();

		entity.setUnindexedProperty(CMotd.CREATED, nowDate);
		entity.setUnindexedProperty(CMotd.TITLE, "New MOTD :D");
		entity.setUnindexedProperty(CMotd.CONTENT, "This is my content.\nHope you like it :)");
		entity.setProperty(CMotd.EXPIRES, new Date(nowDate.getTime() + 1000000));
		DatastoreUtils.setUnindexedProperty(entity, CMotd.TYPE, MotdTypes.INFO);

		DatastoreUtils.put(entity);
	}

	private void clearAnalytics() {
		List<Key> deleteKeys = new ArrayList<>();

		deleteKeys.addAll(DatastoreUtils.getKeys(DatastoreTables.ANALYTICS_SESSION));
		deleteKeys.addAll(DatastoreUtils.getKeys(DatastoreTables.ANALYTICS_SCENE));
		deleteKeys.addAll(DatastoreUtils.getKeys(DatastoreTables.ANALYTICS_EVENT));

		DatastoreUtils.delete(deleteKeys);
	}

	private void changeEventTimeToDouble() {
		Iterable<Entity> events = DatastoreUtils.getEntities(DatastoreTables.ANALYTICS_EVENT);
		List<Entity> updated = new ArrayList<>();
		for (Entity event : events) {
			Object oldTime = event.getProperty(CAnalyticsEvent.TIME);
			double time = 0;
			if (oldTime instanceof Date) {
				Entity scene = DatastoreUtils.getEntity(event.getParent());
				Date sceneDate = (Date) scene.getProperty(CAnalyticsScene.START_TIME);
				Date date = (Date) event.getProperty(CAnalyticsEvent.TIME);
				long diffMs = date.getTime() - sceneDate.getTime();
				time = diffMs * 0.001;
			} else if (oldTime instanceof Double) {
				time = (Double) event.getProperty(CAnalyticsEvent.TIME);
			}
			event.setUnindexedProperty(CAnalyticsEvent.TIME, time);
			updated.add(event);
		}
		DatastoreUtils.put(updated);
	}

	private void resetAnalyticsSessions() {
		List<Entity> updated = new ArrayList<>();
		Iterable<Entity> sessions = DatastoreUtils.getEntities(DatastoreTables.ANALYTICS_SESSION);
		for (Entity session : sessions) {
			session.setProperty(CAnalyticsSession.EXPORTED, false);
			updated.add(session);
		}

		Iterable<Entity> scenes = DatastoreUtils.getEntities(DatastoreTables.ANALYTICS_SCENE);
		for (Entity scene : scenes) {
			scene.setProperty(CAnalyticsScene.EXPORTED, false);
			updated.add(scene);
		}

		Iterable<Entity> events = DatastoreUtils.getEntities(DatastoreTables.ANALYTICS_EVENT);
		for (Entity event : events) {
			event.setProperty(CAnalyticsEvent.EXPORTED, false);
			updated.add(event);
		}

		DatastoreUtils.put(updated);
	}

	private void indexDocuments() {
		HashMap<UploadTypes, ArrayList<Document>> documentsToAdd = new HashMap<>();

		for (Entity entity : DatastoreUtils.getEntities(DatastoreTables.PUBLISHED)) {
			UploadTypes uploadType = DatastoreUtils.getPropertyIdStore(entity, CPublished.TYPE, UploadTypes.class);


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

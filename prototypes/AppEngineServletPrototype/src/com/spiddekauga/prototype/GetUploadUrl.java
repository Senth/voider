package com.spiddekauga.prototype;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.util.UUID;
import java.util.logging.Logger;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.spiddekauga.appengine.DatastoreUtils;
import com.spiddekauga.utils.Buffers;
import com.spiddekauga.utils.ObjectCrypter;
import com.spiddekauga.voider.network.ActorDef;
import com.spiddekauga.voider.network.Def;
import com.spiddekauga.voider.network.EnemyDef;
import com.spiddekauga.voider.network.KryoFactory;
import com.spiddekauga.voider.network.Resource;

/**
 * Returns a valid upload url
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
@SuppressWarnings("serial")
public class GetUploadUrl extends HttpServlet {
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();

		Object object = readDecryptData(request);
		if (object != null) {
			boolean success = addResourceToDatastore(object);

			if (success) {
				String uploadUrl = blobstoreService.createUploadUrl("/enemyuploadfinished");
				response.addHeader("uploadUrl", uploadUrl);
				mLogger.info("Got upload url: " + uploadUrl);
			} else {
				mLogger.warning("Could not add resource to datastore");
			}
		} else {
			mLogger.warning("Could not read and decrypt data");
		}
	}

	/**
	 * Reads and decrypts the resource to upload and add it to the
	 * appropriate datastore table.
	 * @param request the request containing the data
	 * @return the read object
	 */
	private Object readDecryptData(HttpServletRequest request) {
		try {

			mLogger.info("Reading bytes");
			InputStream inputStream = request.getInputStream();
			byte[] encryptedObject = Buffers.readBytes(inputStream, TEMP_BUFFER_SIZE);
			mLogger.info("Decrypting object");
			ObjectCrypter objectCrypter = CryptConfig.getCrypter();
			byte[] decryptedObject = objectCrypter.decrypt(encryptedObject, byte[].class);
			mLogger.info("Decrypted size: " + decryptedObject.length);

			mLogger.info("Converting object using kryo");
			Input input = new Input(decryptedObject);
			Kryo kryo = KryoFactory.createKryo();
			return kryo.readClassAndObject(input);
		} catch (IOException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException | ClassNotFoundException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Add a resource to the datastore
	 * @param object the object to add to the datastore
	 * @return true if the object was added to the datastore
	 */
	private boolean addResourceToDatastore(Object object) {
		Entity entity = new Entity("enemy");

		if (object instanceof EnemyDef) {
			appendEnemyDef(entity, (EnemyDef) object);
		}

		if (object instanceof Resource) {
			appendResource(entity, (Resource) object);
		}

		if (object instanceof ActorDef) {
			appendActorDef(entity, (ActorDef) object);
		}

		if (object instanceof Def) {
			appendDef(entity, (Def) object);

			// TODO add dependencies
		}

		Key key = DatastoreUtils.mDatastore.put(entity);

		return key != null;
	}

	/**
	 * Append enemy information to the datastore
	 * @param entity the datastore entity to add information to
	 * @param enemyDef the enemy definition to append to the entity
	 */
	private void appendEnemyDef(Entity entity, EnemyDef enemyDef) {
		// TODO append weapon
		entity.setProperty("movementType", enemyDef.movementType);
		entity.setProperty("movementSpeed", enemyDef.movementSpeed);
	}

	/**
	 * Append resource information to the datastore
	 * @param entity the datastore entity to add information to
	 * @param resource the resource to append to the entity
	 */
	private void appendResource(Entity entity, Resource resource) {
		appendUuid(entity, "uuid", resource.id);
	}

	/**
	 * Append actor def information to the datastore
	 * @param entity the datastore entity to add information to
	 * @param actorDef the actor definition to append to the entity
	 */
	private void appendActorDef(Entity entity, ActorDef actorDef) {
		entity.setProperty("maxLife", actorDef.maxLife);
		Blob png = new Blob(actorDef.pngBytes);
		entity.setUnindexedProperty("png", png);
	}

	/**
	 * Append def information to the datastore
	 * @param entity the datastore entity to add information to
	 * @param def the definition to append to the entity
	 */
	private void appendDef(Entity entity, Def def) {
		entity.setProperty("name", def.name);
		entity.setProperty("originalCreator", def.originalCreator);
		entity.setProperty("creator", def.creator);
		entity.setProperty("description", def.description);
		entity.setProperty("date", def.date);
		entity.setProperty("revision", def.revision);
		appendUuid(entity, "copyParentId", def.copyParentId);
	}

	/**
	 * Appends a UUID to the entity with the specified name
	 * @param entity the entity to add the UUID to
	 * @param propertyName name of the property
	 * @param uuid the UUID to add to the entity
	 */
	private void appendUuid(Entity entity, String propertyName, UUID uuid) {
		if (uuid != null) {
			entity.setProperty(propertyName + "-least", uuid.getLeastSignificantBits());
			entity.setProperty(propertyName + "-most", uuid.getMostSignificantBits());
		}
	}

	/** Buffer size, 128kb */
	private static final int TEMP_BUFFER_SIZE = 128 * 1024;
	/** Logger */
	private static final Logger mLogger = Logger.getLogger(GetUploadUrl.class.getName());
}
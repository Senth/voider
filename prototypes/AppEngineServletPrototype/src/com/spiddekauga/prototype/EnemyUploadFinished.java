package com.spiddekauga.prototype;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.oreilly.servlet.multipart.MultipartParser;
import com.oreilly.servlet.multipart.ParamPart;
import com.oreilly.servlet.multipart.Part;
import com.spiddekauga.appengine.BlobUtils;
import com.spiddekauga.appengine.DatastoreUtils;
import com.spiddekauga.utils.ObjectCrypter;
import com.spiddekauga.voider.network.ActorDef;
import com.spiddekauga.voider.network.Def;
import com.spiddekauga.voider.network.EnemyDef;
import com.spiddekauga.voider.network.KryoFactory;
import com.spiddekauga.voider.network.Resource;
import com.spiddekauga.web.VoiderServlet;

/**
 * Enemy has been uploaded
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
@SuppressWarnings("serial")
public class EnemyUploadFinished extends VoiderServlet {
	@Override
	protected void onRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		mLogger.info("Upload done");
		List<BlobKey> uploadedBlobkeys = BlobUtils.getBlobKeysFromUpload(request);
		BlobKey blobKey = uploadedBlobkeys.get(0);
		mLogger.info("Blobkey: " + blobKey);

		boolean deleteBlob = true;

		if (mUser.isLoggedIn()) {
			String uploadType = request.getHeader("uploadType");
			mLogger.info("Upload type: " + uploadType);

			Object resource = readDecryptData(request);

			if (resource != null) {
				boolean resourceAdded = addResourceToDatastore(resource, blobKey);
				if (!resourceAdded) {
					mLogger.severe("Resource could not be added to the datastore; deleting blob.");
				} else {
					deleteBlob = false;
				}
			} else {
				mLogger.severe("Could not get resource; deleting blob.");
			}
		} else {
			mLogger.info("User is not logged in!");
			PrintWriter out = response.getWriter();
			out.print("Error: User is not logged in");
			out.flush();
		}

		if (deleteBlob) {
			BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
			blobstoreService.delete(blobKey);
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
			MultipartParser multipartParser = new MultipartParser(request, request.getContentLength());
			byte[] encryptedObject = null;
			if (multipartParser != null) {
				Part part;
				while ((part = multipartParser.readNextPart()) != null) {
					if (part.getName().equals("resource")) {
						if (part instanceof ParamPart) {
							encryptedObject = ((ParamPart) part).getValue();
							break;
						}
					}
				}
			}

			if (encryptedObject != null) {
				ObjectCrypter objectCrypter = CryptConfig.getCrypter();
				byte[] decryptedObject = objectCrypter.decrypt(encryptedObject, byte[].class);

				if (decryptedObject != null && decryptedObject.length > 0) {
					Input input = new Input(decryptedObject);
					Kryo kryo = KryoFactory.createKryo();
					return kryo.readClassAndObject(input);
				}
			}
		} catch (IOException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException | ClassNotFoundException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Add a resource to the datastore
	 * @param resource the resource to add to the datastore
	 * @param blobKey the blob key to store with the resource
	 * @return true if the object was added to the datastore
	 */
	private boolean addResourceToDatastore(Object resource, BlobKey blobKey) {
		Entity entity = new Entity("enemy");

		DatastoreUtils.setProperty(entity, "blobKey", blobKey);

		if (resource instanceof EnemyDef) {
			appendEnemyDef(entity, (EnemyDef) resource);
		}

		if (resource instanceof Resource) {
			appendResource(entity, (Resource) resource);
		}

		if (resource instanceof ActorDef) {
			appendActorDef(entity, (ActorDef) resource);
		}

		if (resource instanceof Def) {
			appendDef(entity, (Def) resource);

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
		DatastoreUtils.setProperty(entity, "movementType", enemyDef.movementType);
		DatastoreUtils.setProperty(entity, "movementSpeed", enemyDef.movementSpeed);
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
		DatastoreUtils.setProperty(entity, "maxLife", actorDef.maxLife);
		if (actorDef.pngBytes != null) {
			Blob png = new Blob(actorDef.pngBytes);
			entity.setUnindexedProperty("png", png);
		}
	}

	/**
	 * Append def information to the datastore
	 * @param entity the datastore entity to add information to
	 * @param def the definition to append to the entity
	 */
	private void appendDef(Entity entity, Def def) {
		DatastoreUtils.setProperty(entity, "name", def.name);
		DatastoreUtils.setProperty(entity, "originalCreator", def.originalCreator);
		DatastoreUtils.setProperty(entity, "creator", def.creator);
		DatastoreUtils.setProperty(entity, "description", def.description);
		DatastoreUtils.setProperty(entity, "date", def.date);
		DatastoreUtils.setProperty(entity, "revision", def.revision);
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
	private static final Logger mLogger = Logger.getLogger(EnemyUploadFinished.class.getName());
}

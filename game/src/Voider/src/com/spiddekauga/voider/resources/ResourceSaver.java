package com.spiddekauga.voider.resources;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.spiddekauga.utils.JsonWrapper;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.utils.ObjectCrypter;

/**
 * Saves resources into the appropriate place. The saver is also responsible
 * for encrypting the resources.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class ResourceSaver {

	/**
	 * Initializes the class with default parameters
	 */
	public static void init() {
		mCrypter = new ObjectCrypter(Config.Crypto.getFileKey());
	}

	/**
	 * Saves a resource that has a unique id
	 * @param resource the resource to save
	 */
	public static void save(IResource resource) {
		assert(mCrypter != null);

		// Update date and revision
		if (resource instanceof Def) {
			((Def) resource).updateDate();

			int nextRevision = ResourceDatabase.getLatestRevisionNumber(resource.getId());
			nextRevision++;
			((Def) resource).setRevision(nextRevision);
		}

		Json json = new JsonWrapper();
		String jsonString = json.toJson(resource);
		try {
			byte[] encryptedDef = mCrypter.encrypt(jsonString);

			Gdx.app.debug("ResourceSaver", "Encrypted (" + resource.getClass().getSimpleName() + ") " + resource.getId().toString());

			String filePath = ResourceDatabase.getFilePath(resource);
			FileHandle saveFile = Gdx.files.external(filePath);

			ResourceDatabase.addSavedResource(resource);

			// Save the file
			saveFile.writeBytes(encryptedDef, false);

			Gdx.app.debug("ResourceSaver", "Saved resource (" + resource.getClass().getSimpleName() + ") " + filePath);

		} catch (Exception e) {
			Gdx.app.error("ResourceSaver", "Could not encrypt message. Your file has not been saved!");
		}
	}

	/**
	 * Removes all resource of the specified type! BEWARE DRAGONS!
	 * @param resourceType removes all the resources of this type from the folder
	 */
	public static void clearResources(Class<? extends IResource> resourceType) {
		ResourceDatabase.removeAllOf(resourceType);
	}

	/** Private constructor to enfore that no instance exist */
	private ResourceSaver() {
		// Does nothing
	}

	/** Crypter used for encrypting/decrypting files */
	private static ObjectCrypter mCrypter = null;
}

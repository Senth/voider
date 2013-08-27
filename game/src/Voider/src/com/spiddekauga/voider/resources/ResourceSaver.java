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
		String filename = resource.getId().toString();

		// Check if the resource is a definition which contains revision numbering
		if (resource instanceof IResourceRevision) {
			filename += ".r" + String.valueOf(((IResourceRevision) resource).getRevision());
		}

		save(resource, filename);
	}

	/**
	 * Encrypts and saves the object at the appropriate place
	 * If an object already exist with the same filename, the existing object
	 * will be moved filename.bak
	 * @param object the object to encrypt and save
	 * @param filename the filename of the object to save
	 */
	private static void save(Object object, String filename) {
		assert(mCrypter != null);

		Json json = new JsonWrapper();
		String jsonString = json.toJson(object);
		try {
			byte[] encryptedDef = mCrypter.encrypt(jsonString);

			Gdx.app.debug("ResourceSaver", "Encrypted " + filename);

			String relativePath = ResourceNames.getDirPath(object.getClass());
			relativePath += filename;
			FileHandle saveFile = Gdx.files.external(relativePath);

			// File already exist, create backup
			if (saveFile.exists()) {
				saveFile.moveTo(Gdx.files.external(relativePath + Config.File.BACKUP_EXT));
			}

			// Save the file
			saveFile.writeBytes(encryptedDef, false);

			Gdx.app.debug("ResourceSaver", "Saved resource " + filename);

		} catch (Exception e) {
			Gdx.app.error("ResourceSaver", "Could not encrypt message. Your file has not been saved!");
		}
	}

	/**
	 * Removes all resource of the specified type! BEWARE DRAGONS!
	 * @param resourceType removes all the resources of this type from the folder
	 */
	public static void clearResources(Class<? extends IResource> resourceType) {
		try {
			String relativePath = ResourceNames.getDirPath(resourceType);
			FileHandle folder = Gdx.files.external(relativePath);

			if (folder.exists()) {
				folder.deleteDirectory();
			}
		} catch (UndefinedResourceTypeException e) {
			Gdx.app.error("ResourceSaver", "Could not clear resources of the type: " + e.getMessage());
		}
	}

	/** Private constructor to enfore that no instance exist */
	private ResourceSaver() {
		// Does nothing
	}

	/** Crypter used for encrypting/decrypting files */
	private static ObjectCrypter mCrypter = null;
}

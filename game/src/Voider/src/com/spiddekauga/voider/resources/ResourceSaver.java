package com.spiddekauga.voider.resources;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
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
	 * Initializes the class
	 * @param test if the class is used for testing, i.e. it will add
	 * a prefix of Config.File.TEST_PREFIX to all files it saves
	 */
	public static void init(boolean test) {
		mCrypter = new ObjectCrypter(Config.Crypto.getFileKey());
		mTesting = test;
	}

	/**
	 * Initializes the class with default parameters
	 */
	public static void init() {
		init(false);
	}

	/**
	 * Saves a resource that has a unique id
	 * @param resource the resource to save
	 */
	public static void save(IUniqueId resource) {
		save(resource, resource.getId().toString());
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

		Json json = new Json();
		String jsonString = json.toJson(object);
		try {
			byte[] encryptedDef = mCrypter.encrypt(jsonString);

			String relativePath = Config.File.STORAGE + ResourceNames.getDirPath(object.getClass());
			if (mTesting) {
				relativePath += Config.File.TEST_PREFIX;
			}
			relativePath += filename;
			FileHandle saveFile = Gdx.files.external(relativePath);

			// File already exist, create backup
			if (saveFile.exists()) {
				saveFile.moveTo(Gdx.files.external(relativePath + Config.File.BACKUP_EXT));
			}

			// Save the file
			saveFile.writeBytes(encryptedDef, false);

		} catch (Exception e) {
			Gdx.app.error("ResourceSaver", "Could not encrypt message. Your file has not been saved!");
		}
	}

	/** Private constructor to enfore that no instance exist */
	private ResourceSaver() {
		// Does nothing
	}

	/** If this class only is used for testing */
	private static boolean mTesting = false;
	/** Crypter used for encrypting/decrypting files */
	private static ObjectCrypter mCrypter = null;
}

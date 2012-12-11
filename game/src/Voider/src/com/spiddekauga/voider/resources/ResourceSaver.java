package com.spiddekauga.voider.resources;

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
	 */
	public static void init() {
		mCrypter = new ObjectCrypter(Config.Crypto.getFileKey());
	}

	/**
	 * Saves the definition to the appropriate place
	 * @param def the resource to save
	 */
	public static void save(Def def) {
		assert(mCrypter != null);

	}

	/** Private constructor to enfore that no instance exist */
	private ResourceSaver() {
		// Does nothing
	}

	/** Crypter used for encrypting/decrypting files */
	private static ObjectCrypter mCrypter = null;
}

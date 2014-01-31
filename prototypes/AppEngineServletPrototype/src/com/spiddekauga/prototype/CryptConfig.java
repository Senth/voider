package com.spiddekauga.prototype;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.spec.SecretKeySpec;

import com.spiddekauga.utils.ObjectCrypter;

/**
 * Crypto configuration
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class CryptConfig {
	/**
	 * @return the object crypter
	 */
	public static ObjectCrypter getCrypter() {
		if (mObjectCrypter == null) {
			initCrypter();
		}

		return mObjectCrypter;
	}

	/**
	 * Initializes the object crypter
	 */
	private static void initCrypter() {
		// Create file key
		try {
			MessageDigest sha;
			sha = MessageDigest.getInstance("SHA-1");
			byte[] hashedFileKey = sha.digest(FILE_KEY_BYTES);

			// Use only the first 128 bits
			hashedFileKey = Arrays.copyOf(hashedFileKey, 16);

			SecretKeySpec fileKey = new SecretKeySpec(hashedFileKey, "AES");

			mObjectCrypter = new ObjectCrypter(fileKey);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	/** Instance of the object crypter */
	private static ObjectCrypter mObjectCrypter = null;
	/** Salt for file key */
	private static final byte[] FILE_KEY_BYTES = { 15, 35, 68, 86, 57, 2, 99, 105, 127, -38, -100, -35, 35, 48, 68, -79, 95, -22, 0, 15, 0, 0,
		98, 15, 27, 35 };
}

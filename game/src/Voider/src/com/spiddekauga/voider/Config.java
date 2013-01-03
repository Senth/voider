package com.spiddekauga.voider;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.spec.SecretKeySpec;

import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Shape;

/**
 * Game configuration
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class Config {
	/**
	 * Initialization of the config class
	 */
	public static void init() {
		Crypto.init();
		Editor.init();
	}

	/**
	 * Dispose all objects that have been initialized
	 */
	public static void dispose() {
		Editor.PICKING_CIRCLE_SHAPE.dispose();
	}

	/**
	 * All graphical options
	 */
	public static class Graphics {
		/** If we shall use debug_renderer to display graphics instead of sprites (where applicable) */
		public final static boolean USE_DEBUG_RENDERER = true;
	}

	/**
	 * Editor options
	 */
	public static class Editor {
		/** Radius of all picking circles */
		public final static float PICKING_CIRCLE_RADIUS = 1.0f;
		/** Picking shape of the */
		public final static Shape PICKING_CIRCLE_SHAPE = new CircleShape();

		/**
		 * Initialization of the editor configuration
		 */
		public static void init() {
			PICKING_CIRCLE_SHAPE.setRadius(PICKING_CIRCLE_RADIUS);
		}
	}

	/**
	 * Files
	 */
	public static class File {
		/** The external directory used for storing game data */
		public final static String STORAGE = "Voider/";
		/** Backup extension */
		public final static String BACKUP_EXT = ".bak";
		/** The external directory used when storing test game data */
		public final static String TEST_STORAGE = "Voider-test/";
	}

	/**
	 * Encryption
	 */
	public static class Crypto {
		/**
		 * Initialization of the config class
		 */
		public static void init() {
			// Create file key
			try {
				MessageDigest sha;
				sha = MessageDigest.getInstance("SHA-1");
				byte[] hashedFileKey = sha.digest(FILE_KEY_BYTES);

				// Use only the first 128 bits
				hashedFileKey = Arrays.copyOf(hashedFileKey,  16);

				mFileKey = new SecretKeySpec(hashedFileKey, "AES");
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		/**
		 * Returns the file key
		 * @return key for encrypting/decrypting files
		 */
		public static SecretKeySpec getFileKey() {
			return mFileKey;
		}

		/** Salt for file key */
		private static final byte[] FILE_KEY_BYTES = {15, 35, 68, 86, 57, 2, 99, 105, 127, -38, -100, -35, 35, 48, 68, -79, 95, -22, 0, 15, 0, 0, 98, 15, 27, 35};
		/** The actual file key */
		private static SecretKeySpec mFileKey = null;
	}

	/**
	 * Private constructor so that no instance can be created
	 */
	private Config() {
		// Does nothing
	}
}

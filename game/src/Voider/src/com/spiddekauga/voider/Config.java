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
		if (Editor.PICKING_CIRCLE_SHAPE != null) {
			Editor.PICKING_CIRCLE_SHAPE.dispose();
		}
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
	 * Editor options
	 */
	public static class Editor {
		/**
		 * @return picking shape
		 */
		public static Shape getPickingShape() {
			return PICKING_CIRCLE_SHAPE;
		}

		/**
		 * Initialization of the editor configuration
		 */
		public static void init() {
			PICKING_CIRCLE_SHAPE = new CircleShape();
			PICKING_CIRCLE_SHAPE.setRadius(PICKING_CIRCLE_RADIUS);
		}

		/** Radius of all picking circles */
		private final static float PICKING_CIRCLE_RADIUS = 1.0f;
		/** Picking shape of the */
		private static Shape PICKING_CIRCLE_SHAPE = null;
	}

	/**
	 * Actors
	 */
	public static class Actor {
		/**
		 * Static terrain
		 */
		public static class Terrain {
			/** Default size of terrain circle */
			public final static float DEFAULT_CIRCLE_RADIUS = 2.0f;
		}
		/**
		 * Pickups
		 */
		public static class Pickup {
			/** Default radius for the pickups */
			public final static float RADIUS = 30f;
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
	 * Some general game settings
	 */
	public static class Game {
		/**
		 * Various mouse joint settings
		 */
		public static class MouseJoint {
			/** Update frequency of the mouse joint */
			public final static float FREQUENCY = 500;
			/** Maximum force the mouse joint has */
			public final static float FORCE_MAX = 10000;
		}
	}

	/**
	 * All graphical options
	 */
	public static class Graphics {
		/** If we shall use debug_renderer to display graphics instead of sprites (where applicable) */
		public final static boolean USE_DEBUG_RENDERER = true;
		/** Minimum length between two corners in a polygon */
		public final static float EDGE_LENGTH_MIN = 1.19209289550781250000e-7F * 1.19209289550781250000e-7F;
		/** Default width of the graphics */
		public final static float WIDTH = 800;
		/** Default height of the graphics */
		public final static float HEIGHT = 480;
		/** World scaling factor */
		public final static float WORLD_SCALE = 0.1f;
	}

	/**
	 * Input options
	 */
	public static class Input {
		/** How many seconds for a double click? */
		public final static float DOUBLE_CLICK_TIME = 0.2f;
	}

	/** Revision of the game, as in code. This allows files of older revisions
	 * to be loaded into new revisions */
	public final static int REVISION = 1;

	/**
	 * Private constructor so that no instance can be created
	 */
	private Config() {
		// Does nothing
	}
}

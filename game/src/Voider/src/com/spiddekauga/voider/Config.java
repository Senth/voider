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
			public final static float RADIUS = 1f;
		}
		/**
		 * Enemies
		 */
		public static class Enemy {
			/** How close the enemy should be to a path point before it goes to the next */
			public final static float PATH_NODE_CLOSE_SQ = 0.5f * 0.5f;
			/** Minimum angle if we shall turn */
			public final static float TURN_ANGLE_MIN = 0.1f;
			/** When the rotation shall be slowed down */
			public final static float ROTATION_SLOW_DOWN_ANGLE = 3;
			/** How much the rotation shall slow down */
			public final static float ROTATION_SLOW_DOWN_RATE = 0.1f;
			/** When the rotation shall be speeded */
			public final static float ROTATION_SPEED_UP_ANGLE = 10;
			/** How much the rotation shall speed up */
			public final static float ROTATION_SPSEED_UP_RATE = 3;
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

		/**
		 * Enemy editor options
		 */
		public static class Enemy {
			/** How long time ONCE enemy should have reached the goal before it is reset.
			 * In EnemyEditor */
			public final static float PATH_ONCE_RESET_TIME = 2;
			/** Minimum speed of enemies */
			public final static float MOVE_SPEED_MIN = 0.5f;
			/** Maximum speed of enemies */
			public final static float MOVE_SPEED_MAX = 40;
			/** Default movement speed */
			public final static float MOVE_SPEED_DEFAULT = 10;
			/** Step size for movement speed slider */
			public final static float MOVE_SPEED_STEP_SIZE = 0.1f;
			/** Minimum turning speed */
			public final static float TURN_SPEED_MIN = 5;
			/** Maximum turning speed */
			public final static float TURN_SPEED_MAX = 50;
			/** Default turning speed */
			public final static float TURN_SPEED_DEFAULT = 20;
			/** Step size for turning speed slider */
			public final static float TURN_SPEED_STEP_SIZE = 1;
			/** AI distance slider minimum */
			public final static float AI_DISTANCE_MIN = 0;
			/** Default minimum distance for the AI */
			public final static float AI_DISTANCE_MIN_DEFAULT = 20;
			/** AI distance slider maximum */
			public final static float AI_DISTANCE_MAX = 70;
			/** Default maximum distance for teh AI */
			public final static float AI_DISTANCE_MAX_DEFAULT = 40;
			/** AI distance slider step size */
			public final static float AI_DISTANCE_STEP_SIZE = 1;
			/** Text field size for numbers */
			public final static float TEXT_FIELD_NUMBER_WIDTH = 70;
			/** Label padding in front of a slider */
			public final static float LABEL_PADDING_BEFORE_SLIDER = 8;
			/** If the enemy shall saty on screen */
			public final static boolean STAY_ON_SCREEN_DEFAULT = true;
			/** Minimum random movement time */
			public final static float RANDOM_MOVEMENT_TIME_MIN = 0.1f;
			/** Maximum random movement time */
			public final static float RANDOM_MOVEMENT_TIME_MAX = 5;
			/** Default minimum random movement time */
			public final static float RANDOM_MOVEMENT_TIME_MIN_DEFAULT = 0.5f;
			/** Default maximum random movement time */
			public final static float RANDOM_MOVEMENT_TIME_MAX_DEFAULT = 2;
			/** Slide step size for random movement time */
			public final static float RANDOM_MOVEMENT_TIME_STEP_SIZE = 0.1f;
			/** If random movement shall be turned on by default */
			public final static boolean RANDOM_MOVEMENT_DEFAULT = false;
		}

		/** Radius of all picking circles */
		private final static float PICKING_CIRCLE_RADIUS = 1.0f;
		/** Picking shape of the */
		private static Shape PICKING_CIRCLE_SHAPE = null;
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

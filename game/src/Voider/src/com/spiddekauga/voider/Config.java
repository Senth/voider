package com.spiddekauga.voider;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.spec.SecretKeySpec;

import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Shape;
import com.spiddekauga.voider.game.actors.ActorFilterCategories;
import com.spiddekauga.voider.game.actors.ActorShapeTypes;

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
		 * Bullet
		 */
		public static class Bullet {
			/** How often to check if the bullet is out of bounds */
			public final static float CHECK_OUT_OF_BOUNDS_TIME = 1;
			/** Maximum number of bullets */
			public final static int BULLETS_MAX = 1000;
		}
		/**
		 * Enemies
		 */
		public static class Enemy {
			/** How close the enemy should be to a path point before it goes to the next */
			public final static float PATH_NODE_CLOSE_SQ = 0.5f * 0.5f;
			/** Minimum angle if we shall turn */
			public final static float TURN_ANGLE_MIN = 0.5f;
		}
		/**
		 * Pickups
		 */
		public static class Pickup {
			/** Default radius for the pickups */
			public final static float RADIUS = 1f;
		}
		/**
		 * Player
		 */
		public static class Player {
			/** How many seconds we shall save the player's position, in seconds */
			public static float RECENT_POS_SAVE_TIME = 1;
		}
		/**
		 * Static terrain
		 */
		public static class Terrain {
			/** Default size of terrain circle */
			public final static float DEFAULT_CIRCLE_RADIUS = 2.0f;
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
		 * @return picking fixture
		 */
		public static FixtureDef getPickingFixture() {
			return PICKING_CIRCLE_FIXTURE;
		}

		/**
		 * Initialization of the editor configuration
		 */
		public static void init() {
			PICKING_CIRCLE_SHAPE = new CircleShape();
			PICKING_CIRCLE_SHAPE.setRadius(PICKING_CIRCLE_RADIUS);
			PICKING_CIRCLE_FIXTURE = new FixtureDef();
			PICKING_CIRCLE_FIXTURE.filter.categoryBits = ActorFilterCategories.NONE;
			PICKING_CIRCLE_FIXTURE.shape = PICKING_CIRCLE_SHAPE;
		}

		/**
		 * General editor options
		 */
		public static class Actor {
			/**
			 * Visual options
			 */
			public class Visual {
				/** Minimum rotate speed of the weapon */
				public final static float ROTATE_SPEED_MIN = -720;
				/** Maximum rotate speed of the weapon */
				public final static float ROTATE_SPEED_MAX = 720;
				/** Default rotate speed of the weapon */
				public final static float ROTATE_SPEED_DEFAULT = 0;
				/** Step size of rotate speed */
				public final static float ROTATE_SPEED_STEP_SIZE = 1;
			}

			/**
			 * Collision options
			 */
			public class Collision {
				/** Minimum collision damage */
				public final static float DAMAGE_MIN = 0;
				/** Maximum collision damage */
				public final static float DAMAGE_MAX = 100;
				/** Default collision damage */
				public final static float DAMAGE_DEFAULT = 0;
				/** Step size of collision damage */
				public final static float DAMAGE_STEP_SIZE = 1;
			}
		}

		/**
		 * Bullet editor options
		 */
		public static class Bullet {
			/**
			 * Visuals
			 */
			public static class Visual {
				/** Minimum radius for the enemy (when it's a circle) */
				public final static float RADIUS_MIN = 0.2f;
				/** Maximum radius for the enemy (when it's a circle) */
				public final static float RADIUS_MAX = 6f;
				/** Default radius of the enemy (when it's a circle) */
				public final static float RADIUS_DEFAULT = 0.4f;
				/** Step size for radius */
				public final static float RADIUS_STEP_SIZE = 0.1f;
				/** Minimum width/height for the enemy (when it's a rectangle/triangle */
				public final static float SIZE_MIN = RADIUS_MIN * 2;
				/** Maximum width/height for the enemy (when it's a rectangle/triangle */
				public final static float SIZE_MAX = RADIUS_MAX * 2;
				/** Default width/height for the enemy (when it's a rectangle/triangle */
				public final static float SIZE_DEFAULT = RADIUS_DEFAULT * 2;
				/** Step size for the enemy width/height */
				public final static float SIZE_STEP_SIZE = RADIUS_STEP_SIZE;
				/** Default shape type of the enemy */
				public final static ActorShapeTypes SHAPE_DEFAULT = ActorShapeTypes.LINE;
			}
		}

		/**
		 * Enemy editor options
		 */
		public static class Enemy {

			/**
			 * Enemy movement
			 */
			public static class Movement {
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

			/**
			 * Visuals
			 */
			public static class Visual {
				/** Minimum radius for the enemy (when it's a circle) */
				public final static float RADIUS_MIN = 0.2f;
				/** Maximum radius for the enemy (when it's a circle) */
				public final static float RADIUS_MAX = 10f;
				/** Default radius of the enemy (when it's a circle) */
				public final static float RADIUS_DEFAULT = 1;
				/** Step size for radius */
				public final static float RADIUS_STEP_SIZE = 0.1f;
				/** Minimum width/height for the enemy (when it's a rectangle/triangle */
				public final static float SIZE_MIN = RADIUS_MIN * 2;
				/** Maximum width/height for the enemy (when it's a rectangle/triangle */
				public final static float SIZE_MAX = RADIUS_MAX * 2;
				/** Default width/height for the enemy (when it's a rectangle/triangle */
				public final static float SIZE_DEFAULT = RADIUS_DEFAULT * 2;
				/** Step size for the enemy width/height */
				public final static float SIZE_STEP_SIZE = RADIUS_STEP_SIZE;
				/** Default shape type of the enemy */
				public final static ActorShapeTypes SHAPE_DEFAULT = ActorShapeTypes.CIRCLE;
			}

			/**
			 * Weapon aim
			 */
			public static class Weapon {
				/** Minimum rotate speed of the weapon */
				public final static float ROTATE_SPEED_MIN = -720;
				/** Maximum rotate speed of the weapon */
				public final static float ROTATE_SPEED_MAX = 720;
				/** Default rotate speed of the weapon */
				public final static float ROTATE_SPEED_DEFAULT = 20;
				/** Step size of rotate speed */
				public final static float ROTATE_SPEED_STEP_SIZE = 1;
				/** Minimum starting angle */
				public final static float START_ANGLE_MIN = 0;
				/** Maximum start angle */
				public final static float START_ANGLE_MAX = 360;
				/** Default starting angle */
				public final static float START_ANGLE_DEFAULT = 180;
				/** Step size of starting angle */
				public final static float START_ANGLE_STEP_SIZE = 1;
			}
		}

		/**
		 * Level editor
		 */
		public static class Level {
			/**
			 * Enemy options
			 */
			public static class Enemy {
				/** Minimum number of enemies */
				public final static float ENEMIES_MIN = 1;
				/** Maximum number of enemies */
				public final static float ENEMIES_MAX = 20;
				/** Enemies step size */
				public final static float ENEMIES_STEP_SIZE = 1;
				/** Minimum delay between enemy spawns */
				public final static float DELAY_BETWEEN_MIN = 0;
				/** Maximum delay between enemy spawns */
				public final static float DELAY_BETWEEN_MAX = 10;
				/** Default delay time between enemy spawns */
				public final static float DELAY_BETWEEN_DEFAULT = 2;
				/** Stepz size of enemy delay time */
				public final static float DELAY_BETWEEN_STEP_SIZE = 0.1f;
				/** Minimum start trigger delay */
				public final static float TRIGGER_ACTIVATE_DELAY_MIN = 0;
				/** Maximum start trigger delay */
				public final static float TRIGGER_ACTIVATE_DELAY_MAX = 30;
				/** Default trigger delay */
				public final static float TRIGGER_ACTIVATE_DELAY_DEFAULT = 0;
				/** Step sizeof trigger delay */
				public final static float TRIGGER_ACTIVATE_DELAY_STEP_SIZE = 0.1f;
				/** Minimum deactivation trigger delay */
				public final static float TRIGGER_DEACTIVATE_DELAY_MIN = 0;
				/** Maximum deactivation trigger delay */
				public final static float TRIGGER_DEACTIVATE_DELAY_MAX = 300;
				/** Default deactivation trigger delay */
				public final static float TRIGGER_DEACTIVATE_DELAY_DEFAULT = 0;
				/** Step size of deactivation trigger delay */
				public final static float TRIGGER_DEACTIVATE_DELAY_STEP_SIZE = 1;
			}

			/** Enemy snap distance to a path */
			public final static float ENEMY_SNAP_PATH_DISTANCE = 2;
			/** Enemy snap distance squared */
			public final static float ENEMY_SNAP_PATH_DISTANCE_SQ = ENEMY_SNAP_PATH_DISTANCE * ENEMY_SNAP_PATH_DISTANCE;
		}

		/**
		 * Weapon specifics
		 */
		public static class Weapon {
			/** Minimum bullet speed */
			public final static float BULLET_SPEED_MIN = 1;
			/** Maximum bullet speed */
			public final static float BULLET_SPEED_MAX = 50;
			/** Default bullet speed */
			public final static float BULLET_SPEED_DEFAULT = 20;
			/** Step size of bullet speed */
			public final static float BULLET_SPEED_STEP_SIZE = 1;
			/** Minimum cooldown, used for random effect */
			public final static float COOLDOWN_MIN = 0.1f;
			/** Maximum cooldown, used for random effect */
			public final static float COOLDOWN_MAX = 10;
			/** Default minimum cooldown */
			public final static float COOLDOWN_MIN_DEFAULT = 1;
			/** Default maximum cooldown */
			public final static float COOLDOWN_MAX_DEFAULT = COOLDOWN_MIN_DEFAULT;
			/** Step size of cooldown */
			public final static float COOLDOWN_STEP_SIZE = 0.1f;
			/** Minimum bullet damage */
			public final static float DAMAGE_MIN = 1;
			/** Maximum bullet damage  */
			public final static float DAMAGE_MAX = 100;
			/** Default bullet damage */
			public final static float DAMAGE_DEFAULT = 5;
			/** Step size of bullet damage */
			public final static float DAMAGE_STEP_SIZE = 1;
		}

		/** Select definition scene, maximum width of definition */
		public static float SELECT_DEF_WIDTH_MAX = 200;
		/** Text field size for numbers */
		public final static float TEXT_FIELD_NUMBER_WIDTH = 70;
		/** Label padding in front of a slider */
		public final static float LABEL_PADDING_BEFORE_SLIDER = 8;

		/** Radius of all picking circles */
		private final static float PICKING_CIRCLE_RADIUS = 1.0f;
		/** Picking shape */
		private static Shape PICKING_CIRCLE_SHAPE = null;
		/** Picking fixture */
		private static FixtureDef PICKING_CIRCLE_FIXTURE = null;
		/** Default pick size */
		public final static float PICK_SIZE_DEFAULT = 0.0001f;
		/** Path pick size */
		public final static float PICK_PATH_SIZE = 0.5f;
		/** Trigger pick size */
		public final static float PICK_TRIGGER_SIZE = PICK_PATH_SIZE;
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
	 * Some GUI options
	 */
	public static class Gui {
		/** Temporary GUI name when using an invoker */
		public static String GUI_INVOKER_TEMP_NAME = "invoker";
		/** Seconds before text field commands aren't combinable */
		public static float TEXT_FIELD_COMBINABLE_WITHIN = 1;
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
	/** If debugging tests shall be activate */
	public final static boolean DEBUG_TESTS = true;
	/**
	 * Private constructor so that no instance can be created
	 */
	private Config() {
		// Does nothing
	}
}

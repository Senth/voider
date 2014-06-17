package com.spiddekauga.voider;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.spec.SecretKeySpec;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.spiddekauga.utils.ShapeRendererEx;
import com.spiddekauga.voider.Config.Debug.Builds;
import com.spiddekauga.voider.game.actors.ActorShapeTypes;
import com.spiddekauga.voider.resources.IResourceEditorRender;
import com.spiddekauga.voider.resources.IResourcePosition;
import com.spiddekauga.voider.resources.IResourceRender;

/**
 * Game configuration
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class Config {
	/**
	 * Dispose all objects that have been initialized
	 */
	public static void dispose() {
		// Does nothing
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
			/** Default deactivate time for AI enemies */
			public final static float DEACTIVATE_TIME_DEFAULT = 20;
		}

		/**
		 * Pickups
		 */
		public static class Pickup {
			/** Default radius for the pickups */
			public final static float RADIUS = 2;
		}

		/**
		 * Static terrain
		 */
		public static class Terrain {
			/** Default size of terrain circle */
			public final static float DEFAULT_CIRCLE_RADIUS = 3f;
		}

		/**
		 * Player
		 */
		public static class Player {
			/** Maximum player heath */
			public final static float HEALTH_MAX = 100;
		}

		/** Border width of all actors */
		public final static float BORDER_WIDTH = 0.5f;
		/** Outline color */
		public final static Color OUTLINE_COLOR = Color.ORANGE;
		/** Closing outline color (color from corner.end -> corner.begin) */
		public final static Color OUTLINE_CLOSE_COLOR = Color.PINK;
		/** Default name of definitions */
		public final static String NAME_DEFAULT = "(Unnamed)";
		/** Minimum name length */
		public final static int NAME_LENGTH_MIN = 3;
		/** Texture size when saving */
		public final static int SAVE_TEXTURE_SIZE = 128;
	}

	/**
	 * Cache
	 */
	public static class Cache {
		/**
		 * How long time resource when browsing should be available after initial fetch,
		 * in seconds
		 */
		public static final int RESOURCE_BROWSE_TIME = 300;
	}

	/**
	 * Encryption
	 */
	public static class Crypto {
		/**
		 * Initialization of the config class
		 */
		public static void init() {
			try {
				// Create file key
				MessageDigest sha;
				sha = MessageDigest.getInstance("SHA-1");
				byte[] hashedFileKey = sha.digest(FILE_KEY_BYTES);

				// Use only the first 128 bits
				hashedFileKey = Arrays.copyOf(hashedFileKey, 16);

				mFileKey = new SecretKeySpec(hashedFileKey, "AES");

				// Create password key
				sha = MessageDigest.getInstance("SHA-1");
				byte[] hashedPasswordKey = sha.digest(PASSWORD_KEY_BYTES);

				// Use only the first 128 bits
				hashedPasswordKey = Arrays.copyOf(hashedPasswordKey, 16);

				mPasswordKey = new SecretKeySpec(hashedPasswordKey, "AES");
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
		}

		/**
		 * @return key for encrypting/decrypting files
		 */
		public static SecretKeySpec getFileKey() {
			return mFileKey;
		}

		/**
		 * @return the password key
		 */
		public static SecretKeySpec getPasswordKey() {
			return mPasswordKey;
		}

		/** Salt for file key */
		private static final byte[] FILE_KEY_BYTES = { 15, 35, 68, 86, 57, 2, 99, 105, 127, -38, -100, -35, 35, 48, 68, -79, 95, -22, 0, 15, 0, 0,
				98, 15, 27, 35 };
		/** Salt for file key */
		private static final byte[] PASSWORD_KEY_BYTES = { 11, 120, 8, 86, 5, 22, 9, 15, -88, 38, 100, -35, 35, 35, -6, 79, 95, 22, 22, 2, 15, 65, 8,
				-15, -27, -35 };
		/** The actual file key */
		private static SecretKeySpec mFileKey = null;
		/** The actual password key */
		private static SecretKeySpec mPasswordKey = null;

		static {
			init();
		}
	}

	/**
	 * Debug options
	 */
	public static class Debug {
		/**
		 * Control over debug messages
		 */
		public static class Messages {
			/** If loading/unloading debug messages should be turned on/off */
			public static final boolean LOAD_UNLOAD = true;
			/**
			 * If loading/unloading including number of times a resource has been loaded
			 * into a scene
			 */
			public static final boolean LOAD_UNLOAD_EVERY_TIME = true;
			/** If loading/unloading dependencies should be displayed */
			public static final boolean LOAD_UNLOAD_DEPENDENCIES = true;
		}

		/**
		 * If debugging tests shall be activate. This causes extra runtime, but checks so
		 * that none of the checks are broken.
		 */
		public static boolean DEBUG_TESTS = true;
		/** Skip loading text */
		public static final boolean SKIP_LOADING_TIME = true;
		/** Build level */
		public static final Builds BUILD = Builds.DEV_SERVER;
		/** Set to true to turn on the exception handler */
		public static boolean EXCEPTION_HANDLER = isBuildOrAbove(Builds.NIGHTLY) || false;
		/** Set to true in JUNIT tests */
		public static boolean JUNIT_TEST = false;
		/** Logging verbosity */
		public static final int LOG_VERBOSITY = isBuildOrAbove(Builds.BETA) ? Application.LOG_ERROR : Application.LOG_DEBUG;


		/**
		 * All different builds
		 */
		public enum Builds {
			// Front placement is development, later -> more release ready
			/** Local development */
			DEV_LOCAL,
			/** Server development */
			DEV_SERVER,
			/** Released to co-developers */
			NIGHTLY,
			/** Beta tests */
			BETA,
			/** Release to public, i.e. google play */
			RELEASE,
		}

		/**
		 * @param build the build to check if we're at or above
		 * @return true if the current build is equal to or above (later) than the
		 *         specified build
		 */
		public static boolean isBuildOrAbove(Builds build) {
			return BUILD.ordinal() >= build.ordinal();
		}

		/**
		 * @param build the build to check if we're at or below
		 * @return true if the current build is equal to or below (development) than the
		 *         specified build
		 */
		public static boolean isBuildOrBelow(Builds build) {
			return BUILD.ordinal() <= build.ordinal();
		}
	}

	/**
	 * Editor options
	 */
	public static class Editor {
		/**
		 * General editor options
		 */
		public static class Actor {
			/**
			 * Visual options
			 */
			public class Visual {
				/** Minimum rotate speed of an actor */
				public final static float ROTATE_SPEED_MIN = -720;
				/** Maximum rotate speed of an actor */
				public final static float ROTATE_SPEED_MAX = 720;
				/** Default rotate speed of an actor */
				public final static float ROTATE_SPEED_DEFAULT = 0;
				/** Step size of rotate speed */
				public final static float ROTATE_SPEED_STEP_SIZE = 1;

				/** Minimum distance for new corners when drawing */
				public final static float DRAW_NEW_CORNER_MIN_DIST_SQ = 0.75f * 0.75f;
				/**
				 * Minimum angle between corners, if less than this, the corner will be
				 * removed
				 */
				public final static float DRAW_CORNER_ANGLE_MIN = 9;
				/** Maximum squared distance a new corners might be from a line segment */
				public final static float NEW_CORNER_DIST_MAX_SQ = 3 * 3;
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
				public final static float SIZE_MIN = 0.1f;
				/** Maximum width/height for the enemy (when it's a rectangle/triangle */
				public final static float SIZE_MAX = RADIUS_MAX * 2;
				/** Default width/height for the enemy (when it's a rectangle/triangle */
				public final static float SIZE_DEFAULT = RADIUS_DEFAULT * 2;
				/** Step size for the enemy width/height */
				public final static float SIZE_STEP_SIZE = RADIUS_STEP_SIZE;
				/** Default shape type of the enemy */
				public final static ActorShapeTypes SHAPE_DEFAULT = ActorShapeTypes.RECTANGLE;

				/** Minimum distance for new corners when drawing */
				public final static float DRAW_NEW_CORNER_MIN_DIST_SQ = 0.25f * 0.25f;
				/**
				 * Minimum angle between corners, if less than this, the corner will be
				 * removed
				 */
				public final static float DRAW_CORNER_ANGLE_MIN = 5;
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
				/**
				 * How long time ONCE enemy should have reached the goal before it is
				 * reset. In EnemyEditor
				 */
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
				/** Size of enemy buttons in the scroll list */
				public final static int ADD_BUTTON_SIZE = 60;
				/** Maximum percentage of width the add enemy table is allowed for */
				public final static float ADD_ENEMY_TABLE_MAX_WIDTH = 0.25f;
			}

			/**
			 * Path options
			 */
			public static class Path {
				/** Width of displayed path for enemies */
				public final static float WIDTH = 0.5f;
				/** Start color of the path */
				public final static Color START_COLOR = new Color(0, 0.6f, 0, 1);
				/** End color of the path */
				public final static Color END_COLOR = new Color(0.6f, 0, 0, 1);
			}

			/**
			 * Trigger options
			 */
			public static class Trigger {
				/** Color of the trigger */
				public final static Color COLOR = new Color(0, 0, 0.6f, 1);
				/** Width of screen at */
				public final static float SCREEN_AT_WIDTH = 0.2f;
				/** Width of trigger on enemies */
				public final static float ENEMY_WIDTH = 0.4f;
			}

			/** Enemy snap distance to a path */
			public final static float ENEMY_SNAP_PATH_DISTANCE = 2;
			/** Enemy snap distance squared */
			public final static float ENEMY_SNAP_PATH_DISTANCE_SQ = ENEMY_SNAP_PATH_DISTANCE * ENEMY_SNAP_PATH_DISTANCE;
			/** Width of the options message box (in percentage of window width) */
			public final static float OPTIONS_WIDTH = 0.8f;
			/** Height of the options message box (in percentage of window height) */
			public final static float OPTIONS_HEIGHT = 0.7f;
			/** Minimum level speed */
			public final static float LEVEL_SPEED_MIN = 1;
			/** Maximum level speeed */
			public final static float LEVEL_SPEED_MAX = 50;
			/** Default level speed */
			public final static float LEVEL_SPEED_DEFAULT = 15;
			/** Step size of level speeed */
			public final static float LEVEL_SPEED_STEP_SIZE = 1;
		}

		/**
		 * Path specifics
		 */
		public static class Path {
			/** Vector of default path (only one corner) */
			public final static Vector2 DEFAULT_ADD_PATH = new Vector2(-1, 0);
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
			/** Maximum bullet damage */
			public final static float DAMAGE_MAX = 100;
			/** Default bullet damage */
			public final static float DAMAGE_DEFAULT = 5;
			/** Step size of bullet damage */
			public final static float DAMAGE_STEP_SIZE = 1;
		}


		/** Maximum name length */
		public final static int NAME_LENGTH_MAX = 16;
		/** Maximum length of description */
		public final static int DESCRIPTION_LENGTH_MAX = 256;
		/** Maximum length of story */
		public final static int STORY_LENGTH_MAX = 512;
		/** Auto save time, in seconds */
		public final static float AUTO_SAVE_TIME_FORCED = 240;
		/** Auto save time after inactivity, in seconds */
		public final static float AUTO_SAVE_TIME_ON_INACTIVITY = 20;

		/** Corner pick color */
		public final static Color CORNER_COLOR = new Color(0.75f, 0, 0, 1);
		/** Center offset pick color */
		public final static Color CENTER_OFFSET_COLOR = new Color(0, 0.75f, 0, 1);
		/** Selected color overlay */
		public final static Color SELECTED_COLOR = new Color(1, 1, 1, 0.45f);
		/** Default pick size */
		public final static float PICK_SIZE_DEFAULT = 0.0001f;
		/** Path pick size */
		public final static float PICK_PATH_SIZE = 0.5f;
		/** Trigger pick size */
		public final static float PICK_TRIGGER_SIZE = PICK_PATH_SIZE;
		/** Radius of all picking circles */
		public final static float PICKING_CIRCLE_RADIUS_EDITOR = 1;
		/** Radius of level editor picking circles */
		public final static float PICKING_CIRCLE_RADIUS_LEVEL_EDITOR = 2;
		/** Brush draw add color */
		public final static Color BRUSH_ADD_COLOR = Color.GREEN;
		/** Brush erase color */
		public final static Color BRUSH_ERASE_COLOR = Color.RED;
		/** Selection brush color */
		public final static Color BRUSH_SELECTION_COLOR = Color.GRAY;
	}

	/**
	 * Explore options
	 */
	public static class Explore {
		/** Minimum number of string characters for search to begin */
		public static final int SEARCH_LENGTH_MIN = 3;
	}

	/**
	 * Files
	 */
	public static class File {
		/** Preferences name prefix */
		public final static String PREFERENCE_PREFIX;
		/** The external directory used for storing game data */
		public final static String STORAGE;
		/** Revision number length */
		public final static int REVISION_LENGTH = 10;
		/** Uses external images, etc. instead of internal for resources */
		public final static boolean USE_EXTERNAL_RESOURCES = Debug.BUILD == Builds.NIGHTLY;
		/** Database filename */
		public final static String DB_FILENAME = "Voider.db";
		/** User storage */
		private static String mUserStorage;
		/** User preferences prefix */
		private static String mUserPreferencesPrefix;

		/**
		 * @return user preferences prefix
		 */
		public static String getUserPreferencesPrefix() {
			return mUserPreferencesPrefix;
		}

		/**
		 * @return user storage path
		 */
		public static String getUserStorage() {
			return mUserStorage;
		}

		/**
		 * Set the user paths
		 * @param username username of the user to get the path
		 */
		public static void setUserPaths(String username) {
			mUserStorage = STORAGE + username + "/";
			mUserPreferencesPrefix = PREFERENCE_PREFIX + "_" + username + "_";

			// Create folder if it doesn't exist
			FileHandle folder = Gdx.files.external(mUserStorage);
			if (!folder.exists()) {
				folder.mkdirs();
			}
		}

		static {
			// Set storage
			if (Debug.JUNIT_TEST) {
				PREFERENCE_PREFIX = "Voider-JUnit";
			} else {
				switch (Debug.BUILD) {
				case RELEASE:
					PREFERENCE_PREFIX = "Voider";
					break;

				case BETA:
					PREFERENCE_PREFIX = "Voider-beta";
					break;

				case NIGHTLY:
					PREFERENCE_PREFIX = "Voider-nightly";
					break;

				case DEV_SERVER:
					PREFERENCE_PREFIX = "Voider-dev-server";
					break;

				case DEV_LOCAL:
					PREFERENCE_PREFIX = "Voider-local";
					break;

				default:
					PREFERENCE_PREFIX = "Voider-unknown";
					break;
				}
			}
			STORAGE = PREFERENCE_PREFIX + "/";
			setUserPaths("(None)");
		}
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

		/**
		 * How many characters the score should contain, i.e. number of leading zeros
		 */
		public final static int SCORE_CHARACTERS = 10;
		/** Score multiplier, used to create a greater score */
		public final static float SCORE_MULTIPLIER = 10;
		/** Lives offset */
		public final static float LIVES_OFFSET_POSITION = 1;
		/** Border threshold, when border is too out of sync, it will get synced */
		public final static float BORDER_SYNC_THRESHOLD = 0.1f;
	}

	/**
	 * All graphical options
	 */
	public static class Graphics {
		/**
		 * If we shall use debug_renderer to display graphics instead of sprites (where
		 * applicable)
		 */
		public final static boolean USE_DEBUG_RENDERER = false;
		/** Renders regular graphics */
		public final static boolean USE_RELEASE_RENDERER = true;
		/** Epsilon for box 2d */
		private final static float EPSILON = 1.19209289550781250000e-7F;
		/** Minimum length between two corners in a polygon */
		public final static float EDGE_LENGTH_MIN = EPSILON * 1.1f;
		/** Minimum area of a polygon shape */
		public final static float POLYGON_AREA_MIN = EPSILON * 10f;
		/** Default width of the graphics */
		public final static int WIDTH_DEFAULT = 800;
		/** Default height of the graphics */
		public final static int HEIGHT_DEFAULT = 480;
		/** Starting width */
		public final static int WIDTH_START;
		/** Starting height */
		public final static int HEIGHT_START;
		/** World scaling factor */
		public final static float WORLD_SCALE = 0.1f;
		/**
		 * How much bigger of the screen is shown in height from the regular scale. E.g. 3
		 * will show the same amount of free space above and below the level
		 */
		public final static float LEVEL_EDITOR_HEIGHT_SCALE = 2;
		/** Amount of extra space that has been added to the level */
		public final static float LEVEL_EDITOR_HEIGHT_SCALE_INVERT = (LEVEL_EDITOR_HEIGHT_SCALE - 1) / LEVEL_EDITOR_HEIGHT_SCALE;
		/** The actual level scaling */
		/** Level editor scale, this allows the player to see above and below the level */
		public final static float LEVEL_EDITOR_SCALE = WORLD_SCALE * LEVEL_EDITOR_HEIGHT_SCALE;
		/** Maximum frame time length */
		public final static float FRAME_LENGTH_MAX = 0.1f;
		/** Depth level step size */
		public final static float DEPTH_STEP_SIZE = 0.001f;

		static {
			if (Debug.BUILD == Builds.NIGHTLY) {
				WIDTH_START = WIDTH_DEFAULT;
				HEIGHT_START = HEIGHT_DEFAULT;
			} else {
				WIDTH_START = 1280;
				HEIGHT_START = 800;
			}
		}

		/**
		 * Z-value for rendering objects. The further up the enumeration is located the
		 * more in front the object will be rendered.
		 */
		public enum RenderOrders {
			/** Moving objects */
			MOVING_OBJECTS,
			/** Level upper lower borders */
			LEVEL_UPPER_LOWER_BORDERS,
			/** Grid when above */
			GRID_ABOVE,
			/** Brushes */
			BRUSH,
			/** The player's ship */
			PLAYER,
			/** All enemies */
			ENEMY,
			/** Activate actor trigger */
			TRIGGER_ACTOR_ACTIVATE,
			/** A bullet that is shot from an enemy */
			BULLET,
			/** Enemy path, only seen in editor */
			ENEMY_PATH,
			/** Pickup actors */
			PICKUP,
			/** Trigger (only screen at), only seen in editor */
			TRIGGER_SCREEN_AT,
			/** Terrain actor */
			TERRAIN,
			/** Grid below */
			GRID_BELOW,
			/** Background */
			BACKGROUND,

			;

			/**
			 * @return the render order determining in what order to render the objects
			 */
			public int getOrder() {
				return ordinal();
			}

			/**
			 * Default constructor that sets the z-value
			 */
			private RenderOrders() {
				mZValue = -ordinal();
			}

			/**
			 * @return z-value of the object to render
			 */
			public float getZValue() {
				return mZValue;
			}

			/**
			 * Reset z-value offset
			 * @param shapeRenderer the shape renderer to reset the z-value translation
			 * @param object information about z-value translation
			 */
			public static void resetZValueOffsetEditor(ShapeRendererEx shapeRenderer, IResourceEditorRender object) {
				float zValue = object.getRenderOrder().getZValue();
				if (com.spiddekauga.voider.game.actors.Actor.isEditorActive()) {
					if (object instanceof IResourcePosition) {
						if (((IResourcePosition) object).isBeingMoved()) {
							zValue = MOVING_OBJECTS.getZValue();
						}
					}
				}

				shapeRenderer.translate(0, 0, -zValue);
			}

			/**
			 * Offset/Translate the z-value
			 * @param shapeRenderer the shape renderer to translate
			 * @param object information about z-value translation
			 */
			public static void offsetZValueEditor(ShapeRendererEx shapeRenderer, IResourceEditorRender object) {
				float zValue = object.getRenderOrder().getZValue();
				if (com.spiddekauga.voider.game.actors.Actor.isEditorActive()) {
					if (object instanceof IResourcePosition) {
						if (((IResourcePosition) object).isBeingMoved()) {
							zValue = MOVING_OBJECTS.getZValue();
						}
					}
				}

				shapeRenderer.translate(0, 0, zValue);
			}

			/**
			 * Reset z-value offset
			 * @param shapeRenderer the shape renderer to reset the z-value translation
			 * @param renderOrder the render order to offset with
			 */
			public static void resetZValueOffset(ShapeRendererEx shapeRenderer, RenderOrders renderOrder) {
				float zValue = renderOrder.getZValue();
				shapeRenderer.translate(0, 0, -(zValue - 0.5f));
			}

			/**
			 * Offset/Translate the z-value
			 * @param shapeRenderer the shape renderer to translate
			 * @param renderOrder the render order to reset with
			 */
			public static void offsetZValue(ShapeRendererEx shapeRenderer, RenderOrders renderOrder) {
				float zValue = renderOrder.getZValue();
				shapeRenderer.translate(0, 0, zValue - 0.5f);
			}

			/**
			 * Reset z-value offset
			 * @param shapeRenderer the shape renderer to reset the z-value translation
			 * @param object information about z-value translation
			 */
			public static void resetZValueOffset(ShapeRendererEx shapeRenderer, IResourceRender object) {
				float zValue = object.getRenderOrder().getZValue();
				if (com.spiddekauga.voider.game.actors.Actor.isEditorActive()) {
					if (object instanceof IResourcePosition) {
						if (((IResourcePosition) object).isBeingMoved()) {
							zValue = MOVING_OBJECTS.getZValue();
						}
					}
				}

				shapeRenderer.translate(0, 0, -(zValue - 0.5f));
			}

			/**
			 * Offset/Translate the z-value
			 * @param shapeRenderer the shape renderer to translate
			 * @param object information about z-value translation
			 */
			public static void offsetZValue(ShapeRendererEx shapeRenderer, IResourceRender object) {
				float zValue = object.getRenderOrder().getZValue();
				if (com.spiddekauga.voider.game.actors.Actor.isEditorActive()) {
					if (object instanceof IResourcePosition) {
						if (((IResourcePosition) object).isBeingMoved()) {
							zValue = MOVING_OBJECTS.getZValue();
						}
					}
				}

				shapeRenderer.translate(0, 0, zValue - 0.5f);
			}

			/**
			 * Offset the z-value a small amount. Useful when for example a resource will
			 * render many multiple layers and rendering these so all will be shown
			 * accordingly.
			 * @param shapeRenderer the shape renderer to translate
			 * @see #resetZValueOffset(ShapeRendererEx) to reset all calls to this.
			 */
			public static void offsetZValue(ShapeRendererEx shapeRenderer) {
				shapeRenderer.translate(0, 0, MINOR_STEP_SIZE);
				mGlobalZValueOffset += MINOR_STEP_SIZE;
			}

			/**
			 * Resets the offset value. This will reset all calls to
			 * {@link #offsetZValue(ShapeRendererEx)} but not from other.
			 * @param shapeRenderer the shape renderer to reset
			 */
			public static void resetZValueOffset(ShapeRendererEx shapeRenderer) {
				shapeRenderer.translate(0, 0, -mGlobalZValueOffset);
				mGlobalZValueOffset = 0;
			}

			/** The z-value */
			private float mZValue = 0;
			/** Current global offset */
			private static float mGlobalZValueOffset = 0;
			/** Minor step size */
			private final static float MINOR_STEP_SIZE = 0.001f;
		}
	}

	/**
	 * Some GUI options
	 */
	public static class Gui {
		/** Temporary GUI name when using an invoker */
		public final static String GUI_INVOKER_TEMP_NAME = "invoker";
		/**
		 * Temporary GUI name when disabling text fields (so GuiHider doesn't reenable
		 * them)
		 */
		public final static String TEXT_FIELD_DISABLED_NAME = "DISABLED";
		/** Seconds before text field commands aren't combinable */
		public final static float TEXT_FIELD_COMBINABLE_WITHIN = 1;
		/** Seconds before tooltip is shown when hovering over */
		public final static float TOOLTIP_HOVER_SHOW = 0.35f;
		/** Seconds before tooltip is shown when pressing */
		public final static float TOOLTIP_PRESS_SHOW = 1;
		/** Fade duration of the tooltip when hovering */
		public final static float TOOLTIP_HOVER_FADE_DURATION = 0.3f;
		/** Tooltip minimum width before wrap */
		public final static int TOOLTIP_WIDTH_MIN = 200;
		/** Fade in duration of the label and window for error messages */
		public final static float MESSAGE_FADE_IN_DURATION = 0.3f;
		/** Fade out duration of the label and window for error messages */
		public final static float MESSAGE_FADE_OUT_DURATION = 1.0f;
		/** Minimum time shown */
		public final static float MESSAGE_TIME_SHOWN_MIN = 2.0f;
		/** Time to display error message per character */
		public final static float MESSAGE_TIME_PER_CHARACTER = 0.08f;
		/** Width of the score table's first cells */
		public final static int SCORE_TABLE_FIRST_CELL_WIDTH = 100;
	}

	/**
	 * Input options
	 */
	public static class Input {
		/** How many seconds for a double click? */
		public final static float DOUBLE_CLICK_TIME = 0.2f;
	}

	/**
	 * Level
	 */
	public static class Level {
		/**
		 * How much offset from the first resource inside the level the beginning of the
		 * level should be placed
		 */
		public final static float START_COORD_OFFSET = Graphics.WIDTH_DEFAULT * Graphics.WORLD_SCALE * 0.1f;
		/**
		 * How much offset from the last resource inside the level the x-coordinate should
		 * appear
		 */
		public final static float END_COORD_OFFSET = Graphics.WIDTH_DEFAULT * Graphics.WORLD_SCALE * 1.1f;
		/** Speed of the front/top background layer, relative to the level speed */
		public final static float BACKGROUND_TOP_SPEED = 0.75f;
		/** Speed of the back/bottom background layer, relative to the level speed */
		public final static float BACKGROUND_BOTTOM_SPEED = 0.5f;
		/** Screenshot texture width */
		public final static int SAVE_TEXTURE_WIDTH = 256;
		/** Screenshot texture ratio */
		public final static float SAVE_TEXTURE_RATIO = ((float) Graphics.WIDTH_DEFAULT) / Graphics.HEIGHT_DEFAULT;
		/** Screenshot texture height */
		public final static int SAVE_TEXTURE_HEIGHT = (int) (SAVE_TEXTURE_WIDTH / SAVE_TEXTURE_RATIO);
	}

	/**
	 * Menus
	 */
	public static class Menu {
		// Splash screen
		/** Splash screen time, including entering and fade in, excluding fade out exiting */
		public final static float SPLASH_SCREEN_TIME = 3.5f;
		/** Splash screen fade in time */
		public final static float SPLASH_SCREEN_FADE_IN = 1.0f;
		/** Splash screen fade out time */
		public final static float SPLASH_SCREEN_FADE_OUT = 1.0f;
		/** Splash screen enter time */
		public final static float SPLASH_SCREEN_ENTER_TIME = 0.4f;
		/** Splash screen exiting time */
		public final static float SPLASH_SCREEN_EXIT_TIME = 0.1f;

		// Loading text screen
		/** Loading text scene fade in time */
		public final static float LOADING_TEXT_SCENE_FADE_IN = 1f;
		/** Loading text scene fade out time */
		public final static float LOADING_TEXT_SCENE_FADE_OUT = 1f;
		/** Loading text scene enter time */
		public final static float LOADING_TEXT_SCENE_ENTER_TIME = 0.4f;
		/** Loading text scene exiting time */
		public final static float LOADING_TEXT_SCENE_EXIT_TIME = 0.1f;
	}

	/**
	 * Network
	 */
	public static class Network {
		/** Server host */
		public static final String SERVER_HOST;
		/**
		 * Set this variable to a specific build to override the current server host to
		 * point to this build instead
		 */
		public static final Builds OVERRIDE_HOST = null;

		static {
			Builds build = Debug.BUILD;
			if (OVERRIDE_HOST != null) {
				build = OVERRIDE_HOST;
			}

			// END URL WITH A SLASH /
			switch (build) {
			case RELEASE:
				SERVER_HOST = "http://voider-game.com/";
				break;

			case BETA:
				SERVER_HOST = "http://voider-beta.appspot.com/";
				break;

			case NIGHTLY:
				SERVER_HOST = "http://voider-nightly.appspot.com/";
				break;

			case DEV_SERVER:
				SERVER_HOST = "http://voider-dev.appspot.com/";
				break;

			case DEV_LOCAL:
				SERVER_HOST = "http://localhost:8888/";
				break;

			default:
				SERVER_HOST = "invalid";
			}
		}
	}

	/**
	 * User
	 */
	public static class User {
		/** Minimum password length */
		public static final int PASSWORD_LENGTH_MIN = 5;


	}

	/**
	 * Revision of the game, as in code. This allows files of older revisions to be loaded
	 * into new revisions
	 */
	public final static int REVISION = 3;

	/**
	 * Private constructor so that no instance can be created
	 */
	private Config() {
		// Does nothing
	}
}

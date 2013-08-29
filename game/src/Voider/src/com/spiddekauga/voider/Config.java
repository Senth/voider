package com.spiddekauga.voider;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.spec.SecretKeySpec;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonWriter.OutputType;
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
	}

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

		/** Border width of all actors */
		public final static float BORDER_WIDTH = 0.5f;
		/** Outline color */
		public final static Color OUTLINE_COLOR = Color.ORANGE;
		/** Closing outline color (color from corner.end -> corner.begin) */
		public final static Color OUTLINE_CLOSE_COLOR = Color.PINK;
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
				hashedFileKey = Arrays.copyOf(hashedFileKey, 16);

				mFileKey = new SecretKeySpec(hashedFileKey, "AES");
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		/**
		 * Returns the file key
		 * 
		 * @return key for encrypting/decrypting files
		 */
		public static SecretKeySpec getFileKey() {
			return mFileKey;
		}

		/** Salt for file key */
		private static final byte[] FILE_KEY_BYTES = { 15, 35, 68, 86, 57, 2, 99, 105, 127, -38, -100, -35, 35, 48, 68, -79, 95, -22, 0, 15, 0, 0,
			98, 15, 27, 35 };
		/** The actual file key */
		private static SecretKeySpec mFileKey = null;
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
				public final static float DRAW_NEW_CORNER_MIN_DIST_SQ = 1.0f * 1.0f;
				/** Minimum angle between corners, if less than this, the corner will be removed */
				public final static float DRAW_CORNER_ANGLE_MIN = 10;
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
				 * How long time ONCE enemy should have reached the goal before it is reset. In EnemyEditor
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
			public final static float LEVEL_SPEED_DEFAULT = 5;
			/** Step size of level speeed */
			public final static float LEVEL_SPEED_STEP_SIZE = 1;
			/**
			 * Color of above and below the actual level, so the player can see that this doesn't below to the level.
			 */
			public final static Color ABOVE_BELOW_COLOR = new Color(1, 1, 1, 0.1f);
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

		/** Select definition scene, maximum width of definition */
		public static float SELECT_DEF_WIDTH_MAX = 200;
		/** Text field size for numbers */
		public final static float TEXT_FIELD_NUMBER_WIDTH = 70;
		/** Label padding in front of a slider */
		public final static float LABEL_PADDING_BEFORE_SLIDER = 8;
		/** Maximum name length */
		public final static int NAME_LENGTH_MAX = 16;
		/** Maximum length of description */
		public final static int DESCRIPTION_LENGTH_MAX = 256;
		/** Maximum length of story */
		public final static int STORY_LENGTH_MAX = 512;
		/** Auto save time, in seconds */
		public final static float AUTO_SAVE_TIME = 60;

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

		/** How many characters the score should contain, i.e. number
		 * of leading zeros */
		public final static int SCORE_CHARACTERS = 10;
		/** Score multiplier, used to create a greater score */
		public final static float SCORE_MULTIPLIER = 10;
		/** Health color */
		public final static Color HEALTH_COLOR = new Color(1, 0, 0, 0.3f);
		/** Lives offset */
		public final static float LIVES_OFFSET_POSITION = 1;
		/** Border threshold, when border is too out of sync, it will get synced */
		public final static float BORDER_SYNC_THRESHOLD = 0.1f;
	}

	/**
	 * All graphical options
	 */
	public static class Graphics {
		/** If we shall use debug_renderer to display graphics instead of sprites (where applicable) */
		public final static boolean USE_DEBUG_RENDERER = true;
		/** Renders regular graphics */
		public final static boolean USE_RELEASE_RENDERER = true;
		/** Epsilon for box 2d */
		private final static float EPSILON = 1.19209289550781250000e-7F;
		/** Minimum length between two corners in a polygon */
		public final static float EDGE_LENGTH_MIN = EPSILON * 1.1f;
		/** Minimum area of a polygon shape */
		public final static float POLYGON_AREA_MIN = EPSILON * 1.1f;
		/** Default width of the graphics */
		public final static float WIDTH = 800;
		/** Default height of the graphics */
		public final static float HEIGHT = 480;
		/** World scaling factor */
		public final static float WORLD_SCALE = 0.1f;
		/** How much bigger of the screen is shown in height from the regular scale. E.g. 3 will show the same amount of
		 * free space above and below the level */
		public final static float LEVEL_EDITOR_HEIGHT_SCALE = 2;
		/** Level editor scale, this allows the player to see above and below the level */
		public final static float LEVEL_EDITOR_SCALE = WORLD_SCALE * LEVEL_EDITOR_HEIGHT_SCALE;
		/** Maximum frame time length */
		public final static float FRAME_LENGTH_MAX = 0.1f;
	}

	/**
	 * Some GUI options
	 */
	public static class Gui {
		/** Temporary GUI name when using an invoker */
		public final static String GUI_INVOKER_TEMP_NAME = "invoker";
		/** Seconds before text field commands aren't combinable */
		public final static float TEXT_FIELD_COMBINABLE_WITHIN = 1;
		/** Separator padding */
		public final static float SEPARATE_PADDING = 5;
		/** Seconds before tooltip is shown when hovering over */
		public final static float TOOLTIP_HOVER_SHOW = 0.5f;
		/** Seconds before tooltip is shown when pressing */
		public final static float TOOLTIP_PRESS_SHOW = 1;
		/** Fade duration of the tooltip when hovering */
		public final static float TOOLTIP_HOVER_FADE_DURATION = 0.3f;
		/** Tooltip margin for the screen */
		public final static int TOOLTIP_MARGIN_WIDTH = 10;
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

		/**
		 * @return true if we want to display text buttons instead of image buttons
		 */
		public static boolean usesTextButtons() {
			return mUseTextButtons;
		}

		/**
		 * Sets if we want to display text or image buttons
		 * 
		 * @param usesText
		 *            set to true to use text buttons, false for image buttons
		 */
		public static void setUseTextButtons(boolean usesText) {
			mUseTextButtons = usesText;
		}

		/** If we're using text buttons instead of images */
		private static boolean mUseTextButtons = true;
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
		/** How much offset from the first resource inside the level
		 * the beginning of the level should be placed */
		public final static float START_COORD_OFFSET = Graphics.WIDTH * Graphics.WORLD_SCALE * 0.25f;
		/** How much offset from the last resource inside the level the
		 * x-coordinate should appear */
		public final static float END_COORD_OFFSET = Graphics.WIDTH * Graphics.WORLD_SCALE * 1.25f;
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

	/** Revision of the game, as in code.
	 * This allows files of older revisions to be loaded into new revisions */
	public final static int REVISION = 3;
	/** If debugging tests shall be activate */
	public final static boolean DEBUG_TESTS = true;
	/** Output type for JsonWrapper */
	public final static OutputType JSON_OUTPUT_TYPE = OutputType.json;

	/**
	 * Private constructor so that no instance can be created
	 */
	private Config() {
		// Does nothing
	}
}

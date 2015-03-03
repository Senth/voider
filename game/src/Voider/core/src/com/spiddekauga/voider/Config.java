package com.spiddekauga.voider;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.spec.SecretKeySpec;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Vector2;
import com.spiddekauga.utils.ShapeRendererEx;
import com.spiddekauga.voider.Config.Debug.Builds;
import com.spiddekauga.voider.resources.IResourceEditorRender;
import com.spiddekauga.voider.resources.IResourcePosition;
import com.spiddekauga.voider.resources.IResourceRenderShape;

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
			/** Linear dampening when enemy is deactivated */
			public final static float LINEAR_DAMPENING = 0.2f;
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
		/** Texture size of layered images on top of the actor */
		public final static int SAVE_IMAGE_ON_ACTOR_SIZE = SAVE_TEXTURE_SIZE / 4;
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
		/** Highscore cache, in seconds */
		public static final int HIGHSCORE_TIME = 45;
		/** Comment cache, in seconds */
		public static final int COMMENT_TIME = 180;
	}

	/**
	 * Community things
	 */
	public static class Community {
		/** How many hours until a level/campaign is taggable again */
		public static final int TAGGABLE_DELAY = 12;
		/** How many tags to display when tagging */
		public static final int TAGS_TO_DISPLAY = 7;
		/** Tags per row */
		public static final int TAGS_PER_ROW = 2;
		/** Maximum number of tags per user per resource */
		public static final int TAGS_MAX_PER_RESOURCE = 5;
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

		// @formatter:off
		/** Salt for file key */
		private static final byte[] FILE_KEY_BYTES = { 15, 35, 68, 86, 57, 2, 99, 105, 127, -38, -100, -35, 35, 48, 68, -79, 95, -22, 0, 15, 0, 0, 98, 15, 27, 35 };
		/** Salt for file key */
		private static final byte[] PASSWORD_KEY_BYTES = { 11, 120, 8, 86, 5, 22, 9, 15, -88, 38, 100, -35, 35, 35, -6, 79, 95, 22, 22, 2, 15, 65, 8, -15, -27, -35 };
		// @formatter:on
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

		/** Build level */
		public static final Builds BUILD = Builds.DEV_LOCAL;
		/** Set to true to turn on the exception handler */
		public static boolean EXCEPTION_HANDLER = isBuildOrAbove(Builds.NIGHTLY_DEV);
		/** Set to true in JUNIT tests */
		public static boolean JUNIT_TEST = false;
		/** Logging verbosity */
		public static final int LOG_VERBOSITY = isBuildOrAbove(Builds.BETA) ? Application.LOG_ERROR : Application.LOG_DEBUG;
		/**
		 * If debugging tests shall be activate. This causes extra runtime, but checks so
		 * that none of the checks are broken.
		 */
		public static boolean DEBUG_TESTS = isBuildOrBelow(Builds.NIGHTLY_DEV);
		/** Skip loading text */
		public static final boolean SKIP_LOADING_TIME = isBuildOrBelow(Builds.NIGHTLY_RELEASE) ? false : false;


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
			NIGHTLY_DEV,
			/** Nightly released */
			NIGHTLY_RELEASE,
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
				/** Step size of trigger delay */
				public final static float TRIGGER_ACTIVATE_DELAY_STEP_SIZE = 0.1f;
				/** Minimum deactivation trigger delay */
				public final static float TRIGGER_DEACTIVATE_DELAY_MIN = 0;
				/** Maximum deactivation trigger delay */
				public final static float TRIGGER_DEACTIVATE_DELAY_MAX = 300;
				/** Default deactivation trigger delay */
				public final static float TRIGGER_DEACTIVATE_DELAY_DEFAULT = 0;
				/** Step size of deactivation trigger delay */
				public final static float TRIGGER_DEACTIVATE_DELAY_STEP_SIZE = 1;
				/** Number of enemies per row when adding enemies */
				public final static int LIST_COLUMNS = 3;
			}

			/**
			 * Path options
			 */
			public static class Path {
				/** Width of displayed path for enemies */
				public final static float WIDTH = 0.5f;
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

			/** Minimum level length for publishing a level, in seconds */
			public final static float LEVEL_LENGTH_PUBLISH_MIN = 30;
			/** Enemy snap distance to a path */
			public final static float ENEMY_SNAP_PATH_DISTANCE = 2;
			/** Enemy snap distance squared */
			public final static float ENEMY_SNAP_PATH_DISTANCE_SQ = ENEMY_SNAP_PATH_DISTANCE * ENEMY_SNAP_PATH_DISTANCE;
			/** Minimum level speed */
			public final static float LEVEL_SPEED_MIN = 1;
			/** Maximum level speed */
			public final static float LEVEL_SPEED_MAX = 50;
			/** Default level speed */
			public final static float LEVEL_SPEED_DEFAULT = 15;
			/** Step size of level speed */
			public final static float LEVEL_SPEED_STEP_SIZE = 1;
			/** Minimum zoom */
			public final static float ZOOM_MIN = 0.2f;
			/** Maximum zoom */
			public final static float ZOOM_MAX = 10;
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


		/**
		 * When to only show 10ths of coordinates. True when pixels per world coordinate
		 * is below this value
		 */
		public final static int GRID_SHOW_ONLY_MILESTONE_PIXELS_PER_WORLD = 4;
		/** Regular step size of grid */
		public final static int GRID_STEP_SIZE = 2;
		/** Milestone step size. Should be a multiplier of GRID_STEP_SIZE! */
		public final static int GRID_MILESTONE_STEP = GRID_STEP_SIZE * 5;
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
		/** Zoom in amount when clicking on the icon, this value is multiplied */
		public final static float ZOOM_AMOUNT = 0.8f;
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
		public final static boolean USE_EXTERNAL_RESOURCES = Debug.BUILD == Builds.NIGHTLY_DEV;
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

				case NIGHTLY_DEV:
				case NIGHTLY_RELEASE:
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
			// setUserPaths("(None)");
		}
	}

	/**
	 * Some general game settings
	 */
	public static class Game {
		/** Score multiplier, used to create a greater score */
		@Deprecated public final static float SCORE_MULTIPLIER = 10;
		/** Border threshold, when border is too out of sync, it will get synced */
		public final static float BORDER_SYNC_THRESHOLD = 0.1f;
	}

	/**
	 * All graphical options
	 */
	public static class Graphics {
		/** Default blend source factor */
		public final static int BLEND_SRC_FACTOR = GL20.GL_SRC_ALPHA;
		/** Default blend destination factor */
		public final static int BLEND_DST_FACTOR = GL20.GL_ONE_MINUS_SRC_ALPHA;
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
		/** Minimum length between two corners in a polygon, squared */
		public final static float EDGE_LENGTH_MIN_SQUARED = EDGE_LENGTH_MIN * EDGE_LENGTH_MIN;
		/** Minimum area of a polygon shape */
		public final static float POLYGON_AREA_MIN = EPSILON * 100f;
		/** Default width of the graphics, also minimum */
		public final static int WIDTH_DEFAULT = 800;
		/** Default height of the graphics, also minimum */
		public final static int HEIGHT_DEFAULT = 600;
		/** Starting width */
		public final static int WIDTH_START = 1280;
		/** Starting height */
		public final static int HEIGHT_START = 720;
		/** World scaling factor */
		public final static float WORLD_SCALE = 0.1f;
		/**
		 * How much bigger of the screen is shown in height from the regular scale. E.g. 3
		 * will show the same amount of free space above and below the level
		 */
		public final static float LEVEL_EDITOR_HEIGHT_SCALE = 2;
		/** Amount of extra space that has been added to the level */
		public final static float LEVEL_EDITOR_HEIGHT_SCALE_INVERT = 1 / LEVEL_EDITOR_HEIGHT_SCALE;
		/** Level editor scale, this allows the player to see above and below the level */
		public final static float LEVEL_EDITOR_SCALE = WORLD_SCALE * LEVEL_EDITOR_HEIGHT_SCALE;
		/** Maximum frame time length */
		public final static float FRAME_LENGTH_MAX = 0.1f;
		/** Depth level step size */
		public final static float DEPTH_STEP_SIZE = 0.001f;
		/** Widescreen ratio */
		public final static float WIDESCREEN_RATIO = 1.6f / 0.9f;

		/**
		 * Z-value for rendering objects. The further up the enumeration is located the
		 * more in front the object will be rendered.
		 */
		public enum RenderOrders {
			/** Stub, just sets some offset */
			STUB,
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
			public static void resetZValueOffset(ShapeRendererEx shapeRenderer, IResourceRenderShape object) {
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
			public static void offsetZValue(ShapeRendererEx shapeRenderer, IResourceRenderShape object) {
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
		@Deprecated public final static float TOOLTIP_HOVER_SHOW = 0.35f;
		/** Seconds before tooltip is shown when pressing */
		@Deprecated public final static float TOOLTIP_PRESS_SHOW = 1;
		/** Fade duration of the tooltip when hovering */
		@Deprecated public final static float TOOLTIP_HOVER_FADE_DURATION = 0.3f;
		/** Tooltip minimum width before wrap */
		@Deprecated public final static int TOOLTIP_WIDTH_MIN = 200;
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
		/** Screenshot ratio */
		public final static float SAVE_TEXTURE_RATIO = 1.6f;
		/** Screenshot texture height */
		public final static int SAVE_TEXTURE_HEIGHT = (int) (SAVE_TEXTURE_WIDTH / SAVE_TEXTURE_RATIO);
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
		/** Maximum number of connections */
		public static final int CONNECTIONS_MAX = Debug.isBuildOrBelow(Builds.DEV_LOCAL) ? 1 : 5;

		static {
			Builds build = Debug.BUILD;
			if (OVERRIDE_HOST != null) {
				build = OVERRIDE_HOST;
			}

			// END URL WITH A SLASH /
			switch (build) {
			case RELEASE:
				SERVER_HOST = "https://voider-thegame.appspot.com/";
				break;

			case BETA:
				SERVER_HOST = "https://voider-beta.appspot.com/";
				break;

			case NIGHTLY_DEV:
			case NIGHTLY_RELEASE:
				SERVER_HOST = "https://voider-nightly.appspot.com/";
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
		/** Minimum username lengeth */
		public static final int NAME_LENGTH_MIN = 3;
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

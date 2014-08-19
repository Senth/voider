package com.spiddekauga.voider.utils;

import com.spiddekauga.utils.scene.ui.TooltipWidget.ITooltip;
import com.spiddekauga.voider.Config;

/**
 * Class containing all messages for voider, including help function for retrieving
 * messages
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class Messages {

	/**
	 * Print this string when no definition is selected
	 * @param defTypeName name of the definition, this will be inserted into the message
	 * @return text when no definition has been selected.
	 */
	public static String getNoDefSelected(String defTypeName) {
		return "(no " + defTypeName + " selected)";
	}

	/**
	 * Unsaved message, do you want to continue?
	 * @param unsavedType what is it that is unsaved
	 * @param action the action that will be taken.
	 * @return message for the unsaved type with the specified action
	 */
	public static String getUnsavedMessage(String unsavedType, UnsavedActions action) {
		String message = "Your current " + unsavedType + " is unsaved.\n" + "Do you want to save it before " + action.getMessage(unsavedType) + "?";
		return message;
	}

	/**
	 * Calculates the amount of time to show a specific message depending on its length
	 * @param message the message to calculate how long time to display
	 * @return seconds to display the message
	 */
	public static float calculateTimeToShowMessage(String message) {
		return Config.Gui.MESSAGE_TIME_SHOWN_MIN + Config.Gui.MESSAGE_TIME_PER_CHARACTER * message.length();

	}

	/**
	 * Enumeration of unsaved actions
	 */
	public enum UnsavedActions {
		/** When creating a new definition */
		NEW("creating a new UNSAVED_TYPE"),
		/** When loading another definition */
		LOAD("loading another UNSAVED_TYPE"),
		/** When duplicating the existing definition */
		DUPLICATE("duplicating this UNSAVED_TYPE"),
		/** Switching to the level editor */
		LEVEL_EDITOR("switching to Level Editor"),
		/** Switching to the enemy editor */
		ENEMY_EDITOR("switching to Enemy Editor"),
		/** Switching to bullet editor */
		BULLET_EDITOR("switching to Bullet Editor"),
		/** Switching to campaign editor */
		CAMPAIGN_EDITOR("switching to Campaign Editor"),
		/** Returning to main menu */
		MAIN_MENU("exiting to Main Menu"),

		;
		/**
		 * @param unsavedType the unsaved type, which will replace any instance of
		 *        UNSAVED_TYPE within the message.
		 * @return the message of the action
		 */
		public String getMessage(String unsavedType) {
			return mMessage.replace(UNSAVED_TYPE, unsavedType);
		}

		/**
		 * Creates an unsaved action with the message
		 * @param message the message to use for the action. Any instance of UNSAVED_TYPE
		 *        in the message will later be replaced with the actual unsaved type.
		 */
		private UnsavedActions(String message) {
			mMessage = message;
		}

		/** The message that will be used for the action */
		private String mMessage;
		/** replacement variable */
		public final static String UNSAVED_TYPE = "UNSAVED_TYPE";
	}

	/**
	 * Info messages
	 */
	public static class Info {
		/** Text displayed when saving */
		public static final String SAVED = "Saved...";
		/** Bug report was successfully sent */
		public static final String BUG_REPORT_SENT = "Thank you for sending a bug report! :D";
		/** Bug report failed, saved locally instead */
		public static final String BUG_REPORT_SAVED_LOCALLY = "Could not connect to the server, " + "temporarily saved the bug report locally.";
	}

	/**
	 * Messages for level editor
	 */
	public static class Level {
		/**
		 * Title of message box when player is asked to select if s/he shall be
		 * invulnerable
		 */
		public final static String RUN_INVULNERABLE_TITLE = "Test run the level";
		/**
		 * Message when the player is asked to select if s/he shall be invulnerable when
		 * testing the level.
		 */
		public final static String RUN_INVULNERABLE_CONTENT = "Do you want to be invulnerable when " + "you're testing the level?\n\n"
				+ "Hit Escape or Back when you want to stop the test.";
		/** Header for successfully completing the level */
		public final static String COMPLETED_HEADER = "Congratulations!";
		/** Header for game over */
		public final static String GAME_OVER_HEADER = "Game Over!";
		/** Prologue default field */
		public final static String PROLOGUE_DEFAULT = "Write a short prologue to be displayed at the start of the level...";
		/** Epilogue default field */
		public final static String EPILOGUE_DEFAULT = "Write a short epilogue to be displayed at the end of the level...";
	}

	/**
	 * Messages for editors
	 */
	public static class Editor {
		/** Text displayed when duplicating the current resource */
		public final static String DUPLICATE_BOX = "Do you want to duplicate this " + ACTOR_TYPE + "?";
		/** Text to be displayed when exiting the editor */
		public final static String EXIT_TO_MAIN_MENU = "Do you want to exit to main menu?";
		/** Default text for description field */
		public final static String DESCRIPTION_FIELD_DEFAULT = "Write a short description about the " + ACTOR_TYPE + "...";
		/** Default text for name field */
		public final static String NAME_FIELD_DEFAULT = "Give your " + ACTOR_TYPE + " a name...";
	}

	/**
	 * Messages for enemy editor
	 */
	public static class Enemy {
		/** Text displayed when selecting bullet types */
		public final static String SELECT_BULLET = "Select bullet type:";
	}

	/**
	 * Error messages
	 */
	public static class Error {
		/** Polygon complex append */
		public final static String POLYGON_COMPLEX_DRAW_APPEND = "Lines are not allowed to intersect";
		/** Polygon area too small */
		public final static String POLYGON_AREA_TOO_SMALL = "Sorry, something went wrong :'( Try again :D";
		/** Polygon complex add corner */
		public final static String POLYGON_COMPLEX_ADD = "Can't add a corner here, would create an intersection";
		/** Polygon complex remove corner */
		public final static String POLYGON_COMPLEX_REMOVE = "Can't remove this corner, would create an intersection";
		/** Polygon complex move corner */
		public final static String POLYGON_COMPLEX_MOVE = "Can't move here, would create an intersection";
		/** Polygon complex draw/erase */
		public final static String POLYGON_COMPLEX_DRAW_ERASE = POLYGON_COMPLEX_DRAW_APPEND;
		/** Polygon draw/erase line is complex */
		public final static String POLYGON_DRAW_ERASE_LINE_COMPLEX = POLYGON_COMPLEX_DRAW_APPEND;
		/** Bug report */
		public final static String BUG_REPORT_INFO = "The game has crashed due to some unknown bug. "
				+ "Please describe the last 2 steps you did; this helps enormously when debugging :)";
		/** Name must contain X characters */
		public final static String NAME_CHARACTERS_MIN = "must contain at least " + Config.Actor.NAME_LENGTH_MIN + " characters";
	}

	/**
	 * Tooltip messages
	 */
	@SuppressWarnings("javadoc")
	public static class Tooltip {
		public static class Actor {
			public static class Visuals {
				public final static String STARTING_ANGLE = "Initial facing direction";
				public final static String ROTATION_SPEED = "How fast the " + ACTOR_TYPE + " rotates";
				public final static String CIRCLE = "Circle shape";
				public final static String RECTANGLE = "Rectangle shape";
				public final static String TRIANGLE = "Triangle shape";
				public final static String DRAW = "Draw your own shape";

			}

			public static class Option {
				public final static String NAME = "Name of the " + ACTOR_TYPE;
				public final static String DESCRIPTION = "Short description of your " + ACTOR_TYPE;
			}

			public static class Collision {
				public final static String DAMAGE = "Damage caused on player when colliding";
				public final static String DESTROY_ON_COLLIDE = "Destroys the " + ACTOR_TYPE + " when it collides";
			}

			public static class Menu {
				public final static String VISUALS = "Change visuals; shape and rotation";
				public final static String OPTIONS = ACTOR_TYPE + "'s name and description";
				public final static String COLLISION = "Collision options";
			}
		}

		public static class Enemy {
			public static class Menu {
				public final static String MOVEMENT = "Enemy movement";
				public final static String WEAPON = "Enemy weapon";
			}

			public static class Movement {
				public static class Menu {
					public final static String PATH = "Enemy follows a path";
					public final static String STATIONARY = "Stationary";
					public final static String AI = "AI. Setup your own movement rules";
				}

				public static class Path {
					public final static String BACK_AND_FORTH = "Back and forth";
					public final static String LOOP = "Loop";
					public final static String ONCE = "Follow once only";
				}

				public static class Common {
					public final static String MOVEMENT_SPEED = "Movement speed";
					public final static String TURNING_SPEED_BUTTON = "Turns when changing direction";
					public final static String TURNING_SPEED = "How fast the enemy turns";
				}

				public static class Ai {
					public final static String DISTANCE = "Distance to keep to player";
					public final static String RANDOM_MOVEMENT_BUTTON = "Moves randomly when between min & max distance";
					public final static String RANDOM_MOVEMENT = "Delay until next random direction";
				}
			}

			public static class Weapon {
				public final static String WEAPON_BUTTON = "Turn on/off enemy weapon";

				public static class Bullet {
					public final static String SELECT_BULLET = "Selects a bullet type";
					public final static String SPEED = "Bullet speed";
					public final static String DAMAGE = "Bullet damage";
					public final static String COOLDOWN = "Weapon cooldown";
				}

				public static class Aim {
					public final static String DIRECTION = "Shoot in one direction";
					public final static String DIRECTION_ANGLE = "Angle to shoot in";
					public final static String ON_PLAYER = "Shoot on player";
					public final static String MOVE_DIR = "Shoot in movement direction";
					public final static String IN_FRONT_OF_PLAYER = "Shoot in front of player";
					public final static String ROTATE = "Shoot in rotating manner";
					public final static String ROTATE_START_ANGLE = "Start shoot in this angle";
					public final static String ROTATE_SPEED = "Rotation speed";
				}
			}

		}

		public static class Level {
			public static class Pickup {
				public final static String SELECT_NAME = "Pickup name";
				public final static String SELECT_TYPE = "Select a pickup type";
			}

			public static class Enemy {
				public final static String ENEMY_COUNT = "Copies of the selected enemy";
				public final static String ENEMY_SPAWN_DELAY = "Enemy spawn delay";
				public final static String ACTIVATE_DELAY = "Activation delay";
				public final static String DEACTIVATE_DELAY = "Deactivation delay";
				public final static String ADD = "Add enemy to the list";
			}

			public static class Path {
				public final static String ONCE = "Follows path once only";
				public final static String LOOP = "Loops in the path";
				public final static String BACK_AND_FORTH = "Back and forth in the path";
			}

			public static class Option {
				public final static String NAME = "Level name";
				public final static String DESCRIPTION = "Short description";
				public final static String LEVEL_SPEED = "Speed level";
				public final static String REVISION = "Current revision (increases on each save)";
				public final static String PROLOGUE = "Prologue, displayed before level";
				public final static String EPILOGUE = "Epilogue, displayed on level clear";
			}
		}

		public static class Menus {
			public static class Main {
				public final static String PLAY = "Play";
				public final static String EXPLORE = "Explore new content";
				public final static String CREATE = "Create your own content";

				public final static String LOGOUT = "Logout";
			}

			public static class Editor {
				public final static String CAMPAIGN = "Campaign editor";
				public final static String LEVEL = "Level editor";
				public final static String ENEMY = "Enemy editor";
				public final static String BULLET = "Bullet editor";
			}

			public static class File {
				public final static String NEW = "Create a new " + ACTOR_TYPE;
				public final static String SAVE = "Save the " + ACTOR_TYPE;
				public final static String LOAD = "Load another " + ACTOR_TYPE;
				public final static String DUPLICATE = "Create a duplicate of this " + ACTOR_TYPE;
				public final static String PUBLISH = "Publish the " + ACTOR_TYPE + " to the Internet";
				public final static String UNDO = "Undo your previous action";
				public final static String REDO = "Redo your action";
				public final static String RUN = "Test run your level";
				public final static String HIGHLIGHT_ENEMY = "Highlight enemies used during test run";
				public final static String GRID = "Turn on/off the grid";
				public final static String GRID_ADOVE = "Display grid above everything else";
				public final static String INFO = "Info and options for this " + ACTOR_TYPE;
			}
		}

		public static class Tools {
			public final static String SET_CENTER = "Set shape center";
			public final static String RESET_CENTER = "Reset shape center";
			public final static String MOVE = "Move stuff";
			public final static String PAN = "Pan the screen";
			public final static String DRAW_ERASE = "Draw or erase parts to your " + ACTOR_TYPE;
			public final static String DRAW_APPEND = "Create a new " + ACTOR_TYPE + ", or append to an existing";
			public final static String ADJUST_ADD_MOVE_CORNER = "Add or move a shape corner";
			public final static String ADJUST_REMOVE_CORNER = "Remove shape corners";
			public final static String DELETE = "Delete";
			public final static String SELECT = "Select";
			public final static String ENEMY_ADD = "Add enemies";
			public final static String PATH_ADD = "Add enemy paths";
			public final static String SET_ACTIVATE_TRIGGER = "Set enemy activation trigger";
			public final static String SET_DEACTIVATE_DELAY = "Set enemy deactivation trigger";
			public final static String TRIGGER_ADD = "Add triggers";
			public final static String CANCEL = "Clear current selection";
		}
	}

	/**
	 * Editor tooltip messages
	 */
	@SuppressWarnings("javadoc")
	public enum EditorTooltips implements ITooltip {
		// Level tabs
		TAB_ENEMY_ADD("Add enemy tab"),
		TAB_ENEMY("Enemy settings"),
		TAB_PATH("Path settings"),

		// Actor tabs
		TAB_VISUAL("Visual settings"),

		// Enemy tabs
		TAB_MOVEMENT("Movement settings", null, 1),
		TAB_WEAPON("Weapon settings", "https://www.youtube.com/watch?v=SxzoVL5YTHc", 1),
		TAB_COLLISION("Collision settings", "https://www.youtube.com/watch?v=ZbKIrmIbrjk", 1),

		// Bullet tabs
		TAB_BULLET_TEST("Test bullet on weapons", null, 1),

		// Tools
		TOOL_SELECTION("Selection tool", "https://www.youtube.com/watch?v=7R1rK2b8jaU", 1),
		TOOL_PAN_LEVEL("Pan tool", "https://www.youtube.com/watch?v=WThGYvHQSHU", 1),
		TOOL_PAN_ACTOR("Pan tool", "https://www.youtube.com/watch?v=WThGYvHQSHU", TAB_VISUAL, 2),
		TOOL_MOVE_LEVEL("Move tool", "https://www.youtube.com/watch?v=UFmqy6YWMRk", 1),
		TOOL_MOVE_ACTOR("Move tool", "https://www.youtube.com/watch?v=UFmqy6YWMRk", TAB_VISUAL, 2),
		TOOL_DELETE_LEVEL("Delete tool", "https://www.youtube.com/watch?v=96S2M17STaI", 1),
		TOOL_DELETE_ACTOR("Delete tool", "https://www.youtube.com/watch?v=96S2M17STaI", TAB_VISUAL, 2),
		TOOL_CLEAR_SELECTION("Clear selection"),
		TOOL_DRAW_APPEND_TERRAIN("Draw terrain", "https://www.youtube.com/watch?v=UbOF_KgpLzI", 1),
		TOOL_DRAW_ERASE_TERRAIN("Add/Remove area to/from terrain", "https://www.youtube.com/watch?v=UbOF_KgpLzI", 1),
		TOOL_DRAW_CORNER_ADD_TERRAIN("Add/Move terrain corners", "https://www.youtube.com/watch?v=UbOF_KgpLzI", 1),
		TOOL_DRAW_CORNER_REMOVE_TERRAIN("Remove terrain corners", "https://www.youtube.com/watch?v=UbOF_KgpLzI", 1),
		TOOL_DRAW_APPEND_ACTOR("Draw shape", "https://www.youtube.com/watch?v=q9BmR6E5JvM", TAB_VISUAL, 2),
		TOOL_DRAW_ERASE_ACTOR("Add/Remove area to/from shape", "https://www.youtube.com/watch?v=q9BmR6E5JvM", TAB_VISUAL, 2),
		TOOL_DRAW_CORNER_ADD_ACTOR("Add/Move shape corners", "https://www.youtube.com/watch?v=q9BmR6E5JvM", TAB_VISUAL, 2),
		TOOL_DRAW_CORNER_REMOVE_ACTOR("Remove shape corners", "https://www.youtube.com/watch?v=q9BmR6E5JvM", TAB_VISUAL, 2),
		TOOL_ENEMY_ADD("Add enemies", "https://www.youtube.com/watch?v=n3d31c4gaf8", 1),
		TOOL_PATH("Add paths for enemies", "https://www.youtube.com/watch?v=DWeUECW8o2w", 1),
		TOOL_TRIGGER_ACTIVATE("Set enemy activate triggers", "https://www.youtube.com/watch?v=eCTuQdG3v98", 1),
		TOOL_TRIGGER_DEACTIVATE("Set enemy deactivate triggers", "https://www.youtube.com/watch?v=eCTuQdG3v98", 1),
		TOOL_CENTER_SET("Set center", "https://www.youtube.com/watch?v=q9BmR6E5JvM", TAB_VISUAL, 2),
		TOOL_CENTER_RESET("Reset center", "https://www.youtube.com/watch?v=q9BmR6E5JvM", TAB_VISUAL, 2),

		// File
		FILE_NEW_CAMPAIGN("New campaign"),
		FILE_NEW_LEVEL("New level"),
		FILE_NEW_ENEMY("New enemy"),
		FILE_NEW_BULLET("New bullet"),
		FILE_DUPLICATE_LEVEL("Create a copy of this level"),
		FILE_DUPLICATE_ENEMY("Create a copy of this enemy"),
		FILE_DUPLICATE_BULLET("Create a copy of this bullet"),
		FILE_SAVE("Save"),
		FILE_OPEN("Open"),
		FILE_PUBLISH_CAMPAIGN("Publish campaign with levels"),
		FILE_PUBLISH_LEVEL("Publish level with actors"),
		FILE_PUBLISH_ENEMY("Publish this enemy"),
		FILE_PUBLISH_BULLET("Publish this bullet"),
		FILE_INFO_CAMPAIGN("Campaign settings"),
		FILE_INFO_LEVEL("Level settings", "https://www.youtube.com/watch?v=MiQCTPsKMS0"),
		FILE_INFO_ENEMY("Enemy settings"),
		FILE_INFO_BULLET("Bullet settings"),

		// Editors
		EDITOR_CAMPAIGN("Campaign editor"),
		EDITOR_LEVEL("Level editor", "https://www.youtube.com/watch?v=MiQCTPsKMS0"),
		EDITOR_ENEMY("Enemy editor", "https://www.youtube.com/watch?v=MKDrEu0leYA"),
		EDITOR_BULLET("Bullet editor", "https://www.youtube.com/watch?v=guOTjFNKRJU"),

		// Top bar middle actions
		ACTION_UNDO("Undo", "https://www.youtube.com/watch?v=jhUNPagEd7Y"),
		ACTION_REDO("Redo", "https://www.youtube.com/watch?v=jhUNPagEd7Y"),
		ACTION_GRID_TOGGLE("Toggle grid", "https://www.youtube.com/watch?v=jhUNPagEd7Y"),
		ACTION_GRID_ABOVE("Toggle grid above actors", "https://www.youtube.com/watch?v=jhUNPagEd7Y"),
		ACTION_ENEMY_SPAWN("Toggle enemy outline spawns", "https://www.youtube.com/watch?v=dM9aY8YqbAc"),
		ACTION_PLAY("Test-run this level"),

		// Path tab (level editor)
		PATH_LOOP("Enemies loop", "https://www.youtube.com/watch?v=DWeUECW8o2w", TAB_PATH),
		PATH_ONCE("Enemies follow path once", "https://www.youtube.com/watch?v=DWeUECW8o2w", TAB_PATH),
		PATH_BACK_AND_FORTH("Enemies goes back and forth", "https://www.youtube.com/watch?v=DWeUECW8o2w", TAB_PATH),

		// Enemy add tab (level editor)
		ENEMY_ADD_TO_LIST("Add enemy to list"),

		// Enemy settings tab (level editor)
		ENEMY_SPAWN_DELAY("Spawn time between copies", "https://www.youtube.com/watch?v=tzHQkAxrx94", TAB_ENEMY),
		ENEMY_ACTIVATION_DELAY("Delay for first spawn", "https://www.youtube.com/watch?v=tzHQkAxrx94", TAB_ENEMY),
		ENEMY_DEACTIVATION_DELAY("Deactivate delay after triggered", "https://www.youtube.com/watch?v=tzHQkAxrx94", TAB_ENEMY),

		// Enemy movement tab (enemy editor)
		MOVEMENT_PATH("Path movement", "https://www.youtube.com/watch?v=whL_LQK64PA", TAB_MOVEMENT, 2),
		MOVEMENT_STATIONARY("Stationary enemy", "https://www.youtube.com/watch?v=whL_LQK64PA", TAB_MOVEMENT, 2),
		MOVEMENT_AI("AI movement", "https://www.youtube.com/watch?v=whL_LQK64PA", TAB_MOVEMENT, 2),
		MOVEMENT_AI_RANDOM_COOLDOWN("Cooldown for new movement direction", "https://www.youtube.com/watch?v=whL_LQK64PA", MOVEMENT_AI),

		// Enemy weapon tab (enemy editor)
		AIM_ON_PLAYER("Aim on player", "https://www.youtube.com/watch?v=SxzoVL5YTHc", TAB_WEAPON),
		AIM_IN_FRONT_OF_PLAYER("Aim in front of player", "https://www.youtube.com/watch?v=SxzoVL5YTHc", TAB_WEAPON),
		AIM_MOVEMENT_DIRECTION("Shoot in enemy movement direction", "https://www.youtube.com/watch?v=SxzoVL5YTHc", TAB_WEAPON),
		AIM_DIRECTION("Shoot in a specific direction", "https://www.youtube.com/watch?v=SxzoVL5YTHc", TAB_WEAPON),
		AIM_ROTATE("Shoot in a rotating manner", "https://www.youtube.com/watch?v=SxzoVL5YTHc", TAB_WEAPON),

		// Visual tab (actor editor)
		VISUAL_CUSTOM("Draw custom shape", "https://www.youtube.com/watch?v=q9BmR6E5JvM", TAB_VISUAL),

		// Collision tab (enemy editor)
		COLLISION_DESTROY("Destroy enemy if it collides with player", "https://www.youtube.com/watch?v=ZbKIrmIbrjk", TAB_COLLISION),


		;

		/**
		 * Constructs a costum tooltip
		 * @param text tooltip text to display
		 */
		private EditorTooltips(String text) {
			mText = text;
		}

		/**
		 * Constructs a costum tooltip
		 * @param text tooltip text to display
		 * @param youtubeLink link to youtube tutorial
		 */
		private EditorTooltips(String text, String youtubeLink) {
			mText = text;
			mYoutubeLink = youtubeLink;
		}

		/**
		 * Constructs a costum tooltip
		 * @param text tooltip text to display
		 * @param youtubeLink link to youtube tutorial
		 * @param parent parent tooltip. Set to null if this is a root tooltip
		 */
		private EditorTooltips(String text, String youtubeLink, ITooltip parent) {
			mText = text;
			mYoutubeLink = youtubeLink;
			mParent = parent;
		}

		/**
		 * Constructs a costum tooltip
		 * @param text tooltip text to display
		 * @param youtubeLink link to youtube totorial
		 * @param parent parent tooltip. Set to null if this is a root tooltip
		 * @param permanentLevel set to the level of priority the permanent should have.
		 *        Set to null if you don't want this tooltip to be a permanent
		 */
		private EditorTooltips(String text, String youtubeLink, ITooltip parent, Integer permanentLevel) {
			mText = text;
			mYoutubeLink = youtubeLink;
			mPermanentLevel = permanentLevel;
			mParent = parent;
		}

		/**
		 * Constructs a costum tooltip
		 * @param text tooltip text to display
		 * @param youtubeLink link to youtube tutorial
		 * @param permanentLevel set to the level of priority the permanent should have.
		 *        Set to null if you don't want this tooltip to be a permanent
		 */
		private EditorTooltips(String text, String youtubeLink, Integer permanentLevel) {
			mText = text;
			mYoutubeLink = youtubeLink;
			mPermanentLevel = permanentLevel;
		}

		/**
		 * Constructs a costum tooltip
		 * @param text tooltip text to display
		 * @param youtubeLink link to youtube tutorial, may be null
		 * @param parent parent tooltip. Set to null if this is a root tooltip
		 * @param permanentLevel set to the level of priority the permanent should have.
		 *        Set to null if you don't want this tooltip to be a permanent
		 * @param hotkey a hotkey for the tooltip, may be null @param youtubeLink link to
		 *        youtube tutorial, may be null
		 * @param youtubeOnly set to true to only show the youtube link and no hover
		 *        messages.
		 * @param hideWhenHidden true (default) to hide the tooltip if the actor is
		 *        hidden. If false the tooltip will be shown even though the actor is
		 *        hidden.
		 */
		private EditorTooltips(String text, String youtubeLink, ITooltip parent, Integer permanentLevel, String hotkey, boolean youtubeOnly,
				boolean hideWhenHidden) {
			mText = text;
			mPermanentLevel = permanentLevel;
			mParent = parent;
			mHotkey = hotkey;
			mYoutubeLink = youtubeLink;
			mYoutubeOnly = youtubeOnly;
			mHideWhenHidden = hideWhenHidden;
		}

		@Override
		public String getText() {
			return mText;
		}

		@Override
		public String getHotkey() {
			return mHotkey;
		}

		@Override
		public String getYoutubeLink() {
			return mYoutubeLink;
		}

		@Override
		public boolean isYoutubeOnly() {
			return mYoutubeOnly;
		}

		@Override
		public boolean isPermanent() {
			return mPermanentLevel != null;
		}

		@Override
		public ITooltip getParent() {
			return mParent;
		}

		@Override
		public boolean hasYoutubeLink() {
			return mYoutubeLink != null;
		}

		@Override
		public boolean hasHotkey() {
			return mHotkey != null;
		}

		@Override
		public int getLevel() {
			return mPermanentLevel;
		}

		@Override
		public boolean shouldHideWhenHidden() {
			return mHideWhenHidden;
		}

		/** Text for the tooltip */
		private String mText;
		/** Hotkey for tooltip */
		private String mHotkey = null;
		/** YouTube link */
		private String mYoutubeLink = null;
		/** If the tooltip is YouTube-only */
		private boolean mYoutubeOnly = false;
		/** Parent tooltip */
		private ITooltip mParent;
		/** Level of the tooltip */
		private Integer mPermanentLevel = null;
		/** Should hide when hidden */
		private boolean mHideWhenHidden = true;
	}

	/**
	 * Replaces ACTOR_TYPE inside a message with the correct name
	 * @param message the original message (this will never be changed)
	 * @param actorTypeName name of the actor type to be shown in the message
	 * @return new message where ACTOR_TYPE has been replaced with actorTypeName :)
	 */
	public static String replaceName(String message, String actorTypeName) {
		return message.replace(ACTOR_TYPE, actorTypeName);
	}

	/** Actor type to be replaced in text */
	private final static String ACTOR_TYPE = "ACTOR_TYPE";
}

package com.spiddekauga.voider.utils;

import com.spiddekauga.utils.scene.ui.TooltipWidget.ITooltip;
import com.spiddekauga.voider.ClientVersions;
import com.spiddekauga.voider.Config;

/**
 * Class containing all messages for voider, including help function for retrieving
 * messages
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class Messages {
	// @formatter:off

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
		/** Switching to ship editor */
		SHIP_EDITOR("switching to Ship Editor"),
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
	 * Info Menu messages
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
		public final static String RUN_INVULNERABLE_TITLE = "Test play level";
		/**
		 * Message when the player is asked to select if s/he shall be invulnerable when
		 * testing the level.
		 */
		public final static String RUN_INVULNERABLE_CONTENT = "Do you want to be invulnerable when you're testing the level?\n\n"
				+ "Hit Escape or Back when you want to go back.";
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
		public final static String BUG_REPORT_INFO = "You found a bug! "
				+ "Please describe the last things you did. This helps enormously when debugging, thank you :)";
		/** Name must contain X characters */
		public final static String NAME_CHARACTERS_MIN = "must contain at least " + Config.Actor.NAME_LENGTH_MIN + " characters";
	}

	/**
	 * Client version messages
	 */
	public static class Version {

		/**
		 * Get required update message
		 * @param newVersion latest client version available
		 * @return required message update
		 */
		public static String getRequiredUpdate(String newVersion) {
			String message = UPDATE_REQUIRED;
			message = message.replaceAll(OLD_VERSION_STRING, ClientVersions.getLatest().toString());
			message = message.replaceAll(NEW_VERSION_STRING, newVersion);
			return message;
		}

		/**
		 * Get available update message
		 * @param newVersion latest client version available
		 * @return available message update
		 */
		public static String getOptionalUpdate(String newVersion) {
			String message = UPDATE_OPTIONAL;
			message = message.replaceAll(OLD_VERSION_STRING, ClientVersions.getLatest().toString());
			message = message.replaceAll(NEW_VERSION_STRING, newVersion);
			return message;
		}

		private static final String OLD_VERSION_STRING = "OLD-VERSION";
		private static final String NEW_VERSION_STRING = "NEW-VERSION";

		/** Update required message */
		private static final String UPDATE_REQUIRED = "You are offline!\n"
				+ "A _mandatory_ update is available for Voider. "
				+ "Please update Voider access online features.\n\n"
				+ "Your version: " + OLD_VERSION_STRING + "\n"
				+ "Latest version: " + NEW_VERSION_STRING;

		/** Update available message */
		private static final String UPDATE_OPTIONAL = "An optional update is available for Voider. "
				+ "Please update Voider for bugfixes, improvements and new features.\n\n"
				+ "Your version: " + OLD_VERSION_STRING + "\n"
				+ "Latest version: " + NEW_VERSION_STRING;

	}

	/**
	 * Editor tooltip messages
	 */
	@SuppressWarnings("javadoc")
	public enum EditorTooltips implements ITooltip {
		// Level tabs
		TAB_ENEMY_ADD("Add enemy tab", "07m21s"),
		TAB_ENEMY("Enemy settings"),
		TAB_PATH("Path settings", "09m05s"),
		TAB_COLOR_LEVEL("Terrain color", "05m13s"),

		// Actor tabs
		TAB_VISUAL("Shape settings", "18m53s", 1),
		TAB_COLOR_ACTOR("Color settings", "21m27s", 1),

		// Enemy tabs
		TAB_MOVEMENT("Movement settings", "13m16s", 1),
		TAB_WEAPON("Weapon settings", "15m35s", 1),
		TAB_COLLISION("Collision settings", "21m43s", 1),

		// Bullet tabs
		TAB_BULLET_TEST("Test bullet on weapons", "22m35s", 1),

		// Tools
		TOOL_SELECTION("Selection tool (S)", , 1),
		TOOL_PAN_LEVEL("Pan tool", , 1),
		TOOL_PAN_ACTOR(TOOL_PAN_LEVEL.mText, TOOL_PAN_LEVEL.mYoutubeLink, TAB_VISUAL, 2),
		TOOL_MOVE_LEVEL("Move tool (M)", , 1),
		TOOL_MOVE_ACTOR(TOOL_MOVE_LEVEL.mText, TOOL_MOVE_LEVEL.mYoutubeLink, TAB_VISUAL, 2),
		TOOL_DELETE_LEVEL("Delete tool (Del)", , 1),
		TOOL_DELETE_ACTOR(TOOL_DELETE_LEVEL.mText, TOOL_DELETE_LEVEL.mYoutubeLink, TAB_VISUAL, 2),
		TOOL_CLEAR_SELECTION("Clear selection (Esc/Back)", ),
		TOOL_ZOOM_IN_LEVEL("Zoom in", , 1),
		TOOL_ZOOM_OUT_LEVEL("Zoom out", , 1),
		TOOL_ZOOM_RESET_LEVEL("Reset zoom", , 1),
		TOOL_ZOOM_IN_ACTOR(TOOL_ZOOM_IN_LEVEL.mText, TOOL_ZOOM_IN_LEVEL.mYoutubeLink, TAB_VISUAL, 2),
		TOOL_ZOOM_OUT_ACTOR(TOOL_ZOOM_OUT_LEVEL.mText, TOOL_ZOOM_OUT_LEVEL.mYoutubeLink, TAB_VISUAL, 2),
		TOOL_ZOOM_RESET_ACTOR(TOOL_ZOOM_RESET_LEVEL.mText, TOOL_ZOOM_RESET_LEVEL.mYoutubeLink, TAB_VISUAL, 2),
		TOOL_DRAW_APPEND_TERRAIN("Draw terrain (D)", , 1),
		TOOL_DRAW_ERASE_TERRAIN("Add/Remove area to/from terrain (Shift+D)", , 1),
		TOOL_DRAW_CORNER_ADD_TERRAIN("Add/Move terrain corners", , 1),
		TOOL_DRAW_CORNER_REMOVE_TERRAIN("Remove terrain corners", , 1),
		TOOL_DRAW_APPEND_ACTOR("Draw shape (T)", , TAB_VISUAL, 2),
		TOOL_DRAW_ERASE_ACTOR("Add/Remove area to/from shape (Shift+T)", , TAB_VISUAL, 2),
		TOOL_DRAW_CORNER_ADD_ACTOR("Add/Move shape corners (C)", , TAB_VISUAL, 2),
		TOOL_DRAW_CORNER_REMOVE_ACTOR("Remove shape corners (Shift+C)", , TAB_VISUAL, 2),
		TOOL_ENEMY_ADD("Add enemies (E)", , 1),
		TOOL_PATH("Add paths for enemies (P)", , 1),
		TOOL_TRIGGER_ACTIVATE("Set enemy activate triggers (T)", , 1),
		TOOL_TRIGGER_DEACTIVATE("Set enemy deactivate triggers (Shift+T)", , 1),
		TOOL_CENTER_SET("Set center", , TAB_VISUAL, 2),
		TOOL_CENTER_RESET("Reset center", , TAB_VISUAL, 2),

		// File
		FILE_NEW_CAMPAIGN("New campaign"),
		FILE_NEW_LEVEL("New level"),
		FILE_NEW_ENEMY("New enemy"),
		FILE_NEW_BULLET("New bullet"),
		FILE_NEW_SHIP("New ship"),
		FILE_DUPLICATE_LEVEL("Create a copy of this level", ),
		FILE_DUPLICATE_ENEMY("Create a copy of this enemy", ),
		FILE_DUPLICATE_BULLET("Create a copy of this bullet", ),
		FILE_DUPLICATE_SHIP("Create a copy of this ship", ),
		FILE_SAVE("Save"),
		FILE_OPEN("Open"),
		FILE_PUBLISH_CAMPAIGN("Publish campaign with levels"),
		FILE_PUBLISH_LEVEL("Publish level with actors", ),
		FILE_PUBLISH_ENEMY("Publish this enemy", ),
		FILE_PUBLISH_BULLET("Publish this bullet", ),
		FILE_INFO_CAMPAIGN("Campaign settings"),
		FILE_INFO_LEVEL("Level settings", ),
		FILE_INFO_ENEMY("Enemy settings"),
		FILE_INFO_BULLET("Bullet settings"),
		FILE_INFO_SHIP("Ship name"),

		// Editors
		EDITOR_CAMPAIGN("Campaign editor"),
		EDITOR_LEVEL("Level editor", ),
		EDITOR_ENEMY("Enemy editor", ),
		EDITOR_BULLET("Bullet editor", ),
		EDITOR_SHIP("Ship editor"),

		// Top bar middle actions
		ACTION_UNDO("Undo", ),
		ACTION_REDO("Redo", ),
		ACTION_BUG_REPORT("Bug Report"),
		ACTION_GRID_TOGGLE("Toggle grid", ),
		ACTION_GRID_ABOVE("Toggle grid above actors", ),
		ACTION_PLAY_FROM_HERE("Test play level from current position", ),
		ACTION_PLAY_FROM_START("Test play level", ),

		// Path tab (level editor)
		PATH_LOOP("Enemies loop", , TAB_PATH),
		PATH_ONCE("Enemies follow path once", , TAB_PATH),
		PATH_BACK_AND_FORTH("Enemies goes back and forth", , TAB_PATH),

		// Enemy add tab (level editor)
		ENEMY_ADD_TO_LIST("Add enemy to list"),

		// Enemy settings tab (level editor)
		ENEMY_SPAWN_DELAY("Spawn time between copies", , TAB_ENEMY),
		ENEMY_ACTIVATION_DELAY("Spawn delay", , TAB_ENEMY),
		ENEMY_DEACTIVATION_DELAY("Despawn delay", , TAB_ENEMY),

		// Enemy movement tab (enemy editor)
		MOVEMENT_PATH("Path movement", , TAB_MOVEMENT, 2),
		MOVEMENT_STATIONARY("Stationary enemy", , TAB_MOVEMENT, 2),
		MOVEMENT_AI("AI movement", , TAB_MOVEMENT, 2),
		MOVEMENT_AI_RANDOM_COOLDOWN("Cooldown for new movement direction", , MOVEMENT_AI),

		// Enemy weapon tab (enemy editor)
		AIM_ON_PLAYER("Aim on player", , TAB_WEAPON),
		AIM_IN_FRONT_OF_PLAYER("Aim in front of player",, TAB_WEAPON),
		AIM_MOVEMENT_DIRECTION("Shoot in enemy movement direction", , TAB_WEAPON),
		AIM_DIRECTION("Shoot in a specific direction", , TAB_WEAPON),
		AIM_ROTATE("Shoot in a rotating manner", , TAB_WEAPON),

		// Visual tab (actor editor)
		VISUAL_CUSTOM("Draw custom shape", , TAB_VISUAL),

		// Collision tab (enemy editor)
		COLLISION_DESTROY("Destroy enemy if it collides with player", , TAB_COLLISION),


		;

		/**
		 * Constructs a temporary tooltip
		 * @param text tooltip text to display
		 */
		private EditorTooltips(String text) {
			mText = text;
		}

		/**
		 * Constructs a temporary tooltip
		 * @param text tooltip text to display
		 * @param youtubeTime time in the YouTube tutorial
		 */
		private EditorTooltips(String text, String youtubeTime) {
			mText = text;
			mYoutubeTime = youtubeTime;
		}

		/**
		 * Constructs a temporary tooltip
		 * @param text tooltip text to display
		 * @param youtubeTime time in the YouTube tutorial
		 * @param parent parent tooltip. Set to null if this is a root tooltip
		 */
		private EditorTooltips(String text, String youtubeTime, ITooltip parent) {
			mText = text;
			mYoutubeTime = youtubeTime;
			mParent = parent;
		}

		/**
		 * Constructs a permanent tooltip
		 * @param text tooltip text to display
		 * @param youtubeTime time in the YouTube tutorial
		 * @param parent parent tooltip. Set to null if this is a root tooltip
		 * @param permanentLevel set to the level of priority the permanent should have.
		 *        Set to null if you don't want this tooltip to be a permanent
		 */
		private EditorTooltips(String text, String youtubeTime, ITooltip parent, Integer permanentLevel) {
			mText = text;
			mYoutubeTime = youtubeTime;
			mPermanentLevel = permanentLevel;
			mParent = parent;
		}

		/**
		 * Constructs a permanent tooltip
		 * @param text tooltip text to display
		 * @param youtubeTime time in the YouTube tutorial
		 * @param permanentLevel set to the level of priority the permanent should have.
		 *        Set to null if you don't want this tooltip to be a permanent
		 */
		private EditorTooltips(String text, String youtubeTime, Integer permanentLevel) {
			mText = text;
			mYoutubeTime = youtubeTime;
			mPermanentLevel = permanentLevel;
		}

		/**
		 * Constructs a temporary tooltip
		 * @param text tooltip text to display
		 * @param youtubeTime time in the YouTube tutorial
		 * @param parent parent tooltip. Set to null if this is a root tooltip
		 * @param permanentLevel set to the level of priority the permanent should have.
		 *        Set to null if you don't want this tooltip to be a permanent
		 * @param hotkey a hotkey for the tooltip, may be null @param youtubeLink link to
		 *        YouTube tutorial, may be null
		 * @param youtubeOnly set to true to only show the YouTube link and no hover
		 *        messages.
		 * @param hideWhenHidden true (default) to hide the tooltip if the actor is
		 *        hidden. If false the tooltip will be shown even though the actor is
		 *        hidden.
		 */
		private EditorTooltips(String text, String youtubeTime, ITooltip parent, Integer permanentLevel, String hotkey, boolean youtubeOnly,
				boolean hideWhenHidden) {
			mText = text;
			mPermanentLevel = permanentLevel;
			mParent = parent;
			mHotkey = hotkey;
			mYoutubeTime = youtubeTime;
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
			if (mYoutubeTime != null) {
			return YOUTUBE_URL + "#t=" + mYoutubeTime;
			} else {
				return null;
			}
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
			return mYoutubeTime != null;
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
		/** If the tooltip is YouTube-only */
		private boolean mYoutubeOnly = false;
		/** YouTube time */
		private String mYoutubeTime = null;
		/** Parent tooltip */
		private ITooltip mParent;
		/** Level of the tooltip */
		private Integer mPermanentLevel = null;
		/** Should hide when hidden */
		private boolean mHideWhenHidden = true;


		/** YouTube video */
		private static final String YOUTUBE_URL = "https://youtu.be/KRPMoLZ2ZN8";
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

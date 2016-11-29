package com.spiddekauga.voider.utils;

import com.spiddekauga.utils.scene.ui.TooltipWidget.ITooltip;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.repo.misc.SettingRepo;

import java.util.Date;

/**
 * Class containing all messages for voider, including help function for retrieving messages
 */
public class Messages {
// @formatter:off

	/** Actor type to be replaced in text */
	private final static String ACTOR_TYPE = "$ACTOR_TYPE";

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
	 * Replaces ACTOR_TYPE inside a message with the correct name
	 * @param message the original message (this will never be changed)
	 * @param actorTypeName name of the actor type to be shown in the message
	 * @return new message where ACTOR_TYPE has been replaced with actorTypeName :)
	 */
	public static String replaceName(String message, String actorTypeName) {
		return message.replace(ACTOR_TYPE, actorTypeName);
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
		/** replacement variable */
		public final static String UNSAVED_TYPE = "UNSAVED_TYPE";
		/** The message that will be used for the action */
		private String mMessage;

		/**
		 * Creates an unsaved action with the message
		 * @param message the message to use for the action. Any instance of UNSAVED_TYPE
		 *        in the message will later be replaced with the actual unsaved type.
		 */
		private UnsavedActions(String message) {
			mMessage = message;
		}

		/**
		 * @param unsavedType the unsaved type, which will replace any instance of
		 *        UNSAVED_TYPE within the message.
		 * @return the message of the action
		 */
		public String getMessage(String unsavedType) {
			return mMessage.replace(UNSAVED_TYPE, unsavedType);
		}
	}

	/**
	 * Editor tooltip messages
	 */
	@SuppressWarnings("javadoc")
	public enum EditorTooltips implements ITooltip {
		// Level tabs
		TAB_ENEMY_ADD("Add enemy tab", "7m21s"),
		TAB_ENEMY("Enemy settings"),
		TAB_PATH("Path settings", "9m05s"),
		TAB_COLOR_LEVEL("Terrain color", "5m13s"),

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
		TOOL_SELECTION("Selection tool (S)", "5m26s", 1),
		TOOL_PAN_LEVEL("Pan tool", "6m36s", 1),
		TOOL_PAN_ACTOR(TOOL_PAN_LEVEL.mText, TOOL_PAN_LEVEL.mYoutubeTime, TAB_VISUAL, 2),
		TOOL_MOVE_LEVEL("Move tool (M)", "5m53s", 1),
		TOOL_MOVE_ACTOR(TOOL_MOVE_LEVEL.mText, TOOL_MOVE_LEVEL.mYoutubeTime, TAB_VISUAL, 2),
		TOOL_DELETE_LEVEL("Delete tool (Del)", "6m12s", 1),
		TOOL_DELETE_ACTOR(TOOL_DELETE_LEVEL.mText, TOOL_DELETE_LEVEL.mYoutubeTime, TAB_VISUAL, 2),
		TOOL_CLEAR_SELECTION("Clear selection (Esc/Back)", TOOL_SELECTION.mYoutubeTime),
		TOOL_ZOOM_IN_LEVEL("Zoom in", "7m", 1),
		TOOL_ZOOM_OUT_LEVEL("Zoom out", "7m" , 1),
		TOOL_ZOOM_RESET_LEVEL("Reset zoom", "7m", 1),
		TOOL_ZOOM_IN_ACTOR(TOOL_ZOOM_IN_LEVEL.mText, TOOL_ZOOM_IN_LEVEL.mYoutubeTime, TAB_VISUAL, 2),
		TOOL_ZOOM_OUT_ACTOR(TOOL_ZOOM_OUT_LEVEL.mText, TOOL_ZOOM_OUT_LEVEL.mYoutubeTime, TAB_VISUAL, 2),
		TOOL_ZOOM_RESET_ACTOR(TOOL_ZOOM_RESET_LEVEL.mText, TOOL_ZOOM_RESET_LEVEL.mYoutubeTime, TAB_VISUAL, 2),
		TOOL_DRAW_APPEND_TERRAIN("Draw terrain (D)", "3m48s", 1),
		TOOL_DRAW_ERASE_TERRAIN("Add/Remove area to/from terrain (Shift+D)", "4m33s", 1),
		TOOL_DRAW_CORNER_ADD_TERRAIN("Add/Move terrain corners", "4m45s" , 1),
		TOOL_DRAW_CORNER_REMOVE_TERRAIN("Remove terrain corners", "5m2s", 1),
		TOOL_DRAW_APPEND_ACTOR("Draw shape (T)", "20m", TAB_VISUAL, 2),
		TOOL_DRAW_ERASE_ACTOR("Add/Remove area to/from shape (Shift+T)", "20m", TAB_VISUAL, 2),
		TOOL_DRAW_CORNER_ADD_ACTOR("Add/Move shape corners (C)", "20m", TAB_VISUAL, 2),
		TOOL_DRAW_CORNER_REMOVE_ACTOR("Remove shape corners (Shift+C)", "20m", TAB_VISUAL, 2),
		TOOL_ENEMY_ADD("Add enemies (E)", "7m21s", 1),
		TOOL_PATH("Add paths for enemies (P)", "8m30s", 1),
		TOOL_TRIGGER_ACTIVATE("Set enemy spawn triggers (T)", "10m08s", 1),
		TOOL_TRIGGER_DEACTIVATE("Set enemy despawn triggers (Shift+T)", "10m08s", 1),
		TOOL_CENTER_SET("Set center", "20m49s", TAB_VISUAL, 2),
		TOOL_CENTER_RESET("Reset center", "20m49s", TAB_VISUAL, 2),

		// File
		FILE_NEW_CAMPAIGN("New campaign"),
		FILE_NEW_LEVEL("New level"),
		FILE_NEW_ENEMY("New enemy"),
		FILE_NEW_BULLET("New bullet"),
		FILE_NEW_SHIP("New ship"),
		FILE_DUPLICATE_LEVEL("Create a copy of this level", "1m17s"),
		FILE_DUPLICATE_ENEMY("Create a copy of this enemy", "1m17s"),
		FILE_DUPLICATE_BULLET("Create a copy of this bullet","1m17s" ),
		FILE_DUPLICATE_SHIP("Create a copy of this ship","1m17s" ),
		FILE_SAVE("Save"),
		FILE_OPEN("Open"),
		FILE_PUBLISH_CAMPAIGN("Publish campaign with levels"),
		FILE_PUBLISH_LEVEL("Publish level with actors", "2m37s"),
		FILE_PUBLISH_ENEMY("Publish this enemy", "2m37s"),
		FILE_PUBLISH_BULLET("Publish this bullet", "2m37s"),
		FILE_INFO_CAMPAIGN("Campaign settings"),
		FILE_INFO_LEVEL("Level settings", ""),
		FILE_INFO_ENEMY("Enemy settings"),
		FILE_INFO_BULLET("Bullet settings"),
		FILE_INFO_SHIP("Ship name"),

		// Editors
		EDITOR_CAMPAIGN("Campaign editor"),
		EDITOR_LEVEL("Level editor", "33s"),
		EDITOR_ENEMY("Enemy editor", "12m45s"),
		EDITOR_BULLET("Bullet editor", "22m23s"),
		EDITOR_SHIP("Ship editor"),

		// Top bar middle actions
		ACTION_UNDO("Undo", "47s"),
		ACTION_REDO("Redo", "47s"),
		ACTION_BUG_REPORT("Bug Report", "50s"),
		ACTION_GRID_TOGGLE("Toggle grid", "53s"),
		ACTION_PLAY_FROM_HERE("Test play level from current position", "1m1s"),
		ACTION_PLAY_FROM_START("Test play level", "57s"),

		// Path tab (level editor)
		PATH_LOOP("Enemies loop", "9m5s", TAB_PATH),
		PATH_ONCE("Enemies follow path once", "9m5s", TAB_PATH),
		PATH_BACK_AND_FORTH("Enemies goes back and forth", "9m5s", TAB_PATH),

		// Enemy add tab (level editor)
		ENEMY_ADD_TO_LIST("Add enemy to list"),

		// Enemy settings tab (level editor)
		ENEMY_SPAWN_DELAY("Spawn time between copies", "9m43s", TAB_ENEMY),
		ENEMY_ACTIVATION_DELAY("Spawn delay", "10m45s", TAB_ENEMY),
		ENEMY_DEACTIVATION_DELAY("Despawn delay", "10m45s", TAB_ENEMY),

		// Enemy movement tab (enemy editor)
		MOVEMENT_PATH("Path movement", "13m16s", TAB_MOVEMENT, 2),
		MOVEMENT_STATIONARY("Stationary enemy", "15m29s", TAB_MOVEMENT, 2),
		MOVEMENT_AI("AI movement", "14:15", TAB_MOVEMENT, 2),
		MOVEMENT_AI_RANDOM_COOLDOWN("Cooldown for new movement direction", "14m45s", MOVEMENT_AI),

		// Enemy weapon tab (enemy editor)
		AIM_ON_PLAYER("Aim on player", "17m15s", TAB_WEAPON),
		AIM_IN_FRONT_OF_PLAYER("Aim in front of player","17m15s", TAB_WEAPON),
		AIM_MOVEMENT_DIRECTION("Shoot in enemy movement direction", "17m15s", TAB_WEAPON),
		AIM_DIRECTION("Shoot in a specific direction","17m15s" , TAB_WEAPON),
		AIM_ROTATE("Shoot in a rotating manner", "17m15s", TAB_WEAPON),

		// Visual tab (actor editor)
		VISUAL_CUSTOM("Draw custom shape", "20m" , TAB_VISUAL),

		// Collision tab (enemy editor)
		COLLISION_DESTROY("Destroy enemy if it collides with player", "21m43s", TAB_COLLISION),


		;

		/** YouTube video */
		private static final String YOUTUBE_URL = "https://youtu.be/KRPMoLZ2ZN8";
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

		/**
		 * Constructs a temporary tooltip
		 * @param text tooltip text to display
		 */
		EditorTooltips(String text) {
			mText = text;
		}

		/**
		 * Constructs a temporary tooltip
		 * @param text tooltip text to display
		 * @param youtubeTime time in the YouTube tutorial
		 */
		EditorTooltips(String text, String youtubeTime) {
			mText = text;
			mYoutubeTime = youtubeTime;
		}

		/**
		 * Constructs a temporary tooltip
		 * @param text tooltip text to display
		 * @param youtubeTime time in the YouTube tutorial
		 * @param parent parent tooltip. Set to null if this is a root tooltip
		 */
		EditorTooltips(String text, String youtubeTime, ITooltip parent) {
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
		EditorTooltips(String text, String youtubeTime, ITooltip parent, Integer permanentLevel) {
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
		EditorTooltips(String text, String youtubeTime, Integer permanentLevel) {
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
		EditorTooltips(String text, String youtubeTime, ITooltip parent, Integer permanentLevel, String hotkey, boolean youtubeOnly,
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
		public boolean hasYoutubeLink() {
			return mYoutubeTime != null;
		}

		@Override
		public boolean hasHotkey() {
			return mHotkey != null;
		}

		@Override
		public ITooltip getParent() {
			return mParent;
		}

		@Override
		public int getLevel() {
			return mPermanentLevel;
		}

		@Override
		public boolean shouldHideWhenHidden() {
			return mHideWhenHidden;
		}
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
		public static final String BUG_REPORT_SAVED_LOCALLY = "Could not connect to the server, temporarily saved the bug report locally.";
		/** Functionality request for server restore */
		public static final String SERVER_RESTORED_CHANGE = "If you don't want this to happen in the future, please go to the community site "
				+ "and vote to implement 'Improved Server Reverted functionality'. I can then improve the "
				+ "functionality so that most of your changes can be saved. This will however take substantial amount "
				+ "of time (equivalent of implementing a boss editor).\n\n";
		/** Message shown when the server has been restored and all local data needs to be cleared */
		private static final String SERVER_RESTORED = "An error occured in the server database. Unfortunately this means that all changes "
				+ "after $DATE_TO will be lost. Press 'Revert & Logout' to continue :(.\n"
				+ "\n"
				+ "Note! If you continue playing offline NOTHING will be saved!!!";

		/**
		 * Get the server restored message
		 * @param from when the server reverted
		 * @param to which date the server reverted to
		 * @return server restored message
		 */
		public static String getServerRestored(Date from, Date to) {
			String fromString = SettingRepo.getInstance().date().getDateTime(from);
			String toString = SettingRepo.getInstance().date().getDateTime(to);

			return SERVER_RESTORED.replace("$DATE_FROM", fromString).replace("$DATE_TO", toString);
		}
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
		/** Bug report */
		public final static String BUG_REPORT_INFO = "You found a bug! "
				+ "Please describe the last things you did. This helps enormously when debugging, thank you :)";
		/** Name must contain X characters */
		public final static String NAME_CHARACTERS_MIN = "must contain at least " + Config.Actor.NAME_LENGTH_MIN + " characters";
	}

	/**
	 * Client gameVersion messages
	 */
	public static class Version {

		private static final String OLD_VERSION_STRING = "OLD-VERSION";
		private static final String NEW_VERSION_STRING = "NEW-VERSION";
		/** Update required message */
		private static final String UPDATE_REQUIRED = "You are offline!\n"
				+ "A _mandatory_ update is available for Voider. "
				+ "Please update Voider access online features.\n\n"
				+ "Your gameVersion: " + OLD_VERSION_STRING + "\n"
				+ "Latest gameVersion: " + NEW_VERSION_STRING;
		/** Update available message */
		private static final String UPDATE_OPTIONAL = "An optional update is available for Voider. "
				+ "Please update Voider for bugfixes, improvements and new features.\n\n"
				+ "Your gameVersion: " + OLD_VERSION_STRING + "\n"
				+ "Latest gameVersion: " + NEW_VERSION_STRING;

		/**
		 * Get required update message
		 * @param newVersion latest client gameVersion available
		 * @return required message update
		 */
		public static String getRequiredUpdate(String newVersion) {
			String message = UPDATE_REQUIRED;
			message = message.replaceAll(OLD_VERSION_STRING, SettingRepo.getInstance().info().getCurrentVersion().getVersion());
			message = message.replaceAll(NEW_VERSION_STRING, newVersion);
			return message;
		}

		/**
		 * Get available update message
		 * @param newVersion latest client gameVersion available
		 * @return available message update
		 */
		public static String getOptionalUpdate(String newVersion) {
			String message = UPDATE_OPTIONAL;
			message = message.replaceAll(OLD_VERSION_STRING, SettingRepo.getInstance().info().getCurrentVersion().getVersion());
			message = message.replaceAll(NEW_VERSION_STRING, newVersion);
			return message;
		}

	}
}

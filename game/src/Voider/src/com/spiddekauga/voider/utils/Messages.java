package com.spiddekauga.voider.utils;


/**
 * Class containing all messages for voider, including help function
 * for retrieving messages
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class Messages {

	/**
	 * Print this string when no definition is selected
	 * @param defTypeName name of the definition, this will be inserted into
	 * the message
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
		String message = "Your current " + unsavedType + " is unsaved.\n" +
				"Do you want to save it before " + action.getMessage(unsavedType) + "?";
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
		DUPLICATE("duplicating it"),
		/** Switching to the level editor */
		LEVEL_EDITOR("switching to the level editor"),
		/** Switching to the enemy editor */
		ENEMY_EDITOR("switching to the enemy editor"),
		/** Switching to bullet editor */
		BULLET_EDITOR("switching to the bullet editor"),
		/** Switching to campaign editor */
		CAMPAIGN_EDITOR("switching to the campaign editor")

		;
		/**
		 * @param unsavedType the unsaved type, which will replace any
		 * instance of UNSAVED_TYPE within the message.
		 * @return the message of the action
		 */
		public String getMessage(String unsavedType) {
			return mMessage.replace(UNSAVED_TYPE, unsavedType);
		}

		/**
		 * Creates an unsaved action with the message
		 * @param message the message to use for the action. Any instance
		 * of UNSAVED_TYPE in the message will later be replaced with the actual
		 * unsaved type.
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
	 * Class for all tooltip messages
	 */
	@SuppressWarnings("javadoc")
	public static class Tooltip {
		public static class Level {
			public static class Menu {
				public final static String RUN = "Test run the level from this location. Triggers " +
						"on this screen and before will not be activated, thus some enemies might stand " +
						"still.\n\n" +
						"You can decide whether or not you want to loose life during this test to test the " +
						"level's difficulty.";
				public final static String TERRAIN = "Draw your own Terrain using this tool";
				public final static String PICKUP = "Place various pickups that the player will " +
						"be able to pick up during the game.";
				public final static String ENEMY = "Allows you to place enemies in the level. You, however," +
						" need to create some enemies before using the Enemy Editor found by pressing Back " +
						"(Android) or Escape (PC).\n\n" +
						"Includes tools for enemies such as drawing paths for enemies to follow " +
						"and placing triggers to activate and deactivate the enemies.";
				public final static String OPTION = "Allows you to set general level options such as, name, " +
						"description, level speed, story to be displayed before and after the level.";
			}

			public static class Terrain {
				public final static String ADD = "Adds corners to the terrain, you are also able to" +
						"select other terrains with it.\n\n" +
						"Usage:\n" +
						"When no Terrain is selected it will add a new Terrain and a corner.\n" +
						"When a Terrain is selected it add a new corner.\n" +
						"Click/touch on a corner to move it.\n" +
						"Click/touch on another terrain to select it\n" +
						"Double click on the terrain to \"finish\" and deselect it, " +
						"now you are able to start a new Terrain\n" +
						"Edges of the same Terrain is not allowed to intersect.\n" +
						"Corners cannot be added between other corners at the moment.";
				public final static String REMOVE = "Removes either corners or the whole terrain" +
						"\n\n" +
						"Usage:\n" +
						"Press on a corner to remove it.\n" +
						"Press in a terrain to remove the whole terrain.";
				public final static String MOVE = "Moves the whole terrain. Use add to move individual " +
						"corners.";
			}

			public static class Pickup {
				public final static String ADD = "Adds pickups to the level. To be able to add a pickup " +
						"you must first select one using the \"Select type\" button below. Can also move" +
						"existing pickups.";
				public final static String REMOVE = "Removes pickups from the level by clicking/touching them.";
				public final static String MOVE = "Moves existing pickps.";
				public final static String SELECT_NAME = "Pickup name of new pickups that will be added.";
				public final static String SELECT_TYPE = "Select a pickup type, all new pickups will now be" +
						"of this type. The selected pickup name is displayed to the left.";
			}

			public static class EnemyMenu {
				public final static String ENEMY = "Add/Move/Remove enemies using this tool.";
				public final static String PATH = "Draw paths for the enemies to follow.";
				public final static String TRIGGER = "Create trigger that enemies can use to " +
						"activate and deactivate.";
			}

			public static class Enemy {
				public final static String SELECT = "Selects an enemy, used when you want to see " +
						"the enemy's option without moving it.";
				public final static String ADD = "Adds a new enemy or moves an existing one. " +
						"To add an enemy you first need to select one by pressing \"Select type\"." +
						"\n\n" +
						"Usage:\n" +
						"Static enemies do not have any option and they automatically activates " +
						"and deactivates when they come on the screen.\n" +
						"Enemies that follow a path will snap to the beggining of a path (marked green)." +
						"NOTE: Enemies that follow a path but aren't bound to one will be stationary, but" +
						"still need a trigger to activate.\n" +
						"Path and AI enemies have additional options, see their tooltip for how they work.";
				public final static String REMOVE = "Removes an enemy.";
				public final static String MOVE = "Moves an enemy. Enemies that follow a path will snap to " +
						"the beginning of a path (marked as green).";
				public final static String SELECT_NAME = "Enemy name of new enemies that will be added.";
				public final static String SELECT_TYPE = "Selects an enemy type, all new enemies will now be " +
						"of this type. The selected enemy name is displayed to the left.";
				public final static String ENEMY_COUNT = "Duplicates of the selected enemy.";
				public final static String ENEMY_SPAWN_DELAY = "Delay between when each of the enemy is spawned, " +
						"or rather activated.";
				public final static String SET_ACTIVATE_TRIGGER = "Click/touch to select a trigger to " +
						"be used as an activate trigger for the enemy, i.e. the enemy will activate " +
						"when this trigger is shot. " +
						"If you want to delay the activation by X seconds you can do that below (only shown " +
						"if a trigger has been selected). If you cannot see deactivate trigger, it " +
						"is only available for AI enemies " +
						"as path and stationary enemies automatically deactivates.";
				public final static String ACTIVATE_DELAY = "Delay the activation by X seconds.\nExample of use:\n " +
						"You have one path, but want two different enemies to follow it but not activate exactly at" +
						"the same time. You then set the activation trigger of both enemies to the same value, but" +
						"set this delay in one of the enemies, now the other will start X seconds after the other one.";
				public final static String SET_DEACTIVATE_DELAY = "Click/touch to select a trigger to be used " +
						"as a deactivate trigger for the enemy, i.e. the enemy will deactivate when this " +
						"trigger is shot. If you you want to delay the deactivation by X seconds you can do that below " +
						"(only shown if a trigger has been selected. Deactivate trigger is only available for AI enemies " +
						"as path and stationary enemies automatically deactivates.";
				public final static String DEACTIVATE_DELAY = "Delay the deactivation by X seconds.\nExample of use:\n" +
						"You want the AI enemy to only be active for 10 seconds. You can then select its own activation " +
						"trigger (if one has been created). This trigger will be shot once the enemy is activated, but " +
						"you want to delay the deactivation by 10 seconds, you then set this value to 10.";
			}
		}
	}
}

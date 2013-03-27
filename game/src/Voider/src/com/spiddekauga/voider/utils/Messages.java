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
	 * Messages for level editor
	 */
	public static class Level {
		/** Title of message box when player is asked to select if s/he shall be invulnerable */
		public final static String RUN_INVULNERABLE_TITLE = "Test run the level";
		/** Message when the player is asked to select if s/he shall be invulnerable when testing the level.*/
		public final static String RUN_INVULNERABLE_CONTENT = "Do you want to be invulnerable when " +
				"you're testing the level?\n\n" +
				"Hit Escape or back when you want to stop the test.";
	}

	/**
	 * Tooltip messages
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
						"select other terrains with it." +
						"Usage:" +
						"When no Terrain is selected it will add a new Terrain and a corner." +
						"When a Terrain is selected it add a new corner." +
						"Click/touch on a corner to move it." +
						"Click/touch on another terrain to select it" +
						"Double click on the terrain to \"finish\" and deselect it, " +
						"now you are able to start a new Terrain" +
						"Edges of the same Terrain is not allowed to intersect." +
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
				public final static String REMOVE = "Removes an enemy. If a \"on activate\" trigger is bound to " +
						"this enemy, that trigger will also be removed.";
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

			public static class Path {
				public final static String SELECT = "Selects a path you want to change.";
				public final static String ADD = "Either adds new corners to a path, or " +
						"creates a new path.\n\n" +
						"Usage:\n" +
						"Click/Touch somewhere to continue drawing on the path.\n" +
						"Click/Touch on a corner to move it\n" +
						"Double click on a path to finish it (to be able to start a new path)\n" +
						"Click on another path to select it.";
				public final static String REMOVE = "Click on a path to select it, once a path " +
						"is selected you can click on it to remove it or a corner to just remove " +
						"that corner.";
				public final static String MOVE = "Moves an entire path.";
				public final static String ONCE = "The enemy will follow this path once. When it reaches the end " +
						"it will continue moving in its current direction.";
				public final static String LOOP = "Once the enemy reaches the end it will move directly to the " +
						"beginning of the path follow it again. It will do this forever.";
				public final static String BACK_AND_FORTH = "Once the enemy reaches the end it will follow " +
						"the path backwards to the start, then follow it forward again.";
			}

			public static class Trigger {
				public final static String ADD = "Can either create a new trigger, or move an " +
						"existing one. When clicking on an enemy it will create a \"on activate\" " +
						"trigger, this trigger will be shot when the enemy activates; a click on " +
						"the screen will create a \"on screen\" trigger, this will activate once " +
						"the trigger comes on screen. Note that triggers are not displayed when " +
						"playing the game.";
				public final static String REMOVE = "Click/Touch to remove a trigger.";
				public final static String MOVE =  "Moves an \"on screen\" trigger. \"on activate\" " +
						"triggers are always bound to an enemy and will thus move when moving an enemy.";
			}

			public static class Option {
				public final static String NAME = "Name of the level.";
				public final static String DESCRIPTION = "Write a short description of the level. For others " +
						"to know somewhat what the level is about. Is it hard, many labyrinths?";
				public final static String LEVEL_SPEED = "Initial speed of the level. In the future you will " +
						"be able to change the game-speed in game.";
				public final static String REVISION = "This count is increased with every save. In the future " +
						"you will be able to restore to old revisions; useful when you accidentally broke the " +
						"level and have saved it.";
				public final static String VERSION = "Used when publishing the level. Not available at the moment.";
				public final static String STORY_BEFORE = "This story will be displayed in the loading screen for " +
						"the level. If left empty no story will be displayed.";
				public final static String STORY_AFTER = "This story will be displayed if the player clears the map. " +
						"If the player dies, s/he will never see this story.";
			}
		}
	}
}
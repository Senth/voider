package com.spiddekauga.voider.utils;

import com.spiddekauga.voider.Config;

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
	 * Calculates the amount of time to show a specific message depending
	 * on its length
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
		DUPLICATE("duplicating it"),
		/** Switching to the level editor */
		LEVEL_EDITOR("switching to the level editor"),
		/** Switching to the enemy editor */
		ENEMY_EDITOR("switching to the enemy editor"),
		/** Switching to bullet editor */
		BULLET_EDITOR("switching to the bullet editor"),
		/** Switching to campaign editor */
		CAMPAIGN_EDITOR("switching to the campaign editor"),
		/** Returning to main menu */
		MAIN_MENU("returning to main menu"),

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
	 * Info messages
	 */
	public static class Info {
		/** Text displayed when saving */
		public static String SAVING = "Saving...";
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
		/** Header for successfully completing the level */
		public final static String COMPLETED_HEADER = "Congratulations!";
		/** Header for game over */
		public final static String GAME_OVER_HEADER = "Game Over!";
	}

	/**
	 * Error messages
	 */
	public static class Error {
		/** Polygon complex append */
		public final static String POLYGON_COMPLEX_DRAW_APPEND = "Can't draw a polygon with an intersection!";
		/** Polygon complex add corner */
		public final static String POLYGON_COMPLEX_ADD = "Can't add a corner here, would create an intersection.";
		/** Polygon complex remove corner */
		public final static String POLYGON_COMPLEX_REMOVE = "Can't remove this corner, would create an intersection.";
		/** Polygon complex move corner */
		public final static String POLYGON_COMPLEX_MOVE = "Can't move here, would create an intersection.";
		/** Polygon complex draw/erase */
		public final static String POLYGON_COMPLEX_DRAW_ERASE = "Can't draw a polygon with an intersection!";
		/** Polygon draw/erase line is complex */
		public final static String POLYGON_DRAW_ERASE_LINE_COMPLEX = "Draw line must not intersect itself.";

	}

	/**
	 * Tooltip messages
	 */
	@SuppressWarnings("javadoc")
	public static class Tooltip {
		public static class Actor {
			public static class Visuals {
				public final static String STARTING_ANGLE = "Which direction the " + ACTOR_TYPE + " starts facing " +
						"when created. Not applicable for enemies when the enemy follows a path and uses" +
						"turning, as it will automatically start in right direction then.";
				public final static String ROTATION_SPEED = "If the " + ACTOR_TYPE + " shall rotate. Not applicable " +
						"for enemies it uses turning.";
				public final static String CIRCLE = "Makes the " + ACTOR_TYPE + " in a shape of a circle. (Will reset the shape's center)";
				public final static String RECTANGLE = "Make the " + ACTOR_TYPE + " in a shape of a rectangle. Can " +
						"also be used to make it look like a line. (Will reset the shape's center)";
				public final static String TRIANGLE = "Make the " + ACTOR_TYPE + " in a shape of a triangle. (Will reset the shape's center)";
				public final static String DRAW = "Here you can freely draw your own shape. (Will reset the shape's center)";

			}
			public static class Option {
				public final static String NAME = "Name of the " + ACTOR_TYPE + "";
				public final static String DESCRIPTION = "A short description of your " + ACTOR_TYPE + "";
			}
			public static class Collision {
				public final static String DAMAGE = "How much damage shall be inflicted on the other actor " +
						"this " + ACTOR_TYPE + " collides with, i.e. it works almost like a spike shield. If the " +
						"" + ACTOR_TYPE + " shall be destroyed when it collides it will inflict the damage directly " +
						"otherwise this is the DPS (damage per second) during the collision.";
				public final static String DESTROY_ON_COLLIDE = "Check this if the " + ACTOR_TYPE + " shall die when " +
						"it collides with another object it can collide with. This will make the collision damage be " +
						"dealt directly instead of DPS (damage per second) that it generally uses.";
			}
			public static class Menu {
				public final static String VISUALS = "Change the " + ACTOR_TYPE + "'s shape and rotation";
				public final static String OPTIONS = "Set a name and description to the " + ACTOR_TYPE;
				public final static String COLLISION = "Set collision damage and if the " + ACTOR_TYPE + " shall " +
						"be destroyed when it collides";
			}
		}

		public static class Enemy {
			public static class Menu {
				public final static String MOVEMENT = "Change the enemy's movement";
				public final static String WEAPON = "Enable or disable an enemy weapon and " +
						"set the weapon settings.";
			}
			public static class Movement {
				public static class Menu {
					public final static String PATH = "The enemy follows a path that is " +
							"drawn in the level editor. You decide inside the level editor " +
							"which kind of path the path will be. You can see all path types " +
							"to the left. Hover over their name to get additional description.";
					public final static String STATIONARY = "Makes the enemy stand still, it will " +
							"never move.";
					public final static String AI = "Make the enemy follow some simple rules to create " +
							"movement behaviors of your choice.";
				}
				public static class Path {
					public final static String BACK_AND_FORTH = "This will make the enemy go back " +
							"and forth on the path you have drawn.";
					public final static String LOOP = "Once the enemy reaches the end of the path it " +
							"will move directly to the beginning and starts following it again.";
					public final static String ONCE = "The enemy will only follow the path once. " +
							"When it reaches the end it will continue in its current direction until +" +
							"its out of screen where it gets destroyed";
				}
				public static class Common {
					public final static String MOVEMENT_SPEED = "How fast the enemy will move.";
					public final static String TURNING_SPEED_BUTTON = "When turned on, the enemy " +
							"will move forward and turn in its direction. This creates a more fluid movement. " +
							"When disabled it will always move in a fixed angle.";
					public final static String TURNING_SPEED = "How fast the enemy turns, this is " +
							"independent of the movement (i.e. it will not use a less turn angle with higher speed).\n\n" +
							"Note that a low turning speed can cause enemies to get stuck if a path turns too sharply. " +
							"Be sure to test this inside the level editor once you draw your paths!";
				}
				public static class Ai {
					public final static String DISTANCE_MIN = "If the enemy is closer than this from the player " +
							"it will start moving away from the player. For example set min and max to 0, this will " +
							"create a \"suicide bomber\" :)";
					public final static String DISTANCE_MAX = "If the enemy is further away than this from the player " +
							"it will start moving towards the player. For example set min and max to 0, this will " +
							"create a \"suicide bomber\" :)";
					public final static String RANDOM_MOVEMENT_BUTTON = "When turned on the enemy will start moving " +
							"randomly when inside the distance range (and not just stand still).";
					public final static String RANDOM_MOVEMENT = "The enemy will start to move in another direction " +
							"somewhere between min and max seconds. The new direction could be in any of 360 degrees.";
				}
			}
			public static class Weapon {
				public final static String WEAPON_BUTTON = "Turn on/off the weapons. Note that you need to select " +
						"a bullet type before the enemy will start to shoot.";
				public final static String BULLET = "Bullet and weapon settings.";
				public final static String AIM = "Settings for how the enemy shall aim";

				public static class Bullet {
					public final static String SELECT_BULLET = "Selects a bullet type. I.e. how it looks and behaves.";
					public final static String SPEED = "How fast the bullet will travel";
					public final static String DAMAGE = "How much damage a bullet will do to the player";
					public final static String COOLDOWN = "How long cooldown the weapon has, the enemy will fire " +
							"directly when the weapon gets operational again. When min and max differs, it will randomize " +
							"a cooldown time between min and max.";
				}
				public static class Aim {
					public final static String ON_PLAYER = "This will shot directly at the player's current " +
							"location. If the player stands still s/he will get shot.";
					public final static String MOVE_DIR = "This will make the enemy shoot in its moving direction.";
					public final static String IN_FRONT_OF_PLAYER = "Calculates where the player is heading and shoots " +
							"in that direction. If the player continues to move in the same direction s/he will get hit.";
					public final static String ROTATE = "Shoots in a rotating manner. Can also be used to shoot in one " +
							"direction when rotate speed is set to 0. For example for a path enemy that has a path following " +
							"the top screen it might only want to shoot down (simulating bombs), set the start angle to 270 " +
							"and the rotate speed to 0 to achieve this effect.";
					public final static String ROTATE_START_ANGLE = "Starting angle which it shoots in. This is more or less " +
							"only useful when you want it to shoot in a specific location when rotation speed is set to 0.";
					public final static String ROTATE_SPEED = "How fast it will rotate the shots.";
				}
			}

		}

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
			public static class Pickup {
				public final static String SELECT_NAME = "Pickup name of new pickups that will be added.";
				public final static String SELECT_TYPE = "Select a pickup type, all new pickups will now be" +
						"of this type. The selected pickup name is displayed to the left.";
			}
			public static class Enemy {
				public final static String SELECT_NAME = "Enemy name of new enemies that will be added.";
				public final static String SELECT_TYPE = "Selects an enemy type, all new enemies will now be " +
						"of this type. The selected enemy name is displayed to the left.";
				public final static String ENEMY_COUNT = "Duplicates of the selected enemy.";
				public final static String ENEMY_SPAWN_DELAY = "Delay between when each of the enemy is spawned, " +
						"or rather activated.";
				public final static String ACTIVATE_DELAY = "Delay the activation by X seconds.\nExample of use:\n " +
						"You have one path, but want two different enemies to follow it but not activate exactly at" +
						"the same time. You then set the activation trigger of both enemies to the same value, but" +
						"set this delay in one of the enemies, now the other will start X seconds after the other one.";
				public final static String DEACTIVATE_DELAY = "Delay the deactivation by X seconds.\nExample of use:\n" +
						"You want the AI enemy to only be active for 10 seconds. You can then select its own activation " +
						"trigger (if one has been created). This trigger will be shot once the enemy is activated, but " +
						"you want to delay the deactivation by 10 seconds, you then set this value to 10.";
			}
			public static class Path {
				public final static String ONCE = "The enemy will follow this path once. When it reaches the end " +
						"it will continue moving in its current direction.";
				public final static String LOOP = "Once the enemy reaches the end it will move directly to the " +
						"beginning of the path follow it again. It will do this forever.";
				public final static String BACK_AND_FORTH = "Once the enemy reaches the end it will follow " +
						"the path backwards to the start, then follow it forward again.";
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
				public final static String STORY_AFTER = "This story will be displayed only if the player successfully " +
						"completes the map. If the player dies, s/he will never see this story :(.";
			}
		}

		public static class Menus {
			public static class Main {
				public final static String DOWNLOADED = "Play user levels or campaigns you have " +
						"downloaded via explore.";
				public final static String EXPLORE = "Explore and download user levels, campaigns " +
						"or just new enemies or bullet types for your own levels.";
				public final static String EDITOR = "Create your own levels, campaigns, enemies, " +
						"and bullets.";
			}
		}

		public static class Tools {
			private final static String ABOUT_TRIGGERS = "About triggers:\n" +
					"All enemies have a default activate and deactivate trigger (not displayed " +
					"in editor); these are described below.\n" +
					"All enemies activate just before entering the screen. If multiple enemies are set " +
					"the first enemy will spawn earlier making sure the last enemy spawns off screen. " +
					"Stationary enemies deactivate when they disappear from the screen. Path enemies will " +
					"only disappear when the whole path AND the enemy has disappeared off the screen. " +
					"AI enemies will deactivate " + Config.Actor.Enemy.DEACTIVATE_TIME_DEFAULT +
					" seconds after activation.";

			public final static String SET_CENTER = "Sets the center of the shape. Generally you want it " +
					"in the middle of the shape (use reset center to accomplish this), but sometimes you " +
					"might want to create som cool effects when rotating.";
			public final static String RESET_CENTER = "Resets the center to the middle of the shape.";
			public final static String MOVE = "Move something :) If several objects are selected, you'll move" +
					"all of them";
			public final static String DRAW_ERASE = "Draw or erase parts of your " + ACTOR_TYPE + ". Starting from the inside will " +
					"add parts whenever you pass the shape's border. Starting from the inside will erase parts of your shape " +
					"whenever you pass the shape's border.";
			public final static String DRAW_APPEND = "Create a new " + ACTOR_TYPE + ", or append to an existing shape." +
					"This tool only works on one " + ACTOR_TYPE + " at a time.";
			public final static String ADJUST_ADD_MOVE_CORNER = "Adjust an existing " + ACTOR_TYPE + " by adding a new corner " +
					"between two existing corners, or move an existing corner.";
			public final static String ADJUST_REMOVE_CORNER = "Removes a corner from the " + ACTOR_TYPE + ", or the entire " +
					"shape itself.";
			public final static String DELETE = "Delets all selected objects and all objects that are clicked on.";
			public final static String SELECT = "Selects one or several objects. Holding Shift while using this or another " +
					"tool will add those resources to the selection. Holding Ctrl will instead remove those from" +
					"the selection. Press ESC to deselect all objects.";
			public final static String ENEMY_ADD = "Add enemies to the level." +
					"\n\n" + ABOUT_TRIGGERS;
			public final static String PATH_ADD = "Add paths for enemies that follows a path";
			public final static String SET_ACTIVATE_TRIGGER = "Click/touch to select a trigger to " +
					"be used as an activate trigger for the enemy, i.e. the enemy will activate " +
					"when this trigger is shot. " +
					"If you want to delay the activation by X seconds you can do that below (only shown " +
					"if a trigger has been selected).\n" +
					"NOTE: All enemies have a default trigger, this is for those that want more control when their" +
					"enemies spawn.";
			public final static String SET_DEACTIVATE_DELAY = "Click/touch to select a trigger to be used " +
					"as a deactivate trigger for the enemy, i.e. the enemy will deactivate when this " +
					"trigger is shot. If you you want to delay the deactivation by X seconds you can do that below " +
					"(only shown if a trigger has been selected.\n" +
					"NOTE: All enemies have a default trigger, this is for those that want more control when their" +
					"enemies spawn.";
			public final static String TRIGGER_ADD = "Add a trigger on the screen, these are used for enemies " +
					"\n\n" + ABOUT_TRIGGERS;
			public final static String CANCEL = "Removes current selection";
		}
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

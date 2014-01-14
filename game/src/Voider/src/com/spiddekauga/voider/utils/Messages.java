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
				"Hit Escape or Back when you want to stop the test.";
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
					public final static String DISTANCE_MIN = "Min distance to player";
					public final static String DISTANCE_MAX = "Max distance to player";
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
			public static class Menu {
				public final static String RUN = "Test play the level";
				public final static String OPTION = "Level name, description, speed, story, ...";
			}
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
			}
			public static class Editor {
				public final static String CAMPAIGN = "Campaign editor";
				public final static String LEVEL = "Level editor";
				public final static String ENEMY = "Enemy editor";
				public final static String BULLET = "Bullet editor";
			}
		}

		public static class Tools {
			public final static String SET_CENTER = "Set shape center";
			public final static String RESET_CENTER = "Reset shape center";
			public final static String MOVE = "Move something :)";
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

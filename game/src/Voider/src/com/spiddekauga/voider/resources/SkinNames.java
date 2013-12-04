package com.spiddekauga.voider.resources;



/**
 * All skin names
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class SkinNames {
	/**
	 * General UI elements
	 */
	public enum General {
		/** Default padding for rows and cells */
		PADDING_DEFAULT,
		/** Separator padding */
		PADDING_SEPARATOR,
		/** Left and right window padding */
		PADDING_WINDOW_LEFT_RIGHT,
		/** Default label */
		LABEL_DEFAULT("default"),
		/** Text button default style */
		TEXT_BUTTON_PRESS("default"),
		/** Text button that can toggle */
		TEXT_BUTTON_TOGGLE("toggle"),
		/** Text button that always is selected */
		TEXT_BUTTON_SELECTED("selected"),
		/** Image button default */
		IMAGE_BUTTON_DEFAULT("default"),
		/** Image button toggle */
		IMAGE_BUTTON_TOGGLE("toggle"),
		/** Check box that uses check boxes */
		CHECK_BOX_DEFAULT("default"),
		/** Check box that uses the radio button style */
		CHECK_BOX_RADIO("radio"),
		/** Slider default */
		SLIDER_DEFAULT("default"),
		/** Loading bar slider */
		SLIDER_LOADING_BAR("loading_bar"),
		/** Text field default */
		TEXT_FIELD_DEFAULT("default"),
		/** Window default style without title */
		WINDOW_DEFAULT("default"),
		/** Window no title */
		WINDOW_NO_TITLE("default"),
		/** Window with title */
		WINDOW_TITLE("title"),
		/** Modal window with no title */
		WINDOW_MODAL("modal"),
		/** Modal window with title */
		WINDOW_MODAL_TITLE("modal_title"),
		/** Scroll pane default, no background */
		SCROLL_PANE_DEFAULT("default"),
		/** Scroll pane with background */
		SCROLL_PANE_WINDOW_BACKGROUND("background"),
		/** List default */
		LIST_DEFAULT("default"),
		/** Select box default */
		SELECT_BOX_DEFAULT("default"),

		;

		/**
		 * Creates a more user-friendly name for the enumeration
		 */
		private General() {
			mName = super.toString().toLowerCase();
		}

		/**
		 * Create a custom name for the enumeration
		 * @param jsonName name in the json-file
		 */
		private General(String jsonName) {
			mName = jsonName;
		}

		/**
		 * @return name of the icon inside the skin
		 */
		@Override
		public String toString() {
			return mName;
		}

		/** skin name of the icon */
		private String mName;
	}

	/**
	 * Game names
	 */
	public enum Game {
		/** Health bar for the game */
		HEALTH_BAR,


		;

		/**
		 * Creates a more user-friendly name for the enumeration
		 */
		private Game() {
			mName = super.toString().toLowerCase();
		}

		/**
		 * Create a custom name for the enumeration
		 * @param jsonName name in the json-file
		 */
		private Game(String jsonName) {
			mName = jsonName;
		}

		/**
		 * @return name of the icon inside the skin
		 */
		@Override
		public String toString() {
			return mName;
		}

		/** skin name of the icon */
		private String mName;
	}

	/**
	 * Editor icon names
	 */
	public enum EditorIcons {
		/** Add a corner between two corners in a shape, or move an existing corner */
		ADD_MOVE_CORNER,
		/** The enemy will shoot in front of the player (i.e. where
		 * the player will be if s/he continues to move in the same direction) */
		AIM_IN_FRONT_PLAYER,
		/** The enemy will shoot in its moving direction */
		AIM_MOVEMENT,
		/** The enemy will shoot on the player, or rather
		 * in the direction where the player is currently located */
		AIM_ON_PLAYER,
		/** The enemy will shoot in a circle (or in a straight line) */
		AIM_ROTATE,
		/** Go to the bullet editor */
		BULLET_EDITOR,
		/** When the bullet editor is selected, i.e. we're in the bullet editor */
		BULLET_EDITOR_SELECTED,
		/** Opens a dialog where the player will be able to select which bullet the enemy should use */
		BULLET_SELECT,
		/** Go to the campaign editor */
		CAMPAIGN_EDITOR,
		/** When the campaign editor is selected, i.e. we're in the campaign editor */
		CAMPAIGN_EDITOR_SELECTED,
		/** Deselects all selected actors, maybe does something else? */
		CANCEL,
		/** Shape of actor (enemy or bullet) */
		CIRCLE_SHAPE,
		/** Collision options for enemies */
		COLLISION,
		/** Delete all selected actors */
		DELETE,
		/** Appends to the custom draw shape. Only enemy and bullet */
		DRAW_APPEND,
		/** Setting this allows the player to draw their own enemies and bullets */
		DRAW_CUSTOM_SHAPE,
		/** Add or remove a part to or from the shape */
		DRAW_ERASE,
		/** Duplicates the current actor or level */
		DUPLICATE,
		/** Add an enemy to the level */
		ENEMY_ADD,
		/** Go to the enemy editor */
		ENEMY_EDITOR,
		/** When the enemy editor is selected, i.e. we're in the enemy editor */
		ENEMY_EDITOR_SELECTED,
		/** Opens a dialog where the player will be able to select which enemy to add */
		ENEMY_SELECT,
		/** Set activate trigger for enemies. I.e. binds the enemy to a trigger */
		ENEMY_SET_ACTIVATE_TRIGGER,
		/** Set deactivate trigger for enemies. I.e. binds the enemy to a trigger */
		ENEMY_SET_DEACTIVATE_TRIGGER,
		/** Information or options for the current level or actor */
		INFO,
		/** Go to the level editor */
		LEVEL_EDITOR,
		/** When the level editor is selected, i.e. we're in the level editor */
		LEVEL_EDITOR_SELECTED,
		/** Loads another resource */
		LOAD,
		/** Move the selected actors */
		MOVE,
		/** Movement options for enemies */
		MOVEMENT,
		/** Creates a new resource */
		NEW,
		/** Turn something off (e.g. enemy weapons or turn movement) */
		OFF,
		/** Turns something on (e.g. enemy weapons or turn movement) */
		ON,
		/** Add or continue on a path in the level (can move corners too) */
		PATH_ADD,
		/** How enemies should move in the path. Back and forth means once it reached
		 * the end it will move backwards on the path */
		PATH_BACK_AND_FORTH,
		/** How enemies should move in the path. Loop means once the enemy reaches the end
		 * it will go directly (i.e. not along the path) to the start and go through
		 * the path again and again... */
		PATH_LOOP,
		/** How enemies should move in the path ONCE means just once.
		 * Then it will just continue in a straight line when it reaches the end of the path */
		PATH_ONCE,
		/** Remove corner from a path */
		PATH_REMOVE_CORNER,
		/** Add a pickup to the level */
		PICKUP_ADD,
		/** Opens a dialog where the player will be able to select which pickup to add */
		PICKUP_SELECT,
		/** Shape of actor (enemy or bullet) */
		RECTANGLE_SHAPE,
		/** Redo */
		REDO,
		/** Removes a corner from an actor shape */
		REMOVE_CORNER,
		/** Resets the center of the shape (only visible when actor shape is set to draw) */
		RESET_CENTER,
		/** Test run the current level */
		RUN,
		/** Save the resource */
		SAVE,
		/** Select one or several actors */
		SELECT,
		/** Set the center of the shape (only visible when actor shape is set to draw) */
		SET_CENTER,
		/** As {@link #ADD_MOVE_CORNER} but for terrain */
		TERRAIN_ADD_MOVE_CORNER,
		/** As {@link #DRAW_APPEND} but for terrain */
		TERRAIN_DRAW_APPEND,
		/** As {@link #DRAW_ERASE} but for terrain */
		TERRAIN_DRAW_ERASE,
		/** As {@link #REMOVE_CORNER} but for terrain */
		TERRAIN_REMOVE_CORNER,
		/** Shape of actor (enemy or bullet) */
		TRIANGLE_SHAPE,
		/** Add a trigger to the level */
		TRIGGER_ADD,
		/** Undo */
		UNDO,
		/** All visual options for enemies and bullets */
		VISUALS,
		/** Weapon options for enemies and bullets */
		WEAPON,

		;

		/**
		 * Creates a more user-friendly name for the enumeration
		 */
		private EditorIcons() {
			mName = super.toString().toLowerCase();
		}

		/**
		 * @return name of the icon inside the skin
		 */
		@Override
		public String toString() {
			return mName;
		}

		/** skin name of the icon */
		private String mName;
	}

	/**
	 * Editor tooltips
	 */
	public enum EditorTooltips {
		/** Damage on actor collision */
		ACTOR_COLLISION_DAMAGE,
		/** If the actor should be destroyed when colliding with the player */
		ACTOR_COLLISION_DESTROY_ON_COLLIDE,
		/** The player can draw how s/eh wants the actor shape to be like */
		ACTOR_DRAW,
		/** Change the actor's visual properties */
		ACTOR_MENU_VISUALS,
		/** Rotation speed of the actor */
		ACTOR_ROTATION_SPEED,
		/** Starting angle of the actor */
		ACTOR_STARTING_ANGLE,
		/** Enemy minimum/maximum distance from the player */
		ENEMY_AI_DISTANCE,
		/** Aim to intercept the player */
		ENEMY_AIM_INTERCEPT_PLAYER,
		/** Aim in the enemy's current direction */
		ENEMY_AIM_MOVE_DIR,
		/** Aim on the player's current location */
		ENEMY_AIM_ROTATE,
		/** Change enemy movement properties */
		ENEMY_MENU_MOVEMENT,
		/** Change enemy weapon properties */
		ENEMY_MENU_WEAPON,
		/** The enemy moves according to some AI rules */
		ENEMY_MOVEMENT_AI,
		/** The enemy follows a path */
		ENEMY_MOVEMENT_PATH,
		/** The enemy stands still */
		ENEMY_MOVEMENT_STATIONARY,
		/** Time between each new direction when using random movement */
		ENEMY_RANDOM_MOVEMENT,
		/** Makes the enemy move even when it's between min and max distance
		 * using random movement */
		ENEMY_RANDOM_MOVEMENT_ON_OFF,
		/** When turned on the enemy will turn (i.e. not move sideways) */
		ENEMY_TURNING_ON_OFF,
		/** Turning speed of the enemy */
		ENEMY_TURNING_SPEED,
		/** Weapon cooldown */
		ENEMY_WEAPON_COOLDOWN,
		/** Bullet damage of the weapon */
		ENEMY_WEAPON_DAMAGE,
		/** How fast the weapon direction will rotate around the enemy. */
		ENEMY_WEAPON_ROTATE_SPEED,
		/** Starting angle of the shooting. Most useful when setting rotate speed to 0. */
		ENEMY_WEAPON_ROTATE_START_ANGLE,
		/** Bullet speed */
		ENEMY_WEAPON_SPEED,
		/** Delay before first enemy spawns */
		LEVEL_ENEMY_ACTIVATE_DELAY,
		/** How many enemies will spawn */
		LEVEL_ENEMY_COUNT,
		/** Delay before the first enemy deactivates */
		LEVEL_ENEMY_DEACTIVATE_DELAY,
		/** Delay between spawning enemies */
		LEVEL_ENEMY_SPAWN_DELAY,
		/** Enemy moves back and forth on the path */
		PATH_BACK_AND_FORTH,
		/** Enemy loops in the path */
		PATH_LOOP,
		/** Enemy follows the path once */
		PATH_ONCE,
		/** Either add a corner between two existing corners, or move an existing corner */
		TOOL_ADJUST_ADD_MOVE_CORNER,
		/** Removes a corner */
		TOOL_ADJUST_REMOVE_CORNER,
		/** Removes current selection */
		TOOL_CANCEL,
		/** Deletes all selected objects */
		TOOL_DELETE,
		/** Append to the end of the shape */
		TOOL_DRAW_APPEND,
		/** Either add or erase parts to your shape */
		TOOL_DRAW_ERASE,
		/** Moves anything */
		TOOL_MOVE,
		/** Add paths */
		TOOL_PATH_ADD,
		/** Add pickups */
		TOOL_PICKUP_ADD,
		/** Reset the center of the shape */
		TOOL_RESET_CENTER,
		/** Select objects */
		TOOL_SELECT,
		/** Set the activation trigger for the enemy */
		TOOL_SET_ACTIVATION_TRIGGER,
		/** Set the center of the shape */
		TOOL_SET_CENTER,
		/** Set the deactivation trigger for the enemy */
		TOOL_SET_DEACTIVATION_TRIGGER,
		/** Adds a trigger to the screen, always a screen trigger */
		TOOL_TRIGGER_ADD,

		;

		/**
		 * Creates a more user-friendly name for the enumeration
		 */
		private EditorTooltips() {
			mName = super.toString().toLowerCase();
		}

		/**
		 * @return name of the icon inside the skin
		 */
		@Override
		public String toString() {
			return mName;
		}

		/** skin name of the icon */
		private String mName;
	}
}

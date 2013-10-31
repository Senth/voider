package com.spiddekauga.voider.resources;


/**
 * All skin names
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class SkinNames {
	/**
	 * Editor icon names
	 */
	public enum EditorIcons {
		/** Shape of actor (enemy or bullet) */
		CIRCLE_SHAPE,
		/** Setting this allows the player to draw their own enemies and bullets */
		DRAW_CUSTOM_SHAPE,
		/** Shape of actor (enemy or bullet) */
		RECTANGLE_SHAPE,
		/** Shape of actor (enemy or bullet) */
		TRIANGLE_SHAPE,
		/** Add a corner between two corners in a shape, or move an existing corner */
		ADD_MOVE_CORNER,
		/** Removes a corner from an actor shape */
		REMOVE_CORNER,
		/** Appends to the custom draw shape. Only enemy and bullet */
		DRAW_APPEND,
		/** Add or remove a part to or from the shape */
		DRAW_ERASE,
		/** Go to the bullet editor */
		BULLET_EDITOR,
		/** When the bullet editor is selected, i.e. we're in the bullet editor */
		BULLET_EDITOR_SELECTED,
		/** Go to the campaign editor */
		CAMPAIGN_EDITOR,
		/** When the campaign editor is selected, i.e. we're in the campaign editor */
		CAMPAIGN_EDITOR_SELECTED,
		/** Go to the enemy editor */
		ENEMY_EDITOR,
		/** When the enemy editor is selected, i.e. we're in the enemy editor */
		ENEMY_EDITOR_SELECTED,
		/** Go to the level editor */
		LEVEL_EDITOR,
		/** When the level editor is selected, i.e. we're in the level editor */
		LEVEL_EDITOR_SELECTED,
		/** Duplicates the current actor or level */
		DUPLICATE,
		/** Loads another resource */
		LOAD,
		/** Creates a new resource */
		NEW,
		/** Information or options for the current level or actor */
		INFO,
		/** Test run the current level */
		RUN,
		/** Save the resource */
		SAVE,
		/** Opens a dialog where the player will be able to select which enemy to add */
		ENEMY_SELECT,
		/** Opens a dialog where the palyer will be able to select which bullet the enemy should use */
		BULLET_SELECT,
		/** Opens a dialog where the player will be able to select which pickup to add */
		PICKUP_SELECT,
		/** Deselects all selected actors, maybe does something else? */
		CANCEL,
		/** Delete all selected actors */
		DELETE,
		/** Move the selected actors */
		MOVE,
		/** Add or continue on a path in the level (can move corners too) */
		PATH_ADD,
		/** Add a pickup to the level */
		PICKUP_ADD,
		/** Resets the center of the shape (only visible when actor shape is set to draw) */
		RESET_CENTER,
		/** Set the center of the shape (only visible when actor shape is set to draw) */
		SET_CENTER,
		/** As {@link #ADD_MOVE_CORNER} but for terrain */
		TERRAIN_ADD_MOVE_CORNER,
		/** As {@link #REMOVE_CORNER} but for terrain */
		TERRAIN_REMOVE_CORNER,
		/** As {@link #DRAW_APPEND} but for terrain */
		TERRAIN_DRAW_APPEND,
		/** As {@link #DRAW_ERASE} but for terrain */
		TERRAIN_DRAW_ERASE,
		/** Add a trigger to the level */
		TRIGGER_ADD,
		/** Add an enemy to the level */
		ENEMY_ADD,
		/** Select one or several actors */
		SELECT,
		/** Remove corner from a path */
		PATH_REMOVE_CORNER,
		/** Undo */
		UNDO,
		/** Redo */
		REDO,
		/** Set activate trigger for enemies. I.e. binds the enemy to a trigger */
		ENEMY_SET_ACTIVATE_TRIGGER,
		/** Set deactivate trigger for enemies. I.e. binds the enemy to a trigger */
		ENEMY_SET_DEACTIVATE_TRIGGER,
		/** How enemies should move in the path ONCE means just once.
		 * Then it will just continue in a straight line when it reaches the end of the path */
		PATH_ONCE,
		/** How enemies should move in the path. Back and forth means once it reached
		 * the end it will move backwards on the path */
		PATH_BACK_AND_FORTH,
		/** How enemies should move in the path. Loop means once the enemy reaches the end
		 * it will go directly (i.e. not along the path) to the start and go through
		 * the path again and again... */
		PATH_LOOP,
		/** All visual options for enemies and bullets */
		VISUALS,
		/** Movement options for enemies */
		MOVEMENT,
		/** Weapon options for enemies and bullets */
		WEAPON,
		/** Collision options for enemies */
		COLLISION,
		/** The enemy will shoot on the player, or rather
		 * in the direction where the player is currently located */
		AIM_ON_PLAYER,
		/** The enemy will shoot in a circle (or in a straight line) */
		AIM_ROTATE,
		/** The enemy will shoot in its moving direction */
		AIM_MOVEMENT,
		/** The enemy will shoot in front of the player (i.e. where
		 * the player will be if s/he continues to move in the same direction) */
		AIM_IN_FRONT_PLAYER,
		/** Turns something on (e.g. enemy weapons or turn movement) */
		ON,
		/** Turn something off (e.g. enemy weapons or turn movement) */
		OFF,

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
}

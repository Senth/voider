package com.spiddekauga.voider.editor;

import com.badlogic.gdx.math.Vector2;
import com.spiddekauga.voider.game.actors.ActorShapeTypes;

/**
 * Interface for actor editors. This interface have some common actions for all actor
 * editors. Such as save, load, new, duplicate.
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public interface IActorEditor extends IEditor {
	// ------------- File Menu ----------------

	/**
	 * @return true if the editor has the ability to undo
	 */
	boolean hasUndo();

	/**
	 * undoes the previous command
	 */
	void undo();

	/**
	 * Redoes the undone command
	 */
	void redo();

	// --------------- Visuals ----------------
	/**
	 * Sets the starting angle of the actor
	 * @param angle starting angle of the actor
	 */
	void setStartingAngle(float angle);

	/**
	 * @return starting angle of the actor
	 */
	float getStartingAngle();

	/**
	 * Sets the rotation speed of the actor
	 * @param rotationSpeed the new rotation speed of the actor
	 */
	void setRotationSpeed(float rotationSpeed);

	/**
	 * @return rotation speed of the actor
	 */
	float getRotationSpeed();

	// --------------- Shape ------------------
	/**
	 * Sets the shape of the enemy
	 * @param shapeType new shape type of the actor
	 */
	void setShapeType(ActorShapeTypes shapeType);

	/**
	 * @return current shape type of the actor
	 */
	ActorShapeTypes getShapeType();

	/**
	 * Sets the circle radius of a shape
	 * @param radius new radius of the shape
	 */
	void setShapeRadius(float radius);

	/**
	 * @return current circle radius of the shape
	 */
	float getShapeRadius();

	/**
	 * Sets the width of the shape
	 * @param width new width of the shape
	 */
	void setShapeWidth(float width);

	/**
	 * @return current shape width
	 */
	float getShapeWidth();

	/**
	 * Sets the height of the shape
	 * @param height new height of the shape
	 */
	void setShapeHeight(float height);

	/**
	 * @return current shape height
	 */
	float getShapeHeight();

	/**
	 * Resets the center offset of the actor
	 */
	void resetCenterOffset();

	/**
	 * Sets the center offset of the actor
	 * @param newCenter new center position offset
	 */
	void setCenterOffset(Vector2 newCenter);

	/**
	 * @return center offset of the actor
	 */
	Vector2 getCenterOffset();

	// --------------- Custom Tool ----------------
	/**
	 * Sets the active tool for drawing shapes
	 * @param tool the new tool to select
	 */
	void switchTool(Tools tool);

	/**
	 * @return current active tool for drawing shapes
	 */
	Tools getActiveTool();

	// --------------- Collision -----------------
	/**
	 * Sets colliding damage of the enemy
	 * @param damage how much damage the enemy will inflict on a collision
	 */
	void setCollisionDamage(float damage);

	/**
	 * @return collision damage with the enemy
	 */
	float getCollisionDamage();

	/**
	 * Sets whether this actor shall be destroyed on collision
	 * @param destroyOnCollision set to true to destroy the enemy on collision
	 */
	void setDestroyOnCollide(boolean destroyOnCollision);

	/**
	 * @return true if this enemy shall be destroyed on collision
	 */
	boolean isDestroyedOnCollide();

	/**
	 * All tools used by bullet editor (those in tool menu)
	 */
	enum Tools {
		/** No tool */
		NONE,
		/** Zoom in */
		ZOOM_IN,
		/** Zoom out */
		ZOOM_OUT,
		/** Zoom reset */
		ZOOM_RESET,
		/** Pan */
		PAN,
		/** move */
		MOVE,
		/** Delete */
		DELETE,
		/** Add or move a corner */
		ADD_MOVE_CORNER,
		/** Remove a corner */
		REMOVE_CORNER,
		/** Draw append to the shape */
		DRAW_APPEND,
		/** Draw erase to/from the shape */
		DRAW_ERASE,
		/** Set center */
		CENTER_SET,
		/** Reset center */
		CENTER_RESET,
	}
}

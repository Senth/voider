package com.spiddekauga.voider.editor;

import com.badlogic.gdx.math.Vector2;
import com.spiddekauga.utils.Invoker;
import com.spiddekauga.voider.game.actors.ActorShapeTypes;
import com.spiddekauga.voider.scene.DrawActorTool;

/**
 * Interface for actor editors. This interface have some common
 * actions for all actor editors. Such as save, load, new, duplicate.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public interface IActorEditor {
	/**
	 * @return Invoker for undo/redo
	 */
	Invoker getInvoker();

	// ------------- File Menu ----------------
	/**
	 * Creates a new actor in the current editor
	 */
	void newActor();

	/**
	 * Saves the current actor
	 */
	void saveActor();

	/**
	 * Loads another actor
	 */
	void loadActor();

	/**
	 * Duplicates the current actor
	 */
	void duplicateActor();

	/**
	 * @return true if the bullet is unsaved
	 */
	boolean isUnsaved();

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

	// ------------ Definition -----------------
	/**
	 * @return name of the actor
	 */
	String getName();

	/**
	 * Sets the name of the actor
	 * @param name new name of the actor
	 */
	void setName(String name);

	/**
	 * Sets the description of the actor
	 * @param description description text of the actor
	 */
	void setDescription(String description);

	/**
	 * @return description of the actor
	 */
	String getDescription();

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
	 * Sets the DrawActorTool state
	 * @param state new state of the DrawActorTool
	 */
	void setDrawActorToolState(DrawActorTool.States state);

	/**
	 * @return current state of the draw actor tool
	 */
	DrawActorTool.States getDrawActorToolState();
}

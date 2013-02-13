package com.spiddekauga.voider.editor;

import com.spiddekauga.voider.game.actors.Actor;
import com.spiddekauga.voider.game.actors.ActorShapeTypes;

/**
 * Interface for actor editors. This interface have some common
 * actions for all actor editors. Such as save, load, new, duplicate.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public interface IActorEditor {
	// ------------- DrawActorTool ------------
	/**
	 * Called when an actor is added (not same as #newActor())
	 * @param actor the actor that was created
	 */
	void onActorAdded(Actor actor);

	/**
	 * Called when an actor is removed
	 * @param actor the actor that was removed
	 */
	void onActorRemoved(Actor actor);

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
	 * Sets the starting angle of the enemy
	 * @param angle starting angle of the enemy
	 */
	void setStartingAngle(float angle);

	/**
	 * @return starting angle of the enemy
	 */
	float getStartingAngle();


	// --------------- Shape ------------------
	/**
	 * Sets the shape of the enemy
	 * @param shapeType new shape type of the enemy
	 */
	void setShapeType(ActorShapeTypes shapeType);

	/**
	 * @return current shape type of the enemy
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
}

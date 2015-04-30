package com.spiddekauga.voider.editor;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.physics.box2d.World;
import com.spiddekauga.utils.commands.Command;
import com.spiddekauga.utils.commands.Invoker;
import com.spiddekauga.voider.resources.Def;

/**
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public interface IEditor {
	// ------------- File Menu ----------------
	/**
	 * @return Invoker for undo/redo
	 */
	Invoker getInvoker();

	/**
	 * Creates a new definition in the current editor
	 */
	void newDef();

	/**
	 * Saves the current definition
	 */
	void saveDef();

	/**
	 * Saves the current definition then executes a command
	 * @param command this command will be executed after the resource has been saved
	 */
	void saveDef(Command command);

	/**
	 * Loads another definition
	 */
	void loadDef();

	/**
	 * Tries to duplicate the current definition
	 */
	void duplicateDef();

	/**
	 * Actually duplicates the current definition
	 * @param name new name for the definition
	 * @param description new description for the definition
	 */
	void duplicateDef(String name, String description);

	/**
	 * @return true if the editor is currently drawing
	 */
	boolean isDrawing();

	/**
	 * @return true if the resource is saved
	 */
	boolean isSaved();

	/**
	 * Set the editor in a state of unsaved
	 */
	void setUnsaved();

	/**
	 * @return all non-published dependencies of the current definition
	 */
	ArrayList<Def> getNonPublishedDependencies();

	/**
	 * Try to publish the resource
	 */
	void publishDef();

	/**
	 * @return true if the resource is published
	 */
	boolean isPublished();

	/**
	 * @return true if the resource was just created (i.e. it hasn't been saved at all
	 *         yet)
	 */
	boolean isJustCreated();

	/**
	 * Undo the just created resource, this will show the menu again
	 */
	void undoJustCreated();


	// ------------ Definition --------------
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

	/**
	 * @return definition
	 */
	Def getDef();

	// ------------ Camera/World ----------------
	/**
	 * @return world of the current scene
	 */
	World getWorld();

	/**
	 * @return camera of the current scene
	 */
	OrthographicCamera getCamera();

	// ------------ Grid ----------------
	/**
	 * Turn on or off the grid
	 * @param on true if the grid shall be turned on, false if turned off
	 */
	void setGrid(boolean on);

	/**
	 * @return true if the grid is turned on, false if off.
	 */
	boolean isGridOn();
}

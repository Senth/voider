package com.spiddekauga.voider.editor;

import java.util.ArrayList;

import com.spiddekauga.utils.commands.Command;
import com.spiddekauga.voider.resources.Def;

/**
 * 
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public interface IEditor {
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
	 * Duplicates the current definition
	 */
	void duplicateDef();

	/**
	 * @return true if the editor is currently drawing
	 */
	boolean isDrawing();

	/**
	 * @return true if the resource is saved
	 */
	boolean isSaved();

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
}

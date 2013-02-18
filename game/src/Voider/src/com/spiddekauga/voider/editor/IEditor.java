package com.spiddekauga.voider.editor;

/**
 * Common actions for all editors
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
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
	 * Loads another definition
	 */
	void loadDef();

	/**
	 * Duplicates the current definition
	 */
	void duplicateDef();

	/**
	 * @return true if the current definition in the editor is unsaved
	 */
	boolean isUnsaved();
}

package com.spiddekauga.voider.editor.commands;

import com.spiddekauga.utils.Command;
import com.spiddekauga.voider.editor.IEditor;

/**
 * Discards the old actor and creates a new one with default values
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class CEditorNew extends Command {
	/**
	 * Creates a command that will discard the old actor and create
	 * a new one with default values.
	 * @param editor the editor to create the new actor in
	 */
	public CEditorNew(IEditor editor) {
		mEditor = editor;
	}


	@Override
	public boolean execute() {
		mEditor.newDef();
		return true;
	}

	@Override
	public boolean undo() {
		// Cannot undo new enemy
		return true;
	}

	/** Editor to create a new actor in */
	private IEditor mEditor;
}

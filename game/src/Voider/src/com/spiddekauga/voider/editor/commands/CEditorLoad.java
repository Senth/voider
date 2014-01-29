package com.spiddekauga.voider.editor.commands;

import com.spiddekauga.utils.commands.Command;
import com.spiddekauga.voider.editor.IEditor;

/**
 * Opens up the load actor screen for the current editor
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class CEditorLoad extends Command {
	/**
	 * Creates a load actor command, which opens up a select screen
	 * @param editor the editor to load the actor to
	 */
	public CEditorLoad(IEditor editor) {
		mEditor = editor;
	}

	@Override
	public boolean execute() {
		mEditor.loadDef();
		return true;
	}

	@Override
	public boolean undo() {
		// Cannot undo open loading screen...
		return false;
	}

	/** Editor to invoke the load on */
	IEditor mEditor;
}

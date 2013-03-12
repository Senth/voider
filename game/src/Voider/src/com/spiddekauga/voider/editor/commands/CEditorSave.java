package com.spiddekauga.voider.editor.commands;

import com.spiddekauga.utils.Command;
import com.spiddekauga.voider.editor.IEditor;

/**
 * Saves the current actor
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class CEditorSave extends Command {

	/**
	 * Creates a save command for the current editor
	 * @param editor the active editor which we want to call save on.
	 */
	public CEditorSave(IEditor editor) {
		mEditor = editor;
	}

	@Override
	public boolean execute() {
		mEditor.saveDef();
		return true;
	}

	@Override
	public boolean undo() {
		// Cannot undo a save...
		return false;
	}

	/** Enemy editor to invoke the save on */
	IEditor mEditor;
}

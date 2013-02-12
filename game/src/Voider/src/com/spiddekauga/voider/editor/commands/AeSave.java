package com.spiddekauga.voider.editor.commands;

import com.spiddekauga.utils.Command;
import com.spiddekauga.voider.editor.IActorEditor;

/**
 * Saves the current actor
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class AeSave extends Command {

	/**
	 * Creates a save command for the current editor
	 * @param editor the active editor which we want to call save on.
	 */
	public AeSave(IActorEditor editor) {
		mEditor = editor;
	}

	@Override
	public boolean execute() {
		mEditor.saveActor();
		return true;
	}

	@Override
	public boolean undo() {
		// Cannot undo a save...
		return true;
	}

	/** Enemy editor to invoke the save on */
	IActorEditor mEditor;
}

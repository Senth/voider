package com.spiddekauga.voider.editor.commands;

import com.spiddekauga.utils.Command;
import com.spiddekauga.voider.editor.IActorEditor;

/**
 * Opens up the load actor screen for the current editor
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class AeLoad extends Command {
	/**
	 * Creates a load actor command, which opens up a select screen
	 * @param editor the editor to load the actor to
	 */
	public AeLoad(IActorEditor editor) {
		mEditor = editor;
	}

	@Override
	public boolean execute() {
		mEditor.loadActor();
		return true;
	}

	@Override
	public boolean undo() {
		// Cannot undo open loading screen...
		return true;
	}

	/** Editor to invoke the load on */
	IActorEditor mEditor;
}

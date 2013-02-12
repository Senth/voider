package com.spiddekauga.voider.editor.commands;

import com.spiddekauga.utils.Command;
import com.spiddekauga.voider.editor.IActorEditor;

/**
 * Duplicates the actor in the current editor
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class AeDuplicate extends Command {
	/**
	 * Creates a command which will duplicate the current actor in
	 * the actor editor
	 * @param editor the editor to duplicate the enemy in
	 */
	public AeDuplicate(IActorEditor editor) {
		mEditor = editor;
	}

	@Override
	public boolean execute() {
		mEditor.duplicateActor();
		return true;
	}

	@Override
	public boolean undo() {
		// Cannot undo duplicate
		return true;
	}

	/** Editor to duplicate the enemy in */
	private IActorEditor mEditor;
}

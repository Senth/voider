package com.spiddekauga.voider.editor.commands;

import com.spiddekauga.utils.commands.Command;
import com.spiddekauga.voider.editor.IEditor;

/**
 * Duplicates the actor in the current editor
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class CEditorDuplicate extends Command {
	/**
	 * Creates a command which will duplicate the current actor in
	 * the actor editor
	 * @param editor the editor to duplicate the enemy in
	 */
	public CEditorDuplicate(IEditor editor) {
		mEditor = editor;
	}

	@Override
	public boolean execute() {
		mEditor.saveDef();
		mEditor.duplicateDef();
		return true;
	}

	@Override
	public boolean undo() {
		// Cannot undo duplicate
		return false;
	}

	/** Editor to duplicate the enemy in */
	private IEditor mEditor;
}

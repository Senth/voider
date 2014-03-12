package com.spiddekauga.voider.editor.commands;

import com.spiddekauga.utils.commands.Command;
import com.spiddekauga.voider.editor.IEditor;

/**
 * Saves the current actor
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class CEditorSave extends Command {

	/**
	 * Creates a save command for the current editor
	 * @param editor the active editor which we want to call save on.
	 */
	public CEditorSave(IEditor editor) {
		this(editor, null);
	}

	/**
	 * Creates a save command for the current editor and executes
	 * an optional command after the resource has been saved
	 * @param editor the active editor to call saveDef()
	 * @param executeAfterSaved command to execute after the resource has
	 * been saved.
	 */
	public CEditorSave(IEditor editor, Command executeAfterSaved) {
		mEditor = editor;
		mExecuteAfterSaved = executeAfterSaved;
	}

	@Override
	public boolean execute() {
		mEditor.saveDef(mExecuteAfterSaved);
		return true;
	}

	@Override
	public boolean undo() {
		// Cannot undo a save...
		return false;
	}

	/** Enemy editor to invoke the save on */
	IEditor mEditor;
	/** Optional command to execute after the resource has been saved */
	Command mExecuteAfterSaved;
}

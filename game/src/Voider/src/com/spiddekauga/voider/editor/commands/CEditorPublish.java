package com.spiddekauga.voider.editor.commands;

import com.spiddekauga.utils.commands.Command;
import com.spiddekauga.voider.editor.IEditor;

/**
 * Tries to publish the definition.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class CEditorPublish extends Command {
	/**
	 * Try to publish the definition.
	 * @param editor the editor to call publish in
	 */
	public CEditorPublish(IEditor editor) {
		mEditor = editor;
	}

	@Override
	public boolean execute() {
		mEditor.publishDef();
		return true;
	}

	@Override
	public boolean undo() {
		return false;
	}

	/** Editor to call publish in */
	private IEditor mEditor;
}

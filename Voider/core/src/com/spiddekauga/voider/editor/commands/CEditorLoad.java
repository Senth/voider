package com.spiddekauga.voider.editor.commands;

import com.spiddekauga.voider.editor.IEditor;

/**
 * Opens up the load actor screen for the current editor
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class CEditorLoad extends CEditor<IEditor> {
	/**
	 * Creates a load actor command, which opens up a select screen
	 * @param editor the editor to load the actor to
	 */
	public CEditorLoad(IEditor editor) {
		super(editor);
	}

	@Override
	public boolean execute() {
		mEditor.loadDef();
		return true;
	}
}

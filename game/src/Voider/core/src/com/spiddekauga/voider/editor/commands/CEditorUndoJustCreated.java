package com.spiddekauga.voider.editor.commands;

import com.spiddekauga.voider.editor.IEditor;

/**
 * Undoes a just created resource, i.e. the menu is shown again
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class CEditorUndoJustCreated extends CEditor<IEditor> {
	/**
	 * Undo a just created resource.
	 * @param editor the editor to undo the just created resource
	 */
	public CEditorUndoJustCreated(IEditor editor) {
		super(editor);
	}

	@Override
	public boolean execute() {
		mEditor.undoJustCreated();
		return true;
	}
}

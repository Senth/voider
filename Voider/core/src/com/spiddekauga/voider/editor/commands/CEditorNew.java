package com.spiddekauga.voider.editor.commands;

import com.spiddekauga.voider.editor.IEditor;

/**
 * Discards the old actor and creates a new one with default values
 */
public class CEditorNew extends CEditor<IEditor> {
/**
 * Creates a command that will discard the old actor and create a new one with default values.
 * @param editor the editor to create the new actor in
 */
public CEditorNew(IEditor editor) {
	super(editor);
}


@Override
public boolean execute() {
	mEditor.newDef();
	return true;
}
}

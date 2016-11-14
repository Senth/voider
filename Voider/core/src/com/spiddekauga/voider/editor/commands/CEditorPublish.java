package com.spiddekauga.voider.editor.commands;

import com.spiddekauga.voider.editor.IEditor;

/**
 * Tries to publish the definition.
 */
public class CEditorPublish extends CEditor<IEditor> {
/**
 * Try to publish the definition.
 * @param editor the editor to call publish in
 */
public CEditorPublish(IEditor editor) {
	super(editor);
}

@Override
public boolean execute() {
	mEditor.publishDef();
	return true;
}
}

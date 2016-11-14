package com.spiddekauga.voider.editor.commands;

import com.spiddekauga.voider.editor.IEditor;

/**
 * Duplicates a definition in the current editor
 */
public class CEditorDuplicate extends CEditor<IEditor> {
private String mName = null;
private String mDescription = "";

/**
 * Either a) Open up a dialog where the user can set a new name and description for the new copy; or
 * b) if a name is set through {@link #setName(String)} it will actually duplicate the current
 * definition. The description doesn't have to be set.
 * @param editor the editor to duplicate the definition in
 */
public CEditorDuplicate(IEditor editor) {
	super(editor);
}

/**
 * Duplicates the definition with a new name and description. Note that these variables can be
 * changed by calling {@link #setName(String)} and {@link #setDescription(String)}
 * @param editor the editor to duplicate the definition in
 * @param name new name of the definition
 * @param description new description of the definition
 */
public CEditorDuplicate(IEditor editor, String name, String description) {
	super(editor);
	mName = name;
	mDescription = description;
}

@Override
public boolean execute() {
	if (mName != null) {
		mEditor.duplicateDef(mName, mDescription);
	} else {
		mEditor.duplicateDef();
	}
	return true;
}

/**
 * Set a new name for the duplicate.
 * @param name new name. If not null this will actually cause the command to duplicate the
 * definition. If null it will instead open a dialog where a name and description can be set.
 */
public void setName(String name) {
	mName = name;
}

/**
 * Set a description for the editor
 * @param description new description
 */
public void setDescription(String description) {
	mDescription = description;
}
}

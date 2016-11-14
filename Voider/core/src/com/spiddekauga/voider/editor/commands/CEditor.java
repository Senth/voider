package com.spiddekauga.voider.editor.commands;

import com.spiddekauga.utils.commands.CRun;
import com.spiddekauga.voider.editor.IEditor;

/**
 * Editor command
 * @param <EditorType> type of editor
 */
public abstract class CEditor<EditorType extends IEditor> extends CRun {
/** Editor */
protected EditorType mEditor;

/**
 * @param editor
 */
protected CEditor(EditorType editor) {
	mEditor = editor;
}
}

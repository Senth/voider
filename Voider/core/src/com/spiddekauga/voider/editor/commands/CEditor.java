package com.spiddekauga.voider.editor.commands;

import com.spiddekauga.utils.commands.CRun;
import com.spiddekauga.voider.editor.IEditor;

/**
 * Editor command
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 * @param <EditorType> type of editor
 */
public abstract class CEditor<EditorType extends IEditor> extends CRun {
	/**
	 * @param editor
	 */
	protected CEditor(EditorType editor) {
		mEditor = editor;
	}

	/** Editor */
	protected EditorType mEditor;
}

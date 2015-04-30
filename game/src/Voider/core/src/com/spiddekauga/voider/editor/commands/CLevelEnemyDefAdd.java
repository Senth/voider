package com.spiddekauga.voider.editor.commands;

import java.util.UUID;

import com.spiddekauga.voider.editor.LevelEditor;

/**
 * Adds an enemy definition to the add enemy list
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class CLevelEnemyDefAdd extends CEditor<LevelEditor> {
	/**
	 * Creates a command that will add an enemy to the list
	 * @param id enemy id to add to the list
	 * @param levelEditor level editor to select the enemy in
	 */
	public CLevelEnemyDefAdd(UUID id, LevelEditor levelEditor) {
		super(levelEditor);
		mId = id;
	}

	@Override
	public boolean execute() {
		return mEditor.addEnemyDef(mId);
	}

	@Override
	public boolean undo() {
		return mEditor.removeEnemyDef(mId);
	}

	/** The enemy to add to the list */
	private UUID mId;
}

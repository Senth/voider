package com.spiddekauga.voider.editor.commands;

import java.util.UUID;

import com.spiddekauga.voider.editor.LevelEditor;

/**
 * Removes an enemy definition from the add enemy list
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class CLevelEnemyDefRemove extends CEditor<LevelEditor> {
	/**
	 * Creates a command that will remove an enemy from the list
	 * @param id enemy id to remove from the list
	 * @param levelEditor level editor to select the enemy in
	 */
	public CLevelEnemyDefRemove(UUID id, LevelEditor levelEditor) {
		super(levelEditor);
		mId = id;
	}

	@Override
	public boolean execute() {
		return mEditor.removeEnemyDef(mId);
	}

	@Override
	public boolean undo() {
		return mEditor.addEnemyDef(mId);
	}

	/** The enemy to add to the list */
	private UUID mId;
}

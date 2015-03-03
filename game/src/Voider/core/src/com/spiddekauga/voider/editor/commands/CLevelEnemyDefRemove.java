package com.spiddekauga.voider.editor.commands;

import java.util.UUID;

import com.spiddekauga.utils.commands.Command;
import com.spiddekauga.voider.editor.LevelEditor;

/**
 * Removes an enemy definition from the add enemy list
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class CLevelEnemyDefRemove extends Command {
	/**
	 * Creates a command that will remove an enemy from the list
	 * @param id enemy id to remove from the list
	 * @param levelEditor level editor to select the enemy in
	 */
	public CLevelEnemyDefRemove(UUID id, LevelEditor levelEditor) {
		mId = id;
		mLevelEditor = levelEditor;
	}

	@Override
	public boolean execute() {
		boolean success = mLevelEditor.removeEnemyDef(mId);
		return success;
	}

	@Override
	public boolean undo() {
		boolean success = mLevelEditor.addEnemyDef(mId);
		return success;
	}

	/** The enemy to add to the list */
	private UUID mId;
	/** Level editor to select the enemy in */
	private LevelEditor mLevelEditor;
}

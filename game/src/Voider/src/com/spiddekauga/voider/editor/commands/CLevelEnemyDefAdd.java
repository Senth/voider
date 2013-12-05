package com.spiddekauga.voider.editor.commands;

import java.util.UUID;

import com.spiddekauga.utils.Command;
import com.spiddekauga.voider.editor.LevelEditor;

/**
 * Selects an enemy definition to be used for the level
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class CLevelEnemyDefAdd extends Command {
	/**
	 * Creates a command that will select an enemy in the specified level editor.
	 * @param id enemy id to select
	 * @param levelEditor level editor to select the enemy in
	 */
	public CLevelEnemyDefAdd(UUID id, LevelEditor levelEditor) {
		mId = id;
		mLevelEditor = levelEditor;
	}

	@Override
	public boolean execute() {
		boolean success = mLevelEditor.addEnemyDef(mId);
		return success;
	}

	@Override
	public boolean undo() {
		boolean success = mLevelEditor.removeEnemyDef(mId);
		return success;
	}

	/** The enemy to add to the list */
	private UUID mId;
	/** Level editor to select the enemy in */
	private LevelEditor mLevelEditor;
}

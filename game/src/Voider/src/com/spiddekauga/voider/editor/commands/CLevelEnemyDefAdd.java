package com.spiddekauga.voider.editor.commands;

import java.util.UUID;

import com.spiddekauga.utils.commands.Command;
import com.spiddekauga.voider.editor.LevelEditor;

/**
 * Adds an enemy definition to the add enemy list
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class CLevelEnemyDefAdd extends Command {
	/**
	 * Creates a command that will add an enemy to the list
	 * @param id enemy id to add to the list
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

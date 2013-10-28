package com.spiddekauga.voider.editor.commands;

import java.util.UUID;

import com.spiddekauga.utils.Command;
import com.spiddekauga.voider.editor.LevelEditor;
import com.spiddekauga.voider.game.actors.ActorDef;

/**
 * Selects an enemy definition to be used for the level
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class CLevelEnemyDefSelect extends Command {
	/**
	 * Creates a command that will select an enemy in the specified level editor.
	 * @param id enemy id to select
	 * @param levelEditor level editor to select the enemy in
	 */
	public CLevelEnemyDefSelect(UUID id, LevelEditor levelEditor) {
		mId = id;
		mLevelEditor = levelEditor;
		ActorDef selectedEnemy = levelEditor.getSelectedEnemyDef();
		if (selectedEnemy != null) {
			mIdPrev = selectedEnemy.getId();
			mRevisionPrev = selectedEnemy.getRevision();
		}
	}

	@Override
	public boolean execute() {
		boolean success = mLevelEditor.selectEnemyDef(mId);
		return success;
	}

	@Override
	public boolean undo() {
		boolean success = mLevelEditor.selectEnemyDef(mIdPrev);
		return success;
	}

	/** The enemy to select (on execute) */
	private UUID mId;
	/** Previous enemy id (on undo) */
	private UUID mIdPrev = null;
	/** Previous enemy revision (on undo) */
	private int mRevisionPrev = -1;
	/** Level editor to select the enemy in */
	private LevelEditor mLevelEditor;
}

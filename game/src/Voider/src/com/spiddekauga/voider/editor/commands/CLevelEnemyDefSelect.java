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
	 * @param enemyId id of the enemie to select
	 * @param levelEditor level editor to select the enemy in
	 */
	public CLevelEnemyDefSelect(UUID enemyId, LevelEditor levelEditor) {
		mEnemyId = enemyId;
		mLevelEditor = levelEditor;
		ActorDef selectedEnemy = levelEditor.getSelectedEnemyDef();
		if (selectedEnemy != null) {
			mPrevEnemyId = selectedEnemy.getId();
		}
	}

	@Override
	public boolean execute() {
		boolean success = mLevelEditor.selectEnemyDef(mEnemyId);
		return success;
	}

	@Override
	public boolean undo() {
		boolean success = mLevelEditor.selectEnemyDef(mPrevEnemyId);
		return success;
	}

	/** The enemy to select (on execute) */
	private UUID mEnemyId;
	/** Previous enemy id (on undo) */
	private UUID mPrevEnemyId = null;
	/** Level editor to select the enemy in */
	private LevelEditor mLevelEditor;
}

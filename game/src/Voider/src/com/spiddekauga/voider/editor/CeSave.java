package com.spiddekauga.voider.editor;

import com.spiddekauga.utils.Command;

/**
 * Saves the enemy
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class CeSave extends Command {

	/**
	 * Creates a save command for the current enemy editor
	 * @param enemyEditor the active enemy editor which we want to call
	 * save on.
	 */
	public CeSave(EnemyEditor enemyEditor) {
		mEnemyEditor = enemyEditor;
	}

	@Override
	public boolean execute() {
		mEnemyEditor.saveEnemy();
		return true;
	}

	@Override
	public boolean undo() {
		// Cannot undo a save...
		return true;
	}

	/** Enemy editor to invoke the save on */
	EnemyEditor mEnemyEditor;
}

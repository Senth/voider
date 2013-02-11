package com.spiddekauga.voider.editor;

import com.spiddekauga.utils.Command;

/**
 * Discards the old enemy actor and creates a new one with default values
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class CeNew extends Command {
	/**
	 * Creates a command that will discard the old enemy actor and create
	 * a new one with default values.
	 * @param enemyEditor the editor to create the new actor in
	 */
	public CeNew(EnemyEditor enemyEditor) {
		mEnemyEditor = enemyEditor;
	}


	@Override
	public boolean execute() {
		mEnemyEditor.newEnemy();
		return true;
	}

	@Override
	public boolean undo() {
		// Cannot undo new enemy
		return true;
	}

	/** Enemy editor to create a new enemy in */
	private EnemyEditor mEnemyEditor;
}

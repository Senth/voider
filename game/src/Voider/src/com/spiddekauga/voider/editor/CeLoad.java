package com.spiddekauga.voider.editor;

import com.spiddekauga.utils.Command;

/**
 * Opens up the load enemy screen
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class CeLoad extends Command {
	/**
	 * Creates a load enemy command, which opens up the enemy screen
	 * @param enemyEditor the editor to load the enemy to
	 */
	public CeLoad(EnemyEditor enemyEditor) {
		mEnemyEditor = enemyEditor;
	}

	@Override
	public boolean execute() {
		mEnemyEditor.loadEnemy();
		return true;
	}

	@Override
	public boolean undo() {
		// Cannot undo open loading screen...
		return true;
	}

	/** Enemy editor to invoke the load on */
	EnemyEditor mEnemyEditor;
}

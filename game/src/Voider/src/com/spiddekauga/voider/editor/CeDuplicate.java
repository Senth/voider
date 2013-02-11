package com.spiddekauga.voider.editor;

import com.spiddekauga.utils.Command;

/**
 * Duplicates the current enemy in the enemy editor
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class CeDuplicate extends Command {
	/**
	 * Creates a command which will duplicate the current enemy in
	 * the enemy editor
	 * @param enemyEditor the editor to duplicate the enemy in
	 */
	public CeDuplicate(EnemyEditor enemyEditor) {
		mEnemyEditor = enemyEditor;
	}

	@Override
	public boolean execute() {
		mEnemyEditor.duplicateEnemy();
		return true;
	}

	@Override
	public boolean undo() {
		// Cannot undo duplicate
		return true;
	}

	/** Enemy editor to duplicate the enemy in */
	private EnemyEditor mEnemyEditor;
}

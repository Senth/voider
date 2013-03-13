package com.spiddekauga.voider.editor.commands;

import java.util.UUID;

import com.spiddekauga.utils.Command;
import com.spiddekauga.voider.editor.EnemyEditor;
import com.spiddekauga.voider.game.actors.BulletActorDef;

/**
 * Selects a bullet definition to be used for the enemies
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class CEnemyBulletDefSelect extends Command {
	/**
	 * Creates a command that will select a bullet type for the specified enemy editor
	 * @param bulletId id of the bullet definition to select
	 * @param enemyEditor the enemy editor to select the bullet in
	 */
	public CEnemyBulletDefSelect(UUID bulletId, EnemyEditor enemyEditor) {
		mEnemyEditor = enemyEditor;
		mBulletId = bulletId;
		BulletActorDef selectedBullet = mEnemyEditor.getSelectedBulletDef();
		if (selectedBullet != null) {
			mPrevBulletId = selectedBullet.getId();
		}
	}

	@Override
	public boolean execute() {
		boolean success = mEnemyEditor.selectBulletDef(mBulletId);
		return success;
	}

	@Override
	public boolean undo() {
		boolean success = mEnemyEditor.selectBulletDef(mPrevBulletId);
		return success;
	}

	/** Bullet to select (on execute) */
	private UUID mBulletId;
	/** Previous bullet (on undo) */
	private UUID mPrevBulletId = null;
	/** Enemy edito to select the bullet in */
	private EnemyEditor mEnemyEditor;
}

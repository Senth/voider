package com.spiddekauga.voider.editor.commands;

import java.util.UUID;

import com.spiddekauga.utils.commands.Command;
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
	 * @param id bullet id definition to select
	 * @param enemyEditor the enemy editor to select the bullet in
	 */
	public CEnemyBulletDefSelect(UUID id, EnemyEditor enemyEditor) {
		mEnemyEditor = enemyEditor;
		mId = id;
		BulletActorDef selectedBullet = mEnemyEditor.getSelectedBulletDef();
		if (selectedBullet != null) {
			mIdPrev = selectedBullet.getId();
		}
	}

	@Override
	public boolean execute() {
		boolean success = mEnemyEditor.selectBulletDef(mId);
		return success;
	}

	@Override
	public boolean undo() {
		boolean success = mEnemyEditor.selectBulletDef(mIdPrev);
		return success;
	}

	/** Bullet to select (on execute) */
	private UUID mId;
	/** Previous bullet (on undo) */
	private UUID mIdPrev = null;
	/** Enemy edito to select the bullet in */
	private EnemyEditor mEnemyEditor;
}

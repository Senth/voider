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
	 * @param id bullet id definition to select
	 * @param revision bullet revision
	 * @param enemyEditor the enemy editor to select the bullet in
	 */
	public CEnemyBulletDefSelect(UUID id, int revision, EnemyEditor enemyEditor) {
		mEnemyEditor = enemyEditor;
		mId = id;
		mRevision = revision;
		BulletActorDef selectedBullet = mEnemyEditor.getSelectedBulletDef();
		if (selectedBullet != null) {
			mIdPrev = selectedBullet.getId();
			mRevisionPrev = selectedBullet.getRevision();
		}
	}

	@Override
	public boolean execute() {
		boolean success = mEnemyEditor.selectBulletDef(mId, mRevision);
		return success;
	}

	@Override
	public boolean undo() {
		boolean success = mEnemyEditor.selectBulletDef(mIdPrev, mRevisionPrev);
		return success;
	}

	/** Bullet to select (on execute) */
	private UUID mId;
	/** Previous bullet (on undo) */
	private UUID mIdPrev = null;
	/** Bullet revision */
	private int mRevision;
	/** Previous bullet revision (on undo) */
	private int mRevisionPrev = -1;
	/** Enemy edito to select the bullet in */
	private EnemyEditor mEnemyEditor;
}

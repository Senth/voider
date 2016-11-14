package com.spiddekauga.voider.editor.commands;

import com.spiddekauga.voider.editor.EnemyEditor;
import com.spiddekauga.voider.game.actors.BulletActorDef;

import java.util.UUID;

/**
 * Selects a bullet definition to be used for the enemies
 */
public class CEnemyBulletDefSelect extends CEditor<EnemyEditor> {
/** Bullet to select (on execute) */
private UUID mId;
/** Previous bullet (on undo) */
private UUID mIdPrev = null;

/**
 * Creates a command that will select a bullet type for the specified enemy editor
 * @param id bullet id definition to select
 * @param enemyEditor the enemy editor to select the bullet in
 */
public CEnemyBulletDefSelect(UUID id, EnemyEditor enemyEditor) {
	super(enemyEditor);
	mId = id;
	BulletActorDef selectedBullet = mEditor.getSelectedBulletDef();
	if (selectedBullet != null) {
		mIdPrev = selectedBullet.getId();
	}
}

@Override
public boolean execute() {
	boolean success = mEditor.selectBulletDef(mId);
	return success;
}

@Override
public boolean undo() {
	boolean success = mEditor.selectBulletDef(mIdPrev);
	return success;
}
}

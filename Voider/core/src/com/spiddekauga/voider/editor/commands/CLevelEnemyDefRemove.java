package com.spiddekauga.voider.editor.commands;

import com.spiddekauga.voider.editor.LevelEditor;

import java.util.UUID;

/**
 * Removes an enemy definition from the add enemy list
 */
public class CLevelEnemyDefRemove extends CEditor<LevelEditor> {
/** The enemy to add to the list */
private UUID mId;

/**
 * Creates a command that will remove an enemy from the list
 * @param id enemy id to remove from the list
 * @param levelEditor level editor to select the enemy in
 */
public CLevelEnemyDefRemove(UUID id, LevelEditor levelEditor) {
	super(levelEditor);
	mId = id;
}

@Override
public boolean execute() {
	return mEditor.removeEnemyDef(mId);
}

@Override
public boolean undo() {
	return mEditor.addEnemyDef(mId);
}
}

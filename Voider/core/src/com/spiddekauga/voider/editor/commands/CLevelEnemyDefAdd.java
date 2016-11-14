package com.spiddekauga.voider.editor.commands;

import com.spiddekauga.voider.editor.LevelEditor;

import java.util.UUID;

/**
 * Adds an enemy definition to the add enemy list
 */
public class CLevelEnemyDefAdd extends CEditor<LevelEditor> {
/** The enemy to add to the list */
private UUID mId;

/**
 * Creates a command that will add an enemy to the list
 * @param id enemy id to add to the list
 * @param levelEditor level editor to select the enemy in
 */
public CLevelEnemyDefAdd(UUID id, LevelEditor levelEditor) {
	super(levelEditor);
	mId = id;
}

@Override
public boolean execute() {
	return mEditor.addEnemyDef(mId);
}

@Override
public boolean undo() {
	return mEditor.removeEnemyDef(mId);
}
}

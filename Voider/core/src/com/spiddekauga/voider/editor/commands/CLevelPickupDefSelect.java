package com.spiddekauga.voider.editor.commands;

import com.spiddekauga.voider.editor.LevelEditor;
import com.spiddekauga.voider.game.actors.ActorDef;

import java.util.UUID;

/**
 * Selects a pickup definition to be used for the level
 */
public class CLevelPickupDefSelect extends CEditor<LevelEditor> {
/** The pickup to select (on execute) */
private UUID mPickupId;
/** Previous pickup id (on undo) */
private UUID mPrevPickupId = null;

/**
 * Creates a command that will select a pickup in the specified level editor.
 * @param pickupId id of the pickup to select
 * @param levelEditor the level editor to select the pickup in
 */
public CLevelPickupDefSelect(UUID pickupId, LevelEditor levelEditor) {
	super(levelEditor);
	mPickupId = pickupId;
	ActorDef selectedPickup = levelEditor.getSelectedPickupDef();
	if (selectedPickup != null) {
		mPrevPickupId = selectedPickup.getId();
	}
}

@Override
public boolean execute() {
	return mEditor.selectPickupDef(mPickupId);
}

@Override
public boolean undo() {
	return mEditor.selectPickupDef(mPrevPickupId);
}
}

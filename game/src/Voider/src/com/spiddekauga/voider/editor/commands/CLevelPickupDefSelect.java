package com.spiddekauga.voider.editor.commands;

import java.util.UUID;

import com.spiddekauga.utils.commands.Command;
import com.spiddekauga.voider.editor.LevelEditor;
import com.spiddekauga.voider.game.actors.ActorDef;

/**
 * Selects a pickup definition to be used for the level
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class CLevelPickupDefSelect extends Command {
	/**
	 * Creates a command that will select a pickup in the specified
	 * level editor.
	 * @param pickupId id of the pickup to select
	 * @param levelEditor the level editor to select the pickup in
	 */
	public CLevelPickupDefSelect(UUID pickupId, LevelEditor levelEditor) {
		mPickupId = pickupId;
		mLevelEditor = levelEditor;
		ActorDef selectedPickup = levelEditor.getSelectedPickupDef();
		if (selectedPickup != null) {
			mPrevPickupId = selectedPickup.getId();
		}
	}

	@Override
	public boolean execute() {
		boolean success = mLevelEditor.selectPickupDef(mPickupId);
		return success;
	}

	@Override
	public boolean undo() {
		boolean success = mLevelEditor.selectPickupDef(mPrevPickupId);
		return success;
	}

	/** The pickup to select (on execute) */
	private UUID mPickupId;
	/** Previous pickup id (on undo) */
	private UUID mPrevPickupId = null;
	/** Level editor to select the pickup in */
	private LevelEditor mLevelEditor;
}

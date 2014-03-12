package com.spiddekauga.voider.editor.commands;

import com.spiddekauga.utils.commands.Command;
import com.spiddekauga.voider.editor.LevelEditor;

/**
 * Tests to run a level
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class CLevelRun extends Command {
	/**
	 * Creates a command that will test run the current level.
	 * @param invulnerable set to true to make the player invulnerable when testing
	 * @param levelEditor the editor to run the command on
	 */
	public CLevelRun(boolean invulnerable, LevelEditor levelEditor) {
		mLevelEditor = levelEditor;
		mInvulnerable = invulnerable;
	}

	@Override
	public boolean execute() {
		mLevelEditor.runFromHere(mInvulnerable);
		return true;
	}

	@Override
	public boolean undo() {
		// Cannot undo this command
		return false;
	}

	/** Level editor to run the command on */
	private LevelEditor mLevelEditor;
	/** If the player shall be invulnerable or not */
	private boolean mInvulnerable;
}

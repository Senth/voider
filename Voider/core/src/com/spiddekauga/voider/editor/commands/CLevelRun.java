package com.spiddekauga.voider.editor.commands;

import com.spiddekauga.voider.editor.LevelEditor;

/**
 * Tests to run a level
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class CLevelRun extends CEditor<LevelEditor> {
	/**
	 * Creates a command that will test run the current level.
	 * @param invulnerable set to true to make the player invulnerable when testing
	 * @param runFromStart true if we should run from from the start, false to run from
	 *        the current position
	 * @param levelEditor the editor to run the command on
	 */
	public CLevelRun(boolean invulnerable, boolean runFromStart, LevelEditor levelEditor) {
		super(levelEditor);
		mInvulnerable = invulnerable;
		mRunFromStart = runFromStart;
	}

	@Override
	public boolean execute() {
		if (mRunFromStart) {
			mEditor.runFromStart(mInvulnerable);
		} else {
			mEditor.runFromHere(mInvulnerable);
		}
		return true;
	}

	/** If the player shall be invulnerable or not */
	private boolean mInvulnerable;
	/** True to run from start, false to run from current position */
	private boolean mRunFromStart;
}

package com.spiddekauga.utils.commands;

import com.badlogic.gdx.utils.Disposable;

/**
 * Common interface for commands that can be passed as argument and
 * executed later.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public abstract class Command implements Disposable {
	/**
	 * Executes the command
	 * @return true if the command was successfully executed
	 */
	public abstract boolean execute();

	/**
	 * Executes the undo command, i.e. it reverses the effect of the execute command
	 * @return true if the command was successfully undone
	 */
	public abstract boolean undo();

	@Override
	public void dispose() {
		// Does nothing
	}

	/**
	 * Sets the command as chained
	 */
	void setAsChanied() {
		mChained = true;
	}

	/**
	 * @return true if this command is chained
	 */
	boolean isChained() {
		return mChained;
	}

	/** True if the command shall be chanied, just as SequenceCommand */
	private boolean mChained = false;
}

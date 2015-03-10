package com.spiddekauga.utils.commands;

/**
 * Custom runnable command. By default it's not undoable
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public abstract class CRun extends Command {
	@Override
	public boolean undo() {
		return false;
	}
}

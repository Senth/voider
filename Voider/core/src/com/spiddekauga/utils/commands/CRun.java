package com.spiddekauga.utils.commands;

/**
 * Custom runnable command. By default it's not undoable
 */
public abstract class CRun extends Command {
@Override
public boolean undo() {
	return false;
}
}

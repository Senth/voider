package com.spiddekauga.voider.utils.commands;

import com.spiddekauga.utils.commands.Command;
import com.spiddekauga.voider.utils.Synchronizer;

/**
 * Fix conflicts
 */
public class CSyncFixConflict extends Command {
private boolean mKeepLocal;

/**
 * Sets if we should keep local or server resource versions
 * @param keepLocal true if we want to keep local versions, false if we should keep server gameVersion
 */
public CSyncFixConflict(boolean keepLocal) {
	mKeepLocal = keepLocal;
}

@Override
public boolean execute() {
	Synchronizer.getInstance().fixConflict(mKeepLocal);
	return true;
}

@Override
public boolean undo() {
	// Does nothing
	return false;
}
}

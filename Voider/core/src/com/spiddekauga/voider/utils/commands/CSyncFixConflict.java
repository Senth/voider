package com.spiddekauga.voider.utils.commands;

import com.spiddekauga.utils.commands.Command;
import com.spiddekauga.voider.utils.Synchronizer;

/**
 * Fix conflicts
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class CSyncFixConflict extends Command {
	/**
	 * Sets if we should keep local or server resource versions
	 * @param keepLocal true if we want to keep local versions, false if we should keep
	 *        server version
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

	private boolean mKeepLocal;
}

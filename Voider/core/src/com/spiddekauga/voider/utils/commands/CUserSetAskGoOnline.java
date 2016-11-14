package com.spiddekauga.voider.utils.commands;

import com.spiddekauga.utils.commands.Command;
import com.spiddekauga.voider.repo.user.User;

/**
 * Sets if the user should be asked to go online or not this session
 */
public class CUserSetAskGoOnline extends Command {
private boolean mAsk;

/**
 * Creates a command that sets if the user should be asked in the future to connect to the server if
 * in offline mode.
 * @param ask true if should ask
 */
public CUserSetAskGoOnline(boolean ask) {
	mAsk = ask;
}

@Override
public boolean execute() {
	User.getGlobalUser().setAskToGoOnline(mAsk);
	return true;
}

@Override
public boolean undo() {
	return false;
}
}

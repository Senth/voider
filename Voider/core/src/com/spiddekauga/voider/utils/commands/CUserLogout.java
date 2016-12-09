package com.spiddekauga.voider.utils.commands;

import com.spiddekauga.utils.commands.Command;
import com.spiddekauga.voider.repo.user.User;

/**
 * Logs out the user and returns to the login screen.
 */
public class CUserLogout extends Command {
@Override
public boolean execute() {
	User user = User.getGlobalUser();
	user.logoutAndGotoLogin();
	return true;
}

@Override
public boolean undo() {
	// Cannot be undone
	return false;
}
}

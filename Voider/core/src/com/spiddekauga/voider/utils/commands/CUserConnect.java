package com.spiddekauga.voider.utils.commands;

import com.spiddekauga.utils.commands.Command;
import com.spiddekauga.voider.repo.user.User;

/**
 * Connect the current user to the internet
 */
public class CUserConnect extends Command {
@Override
public boolean execute() {
	User user = User.getGlobalUser();

	// Connect if offline
	if (user.isLoggedIn() && !user.isOnline()) {
		user.login();
	}

	return true;
}

@Override
public boolean undo() {
	// Cannot be undone
	return false;
}
}

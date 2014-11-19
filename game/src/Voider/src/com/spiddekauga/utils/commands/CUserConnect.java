package com.spiddekauga.utils.commands;

import com.spiddekauga.voider.utils.User;

/**
 * Connect the current user to the internet
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
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

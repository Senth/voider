package com.spiddekauga.voider.utils.commands;

import com.spiddekauga.utils.commands.Command;
import com.spiddekauga.voider.utils.User;

/**
 * Logs out the user and returns to the login screen.
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class CUserLogout extends Command {
	@Override
	public boolean execute() {
		User user = User.getGlobalUser();
		user.logout();
		return true;
	}

	@Override
	public boolean undo() {
		// Cannot be undone
		return false;
	}
}

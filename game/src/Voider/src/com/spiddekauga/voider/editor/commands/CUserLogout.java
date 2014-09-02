package com.spiddekauga.voider.editor.commands;

import com.spiddekauga.utils.commands.Command;
import com.spiddekauga.voider.menu.LoginScene;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.IMethodEntity;
import com.spiddekauga.voider.network.entities.user.LogoutMethodResponse;
import com.spiddekauga.voider.repo.IResponseListener;
import com.spiddekauga.voider.repo.user.UserLocalRepo;
import com.spiddekauga.voider.repo.user.UserWebRepo;
import com.spiddekauga.voider.scene.SceneSwitcher;
import com.spiddekauga.voider.utils.User;

/**
 * Logs out the user and returns to the login screen.
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class CUserLogout extends Command implements IResponseListener {
	@Override
	public boolean execute() {
		User user = User.getGlobalUser();
		// Online
		if (user.isLoggedIn()) {
			UserWebRepo.getInstance().logout(this);
		}
		// Offline
		else {
			clearCurrentUser();
		}
		user.logout();
		return true;
	}

	@Override
	public boolean undo() {
		// Cannot be undone
		return false;
	}

	@Override
	public void handleWebResponse(IMethodEntity method, IEntity response) {
		if (response instanceof LogoutMethodResponse) {
			clearCurrentUser();
		}
	}

	/**
	 * Removes saved variables for the current user
	 */
	private void clearCurrentUser() {
		UserLocalRepo.removeLastUser();
		SceneSwitcher.dispose();
		SceneSwitcher.switchTo(new LoginScene());
	}
}

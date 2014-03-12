package com.spiddekauga.voider.menu;

import com.spiddekauga.utils.commands.Command;

/**
 * Tries to create an offline user using a LoginScene
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
class CCreateOfflineUser extends Command {
	/**
	 * Creates a command that tries to create an offline user via a LoginScene
	 * @param loginScene the login scene to call
	 * @param username
	 * @param password
	 * @param email
	 */
	CCreateOfflineUser(LoginScene loginScene, String username, String password, String email) {
		mLoginScene = loginScene;
		mUsername = username;
		mPassword = password;
		mEmail = email;
	}

	@Override
	public boolean execute() {
		mLoginScene.createOfflineUser(mUsername, mPassword, mEmail);
		return true;
	}

	@Override
	public boolean undo() {
		// Cannot undo
		return false;
	}

	/** Username */
	private String mUsername;
	/** Password */
	private String mPassword;
	/** Email */
	private String mEmail;
	/** GUI to send error messages to */
	private LoginScene mLoginScene;
}

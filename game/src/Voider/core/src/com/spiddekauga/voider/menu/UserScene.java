package com.spiddekauga.voider.menu;


/**
 * Scene for user/player settings
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class UserScene extends MenuScene {
	/**
	 * Creates a user scene
	 */
	public UserScene() {
		super(new UserGui());

		getGui().setScene(this);
	}

	/**
	 * Change password
	 * @param oldPassword old password
	 * @param newPassword new password
	 */
	void setPassword(String oldPassword, String newPassword) {
		// TODO
	}

	@Override
	protected UserGui getGui() {
		return (UserGui) super.getGui();
	}
}

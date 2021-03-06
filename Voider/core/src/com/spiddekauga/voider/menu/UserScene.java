package com.spiddekauga.voider.menu;

import com.spiddekauga.voider.repo.user.User;
import com.spiddekauga.voider.utils.event.EventDispatcher;
import com.spiddekauga.voider.utils.event.EventTypes;
import com.spiddekauga.voider.utils.event.GameEvent;
import com.spiddekauga.voider.utils.event.IEventListener;


/**
 * Scene for user/player settings
 */
public class UserScene extends MenuScene {
private IEventListener mPasswordChangeListener = new IEventListener() {
	@Override
	public void handleEvent(GameEvent event) {
		switch (event.type) {
		case USER_PASSWORD_CHANGE_MISMATCH:
			getGui().setOldPasswordErrorText("wrong password");
			break;

		case USER_PASSWORD_CHANGE_TOO_SHORT:
			getGui().setNewPasswordErrorText("too short");
			break;

		case USER_PASSWORD_CHANGED:
			getGui().clearErrors();
			getGui().clearPasswordFields();
			break;

		default:
			break;
		}
	}
};

/**
 * Creates a user scene
 */
public UserScene() {
	super(new UserGui());

	getGui().setScene(this);
}

@Override
protected void onResume(Outcomes outcome, Object message, Outcomes loadingOutcome) {
	super.onResume(outcome, message, loadingOutcome);

	EventDispatcher eventDispatcher = EventDispatcher.getInstance();
	eventDispatcher.connect(EventTypes.USER_PASSWORD_CHANGE_MISMATCH, mPasswordChangeListener);
	eventDispatcher.connect(EventTypes.USER_PASSWORD_CHANGE_TOO_SHORT, mPasswordChangeListener);
	eventDispatcher.connect(EventTypes.USER_PASSWORD_CHANGED, mPasswordChangeListener);
}

@Override
protected void onPause() {
	EventDispatcher eventDispatcher = EventDispatcher.getInstance();
	eventDispatcher.disconnect(EventTypes.USER_PASSWORD_CHANGE_MISMATCH, mPasswordChangeListener);
	eventDispatcher.disconnect(EventTypes.USER_PASSWORD_CHANGE_TOO_SHORT, mPasswordChangeListener);
	eventDispatcher.disconnect(EventTypes.USER_PASSWORD_CHANGED, mPasswordChangeListener);

	super.onPause();
}

@Override
protected UserGui getGui() {
	return (UserGui) super.getGui();
}

/**
 * Change password
 * @param oldPassword old password
 * @param newPassword new password
 */
void setPassword(String oldPassword, String newPassword) {
	User.getGlobalUser().changePassword(oldPassword, newPassword);
}
}

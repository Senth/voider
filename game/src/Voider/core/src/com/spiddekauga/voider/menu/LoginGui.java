package com.spiddekauga.voider.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.utils.Disposable;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.utils.scene.ui.AlignTable;
import com.spiddekauga.utils.scene.ui.ButtonListener;
import com.spiddekauga.utils.scene.ui.HideManual;
import com.spiddekauga.utils.scene.ui.MsgBoxExecuter;
import com.spiddekauga.utils.scene.ui.NotificationShower.NotificationTypes;
import com.spiddekauga.utils.scene.ui.TextFieldListener;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.Config.Debug.Builds;
import com.spiddekauga.voider.repo.resource.SkinNames;
import com.spiddekauga.voider.scene.Gui;
import com.spiddekauga.voider.scene.ui.UiStyles.TextButtonStyles;
import com.spiddekauga.voider.utils.event.UpdateEvent;

/**
 * GUI for login
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class LoginGui extends Gui {
	/**
	 * Sets the login scene
	 * @param loginScene the login scene
	 */
	void setLoginScene(LoginScene loginScene) {
		mLoginScene = loginScene;
	}


	@Override
	public void initGui() {
		super.initGui();

		mWidgets.login.table.setAlignTable(Horizontal.CENTER, Vertical.MIDDLE);
		mWidgets.login.table.setAlignRow(Horizontal.LEFT, Vertical.MIDDLE);
		mWidgets.register.table.setAlignTable(Horizontal.CENTER, Vertical.MIDDLE);
		mWidgets.register.table.setAlignRow(Horizontal.LEFT, Vertical.MIDDLE);
		mWidgets.send.table.setAlignTable(Horizontal.CENTER, Vertical.MIDDLE);
		mWidgets.send.table.setAlignRow(Horizontal.LEFT, Vertical.MIDDLE);
		mWidgets.reset.table.setAlignTable(Horizontal.CENTER, Vertical.MIDDLE);
		mWidgets.reset.table.setAlignRow(Horizontal.LEFT, Vertical.MIDDLE);

		initLoginTable();
		initPasswordResetNew();
		initPasswordResetSendToken();
		mWidgets.reset.hider.hide();
		mWidgets.send.hider.hide();

		initRegisterTable();
		mWidgets.register.hider.hide();

		setBackground(SkinNames.GeneralImages.BACKGROUND_SPACE, true);
	}

	@Override
	public void dispose() {
		super.dispose();
		mWidgets.dispose();
	}

	/**
	 * Focus username field
	 */
	void focusUsernameField() {
		getStage().setKeyboardFocus(mWidgets.login.username);
		mWidgets.login.username.selectAll();
	}

	/**
	 * Initializes the login table
	 */
	private void initLoginTable() {
		AlignTable table = mWidgets.login.table;

		// Username
		mWidgets.login.usernameListener = new TextFieldListener() {
			@Override
			protected void onEnter(String newText) {
				if (mWidgets.login.passwordListener.isTextFieldEmpty()) {
					getStage().setKeyboardFocus(mWidgets.login.password);
				} else {
					login();
				}
			}
		};
		mWidgets.login.username = mUiFactory.addTextField(null, false, "Username", mWidgets.login.usernameListener, table, null);

		// Password
		mWidgets.login.passwordListener = new TextFieldListener() {
			@Override
			protected void onEnter(String newText) {
				if (mWidgets.login.usernameListener.isTextFieldEmpty()) {
					getStage().setKeyboardFocus(mWidgets.login.username);
				} else {
					login();
				}
			}
		};
		mWidgets.login.password = mUiFactory.addPasswordField(null, false, "Password", mWidgets.login.passwordListener, table, null);
		mWidgets.login.password.setName("password");

		// Forgot password
		table.row().setAlign(Vertical.BOTTOM);
		ButtonListener buttonListener = new ButtonListener() {
			@Override
			protected void onPressed(Button button) {
				mWidgets.login.hider.hide();
				mWidgets.send.hider.show();
			}
		};
		mUiFactory.button.addText("Forgot Password", TextButtonStyles.TRANSPARENT_PRESS, table, buttonListener, null, null);

		// Register
		table.row();
		buttonListener = new ButtonListener() {
			@Override
			protected void onPressed(Button button) {
				mWidgets.login.hider.hide();
				mWidgets.register.hider.show();
			}
		};
		mUiFactory.button.addText("Register", TextButtonStyles.TRANSPARENT_PRESS, table, buttonListener, null, null);


		// Set fixed width
		table.layout();
		table.setKeepWidth(true);


		// Buttons
		table.row().setFillWidth(true).setEqualCellSize(true);

		// Exit game
		buttonListener = new ButtonListener() {
			@Override
			protected void onPressed(Button button) {
				Gdx.app.exit();
			}
		};
		mUiFactory.button.addText("Exit", TextButtonStyles.FILLED_PRESS, table, buttonListener, null, null);
		table.getCell().setFixedWidth(false);

		// Login
		buttonListener = new ButtonListener() {
			@Override
			protected void onPressed(Button button) {
				login();
			}
		};
		mUiFactory.button.addText("Login", TextButtonStyles.FILLED_PRESS, table, buttonListener, null, null);
		table.getCell().setFixedWidth(false);
		mUiFactory.button.addPadding(table.getRow());
		mWidgets.login.table.layout();

		// Add to stage
		addActor(mWidgets.login.table);
	}

	/**
	 * Logins in the the current username and password fields
	 */
	private void login() {
		if (isLoginFieldsValid(true)) {
			mLoginScene.login(mWidgets.login.username.getText(), mWidgets.login.password.getText());
		}
	}

	/**
	 * @param showErrors true if you want to display the error messages
	 * @return true if all login fields are filled in
	 */
	private boolean isLoginFieldsValid(boolean showErrors) {
		String errorMessage = null;

		if (mWidgets.login.usernameListener.isTextFieldEmpty()) {
			errorMessage = "Username is empty";
		}
		// Password
		else if (mWidgets.login.passwordListener.isTextFieldEmpty()) {
			errorMessage = "Password is empty";
		}

		if (errorMessage != null) {
			if (showErrors) {
				mNotification.show(NotificationTypes.ERROR, errorMessage);
			}
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Empty register error fields
	 */
	void clearRegisterErrors() {
		mWidgets.register.usernameError.setText("");
		mWidgets.register.passwordError.setText("");
		mWidgets.register.emailError.setText("");
		if (mWidgets.register.keyError != null) {
			mWidgets.register.keyError.setText("");
		}
		mWidgets.register.acceptTermsError.setText("");
	}

	/**
	 * Set register username error
	 * @param text error text
	 */
	void setRegisterUsernameError(String text) {
		mWidgets.register.usernameError.setText(text);
	}

	/**
	 * Set register password error
	 * @param text error text
	 */
	void setRegisterPasswordError(String text) {
		mWidgets.register.passwordError.setText(text);
	}

	/**
	 * Set register email error
	 * @param text error text
	 */
	void setRegisterEmailError(String text) {
		mWidgets.register.emailError.setText(text);
	}

	/**
	 * Set register accept terms error
	 * @param text error text
	 */
	void setRegisterAcceptTermsError(String text) {
		mWidgets.register.acceptTermsError.setText(text);
	}

	/**
	 * Set register key error
	 * @param text error text
	 */
	void setRegisterKeyError(String text) {
		if (mWidgets.register.keyError != null) {
			mWidgets.register.keyError.setText(text);
		}
	}

	/**
	 * Try to register with the specified fields
	 */
	private void register() {
		clearRegisterErrors();
		if (isRegisterFieldsValid(true)) {
			String betaKey = null;
			if (Config.Debug.BUILD == Builds.BETA) {
				betaKey = mWidgets.register.key.getText();
			}

			mLoginScene.register(mWidgets.register.username.getText(), mWidgets.register.password.getText(), mWidgets.register.email.getText(),
					betaKey);
		}
	}

	/**
	 * @param showErrors true if you want to display the error messages
	 * @return true if all register fields are filled and the passwords match
	 */
	private boolean isRegisterFieldsValid(boolean showErrors) {
		boolean failed = false;

		// Test that fields are filled in
		// Username empty
		if (mWidgets.register.usernameListener.isTextFieldEmpty()) {
			setRegisterUsernameError("is empty");
			failed = true;
		}
		// Username length
		if (mWidgets.register.username.getText().length() < Config.User.NAME_LENGTH_MIN) {
			setRegisterUsernameError("must contain at least " + Config.User.NAME_LENGTH_MIN + " chars");
			failed = true;
		}
		// Password empty
		if (mWidgets.register.passwordListener.isTextFieldEmpty()) {
			setRegisterPasswordError("is empty");
			failed = true;
		}
		// Password length
		else if (mWidgets.register.password.getText().length() < Config.User.PASSWORD_LENGTH_MIN) {
			setRegisterPasswordError("must contain at least " + Config.User.PASSWORD_LENGTH_MIN + " chars");
			failed = true;
		}
		// Test that passwords match
		else if (!mWidgets.register.password.getText().equals(mWidgets.register.confirmPassword.getText())) {
			setRegisterPasswordError("passwords don't match!");
			failed = true;
		}
		// Email
		if (mWidgets.register.emailListener.isTextFieldEmpty()) {
			setRegisterEmailError("is empty");
			failed = true;
		}
		// Terms
		if (!mWidgets.register.acceptTerms.isChecked()) {
			setRegisterAcceptTermsError("please check");
			failed = true;
		}

		// Register key
		if (Config.Debug.BUILD == Builds.BETA) {
			// Empty
			if (mWidgets.register.keyListener.isTextFieldEmpty()) {
				setRegisterKeyError("is empty");
				failed = true;
			}
			// isn't correct length
			else if (mWidgets.register.keyListener.getText().length() != Config.Debug.REGISTER_KEY_LENGTH) {
				setRegisterKeyError("invalid key format");
				failed = true;
			}
		}

		return !failed;
	}

	/**
	 * Initializes the register table
	 */
	private void initRegisterTable() {
		AlignTable table = mWidgets.register.table;


		// Username
		mWidgets.register.usernameListener = new RegisterListener();
		mWidgets.register.username = mUiFactory.addTextField("Username", true, "Username", mWidgets.register.usernameListener, table, null);
		mWidgets.register.usernameError = mUiFactory.text.getLastCreatedErrorLabel();

		// Password
		mWidgets.register.passwordListener = new RegisterListener();
		mWidgets.register.password = mUiFactory.addPasswordField("Password", true, "Password", mWidgets.register.passwordListener, table, null);
		mWidgets.register.passwordError = mUiFactory.text.getLastCreatedErrorLabel();

		// Confirm password
		TextFieldListener textFieldListener = new RegisterListener();
		mWidgets.register.confirmPassword = mUiFactory.addPasswordField(null, false, "Confirm password", textFieldListener, table, null);

		// Email
		mWidgets.register.emailListener = new RegisterListener();
		mWidgets.register.email = mUiFactory.addTextField("Email", true, "your@email.com", mWidgets.register.emailListener, table, null);
		mWidgets.register.emailError = mUiFactory.text.getLastCreatedErrorLabel();

		// Beta key
		if (Config.Debug.BUILD == Builds.BETA) {
			mWidgets.register.keyListener = new RegisterListener();
			mWidgets.register.key = mUiFactory.addTextField("Beta Key", true, "", mWidgets.register.keyListener, table, null);
			mWidgets.register.keyError = mUiFactory.text.getLastCreatedErrorLabel();
		}

		// Accept terms
		mWidgets.register.acceptTermsError = mUiFactory.text.addError("Accept ", true, table, null);
		TextButton termsButton = mUiFactory.button.createText("Terms", TextButtonStyles.LINK);
		AlignTable innerTable = (AlignTable) table.getCell().getActor();
		innerTable.add(1, termsButton);
		table.row();
		mWidgets.register.acceptTerms = mUiFactory.button.addImage(SkinNames.General.BUTTON_CHECK_BOX, table, null, null);
		table.row();
		ButtonListener buttonListener = new ButtonListener(termsButton) {
			@Override
			protected void onPressed(Button button) {
				showTerms();
			}
		};
		// mUiFactory.button.addText("Read Terms", TextButtonStyles.LINK, table,
		// buttonListener, null, null);

		// Set fixed width
		table.layout();
		table.setKeepWidth(true);


		// Back
		table.row().setFillWidth(true).setEqualCellSize(true).setPadTop(mUiFactory.getStyles().vars.paddingButton);
		buttonListener = new ButtonListener() {
			@Override
			protected void onPressed(Button button) {
				mWidgets.register.hider.hide();
				mWidgets.login.hider.show();
			}
		};
		mUiFactory.button.addText("Back", TextButtonStyles.FILLED_PRESS, table, buttonListener, null, null);
		table.getCell().setFixedWidth(false);

		// Register
		buttonListener = new ButtonListener() {
			@Override
			protected void onPressed(Button button) {
				register();
			}
		};
		mUiFactory.button.addText("Register", TextButtonStyles.FILLED_PRESS, table, buttonListener, null, null);
		table.getCell().setFixedWidth(false);
		mUiFactory.button.addPadding(table.getRow());
		mWidgets.register.table.row();
		mWidgets.register.table.layout();

		// Add to stage
		addActor(mWidgets.register.table);
	}

	/**
	 * Show terms
	 */
	private void showTerms() {
		String terms = mLoginScene.getTerms();

		if (terms != null) {
			mUiFactory.msgBox.scrollable("Terms and Conditions", terms);
		}
	}

	/**
	 * Initializes the table to forgot password token
	 */
	private void initPasswordResetSendToken() {
		AlignTable table = mWidgets.send.table;

		// Email
		mWidgets.send.emailListener = new TextFieldListener() {
			@Override
			protected void onEnter(String newText) {
				sendToken();
			}

			@Override
			protected void onDone(String newText) {
				mWidgets.reset.email.setText(newText);
			}
		};

		mUiFactory.addTextField("Email", true, "", mWidgets.send.emailListener, table, null);
		mWidgets.send.emailError = mUiFactory.text.getLastCreatedErrorLabel();

		// Already has a token
		table.row().setAlign(Vertical.BOTTOM);
		ButtonListener listener = new ButtonListener() {
			@Override
			protected void onPressed(Button button) {
				mWidgets.send.hider.hide();
				mWidgets.reset.hider.show();
			}
		};
		mUiFactory.button.addText("I Have A Token Already", TextButtonStyles.TRANSPARENT_PRESS, table, listener, null, null);


		// Set fixed width
		table.layout();
		table.setKeepWidth(true);


		// BUTTONS
		// Back
		table.row().setFillWidth(true).setEqualCellSize(true).setPadTop(mUiFactory.getStyles().vars.paddingButton);
		listener = new ButtonListener() {
			@Override
			protected void onPressed(Button button) {
				mWidgets.send.hider.hide();
				mWidgets.login.hider.show();
			}
		};
		mUiFactory.button.addText("Back", TextButtonStyles.FILLED_PRESS, table, listener, null, null);
		table.getCell().setFixedWidth(true);

		// Send Token
		listener = new ButtonListener() {
			@Override
			protected void onPressed(Button button) {
				sendToken();
			}
		};
		mUiFactory.button.addText("Continue", TextButtonStyles.FILLED_PRESS, table, listener, null, null);
		table.getCell().setFixedWidth(true);


		mUiFactory.button.addPadding(table.getRow());
		mWidgets.send.table.row();
		mWidgets.send.table.layout();

		addActor(table);
	}

	/**
	 * Send token request
	 */
	private void sendToken() {
		if (!mWidgets.send.emailListener.isTextFieldEmpty()) {
			mLoginScene.passwordResetSendToken(mWidgets.send.emailListener.getText());
		} else {
			setPasswordResetSendError("is empty");
		}
	}

	/**
	 * Set reset password send token email error
	 * @param text error text
	 */
	void setPasswordResetSendError(String text) {
		mWidgets.send.emailError.setText(text);
	}

	/**
	 * Initializes the table where the user can reset a password with a token
	 */
	private void initPasswordResetNew() {
		AlignTable table = mWidgets.reset.table;


		// Email
		mWidgets.reset.emailListener = new TextFieldListener() {
			@Override
			protected void onEnter(String newText) {
				resetPassword();
			}
		};
		mWidgets.reset.email = mUiFactory.addTextField("Email", true, "your@email.com", mWidgets.reset.emailListener, table, null);
		mWidgets.reset.emailError = mUiFactory.text.getLastCreatedErrorLabel();

		// Password
		mWidgets.reset.passwordListener = new TextFieldListener() {
			@Override
			protected void onEnter(String newText) {
				resetPassword();
			}
		};
		mWidgets.reset.password = mUiFactory.addPasswordField("New Password", true, "New Password", mWidgets.reset.passwordListener, table, null);
		mWidgets.reset.passwordError = mUiFactory.text.getLastCreatedErrorLabel();

		// Confirm password
		TextFieldListener textFieldListener = new TextFieldListener() {
			@Override
			protected void onEnter(String newText) {
				resetPassword();
			}
		};
		mWidgets.reset.confirmPassword = mUiFactory.addPasswordField(null, false, "Confirm password", textFieldListener, table, null);

		// Token
		mWidgets.reset.tokenListener = new TextFieldListener() {
			@Override
			protected void onEnter(String newText) {
				resetPassword();
			}
		};
		mWidgets.reset.token = mUiFactory.addTextField("Token", true, "", mWidgets.reset.tokenListener, table, null);
		mWidgets.reset.tokenError = mUiFactory.text.getLastCreatedErrorLabel();

		// Set fixed width
		table.layout();
		table.setKeepWidth(true);


		// Back
		table.row().setFillWidth(true).setEqualCellSize(true).setPadTop(mUiFactory.getStyles().vars.paddingButton);
		ButtonListener buttonListener = new ButtonListener() {
			@Override
			protected void onPressed(Button button) {
				mWidgets.reset.hider.hide();
				mWidgets.send.hider.show();
			}
		};
		mUiFactory.button.addText("Back", TextButtonStyles.FILLED_PRESS, table, buttonListener, null, null);
		table.getCell().setFixedWidth(false);

		// Reset
		buttonListener = new ButtonListener() {
			@Override
			protected void onPressed(Button button) {
				resetPassword();
			}
		};
		mUiFactory.button.addText("Reset", TextButtonStyles.FILLED_PRESS, table, buttonListener, null, null);
		table.getCell().setFixedWidth(false);
		mUiFactory.button.addPadding(table.getRow());
		mWidgets.reset.table.row();
		mWidgets.reset.table.layout();

		// Add to stage
		addActor(table);
	}

	/**
	 * Reset password
	 */
	private void resetPassword() {
		clearResetPasswordErrors();
		if (isResetPasswordFieldsValid()) {
			mLoginScene.resetPassword(mWidgets.reset.email.getText(), mWidgets.reset.password.getText(), mWidgets.reset.token.getText());
		}
	}

	/**
	 * @return true if all reset password fields are filled and passwords match
	 */
	private boolean isResetPasswordFieldsValid() {
		boolean failed = false;

		// Token
		if (mWidgets.reset.tokenListener.isTextFieldEmpty()) {
			setPasswordResetTokenError("is empty");
			failed = true;
		}
		// Password empty
		if (mWidgets.reset.passwordListener.isTextFieldEmpty()) {
			setPasswordResetPasswordError("is empty");
			failed = true;
		}
		// Password length
		else if (mWidgets.reset.password.getText().length() < Config.User.PASSWORD_LENGTH_MIN) {
			setPasswordResetPasswordError("must contain at least " + Config.User.PASSWORD_LENGTH_MIN + " chars");
			failed = true;
		}
		// Test that passwords match
		else if (!mWidgets.reset.password.getText().equals(mWidgets.reset.confirmPassword.getText())) {
			setPasswordResetPasswordError("passwords don't match!");
			failed = true;
		}
		// Email
		if (mWidgets.reset.emailListener.isTextFieldEmpty()) {
			setPasswordResetEmailError("is empty");
			failed = true;
		}

		return !failed;
	}

	/**
	 * Clear reset password errors
	 */
	private void clearResetPasswordErrors() {
		mWidgets.reset.emailError.setText("");
		mWidgets.reset.passwordError.setText("");
		mWidgets.reset.tokenError.setText("");
	}

	/**
	 * Set register username error
	 * @param text error text
	 */
	void setPasswordResetTokenError(String text) {
		mWidgets.reset.tokenError.setText(text);
	}

	/**
	 * Set register password error
	 * @param text error text
	 */
	void setPasswordResetPasswordError(String text) {
		mWidgets.reset.passwordError.setText(text);
	}

	/**
	 * Set register email error
	 * @param text error text
	 */
	void setPasswordResetEmailError(String text) {
		mWidgets.reset.emailError.setText(text);
	}

	/**
	 * Show message box for creating an offline user meanwhile.
	 */
	void showConnectionError() {
		MsgBoxExecuter msgBox = mUiFactory.msgBox.add("Connection Error");
		msgBox.content("Could not connect to the server. To fix this either wait a couple of hours (if\n"
				+ "server is down) or connect your device to the\n" + "Internet.\n\n" + "Sorry for your incovenience.");
		msgBox.addCancelButtonAndKeys("OK");
	}

	/**
	 * Show password reset window
	 */
	void showPasswordResetWindow() {
		mWidgets.send.hider.hide();
		mWidgets.reset.hider.show();
	}

	/**
	 * Show login window
	 */
	void showLoginWindow() {
		mWidgets.reset.hider.hide();
		mWidgets.register.hider.hide();
		mWidgets.send.hider.hide();
		mWidgets.login.hider.show();
	}

	/**
	 * Show update information
	 * @param updateInfo all update information
	 */
	void showUpdateInfo(UpdateEvent updateInfo) {
		mUiFactory.msgBox.updateMessage(updateInfo);
	}

	/** The login scene */
	private LoginScene mLoginScene = null;
	/** All the widgets */
	private InnerWidgets mWidgets = new InnerWidgets();

	/**
	 * Register text field listener
	 */
	private class RegisterListener extends TextFieldListener {
		@Override
		protected void onEnter(String newText) {
			register();
		}
	}

	/**
	 * Inner widgets
	 */
	private class InnerWidgets implements Disposable {
		Login login = new Login();
		Register register = new Register();
		PasswordSend send = new PasswordSend();
		PasswordReset reset = new PasswordReset();

		private class Login implements Disposable {
			AlignTable table = new AlignTable();
			HideManual hider = new HideManual();
			TextField username = null;
			TextField password = null;

			// Listeners
			TextFieldListener usernameListener = null;
			TextFieldListener passwordListener = null;

			private Login() {
				init();
			}

			@Override
			public void dispose() {
				table.dispose();
				hider.dispose();

				init();
			}

			private void init() {
				hider.addToggleActor(table);
			}
		}

		private class Register implements Disposable {
			AlignTable table = new AlignTable();
			HideManual hider = new HideManual();
			TextField username = null;
			TextField password = null;
			TextField confirmPassword = null;
			TextField email = null;
			TextField key = null;
			ImageButton acceptTerms = null;

			// Listeners
			TextFieldListener usernameListener = null;
			TextFieldListener passwordListener = null;
			TextFieldListener emailListener = null;
			TextFieldListener keyListener = null;

			// Error labels
			Label usernameError = null;
			Label passwordError = null;
			Label emailError = null;
			Label keyError = null;
			Label acceptTermsError = null;

			private Register() {
				init();
			}

			@Override
			public void dispose() {
				table.dispose();
				hider.dispose();

				init();
			}

			private void init() {
				hider.addToggleActor(table);
			}
		}

		private class PasswordSend implements Disposable {
			AlignTable table = new AlignTable();
			HideManual hider = new HideManual();

			// Listeners
			TextFieldListener emailListener = null;

			// Error labels
			Label emailError = null;

			private PasswordSend() {
				init();
			}

			@Override
			public void dispose() {
				table.dispose();
				hider.dispose();

				init();
			}

			private void init() {
				hider.addToggleActor(table);
			}
		}

		private class PasswordReset implements Disposable {
			AlignTable table = new AlignTable();
			HideManual hider = new HideManual();
			TextField email = null;
			TextField password = null;
			TextField confirmPassword = null;
			TextField token = null;

			// Listeners
			TextFieldListener tokenListener = null;
			TextFieldListener passwordListener = null;
			TextFieldListener emailListener = null;

			// Error labels
			Label tokenError = null;
			Label passwordError = null;
			Label emailError = null;

			private PasswordReset() {
				init();
			}

			@Override
			public void dispose() {
				table.dispose();
				hider.dispose();

				init();
			}

			private void init() {
				hider.addToggleActor(table);
			}
		}

		@Override
		public void dispose() {
			login.dispose();
			register.dispose();
			send.dispose();
			reset.dispose();
		}
	}
}

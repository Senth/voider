package com.spiddekauga.voider.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.spiddekauga.utils.scene.ui.AlignTable;
import com.spiddekauga.utils.scene.ui.ButtonListener;
import com.spiddekauga.utils.scene.ui.HideManual;
import com.spiddekauga.utils.scene.ui.MsgBoxExecuter;
import com.spiddekauga.utils.scene.ui.TextFieldListener;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.resources.SkinNames;
import com.spiddekauga.voider.scene.Gui;
import com.spiddekauga.voider.scene.ui.UiStyles.TextButtonStyles;
import com.spiddekauga.voider.utils.Messages;

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

		initLoginTable();

		if (mLoginScene.isRegisterAvailable()) {
			initRegisterTable();
			mRegisterHider.hide();
		}

		setBackground(SkinNames.GeneralImages.BACKGROUND_SPACE, true);
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
		mLoginHider.addToggleActor(table);

		// Username
		mWidgets.login.usernameListener = new TextFieldListener() {
			@Override
			protected void onEnter(String newText) {
				login();
			}
		};
		mWidgets.login.username = mUiFactory.addTextField(null, false, "Username", mWidgets.login.usernameListener, table, null);

		// Password
		mWidgets.login.passwordListener = new TextFieldListener() {
			@Override
			protected void onEnter(String newText) {
				login();
			}
		};
		mWidgets.login.password = mUiFactory.addPasswordField(null, false, "Password", mWidgets.login.passwordListener, table, null);
		mWidgets.login.password.setName("password");

		// Forgot password
		table.row().setAlign(Vertical.BOTTOM);
		ButtonListener buttonListener = new ButtonListener() {
			@Override
			protected void onPressed(Button button) {
				/** @todo forgot password -> send to another GUI screen */
			}
		};
		mUiFactory.addTextButton("Forgot Password", TextButtonStyles.TRANSPARENT_PRESS, table, buttonListener, null, null);

		// Register
		if (mLoginScene.isRegisterAvailable()) {
			table.row();
			buttonListener = new ButtonListener() {
				@Override
				protected void onPressed(Button button) {
					mLoginHider.hide();
					mRegisterHider.show();
				}
			};
			mUiFactory.addTextButton("Register", TextButtonStyles.TRANSPARENT_PRESS, table, buttonListener, null, null);
		}


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
		mUiFactory.addTextButton("Exit", TextButtonStyles.FILLED_PRESS, table, buttonListener, null, null);
		table.getCell().setFixedWidth(false);

		// Login
		buttonListener = new ButtonListener() {
			@Override
			protected void onPressed(Button button) {
				login();
			}
		};
		mUiFactory.addTextButton("Login", TextButtonStyles.FILLED_PRESS, table, buttonListener, null, null);
		table.getCell().setFixedWidth(false);
		mUiFactory.addButtonPadding(table.getRow());
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
				showErrorMessage(errorMessage);
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
	 * Try to register with the specified fields
	 */
	private void register() {
		clearRegisterErrors();
		if (isRegisterFieldsValid(true)) {
			mLoginScene.register(mWidgets.register.username.getText(), mWidgets.register.password.getText(), mWidgets.register.email.getText());
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

		return !failed;
	}

	/**
	 * Initializes the register table
	 */
	private void initRegisterTable() {
		AlignTable table = mWidgets.register.table;
		table.setName("register-table");
		mRegisterHider.addToggleActor(table);


		// Username
		mWidgets.register.usernameListener = new TextFieldListener() {
			@Override
			protected void onEnter(String newText) {
				register();
			}
		};
		mWidgets.register.username = mUiFactory.addTextField("Username", true, "Username", mWidgets.register.usernameListener, table, null);
		mWidgets.register.usernameError = mUiFactory.text.getLastCreatedErrorLabel();

		// Password
		mWidgets.register.passwordListener = new TextFieldListener() {
			@Override
			protected void onEnter(String newText) {
				register();
			}
		};
		mWidgets.register.password = mUiFactory.addPasswordField("Password", true, "Password", mWidgets.register.passwordListener, table, null);
		mWidgets.register.passwordError = mUiFactory.text.getLastCreatedErrorLabel();

		// Confirm password
		TextFieldListener textFieldListener = new TextFieldListener() {
			@Override
			protected void onEnter(String newText) {
				register();
			}
		};
		mWidgets.register.confirmPassword = mUiFactory.addPasswordField(null, false, "Confirm password", textFieldListener, table, null);

		// Email
		mWidgets.register.emailListener = new TextFieldListener() {
			@Override
			protected void onEnter(String newText) {
				register();
			}
		};
		mWidgets.register.email = mUiFactory.addTextField("Email", true, "your@email.com", mWidgets.register.emailListener, table, null);
		mWidgets.register.emailError = mUiFactory.text.getLastCreatedErrorLabel();


		// Set fixed width
		table.layout();
		table.setKeepWidth(true);


		// Back
		table.row().setFillWidth(true).setEqualCellSize(true).setPadTop(mUiFactory.getStyles().vars.paddingButton);
		ButtonListener buttonListener = new ButtonListener() {
			@Override
			protected void onPressed(Button button) {
				mRegisterHider.hide();
				mLoginHider.show();
			}
		};
		mUiFactory.addTextButton("Back", TextButtonStyles.FILLED_PRESS, table, buttonListener, null, null);
		table.getCell().setFixedWidth(false);

		// Register
		buttonListener = new ButtonListener() {
			@Override
			protected void onPressed(Button button) {
				register();
			}
		};
		mUiFactory.addTextButton("Register", TextButtonStyles.FILLED_PRESS, table, buttonListener, null, null);
		table.getCell().setFixedWidth(false);
		mUiFactory.addButtonPadding(table.getRow());
		mWidgets.register.table.row();
		mWidgets.register.table.layout();

		// Add to stage
		addActor(mWidgets.register.table);
	}

	/**
	 * Show message box for creating an offline user meanwhile.
	 */
	void showCouldNotCreateUser() {
		MsgBoxExecuter msgBox = getFreeMsgBox(true);

		msgBox.setTitle("Could not connect to server!");
		msgBox.content("To fix this either wait a couple of hours (if\n" + "server is down) or connect your device to the\n" + "Internet.\n\n"
				+ "Sorry for your incovenience.");

		msgBox.addCancelButtonAndKeys("OK");
		showMsgBox(msgBox);
	}

	/**
	 * Show update needed
	 * @param newVersion new client version
	 * @param changeLog
	 */
	void showUpdateNeeded(String newVersion, String changeLog) {
		String message = Messages.Version.getRequiredUpdate(newVersion);
		mUiFactory.createUpdateMessageBox(message, changeLog, this);
	}

	/**
	 * Show update available
	 * @param newVersion new client version
	 * @param changeLog
	 */
	void showUpdateAvailable(String newVersion, String changeLog) {
		String message = Messages.Version.getOptionalUpdate(newVersion);
		mUiFactory.createUpdateMessageBox(message, changeLog, this);
	}

	/** The login scene */
	private LoginScene mLoginScene = null;
	/** Login hider */
	private HideManual mLoginHider = new HideManual();
	/** Register hider */
	private HideManual mRegisterHider = new HideManual();
	/** All the widgets */
	private InnerWidgets mWidgets = new InnerWidgets();

	/**
	 * Inner widgets
	 */
	private static class InnerWidgets {
		Login login = new Login();
		Register register = new Register();

		private static class Login {
			AlignTable table = new AlignTable();
			TextField username = null;
			TextField password = null;

			// Listeners
			TextFieldListener usernameListener = null;
			TextFieldListener passwordListener = null;
		}

		private static class Register {
			AlignTable table = new AlignTable();
			TextField username = null;
			TextField password = null;
			TextField confirmPassword = null;
			TextField email = null;

			// Listeners
			TextFieldListener usernameListener = null;
			TextFieldListener passwordListener = null;
			TextFieldListener emailListener = null;

			// Error labels
			Label usernameError = null;
			Label passwordError = null;
			Label emailError = null;
		}
	}
}

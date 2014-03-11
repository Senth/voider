package com.spiddekauga.voider.menu;

import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.utils.scene.ui.AlignTable;
import com.spiddekauga.utils.scene.ui.ButtonListener;
import com.spiddekauga.utils.scene.ui.HideManual;
import com.spiddekauga.utils.scene.ui.Label;
import com.spiddekauga.utils.scene.ui.MsgBoxExecuter;
import com.spiddekauga.utils.scene.ui.TextFieldListener;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.resources.InternalNames;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.resources.SkinNames;
import com.spiddekauga.voider.scene.Gui;

/**
 * GUI for login
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
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

		mMainTable.setFillParent(true);
		mMainTable.setTableAlign(Horizontal.CENTER, Vertical.MIDDLE);
		mMainTable.setRowAlign(Horizontal.CENTER, Vertical.MIDDLE);
		mMainTable.setCellPaddingDefault((Float) SkinNames.getResource(SkinNames.General.PADDING_DEFAULT));
		mWidgets.login.table.setPreferences(mMainTable);
		mWidgets.login.table.setRowAlign(Horizontal.LEFT, Vertical.MIDDLE);
		mWidgets.register.table.setPreferences(mMainTable);
		mWidgets.register.table.setRowAlign(Horizontal.LEFT, Vertical.MIDDLE);

		initLoginTable();

		if (mLoginScene.isRegisterAvailable()) {
			initRegisterTable();
			mRegisterHider.hide();
		}
	}

	/**
	 * Initializes the login table
	 */
	private void initLoginTable() {
		Skin skin = ResourceCacheFacade.get(InternalNames.UI_GENERAL);

		// Username
		Label label = new Label("Username", skin, SkinNames.General.LABEL_DEFAULT.toString());
		mWidgets.login.table.add(label);

		mWidgets.login.table.row();
		TextField textField = new TextField("", skin, SkinNames.General.TEXT_FIELD_DEFAULT.toString());
		mWidgets.login.username = textField;
		mWidgets.login.table.add(textField);
		TextFieldListener textFieldListener = new TextFieldListener(textField, null, null) {
			@Override
			protected void onDone(String newText) {
				if (isLoginFieldsValid(false)) {
					//					login();
				}
			}
		};
		mWidgets.login.usernameListener = textFieldListener;

		// Password
		mWidgets.login.table.row();
		label = new Label("Password", skin, SkinNames.General.LABEL_DEFAULT.toString());
		mWidgets.login.table.add(label);

		mWidgets.login.table.row();
		textField = new TextField("", skin, SkinNames.General.TEXT_FIELD_DEFAULT.toString());
		textField.setPasswordMode(true);
		textField.setPasswordCharacter('*');
		mWidgets.login.password = textField;
		mWidgets.login.table.add(textField);
		textFieldListener = new TextFieldListener(textField, null, null) {
			@Override
			protected void onDone(String newText) {
				if (isLoginFieldsValid(false)) {
					//					login();
				}
			}
		};
		mWidgets.login.passwordListener = textFieldListener;

		// Login
		mWidgets.login.table.row();
		Button button = new TextButton("Login", skin, SkinNames.General.TEXT_BUTTON_PRESS.toString());
		mWidgets.login.table.add(button);
		new ButtonListener(button) {
			@Override
			protected void onPressed() {
				if (isLoginFieldsValid(true)) {
					login();
				}
			}
		};

		// Register
		if (mLoginScene.isRegisterAvailable()) {
			button = new TextButton("Register", skin, SkinNames.General.TEXT_BUTTON_PRESS.toString());
			mWidgets.login.table.add(button);
			new ButtonListener(button) {
				@Override
				protected void onPressed() {
					mLoginHider.hide();
					mRegisterHider.show();
				}
			};
		}

		mWidgets.login.table.layout();

		float windowPadding = SkinNames.getResource(SkinNames.General.PADDING_WINDOW_LEFT_RIGHT);
		mWidgets.login.window = new Window("", skin, SkinNames.General.WINDOW_NO_TITLE.toString());
		mWidgets.login.window.add(mWidgets.login.table).pad(windowPadding);
		mMainTable.add(mWidgets.login.window);
		mLoginHider.addToggleActor(mWidgets.login.window);


		mWidgets.login.window.layout();
		mWidgets.login.window.setSize(mWidgets.login.table.getPrefWidth() + windowPadding * 2, mWidgets.login.table.getHeight() + windowPadding * 2);
	}

	/**
	 * Logins in the the current username and password fields
	 */
	private void login() {
		mLoginScene.login(mWidgets.login.username.getText(), mWidgets.login.password.getText());

		// TODO show logging in loading GIF
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
	 * Try to register with the specified fields
	 */
	private void register() {
		mLoginScene.register(
				mWidgets.register.username.getText(),
				mWidgets.register.password.getText(),
				mWidgets.register.email.getText());


		// TODO Show registering GIF
	}

	/**
	 * @param showErrors true if you want to display the error messages
	 * @return true if all register fields are filled and the passwords match
	 */
	private boolean isRegisterFieldsValid(boolean showErrors) {
		String errorMessage = null;

		// Test that fields are filled in
		if (mWidgets.register.usernameListener.isTextFieldEmpty()) {
			errorMessage = "Username is empty";
		}
		// Password empty
		else if (mWidgets.register.passwordListener.isTextFieldEmpty()) {
			errorMessage = "Password is empty";
		}
		// Password length
		else if (mWidgets.register.password.getText().length() < Config.User.PASSWORD_LENGTH_MIN) {
			errorMessage = "Password needs to contain at least " + Config.User.PASSWORD_LENGTH_MIN + " characters.";
		}
		// Test that passwords match
		else if (!mWidgets.register.password.getText().equals(mWidgets.register.confirmPassword.getText())) {
			errorMessage = "Passwords don't match!";
		}
		// Email
		else if (mWidgets.register.emailListener.isTextFieldEmpty()) {
			errorMessage = "Email is empty";
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
	 * Initializes the register table
	 */
	private void initRegisterTable() {
		Skin skin = ResourceCacheFacade.get(InternalNames.UI_GENERAL);


		// Username
		Label label = new Label("Username", skin, SkinNames.General.LABEL_DEFAULT.toString());
		mWidgets.register.table.add(label);

		mWidgets.register.table.row();
		TextField textField = new TextField("", skin, SkinNames.General.TEXT_FIELD_DEFAULT.toString());
		mWidgets.register.username = textField;
		mWidgets.register.table.add(textField);
		TextFieldListener textFieldListener = new TextFieldListener(textField, null, null) {
			@Override
			protected void onDone(String newText) {
				if (isRegisterFieldsValid(false)) {
					//					register();
				}
			}
		};
		mWidgets.register.usernameListener = textFieldListener;

		// Password
		mWidgets.register.table.row();
		label = new Label("Password", skin, SkinNames.General.LABEL_DEFAULT.toString());
		mWidgets.register.table.add(label);

		mWidgets.register.table.row();
		textField = new TextField("", skin, SkinNames.General.TEXT_FIELD_DEFAULT.toString());
		textField.setPasswordMode(true);
		textField.setPasswordCharacter('*');
		mWidgets.register.password = textField;
		mWidgets.register.table.add(textField);
		textFieldListener = new TextFieldListener(textField, null, null) {
			@Override
			protected void onDone(String newText) {
				if (isRegisterFieldsValid(false)) {
					//					register();
				}
			}
		};
		mWidgets.register.passwordListener = textFieldListener;

		// Confirm password
		mWidgets.register.table.row();
		label = new Label("Confirm password", skin, SkinNames.General.LABEL_DEFAULT.toString());
		mWidgets.register.table.add(label);

		mWidgets.register.table.row();
		textField = new TextField("", skin, SkinNames.General.TEXT_FIELD_DEFAULT.toString());
		textField.setPasswordMode(true);
		textField.setPasswordCharacter('*');
		mWidgets.register.confirmPassword = textField;
		mWidgets.register.table.add(textField);
		textFieldListener = new TextFieldListener(textField, null, null) {
			@Override
			protected void onDone(String newText) {
				if (isRegisterFieldsValid(false)) {
					//					register();
				}
			}
		};

		// Email
		mWidgets.register.table.row();
		label = new Label("Email", skin, SkinNames.General.LABEL_DEFAULT.toString());
		mWidgets.register.table.add(label);

		mWidgets.register.table.row();
		textField = new TextField("", skin, SkinNames.General.TEXT_FIELD_DEFAULT.toString());
		mWidgets.register.email = textField;
		mWidgets.register.table.add(textField);
		textFieldListener = new TextFieldListener(textField, null, null) {
			@Override
			protected void onDone(String newText) {
				if (isRegisterFieldsValid(false)) {
					//					register();
				}
			}
		};
		mWidgets.register.emailListener = textFieldListener;


		// Register
		mWidgets.register.table.row();
		Button button = new TextButton("Register", skin, SkinNames.General.TEXT_BUTTON_PRESS.toString());
		mWidgets.register.table.add(button);
		new ButtonListener(button) {
			@Override
			protected void onPressed() {
				if (isRegisterFieldsValid(true)) {
					register();
				}
			}
		};

		// Back
		button = new TextButton("Back", skin, SkinNames.General.TEXT_BUTTON_PRESS.toString());
		mWidgets.register.table.add(button);
		new ButtonListener(button) {
			@Override
			protected void onPressed() {
				mRegisterHider.hide();
				mLoginHider.show();
			}
		};


		mWidgets.register.table.layout();

		float windowPadding = SkinNames.getResource(SkinNames.General.PADDING_WINDOW_LEFT_RIGHT);
		mWidgets.register.window = new Window("", skin, SkinNames.General.WINDOW_NO_TITLE.toString());
		mWidgets.register.window.add(mWidgets.register.table).pad(windowPadding);
		mMainTable.add(mWidgets.register.window);
		mRegisterHider.addToggleActor(mWidgets.register.window);


		mWidgets.register.window.layout();
		mWidgets.register.window.setSize(mWidgets.register.table.getWidth() + windowPadding * 2, mWidgets.register.table.getHeight() + windowPadding * 2);
	}

	/**
	 * Show message box for creating an offline user meanwhile.
	 */
	void showCreateOfflineUser() {
		MsgBoxExecuter msgBox = getFreeMsgBox(true);

		msgBox.setTitle("Create offline user");
		msgBox.content("Could not connect to server!\n\n"
				+ "Do you want to create an offline user meanwhile?\n"
				+ "When a connection can be made the offline user\n"
				+ "will be registered. (If the username and email\n"
				+ "is free you won't have to do anything)\n\n"
				+ "To fix this either wait a couple of hours (if\n"
				+ "server is down) or connect your device to the\n"
				+ "Internet.");

		msgBox.button("Yes, create offline user", new CCreateOfflineUser(mLoginScene, mWidgets.register.username.getText(), mWidgets.register.password.getText(), mWidgets.register.email.getText()));
		msgBox.addCancelButtonAndKeys("No");
		showMsgBox(msgBox);
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
	@SuppressWarnings("javadoc")
	private static class InnerWidgets {
		Login login = new Login();
		Register register = new Register();

		private static class Login {
			Window window = null;
			AlignTable table = new AlignTable();
			TextField username = null;
			TextField password = null;

			// Listeners
			TextFieldListener usernameListener = null;
			TextFieldListener passwordListener = null;
		}

		private static class Register {
			AlignTable table = new AlignTable();
			Window window = null;
			TextField username = null;
			TextField password = null;
			TextField confirmPassword = null;
			TextField email = null;

			// Listeners
			TextFieldListener usernameListener = null;
			TextFieldListener passwordListener = null;
			TextFieldListener emailListener = null;
		}
	}
}

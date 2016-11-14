package com.spiddekauga.voider.menu;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Disposable;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.utils.scene.ui.AlignTable;
import com.spiddekauga.utils.scene.ui.TabWidget;
import com.spiddekauga.utils.scene.ui.TextFieldListener;
import com.spiddekauga.utils.scene.ui.validate.IValidate;
import com.spiddekauga.utils.scene.ui.validate.VFieldLength;
import com.spiddekauga.utils.scene.ui.validate.VFieldMatch;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.repo.resource.SkinNames;
import com.spiddekauga.voider.scene.ui.UiStyles.LabelStyles;

import java.util.ArrayList;

/**

 */
public class UserGui extends MenuGui {

private UserScene mScene = null;
private Widgets mWidget = new Widgets();

/**
 * Sets the user scene
 * @param scene
 */
void setScene(UserScene scene) {
	mScene = scene;
}

@Override
public void initGui() {
	super.initGui();

	initHeader();
	initTabs();
	initAccount();

	addBackButton();
}

private void initHeader() {
	mWidget.tabWidget = mUiFactory.addSettingsWindow("Player Info", mMainTable);
}

private void initTabs() {
	TabWidget tabWidget = mWidget.tabWidget;

	mUiFactory.button.addTab(SkinNames.General.SETTINGS_ACCOUNT, mWidget.account.table, tabWidget);
}

private void initAccount() {
	AlignTable table = mWidget.account.table;
	initTable(table, "Account");

	// Change Password
	mUiFactory.text.addSection("Change Password", table, null);

	// Old Password
	mWidget.account.oldPasswordListener = new ChangePasswordListener();
	mUiFactory.addPasswordField("Old Password", true, "Old Password", mWidget.account.oldPasswordListener, table, null);
	mWidget.account.oldPasswordError = mUiFactory.text.getLastCreatedErrorLabel();

	IValidate validate = new VFieldLength(mWidget.account.oldPasswordListener, mWidget.account.oldPasswordError, Config.User.PASSWORD_LENGTH_MIN);
	mWidget.account.validates.add(validate);


	// New Password
	mWidget.account.newPasswordListener = new ChangePasswordListener();
	mUiFactory.addPasswordField("New Password", true, "New Password", mWidget.account.newPasswordListener, table, null);
	mWidget.account.newPasswordError = mUiFactory.text.getLastCreatedErrorLabel();

	validate = new VFieldLength(mWidget.account.newPasswordListener, mWidget.account.newPasswordError, Config.User.PASSWORD_LENGTH_MIN);
	mWidget.account.validates.add(validate);


	// Confirm password
	mWidget.account.confirmPasswordListener = new ChangePasswordListener();
	mUiFactory.addPasswordField("Confirm Password", true, "Confirm Password", mWidget.account.confirmPasswordListener, table, null);
	mWidget.account.confirmPasswordError = mUiFactory.text.getLastCreatedErrorLabel();

	validate = new VFieldLength(mWidget.account.confirmPasswordListener, mWidget.account.confirmPasswordError, Config.User.PASSWORD_LENGTH_MIN);
	mWidget.account.validates.add(validate);

	validate = new VFieldMatch(mWidget.account.confirmPasswordListener, mWidget.account.confirmPasswordError, mWidget.account.newPasswordListener);
	mWidget.account.validates.add(validate);
}

/**
 * Initializes the table with a header
 * @param table the table to initialize
 * @param header the header text
 */
private static void initTable(AlignTable table, String header) {
	table.setAlign(Horizontal.LEFT, Vertical.MIDDLE);
	table.setName(header);

	table.row().setAlign(Vertical.TOP).setFillWidth(true).setPadBottom(mUiFactory.getStyles().vars.paddingInner);
	table.add().setFillWidth(true);
	mUiFactory.text.add(header, table, LabelStyles.HIGHLIGHT);
	table.add().setFillWidth(true);
}

/**
 * Tries to change the password if all fields are correct
 */
private void changePassword() {
	clearErrors();

	// Validate
	boolean allValid = true;
	for (IValidate validate : mWidget.account.validates) {
		if (!validate.isValid()) {
			validate.printError();
			allValid = false;
		}
	}

	if (allValid) {
		mScene.setPassword(mWidget.account.oldPasswordListener.getText(), mWidget.account.newPasswordListener.getText());
	}
}

/**
 * Clear all error texts
 */
void clearErrors() {
	mWidget.account.oldPasswordError.setText("");
	mWidget.account.newPasswordError.setText("");
	mWidget.account.confirmPasswordError.setText("");
}

/**
 * Clear all password fields
 */
void clearPasswordFields() {
	mWidget.account.oldPasswordListener.getTextField().setText("");
	mWidget.account.newPasswordListener.getTextField().setText("");
	mWidget.account.confirmPasswordListener.getTextField().setText("");
}

/**
 * Sets the new password error text
 * @param text
 */
void setNewPasswordErrorText(String text) {
	mWidget.account.newPasswordError.setText(text);
}

/**
 * Sets the old password error text
 * @param text
 */
void setOldPasswordErrorText(String text) {
	mWidget.account.oldPasswordError.setText(text);
}

private class ChangePasswordListener extends TextFieldListener {
	@Override
	protected void onEnter(String newText) {
		changePassword();
	}
}

private class Widgets implements Disposable {
	TabWidget tabWidget = null;
	Account account = new Account();

	private class Account implements Disposable {
		AlignTable table = new AlignTable();
		// TextField oldPassword = null;
		TextFieldListener oldPasswordListener = null;
		Label oldPasswordError = null;
		ArrayList<IValidate> validates = new ArrayList<>();

		// TextField newPassword = null;
		TextFieldListener newPasswordListener = null;
		Label newPasswordError = null;

		// TextField confirmPassword = null;
		TextFieldListener confirmPasswordListener = null;
		Label confirmPasswordError = null;

		@Override
		public void dispose() {
			table.dispose();
			validates.clear();
		}
	}

	@Override
	public void dispose() {
		account.dispose();
	}
}
}

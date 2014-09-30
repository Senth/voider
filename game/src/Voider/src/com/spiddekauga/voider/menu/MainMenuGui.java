package com.spiddekauga.voider.menu;

import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.utils.scene.ui.AlignTable;
import com.spiddekauga.utils.scene.ui.ButtonListener;
import com.spiddekauga.utils.scene.ui.MsgBoxExecuter;
import com.spiddekauga.utils.scene.ui.UiFactory.Positions;
import com.spiddekauga.voider.editor.commands.CGameQuit;
import com.spiddekauga.voider.editor.commands.CUserLogout;
import com.spiddekauga.voider.menu.MainMenu.Menus;
import com.spiddekauga.voider.resources.SkinNames;
import com.spiddekauga.voider.utils.Messages;
import com.spiddekauga.voider.utils.User;


/**
 * GUI for main menu
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class MainMenuGui extends MenuGui {
	@Override
	public void initGui() {
		super.initGui();

		mMainTable.setAlign(Horizontal.CENTER, Vertical.MIDDLE);
		mPlayerInfoTable.setAlign(Horizontal.RIGHT, Vertical.TOP);
		mOptionTable.setAlign(Horizontal.LEFT, Vertical.BOTTOM);
		mLogoutTable.setAlign(Horizontal.RIGHT, Vertical.BOTTOM);
		mSpiddekaugaTable.setAlign(Horizontal.LEFT, Vertical.TOP);

		addActor(mPlayerInfoTable);
		addActor(mOptionTable);
		addActor(mLogoutTable);
		addActor(mSpiddekaugaTable);

		initMainMenu();
	}

	@Override
	public void dispose() {
		super.dispose();
		mPlayerInfoTable.dispose();
		mOptionTable.dispose();
		mLogoutTable.dispose();
		mSpiddekaugaTable.dispose();
	}

	/**
	 * Show quit main menu dialog
	 */
	void showQuitMsgBox() {
		MsgBoxExecuter msgBox = getFreeMsgBox(true);
		msgBox.setTitle("Quit game?");
		msgBox.content("\nDo you want to quit the game?");
		msgBox.button("Quit", new CGameQuit());
		msgBox.button("Logout", new CUserLogout());
		msgBox.addCancelButtonAndKeys();
		showMsgBox(msgBox);
	}

	/**
	 * Initializes the main menu
	 */
	private void initMainMenu() {
		// Play
		mMainTable.row();
		Button button = mUiFactory.addImageButtonLabel(SkinNames.General.PLAY, "Play", Positions.BOTTOM, mMainTable, null, null);
		new ButtonListener(button) {
			@Override
			protected void onPressed(Button button) {
				mMenuScene.pushMenu(Menus.PLAY);
			}
		};


		// Explore
		button = mUiFactory.addImageButtonLabel(SkinNames.General.EXPLORE, "Explore", Positions.BOTTOM, mMainTable, null, null);
		new ButtonListener(button) {
			@Override
			protected void onPressed(Button button) {
				mMenuScene.gotoExplore();
			}
		};
		mUiFactory.addButtonPadding(mMainTable);


		// Create
		button = mUiFactory.addImageButtonLabel(SkinNames.General.CREATE, "Create", Positions.BOTTOM, mMainTable, null, null);
		new ButtonListener(button) {
			@Override
			protected void onPressed(Button button) {
				mMenuScene.pushMenu(Menus.EDITOR);
			}
		};
		mUiFactory.addButtonPadding(mMainTable);


		// Options
		button = mUiFactory.addImageButton(SkinNames.General.SETTINGS_BIG, mOptionTable, null, null);
		new ButtonListener(button) {
			@Override
			protected void onPressed(Button button) {
				// TODO go to options
			}
		};

		// Player Info
		button = mUiFactory.addImageButton(SkinNames.General.PLAYER_BIG, mPlayerInfoTable, null, null);
		new ButtonListener(button) {
			@Override
			protected void onPressed(Button button) {
				// TODO go to info
			}
		};

		// Spiddekauga Info
		button = mUiFactory.addImageButton(SkinNames.General.SPIDDEKAUGA_INFO, mSpiddekaugaTable, null, null);
		new ButtonListener(button) {
			@Override
			protected void onPressed(Button button) {
				// TODO go to game info
			}
		};

		// Logout
		button = mUiFactory.addImageButton(SkinNames.General.LOGOUT, mLogoutTable, null, null);
		new ButtonListener(button) {
			@Override
			protected void onPressed(Button button) {
				MsgBoxExecuter msgBox = getFreeMsgBox(true);

				msgBox.setTitle("Logout");

				msgBox.content("Do you want to logout?", Align.center);

				if (!User.getGlobalUser().isOnline()) {
					msgBox.contentRow();
					msgBox.content("NOTE! You are currently offline.\nYou will only be able to login (and play) if you have an Internet connection.",
							Align.center).padTop(mUiFactory.getStyles().vars.paddingSeparator);
				}

				msgBox.addCancelButtonAndKeys();
				msgBox.button("Logout", new CUserLogout());
				showMsgBox(msgBox);
			}
		};
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

	private AlignTable mOptionTable = new AlignTable();
	private AlignTable mPlayerInfoTable = new AlignTable();
	private AlignTable mLogoutTable = new AlignTable();
	private AlignTable mSpiddekaugaTable = new AlignTable();

}

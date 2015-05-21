package com.spiddekauga.voider.menu;

import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.spiddekauga.utils.commands.CGameQuit;
import com.spiddekauga.utils.commands.CRun;
import com.spiddekauga.utils.commands.Command;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.utils.scene.ui.AlignTable;
import com.spiddekauga.utils.scene.ui.ButtonListener;
import com.spiddekauga.utils.scene.ui.MsgBoxExecuter;
import com.spiddekauga.voider.menu.MainMenu.Scenes;
import com.spiddekauga.voider.repo.resource.SkinNames;
import com.spiddekauga.voider.scene.ui.UiFactory.Positions;
import com.spiddekauga.voider.scene.ui.UiStyles.LabelStyles;
import com.spiddekauga.voider.utils.User;
import com.spiddekauga.voider.utils.commands.CUserLogout;


/**
 * GUI for main menu
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
class MainMenuGui extends MenuGui {
	/**
	 * Public constructor
	 */
	public MainMenuGui() {
		// Does nothing
	}

	/**
	 * Set the main menu scene
	 * @param scene
	 */
	void setScene(MainMenu scene) {
		mScene = scene;
	}

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
		MsgBoxExecuter msgBox = mUiFactory.msgBox.add("Quit Game?");
		msgBox.content("Do you want to quit the game?");
		msgBox.addCancelButtonAndKeys();

		Command logoutCommand = null;
		if (User.getGlobalUser().isOnline()) {
			logoutCommand = new CUserLogout();
		} else {
			logoutCommand = new CRun() {
				@Override
				public boolean execute() {
					showConfirmLogout();
					return true;
				}
			};
		}

		msgBox.button("Logout", logoutCommand);
		msgBox.button("Quit", new CGameQuit());
	}

	/**
	 * Initializes the main menu
	 */
	private void initMainMenu() {
		// Play
		Button button = mUiFactory.button.addImageWithLabel(SkinNames.General.PLAY, "Play", Positions.BOTTOM, null, mMainTable, null, null);
		new ButtonListener(button) {
			@Override
			protected void onPressed(Button button) {
				mScene.gotoExplore();
			}
		};
		mUiFactory.button.addSound(button);
		mUiFactory.button.addPadding(mMainTable);


		// Create
		button = mUiFactory.button.addImageWithLabel(SkinNames.General.CREATE, "Create", Positions.BOTTOM, null, mMainTable, null, null);
		new ButtonListener(button) {
			@Override
			protected void onPressed(Button button) {
				mScene.gotoScene(Scenes.EDITOR);
			}
		};
		mUiFactory.button.addSound(button);
		mUiFactory.button.addPadding(mMainTable);


		// Options
		button = mUiFactory.button.addImage(SkinNames.General.SETTINGS_BIG, mOptionTable, null, null);
		new ButtonListener(button) {
			@Override
			protected void onPressed(Button button) {
				mScene.gotoScene(Scenes.SETTINGS);
			}
		};
		mUiFactory.button.addSound(button);

		// Player Info
		button = mUiFactory.button.addImage(SkinNames.General.PLAYER_BIG, mPlayerInfoTable, null, null);
		new ButtonListener(button) {
			@Override
			protected void onPressed(Button button) {
				// TODO go to info
			}
		};
		mUiFactory.button.addSound(button);

		// Spiddekauga Info
		button = mUiFactory.button.addImage(SkinNames.General.SPIDDEKAUGA_INFO, mSpiddekaugaTable, null, null);
		new ButtonListener(button) {
			@Override
			protected void onPressed(Button button) {
				mScene.gotoScene(Scenes.CREDITS);
			}
		};
		mUiFactory.button.addSound(button);

		// Logout
		button = mUiFactory.button.addImage(SkinNames.General.LOGOUT, mLogoutTable, null, null);
		new ButtonListener(button) {
			@Override
			protected void onPressed(Button button) {
				showQuitMsgBox();
			}
		};
		mUiFactory.button.addSound(button);
	}

	/**
	 * Show Confirm logout message box
	 */
	void showConfirmLogout() {
		MsgBoxExecuter msgBox = mUiFactory.msgBox.add("Confirm Logout");
		msgBox.content("You are currently offline.\nYou can only login again if you have an Internet connection.", Align.center,
				LabelStyles.HIGHLIGHT.getStyle());

		msgBox.addCancelButtonAndKeys();
		msgBox.button("Logout", new CUserLogout());
	}

	/**
	 * Shows client changes since last login
	 * @param changeLog new changes
	 */
	void showChangesSinceLastLogin(String changeLog) {
		mUiFactory.msgBox.changeLog("ChangeLog", "New changes since you last logged in on this device", changeLog);
	}

	/**
	 * Show terms
	 * @param terms
	 */
	void showTerms(final String terms) {
		final MsgBoxExecuter msgBox = mUiFactory.msgBox.add("New terms and conditions");
		msgBox.content("Terms and conditions have been changed since\nyou last logged in on this device.\n\nPlease accept these.");

		msgBox.button("Read Terms", new CRun() {
			@Override
			public boolean execute() {
				mUiFactory.msgBox.scrollable("Terms and Conditions", terms);
				return false;
			}
		});

		msgBox.button("Accept", new CRun() {
			@Override
			public boolean execute() {
				mScene.acceptTerms();
				return true;
			}
		});
	}


	private AlignTable mOptionTable = new AlignTable();
	private AlignTable mPlayerInfoTable = new AlignTable();
	private AlignTable mLogoutTable = new AlignTable();
	private AlignTable mSpiddekaugaTable = new AlignTable();

	private MainMenu mScene = null;
}

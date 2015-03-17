package com.spiddekauga.voider.menu;

import java.util.ArrayList;

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
import com.spiddekauga.voider.menu.MainMenu.Menus;
import com.spiddekauga.voider.network.misc.Motd;
import com.spiddekauga.voider.repo.misc.SettingRepo;
import com.spiddekauga.voider.repo.misc.SettingRepo.SettingDateRepo;
import com.spiddekauga.voider.repo.resource.SkinNames;
import com.spiddekauga.voider.scene.ui.UiFactory.Positions;
import com.spiddekauga.voider.scene.ui.UiStyles.LabelStyles;
import com.spiddekauga.voider.utils.Messages;
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
				mMenuScene.gotoExplore();
			}
		};
		mUiFactory.button.addSound(button);
		mUiFactory.button.addPadding(mMainTable);


		// Create
		button = mUiFactory.button.addImageWithLabel(SkinNames.General.CREATE, "Create", Positions.BOTTOM, null, mMainTable, null, null);
		new ButtonListener(button) {
			@Override
			protected void onPressed(Button button) {
				mMenuScene.pushMenu(Menus.EDITOR);
			}
		};
		mUiFactory.button.addSound(button);
		mUiFactory.button.addPadding(mMainTable);


		// Options
		button = mUiFactory.button.addImage(SkinNames.General.SETTINGS_BIG, mOptionTable, null, null);
		new ButtonListener(button) {
			@Override
			protected void onPressed(Button button) {
				mMenuScene.pushMenu(Menus.SETTINGS);
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
				mMenuScene.pushMenu(Menus.CREDITS);
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
	 * Show update needed
	 * @param newVersion new client version
	 * @param changeLog
	 */
	void showUpdateNeeded(String newVersion, String changeLog) {
		String message = Messages.Version.getRequiredUpdate(newVersion);
		mUiFactory.msgBox.updateMessage(true, message, changeLog);
	}

	/**
	 * Show update available
	 * @param newVersion new client version
	 * @param changeLog
	 */
	void showUpdateAvailable(String newVersion, String changeLog) {
		String message = Messages.Version.getOptionalUpdate(newVersion);
		mUiFactory.msgBox.updateMessage(false, message, changeLog);
	}

	/**
	 * Shows client changes since last login
	 * @param changeLog new changes
	 */
	void showChangesSinceLastLogin(String changeLog) {
		mUiFactory.msgBox.changeLog("ChangeLog", "New changes since you last logged in on this device", changeLog);
	}

	/**
	 * Show all message of the day
	 * @param motds all messages
	 */
	void showMotds(final ArrayList<Motd> motds) {
		SettingDateRepo dateRepo = SettingRepo.getInstance().date();

		for (final Motd motd : motds) {
			MsgBoxExecuter msgBox = mUiFactory.msgBox.add(motd.title);

			String date = dateRepo.getDateTime(motd.created);

			msgBox.content(date, Align.center);
			msgBox.contentRow();
			msgBox.content(motd.content, Align.center);

			// Update MOTD view date when viewed
			Command motdViewed = new CRun() {
				@Override
				public boolean execute() {
					mMenuScene.motdViewed(motd);
					return true;
				}
			};

			msgBox.addCancelButtonAndKeys("OK", motdViewed);
		}
	}

	private AlignTable mOptionTable = new AlignTable();
	private AlignTable mPlayerInfoTable = new AlignTable();
	private AlignTable mLogoutTable = new AlignTable();
	private AlignTable mSpiddekaugaTable = new AlignTable();
}

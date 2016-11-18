package com.spiddekauga.voider.menu;

import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.utils.Disposable;
import com.spiddekauga.utils.commands.CGameQuit;
import com.spiddekauga.utils.commands.CRun;
import com.spiddekauga.utils.commands.Command;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.utils.scene.ui.AlignTable;
import com.spiddekauga.utils.scene.ui.ButtonListener;
import com.spiddekauga.utils.scene.ui.Cell;
import com.spiddekauga.utils.scene.ui.MsgBox;
import com.spiddekauga.voider.menu.MainMenu.Scenes;
import com.spiddekauga.voider.repo.resource.SkinNames;
import com.spiddekauga.voider.repo.user.User;
import com.spiddekauga.voider.scene.ui.UiFactory.BarLocations;
import com.spiddekauga.voider.scene.ui.UiFactory.Positions;
import com.spiddekauga.voider.scene.ui.UiStyles.LabelStyles;
import com.spiddekauga.voider.scene.ui.UiStyles.TextButtonStyles;
import com.spiddekauga.voider.utils.commands.CUserLogout;
import com.spiddekauga.voider.version.Version;

import java.util.List;


/**
 * GUI for main menu
 */
class MainMenuGui extends MenuGui {
private MainMenu mScene = null;
private InnerWidgets mWidgets = new InnerWidgets();

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
public void onCreate() {
	super.onCreate();

	initTopBar();
	initMainMenu();
}

/**
 * Init top bar
 */
private void initTopBar() {
	mUiFactory.addBar(BarLocations.TOP, false, getStage());
	initLeftTopBar();
	initRightTopBar();
}

/**
 * Initializes the main menu
 */
private void initMainMenu() {
	mMainTable.setAlign(Horizontal.CENTER, Vertical.MIDDLE);
	mWidgets.bottomRight.table.setAlign(Horizontal.RIGHT, Vertical.BOTTOM);
	mWidgets.bottomLeft.table.setAlign(Horizontal.LEFT, Vertical.BOTTOM);

	addActor(mWidgets.bottomRight.table);
	addActor(mWidgets.bottomLeft.table);

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

	// Spiddekauga Info
	button = mUiFactory.button.addImage(SkinNames.General.INFO_BIG, mWidgets.bottomLeft.table, null, null);
	new ButtonListener(button) {
		@Override
		protected void onPressed(Button button) {
			mScene.gotoScene(Scenes.CREDITS);
		}
	};
	mUiFactory.button.addSound(button);

	// Logout
	button = mUiFactory.button.addImage(SkinNames.General.LOGOUT, mWidgets.bottomRight.table, null, null);
	new ButtonListener(button) {
		@Override
		protected void onPressed(Button button) {
			showQuitMsgBox();
		}
	};
	mUiFactory.button.addSound(button);
}

/**
 * Init left top bar
 */
private void initLeftTopBar() {
	AlignTable table = mWidgets.topLeft.table;
	initTopBarTable(table, Horizontal.LEFT);

	// Player Info
	Button button = mUiFactory.button.addImage(SkinNames.General.PANEL_PLAYER, table, null, null);
	ButtonListener buttonListener = new ButtonListener(button) {
		@Override
		protected void onPressed(Button button) {
			mScene.gotoScene(Scenes.USER);
		}
	};
	mUiFactory.button.addSound(button);

	// Username
	Cell cell = mUiFactory.button.addText("", TextButtonStyles.LINK, table, buttonListener, null, null);
	mWidgets.topLeft.username = (TextButton) cell.getActor();
	mUiFactory.button.addSound(mWidgets.topLeft.username);
}

/**
 * Init right top bar
 */
private void initRightTopBar() {
	AlignTable table = mWidgets.topRight.table;
	initTopBarTable(table, Horizontal.RIGHT);

	Button button = null;

	// Bug Report
	button = mUiFactory.button.addImage(SkinNames.General.PANEL_BUG, table, null, null);
	mUiFactory.button.addSound(button);
	new ButtonListener(button) {
		@Override
		protected void onPressed(Button button) {
			mUiFactory.msgBox.bugReport();
		}
	};

	// Reddit
	button = mUiFactory.button.addImage(SkinNames.General.PANEL_REDDIT, table, null, null);
	mUiFactory.button.addSound(button);
	new ButtonListener(button) {
		@Override
		protected void onPressed(Button button) {
			MsgBox msgBox = mUiFactory.msgBox.add("Open Reddit Community?");
			msgBox.content("Do you want to go to the reddit community?\n" + "This will open in your browser");

			Command openReddit = new CRun() {
				@Override
				public boolean execute() {
					mScene.gotoReddit();
					return true;
				}
			};

			msgBox.addCancelButtonAndKeys();
			msgBox.button("Open Reddit", openReddit);
		}

		;
	};

	// Options
	button = mUiFactory.button.addImage(SkinNames.General.PANEL_SETTINGS, table, null, null);
	mUiFactory.button.addSound(button);
	new ButtonListener(button) {
		@Override
		protected void onPressed(Button button) {
			mScene.gotoScene(Scenes.SETTINGS);
		}
	};
}

/**
 * Show quit main menu dialog
 */
void showQuitMsgBox() {
	MsgBox msgBox = mUiFactory.msgBox.add("Quit Game?");
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
 * Initialize a top bar table
 * @param table
 * @param position
 */
private void initTopBarTable(AlignTable table, Horizontal position) {
	table.setAlign(position, Vertical.TOP);
	table.setMargin(0, mUiFactory.getStyles().vars.paddingOuter, 0, mUiFactory.getStyles().vars.paddingOuter);
	table.setAlignRow(position, Vertical.MIDDLE);
	table.row().setHeight(mUiFactory.getStyles().vars.barUpperLowerHeight);
	addActor(table);
}

/**
 * Show Confirm logout message box
 */
void showConfirmLogout() {
	MsgBox msgBox = mUiFactory.msgBox.add("Confirm Logout");
	msgBox.content("You are currently offline.\nYou can only login again if you have an Internet connection.", Align.center,
			LabelStyles.HIGHLIGHT.getStyle());

	msgBox.addCancelButtonAndKeys();
	msgBox.button("Logout", new CUserLogout());
}

@Override
public void onDestroy() {
	mWidgets.dispose();
	super.onDestroy();
}

/**
 * Reset the username, should be called initially and whenever the online/offline mode changes
 * @param username
 * @param online true if online
 */
void resetUsername(String username, boolean online) {
	TextButton button = mWidgets.topLeft.username;
	if (button != null) {
		String onlineText = online ? " (online)" : " (offline)";
		button.setText(username + onlineText);
		button.pack();

		// Change color
		if (online) {
			button.getStyle().fontColor = TextButtonStyles.LINK.getStyle().fontColor;
		} else {
			button.getStyle().fontColor = LabelStyles.WARNING.getStyle().fontColor;
		}
	}
}

/**
 * Shows client changes since last login
 * @param versions all new versions since last login
 */
void showChangesSinceLastLogin(List<Version> versions) {
	mUiFactory.msgBox.changeLog("ChangeLog", "New changes since you last logged in on this device", versions);
}

/**
 * Show terms
 * @param terms
 */
void showTerms(final String terms) {
	final MsgBox msgBox = mUiFactory.msgBox.add("New terms and conditions");
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

private class InnerWidgets implements Disposable {
	private Default bottomLeft = new Default();
	private Default bottomRight = new Default();
	private Default topRight = new Default();
	private TopLeft topLeft = new TopLeft();

	class Default implements Disposable {
		AlignTable table = new AlignTable();

		@Override
		public void dispose() {
			table.dispose();
		}
	}

	class TopLeft implements Disposable {
		AlignTable table = new AlignTable();
		TextButton username = null;

		@Override
		public void dispose() {
			table.dispose();
		}
	}

	@Override
	public void dispose() {
		bottomLeft.dispose();
		bottomRight.dispose();
		topRight.dispose();
		topLeft.dispose();
	}
}
}

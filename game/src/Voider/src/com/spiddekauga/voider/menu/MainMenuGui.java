package com.spiddekauga.voider.menu;

import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.utils.scene.ui.AlignTable;
import com.spiddekauga.utils.scene.ui.ButtonListener;
import com.spiddekauga.utils.scene.ui.TooltipListener;
import com.spiddekauga.voider.menu.MainMenu.Menus;
import com.spiddekauga.voider.resources.InternalNames;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.resources.SkinNames;
import com.spiddekauga.voider.utils.Messages;


/**
 * GUI for main menu
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class MainMenuGui extends MenuGui {
	@Override
	public void initGui() {
		super.initGui();

		mMainTable.setAlign(Horizontal.CENTER, Vertical.MIDDLE);
		mInfoTable.setPreferences(mMainTable);
		mInfoTable.setAlign(Horizontal.RIGHT, Vertical.TOP);
		mOptionTable.setPreferences(mMainTable);
		mOptionTable.setAlign(Horizontal.RIGHT, Vertical.BOTTOM);
		mLogoutTable.setPreferences(mMainTable);
		mLogoutTable.setAlign(Horizontal.LEFT, Vertical.TOP);

		getStage().addActor(mInfoTable);
		getStage().addActor(mOptionTable);
		getStage().addActor(mLogoutTable);

		initMainMenu();
	}

	@Override
	public void dispose() {
		super.dispose();
		mInfoTable.dispose();
		mOptionTable.dispose();
		mLogoutTable.dispose();
	}

	/**
	 * Initializes the main menu
	 */
	private void initMainMenu() {
		Skin skin = ResourceCacheFacade.get(InternalNames.UI_GENERAL);

		// Play
		mMainTable.row();
		Button button = new ImageButton(skin, SkinNames.General.PLAY.toString());
		mMainTable.add(button);
		TooltipListener tooltipListener = new TooltipListener(button, Messages.Tooltip.Menus.Main.PLAY);
		new ButtonListener(button, tooltipListener) {
			@Override
			protected void onPressed() {
				mMenuScene.pushMenu(Menus.PLAY);
			}
		};


		// Explore
		button = new ImageButton(skin, SkinNames.General.EXPLORE.toString());
		mMainTable.add(button);
		tooltipListener = new TooltipListener(button, Messages.Tooltip.Menus.Main.EXPLORE);
		new ButtonListener(button, tooltipListener) {
			@Override
			protected void onPressed() {
				mMenuScene.gotoExplore();
			}
		};


		// Create
		button = new ImageButton(skin, SkinNames.General.CREATE.toString());
		mMainTable.add(button);
		tooltipListener = new TooltipListener(button, Messages.Tooltip.Menus.Main.CREATE);
		new ButtonListener(button, tooltipListener) {
			@Override
			protected void onPressed() {
				mMenuScene.gotoEditor();
			}
		};



		// Options
		button = new ImageButton(skin, SkinNames.General.OPTIONS.toString());
		mOptionTable.add(button);
		new ButtonListener(button) {
			@Override
			protected void onPressed() {
				// TODO go to options
			}
		};

		// Info
		button = new ImageButton(skin, SkinNames.General.INFO.toString());
		mInfoTable.add(button);
		new ButtonListener(button) {
			@Override
			protected void onPressed() {
				// TODO go to info
			}
		};

		// Logout
		button = new ImageButton(skin, SkinNames.General.LOGOUT.toString());
		mLogoutTable.add(button);
		new TooltipListener(button, Messages.Tooltip.Menus.Main.LOGOUT);
		new ButtonListener(button, tooltipListener) {
			@Override
			protected void onPressed() {
				mMenuScene.logout();
			}
		};
	}

	/** Option table */
	private AlignTable mOptionTable = new AlignTable();
	/** Info table */
	private AlignTable mInfoTable = new AlignTable();
	/** Logout table */
	private AlignTable mLogoutTable = new AlignTable();

}

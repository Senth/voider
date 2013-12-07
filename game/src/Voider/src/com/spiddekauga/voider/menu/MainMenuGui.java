package com.spiddekauga.voider.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.utils.scene.ui.AlignTable;
import com.spiddekauga.utils.scene.ui.ButtonListener;
import com.spiddekauga.voider.menu.MainMenu.Menus;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.resources.ResourceNames;
import com.spiddekauga.voider.resources.SkinNames;


/**
 * GUI for main menu
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class MainMenuGui extends MenuGui {
	@Override
	public void initGui() {
		super.initGui();

		mMainTable.setTableAlign(Horizontal.CENTER, Vertical.MIDDLE);
		mMainTable.setRowAlign(Horizontal.CENTER, Vertical.MIDDLE);
		mMainTable.setCellPaddingDefault((Float) SkinNames.getResource(SkinNames.General.PADDING_DEFAULT));
		mInfoTable.setPreferences(mMainTable);
		mInfoTable.setTableAlign(Horizontal.RIGHT, Vertical.TOP);
		mInfoTable.setRowAlign(Horizontal.RIGHT, Vertical.TOP);
		mOptionTable.setPreferences(mMainTable);
		mOptionTable.setTableAlign(Horizontal.RIGHT, Vertical.BOTTOM);
		mOptionTable.setRowAlign(Horizontal.RIGHT, Vertical.BOTTOM);

		getStage().addActor(mInfoTable);
		getStage().addActor(mOptionTable);

		initMainMenu();
	}

	/**
	 * Initializes the main menu
	 */
	private void initMainMenu() {
		Skin skin = ResourceCacheFacade.get(ResourceNames.UI_GENERAL);
		TextButtonStyle textPressStyle = skin.get(SkinNames.General.TEXT_BUTTON_PRESS.toString(), TextButtonStyle.class);

		// Set same size on all buttons
		float maxWidth = Gdx.graphics.getWidth() * 0.66f / 3;

		// Play
		mMainTable.row();
		Button button = new TextButton("Play", textPressStyle);
		mMainTable.add(button).setSize(maxWidth, maxWidth);
		new ButtonListener(button) {
			@Override
			protected void onPressed() {
				mMenuScene.pushMenu(Menus.PLAY);
			}
		};

		// Create
		button = new TextButton("Create", textPressStyle);
		mMainTable.add(button).setSize(maxWidth, maxWidth);
		new ButtonListener(button) {
			@Override
			protected void onPressed() {
				mMenuScene.gotoEditor();
			}
		};

		// Explore
		button = new TextButton("Explore", textPressStyle);
		mMainTable.add(button).setSize(maxWidth, maxWidth);
		new ButtonListener(button) {
			@Override
			protected void onPressed() {
				// TODO go to explore menu
			}
		};



		// Options
		button = new TextButton("Options", textPressStyle);
		mOptionTable.add(button);
		new ButtonListener(button) {
			@Override
			protected void onPressed() {
				// TODO go to options
			}
		};

		button = new TextButton("Info", textPressStyle);
		mInfoTable.add(button);
		new ButtonListener(button) {
			@Override
			protected void onPressed() {
				// TODO go to options
			}
		};
	}

	/** Option table */
	private AlignTable mOptionTable = new AlignTable();
	/** Info table */
	private AlignTable mInfoTable = new AlignTable();

}

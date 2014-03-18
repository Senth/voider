package com.spiddekauga.voider.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.utils.scene.ui.ButtonListener;
import com.spiddekauga.voider.resources.SkinNames;

/**
 * GUI for play menu
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class PlayMenuGui extends MenuGui {
	@Override
	public void initGui() {
		super.initGui();

		mMainTable.setAlign(Horizontal.CENTER, Vertical.MIDDLE);
		mMainTable.setCellPaddingDefault((Float)SkinNames.getResource(SkinNames.General.PADDING_DEFAULT));

		initMenu();
	}

	@Override
	public void resetValues() {
		super.resetValues();

		if (mMenuScene.hasResumeGame()) {
			mResumeButton.setDisabled(false);
		} else {
			mResumeButton.setDisabled(true);
		}
	}

	/**
	 * Init the menu
	 */
	private void initMenu() {
		TextButtonStyle textPressStyle = SkinNames.getResource(SkinNames.General.TEXT_BUTTON_PRESS);

		float maxWidth = Gdx.graphics.getWidth() * 0.66f / 3;

		// Resume
		Button button = new TextButton("Resume", textPressStyle);
		mResumeButton = button;
		mMainTable.add(button).setSize(maxWidth, maxWidth);
		new ButtonListener(button) {
			@Override
			protected void onPressed() {
				mMenuScene.resumeGame();
			}
		};

		// New Game
		button = new TextButton("New Game", textPressStyle);
		mMainTable.add(button).setSize(maxWidth, maxWidth);
		new ButtonListener(button) {
			@Override
			protected void onPressed() {
				mMenuScene.newGame();
			}
		};


		// Set same size of buttons
	}

	/** Resume button */
	Button mResumeButton = null;
}

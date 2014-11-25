package com.spiddekauga.voider.menu;

import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.utils.scene.ui.ButtonListener;
import com.spiddekauga.voider.repo.resource.SkinNames;
import com.spiddekauga.voider.scene.ui.UiFactory.Positions;

/**
 * GUI for play menu
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
class PlayMenuGui extends MenuGui {
	/**
	 * Public constructor
	 */
	public PlayMenuGui() {
		// Does nothing
	}

	@Override
	public void initGui() {
		super.initGui();

		mMainTable.setAlign(Horizontal.CENTER, Vertical.MIDDLE);

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
		// Resume
		Button button = mUiFactory.button.addImageWithLabel(SkinNames.General.GAME_CONTINUE, "Resume", Positions.BOTTOM, null, mMainTable, null, null);
		mResumeButton = button;
		new ButtonListener(button) {
			@Override
			protected void onPressed(Button button) {
				mMenuScene.resumeGame();
			}
		};

		// New Game
		button = mUiFactory.button.addImageWithLabel(SkinNames.General.GAME_NEW, "New Game", Positions.BOTTOM, null, mMainTable, null, null);
		new ButtonListener(button) {
			@Override
			protected void onPressed(Button button) {
				mMenuScene.newGame();
			}
		};
		mUiFactory.button.addPadding(mMainTable);
	}

	/** Resume button */
	Button mResumeButton = null;
}

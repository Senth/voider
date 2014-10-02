package com.spiddekauga.voider.menu;

import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.utils.scene.ui.ButtonListener;
import com.spiddekauga.utils.scene.ui.UiFactory.Positions;
import com.spiddekauga.voider.resources.SkinNames;

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
		Button button = mUiFactory.addImageButtonLabel(SkinNames.General.GAME_CONTINUE, "Resume", Positions.BOTTOM, mMainTable, null, null);
		mResumeButton = button;
		new ButtonListener(button) {
			@Override
			protected void onPressed(Button button) {
				mMenuScene.resumeGame();
			}
		};

		// New Game
		button = mUiFactory.addImageButtonLabel(SkinNames.General.GAME_NEW, "New Game", Positions.BOTTOM, mMainTable, null, null);
		new ButtonListener(button) {
			@Override
			protected void onPressed(Button button) {
				mMenuScene.newGame();
			}
		};
		mUiFactory.addButtonPadding(mMainTable);
	}

	/** Resume button */
	Button mResumeButton = null;
}

package com.spiddekauga.voider.game;

import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.utils.scene.ui.Label;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.resources.InternalNames;
import com.spiddekauga.voider.scene.Gui;

/**
 * GUI for the prologue scene
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
class LoadingTextSceneGui extends Gui {
	/**
	 * Constructor that sets the loading text
	 * @param loadingText text to be displayed while loading
	 */
	LoadingTextSceneGui(String loadingText) {
		mText = loadingText;
	}

	@Override
	public void initGui() {
		super.initGui();

		Skin skin = ResourceCacheFacade.get(InternalNames.UI_GENERAL);
		mLabel = new Label(mText, skin);
		mLabel.setWrap(true);
		mMainTable.setTableAlign(Horizontal.CENTER, Vertical.MIDDLE);
		mMainTable.setRowAlign(Horizontal.CENTER, Vertical.MIDDLE);
		mLabel.setColor(1, 1, 1, 0);
		mMainTable.add(mLabel);

		// Fade in
		mLabel.addAction(Actions.sequence(Actions.delay(Config.Menu.LOADING_TEXT_SCENE_ENTER_TIME), Actions.fadeIn(Config.Menu.LOADING_TEXT_SCENE_FADE_IN)));
	}

	/**
	 * Fade out the text
	 */
	public void fadeOut() {
		if (mLabel != null) {
			mLabel.addAction(Actions.sequence(Actions.fadeOut(Config.Menu.LOADING_TEXT_SCENE_FADE_OUT), Actions.delay(Config.Menu.SPLASH_SCREEN_EXIT_TIME), Actions.removeActor()));
		}
	}

	/**
	 * @return true when the text has faded out
	 */
	public boolean hasFaded() {
		return mLabel == null || mLabel.getStage() == null;
	}

	/** Text to be displayed */
	String mText;
	/** Label for the loading text */
	Label mLabel = null;
}

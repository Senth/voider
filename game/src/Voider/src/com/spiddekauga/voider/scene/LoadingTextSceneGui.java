package com.spiddekauga.voider.scene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.voider.config.ConfigIni;
import com.spiddekauga.voider.config.IC_Menu.IC_Time;

/**
 * GUI for the prologue scene
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

		mLabel = mUiFactory.text.add(mText, true, mMainTable);
		mLabel.setAlignment(Align.center);
		Color labelColor = mLabel.getColor();
		labelColor.a = 0;
		mLabel.setColor(labelColor);
		mMainTable.getCell().setFixedWidth(true).setWidth(Gdx.graphics.getWidth() * 0.8f);
		mMainTable.setAlign(Horizontal.CENTER, Vertical.MIDDLE);

		// Fade in
		IC_Time icTime = ConfigIni.getInstance().menu.time;
		mLabel.addAction(Actions.sequence(Actions.delay(icTime.getSceneEnterTime()), Actions.fadeIn(icTime.getSceneUiFadeIn())));
	}

	/**
	 * Fade out the text
	 */
	public void fadeOut() {
		IC_Time icTime = ConfigIni.getInstance().menu.time;
		mLabel.addAction(Actions.sequence(Actions.fadeOut(icTime.getSceneUiFadeOut()), Actions.delay(icTime.getSceneExitTime()),
				Actions.removeActor()));
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

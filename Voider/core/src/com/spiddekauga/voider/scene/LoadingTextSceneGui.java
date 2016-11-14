package com.spiddekauga.voider.scene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.utils.scene.ui.Background;
import com.spiddekauga.voider.config.ConfigIni;
import com.spiddekauga.voider.config.IC_Menu.IC_Time;
import com.spiddekauga.voider.repo.resource.SkinNames;

/**
 * GUI for the prologue scene

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

		setBackground(SkinNames.GeneralImages.BACKGROUND_SPACE, true);

		mLabel = mUiFactory.text.add(mText, false, mMainTable);
		mLabel.setAlignment(Align.center);
		mLabel.pack();

		float maxWidth = Gdx.graphics.getWidth() * 0.8f;
		if (mLabel.getPrefWidth() > maxWidth) {
			mMainTable.getCell().setFixedWidth(true).setWidth(maxWidth);
			mLabel.setWrap(true);
		}

		mMainTable.setColor(1, 1, 1, 0);
		mMainTable.setAlign(Horizontal.CENTER, Vertical.MIDDLE);
		mMainTable.setPad(mUiFactory.getStyles().vars.paddingInner);
		mMainTable.setBackgroundImage(new Background(mUiFactory.getStyles().color.notificationBackground));

		// Fade in
		IC_Time icTime = ConfigIni.getInstance().menu.time;
		mMainTable.addAction(Actions.sequence(Actions.delay(icTime.getSceneEnterTime()), Actions.fadeIn(icTime.getSceneUiFadeIn())));
	}

	/**
	 * Fade out the text
	 */
	void fadeOut() {
		IC_Time icTime = ConfigIni.getInstance().menu.time;
		mMainTable.addAction(Actions.sequence(Actions.fadeOut(icTime.getSceneUiFadeOut()), Actions.delay(icTime.getSceneExitTime()),
				Actions.removeActor()));
	}

	/**
	 * @return true when the text has faded out
	 */
	boolean hasFaded() {
		return mLabel == null || mMainTable.getStage() == null;
	}

	/** Text to be displayed */
	String mText;
	/** Label for the loading text */
	Label mLabel = null;
}

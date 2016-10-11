package com.spiddekauga.voider.scene;

import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Slider.SliderStyle;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.voider.config.ConfigIni;
import com.spiddekauga.voider.config.IC_Menu.IC_Time;
import com.spiddekauga.voider.repo.resource.SkinNames;

/**
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class LoadingProgressGui extends Gui {

	@Override
	public void initGui() {
		super.initGui();

		setBackground(SkinNames.GeneralImages.BACKGROUND_SPACE, true);

		SliderStyle style = SkinNames.getResource(SkinNames.General.SLIDER_LOADING_BAR);
		mSlider = new Slider(0, 100, 1, false, style);
		mMainTable.add(mSlider).setWidth(mUiFactory.getStyles().vars.rightPanelWidth);
		mMainTable.setColor(1, 1, 1, 0);
		mMainTable.setAlign(Horizontal.CENTER, Vertical.MIDDLE);

		// Fade in
		IC_Time icTime = ConfigIni.getInstance().menu.time;

		mMainTable.addAction(Actions.sequence(Actions.delay(icTime.getSceneEnterTime()), Actions.fadeIn(icTime.getSceneUiFadeIn())));
	}

	/**
	 * Set progress value
	 * @param progress in [0,100] range
	 */
	void updateProgress(float progress) {
		mSlider.setValue(progress);
	}

	/**
	 * Fade out the scene
	 */
	void fadeOut() {
		IC_Time icTime = ConfigIni.getInstance().menu.time;
		mMainTable.addAction(Actions.sequence(Actions.fadeOut(icTime.getSceneUiFadeOut()), Actions.delay(icTime.getSceneExitTime()),
				Actions.removeActor()));
	}

	/**
	 * @return true if the scene has faded
	 */
	boolean hasFaded() {
		return mMainTable.getStage() == null;
	}


	private Slider mSlider = null;
}

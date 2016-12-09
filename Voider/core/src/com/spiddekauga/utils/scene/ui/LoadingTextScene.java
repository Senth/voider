package com.spiddekauga.utils.scene.ui;

import com.badlogic.gdx.Gdx;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.config.ConfigIni;
import com.spiddekauga.voider.config.IC_Menu.IC_Time;
import com.spiddekauga.voider.repo.resource.ResourceCacheFacade;
import com.spiddekauga.voider.resources.InternalDeps;

/**
 * Loading scene that displays a text while loading. The text is automatically calculates
 * the amount of time to show the text depending on the length of the text.

 */
public class LoadingTextScene extends LoadingScene {
	/**
	 * Constructor that takes the text to be loaded
	 * @param text text to be shown while loading
	 */
	public LoadingTextScene(String text) {
		super(new LoadingTextSceneGui(text));

		IC_Time icTime = ConfigIni.getInstance().menu.time;
		mDisplayTimeDone = icTime.getDisplayTime(text);
	}

	@Override
	protected void update(float deltaTime) {
		super.update(deltaTime);

		if (!Config.Debug.SKIP_LOADING_TIME) {
			switch (mState) {
			case DISPLAY:
				mDisplayTimeCurrent += Gdx.graphics.getDeltaTime();
				if (mDisplayTimeCurrent >= mDisplayTimeDone && !ResourceCacheFacade.isLoading()) {
					getGui().fadeOut();
					mState = States.FADING;
				}
				break;


			case FADING:
				if (getGui().hasFaded()) {
					setOutcome(Outcomes.LOADING_SUCCEEDED);
				}
				break;
			}
		} else if (!ResourceCacheFacade.isLoading()) {
			setOutcome(Outcomes.LOADING_SUCCEEDED);
		}
	}

	@Override
	protected void loadResources() {
		super.loadResources();

		ResourceCacheFacade.load(this, InternalDeps.UI_GENERAL);
		ResourceCacheFacade.finishLoading();
	}

	@Override
	protected LoadingTextSceneGui getGui() {
		return (LoadingTextSceneGui) super.getGui();
	}

	@Override
	protected boolean onKeyDown(int keycode) {
		boolean handled = super.onKeyDown(keycode);

		if (!handled) {
			skip();
		}

		return true;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		boolean handled = super.touchDown(screenX, screenY, pointer, button);

		if (!handled) {
			skip();
		}

		return true;
	}

	/**
	 * Skip the scene, i.e. sets the display time to the end
	 */
	private void skip() {
		mDisplayTimeCurrent = mDisplayTimeDone;
	}

	/**
	 * States of the loading text scene
	 */
	private enum States {
		/** Displaying the loading scene */
		DISPLAY,
		/** When the text is fadingis fading */
		FADING,
	}

	/** How long the text shall be displayed */
	private float mDisplayTimeDone;
	/** How long the text has been displayed */
	private float mDisplayTimeCurrent = 0;
	private States mState = States.DISPLAY;
}

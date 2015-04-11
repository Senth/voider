package com.spiddekauga.voider.app;

import com.spiddekauga.voider.config.ConfigIni;
import com.spiddekauga.voider.config.IC_Menu.IC_Time;
import com.spiddekauga.voider.repo.resource.InternalNames;
import com.spiddekauga.voider.repo.resource.ResourceCacheFacade;
import com.spiddekauga.voider.scene.LoadingScene;

/**
 * Splash screen, displays Spiddekauga Logo
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class SplashScreen extends LoadingScene {

	/**
	 * Default constructor
	 */
	public SplashScreen() {
		super(new SplashScreenGui());

		setClearColor(0, 0, 0, 0);
	}

	@Override
	protected void loadResources() {
		super.loadResources();
		ResourceCacheFacade.load(InternalNames.IMAGE_SPLASH_SCREEN);
		ResourceCacheFacade.finishLoading();
	}

	@Override
	protected void unloadResources() {
		super.unloadResources();
		ResourceCacheFacade.unload(InternalNames.IMAGE_SPLASH_SCREEN);
	}

	@Override
	protected void update(float deltaTime) {
		super.update(deltaTime);

		IC_Time icTime = ConfigIni.getInstance().menu.time;

		switch (mState) {
		case DISPLAY:
			mDisplayTime += deltaTime;
			if (mDisplayTime >= icTime.getSplashScreenTime() && !ResourceCacheFacade.isLoading()) {
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
		mDisplayTime = ConfigIni.getInstance().menu.time.getSplashScreenTime();
	}

	@Override
	protected void render() {
		enableBlendingWithDefaults();
		super.render();
	}

	@Override
	protected SplashScreenGui getGui() {
		return (SplashScreenGui) super.getGui();
	}

	/**
	 * States of the splash screen
	 */
	private enum States {
		/** Displaying splash screen */
		DISPLAY,
		/** When the splash screen is fading */
		FADING,
	}

	/** How long the splash screen has been displayed */
	private float mDisplayTime = 0;
	/** Current splash screen state */
	States mState = States.DISPLAY;


}

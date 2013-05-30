package com.spiddekauga.voider.app;

import com.badlogic.gdx.Gdx;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.resources.ResourceNames;
import com.spiddekauga.voider.resources.UndefinedResourceTypeException;
import com.spiddekauga.voider.scene.LoadingScene;

/**
 * Splash screen, displays Spiddekauga Logo
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class SplashScreen extends LoadingScene {

	/**
	 * Default constructor
	 */
	public SplashScreen() {
		super(new SplashScreenGui());

		setClearColor(1, 1, 1, 0);

		// Load splash screen image
		ResourceCacheFacade.load(ResourceNames.SPLASH_SCREEN);

		try {
			ResourceCacheFacade.finishLoading();
		} catch (UndefinedResourceTypeException e) {
			setOutcome(Outcomes.LOADING_FAILED_UNDEFINED_TYPE);
		}

		mGui.initGui();
	}

	@Override
	protected void update() {
		super.update();

		switch (mState) {
		case DISPLAY:
			mDisplayTime += Gdx.graphics.getDeltaTime();
			if (mDisplayTime >= Config.Menu.SPLASH_SCREEN_TIME && !ResourceCacheFacade.isLoading()) {
				((SplashScreenGui)mGui).fadeOut();
				mState = States.FADING;
			}
			break;


		case FADING:
			if (((SplashScreenGui)mGui).hasFaded()) {
				setOutcome(Outcomes.LOADING_SUCCEEDED);
			}
			break;

		}
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
	/** Splash screen states */
	States mState = States.DISPLAY;


}

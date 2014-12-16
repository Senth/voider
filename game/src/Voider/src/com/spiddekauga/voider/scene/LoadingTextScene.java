package com.spiddekauga.voider.scene;

import com.badlogic.gdx.Gdx;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.repo.resource.InternalNames;
import com.spiddekauga.voider.repo.resource.ResourceCacheFacade;

/**
 * Loading scene that displays a text while loading. The text is automatically calculates
 * the amount of time to show the text depending on the length of the text.
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class LoadingTextScene extends LoadingScene {
	/**
	 * Constructor that takes the text to be loaded
	 * @param text text to be shown while loading
	 */
	public LoadingTextScene(String text) {
		super(new LoadingTextSceneGui(text));

		// TODO change loading scene text time
		mDisplayTimeMax = 7;
	}

	@Override
	protected void update(float deltaTime) {
		super.update(deltaTime);

		if (!Config.Debug.SKIP_LOADING_TIME) {
			switch (mState) {
			case DISPLAY:
				mDisplayTimeCurrent += Gdx.graphics.getDeltaTime();
				if (mDisplayTimeCurrent >= mDisplayTimeMax && !ResourceCacheFacade.isLoading()) {
					((LoadingTextSceneGui) mGui).fadeOut();
					mState = States.FADING;
				}
				break;


			case FADING:
				if (((LoadingTextSceneGui) mGui).hasFaded()) {
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

		ResourceCacheFacade.load(InternalNames.UI_GENERAL);
		ResourceCacheFacade.finishLoading();
	}

	@Override
	protected void unloadResources() {
		super.unloadResources();

		ResourceCacheFacade.unload(InternalNames.UI_GENERAL);
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
	private float mDisplayTimeMax;
	/** How long the text has been displayed */
	private float mDisplayTimeCurrent = 0;
	/** Current loading text scene state */
	private States mState = States.DISPLAY;
}

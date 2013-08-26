package com.spiddekauga.voider.game;

import com.badlogic.gdx.Gdx;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.resources.ResourceNames;
import com.spiddekauga.voider.scene.LoadingScene;
import com.spiddekauga.voider.utils.Messages;

/**
 * Loading scene that displays a text while loading. The text is automatically
 * calculates the amount of time to show the text depending on the length of the
 * text.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class LoadingTextScene extends LoadingScene {
	/**
	 * Constructor that takes the text to be loaded
	 * @param text text to be shown while loading
	 */
	public LoadingTextScene(String text) {
		super(new LoadingTextSceneGui(text));

		mDisplayTimeMax = Messages.calculateTimeToShowMessage(text);
	}

	@Override
	protected void update(float deltaTime) {
		super.update(deltaTime);

		switch (mState) {
		case DISPLAY:
			mDisplayTimeCurrent += Gdx.graphics.getDeltaTime();
			if (mDisplayTimeCurrent >= mDisplayTimeMax) {
				((LoadingTextSceneGui)mGui).fadeOut();
				mState = States.FADING;
			}
			break;


		case FADING:
			if (((LoadingTextSceneGui)mGui).hasFaded()) {
				setOutcome(Outcomes.LOADING_SUCCEEDED);
			}
			break;
		}
	}

	@Override
	protected void loadResources() {
		super.loadResources();

		ResourceCacheFacade.load(ResourceNames.UI_GENERAL);
	}

	@Override
	protected void unloadResources() {
		super.unloadResources();

		ResourceCacheFacade.unload(ResourceNames.UI_GENERAL);
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

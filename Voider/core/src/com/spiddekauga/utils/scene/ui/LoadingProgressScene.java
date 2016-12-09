package com.spiddekauga.utils.scene.ui;

import com.spiddekauga.voider.repo.resource.ResourceCacheFacade;
import com.spiddekauga.voider.resources.InternalDeps;

/**
 * Displays a progress bar for loading local resources

 */
public class LoadingProgressScene extends LoadingScene {
	/**
	 * Default constructor
	 */
	public LoadingProgressScene() {
		super(new LoadingProgressGui());
	}

	@Override
	protected void loadResources() {
		super.loadResources();

		ResourceCacheFacade.load(this, InternalDeps.UI_GENERAL);
		ResourceCacheFacade.finishLoading();
	}

	@Override
	protected void update(float deltaTime) {
		super.update(deltaTime);

		switch (mState) {
		case DISPLAY:

			if (!ResourceCacheFacade.isLoading()) {
				getGui().updateProgress(100);
				getGui().fadeOut();
				mState = States.FADING;
			} else {
				getGui().updateProgress(ResourceCacheFacade.getProgress());
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
	protected LoadingProgressGui getGui() {
		return (LoadingProgressGui) super.getGui();
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

	private States mState = States.DISPLAY;
}

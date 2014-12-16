package com.spiddekauga.voider.scene;

import com.spiddekauga.voider.repo.resource.InternalNames;
import com.spiddekauga.voider.repo.resource.ResourceCacheFacade;

/**
 * Displays a progress bar for loading local resources
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
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

		ResourceCacheFacade.load(InternalNames.UI_GENERAL);
		ResourceCacheFacade.finishLoading();
	}

	@Override
	protected void unloadResources() {
		super.unloadResources();

		ResourceCacheFacade.unload(InternalNames.UI_GENERAL);
	}

	@Override
	protected void update(float deltaTime) {
		super.update(deltaTime);

		switch (mState) {
		case DISPLAY:

			if (!ResourceCacheFacade.isLoading()) {
				((LoadingProgressGui) mGui).updateProgress(100);
				((LoadingProgressGui) mGui).fadeOut();
				mState = States.FADING;
			} else {
				((LoadingProgressGui) mGui).updateProgress(ResourceCacheFacade.getProgress());
			}
			break;
		case FADING:
			if (((LoadingProgressGui) mGui).hasFaded()) {
				setOutcome(Outcomes.LOADING_SUCCEEDED);
			}
			break;

		}
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

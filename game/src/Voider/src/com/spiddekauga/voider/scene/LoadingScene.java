package com.spiddekauga.voider.scene;

import com.spiddekauga.voider.repo.resource.ResourceCacheFacade;
import com.spiddekauga.voider.repo.resource.ResourceCorruptException;
import com.spiddekauga.voider.repo.resource.ResourceNotFoundException;

/**
 * Base class for all loading scenes
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public abstract class LoadingScene extends Scene {
	/**
	 * Constructor which takes the GUI object
	 * @param gui the GUI this loading scene is bound to.
	 */
	public LoadingScene(Gui gui) {
		super(gui);
	}

	@Override
	protected void onActivate(Outcomes outcome, Object message, Outcomes loadingOutcome) {
		super.onActivate(outcome, message, loadingOutcome);

		if (loadingOutcome == Outcomes.LOADING_SUCCEEDED) {
			if (mSceneToLoad != null) {
				mSceneToLoad.loadResources();
			}
		}
	}

	@Override
	protected void update(float deltaTime) {
		try {
			ResourceCacheFacade.update();
		} catch (ResourceNotFoundException e) {
			setOutcome(Outcomes.LOADING_FAILED_MISSING_FILE, e.toString());
		} catch (ResourceCorruptException e) {
			setOutcome(Outcomes.LOADING_FAILED_CORRUPT_FILE, e.toString());
		}
	}

	/**
	 * Sets the scene to load
	 * @param sceneToLoad the scene to load
	 */
	public void setSceneToload(Scene sceneToLoad) {
		mSceneToLoad = sceneToLoad;
	}

	/** The scene to load */
	public Scene mSceneToLoad = null;
}

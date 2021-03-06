package com.spiddekauga.utils.scene.ui;

import com.spiddekauga.voider.repo.resource.ResourceCacheFacade;
import com.spiddekauga.voider.repo.resource.ResourceCorruptException;
import com.spiddekauga.voider.repo.resource.ResourceNotFoundException;

/**
 * Base class for all loading scenes

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
	protected void update(float deltaTime) {
		try {
			ResourceCacheFacade.update();
		} catch (ResourceNotFoundException e) {
			setOutcome(Outcomes.LOADING_FAILED_MISSING_FILE, e.toString());
		} catch (ResourceCorruptException e) {
			setOutcome(Outcomes.LOADING_FAILED_CORRUPT_FILE, e.toString());
		}
	}


}

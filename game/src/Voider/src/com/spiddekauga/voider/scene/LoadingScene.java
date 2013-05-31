package com.spiddekauga.voider.scene;

import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.resources.ResourceCorruptException;
import com.spiddekauga.voider.resources.ResourceNotFoundException;

/**
 * Base class for all loading scenes
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public abstract class LoadingScene extends Scene {
	/**
	 * Constructor which takes the GUI object
	 * @param gui the GUI this loading scene is bound to.
	 */
	public LoadingScene(Gui gui){
		super(gui);
	}

	@Override
	protected void update() {
		try {
			ResourceCacheFacade.update();
		} catch (ResourceNotFoundException e) {
			setOutcome(Outcomes.LOADING_FAILED_MISSING_FILE, e.toString());
		} catch (ResourceCorruptException e) {
			setOutcome(Outcomes.LOADING_FAILED_CORRUPT_FILE, e.toString());
		}
	}

	@Override
	public final boolean hasResources() {
		return false;
	}
}

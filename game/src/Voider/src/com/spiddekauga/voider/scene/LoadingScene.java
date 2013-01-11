package com.spiddekauga.voider.scene;

import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.resources.ResourceCorruptException;
import com.spiddekauga.voider.resources.ResourceNotFoundException;
import com.spiddekauga.voider.resources.UndefinedResourceTypeException;
import com.spiddekauga.voider.ui.UiEvent;

/**
 * Base class for all loading scenes
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public abstract class LoadingScene extends Scene {

	/* (non-Javadoc)
	 * @see com.spiddekauga.voider.ui.IUiListener#onUiEvent(com.spiddekauga.voider.ui.UiEvent)
	 */
	@Override
	public void onUiEvent(UiEvent event) {
		// Does nothing
	}

	@Override
	public void update() {
		try {
			ResourceCacheFacade.update();
		} catch (UndefinedResourceTypeException e) {
			setOutcome(Outcomes.LOADING_FAILED_UNDEFINED_TYPE, e.toString());
		} catch (ResourceNotFoundException e) {
			setOutcome(Outcomes.LOADING_FAILED_MISSING_FILE, e.toString());
		} catch (ResourceCorruptException e) {
			setOutcome(Outcomes.LOADING_FAILED_CORRUPT_FILE, e.toString());
		}
	}

	/* (non-Javadoc)
	 * @see com.spiddekauga.voider.Scene#hasResources()
	 */
	@Override
	public final boolean hasResources() {
		return false;
	}
}

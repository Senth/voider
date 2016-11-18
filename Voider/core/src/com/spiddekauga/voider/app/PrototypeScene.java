package com.spiddekauga.voider.app;

import com.badlogic.gdx.Input;
import com.spiddekauga.utils.KeyHelper;
import com.spiddekauga.voider.repo.resource.ResourceCacheFacade;
import com.spiddekauga.voider.resources.InternalDeps;
import com.spiddekauga.utils.scene.ui.Scene;

/**
 * Prototype scene. Mainly for testing new things before implementing them in the game.
 */
public class PrototypeScene extends Scene {
/**
 * Default constructor
 */
public PrototypeScene() {
	super(new PrototypeGui());
}

@Override
public boolean onKeyDown(int keycode) {
	if (KeyHelper.isBackPressed(keycode)) {
		setOutcome(Outcomes.NOT_APPLICAPLE);
		return true;
	} else if (keycode == Input.Keys.F5) {
//		getGui().destroy();
//		getGui().create();
		return false;
	}

	return super.onKeyDown(keycode);
}

@Override
protected void loadResources() {
	super.loadResources();
	ResourceCacheFacade.load(this, InternalDeps.UI_GENERAL);
}

@Override
protected void onResume(Outcomes outcome, Object message, Outcomes loadingOutcome) {
	super.onResume(outcome, message, loadingOutcome);
}

}

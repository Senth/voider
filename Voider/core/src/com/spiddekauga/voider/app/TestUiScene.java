package com.spiddekauga.voider.app;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.spiddekauga.utils.KeyHelper;
import com.spiddekauga.voider.repo.resource.ResourceCacheFacade;
import com.spiddekauga.voider.resources.InternalDeps;
import com.spiddekauga.voider.scene.Scene;
import com.spiddekauga.voider.scene.ui.UiFactory;

/**
 * Scene for testing UI elements
 */
public class TestUiScene extends Scene {
/** Loading value */
float mLoadingValue = 0;
/** How much loading per second */
float LOADING_PER_SECOND = 20;

/**
 * Default constructor
 */
public TestUiScene() {
	super(new TestUiGui());
	setClearColor(UiFactory.getInstance().getStyles().color.sceneBackground);
}

@Override
public boolean onKeyDown(int keycode) {
	if (keycode == Input.Keys.F5) {
		reloadUi();
	} else if (KeyHelper.isBackPressed(keycode)) {
		Gdx.app.exit();
	} else if (keycode == Input.Keys.F11) {
		getGui().showProgressBar("This is a progress window that is quite long...\nMultiple lines...");
		getGui().updateProgressBar(50, "50 / 100");
	} else if (keycode == Input.Keys.F12) {
		getGui().hideProgressBar();
	}

	return false;
}

@Override
public void update(float deltaTime) {
	mLoadingValue += LOADING_PER_SECOND * deltaTime;
	if (mLoadingValue > 100) {
		mLoadingValue = 0;
	}

	((TestUiGui) getGui()).setLoadingBar(mLoadingValue);
	((TestUiGui) getGui()).setHealthBar(100 - mLoadingValue);
}

@Override
protected void loadResources() {
	super.loadResources();
	ResourceCacheFacade.load(this, InternalDeps.UI_GENERAL);
	ResourceCacheFacade.load(this, InternalDeps.UI_GAME);
}

@Override
protected void onActivate(Outcomes outcome, Object message, Outcomes loadingOutcome) {
	super.onActivate(outcome, message, loadingOutcome);
}

/**
 * Reload UI elements
 */
private void reloadUi() {
	ResourceCacheFacade.reload(InternalDeps.UI_GENERAL);

	getGui().dispose();
	getGui().initGui();
}
}

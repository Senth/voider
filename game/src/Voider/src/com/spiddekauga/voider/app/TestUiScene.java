package com.spiddekauga.voider.app;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.spiddekauga.utils.KeyHelper;
import com.spiddekauga.utils.scene.ui.UiFactory;
import com.spiddekauga.voider.repo.resource.InternalNames;
import com.spiddekauga.voider.repo.resource.ResourceCacheFacade;
import com.spiddekauga.voider.scene.Scene;

/**
 * Scene for testing UI elements
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class TestUiScene extends Scene {
	/**
	 * Default constructor
	 */
	public TestUiScene() {
		super(new TestUiGui());
		setClearColor(UiFactory.getInstance().getStyles().color.sceneBackground);
	}

	@Override
	protected void loadResources() {
		super.loadResources();
		ResourceCacheFacade.load(InternalNames.UI_GENERAL);
		ResourceCacheFacade.load(InternalNames.UI_GAME);
	}

	@Override
	protected void unloadResources() {
		super.unloadResources();
		ResourceCacheFacade.unload(InternalNames.UI_GENERAL);
		ResourceCacheFacade.unload(InternalNames.UI_GAME);
	}

	@Override
	protected void onActivate(Outcomes outcome, Object message) {
		super.onActivate(outcome, message);
	}

	@Override
	public boolean onKeyDown(int keycode) {
		if (keycode == Input.Keys.F5) {
			reloadUi();
		} else if (KeyHelper.isBackPressed(keycode)) {
			Gdx.app.exit();
		} else if (keycode == Input.Keys.F11) {
			mGui.showProgressBar("This is a progress window that is quite long...\nMultiple lines...");
			mGui.updateProgressBar(50, "50 / 100");
		} else if (keycode == Input.Keys.F12) {
			mGui.hideProgressBar();
		}

		return false;
	}

	@Override
	public void update(float deltaTime) {
		mLoadingValue += LOADING_PER_SECOND * deltaTime;
		if (mLoadingValue > 100) {
			mLoadingValue = 0;
		}

		((TestUiGui) mGui).setLoadingBar(mLoadingValue);
		((TestUiGui) mGui).setHealthBar(100 - mLoadingValue);
	}

	/**
	 * Reload UI elements
	 */
	private void reloadUi() {
		ResourceCacheFacade.reload(InternalNames.UI_GENERAL);

		mGui.dispose();
		mGui.initGui();
	}

	/** Loading value */
	float mLoadingValue = 0;
	/** How much loading per second */
	float LOADING_PER_SECOND = 20;
}

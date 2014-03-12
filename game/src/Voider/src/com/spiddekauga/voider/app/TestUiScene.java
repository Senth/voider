package com.spiddekauga.voider.app;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.spiddekauga.utils.KeyHelper;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.resources.InternalNames;
import com.spiddekauga.voider.scene.Scene;

/**
 * Scene for testing UI elements
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class TestUiScene extends Scene {
	/**
	 * Default constructor
	 */
	public TestUiScene() {
		super(new TestUiGui());
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
		mGui.initGui();
	}

	@Override
	public boolean keyDown(int keycode) {
		if (keycode == Input.Keys.F5) {
			reloadUi();
		} else if (KeyHelper.isBackPressed(keycode)) {
			Gdx.app.exit();
		}

		return false;
	}

	@Override
	public void update(float deltaTime) {
		mLoadingValue += LOADING_PER_SECOND * deltaTime;
		if (mLoadingValue > 100) {
			mLoadingValue = 0;
		}

		((TestUiGui)mGui).setLoadingBar(mLoadingValue);
		((TestUiGui)mGui).setHealthBar(100-mLoadingValue);
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

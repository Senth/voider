package com.spiddekauga.voider.menu;

import com.spiddekauga.utils.KeyHelper;
import com.spiddekauga.voider.repo.resource.InternalNames;
import com.spiddekauga.voider.repo.resource.ResourceCacheFacade;
import com.spiddekauga.voider.resources.InternalDeps;
import com.spiddekauga.voider.scene.Gui;
import com.spiddekauga.voider.scene.Scene;

/**
 * Menu scene
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public abstract class MenuScene extends Scene {
	/**
	 * @param gui
	 */
	protected MenuScene(Gui gui) {
		super(gui);
	}

	@Override
	protected void loadResources() {
		super.loadResources();

		ResourceCacheFacade.load(this, InternalDeps.UI_GENERAL);
		ResourceCacheFacade.load(this, InternalNames.MUSIC_TITLE);
		ResourceCacheFacade.load(this, InternalDeps.UI_SFX);
	}

	@Override
	protected boolean onKeyDown(int keycode) {
		if (KeyHelper.isBackPressed(keycode)) {
			endScene();
			return true;
		}

		return super.onKeyDown(keycode);
	}
}

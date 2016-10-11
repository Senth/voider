package com.spiddekauga.voider.menu;

import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.spiddekauga.utils.scene.ui.ButtonListener;
import com.spiddekauga.voider.repo.resource.SkinNames;
import com.spiddekauga.voider.scene.Gui;
import com.spiddekauga.voider.scene.Scene;
import com.spiddekauga.voider.scene.SceneSwitcher;

/**
 * Common class for all menus used by Main Menu Scene
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
class MenuGui extends Gui {
	@Override
	public void initGui() {
		super.initGui();

		setBackground(SkinNames.GeneralImages.BACKGROUND_SPACE, true);
	}

	/**
	 * Adds a back button
	 */
	protected void addBackButton() {
		ButtonListener buttonListener = new ButtonListener() {
			@Override
			protected void onPressed(Button button) {
				Scene scene = SceneSwitcher.getActiveScene(true);
				if (scene != null) {
					scene.endScene();
				}
			}
		};

		addActor(mUiFactory.button.createBackButton(buttonListener));
	}
}

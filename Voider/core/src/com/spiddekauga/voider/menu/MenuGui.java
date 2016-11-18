package com.spiddekauga.voider.menu;

import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.spiddekauga.utils.scene.ui.ButtonListener;
import com.spiddekauga.voider.repo.resource.SkinNames;
import com.spiddekauga.utils.scene.ui.Gui;
import com.spiddekauga.utils.scene.ui.Scene;
import com.spiddekauga.utils.scene.ui.SceneSwitcher;

/**
 * Common class for all menus used by Main Menu Scene
 */
class MenuGui extends Gui {
@Override
public void onCreate() {
	super.onCreate();

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

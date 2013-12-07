package com.spiddekauga.voider.editor;

import com.spiddekauga.utils.KeyHelper;
import com.spiddekauga.voider.menu.MainMenu;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.resources.ResourceNames;
import com.spiddekauga.voider.scene.Scene;
import com.spiddekauga.voider.scene.SceneSwitcher;

/**
 * Scene for selecting which editor to go to
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class EditorSelectionScene extends Scene {
	/**
	 * Default constructor
	 */
	public EditorSelectionScene() {
		super(new EditorSelectionGui());

		((EditorSelectionGui)mGui).setEditorSelectionScene(this);
	}

	@Override
	protected void loadResources() {
		super.loadResources();
		ResourceCacheFacade.load(ResourceNames.UI_EDITOR_BUTTONS);
		ResourceCacheFacade.load(ResourceNames.UI_GENERAL);
	}

	@Override
	protected void unloadResources() {
		super.unloadResources();
		ResourceCacheFacade.unload(ResourceNames.UI_EDITOR_BUTTONS);
		ResourceCacheFacade.unload(ResourceNames.UI_GENERAL);
	}

	@Override
	public boolean keyDown(int keycode) {
		if (KeyHelper.isBackPressed(keycode)) {
			SceneSwitcher.returnTo(MainMenu.class);
			return true;
		}
		return false;
	}

	/**
	 * Go to level editor
	 */
	void gotoLevelEditor() {
		setNextScene(new LevelEditor());
		setOutcome(Outcomes.NOT_APPLICAPLE);
	}

	/**
	 * Go to enemy editor
	 */
	void gotoEnemyEditor() {
		setNextScene(new EnemyEditor());
		setOutcome(Outcomes.NOT_APPLICAPLE);
	}

	/**
	 * Go to bullet editor
	 */
	void gotoBulletEditor() {
		setNextScene(new BulletEditor());
		setOutcome(Outcomes.NOT_APPLICAPLE);
	}
}

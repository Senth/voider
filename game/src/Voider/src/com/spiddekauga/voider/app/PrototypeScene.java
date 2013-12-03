package com.spiddekauga.voider.app;

import com.badlogic.gdx.Input;
import com.spiddekauga.utils.KeyHelper;
import com.spiddekauga.voider.game.actors.EnemyActorDef;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.resources.ResourceNames;
import com.spiddekauga.voider.scene.Scene;

/**
 * Prototype scene. Mainly for testing new things before implementing
 * them in the game.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class PrototypeScene extends Scene {
	/**
	 * Default constructor
	 */
	public PrototypeScene() {
		super(new PrototypeGui());

		((PrototypeGui)mGui).setScene(this);
	}

	@Override
	protected void loadResources() {
		super.loadResources();
		ResourceCacheFacade.load(ResourceNames.UI_GENERAL);
		ResourceCacheFacade.loadAllOf(this, EnemyActorDef.class, true);
	}

	@Override
	protected void unloadResources() {
		super.unloadResources();
		ResourceCacheFacade.unload(ResourceNames.UI_GENERAL);
		ResourceCacheFacade.unloadAllOf(this, EnemyActorDef.class, true);
	}

	@Override
	protected void onActivate(Outcomes outcomes, Object message) {
		mGui.initGui();
	}

	@Override
	public boolean keyDown(int keycode) {
		if (KeyHelper.isBackPressed(keycode)) {
			setOutcome(Outcomes.NOT_APPLICAPLE);
			return true;
		} else if (keycode == Input.Keys.F5) {
			mGui.dispose();
			mGui.initGui();
		}

		return false;
	}

	@Override
	public void update(float deltaTime) {
		super.update(deltaTime);

		// TODO
	}
}

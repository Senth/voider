package com.spiddekauga.voider.app;

import com.badlogic.gdx.Input;
import com.spiddekauga.utils.KeyHelper;
import com.spiddekauga.voider.repo.resource.ExternalTypes;
import com.spiddekauga.voider.repo.resource.InternalNames;
import com.spiddekauga.voider.repo.resource.ResourceCacheFacade;
import com.spiddekauga.voider.resources.InternalDeps;
import com.spiddekauga.voider.scene.Scene;

/**
 * Prototype scene. Mainly for testing new things before implementing them in the game.
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class PrototypeScene extends Scene {
	/**
	 * Default constructor
	 */
	public PrototypeScene() {
		super(new PrototypeGui());
	}

	@Override
	protected void loadResources() {
		super.loadResources();
		ResourceCacheFacade.load(InternalDeps.UI_GENERAL);
		ResourceCacheFacade.load(BACKGROUND_TO_USE);
		ResourceCacheFacade.loadAllOf(this, ExternalTypes.ENEMY_DEF, true);
	}

	@Override
	protected void unloadResources() {
		super.unloadResources();
		ResourceCacheFacade.unload(InternalDeps.UI_GENERAL);
		ResourceCacheFacade.unload(BACKGROUND_TO_USE);
	}

	@Override
	protected void onActivate(Outcomes outcome, Object message, Outcomes loadingOutcome) {
		super.onActivate(outcome, message, loadingOutcome);
	}

	@Override
	public boolean onKeyDown(int keycode) {
		if (KeyHelper.isBackPressed(keycode)) {
			setOutcome(Outcomes.NOT_APPLICAPLE);
			return true;
		} else if (keycode == Input.Keys.F5) {
			getGui().dispose();
			getGui().initGui();
		}

		return false;
	}

	@Override
	public void update(float deltaTime) {
		super.update(deltaTime);
	}

	/** Background to use for the prototype scene */
	static final InternalNames BACKGROUND_TO_USE = InternalNames.LEVEL_BACKGROUND_TUNNELS_720;
}

package com.spiddekauga.voider.editor;

import com.badlogic.gdx.Input;
import com.spiddekauga.utils.KeyHelper;
import com.spiddekauga.voider.game.Themes;
import com.spiddekauga.voider.repo.resource.InternalNames;
import com.spiddekauga.voider.repo.resource.ResourceCacheFacade;
import com.spiddekauga.voider.scene.Scene;

/**
 * Scene for selecting theme on a mobile device
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class ThemeSelectScene extends Scene {
	/**
	 * Sets the theme to display
	 * @param theme the theme to display in fullscreen
	 */
	public ThemeSelectScene(Themes theme) {
		super(new ThemeSelectGui());
		mTheme = theme;

		((ThemeSelectGui) mGui).setScene(this);
	}

	@Override
	protected void loadResources() {
		ResourceCacheFacade.load(InternalNames.UI_EDITOR);
		ResourceCacheFacade.load(mTheme.getDependency());

		super.loadResources();
	}

	@Override
	protected void unloadResources() {
		ResourceCacheFacade.unload(InternalNames.UI_EDITOR);
		ResourceCacheFacade.unload(mTheme.getDependency());

		super.unloadResources();
	}

	@Override
	protected boolean onKeyDown(int keycode) {
		if (KeyHelper.isBackPressed(keycode)) {
			cancel();
		} else if (keycode == Input.Keys.ENTER) {
			select();
		}

		return super.onKeyDown(keycode);
	}

	/**
	 * @return theme to select
	 */
	Themes getTheme() {
		return mTheme;
	}

	/**
	 * Cancel selection
	 */
	void cancel() {
		setOutcome(Outcomes.THEME_SELECT_CANCEL);
	}

	/**
	 * Select the theme
	 */
	void select() {
		setOutcome(Outcomes.THEME_SELECTED, mTheme);
	}

	/** Theme to display */
	private Themes mTheme;
}

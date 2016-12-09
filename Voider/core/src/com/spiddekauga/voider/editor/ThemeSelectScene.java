package com.spiddekauga.voider.editor;

import com.badlogic.gdx.Input;
import com.spiddekauga.utils.KeyHelper;
import com.spiddekauga.voider.game.Themes;
import com.spiddekauga.voider.repo.resource.ResourceCacheFacade;
import com.spiddekauga.voider.resources.InternalDeps;
import com.spiddekauga.utils.scene.ui.Scene;

/**
 * Scene for selecting theme on a mobile device
 */
public class ThemeSelectScene extends Scene {
/** Theme to display */
private Themes mTheme;

/**
 * Sets the theme to display
 * @param theme the theme to display in fullscreen
 */
public ThemeSelectScene(Themes theme) {
	super(new ThemeSelectGui());
	mTheme = theme;

	((ThemeSelectGui) getGui()).setScene(this);
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

@Override
protected void loadResources() {
	ResourceCacheFacade.load(this, InternalDeps.UI_EDITOR);
	ResourceCacheFacade.load(this, mTheme.getDependency());

	super.loadResources();
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

/**
 * @return theme to select
 */
Themes getTheme() {
	return mTheme;
}
}

package com.spiddekauga.utils.scene.ui;

/**
 * Manually hides/shows the toggle actors
 */
public class HideManual extends GuiHider {
/** If the GUI objects shall be hidden or shown */
private boolean mShow = true;

/**
 * Default constructor, shows the GUI by default
 */
public HideManual() {

}

/**
 * Sets if the GUI objects should be hidden or shown
 * @param visible set to true to show, false to hide
 */
public void setVisibility(boolean visible) {
	if (visible) {
		show();
	} else {
		hide();
	}
}

/**
 * Shows all the toggle actors
 */
public void show() {
	mShow = true;
	updateToggleActors();
}

/**
 * Hides all the actors
 */
public void hide() {
	mShow = false;
	updateToggleActors();
}

@Override
protected boolean shallShowActors() {
	return mShow;
}
}

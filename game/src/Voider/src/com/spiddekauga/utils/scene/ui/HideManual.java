package com.spiddekauga.utils.scene.ui;

/**
 * Manually hides/shows the toggle actors
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class HideManual extends GuiHider {
	/**
	 * Default constructor, shows the GUI by default
	 */
	public HideManual() {

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

	/** If the GUI objects shall be hidden or shown */
	private boolean mShow = true;
}

package com.spiddekauga.voider.app;

import com.spiddekauga.voider.scene.Scene;

/**
 * Main menu of the scene
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class MainMenu extends Scene {
	/**
	 * Default constructor for main menu
	 */
	public MainMenu() {
		super(new MainMenuGui());
	}

	@Override
	protected void update() {

	}

	@Override
	public boolean hasResources() {
		return true;
	}

	@Override
	public void loadResources() {

	}

	@Override
	public void unloadResources() {

	}

	@Override
	public void onActivate(Outcomes outcome, String message) {

	}
}

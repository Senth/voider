package com.spiddekauga.voider.app;

import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.spiddekauga.voider.scene.Gui;

/**
 * GUI for main menu
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class MainMenuGui extends Gui {
	@Override
	public void initGui() {

	}

	private MenuButtons menu = new MenuButtons();

	/**
	 * Menu buttons
	 */
	@SuppressWarnings("javadoc")
	private static class MenuButtons {
		Button resume = null;
		Button campaign = null;
		Button downloaded = null;
		Button explore = null;
		Button options = null;
	}

}

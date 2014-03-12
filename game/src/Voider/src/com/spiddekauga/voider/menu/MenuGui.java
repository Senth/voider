package com.spiddekauga.voider.menu;

import com.spiddekauga.voider.scene.Gui;

/**
 * Common class for all menus used by Main Menu Scene
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class MenuGui extends Gui {
	/**
	 * Sets the menu scene
	 * @param menuScene the menu scene to set
	 */
	void setMenuScene(MainMenu menuScene) {
		mMenuScene = menuScene;
	}

	/** The main menu scene */
	protected MainMenu mMenuScene = null;
}

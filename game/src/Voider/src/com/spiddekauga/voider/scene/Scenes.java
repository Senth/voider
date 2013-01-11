/**
 * @file
 * @version 0.1
 * Copyright © Matteus Magnusson
 *
 * @section LICENSE
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details at
 * http://www.gnu.org/copyleft/gpl.html
 */
package com.spiddekauga.voider.scene;

/**
 * All scenes for the game. E.g. Menus, Games, Editors.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public enum Scenes {
	/**
	 * The splash screen, starts when the game is started
	 */
	SPLASH,
	/**
	 * Main menu, where one can create new games etc.
	 */
	MAIN_MENU,
	/**
	 * The actual gameplay
	 */
	GAME,
	/**
	 * Arbitrary text to display. Used for story before and after a level.
	 * For before a level this also includes all the loading, meaning no
	 * loading screen will be called.
	 */
	TEXT,
	/**
	 * Loading screen.
	 */
	LOADING,
	/**
	 * When editing a level
	 */
	EDITOR_LEVEL,
	/**
	 * When editing a campaign
	 */
	EDITOR_CAMPAIGN,
	/**
	 * When editing enemies
	 */
	EDITOR_ENEMY,
	/**
	 * When editing bosses
	 */
	EDITOR_BOSS
}

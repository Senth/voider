package com.spiddekauga.voider.resources;

import java.util.ArrayList;

import com.spiddekauga.voider.game.actors.PlayerActorDef;

/**
 * Checks if all resources are available.
 * @todo download the resource instead of creating them...
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class ResourceChecker {
	/**
	 * Check for all necessary resources, like player ships. If these resources
	 * are not found, this method will create new type of those resources
	 */
	public static void checkAndCreateResources() {
		if (!checkPlayerShips()) {
			createPlayerShips();
		}
	}

	/**
	 * Checks for player ships
	 * @return true if a player ship was found
	 */
	private static boolean checkPlayerShips() {
		ArrayList<ResourceItem> ships = ResourceDatabase.getAllExistingResource(PlayerActorDef.class);

		/** @todo check for specific ships */

		return !ships.isEmpty();
	}

	/**
	 * Creates player ships
	 */
	private static void createPlayerShips() {
		PlayerActorDef playerActorDef = new PlayerActorDef();
		playerActorDef.setRevision(1);
		ResourceSaver.save(playerActorDef);
	}

}

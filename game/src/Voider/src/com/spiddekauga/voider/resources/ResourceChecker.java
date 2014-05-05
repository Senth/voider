package com.spiddekauga.voider.resources;

import java.util.ArrayList;
import java.util.UUID;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.game.actors.PlayerActorDef;
import com.spiddekauga.voider.repo.ResourceLocalRepo;

/**
 * Checks if all resources are available.
 * @todo download the resource instead of creating them...
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class ResourceChecker {
	/**
	 * Check for all necessary resources, like player ships. If these resources
	 * are not found, this method will create new type of those resources
	 */
	public static void checkAndCreateResources() {
		createFolders();

		if (isMissingPlayerShips()) {
			createPlayerShips();
		}
	}

	/**
	 * Create folders
	 */
	private static void createFolders() {
		FileHandle folder = Gdx.files.external(Config.File.STORAGE);

		if (!folder.exists()) {
			folder.mkdirs();
		}
	}

	/**
	 * Checks for player ships
	 * @return true if a player ship was found
	 */
	private static boolean isMissingPlayerShips() {
		ArrayList<UUID> ships = ResourceLocalRepo.getAll(ExternalTypes.PLAYER_DEF);

		/** @todo check for specific ships */

		boolean missing = ships.isEmpty();

		return missing;
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

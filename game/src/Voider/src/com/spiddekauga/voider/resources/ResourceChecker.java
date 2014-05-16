package com.spiddekauga.voider.resources;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.UUID;

import com.spiddekauga.voider.game.actors.PlayerActorDef;
import com.spiddekauga.voider.repo.ResourceLocalRepo;
import com.spiddekauga.voider.utils.User;
import com.spiddekauga.voider.utils.User.UserEvents;

/**
 * Checks if all resources are available.
 * @todo download the resource instead of creating them...
 * @todo copy them from the local storage instead...
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class ResourceChecker implements Observer {
	/**
	 * Private constructor to enforce singleton usage
	 */
	private ResourceChecker() {
		User.getGlobalUser().addObserver(this);
	}

	/**
	 * Initializes the resources checker
	 */
	public static void init() {
		if (mInstance == null) {
			mInstance = new ResourceChecker();
		}
	}

	/**
	 * Disposes the resource checked
	 */
	public static void dispose() {
		if (mInstance != null) {
			User.getGlobalUser().deleteObserver(mInstance);
			mInstance = null;
		}
	}

	/**
	 * Check for all necessary resources, like player ships. If these resources
	 * are not found, this method will create new type of those resources
	 */
	private void checkAndCreateResources() {
		if (isMissingPlayerShips()) {
			createPlayerShips();
		}
	}

	/**
	 * Checks for player ships
	 * @return true if a player ship was found
	 */
	private boolean isMissingPlayerShips() {
		ArrayList<UUID> ships = ResourceLocalRepo.getAll(ExternalTypes.PLAYER_DEF);

		/** @todo check for specific ships */

		boolean missing = ships.isEmpty();

		return missing;
	}

	/**
	 * Creates player ships
	 */
	private void createPlayerShips() {
		PlayerActorDef playerActorDef = new PlayerActorDef();
		playerActorDef.setRevision(1);
		ResourceLocalRepo.save(playerActorDef);
	}

	@Override
	public void update(Observable object, Object arg) {
		if (object instanceof User) {
			if (arg instanceof UserEvents) {
				switch ((UserEvents)arg) {
				case LOGIN:
					checkAndCreateResources();
					break;

				default:
					break;
				}
			}
		}
	}


	/** This instance */
	private static ResourceChecker mInstance = null;
}

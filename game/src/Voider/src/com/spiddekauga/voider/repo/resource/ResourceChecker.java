package com.spiddekauga.voider.repo.resource;

import java.util.ArrayList;
import java.util.UUID;

import com.spiddekauga.voider.game.actors.PlayerActorDef;
import com.spiddekauga.voider.utils.event.EventDispatcher;
import com.spiddekauga.voider.utils.event.EventTypes;
import com.spiddekauga.voider.utils.event.GameEvent;
import com.spiddekauga.voider.utils.event.IEventListener;

/**
 * Checks if all resources are available.
 * @todo download the resource instead of creating them...
 * @todo copy them from the local storage instead...
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class ResourceChecker implements IEventListener {
	/**
	 * Private constructor to enforce singleton usage
	 */
	private ResourceChecker() {
		EventDispatcher.getInstance().connect(EventTypes.USER_LOGIN, this);
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
			EventDispatcher.getInstance().disconnect(EventTypes.USER_LOGIN, mInstance);
			mInstance = null;
		}
	}

	/**
	 * Check for all necessary resources, like player ships. If these resources are not
	 * found, this method will create new type of those resources
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
		playerActorDef.setId(SHIP_REGULAR_ID);
		ResourceLocalRepo.save(playerActorDef);
		ResourceLocalRepo.removeRevisions(playerActorDef.getId());
	}

	@Override
	public void handleEvent(GameEvent event) {
		switch (event.type) {
		case USER_LOGIN:
			checkAndCreateResources();
			break;

		default:
			break;
		}
	}

	// Player ships
	/** Regular ship */
	private static final UUID SHIP_REGULAR_ID = UUID.fromString("6d5f7bd4-e947-4d91-9c57-ce982f0542d0");
	/** This instance */
	private static ResourceChecker mInstance = null;
}

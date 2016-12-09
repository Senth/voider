package com.spiddekauga.voider.repo.resource;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.spiddekauga.voider.Config.Debug;
import com.spiddekauga.voider.Config.Debug.Builds;
import com.spiddekauga.voider.utils.event.EventDispatcher;
import com.spiddekauga.voider.utils.event.EventTypes;
import com.spiddekauga.voider.utils.event.GameEvent;
import com.spiddekauga.voider.utils.event.IEventListener;

import java.util.UUID;

/**
 * Checks if all resources are available.
 * @todo download the resource instead of creating them...
 * @todo copy them from the local storage instead...
 */
public class ResourceChecker implements IEventListener {
// Player ships
private static final UUID SHIP_FIGHTER_ID = UUID.fromString("7e21267c-839d-4e6f-963a-a193d4d607d4");
private static ResourceChecker mInstance = null;

/**
 * Private constructor to enforce singleton usage
 */
private ResourceChecker() {
	EventDispatcher.getInstance().connect(EventTypes.USER_LOGGED_IN, this);
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
		EventDispatcher.getInstance().disconnect(EventTypes.USER_LOGGED_IN, mInstance);
		mInstance = null;
	}
}

@Override
public void handleEvent(GameEvent event) {
	switch (event.type) {
	case USER_LOGGED_IN:
		checkAndCreateResources();
		break;

	default:
		break;
	}
}

/**
 * Check for all necessary resources, like player ships. If these resources are not found, this
 * method will create new type of those resources
 */
private void checkAndCreateResources() {
	if (isMissingPlayerShips()) {
		createPlayerShips();
	} else if (Debug.isBuildOrBelow(Builds.NIGHTLY_RELEASE) && Debug.isBuildOrAbove(Builds.DEV_SERVER)) {
		// Remove player ship than add it again... Added a wrong gameVersion first time
		removePlayerShips();
		createPlayerShips();
	}
}

/**
 * Checks for player ships
 * @return true if a player ship was found
 */
private boolean isMissingPlayerShips() {
	return !ResourceLocalRepo.exists(SHIP_FIGHTER_ID);
}

/**
 * Creates player ships
 */
private void createPlayerShips() {
	// Copy ship from internal resources to external resources
	FileHandle internal = Gdx.files.internal("export/" + SHIP_FIGHTER_ID);
	FileHandle external = Gdx.files.external(ResourceLocalRepo.getFilepath(SHIP_FIGHTER_ID));
	internal.copyTo(external);
	;

	// Add to Local DB
	ResourceLocalRepo.addDownloaded(SHIP_FIGHTER_ID, ExternalTypes.PLAYER_DEF);
}

/**
 * Remove player ship
 */
private void removePlayerShips() {
	ResourceLocalRepo.remove(SHIP_FIGHTER_ID);
}
}

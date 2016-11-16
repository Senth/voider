package com.spiddekauga.voider.scene.ui;

import com.spiddekauga.voider.network.misc.Motd;
import com.spiddekauga.voider.repo.misc.SettingRepo;
import com.spiddekauga.voider.repo.misc.SettingRepo.SettingInfoRepo;
import com.spiddekauga.voider.repo.user.User;
import com.spiddekauga.voider.utils.event.EventDispatcher;
import com.spiddekauga.voider.utils.event.EventTypes;
import com.spiddekauga.voider.utils.event.GameEvent;
import com.spiddekauga.voider.utils.event.IEventListener;
import com.spiddekauga.voider.utils.event.MotdEvent;
import com.spiddekauga.voider.utils.event.ServerRestoreEvent;
import com.spiddekauga.voider.utils.event.UpdateEvent;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Listens to events and displays that information on the screen
 */
public class InfoDisplayer implements IEventListener {
private static InfoDisplayer mInstance = new InfoDisplayer();
private UiFactory mUiFactory = UiFactory.getInstance();

/**
 * Default constructor
 */
private InfoDisplayer() {
	EventDispatcher eventDispatcher = EventDispatcher.getInstance();
	eventDispatcher.connect(EventTypes.MOTD_CURRENT, this);
	eventDispatcher.connect(EventTypes.MOTD_NEW, this);
	eventDispatcher.connect(EventTypes.UPDATE_AVAILABLE, this);
	eventDispatcher.connect(EventTypes.UPDATE_REQUIRED, this);
	eventDispatcher.connect(EventTypes.SERVER_RESTORE, this);
}

/**
 * @return instance of this class
 */
public static InfoDisplayer getInstance() {
	return mInstance;
}

@Override
public void handleEvent(GameEvent event) {
	switch (event.type) {
	case MOTD_CURRENT:
	case MOTD_NEW:
		handleMotd((MotdEvent) event);
		break;

	case UPDATE_AVAILABLE:
	case UPDATE_REQUIRED:
		handleUpdateEvent((UpdateEvent) event);
		break;

	case SERVER_RESTORE:
		handleServerRestoreEvent((ServerRestoreEvent) event);
		break;

	default:
		// Not used
		break;
	}
}

/**
 * Handle MOTD
 */
private void handleMotd(MotdEvent event) {
	List<Motd> filteredMotds = event.motds;

	// Remove MOTD we already have shown the user
	if (User.getGlobalUser().isLoggedIn()) {
		SettingInfoRepo infoRepo = SettingRepo.getInstance().info();
		filteredMotds = infoRepo.filterMotds(event.motds);

		// Sort these by created date (oldest first)
		Collections.sort(event.motds, new Comparator<Motd>() {
			@Override
			public int compare(Motd o1, Motd o2) {
				if (o1.created.before(o2.created)) {
					return -1;
				}
				if (o1.created.after(o2.created)) {
					return 1;
				}
				return 0;
			}
		});
	}

	// Show MOTDs
	for (Motd motd : filteredMotds) {
		mUiFactory.msgBox.motd(motd);
	}
}

/**
 * Handle update event
 */
private void handleUpdateEvent(UpdateEvent event) {
	mUiFactory.msgBox.updateMessage(event);
}

/**
 * Handle server restored
 */
private void handleServerRestoreEvent(ServerRestoreEvent event) {
	mUiFactory.msgBox.serverRestored(event);
}
}

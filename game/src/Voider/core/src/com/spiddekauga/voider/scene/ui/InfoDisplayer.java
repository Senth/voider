package com.spiddekauga.voider.scene.ui;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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

/**
 * Listens to events and displays that information on the screen
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class InfoDisplayer implements IEventListener {
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

	@Override
	public void handleEvent(GameEvent event) {
		switch (event.type) {
		case MOTD_CURRENT:
		case MOTD_NEW:
			handleMotd(event);
			break;

		case UPDATE_AVAILABLE:
		case UPDATE_REQUIRED:
			handleUpdateEvent(event);
			break;

		case SERVER_RESTORE:
			handleServerRestoreEvent(event);
			break;

		default:
			// Not used
			break;
		}
	}

	/**
	 * Handle MOTD
	 * @param event
	 */
	private void handleMotd(GameEvent event) {
		if (event instanceof MotdEvent) {
			MotdEvent motdEvent = (MotdEvent) event;

			List<Motd> filteredMotds = motdEvent.motds;

			// Remove MOTD we already have shown the user
			if (User.getGlobalUser().isLoggedIn()) {
				SettingInfoRepo infoRepo = SettingRepo.getInstance().info();
				filteredMotds = infoRepo.filterMotds(motdEvent.motds);

				// Sort these by created date (oldest first)
				Collections.sort(motdEvent.motds, new Comparator<Motd>() {
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
	}

	/**
	 * Handle update event
	 * @param event
	 */
	private void handleUpdateEvent(GameEvent event) {
		if (event instanceof UpdateEvent) {
			mUiFactory.msgBox.updateMessage((UpdateEvent) event);
		}
	}

	/**
	 * Handle server restored
	 * @param event
	 */
	private void handleServerRestoreEvent(GameEvent event) {
		if (event instanceof ServerRestoreEvent) {
			// TODO
		}
	}

	/**
	 * @return instance of this class
	 */
	public static InfoDisplayer getInstance() {
		return mInstance;
	}


	private UiFactory mUiFactory = UiFactory.getInstance();

	private static InfoDisplayer mInstance = new InfoDisplayer();
}

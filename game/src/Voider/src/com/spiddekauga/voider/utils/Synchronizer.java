package com.spiddekauga.voider.utils;

import java.util.Observable;
import java.util.Observer;

import com.spiddekauga.voider.network.entities.ChatMessage;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.ISuccessStatuses;
import com.spiddekauga.voider.network.entities.method.IMethodEntity;
import com.spiddekauga.voider.repo.ICallerResponseListener;
import com.spiddekauga.voider.repo.ResourceRepo;
import com.spiddekauga.voider.scene.SceneSwitcher;
import com.spiddekauga.voider.server.IMessageListener;
import com.spiddekauga.voider.server.MessageGateway;

/**
 * Listens to server synchronize events when to synchronize. Also checks
 * synchronize everything when user logs in
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class Synchronizer implements IMessageListener, Observer, ICallerResponseListener {
	/**
	 * Initializes the synchronizer. Private constructor to enforce
	 * singleton usage
	 */
	private Synchronizer() {
		User.getGlobalUser().addObserver(this);
		MessageGateway.getInstance().addListener(this);
	}

	/**
	 * @return singleton instance of this Synchronizer
	 */
	public static Synchronizer getInstance() {
		if (mInstance == null) {
			mInstance = new Synchronizer();
		}
		return mInstance;
	}

	@Override
	public void onMessage(ChatMessage<?> message) {
		switch (message.type) {
		case SYNC_DOWNLOAD:
			mResourceRepo.syncDownload(this);
			SceneSwitcher.showWaitWindow("Synchronizing downloaded resources");

			break;
		case SYNC_USER_RESOURCES:
			// TODO sync user resources
			break;

		default:
			// Does nothing
			break;

		}

	}

	@Override
	public void update(Observable o, Object arg) {
		// TODO Auto-generated method stub
	}

	@Override
	public void handleWebResponse(IMethodEntity method, IEntity response) {
		if (response instanceof ISuccessStatuses) {
			SceneSwitcher.hideWaitWindow();
			if (((ISuccessStatuses) response).isSuccessful()) {
				SceneSwitcher.showSuccessMessage("Synchronization successful");
			} else {
				SceneSwitcher.showErrorMessage("Synchronization failed");
			}
		}
	}

	/** Resource repository */
	private ResourceRepo mResourceRepo = ResourceRepo.getInstance();

	/** Instance of this class */
	private static Synchronizer mInstance = null;
}

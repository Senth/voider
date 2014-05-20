package com.spiddekauga.voider.utils;

import java.util.LinkedList;
import java.util.Queue;

import com.spiddekauga.voider.network.entities.ChatMessage;
import com.spiddekauga.voider.network.entities.ChatMessage.MessageTypes;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.ISuccessStatuses;
import com.spiddekauga.voider.network.entities.method.IMethodEntity;
import com.spiddekauga.voider.network.entities.method.SyncUserResourcesMethod;
import com.spiddekauga.voider.network.entities.method.SyncUserResourcesMethodResponse;
import com.spiddekauga.voider.repo.ICallerResponseListener;
import com.spiddekauga.voider.repo.ResourceRepo;
import com.spiddekauga.voider.scene.SceneSwitcher;
import com.spiddekauga.voider.server.IMessageListener;
import com.spiddekauga.voider.server.MessageGateway;

/**
 * Listens to server synchronize events when to synchronize. Also checks synchronize everything when user logs in
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class Synchronizer implements IMessageListener, ICallerResponseListener {
	/**
	 * Initializes the synchronizer. Private constructor to enforce singleton usage
	 */
	private Synchronizer() {
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
		synchronize(message.type);
	}

	/**
	 * Synchronize the specified message type
	 * @param type the synchronize type to synchronize
	 * @return true if the type was handled
	 */
	public boolean synchronize(MessageTypes type) {
		return synchronize(type, null);
	}

	/**
	 * Synchronize the specified message type
	 * @param type the synchronize type to synchronize
	 * @param responseListener use a specified response listener, set to null to skip
	 * @return true if the type was handled
	 */
	public boolean synchronize(MessageTypes type, ICallerResponseListener responseListener) {
		ICallerResponseListener[] responseListeners = null;
		if (responseListener != null) {
			responseListeners = new ICallerResponseListener[2];
		} else {
			responseListeners = new ICallerResponseListener[1];
		}
		responseListeners[0] = this;
		responseListeners[1] = responseListener;

		switch (type) {
		case SYNC_DOWNLOAD:
			mResourceRepo.syncDownload(responseListeners);
			SceneSwitcher.showWaitWindow("Synchronizing downloaded resources\nThis may take a while...");

			break;
		case SYNC_USER_RESOURCES:
			mResourceRepo.syncUserResources(responseListeners);
			SceneSwitcher.showWaitWindow("Synchronizing user resources\nThis may take a while...");
			break;

		default:
			return false;
		}

		return true;
	}

	/**
	 * Synchronize everything
	 * @param responseListener the listener that also should get the web response
	 */
	public void synchronizeAll(ICallerResponseListener responseListener) {
		mResponseListener = responseListener;

		mSyncQueue.add(MessageTypes.SYNC_DOWNLOAD);
		mSyncQueue.add(MessageTypes.SYNC_USER_RESOURCES);

		syncNextInQueue();
	}

	@Override
	public void handleWebResponse(IMethodEntity method, IEntity response) {
		if (mSyncQueue.isEmpty()) {
			SceneSwitcher.hideWaitWindow();
		}

		if (response instanceof SyncUserResourcesMethodResponse) {
			handleSyncUserResourceResponse((SyncUserResourcesMethod) method, (SyncUserResourcesMethodResponse) response);
		} else if (response instanceof ISuccessStatuses) {
			if (((ISuccessStatuses) response).isSuccessful()) {
				SceneSwitcher.showSuccessMessage("Sync complete");
			} else {
				SceneSwitcher.showErrorMessage("Sync failed");
			}
		}

		syncNextInQueue();
	}

	/**
	 * Sync the next item in the queue
	 */
	private void syncNextInQueue() {
		boolean handled = false;
		while (!mSyncQueue.isEmpty() && !handled) {
			handled = synchronize(mSyncQueue.remove(), mResponseListener);
		}
	}

	/**
	 * Handle user resource revision synchronization
	 * @param method parameters sent to the server
	 * @param response response from the server
	 */
	private void handleSyncUserResourceResponse(SyncUserResourcesMethod method, SyncUserResourcesMethodResponse response) {
		if (response.downloadStatus) {
			switch (response.uploadStatus) {
			case FAILED_CONNECTION:
			case FAILED_INTERNAL:
			case FAILED_USER_NOT_LOGGED_IN:
				SceneSwitcher.showErrorMessage("Sync failed");
				break;

			case SUCCESS_ALL:
				SceneSwitcher.showSuccessMessage("Sync complete");
				break;

			case SUCCESS_PARTIAL:
				SceneSwitcher.showHighlightMessage("Sync conflict");
				// TODO handle conflict
				break;
			}
		} else {
			SceneSwitcher.showErrorMessage("Sync failed");
		}
	}

	/** Queue for what to synchronize */
	private Queue<MessageTypes> mSyncQueue = new LinkedList<>();
	/** Current response listener */
	private ICallerResponseListener mResponseListener = null;
	/** Resource repository */
	private ResourceRepo mResourceRepo = ResourceRepo.getInstance();

	/** Instance of this class */
	private static Synchronizer mInstance = null;
}

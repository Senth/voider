package com.spiddekauga.voider.utils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import com.spiddekauga.utils.Observable;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.IMethodEntity;
import com.spiddekauga.voider.network.entities.misc.BugReportEntity;
import com.spiddekauga.voider.network.entities.misc.BugReportMethod;
import com.spiddekauga.voider.network.entities.misc.BugReportMethodResponse;
import com.spiddekauga.voider.network.entities.misc.ChatMessage;
import com.spiddekauga.voider.network.entities.resource.DownloadSyncMethod;
import com.spiddekauga.voider.network.entities.resource.DownloadSyncMethodResponse;
import com.spiddekauga.voider.network.entities.resource.UserResourcesSyncMethod;
import com.spiddekauga.voider.network.entities.resource.UserResourcesSyncMethodResponse;
import com.spiddekauga.voider.network.entities.stat.HighscoreSyncMethod;
import com.spiddekauga.voider.network.entities.stat.HighscoreSyncMethodResponse;
import com.spiddekauga.voider.repo.IResponseListener;
import com.spiddekauga.voider.repo.misc.BugReportWebRepo;
import com.spiddekauga.voider.repo.resource.ExternalTypes;
import com.spiddekauga.voider.repo.resource.ResourceCacheFacade;
import com.spiddekauga.voider.repo.resource.ResourceLocalRepo;
import com.spiddekauga.voider.repo.resource.ResourceRepo;
import com.spiddekauga.voider.repo.stat.HighscoreRepo;
import com.spiddekauga.voider.resources.BugReportDef;
import com.spiddekauga.voider.scene.SceneSwitcher;
import com.spiddekauga.voider.server.IMessageListener;
import com.spiddekauga.voider.server.MessageGateway;

/**
 * Listens to server synchronize events when to synchronize. Also checks synchronize
 * everything when user logs in
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class Synchronizer extends Observable implements IMessageListener, IResponseListener {
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
		switch (message.type) {
		case SYNC_COMMUNITY_DOWNLOAD:
			synchronize(SyncTypes.COMMUNITY_RESOURCES);
			break;

		case SYNC_USER_RESOURCES:
			synchronize(SyncTypes.USER_RESOURCES);
			break;

		case SYNC_HIGHSCORE:
			synchronize(SyncTypes.HIGHSCORES);

		default:
			// Does nothing
			break;

		}
	}

	/**
	 * Synchronize the specified message type
	 * @param type the synchronize type to synchronize
	 * @return true if the type was handled
	 */
	public boolean synchronize(SyncTypes type) {
		return synchronize(type, null);
	}

	/**
	 * Synchronize the specified message type
	 * @param type the synchronize type to synchronize
	 * @param responseListener use a specified response listener, set to null to skip
	 * @return true if the type was handled
	 */
	public boolean synchronize(SyncTypes type, IResponseListener responseListener) {
		if (!User.getGlobalUser().isOnline()) {
			return false;
		}

		IResponseListener[] responseListeners = null;
		if (responseListener != null) {
			responseListeners = new IResponseListener[2];
			responseListeners[0] = this;
			responseListeners[1] = responseListener;
		} else {
			responseListeners = new IResponseListener[1];
			responseListeners[0] = this;
		}


		// TODO remove wait window for syncing that doesn't need it

		switch (type) {
		case COMMUNITY_RESOURCES:
			mResourceRepo.syncDownload(responseListeners);
			SceneSwitcher.showWaitWindow("Synchronizing downloaded resources\nThis may take a while...");

			break;
		case USER_RESOURCES:
			mResourceRepo.syncUserResources(responseListeners);
			SceneSwitcher.showWaitWindow("Synchronizing user resources\nThis may take a while...");
			break;

		case BUG_REPORTS:
			uploadBugReports(responseListeners);
			break;

		case HIGHSCORES:
			mHighscoreRepo.sync(responseListeners);
			break;
		}

		return true;
	}

	/**
	 * Upload bug reports
	 * @param responseListeners all response listener
	 */
	private void uploadBugReports(IResponseListener[] responseListeners) {
		BugReportWebRepo webRepo = BugReportWebRepo.getInstance();

		// Get all existing bug reports
		ArrayList<BugReportDef> bugReportDefs = ResourceCacheFacade.getAll(ExternalTypes.BUG_REPORT);
		ArrayList<BugReportEntity> bugsToSend = new ArrayList<>();

		for (BugReportDef bugReportDef : bugReportDefs) {
			bugsToSend.add(bugReportDef.toNetworkEntity());
		}

		if (!bugsToSend.isEmpty()) {
			webRepo.sendBugReport(bugsToSend, responseListeners);
			SceneSwitcher.showWaitWindow("Uploading saved bug reports");
		}
		// Hide message window if it has been shown
		else if (mSyncQueue.isEmpty()) {
			SceneSwitcher.hideWaitWindow();
		}
	}

	/**
	 * Synchronize everything
	 */
	public void synchronizeAll() {
		synchronizeAll(null);
	}

	/**
	 * Synchronize everything
	 * @param responseListener the listener that also should get the web response, may be
	 *        null
	 */
	public void synchronizeAll(IResponseListener responseListener) {
		mResponseListener = responseListener;

		mSyncQueue.add(SyncTypes.COMMUNITY_RESOURCES);
		mSyncQueue.add(SyncTypes.USER_RESOURCES);
		mSyncQueue.add(SyncTypes.HIGHSCORES);
		mSyncQueue.add(SyncTypes.BUG_REPORTS);

		syncNextInQueue();
	}

	@Override
	public void handleWebResponse(IMethodEntity method, IEntity response) {
		if (mSyncQueue.isEmpty()) {
			SceneSwitcher.hideWaitWindow();
		}

		if (response instanceof UserResourcesSyncMethodResponse) {
			handleSyncUserResourceResponse((UserResourcesSyncMethod) method, (UserResourcesSyncMethodResponse) response);
		} else if (response instanceof DownloadSyncMethodResponse) {
			handleSyncDownloadResponse((DownloadSyncMethod) method, (DownloadSyncMethodResponse) response);
		} else if (response instanceof BugReportMethodResponse) {
			handlePostBugReport((BugReportMethod) method, (BugReportMethodResponse) response);
		} else if (response instanceof HighscoreSyncMethodResponse) {
			handleSyncHighscoreResponse((HighscoreSyncMethod) method, (HighscoreSyncMethodResponse) response);
		}


		syncNextInQueue();
	}

	/**
	 * Handle sync highscore responses
	 * @param method parameters to the server
	 * @param response server response
	 */
	private void handleSyncHighscoreResponse(HighscoreSyncMethod method, HighscoreSyncMethodResponse response) {
		if (response.isSuccessful()) {
			SceneSwitcher.showSuccessMessage("Successfully synced player highscores");
		} else {
			SceneSwitcher.showErrorMessage("Failed to sync player highscores");
		}
	}

	/**
	 * Handle sync download responses
	 * @param method parameters to the server
	 * @param response server response
	 */
	private void handleSyncDownloadResponse(DownloadSyncMethod method, DownloadSyncMethodResponse response) {
		if (response.isSuccessful()) {
			notifyObservers(SyncEvents.COMMUNITY_DOWNLOAD_SUCCESS);
			SceneSwitcher.showSuccessMessage("Successfully synced community resources");
		} else {
			notifyObservers(SyncEvents.COMMUNITY_DOWNLOAD_FAILED);
			SceneSwitcher.showErrorMessage("Failed to sync community resources");
		}
	}

	/**
	 * Handle upload bug reports
	 * @param method parameters to the server
	 * @param response response from the server
	 */
	private void handlePostBugReport(BugReportMethod method, BugReportMethodResponse response) {
		// Show message
		switch (response.status) {
		case FAILED_CONNECTION:
		case FAILED_SERVER_ERROR:
		case FAILED_USER_NOT_LOGGED_IN:
			SceneSwitcher.showErrorMessage("Failed to upload saved bug reports");
			break;

		case SUCCESS:
			SceneSwitcher.showSuccessMessage("Successfully sent saved bug reports");
			break;

		case SUCCESS_WITH_ERRORS:
			SceneSwitcher.showHighlightMessage("Failed to sent some bug reports");
			break;
		}


		// Remove successful bug reports from local database
		if (response.isSuccessful()) {
			for (BugReportEntity bugReport : method.bugs) {
				// Check if successful
				if (!response.failedBugReports.contains(bugReport.id)) {
					ResourceLocalRepo.remove(bugReport.id);
				}
			}
		}
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
	private void handleSyncUserResourceResponse(UserResourcesSyncMethod method, UserResourcesSyncMethodResponse response) {
		switch (response.uploadStatus) {
		case FAILED_CONNECTION:
		case FAILED_INTERNAL:
		case FAILED_USER_NOT_LOGGED_IN:
			if (response.downloadStatus) {
				SceneSwitcher.showErrorMessage("Failed to upload user resources, only downloaded");
			} else {
				SceneSwitcher.showErrorMessage("Failed to sync user resources");
			}
			notifyObservers(SyncEvents.USER_RESOURCES_UPLOAD_FAILED);
			break;

		case SUCCESS_ALL:
			if (response.downloadStatus) {
				SceneSwitcher.showSuccessMessage("Successfully synced user resources");
			} else {
				SceneSwitcher.showErrorMessage("Failed to download user resources, only uploaded");
			}
			notifyObservers(SyncEvents.USER_RESOURCES_UPLOAD_SUCCESS);
			break;

		case SUCCESS_PARTIAL:
			SceneSwitcher.showHighlightMessage("Synced user resources, but found conflicts");
			// TODO handle conflict

			notifyObservers(SyncEvents.USER_RESOURCES_UPLOAD_CONFLICT);
			break;
		}

		// Send sync messages
		if (response.downloadStatus) {
			notifyObservers(SyncEvents.USER_RESOURCES_DOWNLOAD_SUCCESS);
		} else {
			notifyObservers(SyncEvents.USER_RESOURCES_DOWNLOAD_FAILED);
		}
	}

	/**
	 * Synchronization types
	 */
	public enum SyncTypes {
		/** Synchronized community downloaded resources */
		COMMUNITY_RESOURCES,
		/** Synchronize user resources */
		USER_RESOURCES,
		/** Upload bug reports */
		BUG_REPORTS,
		/** Highscores */
		HIGHSCORES,
	}

	/**
	 * Sync event enumerations
	 */
	public enum SyncEvents {
		/** Successfully downloaded user resources */
		USER_RESOURCES_DOWNLOAD_SUCCESS,
		/** Failed to download user resources */
		USER_RESOURCES_DOWNLOAD_FAILED,
		/** Successfully uploaded and synced ALL user resources */
		USER_RESOURCES_UPLOAD_SUCCESS,
		/** Failed to upload user resources */
		USER_RESOURCES_UPLOAD_FAILED,
		/** Conflict when uploading user resources */
		USER_RESOURCES_UPLOAD_CONFLICT,
		/** Downloaded new community resources */
		COMMUNITY_DOWNLOAD_SUCCESS,
		/** Failed to download community resources */
		COMMUNITY_DOWNLOAD_FAILED,
	}

	/** Queue for what to synchronize */
	private Queue<SyncTypes> mSyncQueue = new LinkedList<>();
	/** Current response listener */
	private IResponseListener mResponseListener = null;
	/** Resource repository */
	private ResourceRepo mResourceRepo = ResourceRepo.getInstance();
	/** Highscore repository */
	private HighscoreRepo mHighscoreRepo = HighscoreRepo.getInstance();

	/** Instance of this class */
	private static Synchronizer mInstance = null;
}

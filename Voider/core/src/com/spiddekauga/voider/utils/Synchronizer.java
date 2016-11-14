package com.spiddekauga.voider.utils;

import com.spiddekauga.net.IDownloadProgressListener;
import com.spiddekauga.utils.scene.ui.NotificationShower;
import com.spiddekauga.utils.scene.ui.NotificationShower.NotificationTypes;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.menu.LoginScene;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.IMethodEntity;
import com.spiddekauga.voider.network.misc.BugReportEntity;
import com.spiddekauga.voider.network.misc.BugReportMethod;
import com.spiddekauga.voider.network.misc.BugReportResponse;
import com.spiddekauga.voider.network.resource.DownloadSyncResponse;
import com.spiddekauga.voider.network.resource.ResourceConflictEntity;
import com.spiddekauga.voider.network.resource.UserResourceSyncMethod;
import com.spiddekauga.voider.network.resource.UserResourceSyncResponse;
import com.spiddekauga.voider.network.stat.HighscoreSyncResponse;
import com.spiddekauga.voider.network.stat.StatSyncResponse;
import com.spiddekauga.voider.repo.IResponseListener;
import com.spiddekauga.voider.repo.analytics.AnalyticsRepo;
import com.spiddekauga.voider.repo.misc.BugReportWebRepo;
import com.spiddekauga.voider.repo.resource.ExternalTypes;
import com.spiddekauga.voider.repo.resource.ResourceCacheFacade;
import com.spiddekauga.voider.repo.resource.ResourceLocalRepo;
import com.spiddekauga.voider.repo.resource.ResourceRepo;
import com.spiddekauga.voider.repo.stat.HighscoreRepo;
import com.spiddekauga.voider.repo.stat.StatRepo;
import com.spiddekauga.voider.repo.user.User;
import com.spiddekauga.voider.resources.BugReportDef;
import com.spiddekauga.voider.scene.SceneSwitcher;
import com.spiddekauga.voider.scene.ui.UiFactory;
import com.spiddekauga.voider.utils.event.EventDispatcher;
import com.spiddekauga.voider.utils.event.EventTypes;
import com.spiddekauga.voider.utils.event.GameEvent;
import com.spiddekauga.voider.utils.event.IEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

/**
 * Listens to server synchronize events when to synchronize. Also checks synchronize everything when
 * user logs in
 */
public class Synchronizer implements IResponseListener, IEventListener {
private static final EventDispatcher mEventDispatcher = EventDispatcher.getInstance();
private static final Synchronizer mInstance = new Synchronizer();
private NotificationShower mNotification = NotificationShower.getInstance();
private Semaphore mSemaphore = new Semaphore(1);
private BlockingQueue<SyncClass> mSyncQueue = new LinkedBlockingQueue<>();
private ResourceRepo mResourceRepo = ResourceRepo.getInstance();
private HighscoreRepo mHighscoreRepo = HighscoreRepo.getInstance();
private StatRepo mStatRepo = StatRepo.getInstance();
private AnalyticsRepo mAnalyticsRepo = AnalyticsRepo.getInstance();
private IDownloadProgressListener mDownloadProgressListener = new IDownloadProgressListener() {
	@Override
	public void handleFileDownloaded(int cComplete, int cTotal) {
		float completePercent = cComplete / ((float) cTotal) * 100;
		SceneSwitcher.updateProgressBar(completePercent, "" + cComplete + " / " + cTotal);

		// Hide
		if (cComplete == cTotal) {
			SceneSwitcher.hideProgressBar();
		}
	}
};
/**
 * Runs a thread in the background for synchronizing the queue
 */
private Thread mThread = new Thread() {
	@Override
	public void run() {
		while (true) {
			if (!mSyncQueue.isEmpty()) {
				try {
					mSemaphore.acquire();
					try {
						synchronize(mSyncQueue.take());
					} catch (InterruptedException e) {
						mSemaphore.release();
					}
				} catch (InterruptedException e) {
					// Does nothing
				}
			} else {
				try {
					sleep(200);
				} catch (InterruptedException e) {
					// Does nothing
				}
			}
		}
	}
};
private HashMap<UUID, ResourceConflictEntity> mConflictsFound = null;

/**
 * Initializes the synchronizer. Private constructor to enforce singleton usage
 */
private Synchronizer() {
	EventDispatcher eventDispatcher = EventDispatcher.getInstance();

	eventDispatcher.connect(EventTypes.SYNC_COMMUNITY_DOWNLOAD, this);
	eventDispatcher.connect(EventTypes.SYNC_USER_RESOURCES, this);
	eventDispatcher.connect(EventTypes.SYNC_HIGHSCORE, this);
	eventDispatcher.connect(EventTypes.SYNC_STATS, this);
	eventDispatcher.connect(EventTypes.USER_CONNECTED, this);

	mThread.start();
}

/**
 * @return singleton instance of this Synchronizer
 */
public static Synchronizer getInstance() {
	return mInstance;
}

@Override
public void handleEvent(GameEvent event) {
	switch (event.type) {
	case SYNC_COMMUNITY_DOWNLOAD:
		synchronize(SyncTypes.COMMUNITY_RESOURCES);
		break;

	case SYNC_USER_RESOURCES:
		synchronize(SyncTypes.USER_RESOURCES);
		break;

	case SYNC_HIGHSCORE:
		synchronize(SyncTypes.HIGHSCORES);
		break;

	case SYNC_STATS:
		synchronize(SyncTypes.STATS);
		break;

	case USER_CONNECTED:
		synchronizeAll();
		break;

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
 * Synchronize everything
 */
public void synchronizeAll() {
	synchronizeAll(null);
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

	switch (type) {
	case COMMUNITY_RESOURCES:
	case USER_RESOURCES:
		try {
			mSyncQueue.put(new SyncDownload(type, responseListener, false));
		} catch (InterruptedException e1) {
			// Does nothing
		}
		break;

	case ANALYTICS:
	case BUG_REPORTS:
	case HIGHSCORES:
	case STATS:
		try {
			mSyncQueue.put(new SyncClass(type));
		} catch (InterruptedException e) {
			// Does nothing
		}
		break;

	case USER_RESOURCE_FIX_CONFLICTS:
		Config.Debug.debugException("Cannot fix user resource conflict through synchronize() method");
		break;

	case WAIT_FOR_LOGIN_SCREEN_TO_DISAPPEAR:
		Config.Debug.debugException("Cannoct call WAIT_FOR_LOGIN_SCREEN_TO_DISAPPEAR through synchronize() method");
		break;
	}

	return true;
}

/**
 * Synchronize everything
 * @param responseListener the listener that also should get the web response, may be null
 */
public void synchronizeAll(IResponseListener responseListener) {
	mSyncQueue.add(new SyncDownload(SyncTypes.COMMUNITY_RESOURCES, responseListener, true));
	mSyncQueue.add(new SyncDownload(SyncTypes.USER_RESOURCES, responseListener, true));
	mSyncQueue.add(new SyncClass(SyncTypes.HIGHSCORES, responseListener));
	mSyncQueue.add(new SyncClass(SyncTypes.STATS, responseListener));
	mSyncQueue.add(new SyncClass(SyncTypes.BUG_REPORTS, responseListener));
	mSyncQueue.add(new SyncClass(SyncTypes.ANALYTICS, responseListener));
}

/**
 * Fix conflicts
 * @param keepLocal true if we want to keep the local versions, false if we want to keep the server
 * version.
 */
public void fixConflict(boolean keepLocal) {
	if (mConflictsFound != null && User.getGlobalUser().isOnline()) {
		try {
			mSyncQueue.put(new SyncFixConflict(mConflictsFound, keepLocal));
		} catch (InterruptedException e) {
			// Does nothing
		}
		mConflictsFound = null;
	}
}

/**
 * Synchronize the specified message type
 * @param syncClass what to sync
 */
private void synchronize(SyncClass syncClass) {
	if (!User.getGlobalUser().isOnline()) {
		return;
	}

	IResponseListener[] responseListeners = addSynchronizerToListeners(syncClass.responseListener);

	switch (syncClass.syncType) {
	case COMMUNITY_RESOURCES:
		if (syncClass instanceof SyncDownload) {
			// Show progress bar
			if (((SyncDownload) syncClass).showProgress) {
				mResourceRepo.syncDownload(mDownloadProgressListener, responseListeners);
				SceneSwitcher.showProgressBar("Downloading Internet\nThis may take a while...");
			}
			// Silent sync
			else {
				mResourceRepo.syncDownload(null, responseListeners);
			}
		}
		break;

	case USER_RESOURCES:
		if (syncClass instanceof SyncDownload) {
			// Show progress bar
			if (((SyncDownload) syncClass).showProgress) {
				mResourceRepo.syncUserResources(mDownloadProgressListener, responseListeners);
				SceneSwitcher.showProgressBar("Synchronizing your levels, enemies, and bullets.\nThis may take a while...");
			}
			// Silent sync
			else {
				mResourceRepo.syncUserResources(null, responseListeners);
			}
		}
		break;

	case BUG_REPORTS:
		uploadBugReports(responseListeners);
		break;

	case HIGHSCORES:
		mHighscoreRepo.sync(responseListeners);
		break;

	case STATS:
		mStatRepo.sync(responseListeners);
		break;

	case USER_RESOURCE_FIX_CONFLICTS:
		if (syncClass instanceof SyncFixConflict) {
			mResourceRepo.fixUserResourceConflict(((SyncFixConflict) syncClass).conflicts, ((SyncFixConflict) syncClass).keepLocal,
					mDownloadProgressListener, responseListeners);
		}
		break;

	case ANALYTICS:
		mAnalyticsRepo.sync(responseListeners);
		break;

	case WAIT_FOR_LOGIN_SCREEN_TO_DISAPPEAR:
		if (syncClass instanceof WaitForLoginScreenToDisappear) {
			WaitForLoginScreenToDisappear waitForLoginScreenToDisappear = (WaitForLoginScreenToDisappear) syncClass;

			while (!waitForLoginScreenToDisappear.isDone()) {
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		break;
	}
}

/**
 * Add the this class to the front of the response listener
 * @param responseListener existing response listener
 * @return array of response listener with the Synchronizer placed first
 */
private IResponseListener[] addSynchronizerToListeners(IResponseListener responseListener) {
	IResponseListener[] responseListeners = null;
	if (responseListener != null) {
		responseListeners = new IResponseListener[2];
		responseListeners[0] = this;
		responseListeners[1] = responseListener;
	} else {
		responseListeners = new IResponseListener[1];
		responseListeners[0] = this;
	}

	return responseListeners;
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
	} else {
		mSemaphore.release();
	}
}

@Override
public void handleWebResponse(IMethodEntity method, IEntity response) {
	SceneSwitcher.hideWaitWindow();

	if (response instanceof UserResourceSyncResponse) {
		handleSyncUserResourceResponse((UserResourceSyncMethod) method, (UserResourceSyncResponse) response);
	} else if (response instanceof DownloadSyncResponse) {
		handleSyncDownloadResponse((DownloadSyncResponse) response);
	} else if (response instanceof BugReportResponse) {
		handlePostBugReport((BugReportMethod) method, (BugReportResponse) response);
	} else if (response instanceof HighscoreSyncResponse) {
		handleSyncHighscoreResponse((HighscoreSyncResponse) response);
	} else if (response instanceof StatSyncResponse) {
		handleStatSyncResponse((StatSyncResponse) response);
	}

	mSemaphore.release();
}

/**
 * Handle user resource revision synchronization
 * @param method parameters sent to the server
 * @param response response from the server
 */
private void handleSyncUserResourceResponse(UserResourceSyncMethod method, UserResourceSyncResponse response) {
	switch (response.uploadStatus) {
	case FAILED_CONNECTION:
	case FAILED_INTERNAL:
	case FAILED_USER_NOT_LOGGED_IN:
		if (response.downloadStatus) {
			mNotification.showError("Failed to upload player resources");
		} else {
			mNotification.showError("Player resources sync failed");
		}
		mEventDispatcher.fire(new GameEvent(EventTypes.SYNC_USER_RESOURCES_UPLOAD_FAILED));
		break;

	case SUCCESS_ALL:
		// No Conflicts
		if (method.conflictKeepLocal == null) {
			if (response.downloadStatus) {
				mNotification.showSuccess("Player resources synced");
			} else {
				mNotification.showError("Download of new resources failed");
			}
		}
		// Conflicts
		else {
			mNotification.showSuccess("Conflicts resolved");
		}
		mEventDispatcher.fire(new GameEvent(EventTypes.SYNC_USER_RESOURCES_UPLOAD_SUCCESS));
		break;

	case SUCCESS_CONFLICTS:
		mConflictsFound = response.conflicts;
		UiFactory.getInstance().msgBox.conflictWindow();
		mEventDispatcher.fire(new GameEvent(EventTypes.SYNC_USER_RESOURCES_UPLOAD_CONFLICT));
		break;

	case SUCCESS_PARTIAL:
		mNotification.showError("Failed to upload some resources");
		mEventDispatcher.fire(new GameEvent(EventTypes.SYNC_USER_RESOURCES_UPLOAD_PARTIAL));
		break;
	}

	// Send sync messages
	if (response.downloadStatus) {
		mEventDispatcher.fire(new GameEvent(EventTypes.SYNC_USER_RESOURCES_DOWNLOAD_SUCCESS));
	} else {
		mEventDispatcher.fire(new GameEvent(EventTypes.SYNC_USER_RESOURCES_DOWNLOAD_FAILED));
	}
}

/**
 * Handle sync download responses
 * @param response server response
 */
private void handleSyncDownloadResponse(DownloadSyncResponse response) {
	if (response.isSuccessful()) {
		mEventDispatcher.fire(new GameEvent(EventTypes.SYNC_COMMUNITY_DOWNLOAD_SUCCESS));
		mNotification.showSuccess("Published resources synced");
	} else {
		mEventDispatcher.fire(new GameEvent(EventTypes.SYNC_COMMUNITY_DOWNLOAD_FAILED));
		mNotification.showError("Published resources sync failed");
	}
}

/**
 * Handle upload bug reports
 * @param method parameters to the server
 * @param response response from the server
 */
private void handlePostBugReport(BugReportMethod method, BugReportResponse response) {
	// Show message
	switch (response.status) {
	case FAILED_SERVER_CONNECTION:
	case FAILED_SERVER_ERROR:
	case FAILED_USER_NOT_LOGGED_IN:
		mNotification.showError("Couldn't send saved bug reports");
		break;

	case SUCCESS:
		mNotification.showSuccess("Sent saved bug reports");
		break;

	case SUCCESS_PARTIAL:
		mNotification.showHighlight("Sent some bug reports?");
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
 * Handle sync highscore responses
 * @param response server response
 */
private void handleSyncHighscoreResponse(HighscoreSyncResponse response) {
	if (response.isSuccessful()) {
		mNotification.showSuccess("Highscores synced");
		mEventDispatcher.fire(new GameEvent(EventTypes.SYNC_HIGHSCORE_SUCCESS));
	} else {
		mNotification.showError("Highscores sync failed");
		mEventDispatcher.fire(new GameEvent(EventTypes.SYNC_HIGHSCORE_FAILED));
	}
}

/**
 * Handle sync statistics response
 * @param response server response
 */
private void handleStatSyncResponse(StatSyncResponse response) {
	if (response.isSuccessful()) {
		mNotification.show(NotificationTypes.SUCCESS, "Stats synced");
		mEventDispatcher.fire(new GameEvent(EventTypes.SYNC_STATS_SUCCESS));
	} else {
		mNotification.show(NotificationTypes.ERROR, "Stat sync failed");
		mEventDispatcher.fire(new GameEvent(EventTypes.SYNC_STATS_FAILED));
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
	/** Statistics */
	STATS,
	/** Fix conflicts */
	USER_RESOURCE_FIX_CONFLICTS,
	/** Analytics */
	ANALYTICS,
	/** Wait until Login Screen has ended and another screen is active */
	WAIT_FOR_LOGIN_SCREEN_TO_DISAPPEAR,
}

/**
 * Base sync class
 */
private class SyncClass {
	// Extra response listener
	private IResponseListener responseListener = null;
	private SyncTypes syncType;

	/**
	 * Sets the sync type
	 * @param syncType
	 */
	protected SyncClass(SyncTypes syncType) {
		this.syncType = syncType;
	}

	/**
	 * Sets the sync type
	 * @param syncType
	 * @param responseListener
	 */
	protected SyncClass(SyncTypes syncType, IResponseListener responseListener) {
		this.syncType = syncType;
		this.responseListener = responseListener;
	}
}

/**
 * Sync class for downloading stuff
 */
private class SyncDownload extends SyncClass {
	private boolean showProgress;

	/**
	 * Sets the sync type
	 * @param syncType
	 * @param responseListener
	 * @param showProgress true if we want to show the progress bar
	 */
	private SyncDownload(SyncTypes syncType, IResponseListener responseListener, boolean showProgress) {
		super(syncType, responseListener);
		this.showProgress = showProgress;
	}
}

/**
 * Wait to synchronize until we're not at the login screen
 */
private class WaitForLoginScreenToDisappear extends SyncClass {
	protected WaitForLoginScreenToDisappear() {
		super(SyncTypes.WAIT_FOR_LOGIN_SCREEN_TO_DISAPPEAR);
	}

	/**
	 * Check if login screen has disappeared and we can continue with the sync progress
	 * @return true if the login screen has disappeared and another screen is active
	 */
	protected boolean isDone() {
		return !SceneSwitcher.isActiveScene(LoginScene.class);
	}
}

/**
 * Sync class for fixing conflicts
 */
private class SyncFixConflict extends SyncClass {
	private HashMap<UUID, ResourceConflictEntity> conflicts;
	private boolean keepLocal;

	/**
	 * Sets the conflict variables
	 * @param conflicts
	 * @param keepLocal
	 */
	private SyncFixConflict(HashMap<UUID, ResourceConflictEntity> conflicts, boolean keepLocal) {
		super(SyncTypes.USER_RESOURCE_FIX_CONFLICTS);
		this.conflicts = conflicts;
		this.keepLocal = keepLocal;
	}
}
}

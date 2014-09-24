package com.spiddekauga.voider.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

import com.badlogic.gdx.Gdx;
import com.spiddekauga.net.IDownloadProgressListener;
import com.spiddekauga.utils.Observable;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.IMethodEntity;
import com.spiddekauga.voider.network.entities.misc.BugReportEntity;
import com.spiddekauga.voider.network.entities.misc.BugReportMethod;
import com.spiddekauga.voider.network.entities.misc.BugReportMethodResponse;
import com.spiddekauga.voider.network.entities.misc.ChatMessage;
import com.spiddekauga.voider.network.entities.resource.DownloadSyncMethodResponse;
import com.spiddekauga.voider.network.entities.resource.ResourceConflictEntity;
import com.spiddekauga.voider.network.entities.resource.UserResourceSyncMethod;
import com.spiddekauga.voider.network.entities.resource.UserResourceSyncMethodResponse;
import com.spiddekauga.voider.network.entities.stat.HighscoreSyncMethodResponse;
import com.spiddekauga.voider.network.entities.stat.StatSyncMethodResponse;
import com.spiddekauga.voider.repo.IResponseListener;
import com.spiddekauga.voider.repo.misc.BugReportWebRepo;
import com.spiddekauga.voider.repo.resource.ExternalTypes;
import com.spiddekauga.voider.repo.resource.ResourceCacheFacade;
import com.spiddekauga.voider.repo.resource.ResourceLocalRepo;
import com.spiddekauga.voider.repo.resource.ResourceRepo;
import com.spiddekauga.voider.repo.stat.HighscoreRepo;
import com.spiddekauga.voider.repo.stat.StatRepo;
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
		mThread.start();
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
			break;

		case SYNC_STAT:
			synchronize(SyncTypes.STATS);
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
	 * Fix conflicts
	 * @param keepLocal true if we want to keep the local versions, false if we want to
	 *        keep the server version.
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
			Gdx.app.error("Synchronizer", "Cannot fix user resource conflict through synchronize() method");
			break;
		}

		return true;
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
		}
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
		mSyncQueue.add(new SyncDownload(SyncTypes.COMMUNITY_RESOURCES, responseListener, true));
		mSyncQueue.add(new SyncDownload(SyncTypes.USER_RESOURCES, responseListener, true));
		mSyncQueue.add(new SyncClass(SyncTypes.HIGHSCORES, responseListener));
		mSyncQueue.add(new SyncClass(SyncTypes.STATS, responseListener));
		mSyncQueue.add(new SyncClass(SyncTypes.BUG_REPORTS, responseListener));
	}

	@Override
	public void handleWebResponse(IMethodEntity method, IEntity response) {
		SceneSwitcher.hideWaitWindow();

		if (response instanceof UserResourceSyncMethodResponse) {
			handleSyncUserResourceResponse((UserResourceSyncMethod) method, (UserResourceSyncMethodResponse) response);
		} else if (response instanceof DownloadSyncMethodResponse) {
			handleSyncDownloadResponse((DownloadSyncMethodResponse) response);
		} else if (response instanceof BugReportMethodResponse) {
			handlePostBugReport((BugReportMethod) method, (BugReportMethodResponse) response);
		} else if (response instanceof HighscoreSyncMethodResponse) {
			handleSyncHighscoreResponse((HighscoreSyncMethodResponse) response);
		} else if (response instanceof StatSyncMethodResponse) {
			handleStatSyncResponse((StatSyncMethodResponse) response);
		}

		mSemaphore.release();
	}

	/**
	 * Handle sync statistics response
	 * @param response server response
	 */
	private void handleStatSyncResponse(StatSyncMethodResponse response) {
		if (response.isSuccessful()) {
			SceneSwitcher.showSuccessMessage("Stats synced");
		} else {
			SceneSwitcher.showErrorMessage("Stat sync failed");
		}
	}

	/**
	 * Handle sync highscore responses
	 * @param response server response
	 */
	private void handleSyncHighscoreResponse(HighscoreSyncMethodResponse response) {
		if (response.isSuccessful()) {
			SceneSwitcher.showSuccessMessage("Highscores synced");
		} else {
			SceneSwitcher.showErrorMessage("Highscores sync failed");
		}
	}

	/**
	 * Handle sync download responses
	 * @param response server response
	 */
	private void handleSyncDownloadResponse(DownloadSyncMethodResponse response) {
		if (response.isSuccessful()) {
			notifyObservers(SyncEvents.COMMUNITY_DOWNLOAD_SUCCESS);
			SceneSwitcher.showSuccessMessage("Downloaded resources synced");
		} else {
			notifyObservers(SyncEvents.COMMUNITY_DOWNLOAD_FAILED);
			SceneSwitcher.showErrorMessage("Downloaded resources sync failed");
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
			SceneSwitcher.showErrorMessage("Send saved bug reports");
			break;

		case SUCCESS:
			SceneSwitcher.showSuccessMessage("Sent saved bug reports");
			break;

		case SUCCESS_WITH_ERRORS:
			SceneSwitcher.showHighlightMessage("Sent some bug reports?");
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
	 * Handle user resource revision synchronization
	 * @param method parameters sent to the server
	 * @param response response from the server
	 */
	private void handleSyncUserResourceResponse(UserResourceSyncMethod method, UserResourceSyncMethodResponse response) {
		switch (response.uploadStatus) {
		case FAILED_CONNECTION:
		case FAILED_INTERNAL:
		case FAILED_USER_NOT_LOGGED_IN:
			if (response.downloadStatus) {
				SceneSwitcher.showErrorMessage("Downloaded player resources; failed to upload");
			} else {
				SceneSwitcher.showErrorMessage("Player resources sync failed");
			}
			notifyObservers(SyncEvents.USER_RESOURCES_UPLOAD_FAILED);
			break;

		case SUCCESS_ALL:
			// No Conflicts
			if (method.conflictKeepLocal == null) {
				if (response.downloadStatus) {
					SceneSwitcher.showSuccessMessage("Player resources synced");
				} else {
					SceneSwitcher.showErrorMessage("Uploaded player resources; failed to download");
				}
			}
			// Conflicts
			else {
				SceneSwitcher.showSuccessMessage("Conflicts resolved");
			}
			notifyObservers(SyncEvents.USER_RESOURCES_UPLOAD_SUCCESS);
			break;

		case SUCCESS_PARTIAL:
			mConflictsFound = response.conflicts;
			SceneSwitcher.showConflictWindow();

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
		/** Statistics */
		STATS,
		/** Fix conflicts */
		USER_RESOURCE_FIX_CONFLICTS,
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

	/**
	 * Base sync class
	 */
	private class SyncClass {
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

		// Extra response listener
		private IResponseListener responseListener = null;
		private SyncTypes syncType;
	}

	/**
	 * Sync class for downloading stuff
	 */
	private class SyncDownload extends SyncClass {
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

		private boolean showProgress;
	}

	/**
	 * Sync class for fixing conflicts
	 */
	private class SyncFixConflict extends SyncClass {
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

		private HashMap<UUID, ResourceConflictEntity> conflicts;
		private boolean keepLocal;
	}

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
						sleep(1000);
					} catch (InterruptedException e) {
						// Does nothing
					}
				}
			}
		}
	};

	/** Syncing semaphore (so only one thing is synced at the same time) */
	private Semaphore mSemaphore = new Semaphore(1);
	/** Queue for what to synchronize */
	private BlockingQueue<SyncClass> mSyncQueue = new LinkedBlockingQueue<>();
	/** Resource repository */
	private ResourceRepo mResourceRepo = ResourceRepo.getInstance();
	/** Highscore repository */
	private HighscoreRepo mHighscoreRepo = HighscoreRepo.getInstance();
	/** Stats repository */
	private StatRepo mStatRepo = StatRepo.getInstance();
	/** Download progress listener */
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
	/** Last found conflicts */
	private HashMap<UUID, ResourceConflictEntity> mConflictsFound = null;

	/** Instance of this class */
	private static Synchronizer mInstance = null;
}

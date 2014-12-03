package com.spiddekauga.voider.explore;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.spiddekauga.utils.KeyHelper;
import com.spiddekauga.utils.scene.ui.NotificationShower.NotificationTypes;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.IMethodEntity;
import com.spiddekauga.voider.network.entities.resource.DefEntity;
import com.spiddekauga.voider.network.entities.resource.FetchStatuses;
import com.spiddekauga.voider.network.entities.resource.ResourceDownloadMethod;
import com.spiddekauga.voider.network.entities.resource.ResourceDownloadMethodResponse;
import com.spiddekauga.voider.repo.IResponseListener;
import com.spiddekauga.voider.repo.WebWrapper;
import com.spiddekauga.voider.repo.resource.InternalNames;
import com.spiddekauga.voider.repo.resource.ResourceCacheFacade;
import com.spiddekauga.voider.repo.resource.ResourceLocalRepo;
import com.spiddekauga.voider.repo.resource.ResourceRepo;
import com.spiddekauga.voider.scene.Scene;
import com.spiddekauga.voider.utils.Graphics;

/**
 * Common class for all explore scenes
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
abstract class ExploreScene extends Scene implements IResponseListener {
	/**
	 * @param gui explore GUI
	 * @param action the action to do when the resource is selected
	 */
	protected ExploreScene(ExploreGui gui, ExploreActions action) {
		super(gui);

		mAction = action;
		((ExploreGui) mGui).setExploreScene(this);
	}

	@Override
	protected void loadResources() {
		super.loadResources();

		ResourceCacheFacade.load(InternalNames.UI_GENERAL);
	}

	@Override
	protected void unloadResources() {
		ResourceCacheFacade.unload(InternalNames.UI_GENERAL);

		super.unloadResources();
	}


	@Override
	protected boolean onKeyDown(int keycode) {

		if (KeyHelper.isBackPressed(keycode)) {
			endScene();
			return true;
		}

		return super.onKeyDown(keycode);
	}

	@Override
	protected void update(float deltaTime) {
		super.update(deltaTime);

		handleWepResponses();
	}

	@Override
	public final void handleWebResponse(IMethodEntity method, IEntity response) {
		try {
			mWebResponses.put(new WebWrapper(method, response));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Handles existing web responses
	 */
	private void handleWepResponses() {
		while (!mWebResponses.isEmpty()) {
			try {
				WebWrapper webWrapper = mWebResponses.take();
				onWebResponse(webWrapper.method, webWrapper.response);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Create drawable for the def entity if it doesn't exist
	 * @param defEntity the def to create a drawable for if it doesn't exist
	 */
	protected void createDrawable(DefEntity defEntity) {
		if (defEntity.drawable == null && defEntity.png != null) {
			defEntity.drawable = Graphics.pngToDrawable(defEntity.png);
		}
	}

	/**
	 * Handles failed responses statuses
	 * @param status response status from the server
	 */
	protected void handleFailedStatus(FetchStatuses status) {
		switch (status) {
		case FAILED_SERVER_CONNECTION:
			mNotification.show(NotificationTypes.ERROR, "Failed to connect to the server");
			break;

		case FAILED_SERVER_ERROR:
			mNotification.show(NotificationTypes.ERROR, "Internal server error");
			break;

		case FAILED_USER_NOT_LOGGED_IN:
			mNotification.show(NotificationTypes.ERROR, "You have been logged out");
			break;

		case SUCCESS_FETCHED_ALL:
		case SUCCESS_MORE_EXISTS:
			// Does nothing
			break;
		default:
			break;

		}
	}


	/**
	 * @return true if we're currently fetching content
	 */
	abstract boolean isFetchingContent();

	/**
	 * @return true if we have more content to fetch
	 */
	abstract boolean hasMoreContent();

	/**
	 * Fetch more content
	 */
	abstract void fetchMoreContent();

	/**
	 * Repopulate content
	 */
	abstract void repopulateContent();

	/**
	 * Handle synchronized web response. This method should be used instead of in
	 * sub-classes.
	 * @param method parameters to the server
	 * @param response response from the server
	 */
	protected void onWebResponse(IMethodEntity method, IEntity response) {
		if (response instanceof ResourceDownloadMethodResponse) {
			handleResourceDownloadResponse((ResourceDownloadMethod) method, (ResourceDownloadMethodResponse) response);
		}
	}

	/**
	 * Handles a response from downloading a level
	 * @param method parameters to the server method that was called
	 * @param response server response
	 */
	private void handleResourceDownloadResponse(ResourceDownloadMethod method, ResourceDownloadMethodResponse response) {
		mGui.hideWaitWindow();
		if (response.status.isSuccessful()) {
			onResourceDownloaded(mAction);
		} else {
			switch (response.status) {
			case FAILED_CONNECTION:
				mNotification.show(NotificationTypes.ERROR, "Could not connect to the server");
				break;
			case FAILED_DOWNLOAD:
				mNotification.show(NotificationTypes.ERROR, "Download failed, please retry");
				break;
			case FAILED_SERVER_INTERAL:
				mNotification.show(NotificationTypes.ERROR, "Internal server error, please file a bug report");
				break;
			default:
				break;
			}
		}
	}

	/**
	 * Called when a resource has been successfully downloaded. Override this method
	 * @param action the action to do when the resource was downloaded
	 */
	protected void onResourceDownloaded(ExploreActions action) {
		// Does nothing
	}

	/**
	 * Call this when #onSelectAction should be called with the correct action
	 */
	final void selectAction() {
		onSelectAction(mAction);
	}

	/**
	 * Download the specified resource if needed. If already downloaded
	 * {@link #onResourceDownloaded(ExploreActions)} will be called.
	 * @param defEntity the resource to download
	 */
	protected void downloadResource(DefEntity defEntity) {
		if (!ResourceLocalRepo.exists(defEntity.resourceId)) {
			mResourceRepo.download(this, defEntity.resourceId);
			mGui.showWaitWindow("Downloading " + defEntity.name);
		} else {
			onResourceDownloaded(mAction);
		}
	}

	/**
	 * Called when an actor has been selected and pressed again. I.e. default action
	 * @param action the action to take
	 */
	protected abstract void onSelectAction(ExploreActions action);

	/**
	 * @return action to do when a resource has been selected
	 */
	protected ExploreActions getSelectedAction() {
		return mAction;
	}

	private ExploreActions mAction;
	/** Synchronized web responses */
	private BlockingQueue<WebWrapper> mWebResponses = new LinkedBlockingQueue<WebWrapper>();
	private ResourceRepo mResourceRepo = ResourceRepo.getInstance();
}

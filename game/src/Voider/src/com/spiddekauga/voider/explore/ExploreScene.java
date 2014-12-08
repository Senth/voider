package com.spiddekauga.voider.explore;

import java.util.ArrayList;
import java.util.List;
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
import com.spiddekauga.voider.network.entities.resource.RevisionEntity;
import com.spiddekauga.voider.repo.IResponseListener;
import com.spiddekauga.voider.repo.WebWrapper;
import com.spiddekauga.voider.repo.resource.ExternalTypes;
import com.spiddekauga.voider.repo.resource.InternalNames;
import com.spiddekauga.voider.repo.resource.ResourceCacheFacade;
import com.spiddekauga.voider.repo.resource.ResourceLocalRepo;
import com.spiddekauga.voider.repo.resource.ResourceRepo;
import com.spiddekauga.voider.resources.Def;
import com.spiddekauga.voider.scene.Scene;
import com.spiddekauga.voider.utils.Graphics;
import com.spiddekauga.voider.utils.User;

/**
 * Common class for all explore scenes
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
abstract class ExploreScene extends Scene implements IResponseListener {
	/**
	 * @param gui explore GUI
	 * @param action the action to do when the resource is selected
	 * @param defType the definition type to browse/load
	 */
	protected ExploreScene(ExploreGui gui, ExploreActions action, Class<? extends Def> defType) {
		super(gui);

		mLocalType = ExternalTypes.fromType(defType);
		mAction = action;
		((ExploreGui) mGui).setExploreScene(this);
	}

	@Override
	protected void loadResources() {
		super.loadResources();

		ResourceCacheFacade.load(InternalNames.UI_GENERAL);
		ResourceCacheFacade.loadAllOf(this, mLocalType, false);
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
	protected abstract boolean isFetchingContent();

	/**
	 * @return true if we have more content to fetch
	 */
	protected abstract boolean hasMoreContent();

	/**
	 * Fetch more content
	 */
	protected abstract void fetchMoreContent();

	/**
	 * Repopulate content
	 */
	protected void repopulateContent() {
		((ExploreGui) mGui).resetContent();
		if (getView().isLocal()) {
			updateLocalContent();
		}
	}

	/**
	 * Update local content
	 */
	private void updateLocalContent() {
		getAndSetAllResources();

		// Filter
		mFilteredResults.clear();

		for (DefEntity defEntity : mAllLocalDefs) {
			if (defPassesFilter(defEntity)) {
				mFilteredResults.add(defEntity);
			}
		}

		((ExploreGui) mGui).addContent(mFilteredResults);
	}

	/**
	 * Get and set all resources
	 */
	private void getAndSetAllResources() {
		// Already set -> skip
		if (mAllLocalDefs != null) {
			return;
		}

		mAllLocalDefs = new ArrayList<>();

		ArrayList<Def> defs = ResourceCacheFacade.getAll(mLocalType);

		// Convert to defEntity
		for (Def def : defs) {
			mAllLocalDefs.add(def.toDefEntity(false));
		}
	}

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

	/**
	 * Filter only by published
	 * @param published true to only search for published, false for not published, null
	 *        for any
	 */
	protected void setPublished(Boolean published) {
		mFilter.published = published;
		repopulateContent();
	}

	/**
	 * @return true if only filtered by published
	 */
	protected Boolean isPublished() {
		return mFilter.published;
	}

	/**
	 * Set if we should only search for my own
	 * @param onlyMine search only for player's own actors
	 */
	protected void setOnlyMine(boolean onlyMine) {
		mFilter.onlyMine = onlyMine;
		repopulateContent();
	}

	/**
	 * @return true if only mine should be searched
	 */
	protected boolean isOnlyMine() {
		return mFilter.onlyMine;
	}

	/**
	 * Set search string to filter by
	 * @param searchString what to search for
	 */
	protected void setSearchString(String searchString) {
		mFilter.searchString = searchString;
		repopulateContent();
	}

	/**
	 * @return search string
	 */
	protected String getSearchString() {
		return mFilter.searchString;
	}

	/**
	 * Sets the active view
	 * @param view the new active view
	 */
	protected final void setView(ExploreViews view) {
		mView = view;
		((ExploreGui) mGui).resetContent();
		((ExploreGui) mGui).resetContentMargins();
		repopulateContent();
	}

	/**
	 * @return current view
	 */
	protected final ExploreViews getView() {
		return mView;
	}

	/**
	 * Different possible views
	 */
	enum ExploreViews {
		/** Browse and Search locally */
		LOCAL,
		/** Browse online */
		ONLINE_BROWSE,
		/** Search online */
		ONLINE_SEARCH,

		;

		/**
		 * @return true if online is required
		 */
		boolean isOnline() {
			return name().contains("ONLINE");
		}

		/**
		 * @return true if local
		 */
		boolean isLocal() {
			return !isOnline();
		}
	}

	/**
	 * Filter a resource definition through local search
	 * @param defEntity the definition to filter
	 * @return true if the definition passes the filter
	 */
	protected boolean defPassesFilter(DefEntity defEntity) {
		return mFilter.defPassesFilter(defEntity);
	}

	/**
	 * @param <ActorType> type of actor that is selected
	 * @return the selected actor
	 */
	@SuppressWarnings("unchecked")
	protected <ActorType extends DefEntity> ActorType getSelected() {
		return (ActorType) mSelected;
	}

	/**
	 * Sets the selected resource
	 * @param def the selected resource
	 */
	protected void setSelected(DefEntity def) {
		if (mSelected != def) {
			mSelected = def;
			((ExploreGui) mGui).onSelectionChanged(def);
		}
	}

	/**
	 * Set the revision to load instead of the current one (if applicable)
	 * @param revision the revision to load
	 */
	void setRevision(int revision) {
		if (mSelected != null) {
			mSelected.revision = revision;
		}
	}

	/**
	 * Checks if an object exists in an array, but only if the array isn't empty. Helper
	 * method for filtering
	 * @param list the list to check
	 * @param object checks if this is in the list
	 * @return true if the object is in the list or the list is empty
	 */
	protected static boolean isObjectInFilterList(List<?> list, Object object) {
		if (list.isEmpty()) {
			return true;
		} else {
			return list.contains(object);
		}
	}

	/**
	 * @return All revisions of the selected definition, empty if not N/A
	 */
	ArrayList<RevisionEntity> getSelectedResourceRevisions() {
		if (mSelected != null) {
			return ResourceLocalRepo.getRevisions(mSelected.resourceId);
		} else {
			return new ArrayList<>();
		}
	}

	private ArrayList<DefEntity> mAllLocalDefs = null;
	private ArrayList<DefEntity> mFilteredResults = new ArrayList<>();
	private DefEntity mSelected = null;

	private ExploreViews mView = ExploreViews.LOCAL;
	private ExploreActions mAction;
	/** Synchronized web responses */
	private BlockingQueue<WebWrapper> mWebResponses = new LinkedBlockingQueue<WebWrapper>();
	private ResourceRepo mResourceRepo = ResourceRepo.getInstance();
	private SearchFilter mFilter = new SearchFilter();
	private ExternalTypes mLocalType;

	/**
	 * Class for filtering local searches
	 */
	private class SearchFilter {
		String searchString = "";
		boolean onlyMine = false;
		Boolean published = null;

		/**
		 * Filters a resource definition
		 * @param def
		 * @return true if we should keep this definition (i.e. it passes the filter)
		 */
		boolean defPassesFilter(DefEntity def) {
			// Search String
			String searchStringLower = searchString.toLowerCase();

			// Search String - Name
			if (!def.name.toLowerCase().contains(searchStringLower)) {
				// Search String - Original creator
				if (!def.originalCreator.toLowerCase().contains(searchStringLower)) {
					// Search String - Revised by
					if (!def.revisedBy.toLowerCase().contains(searchStringLower)) {
						return false;
					}
				}
			}


			// Only mine
			if (onlyMine) {
				if (!def.revisedByKey.equals(User.getGlobalUser().getServerKey())) {
					return false;
				}
			}

			// Published
			if (published != null) {
				// Only published
				if (published) {
					if (!ResourceLocalRepo.isPublished(def.resourceId)) {
						return false;
					}
				}
				// Not published
				else {
					if (ResourceLocalRepo.isPublished(def.resourceId)) {
						return false;
					}
				}
			}

			return true;
		}
	}
}

package com.spiddekauga.voider.menu;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.UUID;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.spiddekauga.voider.network.entities.resource.RevisionEntity;
import com.spiddekauga.voider.repo.resource.ExternalTypes;
import com.spiddekauga.voider.repo.resource.InternalNames;
import com.spiddekauga.voider.repo.resource.ResourceCacheFacade;
import com.spiddekauga.voider.repo.resource.ResourceLocalRepo;
import com.spiddekauga.voider.repo.resource.ResourceNotFoundException;
import com.spiddekauga.voider.resources.Def;
import com.spiddekauga.voider.resources.IResourceRevision;
import com.spiddekauga.voider.resources.IResourceTexture;
import com.spiddekauga.voider.resources.ResourceItem;
import com.spiddekauga.voider.scene.Scene;
import com.spiddekauga.voider.utils.User;
import com.spiddekauga.voider.utils.event.EventDispatcher;
import com.spiddekauga.voider.utils.event.EventTypes;
import com.spiddekauga.voider.utils.event.GameEvent;
import com.spiddekauga.voider.utils.event.IEventListener;

/**
 * Scene for selecting definitions (actors, levels). It will display a scene for the
 * specified type of resource and you will be able to filter the resource for name,
 * creator, etc.
 * @seciton Outcomes \li DEF_SELECTED selected a definition, the message is the file path
 *          for the select \li DEF_SELECT_CANCEL canceled the selection.
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class SelectDefScene extends Scene implements IEventListener {
	/**
	 * Private common constructor
	 * @param defType the definition type that the player want to select.
	 * @param buttonText text for the load/play button
	 * @param showMineOnly set as true if the player only shall see his/her own
	 *        definitions by default, if showMineOnlyCheckbox is set to true the player
	 *        can change this value.
	 * @param showMineOnlyCheckbox set to true if you want the scene to show a checkbox to
	 *        only display one's own actors.
	 */
	private SelectDefScene(ExternalTypes defType, String buttonText, boolean showMineOnly, boolean showMineOnlyCheckbox) {
		super(new SelectDefGui(showMineOnlyCheckbox, buttonText));

		mShowMineOnly = showMineOnly;
		mDefType = defType;
		((SelectDefGui) mGui).setSelectDefScene(this);

		for (int i = 0; i < mCategoryFilters.length; ++i) {
			mCategoryFilters[i] = new ArrayList<String>();
		}
	}

	/**
	 * Creates a selector scene with the latest revisions of the specified definition
	 * type.
	 * @param defType the definition type that the player want to select.
	 * @param buttonText text for the load/play button
	 * @param showMineOnly set as true if the player only shall see his/her own
	 *        definitions by default, if showMineOnlyCheckbox is set to true the player
	 *        can change this value.
	 * @param showMineOnlyCheckbox set to true if you want the scene to show a checkbox to
	 *        only display one's own actors.
	 * @param canChooseRevision set this to true if the player should be able to select
	 *        another revision of the resource.
	 */
	public SelectDefScene(ExternalTypes defType, String buttonText, boolean showMineOnly, boolean showMineOnlyCheckbox, boolean canChooseRevision) {
		this(defType, buttonText, showMineOnly, showMineOnlyCheckbox);

		mCanChooseRevision = canChooseRevision;
	}

	@Override
	protected void onActivate(Outcomes outcome, Object message, Outcomes loadingOutcome) {
		super.onActivate(outcome, message, loadingOutcome);

		if (loadingOutcome == Outcomes.LOADING_SUCCEEDED) {
			reloadDefinitions();

			mGui.resetValues();
		}
	}

	@Override
	protected void onInit() {
		super.onInit();
		mEventDispatcher.connect(EventTypes.SYNC_COMMUNITY_DOWNLOAD_SUCCESS, this);
		mEventDispatcher.connect(EventTypes.SYNC_USER_RESOURCES_UPLOAD_SUCCESS, this);
		mEventDispatcher.connect(EventTypes.SYNC_USER_RESOURCES_UPLOAD_CONFLICT, this);
	}

	@Override
	protected void onDispose() {
		super.onDispose();
		mEventDispatcher.disconnect(EventTypes.SYNC_COMMUNITY_DOWNLOAD_SUCCESS, this);
		mEventDispatcher.disconnect(EventTypes.SYNC_USER_RESOURCES_UPLOAD_SUCCESS, this);
		mEventDispatcher.disconnect(EventTypes.SYNC_USER_RESOURCES_UPLOAD_CONFLICT, this);
	}

	/**
	 * reload definitions
	 */
	private void reloadDefinitions() {
		mDefs.clear();

		ArrayList<Def> defs = ResourceCacheFacade.getAll(mDefType);

		for (Def def : defs) {
			// Only show published or resources with latest revision
			if (ResourceLocalRepo.isPublished(def.getId())) {
				mDefs.add(new DefVisible(def));
			} else {
				try {
					RevisionEntity revisionInfo = ResourceLocalRepo.getRevisionLatest(def.getId());
					if (revisionInfo.revision == def.getRevision()) {
						mDefs.add(new DefVisible(def));
					}
				} catch (ResourceNotFoundException e) {
					// Does nothing
				}
			}
		}

		defs = null;
	}


	@Override
	protected void loadResources() {
		super.loadResources();
		ResourceCacheFacade.load(InternalNames.UI_GENERAL);
		ResourceCacheFacade.loadAllOf(this, mDefType, false);
	}

	@Override
	protected void unloadResources() {
		super.unloadResources();
		ResourceCacheFacade.unload(InternalNames.UI_GENERAL);
	}

	@Override
	public void handleEvent(GameEvent event) {
		switch (event.type) {
		case SYNC_USER_RESOURCES_DOWNLOAD_SUCCESS:
		case SYNC_COMMUNITY_DOWNLOAD_SUCCESS:
		case SYNC_USER_RESOURCES_UPLOAD_CONFLICT:
			ResourceCacheFacade.loadAllOf(this, mDefType, false);

			new Thread() {
				@Override
				public void run() {
					// Wait until loaded
					while (ResourceCacheFacade.isLoading()) {
						;
					}

					Gdx.app.postRunnable(new Runnable() {

						@Override
						public void run() {
							reloadDefinitions();
							mGui.resetValues();
						}
					});
				}
			}.start();

			break;

		default:
			// Does nothing
			break;
		}
	}

	@Override
	protected void onResize(int width, int height) {
		super.onResize(width, height);
		mGui.resetValues();
	}

	@Override
	public boolean onKeyDown(int keycode) {
		// Set level as complete if we want to go back while testing
		if (keycode == Keys.ESCAPE || keycode == Keys.BACK) {
			cancel();
		}

		return false;
	}

	/**
	 * Sets if we currently only shall show the player's definitions
	 * @param showMineOnly true if only player's own definitions
	 */
	void setShowMineOnly(boolean showMineOnly) {
		mShowMineOnly = showMineOnly;

		updateVisibility();
	}

	/**
	 * @return true if we currently only show the player's definitions
	 */
	boolean shallShowMineOnly() {
		return mShowMineOnly;
	}

	/**
	 * Sets the filter for the definitions
	 * @param filter the filtering string
	 */
	void setFilter(String filter) {
		mFilter = filter.trim();

		updateCategoryFilters();
		updateVisibility();
	}

	/**
	 * @return all loaded definitions
	 */
	ArrayList<DefVisible> getDefs() {
		return mDefs;
	}

	/**
	 * Sets the selected definition
	 * @param def the selected definition
	 */
	void setSelectedDef(Def def) {
		mSelectedDef = def;
	}

	/**
	 * Set another revision of the definition
	 * @param revision the revision to use for the selected definition
	 */
	public void setRevision(int revision) {
		mSelectedRevision = revision;

		// Load the revision
		if (!ResourceCacheFacade.isLoaded(mSelectedDef.getId(), revision)) {
			ResourceCacheFacade.load(this, mSelectedDef.getId(), true, revision);
			ResourceCacheFacade.finishLoading();
		}

		// Set new resource
		mSelectedDef = ResourceCacheFacade.get(mSelectedDef.getId(), revision);

		((SelectDefGui) mGui).resetInfo();
	}

	/**
	 * @return current selected revision
	 */
	int getRevision() {
		return mSelectedRevision;
	}

	/**
	 * Checks if this resource is currently selected
	 * @param defId id of the definition to check if it's currently selected
	 * @return true if it's selected, false if not
	 */
	boolean isDefSelected(UUID defId) {
		if (mSelectedDef != null) {
			return mSelectedDef.equals(defId);
		}
		return false;
	}

	/**
	 * @return the definition type
	 */
	ExternalTypes getDefType() {
		return mDefType;
	}

	/**
	 * @return true if any def is selected
	 */
	boolean isDefSelected() {
		return mSelectedDef != null;
	}

	/**
	 * @return drawable image of the resource, null if none has been selected
	 */
	Drawable getDrawable() {
		if (mSelectedDef instanceof IResourceTexture) {
			return ((IResourceTexture) mSelectedDef).getTextureRegionDrawable();
		}
		return null;
	}

	/**
	 * @return name of the selected definition. An empty string if no definition has been
	 *         selected
	 */
	String getName() {
		if (mSelectedDef != null) {
			return mSelectedDef.getName();
		}
		return "";
	}

	/**
	 * @return current date of the selected definition. An empty string if no definition
	 *         has been selected
	 */
	String getDate() {
		if (mSelectedDef != null) {
			return mSelectedDef.getDateString();
		}
		return "";
	}

	/**
	 * @return description of the definition. An empty string if no definition has been
	 *         selected
	 */
	String getDescription() {
		if (mSelectedDef != null) {
			return mSelectedDef.getDescription();
		}
		return "";
	}

	/**
	 * @return creator of the definition. An empty string if no definition has been
	 *         selected
	 */
	String getCreator() {
		if (mSelectedDef != null) {
			return mSelectedDef.getRevisedBy();
		}
		return "";
	}

	/**
	 * @return original creator of the definition. An empty string if no definition has
	 *         been selected
	 */
	String getOriginalCreator() {
		if (mSelectedDef != null) {
			return mSelectedDef.getOriginalCreator();
		}
		return "";
	}

	/**
	 * @return revision of the definition. An empty string if no definition has been
	 *         selected
	 */
	String getRevisionString() {
		if (mSelectedDef != null) {
			return String.valueOf(mSelectedDef.getRevision());
		}
		return "";
	}

	/**
	 * @return true if the selected definition is published
	 */
	boolean isSelectedPublished() {
		if (mSelectedDef != null) {
			return ResourceLocalRepo.isPublished(mSelectedDef.getId());
		}
		return false;
	}

	/**
	 * @return map with all revisions and dates of the current selected resource, null if
	 *         none was found.
	 */
	ArrayList<RevisionEntity> getSelectedResourceRevisionsWithDates() {
		if (mSelectedDef != null) {
			return ResourceLocalRepo.getRevisions(mSelectedDef.getId());
		}
		return null;
	}

	/**
	 * @return true if the player can select another revision of the resource
	 */
	boolean canChooseRevision() {
		return mCanChooseRevision;
	}

	/**
	 * Loads the selected definition
	 */
	void loadDef() {
		if (mSelectedDef != null) {
			ResourceItem resourceItem = new ResourceItem();
			resourceItem.id = mSelectedDef.getId();
			resourceItem.revision = -1;
			if (mSelectedDef instanceof IResourceRevision) {
				if (!ResourceLocalRepo.isPublished(mSelectedDef.getId())) {
					resourceItem.revision = mSelectedDef.getRevision();
				}
			}

			setOutcome(Outcomes.DEF_SELECTED, resourceItem);
		} else {
			mGui.showErrorMessage("No resource selected");
		}
	}

	/**
	 * Cancels the loading of another definition
	 */
	void cancel() {
		setOutcome(Outcomes.DEF_SELECT_CANCEL);
	}

	/**
	 * Wrapper for a definition and if it shall be shown (due to filters)
	 */
	class DefVisible {
		/**
		 * Creates the wrapper and sets definition and visibility
		 * @param def the definition
		 * @param visible true if it shall be visible
		 */
		DefVisible(Def def, boolean visible) {
			this.def = def;
			this.visible = visible;
		}

		/**
		 * Creates the wrapper. The visibility will be set to true
		 * @param def the definition
		 */
		DefVisible(Def def) {
			this(def, true);
		}

		/** The definition */
		Def def = null;
		/** If it shall be visible or not */
		boolean visible = true;
	}

	/**
	 * Updates the filter categories from the current filter
	 */
	private void updateCategoryFilters() {
		// Remove old filters
		for (ArrayList<String> categoryFilters : mCategoryFilters) {
			categoryFilters.clear();
		}
		mAnyFilters.clear();


		// Split words by space
		String[] words = mFilter.split(" ");
		for (String word : words) {
			word = word.toLowerCase();

			// Is the word classified by a keyword/category.
			String[] wordSplit = word.split(":");

			// A keyword
			if (wordSplit.length == 2) {
				CategoryFilterTypes foundType = null;

				// Search for the keyword
				for (CategoryFilterTypes type : CategoryFilterTypes.values()) {
					if (wordSplit[0].equals(type.getKeyword())) {
						foundType = type;
						break;
					}
				}

				if (foundType != null) {
					mCategoryFilters[foundType.ordinal()].add(wordSplit[1]);
				}
				// Not a valid keyword, add the entire word
				else {
					mAnyFilters.add(word);
				}
			}
			// No keyword, or too many colons
			else {
				mAnyFilters.add(word);
			}
		}
	}

	/**
	 * Updates the visibility of the definitions depending on the current filter and if
	 * only our own actors shall be included.
	 */
	private void updateVisibility() {
		// If no filter, show all/only our
		if (mFilter.equals("")) {
			// Only our
			if (mShowMineOnly) {
				for (DefVisible defVisible : mDefs) {
					if (isOwner(defVisible.def)) {
						defVisible.visible = true;
					} else {
						defVisible.visible = false;
					}
				}
			}
			// All
			else {
				for (DefVisible defVisible : mDefs) {
					defVisible.visible = true;
				}
			}
		}
		// Else filter
		else {
			// Only mine
			if (mShowMineOnly) {
				for (DefVisible defVisible : mDefs) {
					if (isOwner(defVisible.def)) {
						defVisible.visible = shallShowDef(defVisible.def);
					} else {
						defVisible.visible = false;
					}
				}
			}
			// All
			else {
				for (DefVisible defVisible : mDefs) {
					defVisible.visible = shallShowDef(defVisible.def);
				}
			}
		}

		mGui.resetValues();
	}

	/**
	 * @param def the definition to test if the current user is the owner of
	 * @return true if the current user is the owner of the definition
	 */
	private boolean isOwner(Def def) {
		return def.getRevisedBy().equals(User.getGlobalUser().getUsername());
	}

	/**
	 * If the specified definition shall be shown due to the current filter. Does not take
	 * showMineOnly into account.
	 * @param def the definition to test if it shall be shown or not
	 * @return true if specified definition shall be shown
	 */
	private boolean shallShowDef(Def def) {
		// All names in the filters need to be removed
		for (String word : mAnyFilters) {
			boolean found = false;

			for (CategoryFilterTypes type : CategoryFilterTypes.values()) {
				if (type.getValue(def).contains(word)) {
					found = true;
					break;
				}
			}

			if (!found) {
				return false;
			}
		}


		// Check if category specific names exist
		for (CategoryFilterTypes type : CategoryFilterTypes.values()) {
			for (String word : mCategoryFilters[type.ordinal()]) {
				if (!type.getValue(def).contains(word)) {
					return false;
				}
			}
		}

		return true;
	}

	/**
	 * Enumeration will all filter categories
	 */
	@SuppressWarnings("javadoc")
	enum CategoryFilterTypes {
		CREATOR("revised", "getRevisedBy"),
		NAME("name", "getName"),
		ORIGINAL_CREATOR("creator", "getOriginalCreator");

		/**
		 * Constructor, binds a keyword to the category filter
		 * @param keyword name of the keyword
		 * @param methodName name of the method to retrieve the information from a
		 *        definition
		 */
		private CategoryFilterTypes(String keyword, String methodName) {
			mKeyword = keyword;

			try {
				mMethod = Def.class.getMethod(methodName);
			} catch (SecurityException e) {
				Gdx.app.error("CategoryFilterTypes", e.toString());
			} catch (NoSuchMethodException e) {
				Gdx.app.error("CategoryFilterTypes", e.toString());
			}
		}

		/**
		 * @return keyword of the category
		 */
		public String getKeyword() {
			return mKeyword;
		}

		/**
		 * Uses the appropriate method to get the value from the specified definition
		 * @param def the definition to get the value from.
		 * @return lower-cased value from the definition
		 */
		public String getValue(Def def) {
			if (mMethod != null) {
				try {
					return ((String) mMethod.invoke(def)).toLowerCase();
				} catch (Exception e) {
					Gdx.app.error("CategoryFilterTypes", e.toString());
					return "";
				}
			} else {
				return "";
			}
		}

		/** Keyword associated with the category */
		private String mKeyword;
		/** Method used for retrieving the value of the category */
		private Method mMethod = null;
	}

	/** Event dispatcher */
	private static EventDispatcher mEventDispatcher = EventDispatcher.getInstance();
	/** If the player shall be able to choose another revision for the definition */
	private boolean mCanChooseRevision = false;
	/** Currently selected definition */
	private Def mSelectedDef = null;
	/** The selected revision for the selected definition */
	private int mSelectedRevision = -1;
	/** All the loaded definitions */
	private ArrayList<DefVisible> mDefs = new ArrayList<DefVisible>();
	/** Filter for the definitions, which to show and which to hide */
	private String mFilter = "";
	/** Category filters, all these must be found */
	@SuppressWarnings("unchecked") private ArrayList<String>[] mCategoryFilters = new ArrayList[CategoryFilterTypes.values().length];
	/** Word filters for all categories */
	private ArrayList<String> mAnyFilters = new ArrayList<String>();
	/** Shows only one's own actors, this is the value of the checkbox */
	private boolean mShowMineOnly;
	/** Definition type to select from */
	private ExternalTypes mDefType;
}

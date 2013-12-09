package com.spiddekauga.voider.scene;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.spiddekauga.voider.User;
import com.spiddekauga.voider.resources.Def;
import com.spiddekauga.voider.resources.IResource;
import com.spiddekauga.voider.resources.IResourceRevision;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.resources.ResourceDatabase;
import com.spiddekauga.voider.resources.ResourceItem;
import com.spiddekauga.voider.resources.ResourceNames;
import com.spiddekauga.voider.resources.UndefinedResourceTypeException;
import com.spiddekauga.voider.utils.Pools;

/**
 * Scene for selecting definitions (actors, levels). It will display a scene
 * for the specified type of resource and you will be able to filter the resource
 * for name, creator, etc.
 * 
 * @seciton Outcomes
 * \li DEF_SELECTED selected a definition, the message is the file path for the select
 * \li DEF_SELECT_CANCEL canceled the selection.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class SelectDefScene extends WorldScene {
	/**
	 * Private common constructor
	 * @param defType the definition type that the player want to select.
	 * @param showMineOnly set as true if the player only shall see his/her own
	 * definitions by default, if showMineOnlyCheckbox is set to true the player can
	 * change this value.
	 * @param showMineOnlyCheckbox set to true if you want the scene to show a checkbox
	 * to only display one's own actors.
	 */
	private SelectDefScene(Class<? extends IResource> defType, boolean showMineOnly, boolean showMineOnlyCheckbox) {
		super(new SelectDefGui(showMineOnlyCheckbox), 0);

		mShowMineOnly = showMineOnly;
		mDefType = defType;


		((SelectDefGui)mGui).setSelectDefScene(this);

		for (int i = 0; i < mCategoryFilters.length; ++i) {
			mCategoryFilters[i] = new ArrayList<String>();
		}
	}

	/**
	 * Creates a selector scene with the latest revisions of the specified definition type.
	 * @param defType the definition type that the player want to select.
	 * @param showMineOnly set as true if the player only shall see his/her own
	 * definitions by default, if showMineOnlyCheckbox is set to true the player can
	 * change this value.
	 * @param showMineOnlyCheckbox set to true if you want the scene to show a checkbox
	 * to only display one's own actors.
	 * @param canChooseRevision set this to true if the player should be able to select
	 * another revision of the resource.
	 */
	public SelectDefScene(Class<? extends IResource> defType, boolean showMineOnly, boolean showMineOnlyCheckbox, boolean canChooseRevision) {
		this(defType, showMineOnly, showMineOnlyCheckbox);

		mCanChooseRevision = canChooseRevision;
	}

	/**
	 * Creates a selector scene with specific revisions of some of the resources
	 * @param defType the definition type that the player want to select.
	 * @param showMineOnly set as true if the player only shall see his/her own definitions by default,
	 * if showMineOnlyCheckbox is set to true the player can change this value.
	 * @param showMineOnlyCheckbox set to true if you want the scene to show a checkbox
	 * to only display one's own actors.
	 * @param resourceRevisions specific revisions to use for some resources of this type.
	 */
	@Deprecated
	public SelectDefScene(Class<? extends IResource> defType, boolean showMineOnly, boolean showMineOnlyCheckbox, Map<UUID, Integer> resourceRevisions) {
		this(defType, showMineOnly, showMineOnlyCheckbox);

		mRevisionsToUse = resourceRevisions;
	}

	@Override
	protected void onActivate(Outcomes outcome, Object message) {
		super.onActivate(outcome, message);

		if (outcome == Outcomes.LOADING_SUCCEEDED) {
			try {
				@SuppressWarnings("unchecked")
				ArrayList<Def> defs = (ArrayList<Def>) ResourceCacheFacade.getAll(this, mDefType);

				for (Def def : defs) {
					mDefs.add(new DefVisible(def));
				}

				mGui.resetValues();

				Pools.arrayList.free(defs);
				defs = null;

			} catch (UndefinedResourceTypeException e) {
				Gdx.app.error("SelectDefScene", e.toString());
			}
		}
	}

	@Override
	protected void loadResources() {
		super.loadResources();
		ResourceCacheFacade.load(ResourceNames.UI_GENERAL);
		if (mRevisionsToUse != null) {
			ResourceCacheFacade.loadAllOf(this, mDefType, true, mRevisionsToUse);
		} else {
			ResourceCacheFacade.loadAllOf(this, mDefType, true);
		}
	}

	@Override
	protected void unloadResources() {
		super.unloadResources();
		ResourceCacheFacade.unload(ResourceNames.UI_GENERAL);
		if (mRevisionsToUse != null) {
			ResourceCacheFacade.unloadAllOf(this, mDefType, true, mRevisionsToUse);
		} else {
			ResourceCacheFacade.unloadAllOf(this, mDefType, true);
		}
	}

	@Override
	protected void onResize(int width, int height) {
		super.onResize(width, height);
		mGui.resetValues();
	}

	@Override
	public boolean keyDown(int keycode) {
		// Set level as complete if we want to go back while testing
		if (keycode == Keys.ESCAPE || keycode == Keys.BACK) {
			setOutcome(Outcomes.DEF_SELECT_CANCEL);
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
	 * @param defId id of the definition
	 * @param defRevision definition's revision
	 */
	void setSelectedDef(UUID defId, int defRevision) {
		mSelectedDef = (Def) ResourceCacheFacade.get(this, defId, defRevision);
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
	 * @return true if any def is selected
	 */
	boolean isDefSelected() {
		return mSelectedDef != null;
	}

	/**
	 * @return name of the selected definition. An empty string if no
	 * definition has been selected
	 */
	String getName() {
		if (mSelectedDef != null) {
			return mSelectedDef.getName();
		}
		return "";
	}

	/**
	 * @return current date of the selected definition. An empty string if no
	 * definition has been selected
	 */
	String getDate() {
		if (mSelectedDef != null) {
			return mSelectedDef.getDateString();
		}
		return "";
	}

	/**
	 * @return description of the definiton. An empty string if no
	 * definition has been selected
	 */
	String getDescription() {
		if (mSelectedDef != null) {
			return mSelectedDef.getDescription();
		}
		return "";
	}

	/**
	 * @return creator of the definition. An empty string if no
	 * definition has been selected
	 */
	String getCreator() {
		if (mSelectedDef != null) {
			return mSelectedDef.getCreator();
		}
		return "";
	}

	/**
	 * @return original creator of the definition. An empty string if no
	 * definition has been selected
	 */
	String getOriginalCreator() {
		if (mSelectedDef != null) {
			return mSelectedDef.getOriginalCreator();
		}
		return "";
	}

	/**
	 * @return revision of the definition. An empty string if no
	 * definition has been selected
	 */
	String getRevision() {
		if (mSelectedDef != null) {
			return String.valueOf(mSelectedDef.getRevision());
		}
		return "";
	}

	/**
	 * @return map with all revisions and dates of the current selected resource, null
	 * if none was found.
	 */
	Map<Integer, String> getSelectedResourceRevisionsWithDates() {
		if (mSelectedDef != null) {
			return ResourceDatabase.getResourceRevisionsWithDate(mSelectedDef.getId());
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
			ResourceItem resourceItem = Pools.resourceItem.obtain();
			resourceItem.id = mSelectedDef.getId();
			resourceItem.revision = -1;
			if (mSelectedDef instanceof IResourceRevision) {
				resourceItem.revision = mSelectedDef.getRevision();
			}

			setOutcome(Outcomes.DEF_SELECTED, resourceItem);
		} else {
			/** @todo print an error message */
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
	 * Updates the visibility of the definitions depending on the current filter
	 * and if only our own actors shall be included.
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
		return def.getCreator().equals(User.getNickName());
	}

	/**
	 * If the specified definition shall be shown due to the current filter.
	 * Does not take showMineOnly into account.
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
		CREATOR("creator", "getCreator"),
		NAME("name", "getName"),
		ORIGINAL_CREATOR("originalcreator", "getOriginalCreator");

		/**
		 * Constructor, binds a keyword to the category filter
		 * @param keyword name of the keyword
		 * @param methodName name of the method to retrieve the information
		 * from a definition
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

	/** If the player shall be able to choose another revision for the definition */
	private boolean mCanChooseRevision = false;
	/** Revisions to use for some resources (instead of latest) */
	private Map<UUID, Integer> mRevisionsToUse = null;
	/** Currently selected definition */
	private Def mSelectedDef = null;
	/** All the loaded definitions */
	private ArrayList<DefVisible> mDefs = new ArrayList<DefVisible>();
	/** Filter for the definitions, which to show and which to hide */
	private String mFilter = "";
	/** Category filters, all these must be found */
	@SuppressWarnings("unchecked")
	private ArrayList<String>[] mCategoryFilters = new ArrayList[CategoryFilterTypes.values().length];
	/** Word filters for all categories */
	private ArrayList<String> mAnyFilters = new ArrayList<String>();
	/** Shows only one's own actors, this is the value of the checkbox */
	private boolean mShowMineOnly;
	/** Definition type to select from */
	private Class<? extends IResource> mDefType;
}

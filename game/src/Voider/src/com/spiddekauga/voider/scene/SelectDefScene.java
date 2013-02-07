package com.spiddekauga.voider.scene;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.spiddekauga.voider.resources.Def;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.resources.ResourceNames;
import com.spiddekauga.voider.resources.UndefinedResourceTypeException;

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
	 * Creates a selector scene (with no checkbox)
	 * @param defType the definition type that the user want to select. All these
	 * definition types need to be loaded already!
	 * @param showMineOnly set as true if the player only shall see his/her own
	 * definitions by default, if showMineOnlyCheckbox is set to true the player can
	 * changed this value.
	 * @param showMineOnlyCheckbox set to true if you want the scene to show a checkbox
	 * to only display one's own actors.
	 */
	public SelectDefScene(Class<?> defType, boolean showMineOnly, boolean showMineOnlyCheckbox) {
		super(new SelectDefGui(showMineOnlyCheckbox));

		mShowMineOnly = showMineOnly;
		mDefType = defType;

		((SelectDefGui)mGui).setSelectDefScene(this);
	}

	@Override
	public void onActivate(Outcomes outcome, String message) {
		if (outcome  == Outcomes.LOADING_SUCCEEDED) {
			try {
				@SuppressWarnings("unchecked")
				List<Def> defs = (List<Def>) ResourceCacheFacade.get(mDefType);

				for (Def def : defs) {
					mDefs.add(new DefVisible(def));
				}

			} catch (UndefinedResourceTypeException e) {
				Gdx.app.error("SelectDefScene", e.toString());
			}


			mGui.initGui();
		}
	}

	@Override
	public void loadResources() {
		ResourceCacheFacade.load(ResourceNames.EDITOR_BUTTONS);
	}

	@Override
	public void unloadResources() {
		ResourceCacheFacade.unload(ResourceNames.EDITOR_BUTTONS);
	}

	@Override
	public boolean hasResources() {
		return true;
	}

	@Override
	public void onResize(int width, int height) {
		super.onResize(width, height);

		((SelectDefGui)mGui).refillDefTable();
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
		mFilter = filter;

		/** @todo update loaded definitions to show/hide */
	}

	/**
	 * @return all loaded definitions
	 */
	ArrayList<DefVisible> getDefs() {
		return mDefs;
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

	/** All the loaded definitions */
	private ArrayList<DefVisible> mDefs = new ArrayList<DefVisible>();
	/** Filter for the definitions, which to show and which to hide */
	private String mFilter = "";
	/** Shows only one's own actors, this is the value of the checkbox */
	private boolean mShowMineOnly;
	/** Definition type to select from */
	private Class<?> mDefType;
}

package com.spiddekauga.voider.scene;

import com.badlogic.gdx.Input.Keys;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.resources.ResourceNames;

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

	/** Shows only one's own actors, this is the value of the checkbox */
	private boolean mShowMineOnly;
	/** Definition type to select from */
	private Class<?> mDefType;
}

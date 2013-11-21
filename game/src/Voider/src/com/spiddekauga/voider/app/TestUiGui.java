package com.spiddekauga.voider.app;

import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.utils.scene.ui.Label;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.resources.ResourceNames;
import com.spiddekauga.voider.scene.Gui;

/**
 * All UI elements.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class TestUiGui extends Gui {
	@Override
	public void initGui() {
		super.initGui();

		mSkin = ResourceCacheFacade.get(ResourceNames.UI_GENERAL);
		mMainTable.setTableAlign(Horizontal.LEFT, Vertical.TOP);
		mMainTable.setRowAlign(Horizontal.LEFT, Vertical.TOP);
		mMainTable.setCellPaddingDefault(Config.Gui.PADDING_DEFAULT);

		initButtons();
		initCheckBoxRadioButtons();
		initSliders();
	}

	/**
	 * Initialize buttons
	 */
	private void initButtons() {
		mMainTable.row();

		// Default
		Button button = new TextButton("Test default", mSkin, "default");
		mMainTable.add(button);

		// Checkable
		button = new TextButton("Test checkable", mSkin, "toggle");
		mMainTable.add(button);

		// Up
		button = new TextButton("Up", mSkin, "up");
		mMainTable.add(button);

		// Down
		button = new TextButton("Down", mSkin, "down");
		mMainTable.add(button);

		// Checked
		button = new TextButton("Checked", mSkin, "selected");
		mMainTable.add(button);

		// Over
		button = new TextButton("Hover", mSkin, "over");
		mMainTable.add(button);

		// Disabled
		button = new TextButton("Disabled", mSkin, "default");
		button.setDisabled(true);
		mMainTable.add(button);
	}

	/**
	 * Initialize check box / radio buttons
	 */
	private void initCheckBoxRadioButtons() {
		mMainTable.row();

		// Checkboxes
		Button button = new CheckBox("Checkbox 1", mSkin, "default");
		mMainTable.add(button);

		button = new CheckBox("Checkbox 2", mSkin, "default");
		mMainTable.add(button);

		// Radio buttons
		mMainTable.row();
		ButtonGroup buttonGroup = new ButtonGroup();
		button = new CheckBox("Radio 1", mSkin, "radio");
		buttonGroup.add(button);
		mMainTable.add(button);

		button = new CheckBox("Radio 2", mSkin, "radio");
		buttonGroup.add(button);
		mMainTable.add(button);
	}

	/**
	 * Initialize sliders
	 */
	private void initSliders() {
		mMainTable.row();

		// Default
		Label label = new Label("No knobs:", mSkin);
		mMainTable.add(label).setPadRight(10);
		Slider slider = new Slider(0, 100, 1, false, mSkin, "default");
		mMainTable.add(slider);

		// Loading bar
		mMainTable.row();
		label = new Label("Loading bar:", mSkin);
		mMainTable.add(label).setPadRight(10);
		slider = new Slider(0, 100, 1, false, mSkin, "loading-bar");
		mLoadingBar = slider;
		mMainTable.add(slider);
	}

	/**
	 * Sets the loading bar to the specified value
	 * @param loadedValue the amount loaded
	 */
	void setLoadingBar(float loadedValue) {
		mLoadingBar.setValue(loadedValue);
	}

	/** Skin for general */
	Skin mSkin = null;
	/** Loading bar */
	Slider mLoadingBar = null;
}

package com.spiddekauga.voider.app;

import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.utils.scene.ui.ButtonListener;
import com.spiddekauga.utils.scene.ui.Label;
import com.spiddekauga.utils.scene.ui.MsgBoxExecuter;
import com.spiddekauga.utils.scene.ui.TextFieldListener;
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
		initTextFields();
		initCheckBoxRadioButtons();
		initSliders();
		initWindows();
		initScrollPane();
		initList();
		initSelectionBox();
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
	 * Initialize text fields
	 */
	private void initTextFields() {
		mMainTable.row();

		TextField textField = new TextField("", mSkin);
		new TextFieldListener(textField, "Default Text", null);
		mMainTable.add(textField).setPadRight(10);

		textField = new TextField("", mSkin);
		new TextFieldListener(textField, "", null);
		mMainTable.add(textField);
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
		button.setChecked(true);
		mMainTable.add(button).setPadRight(50);

		// Radio buttons
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
		Label label = new Label("Slider:", mSkin);
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
	 * Initialize windows
	 */
	private void initWindows() {
		mMainTable.row();

		// No title
		Window window = new Window("", mSkin, "default");
		window.add("No title");
		mMainTable.add(window);

		// Title
		window = new Window("Title", mSkin, "title");
		window.add("Has title");
		mMainTable.add(window);

		// Modal window, button
		Button button = new TextButton("Modal window (press)", mSkin);
		mMainTable.add(button);
		new ButtonListener(button) {
			@Override
			protected void onPressed() {
				MsgBoxExecuter msgBox = getFreeMsgBox(false);
				msgBox.content("Modal window");
				msgBox.addCancelButtonAndKeys("OK");
				showMsgBox(msgBox);
			}
		};

		// Modal title window, button
		button = new TextButton("Modal title window (press)", mSkin);
		mMainTable.add(button);
		new ButtonListener(button) {
			@Override
			protected void onPressed() {
				MsgBoxExecuter msgBox = getFreeMsgBox(true);
				msgBox.setTitle("Title");
				msgBox.content("Modal window");
				msgBox.addCancelButtonAndKeys("OK");
				showMsgBox(msgBox);
			}
		};
	}

	/**
	 * Initialize scroll pane
	 */
	private void initScrollPane() {
		mMainTable.row();

		Label label = new Label("", mSkin);
		label.setText("Scroll pane\n"
				+ "With background\n"
				+ "Uses same background as window\n"
				+ "aeu aseus a saosuaheu aseou\n"
				+ "aoseuthaoes ase atoeuh saoseuhaoesn auh\n"
				+ "aoeusah  sTHUenuhoeusSNusoruc.sjkbkt \n"
				+ "aoesuasoe tuo uasouarnc.uhoa.ucasuasoethu .usao.cuh.\n"
				+ "aoseunat hao.uh.cuchasoetu\n"
				+ "aoneuthaose .ruca,huaosetuhaoetkmaenkh\n"
				+ "aoneu.sa,oc  sa has rh.usa,o.ucas c  u-e-isk\n"
				+ "aoseutah a.ruchktqjhk-soeu\n");
		ScrollPane scrollPane = new ScrollPane(label, mSkin, "background");
		mMainTable.add(scrollPane);

		label = new Label("", mSkin);
		label.setText("Scroll pane\n"
				+ "Force scrollbars\n"
				+ "Without background\n"
				+ "aeu aseus a saosuaheu aseou\n"
				+ "aoseuthaoes ase atoeuh saoseuhaoesn auh\n"
				+ "aoeusah  sTHUenuhoeusSNusoruc.sjkbkt \n"
				+ "aoesuasoe tuo uasouarnc.uhoa.ucasuasoethu .usao.cuh.\n"
				+ "aoseunat hao.uh.cuchasoetu\n"
				+ "aoneuthaose .ruca,huaosetuhaoetkmaenkh\n"
				+ "aoneu.sa,oc  sa has rh.usa,o.ucas c  u-e-isk\n"
				+ "aoseutah a.ruchktqjhk-soeu\n");
		scrollPane = new ScrollPane(label, mSkin);
		scrollPane.setForceScroll(true, true);
		scrollPane.setFadeScrollBars(false);
		mMainTable.add(scrollPane);
	}

	/**
	 * Initialize list style
	 */
	private void initList() {
		Object[] items = new Object[10];

		for (int i = 0; i < items.length; ++i) {
			items[i] = "List item " + (i+1);
		}

		List list = new List(items, mSkin);
		ScrollPane scrollPane = new ScrollPane(list, mSkin, "background");
		mMainTable.add(scrollPane);
	}

	/**
	 * Initialize selection box
	 */
	private void initSelectionBox() {
		Object[] items = new Object[40];

		for (int i = 0; i < items.length; ++i) {
			items[i] = "Select item " + (i+1);
		}

		SelectBox selectBox = new SelectBox(items, mSkin);
		mMainTable.add(selectBox);
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

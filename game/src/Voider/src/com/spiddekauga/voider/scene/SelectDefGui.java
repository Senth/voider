package com.spiddekauga.voider.scene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputEvent.Type;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox.CheckBoxStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.utils.scene.ui.AlignTable;
import com.spiddekauga.utils.scene.ui.ButtonListener;
import com.spiddekauga.utils.scene.ui.Cell;
import com.spiddekauga.utils.scene.ui.HideManual;
import com.spiddekauga.utils.scene.ui.Label;
import com.spiddekauga.utils.scene.ui.Label.LabelStyle;
import com.spiddekauga.utils.scene.ui.Row;
import com.spiddekauga.utils.scene.ui.TextFieldListener;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.resources.ResourceNames;
import com.spiddekauga.voider.scene.SelectDefScene.DefVisible;

/**
 * GUI for Select Definition Scene. This creates a border at
 * the top for filtering search, and an optionally checkbox for
 * only showing the player's own actors.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class SelectDefGui extends Gui {
	/**
	 * Creates the GUI (but does not init it) for the select actor
	 * @param showMineOnlyCheckbox set to true if you want the scene to show a checkbox
	 * to only display one's own actors.
	 */
	public SelectDefGui(boolean showMineOnlyCheckbox) {
		mShowMineOnlyCheckbox = showMineOnlyCheckbox;
	}

	/**
	 * Sets the select def scene this GUI is bound to.
	 * @param selectDefScene scene this GUI is bound to
	 */
	public void setSelectDefScene(SelectDefScene selectDefScene) {
		mSelectDefScene = selectDefScene;
	}

	@Override
	public void initGui() {
		super.initGui();

		mMainTable.setTableAlign(Horizontal.LEFT, Vertical.TOP);
		mMainTable.setRowAlign(Horizontal.LEFT, Vertical.TOP);
		mMainTable.setScalable(false);
		mDefTable.setPreferences(mMainTable);
		mDefTable.setKeepSize(true);
		mInfoPanel.setPreferences(mMainTable);
		mInfoPanel.setRowPaddingDefault(2, 2, 2, 2);
		mInfoPanel.setKeepSize(true);

		initSearchBar();
		initDefTable();
		initInfoPanel();

		resetValues();
	}

	/**
	 * Initializes the search bar at the top
	 */
	private void initSearchBar() {
		Skin editorSkin = ResourceCacheFacade.get(ResourceNames.UI_GENERAL);

		TextFieldStyle textFieldStyle = editorSkin.get("default", TextFieldStyle.class);
		CheckBoxStyle checkBoxStyle = editorSkin.get("default", CheckBoxStyle.class);

		TextField textField = new TextField("", textFieldStyle);
		Row row = mMainTable.row();
		row.setFillWidth(true);
		Cell cell = mMainTable.add(textField);
		cell.setFillWidth(true);
		new TextFieldListener(textField, "Filter", null) {
			@Override
			protected void onChange(String newText) {
				mSelectDefScene.setFilter(newText);
			}
		};

		if (mShowMineOnlyCheckbox) {
			CheckBox checkBox = new CheckBox("Only mine", checkBoxStyle);
			checkBox.setChecked(mSelectDefScene.shallShowMineOnly());
			new ButtonListener(checkBox) {
				@Override
				protected void onChecked(boolean checked) {
					mSelectDefScene.setShowMineOnly(checked);
				}
			};
			mMainTable.add(checkBox);
		}


	}

	/**
	 * Initializes the definition table
	 */
	private void initDefTable() {
		mMainTable.row().setFillWidth(true).setFillHeight(true);
		mMainTable.add(mDefTable).setFillWidth(true);
	}

	/**
	 * Initializes info panel to the right
	 */
	private void initInfoPanel() {
		mInfoPanel.setName("infopanel");
		mMainTable.add(mInfoPanel).setFillHeight(true);
		mInfoPanelHider.addToggleActor(mInfoPanel);

		Skin editorSkin = ResourceCacheFacade.get(ResourceNames.UI_GENERAL);
		TextButtonStyle buttonStyle = editorSkin.get("default", TextButtonStyle.class);
		LabelStyle labelStyle = editorSkin.get("default", LabelStyle.class);

		// Name
		Label label = new Label("", labelStyle);
		mWidgets.infoPanel.name = label;
		mInfoPanel.add(label);

		// Date
		mInfoPanel.row();
		label = new Label("", labelStyle);
		mWidgets.infoPanel.date = label;
		mInfoPanel.add(label);

		// Description
		mInfoPanel.row();
		label = new Label("Description", labelStyle);
		mInfoPanel.add(label);
		mInfoPanel.row();
		label = new Label("", labelStyle);
		label.setWrap(true);
		mWidgets.infoPanel.description = label;
		mInfoPanel.add(label);

		// Author
		mInfoPanel.row();
		label = new Label("Creator", labelStyle);
		mInfoPanel.add(label);
		label = new Label("", labelStyle);
		mWidgets.infoPanel.creator = label;
		mInfoPanel.add(label);

		// Original author
		mInfoPanel.row();
		label = new Label("Orig. Creator", labelStyle);
		mInfoPanel.add(label);
		label = new Label("", labelStyle);
		mWidgets.infoPanel.originalCreator = label;
		mInfoPanel.add(label);

		// Revision
		mInfoPanel.row();
		label = new Label("Revision", labelStyle);
		mInfoPanel.add(label);
		label = new Label("", labelStyle);
		mWidgets.infoPanel.revision = label;
		mInfoPanel.add(label);

		// Version
		mInfoPanel.row().setFillHeight(true);
		label = new Label("Version", labelStyle);
		mInfoPanel.add(label);
		label = new Label("", labelStyle);
		mWidgets.infoPanel.version = label;
		mInfoPanel.add(label);


		// Padding
		mInfoPanel.row().setFillHeight(true);


		// Select another revision
		mInfoPanel.row();
		TextButton button = new TextButton("Select rev.", buttonStyle);
		new ButtonListener(button) {
			@Override
			protected void onPressed() {
				/** @todo show message box where the player can select another revision */
			}
		};
		mInfoPanel.add(button);

		// Load
		button = new TextButton("Load", buttonStyle);
		new ButtonListener(button) {
			@Override
			protected void onPressed() {
				mSelectDefScene.loadDef();
			}
		};
		mInfoPanel.add(button);

		// Cancel
		button = new TextButton("Cancel", buttonStyle);
		new ButtonListener(button) {
			@Override
			protected void onPressed() {
				mSelectDefScene.cancel();
			}
		};
		mInfoPanel.add(button);
	}

	@Override
	public void resetValues() {
		Skin editorSkin = ResourceCacheFacade.get(ResourceNames.UI_GENERAL);
		TextButtonStyle toggleStyle = editorSkin.get("toggle", TextButtonStyle.class);


		float floatPerRow = Gdx.graphics.getWidth() / Config.Editor.SELECT_DEF_WIDTH_MAX;
		floatPerRow += 0.5f;
		int cellsPerRow = (int) floatPerRow;

		mDefTable.dispose();

		int cellCount = 0;
		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.setMinCheckCount(0);
		for (DefVisible defVisible : mSelectDefScene.getDefs()) {
			if (defVisible.visible) {
				if (cellCount == 0) {
					Row row = mDefTable.row();
					row.setEqualCellSize(true);
					row.setFillWidth(true);
				}

				TextButton button = new TextButton(defVisible.def.getName(), toggleStyle);
				button.setName(defVisible.def.getId().toString());
				button.addListener(mDefListener);

				/** @todo cut text if too long */
				buttonGroup.add(button);
				Cell cell = mDefTable.add(button);
				cell.setFillWidth(true);

				++cellCount;

				if (cellCount == cellsPerRow) {
					cellCount = 0;
				}
			}
		}


		// Add empty cells to create equal spacing
		if (cellCount != 0) {
			mDefTable.add(cellsPerRow - cellCount);
		}


		// Reset values of the info panel.
		mWidgets.infoPanel.name.setText(mSelectDefScene.getName());
		mWidgets.infoPanel.date.setText(mSelectDefScene.getDate());
		mWidgets.infoPanel.description.setText(mSelectDefScene.getDescription());
		mWidgets.infoPanel.creator.setText(mSelectDefScene.getCreator());
		mWidgets.infoPanel.originalCreator.setText(mSelectDefScene.getOriginalCreator());
		mWidgets.infoPanel.revision.setText(mSelectDefScene.getRevision());
		mWidgets.infoPanel.version.setText(mSelectDefScene.getVersion());
	}

	/**
	 * Event listener for buttons
	 */
	private EventListener mDefListener = new EventListener() {
		@Override
		public boolean handle(Event event) {
			if (event.getListenerActor() != null && event instanceof InputEvent) {
				InputEvent inputEvent = (InputEvent)event;
				if (inputEvent.getType() == Type.touchDown) {
					String defName = event.getListenerActor().getName();

					// Pressed same twice -> select this
					if (mSelectDefScene.isDefSelected(defName)) {
						mSelectDefScene.loadDef();
					} else {
						mSelectDefScene.setSelectedDef(defName);
						resetValues();
					}

					mInfoPanelHider.show();
				}
			}
			return true;
		}
	};
	/** Info panel */
	private AlignTable mInfoPanel = new AlignTable();
	/** Info panel hider */
	private HideManual mInfoPanelHider = new HideManual();
	/** Table for all the definitions */
	private AlignTable mDefTable = new AlignTable();
	/** If the checkbox that only shows one's own actors shall be shown */
	private boolean mShowMineOnlyCheckbox;
	/** SelectDefScene this GUI is bound to */
	private SelectDefScene mSelectDefScene = null;
	/** Inner widgets */
	private InnerWidgets mWidgets = new InnerWidgets();

	@SuppressWarnings("javadoc")
	private static class InnerWidgets {
		InfoPanel infoPanel = new InfoPanel();

		static class InfoPanel {
			Label name = null;
			Label date = null;
			Label description = null;
			Label creator = null;
			Label originalCreator = null;
			Label revision = null;
			Label version = null;
		}
	}
}

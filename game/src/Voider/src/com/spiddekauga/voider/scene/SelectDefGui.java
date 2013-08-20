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
import com.spiddekauga.utils.scene.ui.Row;
import com.spiddekauga.utils.scene.ui.TextFieldListener;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.resources.ResourceNames;
import com.spiddekauga.voider.scene.Scene.Outcomes;
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
		mMainTable.setRowAlign(Horizontal.LEFT, Vertical.MIDDLE);
		mMainTable.setScalable(false);
		mDefTable.setPreferences(mMainTable);
		mDefTable.setKeepSize(true);

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

		row = mMainTable.row();
		row.setFillWidth(true);
		cell = mMainTable.add(mDefTable);
		cell.setFillWidth(true);

		resetValues();
	}

	@Override
	public void resetValues() {
		Skin editorSkin = ResourceCacheFacade.get(ResourceNames.UI_GENERAL);
		TextButtonStyle toggleStyle = editorSkin.get("toggle", TextButtonStyle.class);


		float floatPerRow = Gdx.graphics.getWidth() / Config.Editor.SELECT_DEF_WIDTH_MAX;
		floatPerRow += 0.5f;
		int cellsPerRow = (int) floatPerRow;

		mDefTable.clear();

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
				//				cell.setFillHeight(true);
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
					if (mLastCheckedDef.equals(defName)) {
						mSelectDefScene.setOutcome(Outcomes.DEF_SELECTED, defName);
					} else {
						mLastCheckedDef = defName;
					}
				}
			}
			return true;
		}
	};
	/** Last pressed definition (used to load when presesd twice) */
	private String mLastCheckedDef = "";

	/** Table for all the definitions */
	private AlignTable mDefTable = new AlignTable();
	/** If the checkbox that only shows one's own actors shall be shown */
	private boolean mShowMineOnlyCheckbox;
	/** SelectDefScene this GUI is bound to */
	private SelectDefScene mSelectDefScene = null;
}

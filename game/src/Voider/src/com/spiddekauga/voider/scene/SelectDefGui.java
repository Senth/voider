package com.spiddekauga.voider.scene;

import java.util.ArrayList;
import java.util.UUID;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputEvent.Type;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox.CheckBoxStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.List.ListStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.ScrollPaneStyle;
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
import com.spiddekauga.utils.scene.ui.MsgBoxExecuter;
import com.spiddekauga.utils.scene.ui.ResourceTextureButton;
import com.spiddekauga.utils.scene.ui.TextFieldListener;
import com.spiddekauga.voider.editor.commands.CSelectDefSetRevision;
import com.spiddekauga.voider.game.actors.ActorDef;
import com.spiddekauga.voider.resources.Def;
import com.spiddekauga.voider.resources.InternalNames;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.resources.RevisionInfo;
import com.spiddekauga.voider.resources.SkinNames;
import com.spiddekauga.voider.scene.SelectDefScene.DefVisible;

/**
 * GUI for Select Definition Scene. This creates a border at
 * the top for filtering search, and an optionally checkbox for
 * only showing the player's own actors.
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
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

		mMainTable.setAlign(Horizontal.LEFT, Vertical.TOP);
		mMainTable.setKeepSize(true);
		mDefTable.setPreferences(mMainTable);
		mDefTable.setKeepSize(true);
		mDefTable.setName("DefTable");
		mInfoPanel.setPreferences(mMainTable);
		mInfoPanel.setKeepSize(true);
		mInfoPanel.setName("InfoPanel");

		initSearchBar();
		initDefTable();
		initInfoPanel();
		initSelectRevision();

		resetValues();
	}

	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
		mMainTable.setSize(width, height);
		resetValues();
	}

	/**
	 * Initializes the search bar at the top
	 */
	private void initSearchBar() {
		Skin editorSkin = ResourceCacheFacade.get(InternalNames.UI_GENERAL);

		TextFieldStyle textFieldStyle = editorSkin.get("default", TextFieldStyle.class);
		CheckBoxStyle checkBoxStyle = editorSkin.get("default", CheckBoxStyle.class);

		TextField textField = new TextField("", textFieldStyle);
		mMainTable.row().setFillWidth(true);
		mMainTable.add(textField).setFillWidth(true);
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
		mMainTable.add(mDefTable).setFillHeight(true).setFillWidth(true).setAlign(Horizontal.LEFT, Vertical.TOP);
	}

	/**
	 * Initializes info panel to the right
	 */
	private void initInfoPanel() {
		float panelWidth = (Float)SkinNames.getResource(SkinNames.General.SELECT_DEF_INFO_WIDTH);
		mMainTable.add(mInfoPanel).setFillHeight(true).setWidth(panelWidth);
		mInfoPanelHider.addToggleActor(mInfoPanel);

		TextButtonStyle buttonStyle = SkinNames.getResource(SkinNames.General.TEXT_BUTTON_PRESS);
		LabelStyle labelStyle = SkinNames.getResource(SkinNames.General.LABEL_DEFAULT);

		// Image
		mInfoPanel.row(Horizontal.CENTER, Vertical.MIDDLE);
		Image image = new Image();
		mWidgets.infoPanel.image = image;
		mInfoPanel.add(image).setSize(panelWidth*0.7f, panelWidth*0.7f);

		// Name
		mInfoPanel.row();
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

		// Padding
		mInfoPanel.row().setFillHeight(true);


		// Select another revision
		mInfoPanel.row(Horizontal.RIGHT, Vertical.BOTTOM).setFillWidth(true);
		if (mSelectDefScene.canChooseRevision()) {
			TextButton button = new TextButton("Select rev.", buttonStyle);
			mWidgets.infoPanel.selectRevision = button;
			new ButtonListener(button) {
				@Override
				protected void onPressed() {
					showSelectRevisionMsgBox();
				}
			};
			mInfoPanel.add(button);
			mInfoPanel.add().setFillWidth(true);
		}


		// Load
		TextButton button = new TextButton("Load", buttonStyle);
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
		occupateDefTable();

		resetInfoPanel();
	}

	/**
	 * Occupate def table with definitions.
	 */
	void occupateDefTable() {
		TextButtonStyle toggleStyle = SkinNames.getResource(SkinNames.General.TEXT_BUTTON_TOGGLE);
		ImageButtonStyle imageButtonStyle = SkinNames.getResource(SkinNames.General.IMAGE_BUTTON_TOGGLE);

		float floatPerRow = (Gdx.graphics.getWidth() - (Float)SkinNames.getResource(SkinNames.General.SELECT_DEF_INFO_WIDTH));

		if (mSelectDefScene.isDefDrawable()) {
			floatPerRow /= (Float)SkinNames.getResource(SkinNames.General.SELECT_DEF_IMAGE_WIDTH_MAX);
		} else {
			floatPerRow /= (Float)SkinNames.getResource(SkinNames.General.SELECT_DEF_TEXT_WIDTH_MAX);
		}

		floatPerRow += 0.5f;
		int cellsPerRow = (int) floatPerRow;

		mDefTable.dispose();

		int cellCount = 0;
		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.setMinCheckCount(0);
		for (DefVisible defVisible : mSelectDefScene.getDefs()) {
			if (defVisible.visible) {
				if (cellCount == 0) {
					mDefTable.row().setEqualCellSize(true).setFillWidth(true);
				}

				Button button;
				if (mSelectDefScene.isDefDrawable()) {
					button = new ResourceTextureButton((ActorDef) defVisible.def, imageButtonStyle);
				} else {
					button = new TextButton(defVisible.def.getName(), toggleStyle);
					/** @todo cut text if too long */
				}
				button.setName(defVisible.def.getId().toString() + "_" + defVisible.def.getRevision());
				button.addListener(mDefListener);


				buttonGroup.add(button);
				Cell cell = mDefTable.add(button).setFillWidth(true);

				if (mSelectDefScene.isDefDrawable()) {
					cell.setBoxShaped(true);
				}

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

		mDefTable.invalidateHierarchy();
	}

	/**
	 * Resets the info panel
	 */
	void resetInfoPanel() {
		mWidgets.infoPanel.image.setDrawable(mSelectDefScene.getDrawable());
		mWidgets.infoPanel.name.setText(mSelectDefScene.getName());
		mWidgets.infoPanel.date.setText(mSelectDefScene.getDate());
		mWidgets.infoPanel.description.setText(mSelectDefScene.getDescription());
		mWidgets.infoPanel.creator.setText(mSelectDefScene.getCreator());
		mWidgets.infoPanel.originalCreator.setText(mSelectDefScene.getOriginalCreator());
		mWidgets.infoPanel.revision.setText(mSelectDefScene.getRevisionString());

		if (mWidgets.infoPanel.selectRevision != null) {
			if (mSelectDefScene.isDefSelected()) {
				mWidgets.infoPanel.selectRevision.setDisabled(false);
			} else {
				mWidgets.infoPanel.selectRevision.setDisabled(true);
			}
		}

		mInfoPanelHider.show();

		mInfoPanel.layout();
		mInfoPanel.layout();
		mInfoPanel.invalidateHierarchy();
	}

	/**
	 * Initialize select revision
	 */
	private void initSelectRevision() {
		String[] revisions = new String[0];
		List list = new List(revisions, (ListStyle)SkinNames.getResource(SkinNames.General.LIST_DEFAULT));
		mWidgets.revisionBox.list = list;
		mWidgets.revisionBox.scrollPane = new ScrollPane(list, (ScrollPaneStyle)SkinNames.getResource(SkinNames.General.SCROLL_PANE_DEFAULT));
		mRevisionTable.setPreferences(mMainTable);
		mRevisionTable.row().setFillHeight(true).setFillWidth(true);
		mRevisionTable.add(mWidgets.revisionBox.scrollPane).setSize(100,100).setFillHeight(true).setFillWidth(true);
	}

	/**
	 * Updates the revision list
	 */
	private void updateRevisionList() {
		ArrayList<RevisionInfo> resourceRevisions = mSelectDefScene.getSelectedResourceRevisionsWithDates();

		String[] revisions;
		if (resourceRevisions != null) {
			revisions = new String[resourceRevisions.size()];

			// Calculate number length
			String latestRevision = String.valueOf(revisions.length);
			int revisionStringLength = latestRevision.length();

			for (int i = 0; i < revisions.length; ++i) {
				int revisionInt = revisions.length - i;
				RevisionInfo revisionInfo = resourceRevisions.get(i);
				revisions[i] = String.format("%0" + revisionStringLength + "d  %d - %s", revisionInt, revisionInfo.revision, Def.getDateString(revisionInfo.date));
			}
		} else {
			revisions = new String[0];
		}

		mWidgets.revisionBox.list.setItems(revisions);
	}

	/**
	 * Show select revision message box
	 */
	private void showSelectRevisionMsgBox() {
		MsgBoxExecuter msgBox = getFreeMsgBox(true);

		msgBox.setTitle("Select another revision");
		msgBox.content(mRevisionTable);

		mRevisionTable.setKeepSize(true);
		mRevisionTable.setSize(Gdx.graphics.getWidth() * 0.6f, Gdx.graphics.getHeight() * 0.6f);

		updateRevisionList();

		// Get latest revision number
		int latestRevision = mWidgets.revisionBox.list.getItems().length;

		msgBox.button("Latest", new CSelectDefSetRevision(latestRevision, mSelectDefScene));
		msgBox.button("Select", new CSelectDefSetRevision(mWidgets.revisionBox.list, mSelectDefScene));
		msgBox.addCancelButtonAndKeys();

		showMsgBox(msgBox);

		getStage().setScrollFocus(mWidgets.revisionBox.list);
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

					UUID id = null;
					int revision = -1;
					String[] splitStrings = defName.split("_");
					if (splitStrings.length == 2) {
						id = UUID.fromString(splitStrings[0]);
						revision = Integer.parseInt(splitStrings[1]);
					} else {
						Gdx.app.error("SelectDefGui", "Split string does not contain one _");
					}

					// Pressed same twice -> select this
					if (mSelectDefScene.isDefSelected(id)) {
						mSelectDefScene.loadDef();
					} else {
						mSelectDefScene.setSelectedDef(id, revision);
						resetInfoPanel();
					}
				}
			}
			return true;
		}
	};

	/** Info panel */
	private AlignTable mInfoPanel = new AlignTable();
	/** Info panel hider */
	private HideManual mInfoPanelHider = new HideManual();
	/** Msgbox table */
	private AlignTable mRevisionTable = new AlignTable();
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
		RevisionBox revisionBox = new RevisionBox();

		static class RevisionBox {
			List list = null;
			ScrollPane scrollPane = null;
		}

		static class InfoPanel {
			Image image = null;
			Label name = null;
			Label date = null;
			Label description = null;
			Label creator = null;
			Label originalCreator = null;
			Label revision = null;
			Button selectRevision = null;
		}
	}
}

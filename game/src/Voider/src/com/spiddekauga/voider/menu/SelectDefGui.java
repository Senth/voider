package com.spiddekauga.voider.menu;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.List.ListStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.ScrollPaneStyle;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.utils.scene.ui.AlignTable;
import com.spiddekauga.utils.scene.ui.Background;
import com.spiddekauga.utils.scene.ui.ButtonListener;
import com.spiddekauga.utils.scene.ui.HideManual;
import com.spiddekauga.utils.scene.ui.MsgBoxExecuter;
import com.spiddekauga.utils.scene.ui.ResourceTextureButton;
import com.spiddekauga.utils.scene.ui.TextFieldListener;
import com.spiddekauga.utils.scene.ui.UiFactory.CheckBoxStyles;
import com.spiddekauga.utils.scene.ui.UiFactory.TextButtonStyles;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.editor.commands.CSelectDefSetRevision;
import com.spiddekauga.voider.menu.SelectDefScene.DefVisible;
import com.spiddekauga.voider.network.entities.RevisionEntity;
import com.spiddekauga.voider.resources.SkinNames;
import com.spiddekauga.voider.scene.Gui;
import com.spiddekauga.voider.utils.User;

/**
 * GUI for Select Definition Scene. This creates a border at the top for filtering search,
 * and an optionally checkbox for only showing the player's own actors.
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class SelectDefGui extends Gui {
	/**
	 * Creates the GUI (but does not init it) for the select actor
	 * @param showMineOnlyCheckbox set to true if you want the scene to show a checkbox to
	 *        only display one's own actors.
	 * @param buttonText what text to display on load/play ?
	 */
	public SelectDefGui(boolean showMineOnlyCheckbox, String buttonText) {
		mShowMineOnlyCheckbox = showMineOnlyCheckbox;
		mButtonText = buttonText;
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

		initSearchBar();
		initRightPanel();
		initInfo();
		initActions();
		initTopBar();
		initContent();
		initSelectRevision();

		mWidgets.info.hider.hide();

		getStage().setScrollFocus(mWidgets.content.scrollPane);
	}

	@Override
	public void dispose() {
		super.dispose();
	}

	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
		dispose();
		initGui();
		mWidgets.info.table.dispose();
		mWidgets.search.table.dispose();
		mWidgets.rightPanel.dispose();
		mWidgets.content.table.dispose();
	}

	@Override
	public void resetValues() {
		super.resetValues();

		resetContent(mSelectDefScene.getDefs());
	}

	/**
	 * Initializes the top bar
	 */
	private void initTopBar() {
		mWidgets.topBar = new Background((Color) SkinNames.getResource(SkinNames.GeneralVars.WIDGET_BACKGROUND_COLOR));
		mWidgets.topBar.setHeight((Float) SkinNames.getResource(SkinNames.GeneralVars.BAR_UPPER_LOWER_HEIGHT));
		mWidgets.topBar.setWidth(Gdx.graphics.getWidth());
		getStage().addActor(mWidgets.topBar);
		mWidgets.topBar.setZIndex(0);
		mWidgets.topBar.setPosition(0, Gdx.graphics.getHeight() - mWidgets.topBar.getHeight());
	}

	/**
	 * Initializes search bar
	 */
	private void initSearchBar() {
		AlignTable table = mWidgets.search.table;
		table.setName("search");
		table.dispose(true);
		table.setAlignTable(Horizontal.LEFT, Vertical.TOP);
		table.setAlignRow(Horizontal.LEFT, Vertical.MIDDLE);
		table.setMargin(mUiFactory.getStyles().vars.paddingOuter);
		getStage().addActor(table);

		TextFieldListener textFieldListener = new TextFieldListener() {
			@Override
			protected void onChange(String newText) {
				if (newText.equals("Search")) {
					mSelectDefScene.setFilter("");
				} else {
					mSelectDefScene.setFilter(newText);
				}
			}
		};
		mUiFactory.addTextField(null, false, "Search", textFieldListener, table, null);
		table.getRow().setFillWidth(true);
		table.getCell().resetWidth().setFillWidth(true);
		mUiFactory.addCheckboxPadding(table);

		if (mShowMineOnlyCheckbox) {
			ButtonGroup buttonGroup = new ButtonGroup();

			mUiFactory.addCheckBox("All", CheckBoxStyles.RADIO, null, buttonGroup, table);
			mUiFactory.addCheckboxPadding(table);
			ButtonListener buttonListener = new ButtonListener() {
				@Override
				protected void onChecked(boolean checked) {
					mSelectDefScene.setShowMineOnly(checked);
				}
			};
			mUiFactory.addCheckBox("Mine", CheckBoxStyles.RADIO, buttonListener, buttonGroup, table);
			mUiFactory.addCheckboxPadding(table);
		}
	}

	/**
	 * Initializes info panel
	 */
	private void initInfo() {
		mWidgets.info.hider = new HideManual();

		AlignTable table = mWidgets.info.table;
		table.setAlignTable(Horizontal.RIGHT, Vertical.TOP);
		table.setAlignRow(Horizontal.LEFT, Vertical.TOP);
		table.setBackgroundImage(new Background(mUiFactory.getStyles().color.widgetBackground));
		mWidgets.rightPanel.row().setFillHeight(true).setFillWidth(true);
		mWidgets.rightPanel.add(table).setFillHeight(true).setFillWidth(true);

		// Name
		mWidgets.info.name = mUiFactory.addPanelSection("", table, null);
		table.getRow().setAlign(Horizontal.CENTER, Vertical.TOP);

		// Description
		table.row(Horizontal.CENTER, Vertical.TOP);
		mWidgets.info.description = mUiFactory.addLabel("", true, table);

		// Created by
		mUiFactory.addPanelSection("Created by", table, null);
		mWidgets.info.createdBy = mUiFactory.addIconLabel(SkinNames.GeneralImages.PLAYER, "", table, null);

		// Revised by
		mUiFactory.addPanelSection("Revised by", table, null);
		mWidgets.info.revisedBy = mUiFactory.addIconLabel(SkinNames.GeneralImages.PLAYER, "", table, null);

		// Date
		mWidgets.info.date = mUiFactory.addIconLabel(SkinNames.GeneralImages.DATE, "", table, null);


		// Revision
		if (mSelectDefScene.canChooseRevision()) {
			mWidgets.info.revision = mUiFactory.addIconLabel(SkinNames.GeneralImages.EDIT, "", table, mWidgets.info.hider);
		}

		table.row().setFillHeight(true).setFillWidth(true);
		table.add().setFillHeight(true).setFillWidth(true);
	}

	/**
	 * Initialize the right panel
	 */
	private void initRightPanel() {
		float outerMargin = SkinNames.getResource(SkinNames.GeneralVars.PADDING_OUTER);
		float topMargin = SkinNames.getResource(SkinNames.GeneralVars.BAR_UPPER_LOWER_HEIGHT);
		topMargin += outerMargin;
		float infoWidth = SkinNames.getResource(SkinNames.GeneralVars.RIGHT_PANEL_WIDTH);

		AlignTable table = mWidgets.rightPanel;
		table.setKeepWidth(true).setWidth(infoWidth);
		table.setMargin(topMargin, outerMargin, outerMargin, outerMargin);
		table.setAlign(Horizontal.RIGHT, Vertical.TOP);
		table.setName("right-panel");
		getStage().addActor(table);
	}

	/**
	 * Initializes action buttons
	 */
	private void initActions() {
		AlignTable table = mWidgets.rightPanel;

		ButtonListener buttonListener;

		// Open older revision
		if (mSelectDefScene.canChooseRevision()) {
			table.row().setFillWidth(true).setEqualCellSize(true);
			buttonListener = new ButtonListener() {
				@Override
				protected void onPressed() {
					showSelectRevisionMsgBox();
				}
			};
			mUiFactory.addTextButton("Load older version", TextButtonStyles.PRESS, table, buttonListener, mWidgets.info.hider, null);
			table.getCell().setFillWidth(true);
		}


		// Menu
		table.row().setFillWidth(true).setEqualCellSize(true);
		buttonListener = new ButtonListener() {
			@Override
			protected void onPressed() {
				mSelectDefScene.cancel();
			}
		};
		mUiFactory.addTextButton("Back", TextButtonStyles.PRESS, table, buttonListener, null, null);
		table.getCell().resetWidth().setFillWidth(true);

		// Load/Open
		buttonListener = new ButtonListener() {
			@Override
			protected void onPressed() {
				mSelectDefScene.loadDef();
			}
		};
		mUiFactory.addTextButton(mButtonText, TextButtonStyles.PRESS, table, buttonListener, null, null);
		table.getCell().resetWidth().setFillWidth(true);
	}

	/**
	 * Initialize content
	 */
	private void initContent() {
		// TODO change scroll pane style to no visible scroll bars
		ScrollPaneStyle scrollPaneStyle = SkinNames.getResource(SkinNames.General.SCROLL_PANE_DEFAULT);
		AlignTable table = mWidgets.content.table;
		table.setName("content");
		table.setAlign(Horizontal.LEFT, Vertical.TOP);
		table.setHasPreferredHeight(false).setHasPreferredWidth(false);
		mWidgets.content.scrollPane = new ScrollPane(table, scrollPaneStyle);
		getStage().addActor(mWidgets.content.scrollPane);
		table.setAlign(Horizontal.LEFT, Vertical.TOP);

		resetContentMargins();
	}

	/**
	 * Reset content margins
	 */
	private void resetContentMargins() {
		if (mWidgets.content.scrollPane != null) {
			float screenWidth = Gdx.graphics.getWidth();
			float screenHeight = Gdx.graphics.getHeight();
			float marginLeft = 0;
			float marginRight = mWidgets.rightPanel.getWidthWithMargin();
			float marginTop = mWidgets.rightPanel.getMarginTop();
			float marginBottom = mWidgets.rightPanel.getMarginBottom();


			ScrollPane scrollPane = mWidgets.content.scrollPane;
			float width = screenWidth - marginLeft - marginRight;
			scrollPane.setWidth(width);
			scrollPane.setHeight(screenHeight - marginTop - marginBottom);
			scrollPane.setPosition(marginLeft, marginBottom);
			mWidgets.content.table.setWidth(width);
			mWidgets.content.table.setKeepWidth(true);
		}
	}

	/**
	 * Reset content
	 * @param defs definitions to update
	 */
	synchronized void resetContent(ArrayList<DefVisible> defs) {
		// Populate table
		AlignTable table = mWidgets.content.table;
		table.dispose();

		if (defs.isEmpty()) {
			return;
		}

		resetContentMargins();

		mWidgets.content.buttonGroup = new ButtonGroup();


		addContent(defs);

		resetInfo();

		if (mWidgets.content.scrollPane != null) {
			mWidgets.content.scrollPane.setScrollPercentY(0);
		}
	}

	/**
	 * Adds more definitions to the existing content
	 * @param defs the definitions to add
	 */
	void addContent(ArrayList<DefVisible> defs) {
		AlignTable table = mWidgets.content.table;

		// Calculate how many levels per row
		float floatLevelsPerRow = mWidgets.content.scrollPane.getWidth() / Config.Level.SAVE_TEXTURE_WIDTH;
		int levelsPerRow = (int) floatLevelsPerRow;
		if (floatLevelsPerRow != levelsPerRow) {
			levelsPerRow++;
		}

		int columnIndex = levelsPerRow;

		float paddingExplore = mUiFactory.getStyles().vars.paddingExplore;

		// Populate table
		for (DefVisible level : defs) {
			AlignTable levelTable = createDefTable(level);

			if (columnIndex == levelsPerRow) {
				table.row().setFillWidth(true).setEqualCellSize(true).setPadTop(paddingExplore).setPadRight(paddingExplore);
				columnIndex = 0;
			}

			table.add(levelTable).setFillWidth(true).setPadLeft(paddingExplore);

			columnIndex++;
		}

		// Set pad bottom for last row
		table.getRow().setPadBottom(paddingExplore);

		// Pad with empty cells
		if (columnIndex > 0 && columnIndex < levelsPerRow) {
			int columnsToPad = levelsPerRow - columnIndex;
			for (int i = 0; i < columnsToPad; ++i) {
				table.add().setFillWidth(true);
			}
		}

		table.invalidate();
		table.layout();
		mWidgets.content.scrollPane.invalidate();
	}

	/**
	 * Create definition image table
	 * @param def the definition to create an image for
	 * @return table with definition image and name
	 */
	private AlignTable createDefTable(final DefVisible def) {
		AlignTable table = new AlignTable();
		table.setAlign(Horizontal.CENTER, Vertical.MIDDLE);

		// Image button
		ImageButtonStyle defaultImageStyle = SkinNames.getResource(SkinNames.General.IMAGE_BUTTON_TOGGLE);

		Button button = new ResourceTextureButton(def.def, defaultImageStyle);
		table.row().setFillWidth(true);
		table.add(button).setFillWidth(true).setKeepAspectRatio(true);
		new ButtonListener(button) {
			@Override
			protected void onChecked(boolean checked) {
				if (checked) {
					mSelectDefScene.setSelectedDef(def.def);

					if (mSelectDefScene.isSelectedPublished()) {
						mWidgets.info.hider.hide();
						mWidgets.rightPanel.invalidate();
					} else {
						mWidgets.info.hider.show();
						mWidgets.rightPanel.invalidate();
					}

					resetInfo();
				}
			}

			@Override
			protected void onDown() {
				mWasCheckedOnDown = mButton.isChecked();
			}

			@Override
			protected void onUp() {
				if (mWasCheckedOnDown) {
					mSelectDefScene.loadDef();
				}
			}

			/** If this level was selected before */
			private boolean mWasCheckedOnDown = false;
		};
		mWidgets.content.buttonGroup.add(button);

		// Def name
		table.row();
		mUiFactory.addLabel(def.def.getName(), false, table);
		table.getCell().setHeight(mUiFactory.getStyles().vars.rowHeight);

		return table;
	}

	/**
	 * Reset info panel
	 */
	void resetInfo() {
		mWidgets.info.createdBy.setText(mSelectDefScene.getOriginalCreator());
		mWidgets.info.date.setText(mSelectDefScene.getDate());
		mWidgets.info.description.setText(mSelectDefScene.getDescription());
		mWidgets.info.name.setText(mSelectDefScene.getName());
		mWidgets.info.revisedBy.setText(mSelectDefScene.getCreator());

		if (mWidgets.info.revision != null) {
			mWidgets.info.revision.setText(mSelectDefScene.getRevisionString());
		}
	}

	/**
	 * Show select revision message box
	 */
	private void showSelectRevisionMsgBox() {
		MsgBoxExecuter msgBox = getFreeMsgBox(true);

		msgBox.setTitle("Select another revision");
		msgBox.content(mWidgets.revision.table);

		mWidgets.revision.table.setKeepSize(true);
		mWidgets.revision.table.setSize(Gdx.graphics.getWidth() * 0.6f, Gdx.graphics.getHeight() * 0.6f);

		updateRevisionList();

		// Get latest revision number
		int latestRevision = mWidgets.revision.list.getItems().size;

		msgBox.button("Latest", new CSelectDefSetRevision(latestRevision, mSelectDefScene));
		msgBox.button("Select", new CSelectDefSetRevision(mWidgets.revision.list, mSelectDefScene));
		msgBox.addCancelButtonAndKeys();

		showMsgBox(msgBox);

		getStage().setScrollFocus(mWidgets.revision.list);
	}

	/**
	 * Updates the revision list
	 */
	private void updateRevisionList() {
		ArrayList<RevisionEntity> resourceRevisions = mSelectDefScene.getSelectedResourceRevisionsWithDates();

		String[] revisions;
		if (resourceRevisions != null) {
			revisions = new String[resourceRevisions.size()];

			// Calculate number length
			String latestRevision = String.valueOf(revisions.length);
			int revisionStringLength = latestRevision.length();

			for (int i = 0; i < revisions.length; ++i) {
				int revisionInt = revisions.length - 1 - i;
				RevisionEntity revisionInfo = resourceRevisions.get(i);
				String dateString = User.getGlobalUser().dateToString(revisionInfo.date);
				revisions[revisionInt] = String.format("%0" + revisionStringLength + "d - %s", revisionInfo.revision, dateString);
			}
		} else {
			revisions = new String[0];
		}

		mWidgets.revision.list.setItems(revisions);
	}

	/**
	 * Initialize select revision
	 */
	private void initSelectRevision() {
		List<String> list = new List<String>((ListStyle) SkinNames.getResource(SkinNames.General.LIST_DEFAULT));
		mWidgets.revision.list = list;
		mWidgets.revision.scrollPane = new ScrollPane(list, (ScrollPaneStyle) SkinNames.getResource(SkinNames.General.SCROLL_PANE_DEFAULT));
		mWidgets.revision.table.setAlign(Horizontal.LEFT, Vertical.TOP);
		mWidgets.revision.table.row().setFillHeight(true).setFillWidth(true);
		mWidgets.revision.table.add(mWidgets.revision.scrollPane).setFillHeight(true).setFillWidth(true);
	}

	/** If the checkbox that only shows one's own actors shall be shown */
	private boolean mShowMineOnlyCheckbox;
	/** SelectDefScene this GUI is bound to */
	private SelectDefScene mSelectDefScene = null;
	/** Text to display on play/load button */
	private String mButtonText = "";
	/** Inner widgets */
	private Widgets mWidgets = new Widgets();

	@SuppressWarnings("javadoc")
	private static class Widgets {
		Background topBar = null;
		Search search = new Search();
		Info info = new Info();
		AlignTable rightPanel = new AlignTable();
		Content content = new Content();
		Revision revision = new Revision();

		private static class Revision {
			AlignTable table = new AlignTable();
			List<String> list = null;
			ScrollPane scrollPane = null;
		}

		private static class Search {
			AlignTable table = new AlignTable();
		}

		private static class Info {
			AlignTable table = new AlignTable();
			Label name = null;
			Label description = null;
			// RatingWidget rating = null;
			Label revisedBy = null;
			Label createdBy = null;
			Label date = null;
			Label revision = null;
			// Label plays = null;
			// Label likes = null;
			// Label tags = null;
			HideManual hider = null;
		}

		private static class Content {
			AlignTable table = new AlignTable();
			ScrollPane scrollPane = null;
			ButtonGroup buttonGroup = new ButtonGroup();
		}
	}
}

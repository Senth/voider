package com.spiddekauga.voider.explore;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List.ListStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.ScrollPaneListener;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.ScrollPaneStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.utils.Disposable;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.utils.scene.ui.AlignTable;
import com.spiddekauga.utils.scene.ui.AnimationWidget;
import com.spiddekauga.utils.scene.ui.AnimationWidget.AnimationWidgetStyle;
import com.spiddekauga.utils.scene.ui.Background;
import com.spiddekauga.utils.scene.ui.ButtonListener;
import com.spiddekauga.utils.scene.ui.GuiHider;
import com.spiddekauga.utils.scene.ui.HideListener;
import com.spiddekauga.utils.scene.ui.HideManual;
import com.spiddekauga.utils.scene.ui.MsgBoxExecuter;
import com.spiddekauga.utils.scene.ui.Row;
import com.spiddekauga.utils.scene.ui.TabWidget;
import com.spiddekauga.utils.scene.ui.TextFieldListener;
import com.spiddekauga.utils.scene.ui.VisibilityChangeListener;
import com.spiddekauga.voider.explore.ExploreScene.ExploreViews;
import com.spiddekauga.voider.network.resource.DefEntity;
import com.spiddekauga.voider.network.resource.RevisionEntity;
import com.spiddekauga.voider.repo.misc.SettingRepo;
import com.spiddekauga.voider.repo.misc.SettingRepo.SettingDateRepo;
import com.spiddekauga.voider.repo.resource.ResourceLocalRepo;
import com.spiddekauga.voider.repo.resource.SkinNames;
import com.spiddekauga.voider.repo.resource.SkinNames.ISkinNames;
import com.spiddekauga.voider.scene.Gui;
import com.spiddekauga.voider.scene.ui.ButtonFactory.TabRadioWrapper;
import com.spiddekauga.voider.scene.ui.UiFactory.Positions;
import com.spiddekauga.voider.scene.ui.UiStyles.CheckBoxStyles;
import com.spiddekauga.voider.scene.ui.UiStyles.TextButtonStyles;
import com.spiddekauga.voider.utils.event.EventDispatcher;
import com.spiddekauga.voider.utils.event.EventTypes;
import com.spiddekauga.voider.utils.event.GameEvent;
import com.spiddekauga.voider.utils.event.IEventListener;

/**
 * Common GUI for all explore scenes
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
abstract class ExploreGui extends Gui {
	/**
	 * Hidden constructor
	 */
	protected ExploreGui() {
		// Does nothing
	}

	/**
	 * Set the explore scene
	 * @param exploreScene
	 */
	void setExploreScene(ExploreScene exploreScene) {
		mScene = exploreScene;
	}

	@Override
	public void initGui() {
		super.initGui();

		mWidgets = new Widgets();

		connectUserListeners();
		initRightPanel();
		initInfo(mWidgets.info.table, mWidgets.info.hider);
		initLeftPanel();
		initContent();
		initView();
		initViewButtons();
		initSearchFilters(mWidgets.search.table, mWidgets.search.contentHider);
		if (mScene.getSelectedAction() == ExploreActions.LOAD) {
			initSelectRevision();
		}

		// Initialize last
		initTopBar();

		getStage().setScrollFocus(mWidgets.content.scrollPane);
	}

	@Override
	public void dispose() {
		mWidgets.dispose();
		mLeftPanel.dispose();
		mRightPanel.dispose();

		disconnectUserListeners();

		super.dispose();
	}

	@Override
	public void resetValues() {
		super.resetValues();

		resetSearchFilters();
		resetInfo();
	}

	/**
	 * Creates user connected/disconnect events
	 */
	private void connectUserListeners() {
		EventDispatcher eventDispatcher = EventDispatcher.getInstance();
		eventDispatcher.connect(EventTypes.USER_CONNECTED, mUserListener);
		eventDispatcher.connect(EventTypes.USER_DISCONNECTED, mUserListener);
	}

	/**
	 * Disconnect user listeners
	 */
	private void disconnectUserListeners() {
		EventDispatcher eventDispatcher = EventDispatcher.getInstance();
		eventDispatcher.disconnect(EventTypes.USER_CONNECTED, mUserListener);
		eventDispatcher.disconnect(EventTypes.USER_DISCONNECTED, mUserListener);
	}

	/**
	 * Called when the user has gone online
	 */
	protected void onUserOnline() {

	}

	/**
	 * Called when the user has gone offline
	 */
	protected void onUserOffline() {

	}

	/**
	 * Reset the content
	 */
	void resetContent() {
		mWidgets.content.table.dispose();
		mWidgets.content.buttonGroup = new ButtonGroup();

		if (mScene != null && mScene.isFetchingContent()) {
			addWaitIconToContent();
		}
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
	 * Initialize view table (for top left buttons)
	 */
	private void initView() {
		AlignTable table = mWidgets.view.table;
		table.setMargin(mUiFactory.getStyles().vars.paddingOuter);
		table.setAlign(Horizontal.LEFT, Vertical.TOP);
		addActor(table);
	}

	/**
	 * Add a view button
	 * @param iconName name of the button image
	 * @param listener button listener for this button
	 * @param hideListeners hide listeners for this button
	 * @return the created button
	 */
	protected Button addViewButton(ISkinNames iconName, ButtonListener listener, HideListener... hideListeners) {
		Button button = mUiFactory.button.createImage(iconName);
		mWidgets.view.table.add(button);
		button.addListener(listener);
		mWidgets.view.buttonGroup.add(button);

		for (HideListener hideListener : hideListeners) {
			hideListener.addButton(button);
		}

		return button;
	}

	/**
	 * Initialize the left panel
	 */
	private void initLeftPanel() {
		TabWidget tabWidget = mUiFactory.createRightPanel();
		tabWidget.setName("left-panel");
		mLeftPanel = tabWidget;

		tabWidget.setTabPosition(Positions.RIGHT);
		tabWidget.setAlignTable(Horizontal.LEFT, Vertical.TOP);
		tabWidget.setContentHideable(true);

		VisibilityChangeListener visibilityListener = new VisibilityChangeListener() {
			@Override
			public void onVisibilyChange(VisibilityChangeEvent event, Actor actor) {
				if (actor == mLeftPanel) {
					mLeftPanel.layout();
					resetContentMargins();
				}
			}
		};
		tabWidget.addListener(visibilityListener);

		initPanel(tabWidget);
	}

	/**
	 * Add definitions to the content table
	 * @param defs all definitions to add to the content
	 */
	protected final void addContent(List<? extends DefEntity> defs) {
		beginAddContent();

		DefEntity selectedDef = mScene.getSelected();

		for (DefEntity def : defs) {
			boolean selected = selectedDef == def;
			addContent(createContentActor(def, selected));
		}

		endAddContent();
	}

	/**
	 * Creates the content UI actor to display inside the content from a definition
	 * @param defEntity the definition to create the content from
	 * @param selected if the current definition is selected
	 * @return actor to be added to the content
	 */
	protected abstract Actor createContentActor(final DefEntity defEntity, boolean selected);

	/**
	 * Selected definition was changed
	 * @param defEntity the definition that was changed
	 */
	protected void onSelectionChanged(DefEntity defEntity) {
		resetInfo();

		// Shall we show or hide the Select Revision?
		if (defEntity != null && mScene.getView().isLocal() && !ResourceLocalRepo.isPublished(defEntity.resourceId)) {
			mWidgets.selectRevisionHider.show();
		} else {
			mWidgets.selectRevisionHider.hide();
		}
	}

	/**
	 * Initialize view buttons
	 */
	protected void initViewButtons() {
		ButtonListener listener = new ButtonListener() {
			@Override
			protected void onPressed(Button button) {
				mScene.setView(ExploreViews.LOCAL);
			}
		};
		mWidgets.view.local = addViewButton(SkinNames.General.EXPLORE_LOCAL, listener, mWidgets.search.viewHider, mWidgets.search.publishedHider);
	}

	/**
	 * Resets the view buttons
	 */
	protected void resetViewButtons() {
		if (mWidgets.view.local != null) {
			if (mScene.getView() == ExploreViews.LOCAL) {
				mWidgets.view.local.setChecked(true);
			}
		}
	}

	/**
	 * Initialize search filters tab
	 * @param table content table
	 * @param contentHider
	 */
	protected void initSearchFilters(AlignTable table, GuiHider contentHider) {
		// Tab Button
		Button button = mUiFactory.button.addTabScroll(SkinNames.General.SEARCH_FILTER, mWidgets.search.table, mWidgets.search.contentHider,
				mLeftPanel);
		mWidgets.search.viewHider.addToggleActor(button);

		table.setName("search-filters");
		mUiFactory.text.addPanelSection("Search Filter", table, null);
		table.getRow().setAlign(Horizontal.CENTER, Vertical.TOP);
		table.getCell();


		// Search Field
		TextFieldListener textFieldListener = new TextFieldListener() {
			@Override
			protected void onChange(String newText) {
				mScene.setSearchString(newText);
			}
		};
		mUiFactory.text.addPanelSection("Free-text Search", table, null);
		mWidgets.search.searchText = mUiFactory.addTextField(null, false, "Name or Creator", textFieldListener, table, null);


		// Published
		mUiFactory.text.addPanelSection("Published?", table, mWidgets.search.publishedHider);
		TabRadioWrapper anyTab = mUiFactory.button.createTabRadioWrapper("Any");
		anyTab.setListener(new ButtonListener() {
			@Override
			protected void onPressed(Button button) {
				mScene.setPublished(null);
				mWidgets.search.onlyMineHider.show();
			}
		});

		TabRadioWrapper onTab = mUiFactory.button.createTabRadioWrapper("Only Published");
		onTab.setListener(new ButtonListener() {
			@Override
			protected void onPressed(Button button) {
				mScene.setPublished(true);
				mWidgets.search.onlyMineHider.show();
			}
		});

		TabRadioWrapper offTab = mUiFactory.button.createTabRadioWrapper("Not published");
		offTab.setListener(new ButtonListener() {
			@Override
			protected void onPressed(Button button) {
				mScene.setPublished(false);
				mWidgets.search.onlyMineHider.hide();
			}
		});

		mUiFactory.button.addTabs(table, contentHider, true, null, null, anyTab, onTab, offTab);
		mWidgets.search.publishedAny = anyTab.getButton();
		mWidgets.search.publishedYes = onTab.getButton();
		mWidgets.search.publishedNo = offTab.getButton();
		mWidgets.search.publishedHider.addToggleActor(anyTab.getButton());
		mWidgets.search.publishedHider.addToggleActor(onTab.getButton());
		mWidgets.search.publishedHider.addToggleActor(offTab.getButton());
		mWidgets.search.publishedHider.addChild(anyTab.getHider());
		mWidgets.search.publishedHider.addChild(onTab.getHider());
		mWidgets.search.publishedHider.addChild(offTab.getHider());


		// Only mine
		ButtonListener buttonListener = new ButtonListener() {
			@Override
			protected void onChecked(Button button, boolean checked) {
				mScene.setOnlyMine(checked);
			}
		};
		mWidgets.search.onlyMine = mUiFactory.button.addCheckBoxRow("Only mine", CheckBoxStyles.CHECK_BOX, buttonListener, null, table);
		mWidgets.search.onlyMineHider.addToggleActor(mWidgets.search.onlyMine);


		// Clear button
		button = mUiFactory.button.createText("Clear Filters", TextButtonStyles.FILLED_PRESS);
		new ButtonListener(button) {
			@Override
			protected void onPressed(Button button) {
				resetSearchFilters();
			}
		};

		mLeftPanel.addActionButton(button);
		mLeftPanel.layout();
	}

	/**
	 * Reset search filters
	 * @see #clearSearchFilters() to set to default values
	 */
	protected void resetSearchFilters() {
		if (mWidgets.search.searchText != null) {
			mWidgets.search.searchText.setText(mScene.getSearchString());
			mWidgets.search.onlyMine.setChecked(mScene.isOnlyMine());

			// Published
			if (mScene.isPublished() == null) {
				mWidgets.search.publishedAny.setChecked(true);
			} else if (mScene.isPublished()) {
				mWidgets.search.publishedYes.setChecked(true);
			} else {
				mWidgets.search.publishedNo.setChecked(true);
			}
		}
	}

	/**
	 * Clear search filters
	 * @see #resetSearchFilters()
	 */
	protected void clearSearchFilters() {
		mWidgets.search.searchText.setText("");
		mWidgets.search.onlyMine.setChecked(false);
		mWidgets.search.publishedAny.setChecked(true);
	}

	/**
	 * @return search filters hider
	 */
	protected HideListener getSearchFilterHider() {
		return mWidgets.search.viewHider;
	}

	/**
	 * Initialize the right panel
	 */
	private void initRightPanel() {
		TabWidget tabWidget = mUiFactory.createRightPanel();
		tabWidget.setName("right-panel");
		mRightPanel = tabWidget;

		// Info
		mUiFactory.button.addTab(SkinNames.General.OVERVIEW, mWidgets.info.table, mWidgets.info.hider, mRightPanel);

		// Add actions
		// Revision
		if (mScene.getSelectedAction() == ExploreActions.LOAD) {
			Button button = mUiFactory.button.createText("Select Revision", TextButtonStyles.FILLED_PRESS);
			new ButtonListener(button) {
				@Override
				protected void onPressed(Button button) {
					showSelectRevisionMsgBox();
				}
			};
			tabWidget.addActionButtonGlobal(button);
			tabWidget.addActionButtonRow();
			mWidgets.selectRevisionHider.addToggleActor(button);
		}

		// Back
		Button button = mUiFactory.button.createText("Back", TextButtonStyles.FILLED_PRESS);
		new ButtonListener(button) {
			@Override
			protected void onPressed(Button button) {
				mScene.endScene();
			}
		};
		tabWidget.addActionButtonGlobal(button);

		// The action
		button = mUiFactory.button.createText(mScene.getSelectedAction().toString(), TextButtonStyles.FILLED_PRESS);
		new ButtonListener(button) {
			@Override
			protected void onPressed(Button button) {
				mScene.selectAction();
			}
		};
		tabWidget.addActionButtonGlobal(button);

		initPanel(tabWidget);
	}

	/**
	 * Show select revision message box
	 */
	private void showSelectRevisionMsgBox() {
		MsgBoxExecuter msgBox = mUiFactory.msgBox.add("Select Revision");
		msgBox.content(mWidgets.revision.scrollPane);

		mWidgets.revision.scrollPane.setSize(Gdx.graphics.getWidth() * 0.6f, Gdx.graphics.getHeight() * 0.6f);

		updateRevisionList();

		msgBox.addCancelButtonAndKeys();
		msgBox.button("Load Revision", new CExploreLoadRevision(mScene, this));

		getStage().setScrollFocus(mWidgets.revision.list);
	}

	/**
	 * Update the revision list
	 */
	private void updateRevisionList() {
		ArrayList<RevisionEntity> resourceRevisions = mScene.getSelectedResourceRevisions();

		String[] revisions = new String[resourceRevisions.size()];

		// Calculate number length
		String latestRevision = String.valueOf(revisions.length);
		int revisionStringLength = latestRevision.length();

		// Latest revision is at the top
		for (int i = 0; i < revisions.length; ++i) {
			int arrayPos = revisions.length - 1 - i;
			RevisionEntity revisionInfo = resourceRevisions.get(i);
			String dateString = mDateRepo.getDate(revisionInfo.date);
			revisions[arrayPos] = String.format("%0" + revisionStringLength + "d  -  %s", revisionInfo.revision, dateString);
		}

		mWidgets.revision.list.setItems(revisions);
	}

	/**
	 * Initialize select revision
	 */
	private void initSelectRevision() {
		com.badlogic.gdx.scenes.scene2d.ui.List<String> list = new com.badlogic.gdx.scenes.scene2d.ui.List<>(
				(ListStyle) SkinNames.getResource(SkinNames.General.LIST_DEFAULT));
		mWidgets.revision.list = list;
		mWidgets.revision.scrollPane = new ScrollPane(list, mUiFactory.getStyles().scrollPane.noBackground);
	}

	/**
	 * Common initialization for left and right panel
	 * @param tabWidget panel to initialize
	 */
	private void initPanel(TabWidget tabWidget) {
		addActor(tabWidget);

		tabWidget.setMarginBottom(mUiFactory.getStyles().vars.paddingOuter);

		tabWidget.layout();
	}

	/**
	 * Initialize the content table
	 */
	private void initContent() {
		ScrollPaneStyle scrollPaneStyle = mUiFactory.getStyles().scrollPane.noBackground;
		AlignTable table = mWidgets.content.table;
		table.setName("content");
		table.setHasPreferredHeight(false).setHasPreferredWidth(false);
		mWidgets.content.scrollPane = new ScrollPane(table, scrollPaneStyle);
		getStage().addActor(mWidgets.content.scrollPane);
		table.setAlign(Horizontal.LEFT, Vertical.TOP);
		mWidgets.content.scrollPane.addListener(new ScrollPaneListener() {
			@Override
			public void hitEdge(ScrollPane scrollPane, Edge edge) {
				if (edge == Edge.BOTTOM) {
					if (mScene.isFetchingContent() && mScene.hasMoreContent()) {
						onFetchMoreContent();
					}
				}
			}
		});

		resetContentMargins();
	}

	/**
	 * Called when more content should be fetched.
	 */
	protected void onFetchMoreContent() {
		mScene.fetchMoreContent();
		addWaitIconToContent();
	}

	/**
	 * Reset content margins
	 */
	protected void resetContentMargins() {
		if (mWidgets.content.scrollPane != null) {
			float screenWidth = Gdx.graphics.getWidth();
			float screenHeight = Gdx.graphics.getHeight();
			float marginLeft = mLeftPanel.isVisible() ? mLeftPanel.getWidthWithMargin() : mUiFactory.getStyles().vars.paddingOuter;
			float marginRight = mRightPanel.getWidthWithMargin();
			float marginTop = mUiFactory.getStyles().vars.barUpperLowerHeight;
			float marginBottom = mUiFactory.getStyles().vars.paddingOuter;


			ScrollPane scrollPane = mWidgets.content.scrollPane;
			float width = screenWidth - marginLeft - marginRight;
			scrollPane.setWidth(width);
			scrollPane.setHeight(screenHeight - marginTop - marginBottom);
			scrollPane.setPosition(marginLeft, marginBottom);
			mWidgets.content.table.setWidth(width);
			mWidgets.content.table.setKeepWidth(true);

			mWidgets.content.table.invalidateHierarchy();
			mWidgets.content.scrollPane.validate();

			int cActorsPerRowOld = mActorsPerRow;
			calculateActorsPerRow();
			if (mActorsPerRow != cActorsPerRowOld && cActorsPerRowOld != 0) {
				repopulateContent();
			}
		}
	}

	/**
	 * Repopulate contents
	 */
	private void repopulateContent() {
		resetContent();
		mScene.repopulateContent();
	}

	/**
	 * Add a wait icon to the content table
	 */
	protected void addWaitIconToContent() {
		mWidgets.content.waitIconRow = addWaitIconToTable(mWidgets.content.table);
	}

	/**
	 * Remove wait icon from the content
	 */
	private void removeWaitIconFromContent() {
		if (mWidgets.content.waitIconRow != null) {
			mWidgets.content.table.removeRow(mWidgets.content.waitIconRow, true);
			mWidgets.content.waitIconRow = null;
		}
	}

	/**
	 * Clear empty cells from the content
	 */
	private void removeEmptyCellsFromContent() {
		Row lastRow = mWidgets.content.table.getRow();
		if (lastRow != null) {
			lastRow.removeEmptyCells();
		}
	}

	/**
	 * Calculates the number of actors to display per row in the content
	 */
	private void calculateActorsPerRow() {
		float floatActorsPerRow = mWidgets.content.scrollPane.getWidth() / getMaxActorWidth();
		mActorsPerRow = (int) floatActorsPerRow;
		if (floatActorsPerRow != mActorsPerRow) {
			mActorsPerRow++;
		}
	}

	/**
	 * @return maximum actor width of the content
	 */
	protected abstract float getMaxActorWidth();

	/**
	 * Call this method before beginning to add content via
	 */
	protected void beginAddContent() {
		if (mAddingContent) {
			Gdx.app.error("ExploreGui", "Already called beginAddContent()");
		}

		mAddingContent = true;
		removeWaitIconFromContent();
		removeEmptyCellsFromContent();
	}

	/**
	 * Add something to the content
	 * @param actor the actor to add to the content
	 */
	protected void addContent(Actor actor) {
		if (!mAddingContent) {
			Gdx.app.error("ExploreGui", "Haven't called beginAddContent()");
			return;
		}

		float paddingExplore = mUiFactory.getStyles().vars.paddingExplore;

		AlignTable table = mWidgets.content.table;

		// Add new row
		if (table.getRow() == null || table.getRow().getCellCount() == mActorsPerRow) {
			table.row().setPadTop(paddingExplore).setFillWidth(true).setEqualCellSize(true).setPadRight(paddingExplore);
		}

		table.add(actor).setFillWidth(true).setPadLeft(paddingExplore);
	}

	/**
	 * Add a button to the content button group
	 * @param button the button to add
	 */
	protected void addContentButton(Button button) {
		mWidgets.content.buttonGroup.add(button);
	}

	/**
	 * Helper method for getting the correct string for enum ranges
	 * @param enumObject enumeration range, can be null
	 * @return correct string to display for the enum
	 */
	protected String getEnumString(Enum<?> enumObject) {
		if (enumObject != null) {
			return enumObject.toString();
		} else {
			return "Not Found";
		}
	}

	/**
	 * Call this method after you have finished adding content
	 */
	protected void endAddContent() {
		if (!mAddingContent) {
			Gdx.app.error("ExploreGui", "Haven't called beginAddContent()");
			return;
		}

		mAddingContent = false;

		// Pad with empty cells
		Row row = mWidgets.content.table.getRow();
		if (row != null && row.getCellCount() < mActorsPerRow) {
			mWidgets.content.table.add(mActorsPerRow - row.getCellCount());
		}
		mWidgets.content.table.invalidateHierarchy();
		mWidgets.content.scrollPane.layout();

		// Fetch more content if view isn't full
		if (mScene.hasMoreContent() && mWidgets.content.table.getHeight() < mWidgets.content.scrollPane.getHeight()) {
			onFetchMoreContent();
		}
	}

	/**
	 * Initialize the information table in the right panel. This populates the table with
	 * the default information. Override this table to add more information to it
	 * @param table information table
	 * @param hider info hider
	 */
	protected void initInfo(AlignTable table, HideListener hider) {
		// Name
		mWidgets.info.name = mUiFactory.text.addPanelSection("", table, null);
		table.getRow().setAlign(Horizontal.CENTER, Vertical.TOP);

		// Description
		table.row(Horizontal.CENTER, Vertical.TOP);
		mWidgets.info.description = mUiFactory.text.add("", true, table);

		// Created by
		mUiFactory.text.addPanelSection("Created By", table, null);
		mWidgets.info.createbBy = mUiFactory.addIconLabel(SkinNames.GeneralImages.PLAYER, "", false, table, null);

		// Revised by
		mUiFactory.text.addPanelSection("Revised By", table, mWidgets.info.revisedHider);
		mWidgets.info.revisedBy = mUiFactory.addIconLabel(SkinNames.GeneralImages.PLAYER, "", false, table, mWidgets.info.revisedHider);

		// Date
		mWidgets.info.date = mUiFactory.addIconLabel(SkinNames.GeneralImages.DATE, "", false, table, null);
	}

	/**
	 * Resets the values of info
	 */
	protected void resetInfo() {
		DefEntity actor = mScene.getSelected();

		if (actor != null) {
			// Has created UI elements
			if (mWidgets.info.name != null) {
				mWidgets.info.createbBy.setText(actor.originalCreator);
				mWidgets.info.date.setText(mDateRepo.getDate(actor.date));
				mWidgets.info.description.setText(actor.description);
				mWidgets.info.name.setText(actor.name);

				// Revised by another person
				if (!actor.originalCreatorKey.equals(actor.revisedByKey)) {
					mWidgets.info.revisedHider.show();
					mWidgets.info.revisedBy.setText(actor.revisedBy);
				} else {
					mWidgets.info.revisedHider.hide();
				}
			}
		} else {
			// Has created UI elements
			if (mWidgets.info.name != null) {
				mWidgets.info.createbBy.setText("");
				mWidgets.info.date.setText("");
				mWidgets.info.description.setText("");
				mWidgets.info.name.setText("");
				mWidgets.info.revisedHider.hide();
			}
		}
	}

	/**
	 * Add a wait icon to a new row for the specified table
	 * @param table the table to add the animation wait widget to
	 * @return row of the wait icon (so it can be removed)
	 */
	protected static Row addWaitIconToTable(AlignTable table) {
		AnimationWidgetStyle waitIconStyle = SkinNames.getResource(SkinNames.General.ANIMATION_WAIT);
		AnimationWidget waitIcon = new AnimationWidget(waitIconStyle);

		Row row = table.row(Horizontal.CENTER, Vertical.MIDDLE);
		table.add(waitIcon);
		table.invalidate();

		return row;
	}

	/**
	 * Reset enumeration buttons
	 * @param buttons all the buttons
	 * @param checkedEnums all enumerations that should be checked
	 */
	protected static void resetEnumButtons(Button[] buttons, ArrayList<? extends Enum<?>> checkedEnums) {
		clearButtons(buttons);

		for (Enum<?> enumeration : checkedEnums) {
			buttons[enumeration.ordinal()].setChecked(true);
		}
	}

	/**
	 * Clear all buttons
	 * @param buttons all buttons to set as unchecked
	 */
	protected static void clearButtons(Button[] buttons) {
		for (Button button : buttons) {
			button.setChecked(false);
		}
	}

	/**
	 * @return selected revision
	 */
	int getSelectedRevision() {
		if (mWidgets.revision.list != null) {
			String revisionDateString = mWidgets.revision.list.getSelection().getLastSelected();

			if (revisionDateString != null) {
				String revisionString[] = revisionDateString.split("  ");

				if (revisionString.length == 3) {
					return Integer.parseInt(revisionString[0]);
				} else {
					Gdx.app.error("ExploreGui", "Could not split revision string properly: " + revisionDateString);
				}
			}
		}

		return -1;
	}

	private IEventListener mUserListener = new IEventListener() {
		@Override
		public void handleEvent(GameEvent event) {
			switch (event.type) {
			case USER_CONNECTED:
				onUserOnline();

			case USER_DISCONNECTED:
				onUserOffline();

			default:
				break;
			}
		}
	};

	private boolean mAddingContent = false;
	private int mActorsPerRow = 0;
	private ExploreScene mScene = null;
	/** Left panel, search options, tags, etc. */
	protected TabWidget mLeftPanel = null;
	/** Right panel, selected info, comments, etc */
	protected TabWidget mRightPanel = null;
	/** Repository for printing in the correct date format */
	protected SettingDateRepo mDateRepo = SettingRepo.getInstance().date();

	private Widgets mWidgets = null;

	private class Widgets implements Disposable {
		Content content = new Content();
		View view = new View();
		Search search = new Search();
		Background topBar = null;
		Info info = new Info();
		Revision revision = new Revision();
		HideManual selectRevisionHider = new HideManual();

		private class Revision {
			com.badlogic.gdx.scenes.scene2d.ui.List<String> list = null;
			ScrollPane scrollPane = null;
		}

		private class Info implements Disposable {
			AlignTable table = new AlignTable();
			HideListener hider = new HideListener(true);
			Label name = null;
			Label description = null;
			Label createbBy = null;
			Label revisedBy = null;
			Label date = null;
			HideManual revisedHider = new HideManual();

			private Info() {
				init();
			}

			@Override
			public void dispose() {
				table.dispose();
				revisedHider.dispose();
				hider.dispose();
				init();
			}

			private void init() {
				hider.addChild(revisedHider);
			}
		}

		private class Content implements Disposable {
			AlignTable table = new AlignTable();
			ScrollPane scrollPane = null;
			ButtonGroup buttonGroup = new ButtonGroup();
			Row waitIconRow = null;

			@Override
			public void dispose() {
				table.dispose();
				buttonGroup = new ButtonGroup();
			}
		}

		private class View implements Disposable {
			AlignTable table = new AlignTable();
			ButtonGroup buttonGroup = new ButtonGroup();
			Button local = null;

			@Override
			public void dispose() {
				table.dispose();
				buttonGroup = new ButtonGroup();
			}
		}

		private class Search implements Disposable {
			AlignTable table = new AlignTable();
			HideListener viewHider = new HideListener(true) {
				@Override
				protected void onShow() {
					resetContentMargins();
				}
			};
			HideListener publishedHider = new HideListener(true);
			HideListener contentHider = new HideListener(true);
			Button publishedAny = null;
			Button publishedYes = null;
			Button publishedNo = null;
			HideManual onlyMineHider = new HideManual();
			Button onlyMine = null;
			TextField searchText = null;

			private Search() {
				init();
			}

			@Override
			public void dispose() {
				table.dispose();
				contentHider.dispose();
				publishedHider.dispose();
				viewHider.dispose();
				onlyMineHider.dispose();

				init();
			}

			private void init() {
				viewHider.addChild(contentHider);
				contentHider.addChild(publishedHider);
				publishedHider.addChild(onlyMineHider);
			}
		}

		@Override
		public void dispose() {
			content.dispose();
			view.dispose();
			search.dispose();
			info.dispose();
		}
	}
}

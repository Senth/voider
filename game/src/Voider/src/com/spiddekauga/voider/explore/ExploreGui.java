package com.spiddekauga.voider.explore;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
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
import com.spiddekauga.utils.scene.ui.Row;
import com.spiddekauga.utils.scene.ui.TabWidget;
import com.spiddekauga.utils.scene.ui.TextFieldListener;
import com.spiddekauga.utils.scene.ui.VisibilityChangeListener;
import com.spiddekauga.voider.explore.ExploreScene.ExploreViews;
import com.spiddekauga.voider.repo.misc.SettingRepo;
import com.spiddekauga.voider.repo.misc.SettingRepo.SettingDateRepo;
import com.spiddekauga.voider.repo.resource.SkinNames;
import com.spiddekauga.voider.repo.resource.SkinNames.ISkinNames;
import com.spiddekauga.voider.scene.Gui;
import com.spiddekauga.voider.scene.ui.ButtonFactory.TabRadioWrapper;
import com.spiddekauga.voider.scene.ui.UiFactory.Positions;
import com.spiddekauga.voider.scene.ui.UiStyles.CheckBoxStyles;
import com.spiddekauga.voider.scene.ui.UiStyles.TextButtonStyles;

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

		mWidgets = new InnerWidgets();

		initRightPanel();
		initLeftPanel();
		initContent();
		initView();
		initSearchFilters(mWidgets.search.table, mWidgets.search.contentHider);

		// Initialize last
		initTopBar();

		getStage().setScrollFocus(mWidgets.content.scrollPane);
	}

	@Override
	public void dispose() {
		mWidgets.dispose();
		mLeftPanel.dispose();
		mRightPanel.dispose();

		super.dispose();
	}

	@Override
	public void resetValues() {
		super.resetValues();

		resetContent();
		resetSearchFilters();
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
	 */
	protected void addViewButton(ISkinNames iconName, ButtonListener listener, HideListener... hideListeners) {
		Button button = new ImageButton((ImageButtonStyle) SkinNames.getResource(iconName));
		mWidgets.view.table.add(button);
		mWidgets.view.buttonGroup.add(button);
		button.addListener(listener);

		for (HideListener hideListener : hideListeners) {
			hideListener.setButton(button);
		}
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
	 * Initialize view buttons
	 */
	protected void initViewButtons() {
		ButtonListener listener = new ButtonListener() {
			@Override
			protected void onPressed(Button button) {
				mScene.setView(ExploreViews.LOCAL);
			}
		};
		addViewButton(SkinNames.General.EXPLORE_LOCAL, listener, mWidgets.search.viewHider);
	}

	/**
	 * Initialize search filters tab
	 * @param table content table
	 * @param contentHider
	 */
	protected void initSearchFilters(AlignTable table, GuiHider contentHider) {
		// Tab
		Button button = mUiFactory.addTab(SkinNames.General.SEARCH_FILTER, mWidgets.search.table, mWidgets.search.contentHider, mLeftPanel);
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


		// Only mine
		ButtonListener buttonListener = new ButtonListener() {
			@Override
			protected void onChecked(Button button, boolean checked) {
				mScene.setOnlyMine(checked);
			}
		};
		mWidgets.search.onlyMine = mUiFactory.button.addCheckBoxRow("Only mine", CheckBoxStyles.CHECK_BOX, buttonListener, null, table);


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
	 */
	protected void resetSearchFilters() {
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


		// Add actions
		// Revision
		if (mScene.getSelectedAction() == ExploreActions.LOAD) {
			Button button = mUiFactory.button.createText("Select Revision", TextButtonStyles.FILLED_PRESS);
			new ButtonListener(button) {
				@Override
				protected void onPressed(Button button) {
					// TODO select revision
				}
			};
			// tabWidget.addActionButtonGlobal(button);
			// tabWidget.addActionButtonRow();

			// TODO create appropriate hiders
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
			float marginLeft = mLeftPanel.isVisible() ? mLeftPanel.getWidth() : mUiFactory.getStyles().vars.paddingOuter;
			float marginRight = mRightPanel.getWidth();
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

	private boolean mAddingContent = false;
	private int mActorsPerRow = 0;
	private ExploreScene mScene = null;

	/** Left panel tab widget */
	protected TabWidget mLeftPanel = null;
	/** Right panel tab widget */
	protected TabWidget mRightPanel = null;
	/** For getting correct date */
	protected SettingDateRepo mDateRepo = SettingRepo.getInstance().date();

	private InnerWidgets mWidgets = null;

	private class InnerWidgets implements Disposable {
		Content content = new Content();
		View view = new View();
		Search search = new Search();
		Background topBar = null;

		class Content implements Disposable {
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

		class View implements Disposable {
			AlignTable table = new AlignTable();
			ButtonGroup buttonGroup = new ButtonGroup();

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
		}
	}
}

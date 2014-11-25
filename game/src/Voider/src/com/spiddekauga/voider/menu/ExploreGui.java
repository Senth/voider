package com.spiddekauga.voider.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.ScrollPaneListener;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.ScrollPaneStyle;
import com.badlogic.gdx.utils.Disposable;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.utils.scene.ui.AlignTable;
import com.spiddekauga.utils.scene.ui.AnimationWidget;
import com.spiddekauga.utils.scene.ui.AnimationWidget.AnimationWidgetStyle;
import com.spiddekauga.utils.scene.ui.Row;
import com.spiddekauga.utils.scene.ui.TabWidget;
import com.spiddekauga.utils.scene.ui.VisibilityChangeListener;
import com.spiddekauga.voider.repo.misc.SettingRepo;
import com.spiddekauga.voider.repo.misc.SettingRepo.SettingDateRepo;
import com.spiddekauga.voider.repo.resource.SkinNames;
import com.spiddekauga.voider.scene.Gui;
import com.spiddekauga.voider.scene.ui.UiFactory.Positions;

/**
 * Common GUI for all explore scenes
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
abstract class ExploreGui extends Gui {
	/**
	 * Set the explore scene
	 * @param exploreScene
	 */
	void setExploreScene(ExploreScene exploreScene) {
		mExploreScene = exploreScene;
	}

	@Override
	public void initGui() {
		super.initGui();

		mWidgets = new InnerWidgets();

		initRightPanel();
		initLeftPanel();
		initContent();

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
	}

	/**
	 * Reset the content
	 */
	void resetContent() {
		mWidgets.content.table.dispose();
		mWidgets.content.buttonGroup = new ButtonGroup();

		if (mExploreScene != null && mExploreScene.isFetchingContent()) {
			addWaitIconToContent();
		}
	}

	/**
	 * Initialize the left panel
	 */
	private void initLeftPanel() {
		TabWidget tabWidget = mUiFactory.createRightPanel();
		tabWidget.setName("left-panel");
		mLeftPanel = tabWidget;

		tabWidget.setMarginRight(0).setPadRight(0);
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
	 * Initialize the right panel
	 */
	private void initRightPanel() {
		TabWidget tabWidget = mUiFactory.createRightPanel();
		tabWidget.setName("right-panel");
		mRightPanel = tabWidget;

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
					if (mExploreScene.isFetchingContent() && mExploreScene.hasMoreContent()) {
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
	protected abstract void onFetchMoreContent();

	/**
	 * Reset content margins
	 */
	protected void resetContentMargins() {
		if (mWidgets.content.scrollPane != null) {
			float screenWidth = Gdx.graphics.getWidth();
			float screenHeight = Gdx.graphics.getHeight();
			float marginLeft = mLeftPanel.isVisible() ? mLeftPanel.getWidth() : mUiFactory.getStyles().vars.paddingOuter;
			float marginRight = mRightPanel.getWidth() - mRightPanel.getMarginLeft() - mRightPanel.getPadLeft();
			float marginTop = mLeftPanel.getMarginTop();
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
			if (mActorsPerRow != cActorsPerRowOld) {
				mExploreScene.repopulateContent();
			}
		}
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
		if (mExploreScene.hasMoreContent() && mWidgets.content.table.getHeight() < mWidgets.content.scrollPane.getHeight()) {
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
	private ExploreScene mExploreScene = null;

	/** Left panel tab widget */
	protected TabWidget mLeftPanel = null;
	/** Right panel tab widget */
	protected TabWidget mRightPanel = null;
	/** For getting correct date */
	protected SettingDateRepo mDateRepo = SettingRepo.getInstance().date();

	private InnerWidgets mWidgets = null;

	private class InnerWidgets implements Disposable {
		Content content = new Content();

		class Content implements Disposable {
			AlignTable table = new AlignTable();
			ScrollPane scrollPane = null;
			ButtonGroup buttonGroup = new ButtonGroup();
			Row waitIconRow = null;

			@Override
			public void dispose() {
				table.dispose();
			}
		}

		@Override
		public void dispose() {
			content.dispose();
		}
	}
}

package com.spiddekauga.utils.scene.ui;

import java.util.ArrayList;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.ScrollPaneStyle;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.voider.scene.ui.UiFactory.Positions;

/**
 * Widget that allows for tab functionality.
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class TabWidget extends AlignTable {
	/**
	 * Default constructor.
	 * <ul>
	 * <li>Widget alignment to top left</li>
	 * <li>Tab alignment to left</li>
	 * <li>No margin</li>
	 * <li>No padding</li>
	 * <li>No background and fill height</li>
	 * </ul>
	 */
	public TabWidget() {
		setAlign(Horizontal.LEFT, Vertical.TOP);
		mTabTable.setAlign(TAB_ALIGN_TOP_BOTTOM_DEFAULT);
		mTabTable.setName("tab-table");
		mContentInnerTable.setAlign(Horizontal.LEFT, Vertical.TOP);
		mContentInnerTable.setName("content-inner-table");
		mContentOuterTable.setAlign(Horizontal.LEFT, Vertical.TOP);
		mContentOuterTable.setName("content-outer-table");

		mTabRow = super.row(getRowCount(), TAB_ALIGN_TOP_BOTTOM_DEFAULT.horizontal, TAB_ALIGN_TOP_BOTTOM_DEFAULT.vertical);
		super.add(mTabTable);

		mContentOuterRow = super.row(getRowCount(), Horizontal.LEFT, Vertical.TOP);
		mContentOuterCell = super.add(mContentOuterTable);

		mContentOuterTable.row().setFillWidth(true).setFillHeight(true);
		mContentOuterTable.add(mContentInnerTable).setFillWidth(true).setFillHeight(true);

		mActionTable.setKeepWidth(true);
		super.row(getRowCount(), Horizontal.LEFT, Vertical.MIDDLE);
		super.add(mActionTable);
		addActionButtonRow();
	}

	@Override
	public void dispose(boolean disposeActors) {
		mContentInnerTable.dispose(disposeActors);
		mTabButtons.clear();
		mTabTable.dispose();
		mActionTable.dispose();
	}

	@Override
	public void invalidate() {
		super.invalidate();

		mContentInnerTable.invalidate();
		mContentOuterTable.invalidate();
		mTabTable.invalidate();
		mActionTable.invalidate();
	}

	@Override
	public void invalidateHierarchy() {
		invalidate();
		super.invalidateHierarchy();
	}

	@Override
	public void layout() {
		// Update visibility
		if (isAllTabsHidden() && isVisible()) {
			setVisible(false);
		} else if (!isAllTabsHidden() && !isVisible()) {
			setVisible(true);
		}

		updateActionButtonPadding();
		super.layout();
	}

	/**
	 * @deprecated no more rows can be added to TabWidget
	 */
	@Deprecated
	@Override
	public Row row() {
		throw new UnsupportedOperationException("Use TabWidget#addTab()");
	}

	/**
	 * @deprecated no more rows can be added to TabWidget
	 */
	@Deprecated
	@Override
	public Row row(Horizontal horizontal, Vertical vertical) {
		throw new UnsupportedOperationException("Use TabWidget#addTab()");
	}

	/**
	 * @deprecated no more rows can be added to TabWidget
	 */
	@Deprecated
	@Override
	public Row row(int index) {
		throw new UnsupportedOperationException("Use TabWidget#addTab()");
	}

	/**
	 * @deprecated no more rows can be added to TabWidget
	 */
	@Deprecated
	@Override
	public Row row(int index, Horizontal horizontal, Vertical vertical) {
		throw new UnsupportedOperationException("Use TabWidget#addTab()");
	}

	/**
	 * @deprecated no more rows can be added to TabWidget
	 */
	@Deprecated
	@Override
	public Row row(Align alignment) {
		throw new UnsupportedOperationException("Use TabWidget#addTab()");
	}

	/**
	 * @deprecated no more rows can be added to TabWidget
	 */
	@Deprecated
	@Override
	public Cell add() {
		throw new UnsupportedOperationException("Use TabWidget#addTab()");
	}

	/**
	 * @deprecated no more rows can be added to TabWidget
	 */
	@Deprecated
	@Override
	public Cell add(Actor actor) {
		throw new UnsupportedOperationException("Use TabWidget#addTab()");
	}

	/**
	 * @deprecated no more rows can be added to TabWidget
	 */
	@Deprecated
	@Override
	public ArrayList<Cell> add(int cellCount) {
		throw new UnsupportedOperationException("Use TabWidget#addTab()");
	}

	/**
	 * Set to true to fill the available height
	 * @param fillHeight true to fill the height
	 * @return this for chaining
	 */
	public TabWidget setFillHeight(boolean fillHeight) {
		mContentOuterRow.setFillHeight(fillHeight);
		mContentOuterCell.setFillHeight(fillHeight);
		mContentOuterTable.invalidateHierarchy();
		return this;
	}

	/**
	 * Adds a new content tab which is scrollable
	 * @param imageButton tab button image
	 * @param table the table to display this in this tab
	 * @param hider the hider that will hide the table
	 */
	public void addTabScroll(ImageButton imageButton, AlignTable table, HideListener hider) {
		addTab(imageButton, table, hider, true);
	}

	/**
	 * Adds a new content tab which is scrollable. Automatically creates an anonymous
	 * hider
	 * @param imageButton tab button image
	 * @param table the table to display in this tab
	 */
	public void addTabScroll(ImageButton imageButton, AlignTable table) {
		addTabScroll(imageButton, table, new HideListener(true));
	}

	/**
	 * Adds a new content tab
	 * @param imageButtonStyle the style to use for the tab
	 * @param table the table to display in this tab
	 * @param hider the hider that will hide the table
	 */
	public void addTab(ImageButton imageButtonStyle, AlignTable table, HideListener hider) {
		addTab(imageButtonStyle, table, hider, false);
	}

	/**
	 * Adds a new content tab
	 * @param imageButton the style to use for the tab
	 * @param table the table to display in this tab
	 * @param hider the hider that will hide the table
	 * @param scrollable if the tab should be scrollable
	 */
	private void addTab(ImageButton imageButton, AlignTable table, HideListener hider, boolean scrollable) {
		mButtonGroup.add(imageButton);
		mTabHiderLast = hider;

		if (mTabPosition.isLeftOrRight()) {
			mTabTable.row();
		}

		mTabTable.add(imageButton);
		mTabButtons.add(imageButton);

		Actor addActor = table;

		// Create an anonymous inner table if scrollable
		if (scrollable) {
			ScrollPane scrollPane = new ScrollPane(table, mScrollPaneStyle);
			scrollPane.setScrollingDisabled(true, false);
			scrollPane.setCancelTouchFocus(false);
			table.setFillParentWidth(true);
			table.setAlign(Horizontal.LEFT, Vertical.TOP);
			addActor = scrollPane;
		}

		imageButton.addListener(mTabVisibilityListener);
		imageButton.addListener(mTabCheckListener);
		hider.addToggleActor(addActor);
		hider.addButton(imageButton);
		mContentInnerTable.add(addActor).setFillWidth(true).setFillHeight(true);
		mContentInnerTable.getRow().setFillWidth(true).setFillHeight(true);
		invalidateHierarchy();
	}

	/**
	 * Adds a new content tab, automatically creates an anonymous hider
	 * @param imageButton image button for the tab
	 * @param table the table to display in this tab
	 */
	public void addTab(ImageButton imageButton, AlignTable table) {
		addTab(imageButton, table, new HideListener(true));
	}

	/**
	 * Adds an action button to the action table (usually below the content).
	 * @pre a tab must have been added through any of the following methods as these
	 *      action buttons are only visible for this tab
	 *      {@link #addTab(ImageButton, AlignTable)},
	 *      {@link #addTab(ImageButton, AlignTable, HideListener)},
	 *      {@link #addTabScroll(ImageButton, AlignTable)}, or
	 *      {@link #addTabScroll(ImageButton, AlignTable, HideListener)}
	 * @param button the button to add
	 * @see #addActionButtonGlobal(Button) to add a global action button
	 */
	public void addActionButton(Button button) {
		if (mTabHiderLast == null) {
			throw new IllegalStateException("No tab added yet.");
		}
		mTabHiderLast.addToggleActor(button);
		addActionButtonCommon(button);
	}

	/**
	 * Add a global action button. This button is shown all the time if not hidden by
	 * another widget.
	 * @param button the button to add
	 * @see #addActionButton(Button) to add an action button to the last added tab
	 */
	public void addActionButtonGlobal(Button button) {
		addActionButtonCommon(button);
	}

	private void addActionButtonCommon(Button button) {
		button.addListener(mActionButtonVisibilityListener);

		Cell cell = mActionTable.add(button);
		cell.setFillWidth(true);
		if (mActionButtonHeight != 0) {
			cell.setHeight(mActionButtonHeight);
		}
	}

	/**
	 * Adds a top row to the action button table
	 */
	public void addActionButtonRowTop() {
		mActionTable.row(0).setFillWidth(true);
	}

	/**
	 * Adds a row to the action button table
	 */
	public void addActionButtonRow() {
		mActionTable.row().setFillWidth(true);
	}

	/**
	 * Sets and updates the current action button padding
	 * @param pad amount to pad between the buttons and between buttons and the content.
	 */
	public void setActionButtonPad(float pad) {
		mActionButtonPadding = pad;
		updateActionButtonPadding();
	}

	/**
	 * Sets and updates the current action button height
	 * @param height height of all action buttons. 0 will reset the height
	 */
	public void setActionButtonHeight(float height) {
		mActionButtonHeight = height;

		if (height != 0) {
			for (Row row : mActionTable.getRows()) {
				for (Cell cell : row.getCells()) {
					cell.setHeight(mActionButtonHeight);
				}
			}
		} else {
			for (Row row : mActionTable.getRows()) {
				for (Cell cell : row.getCells()) {
					cell.resetHeight();
				}
			}
		}
	}

	/**
	 * Update the width of the action button
	 */
	private void updateActionButtonWidth() {
		mActionTable.setWidth(mContentOuterTable.getWidth());
	}

	/**
	 * Update existing button padding
	 */
	private void updateActionButtonPadding() {
		Padding rowPadding = new Padding();
		if (mTabPosition != Positions.BOTTOM) {
			rowPadding.top = mActionButtonPadding;
		} else {
			rowPadding.bottom = mActionButtonPadding;
		}
		for (Row row : mActionTable.getRows()) {
			row.setPad(rowPadding);

			// Set button padding for all except last visible button
			Cell lastVisibleCell = null;
			for (Cell cell : row.getCells()) {
				if (cell.isVisible()) {
					cell.setPadRight(mActionButtonPadding);
					lastVisibleCell = cell;
				}
			}

			if (lastVisibleCell != null) {
				lastVisibleCell.setPadRight(0);
			}
		}
	}

	@Override
	public Vector2 localToParentCoordinates(Vector2 localCoords) {
		final float rotation = -getRotation();
		final float scaleX = getScaleX();
		final float scaleY = getScaleY();
		final float x = getX();
		final float y = getY();
		if (rotation == 0) {
			if (scaleX == 1 && scaleY == 1) {
				localCoords.x += x;
				localCoords.y += y;
			} else {
				final float originX = getOriginX();
				final float originY = getOriginY();
				localCoords.x = (localCoords.x - originX) * scaleX + originX + x;
				localCoords.y = (localCoords.y - originY) * scaleY + originY + y;
			}
		} else {
			final float cos = (float) Math.cos(rotation * MathUtils.degreesToRadians);
			final float sin = (float) Math.sin(rotation * MathUtils.degreesToRadians);
			final float originX = getOriginX();
			final float originY = getOriginY();
			final float tox = (localCoords.x - originX) * scaleX;
			final float toy = (localCoords.y - originY) * scaleY;
			localCoords.x = (tox * cos + toy * sin) + originX + x;
			localCoords.y = (tox * -sin + toy * cos) + originY + y;
		}
		return localCoords;
	}

	/**
	 * Set content width
	 * @param width width of the content
	 * @return this for chaining
	 */
	public TabWidget setContentWidth(float width) {
		mContentOuterTable.setWidth(width);
		mContentOuterTable.setKeepWidth(true);
		updateActionButtonWidth();
		return this;
	}

	/**
	 * Set content height
	 * @param height height of the content
	 * @return this for chaining
	 */
	public TabWidget setContentHeight(float height) {
		mContentOuterTable.setHeight(height);
		mContentOuterTable.setKeepHeight(true);
		return this;
	}

	/**
	 * Set if content can be hidden by clicking on the active tab
	 * @param hideable true if the content can be hidden
	 * @return this for chaining
	 */
	public TabWidget setContentHideable(boolean hideable) {
		mContentHideable = hideable;
		mButtonGroup.setMinCheckCount(hideable ? 0 : 1);
		if (mButtonGroup.getChecked() == null) {
			setContentVisibility(false);
		}
		return this;
	}

	/**
	 * @return true if the content can be hidden by clicking on the active tab
	 */
	public boolean isContentHideable() {
		return mContentHideable;
	}

	@Override
	public TabWidget setPad(float top, float right, float bottom, float left) {
		mContentOuterTable.setPad(top, right, bottom, left);
		updateActionButtonWidth();
		return this;
	}

	@Override
	public TabWidget setPad(float padding) {
		mContentOuterTable.setPad(padding);
		updateActionButtonWidth();
		return this;
	}

	@Override
	public TabWidget setPad(Padding padding) {
		mContentOuterTable.setPad(padding);
		updateActionButtonWidth();
		return this;
	}

	@Override
	public TabWidget setPadLeft(float paddingLeft) {
		mContentOuterTable.setPadLeft(paddingLeft);
		updateActionButtonWidth();
		return this;
	}

	@Override
	public TabWidget setPadRight(float paddingRight) {
		mContentOuterTable.setPadRight(paddingRight);
		updateActionButtonWidth();
		return this;
	}

	@Override
	public TabWidget setPadBottom(float paddingBottom) {
		mContentOuterTable.setPadBottom(paddingBottom);
		return this;
	}

	@Override
	public TabWidget setPadTop(float paddingTop) {
		mContentOuterTable.setPadTop(paddingTop);
		return this;
	}

	@Override
	public float getPadTop() {
		return mContentOuterTable.getPadTop();
	}

	@Override
	public float getPadRight() {
		return mContentOuterTable.getPadRight();
	}

	@Override
	public float getPadBottom() {
		return mContentOuterTable.getPadBottom();
	}

	@Override
	public float getPadLeft() {
		return mContentOuterTable.getPadLeft();
	}

	@Override
	public Padding getPad() {
		return mContentOuterTable.getPad();
	}

	@Override
	public float getPadX() {
		return mContentOuterTable.getPadX();
	}

	@Override
	public float getPadY() {
		return mContentOuterTable.getPadY();
	}

	@Override
	public float getWidth() {
		return getWidthNoPadding();
	}

	@Override
	public float getHeight() {
		return getHeightNoPadding();
	}

	/**
	 * Set tab alignment (when tab position is top or bottom)
	 * @param horizontal horizontal alignment
	 * @return this for chaining
	 * @see #setAlign(Horizontal,Vertical) for entire widget alignment
	 */
	public TabWidget setAlignTab(Horizontal horizontal) {
		if (mTabPosition == Positions.TOP || mTabPosition == Positions.BOTTOM) {
			mTabRow.setAlign(horizontal, Vertical.MIDDLE);
		}
		return this;
	}

	/**
	 * Set tab alignment (when tab position is left or right)
	 * @param vertical vertical alignment
	 * @return this for chaining
	 * @see #setAlign(Horizontal, Vertical) for entire widget alignment
	 */
	public TabWidget setAlignTab(Vertical vertical) {
		if (mTabPosition == Positions.LEFT || mTabPosition == Positions.RIGHT) {
			mTabRow.setAlign(Horizontal.CENTER, vertical);
		}
		return this;
	}

	/**
	 * Sets the tab location. If you changed the position from bottom or top to left or
	 * right (or vice versa) you need to call either {@link #setAlignTab(Horizontal)} or
	 * {@link #setAlignTab(Vertical)} if you want to use another alignment than the
	 * default one.
	 * @param position tab position relative to the content
	 * @return this for chaining
	 */
	public TabWidget setTabPosition(Positions position) {
		// Skip if new position is same as old
		if (position == mTabPosition) {
			return this;
		}


		// Is fill height?
		boolean fillHeight = mContentOuterRow.shallFillHeight();

		Align tabAlign = getTabAlignment(mTabRow.getAlign(), mTabPosition, position);

		// Remove Tab and Outer table
		mTabTable.remove();
		mContentOuterTable.remove();
		mActionTable.remove();
		super.dispose(false);

		// Add Tab at right position
		switch (position) {
		case BOTTOM:
			super.row(getRowCount(), Horizontal.LEFT, Vertical.TOP);
			super.add(mActionTable);

			mContentOuterRow = super.row(Horizontal.LEFT, Vertical.TOP);
			mContentOuterCell = super.add(mContentOuterTable);

			mTabRow = super.row(getRowCount(), tabAlign.horizontal, tabAlign.vertical);
			super.add(mTabTable);

			break;

		case LEFT:
			mTabRow = super.row(getRowCount(), tabAlign.horizontal, tabAlign.vertical);
			mContentOuterRow = mTabRow;
			super.add(mTabTable);
			mContentOuterCell = super.add(mContentOuterTable);
			super.row(getRowCount(), Horizontal.LEFT, Vertical.TOP);
			super.add(mActionTable);

			break;

		case RIGHT:
			mTabRow = super.row(getRowCount(), tabAlign.horizontal, tabAlign.vertical);
			mContentOuterRow = mTabRow;
			mContentOuterCell = super.add(mContentOuterTable);
			super.add(mTabTable);
			super.row(getRowCount(), Horizontal.LEFT, Vertical.TOP);
			super.add(mActionTable);

			break;

		case TOP:
			mTabRow = super.row(getRowCount(), tabAlign.horizontal, tabAlign.vertical);
			super.add(mTabTable);

			mContentOuterRow = super.row(Horizontal.LEFT, Vertical.TOP);
			mContentOuterCell = super.add(mContentOuterTable);
			super.row(getRowCount(), Horizontal.LEFT, Vertical.TOP);
			super.add(mActionTable);
			break;
		}

		setFillHeight(fillHeight);

		layoutTabButtons(mTabPosition, position);

		mTabPosition = position;

		updateActionButtonPadding();
		invalidate();

		return this;
	}

	/**
	 * Layout tab buttons again after position was changed
	 * @param oldPosition
	 * @param newPosition
	 */
	private void layoutTabButtons(Positions oldPosition, Positions newPosition) {
		if ((oldPosition.isLeftOrRight() && newPosition.isLeftOrRight()) || (oldPosition.isTopOrBottom() && newPosition.isTopOrBottom())) {
			return;
		}

		mTabTable.dispose();


		// Top/Bottom -> One row
		if (newPosition.isTopOrBottom()) {
			for (Button button : mTabButtons) {
				mTabTable.add(button);
			}
		}
		// Left/Right -> Many rows one column
		else {
			for (Button button : mTabButtons) {
				mTabTable.row();
				mTabTable.add(button);
			}
		}
	}

	/**
	 * Get tab alignment
	 * @param oldAlignment old tab alignment
	 * @param oldPosition old position
	 * @param newPosition new position
	 * @return new tab alignment
	 */
	private static Align getTabAlignment(Align oldAlignment, Positions oldPosition, Positions newPosition) {
		switch (newPosition) {
		case BOTTOM:
			if (oldPosition == Positions.TOP) {
				return oldAlignment;
			} else {
				return TAB_ALIGN_TOP_BOTTOM_DEFAULT;
			}

		case LEFT:
			if (oldPosition == Positions.RIGHT) {
				return oldAlignment;
			} else {
				return TAB_ALIGN_LEFT_RIGHT_DEFAULT;
			}

		case RIGHT:
			if (oldPosition == Positions.LEFT) {
				return oldAlignment;
			} else {
				return TAB_ALIGN_LEFT_RIGHT_DEFAULT;
			}

		case TOP:
			if (oldPosition == Positions.BOTTOM) {
				return oldAlignment;
			} else {
				return TAB_ALIGN_TOP_BOTTOM_DEFAULT;
			}
		}

		return oldAlignment;
	}

	/**
	 * Set background for content
	 * @param background content background image
	 * @return this for chaining
	 * @see #setFillHeight(boolean) to fill tab
	 */
	public TabWidget setBackground(Image background) {
		mContentOuterTable.setBackgroundImage(background);
		return this;
	}

	/**
	 * Set scroll pane style
	 * @param style new scrollpane style
	 */
	public void setScrollPaneStyle(ScrollPaneStyle style) {
		mScrollPaneStyle = style;

		for (ScrollPane scrollPane : mScrollPanes) {
			scrollPane.setStyle(style);
		}
	}

	/**
	 * Handle visibility change on specified tab
	 * @param tab tab button that change visibility
	 */
	private void handleVisibilityChange(Button tab) {
		// Visible
		if (tab.isVisible()) {
			// Set the correct visible tab
			if (tab.isChecked()) {
				setContentVisibility(true);
			}

			// Show Tabs (select default one if none have been)
			if (!isVisible()) {
				setVisible(true);
				if (!isContentHideable()) {
					tab.setChecked(true);
				}
			}
		}
		// Hidden
		else {
			// Hide entire widget
			if (isAllTabsHidden()) {
				setVisible(false);

				if (isContentHideable()) {
					setContentVisibility(false);
				}
			}
			// Select another tab
			else if (!isContentHideable()) {
				for (Button button : mTabButtons) {
					if (button.isVisible()) {
						button.setChecked(true);
						break;
					}
				}
			}
		}
	}

	/**
	 * @return true if all tabs are hidden
	 */
	private boolean isAllTabsHidden() {
		for (Button button : mTabButtons) {
			if (button.isVisible()) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Sets the visibility of the content
	 * @param visible true if visible
	 */
	private void setContentVisibility(boolean visible) {
		mContentOuterTable.setVisible(visible);
		mActionTable.setVisible(visible);
		invalidate();
		fire(new VisibilityChangeListener.VisibilityChangeEvent());
	}

	/**
	 * @return true if the content is visible
	 */
	private boolean isContentVisible() {
		return mContentOuterTable.isVisible();
	}

	/**
	 * Listens to tab checked/unchecked events
	 */
	private EventListener mTabCheckListener = new ButtonListener() {
		@Override
		protected void onChecked(Button button, boolean checked) {
			if (isContentHideable()) {
				// Show if was hidden
				if (checked && !isContentVisible()) {
					setContentVisibility(true);
				}
				// Hide if no other tab was checked
				else if (!checked && mButtonGroup.getChecked() == null) {
					setContentVisibility(false);
				}
			}
		}
	};

	/**
	 * Listens to visibility change events for tab buttons. I.e. entire tabs are hidden
	 */
	private EventListener mTabVisibilityListener = new VisibilityChangeListener() {
		@Override
		public void onVisibilyChange(VisibilityChangeEvent event, Actor actor) {
			if (actor instanceof Button) {
				handleVisibilityChange((Button) actor);
			}
		}
	};

	/**
	 * Listens to visibility change events for action buttons
	 */
	private EventListener mActionButtonVisibilityListener = new VisibilityChangeListener() {
		@Override
		public void onVisibilyChange(VisibilityChangeEvent event, Actor actor) {
			updateActionButtonPadding();
		}
	};

	private static final Align TAB_ALIGN_TOP_BOTTOM_DEFAULT = new Align(Horizontal.LEFT, Vertical.MIDDLE);
	private static final Align TAB_ALIGN_LEFT_RIGHT_DEFAULT = new Align(Horizontal.CENTER, Vertical.TOP);
	private Positions mTabPosition = Positions.TOP;
	private ScrollPaneStyle mScrollPaneStyle = new ScrollPaneStyle();
	/** Inner scroll pane */
	private ArrayList<ScrollPane> mScrollPanes = new ArrayList<>();
	private ArrayList<Button> mTabButtons = new ArrayList<>();
	/** Tab row, for setting alignment */
	private Row mTabRow = null;
	/** Tab table (all tabs) */
	private AlignTable mTabTable = new AlignTable();
	/** Content (inner) of the tabs */
	private AlignTable mContentInnerTable = new AlignTable();
	/** Content (outer with background) of the tabs */
	private AlignTable mContentOuterTable = new AlignTable();
	/** Action buttons (below the outer content) */
	private AlignTable mActionTable = new AlignTable();
	/** Row that contains the outer table */
	private Row mContentOuterRow = null;
	/** Cell that contains the outer table */
	private Cell mContentOuterCell = null;
	/** Tab button group */
	private ButtonGroup<Button> mButtonGroup = new ButtonGroup<>();
	/** The hider that is used for the last added tab */
	private HideListener mTabHiderLast = null;
	/** Button padding for the action buttons */
	private float mActionButtonPadding = 0;
	private float mActionButtonHeight = 0;
	private boolean mContentHideable = false;
}

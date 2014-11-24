package com.spiddekauga.utils.scene.ui;

import java.util.ArrayList;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
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

		mTabRow = super.row(TAB_ALIGN_TOP_BOTTOM_DEFAULT);
		super.add(mTabTable);

		mContentOuterRow = super.row(Horizontal.LEFT, Vertical.TOP);
		mContentOuterCell = super.add(mContentOuterTable);

		mContentOuterTable.row().setFillWidth(true).setFillHeight(true);
		mContentOuterTable.add(mContentInnerTable).setFillWidth(true).setFillHeight(true);
	}

	@Override
	public void dispose(boolean disposeActors) {
		mContentInnerTable.dispose(disposeActors);
		mTabButtons.clear();
		mTabTable.dispose();
	}

	@Override
	public void invalidate() {
		super.invalidate();

		mContentInnerTable.invalidate();
		mContentOuterTable.invalidate();
		mTabTable.invalidate();
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
	 * @param imageButtonStyle the style to use for the tab
	 * @param table the table to display this in this tab
	 * @param hider the hider that will hide the table
	 * @return created tab button
	 */
	public Button addTabScroll(ImageButtonStyle imageButtonStyle, AlignTable table, HideListener hider) {
		return addTab(imageButtonStyle, table, hider, true);
	}

	/**
	 * Adds a new content tab which is scrollable. Automatically creates an anonymous
	 * hider
	 * @param imageButtonStyle the style to use for the tab
	 * @param table the table to display in this tab
	 * @return created tab button
	 */
	public Button addTabScroll(ImageButtonStyle imageButtonStyle, AlignTable table) {
		return addTabScroll(imageButtonStyle, table, new HideListener(true));
	}

	/**
	 * Adds a new content tab
	 * @param imageButtonStyle the style to use for the tab
	 * @param table the table to display in this tab
	 * @param hider the hider that will hide the table
	 * @return create tab button
	 */
	public Button addTab(ImageButtonStyle imageButtonStyle, AlignTable table, HideListener hider) {
		return addTab(imageButtonStyle, table, hider, false);
	}

	/**
	 * Adds a new content tab
	 * @param imageButtonStyle the style to use for the tab
	 * @param table the table to display in this tab
	 * @param hider the hider that will hide the table
	 * @param scrollable if the tab should be scrollable
	 * @return create tab button
	 */
	private Button addTab(ImageButtonStyle imageButtonStyle, AlignTable table, HideListener hider, boolean scrollable) {
		ImageButton button = new ImageButton(imageButtonStyle);
		mButtonGroup.add(button);

		if (mTabPosition.isLeftOrRight()) {
			mTabTable.row();
		}

		mTabTable.add(button);
		mTabButtons.add(button);

		Actor addActor = table;

		// Create an anonymous inner table if scrollable
		if (scrollable) {
			ScrollPane scrollPane = new ScrollPane(table, mScrollPaneStyle);
			scrollPane.setScrollingDisabled(true, false);
			scrollPane.setCancelTouchFocus(false);
			addActor = scrollPane;
		}

		button.addListener(mTabVisibilityListener);
		hider.addToggleActor(addActor);
		hider.setButton(button);
		mContentInnerTable.add(addActor).setFillWidth(true).setFillHeight(true);
		mContentInnerTable.getRow().setFillWidth(true).setFillHeight(true);
		invalidateHierarchy();
		return button;
	}

	/**
	 * Adds a new content tab, automatically creates an anonymous hider
	 * @param imageButtonStyle the style to use for the tab
	 * @param table the table to display in this tab
	 * @return created tab button
	 */
	public Button addTab(ImageButtonStyle imageButtonStyle, AlignTable table) {
		return addTab(imageButtonStyle, table, new HideListener(true));
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

	@Override
	public TabWidget setPad(float top, float right, float bottom, float left) {
		mContentOuterTable.setPad(top, right, bottom, left);
		return this;
	}

	@Override
	public TabWidget setPad(float padding) {
		mContentOuterTable.setPad(padding);
		return this;
	}

	@Override
	public TabWidget setPad(Padding padding) {
		mContentOuterTable.setPad(padding);
		return this;
	}

	@Override
	public TabWidget setPadLeft(float paddingLeft) {
		mContentOuterTable.setPadLeft(paddingLeft);
		return this;
	}

	@Override
	public TabWidget setPadRight(float paddingRight) {
		mContentOuterTable.setPadRight(paddingRight);
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
			// return this;
		}


		// Is fill height?
		boolean fillHeight = mContentOuterRow.shallFillHeight();

		Align tabAlign = getTabAlignment(mTabRow.getAlign(), mTabPosition, position);

		// Remove Tab and Outer table
		mTabTable.remove();
		mContentOuterTable.remove();

		// Add Tab at right position
		switch (position) {
		case BOTTOM:
			mContentOuterRow = super.row(Horizontal.LEFT, Vertical.TOP);
			mContentOuterCell = super.add(mContentOuterTable);

			mTabRow = super.row(tabAlign);
			super.add(mTabTable);

			break;

		case LEFT:
			mTabRow = super.row(tabAlign);
			mContentOuterRow = mTabRow;
			super.add(mTabTable);
			mContentOuterCell = super.add(mContentOuterTable);

			break;

		case RIGHT:
			mTabRow = super.row(tabAlign);
			mContentOuterRow = mTabRow;
			mContentOuterCell = super.add(mContentOuterTable);
			super.add(mTabTable);

			break;

		case TOP:
			mTabRow = super.row(tabAlign);
			super.add(mTabTable);

			mContentOuterRow = super.row(Horizontal.LEFT, Vertical.TOP);
			mContentOuterCell = super.add(mContentOuterTable);
			break;
		}

		setFillHeight(fillHeight);

		layoutTabButtons(mTabPosition, position);

		mTabPosition = position;
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
			// Show and select tab
			if (!isVisible()) {
				setVisible(true);
				tab.setChecked(true);
			}
		}
		// Hidden
		else {
			// Hide entire widget
			if (isAllTabsHidden()) {
				setVisible(false);
			}
			// Select another tab
			else {
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
	 * Listens to visibility change events for tab buttons. I.e. entire tabs are hidden
	 */
	private EventListener mTabVisibilityListener = new EventListener() {
		@Override
		public boolean handle(Event event) {
			if (event instanceof VisibilityChangeEvent) {
				if (event.getTarget() instanceof Button) {
					handleVisibilityChange((Button) event.getTarget());
				}
			}
			return false;
		}
	};


	private static final Align TAB_ALIGN_TOP_BOTTOM_DEFAULT = new Align(Horizontal.LEFT, Vertical.MIDDLE);
	private static final Align TAB_ALIGN_LEFT_RIGHT_DEFAULT = new Align(Horizontal.CENTER, Vertical.TOP);
	/** Position of the tabs */
	private Positions mTabPosition = Positions.TOP;
	/** Current scroll pane style */
	private ScrollPaneStyle mScrollPaneStyle = new ScrollPaneStyle();
	/** Inner scroll pane */
	private ArrayList<ScrollPane> mScrollPanes = new ArrayList<>();
	/** All tab buttons */
	private ArrayList<Button> mTabButtons = new ArrayList<>();
	/** Tab row, for setting alignment */
	private Row mTabRow = null;
	/** Tab table (all tabs) */
	private AlignTable mTabTable = new AlignTable();
	/** Content (inner) of the tabs */
	private AlignTable mContentInnerTable = new AlignTable();
	/** Content (outer with background) of the tabs */
	private AlignTable mContentOuterTable = new AlignTable();
	/** Row that contains the outer table */
	private Row mContentOuterRow = null;
	/** Cell that contains the outer table */
	private Cell mContentOuterCell = null;
	/** Tab button group */
	private ButtonGroup mButtonGroup = new ButtonGroup();
}

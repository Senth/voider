package com.spiddekauga.utils.scene.ui;

import java.util.ArrayList;

import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;

/**
 * Widget that allows for tab functionality.
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class TabWidget extends WidgetGroup implements IMargin<TabWidget>, IPadding<TabWidget> {
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
		mWrapperTable.setAlign(Horizontal.LEFT, Vertical.TOP);
		mTabTable.setAlign(Horizontal.LEFT, Vertical.TOP);
		mContentInnerTable.setAlign(Horizontal.LEFT, Vertical.TOP);
		mContentOuterTable.setAlign(Horizontal.LEFT, Vertical.TOP);

		mTabRow = mWrapperTable.row();
		mWrapperTable.add(mTabTable);

		mContentOuterRow = mWrapperTable.row();
		mContentOuterCell = mWrapperTable.add(mContentOuterTable);

		mContentOuterTable.row().setFillWidth(true);
		mContentOuterTable.add(mContentInnerTable).setFillWidth(true);
		mContentOuterRowFill = mContentOuterTable.row();

		addActor(mWrapperTable);
	}

	/**
	 * Adds a new content tab
	 * @param imageButtonStyle the style to use for the tab
	 * @param table the table to display in this tab
	 * @param hider the hider that will hide the table
	 * @return create tab button
	 */
	public Button addTab(ImageButtonStyle imageButtonStyle, AlignTable table, HideListener hider) {
		ImageButton button = new ImageButton(imageButtonStyle);
		mButtonGroup.add(button);
		mTabTable.add(button);
		mTabButtons.add(button);
		button.addListener(mTabVisibilityListener);
		hider.addToggleActor(table);
		hider.setButton(button);
		mContentInnerTable.add(table).setFillWidth(true);
		mContentInnerTable.getRow().setFillWidth(true);
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
	public void layout() {
		mWrapperTable.layout();
	}

	@Override
	public float getHeight() {
		return mWrapperTable.getHeight();
	}

	@Override
	public float getWidth() {
		return mWrapperTable.getWidth();
	}

	@Override
	public float getMinWidth() {
		return mWrapperTable.getMinWidth();
	}

	@Override
	public float getMinHeight() {
		return mWrapperTable.getMinHeight();
	}

	@Override
	public float getPrefWidth() {
		return mWrapperTable.getPrefWidth();
	}

	@Override
	public float getPrefHeight() {
		return mWrapperTable.getPrefHeight();
	}

	@Override
	public float getMaxWidth() {
		return mWrapperTable.getMaxWidth();
	}

	@Override
	public float getMaxHeight() {
		return mWrapperTable.getMaxHeight();
	}

	/**
	 * @return available height inside the tab
	 */
	public float getAvailableHeight() {
		return mContentOuterTable.getAvailableHeight();
	}

	/**
	 * @return available width inside the tab
	 */
	public float getAvailableWidth() {
		return mContentOuterTable.getAvailableWidth();
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

	@Override
	public TabWidget setMargin(float top, float right, float bottom, float left) {
		mWrapperTable.setMargin(top, right, bottom, left);
		return this;
	}

	@Override
	public TabWidget setMargin(float margin) {
		mWrapperTable.setMargin(margin);
		return this;
	}

	@Override
	public TabWidget setMargin(Padding margin) {
		mWrapperTable.setMargin(margin);
		return this;
	}

	@Override
	public TabWidget setMarginLeft(float marginLeft) {
		mWrapperTable.setMarginLeft(marginLeft);
		return this;
	}

	@Override
	public TabWidget setMarginRight(float marginRight) {
		mWrapperTable.setMarginRight(marginRight);
		return this;
	}

	@Override
	public TabWidget setMarginBottom(float marginBottom) {
		mWrapperTable.setMarginBottom(marginBottom);
		return this;
	}

	@Override
	public TabWidget setMarginTop(float marginTop) {
		mWrapperTable.setMarginTop(marginTop);
		return this;
	}

	@Override
	public float getMarginTop() {
		return mWrapperTable.getMarginTop();
	}

	@Override
	public float getMarginRight() {
		return mWrapperTable.getMarginRight();
	}

	@Override
	public float getMarginBottom() {
		return mWrapperTable.getMarginBottom();
	}

	@Override
	public float getMarginLeft() {
		return mWrapperTable.getMarginLeft();
	}

	@Override
	public Padding getMargin() {
		return mWrapperTable.getMargin();
	};

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

	/**
	 * Set tab alignment
	 * @param horizontal horizontal alignment
	 * @return this for chaining
	 * @see #setAlign(Horizontal,Vertical)
	 */
	public TabWidget setTabAlign(Horizontal horizontal) {
		mTabRow.setAlign(horizontal, Vertical.MIDDLE);
		return this;
	}

	/**
	 * Set widget alignment
	 * @param horizontal alignment in x plane
	 * @param vertical alignment in y plane
	 * @return this for chaining
	 * @see #setTabAlign(Horizontal)
	 */
	public TabWidget setAlign(Horizontal horizontal, Vertical vertical) {
		mWrapperTable.setAlignTable(horizontal, vertical);
		return this;
	}

	/**
	 * Set to true to fill the available height
	 * @param fillHeight true to fill the height
	 * @return this for chaining
	 */
	public TabWidget setFillHeight(boolean fillHeight) {
		mContentOuterRow.setFillHeight(fillHeight);
		mContentOuterCell.setFillHeight(fillHeight);
		mContentOuterRowFill.setFillHeight(fillHeight);
		mContentOuterTable.invalidateHierarchy();
		return this;
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

	// /**
	// * @return true if all tabs are hidden
	// */
	// @Override
	// public boolean isVisible() {
	// return mWrapperTable.isVisible();
	// }
	//
	// public void setVisible(boolean visible) {
	// mWrapperTable.setVisible(true);
	// };

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
	 * Listens to visibily change events for tab buttons. I.e. entire tabs are hidden
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

	/** All tab buttons */
	private ArrayList<Button> mTabButtons = new ArrayList<>();
	/** Tab row, for setting alignment */
	private Row mTabRow = null;
	/** Wrapper table */
	private AlignTable mWrapperTable = new AlignTable();
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
	/** Empty row to fill upp the space */
	private Row mContentOuterRowFill = null;
	/** Tab button group */
	private ButtonGroup mButtonGroup = new ButtonGroup();
}

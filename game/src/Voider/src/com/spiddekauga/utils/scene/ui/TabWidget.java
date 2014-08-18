package com.spiddekauga.utils.scene.ui;

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
public class TabWidget extends WidgetGroup {
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

		mContentOuterTable.add(mContentInnerTable);
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
		hider.addToggleActor(table);
		hider.setButton(button);
		mContentInnerTable.add(table);
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
	 * Sets the margin (outside) for this TabWidget.
	 * @param top margin at the top
	 * @param right margin to the right
	 * @param bottom margin at the bottom
	 * @param left margin to the left
	 * @return this TabWidget for chaining
	 */
	public TabWidget setMargin(float top, float right, float bottom, float left) {
		mWrapperTable.setMargin(top, right, bottom, left);
		return this;
	}

	/**
	 * Set the margin (outside) for this TabWidget.
	 * @param margin margin to the left, right, top, and bottom
	 * @return this TabWidget for chaining
	 */
	public TabWidget setMargin(float margin) {
		mWrapperTable.setMargin(margin);
		return this;
	}

	/**
	 * @return top margin
	 */
	public float getMarginTop() {
		return mWrapperTable.getMarginTop();
	}

	/**
	 * @return right margin
	 */
	public float getMarginRight() {
		return mWrapperTable.getMarginRight();
	}

	/**
	 * @return bottom margin
	 */
	public float getMarginBottom() {
		return mWrapperTable.getMarginBottom();
	}

	/**
	 * @return left margin
	 */
	public float getMarginLeft() {
		return mWrapperTable.getMarginLeft();
	}

	/**
	 * Sets the padding for the content
	 * @param top padding at the top
	 * @param right padding to the right
	 * @param bottom padding at the bottom
	 * @param left padding to the left
	 * @return this TabWidget for chaining
	 */
	public TabWidget setPaddingContent(float top, float right, float bottom, float left) {
		mContentOuterTable.setPadding(top, right, bottom, left);
		return this;
	}

	/**
	 * Set the padding for the content.
	 * @param padding padding to the left, right, top, and bottom
	 * @return this TabWidget for chaining
	 */
	public TabWidget setPaddingContent(float padding) {
		mContentOuterTable.setPadding(padding);
		return this;
	}

	/**
	 * @return top padding for the content
	 */
	public float getPaddingContentTop() {
		return mContentOuterTable.getPaddingTop();
	}

	/**
	 * @return right padding for the content
	 */
	public float getPaddingContentRight() {
		return mContentOuterTable.getPaddingRight();
	}

	/**
	 * @return bottom padding for the content
	 */
	public float getPaddingContentBottom() {
		return mContentOuterTable.getPaddingBottom();
	}

	/**
	 * @return left padding for the content
	 */
	public float getPaddingContentLeft() {
		return mContentOuterTable.getPaddingLeft();
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

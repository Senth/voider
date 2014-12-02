package com.spiddekauga.utils.scene.ui;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.Layout;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.SnapshotArray;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.voider.utils.Pools;

/**
 * Table that allows for aligning inside the widgets. This table also fixes scaling of the
 * widgets inside.
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class AlignTable extends WidgetGroup implements Disposable, IMargin<AlignTable>, IPadding<AlignTable> {
	/**
	 * Constructor, creates an empty first row
	 */
	public AlignTable() {
		setTouchable(Touchable.childrenOnly);

		try {
			mfWidth = Actor.class.getDeclaredField("width");
			mfWidth.setAccessible(true);
			mfHeight = Actor.class.getDeclaredField("height");
			mfHeight.setAccessible(true);
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Disposes the rows, cells, but saves the actors inside the table
	 * @see #dispose(boolean) if you want the ability to save the actors.
	 */
	@Override
	public void dispose() {
		dispose(false);
	}

	/**
	 * Disposes the rows and cells of the table, the actors can be saved
	 * @param disposeActors true if you want to call dispose() on the actors.
	 */
	public void dispose(boolean disposeActors) {
		mDisposing = true;
		if (mRows != null) {
			for (Row row : mRows) {
				row.dispose(disposeActors);
				Pools.row.free(row);
			}
			mRows.clear();
		}
		mDisposing = false;
	}

	@Override
	public void invalidateHierarchy() {
		super.invalidateHierarchy();

		mValidLayout = false;
		mValidCellSizes = false;
	}

	@Override
	public void invalidate() {
		super.invalidate();

		mValidLayout = false;
		mValidCellSizes = false;
	}

	@Override
	public void validate() {
		if ((getParent() instanceof ScrollPane && !mValidLayout) || needsLayout()) {
			super.validate();
		}
	}

	/**
	 * @return true if the table has a valid layout
	 */
	public boolean isLayoutValid() {
		return mValidLayout;
	}

	/**
	 * @return table alignment
	 */
	public Align getAlignTable() {
		return mTableAlign;
	}

	/**
	 * @return row alignment
	 */
	public Align getRowAlign() {
		return mRowAlign;
	}

	/**
	 * Set the table alignment
	 * @param align
	 * @return this for chaining
	 */
	public AlignTable setAlignTable(Align align) {
		mTableAlign.set(align);
		return this;
	}

	/**
	 * Set row alignment
	 * @param align
	 * @return this for chaining
	 */
	public AlignTable setAlignRow(Align align) {
		mRowAlign.set(align);
		return this;
	}

	/**
	 * Set table and row alignment
	 * @param align
	 * @return this for chaining
	 */
	public AlignTable setAlign(Align align) {
		mTableAlign.set(align);
		mRowAlign.set(align);
		return this;
	}

	/**
	 * Sets the table alignment
	 * @param horizontal horizontal alignment of the table
	 * @param vertical vertical alignment of the table
	 * @return this table for chaining
	 * @see #setAlignRow(Horizontal,Vertical) sets the row alignment instead
	 * @see #setAlign(Horizontal, Vertical) to set both table and row alignment
	 */
	public AlignTable setAlignTable(Horizontal horizontal, Vertical vertical) {
		mTableAlign.horizontal = horizontal;
		mTableAlign.vertical = vertical;
		return this;
	}

	/**
	 * Sets the default row alignment for new rows that are added
	 * @param horizontal default horizontal alignment for new rows
	 * @param vertical default vertical alignment for new rows
	 * @return this table for chaining
	 * @see #setAlignTable(Horizontal,Vertical) sets table alignment instead
	 * @see #setAlign(Horizontal, Vertical) to set both table and row alignment
	 */
	public AlignTable setAlignRow(Horizontal horizontal, Vertical vertical) {
		mRowAlign.horizontal = horizontal;
		mRowAlign.vertical = vertical;
		return this;
	}

	/**
	 * Sets both the table and row alignment. I.e. same as calling
	 * {@link #setAlignRow(Horizontal, Vertical)} and
	 * {@link #setAlignTable(Horizontal, Vertical)}
	 * @param horizontal default horizontal alignment for table and new rows
	 * @param vertical default vertical alignment for table and new rows
	 * @return this tabale for chaining
	 * @see #setAlignRow(Horizontal, Vertical) for only setting row alignment (on new
	 *      rows)
	 * @see #setAlignTable(Horizontal, Vertical) for only setting table alignment
	 */
	public AlignTable setAlign(Horizontal horizontal, Vertical vertical) {
		setAlignRow(horizontal, vertical);
		setAlignTable(horizontal, vertical);
		return this;
	}

	/**
	 * Sets the default padding for all new cells in the table. By default this is 0 for
	 * all sides. This only affects new cells added to the table. To change the padding of
	 * a Cell, you need to retrieve it from the {@link #add(Actor)} method and then call
	 * the appropriate padding method.
	 * @param top padding at the top
	 * @param right padding to the right
	 * @param bottom padding at the bottom
	 * @param left padding to the left
	 * @return this table for chaining
	 */
	public AlignTable setPaddingCellDefault(float top, float right, float bottom, float left) {
		mCellPaddingDefault.top = top;
		mCellPaddingDefault.right = right;
		mCellPaddingDefault.bottom = bottom;
		mCellPaddingDefault.left = left;
		return this;
	}

	/**
	 * Sets the default padding for all new cells in the table. By default this is 0 for
	 * all sides. This only affects new cells added to the table. To change the padding of
	 * a Cell, you need to retrieve it from the {@link #add(Actor)} method and then call
	 * the appropriate padding method.
	 * @param padding padding for all sides
	 * @return this table for chaining
	 */
	public AlignTable setPaddingCellDefault(float padding) {
		mCellPaddingDefault.top = padding;
		mCellPaddingDefault.right = padding;
		mCellPaddingDefault.bottom = padding;
		mCellPaddingDefault.left = padding;
		return this;
	}

	/**
	 * Sets the default padding for all new rows in the table. By default this is 0 for
	 * all sides. Only affects new rows created after this call.
	 * @param top padding at the top
	 * @param right padding to the right
	 * @param bottom padding at the bottom
	 * @param left padding to the left
	 * @return this table for chaining
	 */
	public AlignTable setPaddingRowDefault(float top, float right, float bottom, float left) {
		mRowPaddingDefault.top = top;
		mRowPaddingDefault.right = right;
		mRowPaddingDefault.bottom = bottom;
		mRowPaddingDefault.left = left;
		return this;
	}

	/**
	 * Sets the default padding for all new rows in the table. By default this is 0 for
	 * all sides. Only affects new rows created after this call.
	 * @param padding padding to the left, right, top, and bottom
	 * @return this table for chaining
	 */
	public AlignTable setPaddingRowDefault(float padding) {
		mRowPaddingDefault.top = padding;
		mRowPaddingDefault.right = padding;
		mRowPaddingDefault.bottom = padding;
		mRowPaddingDefault.left = padding;
		return this;
	}

	/**
	 * Sets the margin (outside) for this table.
	 * @param top margin at the top
	 * @param right margin to the right
	 * @param bottom margin at the bottom
	 * @param left margin to the left
	 * @return this table for chaining
	 */
	@Override
	public AlignTable setMargin(float top, float right, float bottom, float left) {
		mMargin.top = top;
		mMargin.right = right;
		mMargin.bottom = bottom;
		mMargin.left = left;
		return this;
	}

	/**
	 * Set the margin (outside) for this table.
	 * @param margin margin to the left, right, top, and bottom
	 * @return this table for chaining
	 */
	@Override
	public AlignTable setMargin(float margin) {
		mMargin.top = margin;
		mMargin.right = margin;
		mMargin.bottom = margin;
		mMargin.left = margin;
		return this;
	}

	@Override
	public AlignTable setMargin(Padding margin) {
		mMargin.set(margin);
		return this;
	}

	/**
	 * Sets the left margin (outside) for the table
	 * @param marginLeft margin to the left
	 * @return this table for chaining
	 */
	@Override
	public AlignTable setMarginLeft(float marginLeft) {
		mMargin.left = marginLeft;
		return this;
	}

	@Override
	public AlignTable setMarginRight(float marginRight) {
		mMargin.right = marginRight;
		return this;
	}

	@Override
	public AlignTable setMarginTop(float marginTop) {
		mMargin.top = marginTop;
		return this;
	}

	@Override
	public AlignTable setMarginBottom(float marginBottom) {
		mMargin.bottom = marginBottom;
		return this;
	}

	@Override
	public AlignTable setPadLeft(float padLeft) {
		mPadding.left = padLeft;

		return this;
	}

	@Override
	public AlignTable setPadRight(float padRight) {
		mPadding.right = padRight;

		return this;
	}

	@Override
	public AlignTable setPadTop(float padTop) {
		mPadding.top = padTop;

		return this;
	}

	@Override
	public AlignTable setPadBottom(float padBottom) {
		mPadding.bottom = padBottom;

		return this;
	}

	@Override
	public float getMarginTop() {
		return mMargin.top;
	}

	@Override
	public float getMarginRight() {
		return mMargin.right;
	}

	@Override
	public float getMarginBottom() {
		return mMargin.bottom;
	}

	@Override
	public float getMarginLeft() {
		return mMargin.left;
	}

	@Override
	public Padding getMargin() {
		return mMargin;
	}

	@Override
	public AlignTable setPad(float top, float right, float bottom, float left) {
		mPadding.top = top;
		mPadding.right = right;
		mPadding.bottom = bottom;
		mPadding.left = left;
		return this;
	}

	@Override
	public AlignTable setPad(float padding) {
		mPadding.top = padding;
		mPadding.right = padding;
		mPadding.bottom = padding;
		mPadding.left = padding;
		return this;
	}

	@Override
	public AlignTable setPad(Padding padding) {
		mPadding.set(padding);
		return this;
	}

	@Override
	public float getPadTop() {
		return mPadding.top;
	}

	@Override
	public float getPadRight() {
		return mPadding.right;
	}

	@Override
	public float getPadBottom() {
		return mPadding.bottom;
	}

	@Override
	public float getPadLeft() {
		return mPadding.left;
	}

	@Override
	public Padding getPad() {
		return mPadding;
	}

	@Override
	public float getPadX() {
		return mPadding.left + mPadding.right;
	}

	@Override
	public float getPadY() {
		return mPadding.top + mPadding.bottom;
	}

	@Override
	public float getMarginX() {
		return mMargin.left + mMargin.right;
	}

	@Override
	public float getMarginY() {
		return mMargin.top + mMargin.bottom;
	}

	/**
	 * Gets all actors in the table
	 * @param onlyVisible set to true to get visible actors, false to get all
	 * @return array with all actors. Don't forget to free the ArrayList after it has been
	 *         used.
	 */
	public ArrayList<Actor> getActors(boolean onlyVisible) {
		ArrayList<Actor> actors = new ArrayList<>();
		getActors(onlyVisible, actors);
		return actors;
	}

	/**
	 * Add all actors to the specified list
	 * @param onlyVisible set to true to get visible actors, false to get all
	 * @param actors list to add the actors to
	 */
	private void getActors(boolean onlyVisible, ArrayList<Actor> actors) {
		for (Row row : mRows) {
			for (Cell cell : row.getCells()) {
				if (!onlyVisible || cell.isVisible()) {
					Actor actor = cell.getActor();
					if (actor instanceof AlignTable) {
						((AlignTable) actor).getActors(onlyVisible, actors);
					} else if (actor != null) {
						if (actor.getClass() == Table.class) {
							SnapshotArray<Actor> tableActors = ((Table) actor).getChildren();
							for (int i = 0; i < tableActors.size; ++i) {
								Actor tableActor = tableActors.get(i);
								if (!onlyVisible || cell.isVisible()) {
									actors.add(tableActor);
								}
							}
						} else {
							actors.add(actor);
						}
					}
				}
			}
		}
	}

	/**
	 * @return number of rows inside the table
	 */
	public int getRowCount() {
		return mRows.size();
	}

	/**
	 * Adds a new actor to the current row.
	 * @param actor new actor to add
	 * @return cell that was added
	 */
	public Cell add(Actor actor) {
		// Add a row if none exists
		if (mRows.isEmpty()) {
			row();
		}

		if (actor instanceof Layout) {
			((Layout) actor).invalidate();
		}

		Cell newCell = Pools.cell.obtain().setActor(actor);
		newCell.setPad(mCellPaddingDefault);

		Row row = mRows.get(mRows.size() - 1);
		row.add(newCell);

		if (actor != null) {
			super.addActor(actor);
		}

		invalidateHierarchy();

		return newCell;
	}

	/**
	 * @deprecated Use {@link #add(Actor)} instead.
	 */
	@Deprecated
	@Override
	public void addActor(Actor actor) {
		throw new UnsupportedOperationException("Use #add(Actor) instead");
	}

	/**
	 * @deprecated Use {@link #add(Actor)} instead.
	 */
	@Deprecated
	@Override
	public void addActorAt(int index, Actor actor) {
		throw new UnsupportedOperationException("Use #add(Actor) instead");
	}

	/**
	 * @deprecated Use {@link #add(Actor)} instead.
	 */
	@Deprecated
	@Override
	public void addActorAfter(Actor actorAfter, Actor actor) {
		throw new UnsupportedOperationException("Use #add(Actor) instead");
	}

	/**
	 * @deprecated Use {@link #add(Actor)} instead.
	 */
	@Deprecated
	@Override
	public void addActorBefore(Actor actorBefore, Actor actor) {
		throw new UnsupportedOperationException("Use #add(Actor) instead");
	}

	/**
	 * Adds an empty cell to the row
	 * @return cell that was added
	 */
	public Cell add() {
		return add(null);
	}

	/**
	 * Adds a number of empty cells to the current row
	 * @param cellCount number of cells to add
	 * @return a list of all cells that were added
	 */
	public ArrayList<Cell> add(int cellCount) {
		ArrayList<Cell> cells = new ArrayList<Cell>();
		for (int i = 0; i < cellCount; ++i) {
			cells.add(add());
		}
		return cells;
	}

	/**
	 * Adds another row to the table. Uses the default row alignment
	 * @see #row(Horizontal,Vertical) for adding a new row with the specified alignment
	 * @see #setAlignRow(Horizontal,Vertical) for setting the default row alignment
	 * @return the created row
	 */
	public Row row() {
		return row(mRowAlign.horizontal, mRowAlign.vertical);
	}

	/**
	 * Adds another row to the table with a specified layout. This does not set the
	 * default row alignment.
	 * @param horizontal horizontal alignment for this new row
	 * @param vertical vertical alignment for this new row
	 * @see #row() for adding a row with the default row alignment
	 * @see #setAlignRow(Horizontal,Vertical) for setting the default row alignment
	 * @return the created row
	 */
	public Row row(Horizontal horizontal, Vertical vertical) {
		Row row = Pools.row.obtain();
		row.setAlign(horizontal, vertical);
		row.setPad(mRowPaddingDefault);
		mRows.add(row);
		return row;
	}

	/**
	 * Adds another row to the table with a specified layout. This does not set the
	 * default row alignment.
	 * @param alignment the alignment
	 * @see #row() for adding a row with the default row alignment
	 * @see #setAlignRow(Horizontal,Vertical) for setting the default row alignment
	 * @return the created row
	 */
	public Row row(Align alignment) {
		Row row = Pools.row.obtain();
		row.setAlign(alignment);
		row.setPad(mRowPaddingDefault);
		mRows.add(row);
		return row;
	}

	/**
	 * Sets the preferences of this table from another table. Such as default cell padding
	 * default row padding, table alignment, default row alignment.
	 * @param preferencesTable the table to get all the preferences for.
	 * @note scalable is not set here.
	 */
	public void setPreferences(AlignTable preferencesTable) {
		mCellPaddingDefault.set(preferencesTable.mCellPaddingDefault);
		mRowPaddingDefault.set(preferencesTable.mRowPaddingDefault);
		mTableAlign.set(preferencesTable.mTableAlign);
		mRowAlign.set(preferencesTable.mRowAlign);
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);

		invalidateHierarchy();
	}


	/**
	 * Set the width of this table silently, i.e. never calls {@link #invalidate()}.
	 * @param width new width of the table
	 */
	void setWidthSilent(float width) {
		if (mfWidth != null) {
			try {
				mfWidth.set(this, width);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
				setWidth(width);
			}
		} else {
			setWidth(width);
		}
	}

	/**
	 * Sets the height of this table silently, i.e. never calls {@link #invalidate()}.
	 * @param height new height of the table
	 */
	void setHeightSilent(float height) {
		if (mfHeight != null) {
			try {
				mfHeight.set(this, height);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
				setWidth(height);
			}
		} else {
			setWidth(height);
		}
	}

	/**
	 * Sets the maximum width
	 * @param width maximum width of the table, set to 0 for infinite width
	 */
	public void setMaxWidth(float width) {
		mMaxWidth = width;
	}

	/**
	 * Sets the maximum height
	 * @param height maximum height of the table, set to 0 for infinite height
	 */
	public void setMaxHeight(float height) {
		mMaxHeight = height;
	}

	/**
	 * @return table width including horizontal margin
	 */
	public float getWidthWithMargin() {
		return getWidth() + mMargin.left + mMargin.right;
	}

	/**
	 * @return table height including vertical margin
	 */
	public float getHeightWithMargin() {
		return getHeight() + mMargin.top + mMargin.bottom;
	}

	@Override
	public float getMinHeight() {
		return mMinHeight;
	}

	@Override
	public float getMinWidth() {
		return mMinWidth;
	}

	@Override
	public float getPrefHeight() {
		if (getParent() instanceof ScrollPane) {
			return mActualHeight;
		} else if (mKeepHeight || !mHasPreferredHeight) {
			return getHeight();
		} else {
			return mPrefHeight;
		}
	}

	@Override
	public float getPrefWidth() {
		if (getParent() instanceof ScrollPane) {
			return mActualWidth;
		} else if (mKeepWidth || !mHasPreferredWidth) {
			return getWidth();
		} else {
			return mPrefWidth;
		}
	}

	/**
	 * Recalculates the preferred width and height
	 */
	void calculatePreferredSize() {
		mPrefHeight = 0;
		mPrefWidth = 0;
		mMinHeight = 0;
		mMinWidth = 0;

		float width = 0;
		float height = 0;

		for (Row row : mRows) {
			if (row.isVisible()) {
				row.calculatePreferredSize();
				mPrefHeight += row.getPrefHeight();
				mMinHeight += row.getMinHeight();
				height += row.getHeight();

				if (row.getPrefWidth() > mPrefWidth) {
					mPrefWidth = row.getPrefWidth();
				}

				if (row.getMinWidth() > mMinWidth) {
					mMinWidth = row.getMinWidth();
				}

				if (row.getWidth() > width) {
					width = row.getWidth();
				}
			}
		}


		// Add padding
		mPrefHeight += getPadY() + getMarginY();
		mPrefWidth += getPadX() + getMarginX();
		mMinHeight += getPadY() + getMarginY();
		mMinWidth += getPadX() + getMarginX();

		// Override with fill parent
		if (mFillParentHeight && getParent() != null) {
			Actor parent = getParent();
			mPrefHeight = parent.getHeight();
			mMinHeight = parent.getHeight();
			height = parent.getHeight();
		}
		if (mFillParentWidth && getParent() != null) {
			Actor parent = getParent();
			mPrefWidth = parent.getWidth();
			mMinWidth = parent.getWidth();
			width = parent.getWidth();
		}


		// Change size of table to fit the content
		if (!mKeepWidth) {
			setWidthSilent(width);
		}
		if (!mKeepHeight) {
			setHeightSilent(height);
		}
	}

	/**
	 * Calculates the actual size after cells have updated their sizes
	 */
	void calculateActualSize() {
		float width = 0;
		float height = 0;

		for (Row row : mRows) {
			if (row.isVisible()) {
				row.calculateActualSize();
				height += row.getHeight();

				if (row.getWidth() > width) {
					width = row.getWidth();
				}
			}
		}

		mActualHeight = height;
		mActualWidth = width;

		// Override with fill parent
		if (mFillParentHeight && getParent() != null) {
			Actor parent = getParent();
			mActualHeight = parent.getHeight();
			height = parent.getHeight();
		}
		if (mFillParentWidth && getParent() != null) {
			Actor parent = getParent();
			mActualWidth = parent.getWidth();
			width = parent.getWidth();
		}

		// Change size of table to fit the content
		if (!mKeepWidth) {
			setWidthSilent(width);
		}
		if (!mKeepHeight) {
			setHeightSilent(height);
		}

		mValidCellSizes = true;
	}

	/**
	 * Set the size of rows and cells that should fill height or width
	 * @param width available width size, if -1 it uses the max row width
	 * @param height available height size, if -1 it uses the available height
	 */
	void updateSize(float width, float height) {
		// Calculate available extra height
		float rowHeightTotal = 0;
		int cRowFillHeight = 0;
		for (Row row : mRows) {
			if (row.isVisible()) {
				rowHeightTotal += row.getHeight();

				if (row.shallFillHeight()) {
					cRowFillHeight++;
				}
			}
		}

		if (height == -1) {
			if (mFillParentHeight && getParent() != null) {
				height = getParent().getHeight();
			} else if (mKeepHeight) {
				height = getHeight();
			} else {
				height = getAvailableHeight();
			}
		} else {
			height -= mMargin.top + mMargin.bottom;
		}

		if (width == -1) {
			if (mFillParentWidth && getParent() != null) {
				width = getParent().getWidth();
			} else if (mKeepWidth) {
				width = getWidth();
			} else {
				width = getAvailableWidth();
			}
		} else {
			width -= mMargin.left + mMargin.right;
		}


		float extraHeightPerFillHeightRow = 0;
		if (cRowFillHeight > 0) {
			extraHeightPerFillHeightRow = (height - mPadding.top - mPadding.bottom - rowHeightTotal) / cRowFillHeight;
		}


		// Update sizes of rows and cells
		for (Row row : mRows) {
			if (row.isVisible()) {
				float newRowWidth = row.getWidth();
				if (row.shallfillWidth()) {
					newRowWidth = width - (mPadding.left + mPadding.right);
				}

				float newRowHeight = row.getHeight();
				if (row.shallFillHeight()) {
					newRowHeight += extraHeightPerFillHeightRow;
				}

				row.updateSize(newRowWidth, newRowHeight);
			}
		}

		if (!mKeepWidth) {
			setWidthSilent(width);
		}
		if (!mKeepHeight && cRowFillHeight > 0) {
			setHeightSilent(height);
		}
	}

	@Override
	public void setSize(float width, float height) {
		if (!(getParent() instanceof ScrollPane)) {
			super.setSize(width, height);
		}
	}

	@Override
	public float getHeight() {
		return super.getHeight() + getPadY();
	}

	@Override
	public float getWidth() {
		return super.getWidth() + getPadX();
	}

	@Override
	public void layout() {
		if (!mValidCellSizes) {
			calculatePreferredSize();
			updateSize(-1, -1);
			calculateActualSize();
		}

		float rowWidthMax = super.getWidth();
		for (Row row : mRows) {
			if (row.isVisible()) {
				if (row.getWidth() > rowWidthMax) {
					rowWidthMax = row.getWidth();
				}
			}
		}

		setPosition();

		// Layout the rows
		Vector2 offset = new Vector2();
		offset.set(mPadding.left, mPadding.top);
		Vector2 availableRowSize = new Vector2();
		availableRowSize.x = rowWidthMax;

		for (int i = mRows.size() - 1; i >= 0; --i) {
			Row row = mRows.get(i);

			if (row.isVisible()) {
				availableRowSize.y = row.getHeight();
				row.layout(offset, availableRowSize);
				offset.y += availableRowSize.y;
			}
		}


		updateBackgroundPosition();
		updateBackgroundSize();

		mValidLayout = true;

		// ScrollPane needs to be layout again after we fixed this layout
		if (getParent() instanceof ScrollPane) {
			((ScrollPane) getParent()).invalidate();
		}
	}

	/**
	 * @return true if the parent sets the position of the table
	 */
	private boolean isParentSettingPosition() {
		Actor parent = getParent();
		return parent instanceof AlignTable || parent instanceof Table || parent instanceof Window;
	}

	@Override
	public void setTransform(boolean transform) {
		for (Row row : mRows) {
			row.setTransform(transform);
		}
	}

	@Override
	public void setPosition(float x, float y) {
		// Skip setting position if the height of this table is
		if (getParent() instanceof ScrollPane) {
			boolean useXParam = false;
			boolean useYParam = false;
			if (mActualHeight >= getParent().getHeight() || mPositionSetManually) {
				useYParam = true;
			}
			if (mActualWidth >= getParent().getWidth() || mPositionSetManually) {
				useXParam = true;
			}

			if (!useXParam || !useYParam) {
				setPosition();
			}

			float xToUse = useXParam ? x : getX();
			float yToUse = useYParam ? y : getY();

			super.setPosition(xToUse, yToUse);
		} else {
			super.setPosition(x, y);
		}
	}

	/**
	 * Calculate the position of this table. Only works if the parent doesn't set the
	 * position or the position is set manually
	 */
	public void setPosition() {
		if (!isParentSettingPosition() && !mPositionSetManually) {
			// Calculate spaces
			int cRowFillHeight = 0;
			boolean rowFillWidth = false;
			for (Row row : mRows) {
				if (row.shallFillHeight()) {
					cRowFillHeight++;
				}
				if (row.shallfillWidth()) {
					rowFillWidth = true;
				}
			}


			// Set position
			Vector2 position = new Vector2();


			// Set custom position if we don't have any table parent
			// Horizontal offset
			// If fill row, the x offset will always be the margin
			if (rowFillWidth && !mKeepWidth) {
				position.x = mMargin.left;
			}
			// Calculate offset depending on alignment
			else {
				if (mTableAlign.horizontal == Horizontal.LEFT) {
					position.x = mMargin.left;
				} else if (mTableAlign.horizontal == Horizontal.RIGHT) {
					position.x = getAvailableWidth() - mActualWidth + mMargin.left;
				} else if (mTableAlign.horizontal == Horizontal.CENTER) {
					position.x = getAvailableWidth() * 0.5f - mActualWidth * 0.5f;
				}
			}

			// Vertical
			// If fill height, the y offset will always be margin bottom
			if (cRowFillHeight > 0 && !mKeepHeight) {
				position.y = mMargin.bottom;
			}
			// Calculate offset depending on alignment
			else {
				if (mTableAlign.vertical == Vertical.BOTTOM) {
					position.y = mMargin.bottom;
				} else if (mTableAlign.vertical == Vertical.TOP) {
					position.y = getAvailableHeight() - mActualHeight + mMargin.bottom;
				} else if (mTableAlign.vertical == Vertical.MIDDLE) {
					position.y = getAvailableHeight() * 0.5f - mActualHeight * 0.5f;
				}
			}

			super.setPosition((int) position.x, (int) position.y);
		}
	}

	/**
	 * Sets the position of this actor
	 * @param position new position of this table
	 * @return this for chaining
	 */
	public AlignTable setPosition(Vector2 position) {
		setPosition(position.x, position.y);
		return this;
	}

	/**
	 * @return available width of the table
	 */
	public float getAvailableWidth() {
		float availableWidth = 0;
		if (mMaxWidth > 0) {
			return mMaxWidth - (mPadding.left + mPadding.right);
		} else if ((getParent() == null || getParent().getParent() == null) && getStage() != null) {
			availableWidth = getStage().getWidth();
		} else if (getParent() instanceof TabWidget) {
			availableWidth = getStage().getWidth();
		} else if (getParent() instanceof ScrollPane) {
			availableWidth = getParent().getWidth();
		} else {
			availableWidth = getWidth();
		}
		availableWidth -= (mMargin.left + mMargin.right);
		availableWidth -= (mPadding.left + mPadding.right);
		return availableWidth;
	}

	/**
	 * @return available height of the table
	 */
	public float getAvailableHeight() {
		float availableHeight = 0;
		if (mMaxHeight > 0) {
			return mMaxHeight - (mPadding.top + mPadding.bottom);
		} else if ((getParent() == null || getParent().getParent() == null) && getStage() != null) {
			availableHeight = getStage().getHeight();
		} else if (getParent() instanceof TabWidget) {
			availableHeight = getStage().getHeight();
		} else if (getParent() instanceof ScrollPane) {
			availableHeight = ((ScrollPane) getParent()).getHeight();
		} else {
			availableHeight = getHeight();
		}
		availableHeight -= (mMargin.top + mMargin.bottom);
		availableHeight -= (mPadding.top + mPadding.bottom);
		return availableHeight;
	}

	/**
	 * Makes the table keep its size (that was set through {@link #setSize(float, float)},
	 * {@link #setWidth(float)}, or {@link #setHeight(float)}) after the table has been
	 * invalidated. I.e. it will not resize the table to fit the contents.
	 * @param keepSize set to true to keep the size of the table. If set to false table
	 *        will not always be the same size as getPrefWidth/Height.
	 * @return this table for chaining
	 * @see #setKeepWidth(boolean)
	 * @see #setKeepHeight(boolean)
	 */
	public AlignTable setKeepSize(boolean keepSize) {
		setKeepWidth(keepSize);
		setKeepHeight(keepSize);
		return this;
	}

	/**
	 * Makes the table keep its width (that was set through {@link #setSize(float, float)}
	 * or {@link #setWidth(float)} after the table has been invalidated. I.e. it will not
	 * resize the table to fit the contents.
	 * @param keepWidth set to true to keep the width of the table. If set to false table
	 *        will not always be the same size as getPrefWidth().
	 * @return this table for chaining
	 */
	public AlignTable setKeepWidth(boolean keepWidth) {
		mKeepWidth = keepWidth;
		return this;
	}

	/**
	 * Makes the table keep its height (that was set through
	 * {@link #setSize(float, float)} or {@link #setHeight(float)} after the table has
	 * been invalidated. I.e. it will not resize the table to fit the contents.
	 * @param keepHeight set to true to keep the height of the table. If set to false
	 *        table will not always be the same size as getPrefHeight().
	 * @return this table for chaining
	 */
	public AlignTable setKeepHeight(boolean keepHeight) {
		mKeepHeight = keepHeight;
		return this;
	}

	/**
	 * Makes this table's {@link #getPrefWidth()} call {@link #getWidth()} if
	 * hasPreferredWidth is set to false. Useful when this table is inside a ScrollPane.
	 * @param hasPreferredWidth if set to false {@link #getPrefWidth()} will return
	 *        {@link #getWidth()} instead.
	 * @return this table for chaining
	 */
	public AlignTable setHasPreferredWidth(boolean hasPreferredWidth) {
		mHasPreferredWidth = hasPreferredWidth;
		return this;
	}

	/**
	 * Makes this table's {@link #getPrefHeight()} call {@link #getHeight()} if
	 * hasPreferredHeight is set to false. Useful when this table is inside a ScrollPane.
	 * @param hasPreferredHeight if set to false {@link #getPrefHeight()} will return
	 *        {@link #getHeight()} instead.
	 * @return this table for chaining
	 */
	public AlignTable setHasPreferredHeight(boolean hasPreferredHeight) {
		mHasPreferredHeight = hasPreferredHeight;
		return this;
	}

	@Override
	public boolean removeActor(Actor actor) {
		boolean removed = super.removeActor(actor);

		// Remove the cell the actor was in (not if we're disposing)
		if (removed && !mDisposing) {
			Iterator<Row> rowIt = mRows.iterator();
			while (rowIt.hasNext()) {
				Row row = rowIt.next();

				row.removeActor(actor);

				if (row.getCellCount() == 0) {
					rowIt.remove();
					Pools.row.free(row);
				}
			}

			invalidate();
		}

		return removed;
	}

	/**
	 * Removes a whole row including its actors
	 * @param row the row to remove
	 * @param disposeActors true if we shall call dispose on the cell's actors
	 */
	public void removeRow(Row row, boolean disposeActors) {
		mRows.remove(row);
		row.dispose(disposeActors);
		Pools.row.free(row);
	}

	/**
	 * Get all rows. USE WITH CAUTION, never add anything to the row outside of AlignTable
	 * @return all rows
	 */
	public ArrayList<Row> getRows() {
		return mRows;
	}

	/**
	 * @return get last added row, null if no row has been added
	 */
	public Row getRow() {
		if (!mRows.isEmpty()) {
			return mRows.get(mRows.size() - 1);
		}
		return null;
	}

	/**
	 * @return last added cell, null if no cell has been added in the last row
	 */
	public Cell getCell() {
		return getRow().getCell();
	}

	/**
	 * Sets if the table's position should be update manually/externally (will ignore
	 * table alignment and margins).
	 * @param setPositionManually set to true to set this table's position manually
	 * @return this for chaining
	 */
	public AlignTable setPositionManually(boolean setPositionManually) {
		mPositionSetManually = setPositionManually;
		return this;
	}

	/**
	 * @return true if this table's position is set manually/externally
	 */
	public boolean isPositionSetManually() {
		return mPositionSetManually;
	}

	/**
	 * @return true if this table doesn't contain any rows and thus no cells
	 */
	public boolean isEmpty() {
		return getRowCount() == 0;
	}

	/**
	 * @return true if any cell inside this table is visible. This includes empty cells
	 */
	public boolean hasVisibleCells() {
		for (Row row : mRows) {
			if (row.isVisible()) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Set a background image of the table
	 * @param image background image of this table, null to remove an existing background
	 *        image
	 */
	public void setBackgroundImage(Image image) {
		// Remove old background
		if (mBackground != null) {
			removeActor(mBackground);
		}

		mBackground = image;

		if (mBackground != null) {
			updateBackgroundSize();
			super.addActorAt(0, mBackground);
		}
	}

	/**
	 * Updates the background position
	 */
	private void updateBackgroundPosition() {
		if (mBackground != null) {
			// mBackground.setPosition(-mPadding.left, -mPadding.bottom);
		}
	}

	/**
	 * Updates the background size
	 */
	private void updateBackgroundSize() {
		if (mBackground != null) {
			mBackground.setSize(getWidth(), getHeight());
		}
	}

	/**
	 * Fills the width of the parent's width. Usually this is done by filling rows and
	 * cells in AlignTable if the parent of the AlignTable is something else (e.g.
	 * ScrollPane) this allows the table to fill the width of that ScrollPane.
	 * @param fillWidth true to fill the width of the parent actor
	 */
	public void setFillParentWidth(boolean fillWidth) {
		mFillParentWidth = fillWidth;
		invalidate();
	}

	/**
	 * Fills the height of the parent's height. Usually this is done by filling rows and
	 * cells in AlignTable if the parent of the AlignTable is something else (e.g.
	 * ScrollPane) this allows the table to fill the height of that ScrollPane.
	 * @param fillHeight true to fill the height of the parent actor
	 */
	public void setFillParentHeight(boolean fillHeight) {
		mFillParentHeight = fillHeight;
		invalidate();
	}

	/**
	 * Fills the width and height of the parent's width and height. Usually this is done
	 * by filling rows and cells in AlignTable if the parent of the AlignTable is
	 * something else (e.g. Stage) this allows the table to fill the width and height of
	 * that Stage
	 * @param fillParent true to fill the both the width and the height of the parent
	 *        actor
	 */
	@Override
	public void setFillParent(boolean fillParent) {
		setFillParentHeight(fillParent);
		setFillParentWidth(fillParent);
	}

	/** Background image */
	private Image mBackground = null;
	/** Valid cell sizes */
	private boolean mValidCellSizes = true;
	/** Layout is valid, false if the table needs to call layout() */
	private boolean mValidLayout = true;
	/** All the rows of the table */
	private ArrayList<Row> mRows = new ArrayList<Row>();
	/** Table alignment (not cell alignment) */
	private Align mTableAlign = new Align();
	/** Standard row alignment */
	private Align mRowAlign = new Align();
	/** Preferred width of the actors, this is set to the row with highest preferred width */
	private float mPrefWidth = 0;
	/** Preferred height of the table, adds all rows preferred width */
	private float mPrefHeight = 0;
	/** Minimum width, equals all non-scalable cells' width */
	private float mMinWidth = 0;
	/** Minimum height, equals all non-scalable cells' height */
	private float mMinHeight = 0;
	/** Actual height of table */
	private float mActualHeight = 0;
	/** Actual width of table */
	private float mActualWidth = 0;
	/** Maximum height */
	private float mMaxHeight = 0;
	/** Maximum width */
	private float mMaxWidth = 0;
	/**
	 * True if the table shall keep it's width after layout, false if it shall resize
	 * itself
	 */
	private boolean mKeepWidth = false;
	/**
	 * True if the table shall keep it's height after layout, false if it shall resize
	 * itself
	 */
	private boolean mKeepHeight = false;
	/** If we're currently disposing */
	private boolean mDisposing = false;
	/** If this table has a preferred height */
	private boolean mHasPreferredHeight = true;
	/** If this table has a preferred width */
	private boolean mHasPreferredWidth = true;
	/** If this table's position is set manually */
	private boolean mPositionSetManually = false;
	/** Default cell padding */
	private Padding mCellPaddingDefault = new Padding();
	/** Default row padding */
	private Padding mRowPaddingDefault = new Padding();
	/** Outside margin for this table */
	private Padding mMargin = new Padding();
	/** Inside margin for this table */
	private Padding mPadding = new Padding();
	/** Fill parent height */
	private boolean mFillParentHeight = false;
	/** Fill parent width */
	private boolean mFillParentWidth = false;
	/** Private accessor to width */
	private Field mfWidth = null;
	/** Private accessor to height */
	private Field mfHeight = null;

}

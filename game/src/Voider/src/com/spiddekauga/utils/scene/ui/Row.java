package com.spiddekauga.utils.scene.ui;

import java.util.ArrayList;
import java.util.Iterator;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.voider.utils.Pools;

/**
 * Wrapper for a row
 * Contains all the actors for the current row
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class Row implements Poolable {
	/**
	 * Default constructor
	 */
	public Row() {
	}

	/**
	 * Disposes all cells but can save the actors inside the cells.
	 * @param disposeActor true if you want to call dispose() on the actors
	 */
	public void dispose(boolean disposeActor) {
		if (mCells != null) {
			for (Cell cell : mCells) {
				cell.dispose(disposeActor);
				Pools.cell.free(cell);
			}
			Pools.arrayList.free(mCells);
			mCells = null;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void reset() {
		mPrefHeight = 0;
		mPrefWidth = 0;
		mMinHeight = 0;
		mMinWidth = 0;
		mWidth = 0;
		mHeight = 0;

		mEqualSize = false;
		mUseCellAlign = false;

		if (mCells != null) {
			for (Cell cell : mCells) {
				cell.dispose(true);
				Pools.cell.free(cell);
			}
			mCells.clear();
		} else {
			mCells = Pools.arrayList.obtain();
		}

		mAlign.horizontal = Horizontal.LEFT;
		mAlign.vertical = Vertical.MIDDLE;

		mPadding.reset();

		mFillHeight = false;
		mFillWidth = false;
	}

	/**
	 * Sets the cells to equal size
	 * @param equalSize set to true to use equal spacing
	 * @param useCellAlign set this to true if you want to use the cell's alignment
	 * instead of the row's alignment. With this you can accomplish a layout like this:
	 * \code
	 * |————————————————————————————————————|
	 * |Left       |      Right|   Center   |
	 * |————————————————————————————————————|
	 * \endcode
	 * Only applicable if equalSpacing is set to true
	 * @return This row for chaining
	 */
	public Row setEqualCellSize(boolean equalSize, boolean useCellAlign) {
		mEqualSize = equalSize;
		mUseCellAlign = useCellAlign;

		// Check if preferred width needs updating
		if (mEqualSize) {
			float maxPrefWidth = 0;
			for (Cell cell : mCells) {
				maxPrefWidth = cell.getPrefWidth() * mCells.size();

				if (maxPrefWidth > mPrefWidth) {
					mPrefWidth = maxPrefWidth;
					mWidth = cell.getWidth() * mCells.size();
				}
			}
		}

		return this;
	}

	/**
	 * Sets the cells to equal size. Uses the row alignment.
	 * @param equalSize set to true to use equal spacing
	 * @return This row for chaining
	 * @see #setEqualCellSize(boolean,boolean) for using cell alignment instead
	 */
	public Row setEqualCellSize(boolean equalSize) {
		setEqualCellSize(equalSize, false);
		return this;
	}

	/**
	 * Sets the alignment of this row
	 * @param horizontal the horizontal alignment
	 * @param vertical the vertical alignment
	 * @return this row for chaining
	 */
	public Row setAlign(Horizontal horizontal, Vertical vertical) {
		mAlign.horizontal = horizontal;
		mAlign.vertical = vertical;
		return this;
	}

	/**
	 * Sets the padding for left, right, top, bottom
	 * @param pad how much padding should be on the sides
	 * @return this row for chaining
	 */
	public Row setPadding(float pad) {
		setPadLeft(pad);
		setPadRight(pad);
		setPadTop(pad);
		setPadBottom(pad);

		return this;
	}

	/**
	 * Sets the padding for top, right, bottom, and left.
	 * @param top top padding
	 * @param right right padding
	 * @param bottom bottom padding
	 * @param left left padding
	 * @return this cell for chaining
	 */
	public Row setPadding(float top, float right, float bottom, float left) {
		setPadLeft(left);
		setPadRight(right);
		setPadTop(top);
		setPadBottom(bottom);
		return this;
	}

	/**
	 * Sets the padding of the row
	 * @param padding padding information
	 * @return this cell for chaining
	 */
	Row setPadding(Padding padding) {
		setPadLeft(padding.left);
		setPadRight(padding.right);
		setPadTop(padding.top);
		setPadBottom(padding.bottom);
		return this;
	}

	/**
	 * Sets the padding to the left of the row
	 * @param padLeft how much padding should be to the left of the row
	 * @return this row for chaining
	 */
	public Row setPadLeft(float padLeft) {
		mPadding.left = padLeft;

		return this;
	}

	/**
	 * Sets the padding to the right of the row
	 * @param padRight how much padding should be on the right of the row
	 * @return this row for chaining
	 */
	public Row setPadRight(float padRight) {
		mPadding.right = padRight;

		return this;
	}

	/**
	 * Sets the padding at the top of the row
	 * @param padTop how much padding should be at the top of the row
	 * @return this row for chaining
	 */
	public Row setPadTop(float padTop) {
		mPadding.top = padTop;

		return this;
	}

	/**
	 * Sets the padding at teh bottom of the row
	 * @param padBottom how much padding should be at the bottom of the row
	 * @return this row for chaining
	 */
	public Row setPadBottom(float padBottom) {
		mPadding.bottom = padBottom;

		return this;
	}

	/**
	 * @return padding to the left of this row. If dynamic padding is on this
	 * will return the scaled padding instead.
	 */
	public float getPadLeft() {
		return mPadding.left;
	}

	/**
	 * @return padding to the right of this row. If dynamic padding is on this
	 * will return the scaled padding instead.
	 */
	public float getPadRight() {
		return mPadding.right;
	}

	/**
	 * @return padding to the top of this row. If dynamic padding is on this
	 * will return the scaled padding instead.
	 */
	public float getPadTop() {
		return mPadding.top;
	}

	/**
	 * @return padding to the bottom of this row. If dynamic padding is on this
	 * will return the scaled padding instead.
	 */
	public float getPadBottom() {
		return mPadding.bottom;
	}

	/**
	 * @return minimum height of the row. Equals the non-scalable cells' height
	 */
	public float getMinHeight() {
		return mMinHeight;
	}

	/**
	 * @return minimum width of the row. Equals tho non-scalable cells' width
	 */
	public float getMinWidth() {
		return mMinWidth;
	}

	/**
	 * Sets if the row shall fill the remaining width of the table.
	 * This works for all rows in the table.
	 * @param fillWidth set to true if the row shall fill the remaining width of the table
	 * @return this row for chaining
	 */
	public Row setFillWidth(boolean fillWidth) {
		mFillWidth = true;
		return this;
	}

	/**
	 * @return true if the row shall fill the remaining width of the table
	 */
	public boolean shallfillWidth() {
		return mFillWidth;
	}

	/**
	 * Sets if the row shall fill the remaining height of the table.
	 * @todo fill width for row only works on one row per table.
	 * @param fillHeight true if the row shall fill the remaining height of the table.
	 * @return this row for chaining
	 */
	public Row setFillHeight(boolean fillHeight) {
		mFillHeight = fillHeight;
		return this;
	}

	/**
	 * @return true if the row shall fill the height of the table
	 */
	public boolean shallFillHeight() {
		return mFillHeight;
	}

	/**
	 * @return preferred height of the row
	 */
	float getPrefHeight() {
		return mPrefHeight + mPadding.top + mPadding.bottom;
	}

	/**
	 * @return preferred width of the row
	 */
	float getPrefWidth() {
		return mPrefWidth + mPadding.left + mPadding.right;
	}

	/**
	 * @return width of the row
	 */
	float getWidth() {
		return mWidth + getPadLeft() + getPadRight();
	}

	/**
	 * @return height of the row
	 */
	float getHeight() {
		return mHeight + getPadTop() + getPadBottom();
	}

	/**
	 * Removes the specified actor from all cells that matches the actor
	 * @param actor the actor to remove
	 */
	void removeActor(Actor actor) {
		Iterator<Cell> cellIt = mCells.iterator();
		while (cellIt.hasNext()) {
			Cell cell = cellIt.next();

			if (cell.containsActor(actor)) {
				cell.setActor(null);
				cellIt.remove();
				Pools.cell.free(cell);
			}
		}
	}

	/**
	 * @return number of cells in the row
	 */
	int getCellCount() {
		return mCells.size();
	}

	/**
	 * @return all cells
	 */
	ArrayList<Cell> getCells() {
		return mCells;
	}

	/**
	 * Call this to layout the row.
	 * @param startPos starting position of the row
	 * @param size available size for this row
	 */
	void layout(Vector2 startPos, Vector2 size) {
		Vector2 offset = Pools.vector2.obtain();
		offset.set(startPos);
		offset.x += getPadLeft();

		if (mFillWidth) {
			mWidth = size.x - getPadLeft() - getPadRight();
		}
		if (mFillHeight) {
			mHeight = size.y - getPadTop() - getPadBottom();
		}

		// Check if there's a cell that wants to fill the width
		boolean cellFillWidth = false;
		for (Cell cell : mCells) {
			if (cell.isVisible() && cell.shallfillWidth()) {
				cellFillWidth = true;
				break;
			}
		}

		// Horizontal
		if (!cellFillWidth) {
			if (mAlign.horizontal == Horizontal.RIGHT) {
				offset.x += size.x - getWidth();
			} else if (mAlign.horizontal == Horizontal.CENTER) {
				offset.x += size.x * 0.5f - getWidth() * 0.5f;
			}
		}

		// Vertical
		if (mAlign.vertical == Vertical.BOTTOM) {
			offset.y = startPos.y + getPadBottom();
		} else if (mAlign.vertical == Vertical.TOP) {
			offset.y = startPos.y + size.y - mHeight - getPadTop();
		} else if (mAlign.vertical == Vertical.MIDDLE) {
			offset.y = startPos.y + (size.y - mHeight + getPadBottom() - getPadTop()) * 0.5f;
		}

		Vector2 cellSize = Pools.vector2.obtain();
		if (mEqualSize) {
			cellSize.y = mHeight;
			cellSize.x = mWidth / mCells.size();

			for (Cell cell : mCells) {
				cell.layout(offset, cellSize);
				offset.x += cellSize.x;
			}
		} else {
			cellSize.y = mHeight;
			for (Cell cell : mCells) {
				if (cell.isVisible()) {

					// If this cell shall fill the width, add extra width to the cell
					if (cell.shallfillWidth()) {
						float fillWidth = size.x - getPrefWidth();
						cellSize.x = cell.getPrefWidth() + fillWidth;
					} else {
						cellSize.x = cell.getWidth();
					}

					cell.layout(offset, cellSize);
					offset.x += cellSize.x;
				}
			}

		}
		Pools.vector2.free(cellSize);
		Pools.vector2.free(offset);
	}

	/**
	 * Add a cell to the row. Sets the alignment for the actor
	 * @param cell new cell with actor to append
	 */
	void add(Cell cell) {
		mCells.add(cell);
		cell.setAlign(mAlign.horizontal, mAlign.vertical);
	}

	/**
	 * Recalculates the preferred width and preferred height
	 */
	void calculateSize() {
		mPrefHeight = 0;
		mPrefWidth = 0;
		mMinHeight = 0;
		mMinWidth = 0;
		mWidth = 0;
		mHeight = 0;

		for (Cell cell : mCells) {
			if (cell.isVisible()) {
				cell.calculateSize();
				addSize(cell);
			}
		}
	}

	/**
	 * Sets transform for all cells in this row
	 * @param transform true if the all cells in this row shall be able to transform
	 * @return this row for chaining
	 */
	Row setTransform(boolean transform) {
		for (Cell cell : mCells) {
			cell.setTransform(transform);
		}
		return this;
	}

	/**
	 * Adds the width/height, preferred width/height to the total count of
	 * this rows size.
	 * @param cell the cell which width/height we want to add
	 */
	private void addSize(Cell cell) {
		// Get pref width/height
		if (mEqualSize) {
			float maxSize = cell.getPrefWidth() * mCells.size();
			if (maxSize > mPrefWidth) {
				mPrefWidth = maxSize;
				mWidth = cell.getWidth() * mCells.size();
			}

			float minWidth = cell.getPrefWidth() * mCells.size();
			if (minWidth > mMinWidth) {
				mMinWidth = minWidth;
			}
		} else {
			mPrefWidth += cell.getPrefWidth();
			mWidth += cell.getWidth();

			mMinWidth += cell.getPrefWidth() > cell.getWidth() ? cell.getPrefWidth() : cell.getWidth();
		}

		if (cell.getPrefHeight() > mPrefHeight) {
			mPrefHeight = cell.getPrefHeight();
		}
		float minHeight = cell.getPrefHeight() > cell.getHeight() ? cell.getPrefHeight() : cell.getHeight();
		if (minHeight > mMinHeight) {
			mMinHeight = minHeight;
		}
		if (cell.getHeight() > mHeight) {
			mHeight = cell.getHeight();
		}
	}

	/** True if the row uses the full width of the parents getPrefWidth() and sets the cell's size
	 * to equal */
	private boolean mEqualSize = false;
	/** If we shall use cells alignment instead of the row's, only applicable if
	 * #mEqualSpacing is true */
	private boolean mUseCellAlign = false;
	/** All the columns in the table */
	@SuppressWarnings("unchecked")
	private ArrayList<Cell> mCells = Pools.arrayList.obtain();
	/** Total preferred width of the actors in this row */
	private float mPrefWidth = 0;
	/** Preferred height of the actors in this row, this is set to the actor
	 * with most preferred height. */
	private float mPrefHeight = 0;
	/** Width of the row, calculated from the cells */
	private float mWidth = 0;
	/** Height of the row, calculated from the cells */
	private float mHeight = 0;
	/** Minimum width, equals all non-scalable cells' width */
	private float mMinWidth = 0;
	/** Minimum height, equals all non-scalable cells' height */
	private float mMinHeight = 0;
	/** Row alignment */
	private Align mAlign = new Align(Horizontal.LEFT, Vertical.MIDDLE);
	/** If the row shall fill the width of the table */
	private boolean mFillWidth = false;
	/** If the row shall fill the height of the table */
	private boolean mFillHeight = false;

	// Padding
	/** Padding for this row */
	private Padding mPadding = new Padding();
}

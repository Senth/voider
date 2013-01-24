package com.spiddekauga.utils.scene.ui;

import java.util.ArrayList;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.utils.Layout;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.badlogic.gdx.utils.Pools;

/**
 * Wrapper for a row
 * Contains all the actors for the current row
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class Row implements Poolable {
	@Override
	public void reset() {
		mPrefHeight = 0;
		mPrefWidth = 0;

		for (Cell cell : mCells) {
			Pools.free(cell);
		}
		mCells.clear();
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
	public Row setEqualSpacing(boolean equalSize) {
		setEqualCellSize(equalSize, false);
		return this;
	}

	/**
	 * Sets the alignment for the row. This automatically disables use of cell alignment
	 * if it has been turned on.
	 * @param align how the row shall be aligned
	 * @return this row for chaining
	 * @see Align for alignment variables
	 */
	public Row setAlign(int align) {
		mAlign = align;
		mUseCellAlign = false;
		return this;
	}

	/**
	 * @return preferred height of the row
	 */
	float getPrefHeight() {
		return mPrefHeight;
	}

	/**
	 * @return preferred width of the row
	 */
	float getPrefWidth() {
		return mPrefWidth;
	}

	/**
	 * @return width of the row
	 */
	float getWidth() {
		return mWidth;
	}

	/**
	 * @return height of the row
	 */
	float getHeight() {
		return mHeight;
	}

	/**
	 * Call this to layout the row.
	 * @param startPos starting position of the row
	 * @param size available size for this row
	 */
	void layout(Vector2 startPos, Vector2 size) {
		Vector2 offset = Pools.obtain(Vector2.class);
		offset.set(startPos);

		if (mEqualSize) {
			/** @TODO implement equal size */
		} else {
			// Horizontal
			if ((mAlign & Align.RIGHT) > 0) {
				offset.x += size.x - getWidth();
			} else if ((mAlign & Align.CENTER) > 0) {
				offset.x += size.x * 0.5f + getWidth() * 0.5f;
			}

			float halfSizeY = size.y * 0.5f;
			for (Cell cell : mCells) {
				// Vertical Align
				if ((mAlign & Align.BOTTOM) > 0) {
					offset.y = startPos.y;
				} else if ((mAlign & Align.TOP) > 0) {
					offset.y = startPos.y + size.y - cell.getHeight();
				} else if ((mAlign & Align.MIDDLE) > 0) {
					offset.y = startPos.y + halfSizeY - cell.getHeight() * 0.5f;
				}

				cell.mActor.setPosition(offset.x, offset.y);

				offset.x += cell.getWidth();
			}
		}

		Pools.free(offset);
	}

	/**
	 * Add a cell to the row
	 * @param cell new cell with actor to append
	 */
	void add(Cell cell) {
		mCells.add(cell);

		// Get pref width/height
		if (mEqualSize) {
			float maxSize = cell.getPrefWidth() * mCells.size();
			if (maxSize > mPrefWidth) {
				mPrefWidth = maxSize;
				mWidth = cell.getWidth() * mCells.size();
			}
		} else {
			mPrefWidth += cell.getPrefWidth();
			mWidth += cell.getWidth();
		}

		if (cell.getPrefHeight() > mPrefHeight) {
			mPrefHeight = cell.getPrefHeight();
			mHeight = cell.getHeight();
		}
	}

	/**
	 * Recalculates the preferred width and preferred height
	 */
	void calculateSize() {
		mPrefHeight = 0;
		mPrefWidth = 0;

		for (Cell cell : mCells) {
			if (cell.mActor  instanceof Layout) {
				Layout layout = (Layout)cell.mActor;
				mPrefWidth += layout.getPrefWidth();
				if (layout.getPrefHeight() > mPrefHeight) {
					mPrefHeight = layout.getPrefHeight();
				}
			}
		}
	}

	/**
	 * Sets transform for all cells in this actor
	 */
	void setTransform(boolean transform) {
		for (Cell cell : mCells) {
			if (cell.mActor instanceof Group) {
				((Group) cell.mActor).setTransform(transform);
			}
		}
	}

	/** True if the row uses the full width of the parents getPrefWidth() and sets the cell's size
	 * to equal */
	private boolean mEqualSize = false;
	/** If we shall use cells alignment instead of the row's, only applicable if
	 * #mEqualSpacing is true */
	private boolean mUseCellAlign = false;
	/** Row alignment used */
	private int mAlign = Align.LEFT | Align.MIDDLE;
	/** All the columns in the table */
	private ArrayList<Cell> mCells = new ArrayList<Cell>();
	/** Total preferred width of the actors in this row */
	private float mPrefWidth = 0;
	/** Preferred height of the actors in this row, this is set to the actor
	 * with most preferred height. */
	private float mPrefHeight = 0;
	/** Width of the row, calculated from the cells */
	private float mWidth = 0;
	/** Height of the row, calculated from the cells */
	private float mHeight = 0;
}

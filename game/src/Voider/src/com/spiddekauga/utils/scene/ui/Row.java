package com.spiddekauga.utils.scene.ui;

import java.util.ArrayList;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.badlogic.gdx.utils.Pools;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;

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
		mMinHeight = 0;
		mMinWidth = 0;
		mWidth = 0;
		mHeight = 0;
		mScaleX = 1;
		mScaleY = 1;

		mEqualSize = false;
		mUseCellAlign = false;

		for (Cell cell : mCells) {
			Pools.free(cell);
		}
		mCells.clear();

		mAlign.horizontal = Horizontal.LEFT;
		mAlign.vertical = Vertical.MIDDLE;

		mPadding.reset();
		mScaledPadding.reset();
		mDynamicPadding = true;

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
	 * Sets if the row (and its children) can be scaled or not. Rows are scalable by default.
	 * Can be good to turn off for buttons with text.
	 * @param scalable true if the cell should be scalable. If set to false this
	 * will reset any scale.
	 * @return this row for chaining
	 */
	public Row setScalable(boolean scalable) {

		if (!scalable) {
			setScaleX(1);
			setScaleY(1);
		}

		mScalable = scalable;

		return this;
	}

	/**
	 * Sets the padding values to be dynamic. I.e. they will scale depending on the
	 * current scale factor of the table. E.g. setPadLeft(10) and table.setScale(0.5f)
	 * will result in setPadLeft(5).
	 * @param dynamicPadding set to true to activate dynamic padding
	 * @return this row for chaining
	 * @note Setting this to false does not work with scaling AlignTable
	 * depending on the size.
	 */
	public Row setDynamicPadding(boolean dynamicPadding) {
		mDynamicPadding = dynamicPadding;

		if (mDynamicPadding) {
			mScaledPadding.left = mPadding.left * mScaleX;
			mScaledPadding.right = mPadding.right * mScaleX;
			mScaledPadding.top = mPadding.top * mScaleY;
			mScaledPadding.bottom = mPadding.top * mScaleY;
		}

		return this;
	}

	/**
	 * Sets the padding for left, right, top, bottom
	 * @param pad how much padding should be on the sides
	 * @return this row for chaining
	 * @see #setDynamicPadding(boolean)
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
	 * @see #setDynamicPadding(boolean)
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
	 * @see #setDynamicPadding(boolean)
	 */
	public Row setPadLeft(float padLeft) {
		mPadding.left = padLeft;

		if (mDynamicPadding) {
			mScaledPadding.left = mPadding.left * mScaleX;
		}

		return this;
	}

	/**
	 * Sets the padding to the right of the row
	 * @param padRight how much padding should be on the right of the row
	 * @return this row for chaining
	 * @see #setDynamicPadding(boolean)
	 */
	public Row setPadRight(float padRight) {
		mPadding.right = padRight;

		if (mDynamicPadding) {
			mScaledPadding.right = mPadding.right * mScaleX;
		}

		return this;
	}

	/**
	 * Sets the padding at the top of the row
	 * @param padTop how much padding should be at the top of the row
	 * @return this row for chaining
	 * @see #setDynamicPadding(boolean)
	 */
	public Row setPadTop(float padTop) {
		mPadding.top = padTop;

		if (mDynamicPadding) {
			mScaledPadding.top = mPadding.top * mScaleY;
		}

		return this;
	}

	/**
	 * Sets the padding at teh bottom of the row
	 * @param padBottom how much padding should be at the bottom of the row
	 * @return this row for chaining
	 * @see #setDynamicPadding(boolean)
	 */
	public Row setPadBottom(float padBottom) {
		mPadding.bottom = padBottom;

		if (mDynamicPadding) {
			mScaledPadding.bottom = mPadding.bottom * mScaleY;
		}

		return this;
	}

	/**
	 * @return padding to the left of this row. If dynamic padding is on this
	 * will return the scaled padding instead.
	 * @see #setDynamicPadding(boolean)
	 */
	public float getPadLeft() {
		return mDynamicPadding ? mScaledPadding.left : mPadding.left;
	}

	/**
	 * @return padding to the right of this row. If dynamic padding is on this
	 * will return the scaled padding instead.
	 * @see #setDynamicPadding(boolean)
	 */
	public float getPadRight() {
		return mDynamicPadding ? mScaledPadding.right : mPadding.right;
	}

	/**
	 * @return padding to the top of this row. If dynamic padding is on this
	 * will return the scaled padding instead.
	 * @see #setDynamicPadding(boolean)
	 */
	public float getPadTop() {
		return mDynamicPadding ? mScaledPadding.top : mPadding.top;
	}

	/**
	 * @return padding to the bottom of this row. If dynamic padding is on this
	 * will return the scaled padding instead.
	 * @see #setDynamicPadding(boolean)
	 */
	public float getPadBottom() {
		return mDynamicPadding ? mScaledPadding.bottom : mPadding.bottom;
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
	 * Call this to layout the row.
	 * @param startPos starting position of the row
	 * @param size available size for this row
	 */
	void layout(Vector2 startPos, Vector2 size) {
		Vector2 offset = Pools.obtain(Vector2.class);
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
			if (cell.shallfillWidth()) {
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

		Vector2 cellSize = Pools.obtain(Vector2.class);
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
					cellSize.x = cell.getWidth();

					if (cellSize.x == 0) {
						cellSize.x = cell.getPrefWidth();
					}

					// If this cell shall fill the width, add extra width to the cell
					if (cell.shallfillWidth()) {
						float fillWidth = size.x - getPrefWidth();
						cellSize.x += fillWidth;
					}

					cell.layout(offset, cellSize);
					offset.x += cell.getWidth();
				}
			}

		}
		Pools.free(cellSize);
		Pools.free(offset);
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
	 * Sets the scaling factor for x
	 * @param scaleX the x scaling factor
	 * @return this row for chaining
	 */
	Row setScaleX(float scaleX) {
		if (mScalable) {
			mScaleX = scaleX;

			for (Cell cell : mCells) {
				cell.setScaleX(scaleX);
			}

			if (mDynamicPadding) {
				mScaledPadding.left = mPadding.left * mScaleX;
				mScaledPadding.right = mPadding.right * mScaleX;
			}
		}

		return this;
	}

	/**
	 * Sets the scaling factor for y
	 * @param scaleY the y scaling factor
	 * @return this row for chaining
	 */
	Row setScaleY(float scaleY) {
		if (mScalable) {
			mScaleY = scaleY;

			for (Cell cell : mCells) {
				cell.setScaleY(scaleY);
			}

			if (mDynamicPadding) {
				mScaledPadding.top = mPadding.top * mScaleY;
				mScaledPadding.bottom = mPadding.bottom * mScaleY;
			}
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

			// Minimum size of the table (because of non-scalable)
			if (!cell.isScalable()) {
				float minWidth = cell.getPrefWidth() * mCells.size();
				if (minWidth > mMinWidth) {
					mMinWidth = minWidth;
				}
			}
		} else {
			mPrefWidth += cell.getPrefWidth();
			mWidth += cell.getWidth();

			if (!cell.isScalable()) {
				mMinWidth += cell.getPrefWidth();

				float minHeight = cell.getPrefHeight();
				if (minHeight > mMinHeight) {
					mMinHeight = minHeight;
				}
			}
		}

		if (cell.getPrefHeight() > mPrefHeight) {
			mPrefHeight = cell.getPrefHeight();
		}
		if (!cell.isScalable()) {
			float minHeight = cell.getPrefHeight();
			if (minHeight > mMinHeight) {
				mMinHeight = minHeight;
			}
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
	/** Minimum width, equals all non-scalable cells' width */
	private float mMinWidth = 0;
	/** Minimum height, equals all non-scalable cells' height */
	private float mMinHeight = 0;
	/** Scaling x-factor */
	private float mScaleX = 1;
	/** Scaling y-factor */
	private float mScaleY = 1;
	/** If the row and its cells can be scaled */
	private boolean mScalable = true;
	/** Row alignment */
	private Align mAlign = new Align(Horizontal.LEFT, Vertical.MIDDLE);
	/** If the row shall fill the width of the table */
	private boolean mFillWidth = false;
	/** If the row shall fill the height of the table */
	private boolean mFillHeight = false;

	// Padding
	/** Padding for this row */
	private Padding mPadding = new Padding();
	/** Scaled padding for this row */
	private Padding mScaledPadding = new Padding();
	/** If the padding value should be dynamic, i.e. it will increase/decrease
	 * the padding depending on the scale factor */
	private boolean mDynamicPadding = true;
}

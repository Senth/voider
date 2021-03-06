package com.spiddekauga.utils.scene.ui;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.voider.utils.Pools;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Wrapper for a row Contains all the actors for the current row
 */
public class Row implements Poolable, IPadding<Row> {
/**
 * True if the row uses the full width of the parents getPrefWidth() and sets the cell's size to
 * equal
 */
private boolean mEqualSize = false;
/** All the columns in the table */
private ArrayList<Cell> mCells = new ArrayList<>();
/** Total preferred width of the actors in this row */
private float mPrefWidth = 0;
/**
 * Preferred height of the actors in this row, this is set to the actor with most preferred height.
 */
private float mPrefHeight = 0;
/** Width of the row, calculated from the cells */
private float mWidth = 0;
/** Height of the row, calculated from the cells */
private float mHeight = 0;
/** Minimum width, equals all non-scalable cells' width */
private float mMinWidth = 0;
/** Minimum height, equals all non-scalable cells' height */
private float mMinHeight = 0;
/** If the row should have fixed width */
private boolean mFixedWidth = false;
/** If the row should have fixed height */
private boolean mFixedHeight = false;
/** Row alignment */
private Align mAlign = new Align(Horizontal.LEFT, Vertical.MIDDLE);
/** If the row shall fill the width of the table */
private boolean mFillWidth = false;@Override
public Row setPad(Padding padding) {
	setPadLeft(padding.left);
	setPadRight(padding.right);
	setPadTop(padding.top);
	setPadBottom(padding.bottom);
	return this;
}
/** If the row shall fill the height of the table */
private boolean mFillHeight = false;@Override
public Row setPadLeft(float padLeft) {
	mPadding.left = padLeft;

	return this;
}
/** Padding for this row */
private Padding mPadding = new Padding();@Override
public Row setPadRight(float padRight) {
	mPadding.right = padRight;
	return this;
}

/**
 * Default constructor
 */
public Row() {
}@Override
public Row setPadTop(float padTop) {
	mPadding.top = padTop;
	return this;
}

@Override
public void reset() {
	mPrefHeight = 0;
	mPrefWidth = 0;
	mMinHeight = 0;
	mMinWidth = 0;
	mWidth = 0;
	mHeight = 0;
	mFillHeight = false;
	mFillWidth = false;
	mEqualSize = false;
	mFixedHeight = false;
	mFixedWidth = false;
	mAlign.horizontal = Horizontal.LEFT;
	mAlign.vertical = Vertical.MIDDLE;
	mPadding.reset();

	dispose(true);
}@Override
public Row setPadBottom(float padBottom) {
	mPadding.bottom = padBottom;
	return this;
}

/**
 * Disposes all cells but can save the actors inside the cells.
 * @param disposeActor true if you want to call onDestroy() on the actors
 */
public void dispose(boolean disposeActor) {
	for (Cell cell : mCells) {
		cell.dispose(disposeActor);
		Pools.cell.free(cell);
	}
	mCells.clear();
}@Override
public float getPadLeft() {
	return mPadding.left;
}

/**
 * Sets the cells to equal size. Uses the row alignment.
 * @param equalSize set to true to use equal spacing
 * @return This row for chaining
 */
public Row setEqualCellSize(boolean equalSize) {
	mEqualSize = equalSize;

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
}@Override
public float getPadRight() {
	return mPadding.right;
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
}@Override
public float getPadTop() {
	return mPadding.top;
}

/**
 * Sets the horizontal alignment of this row
 * @param horizontal the horizontal alignment
 * @return this row for chaining
 */
public Row setAlign(Horizontal horizontal) {
	mAlign.horizontal = horizontal;
	return this;
}@Override
public float getPadBottom() {
	return mPadding.bottom;
}

/**
 * Sets the vertical alignment of this row
 * @param vertical the vertical alignment
 * @return this row for chaining
 */
public Row setAlign(Vertical vertical) {
	mAlign.vertical = vertical;
	return this;
}@Override
public Padding getPad() {
	return mPadding;
}

/**
 * @return alignment for this row
 */
public Align getAlign() {
	return mAlign;
}@Override
public float getPadX() {
	return mPadding.left + mPadding.right;
}

/**
 * Set the alignment of this row
 * @param align
 * @return this for chaining
 */
public Row setAlign(Align align) {
	mAlign.set(align);
	return this;
}@Override
public float getPadY() {
	return mPadding.top + mPadding.bottom;
}

@Override
public Row setPad(float top, float right, float bottom, float left) {
	setPadLeft(left);
	setPadRight(right);
	setPadTop(top);
	setPadBottom(bottom);
	return this;
}

@Override
public Row setPad(float pad) {
	setPadLeft(pad);
	setPadRight(pad);
	setPadTop(pad);
	setPadBottom(pad);

	return this;
}

/**
 * @return minimum height of the row. Equals the non-scalable cells' height
 */
public float getMinHeight() {
	return mMinHeight + getPadY();
}

/**
 * @return minimum width of the row. Equals tho non-scalable cells' width
 */
public float getMinWidth() {
	return mMinWidth + getPadX();
}

/**
 * @return true if this row has fixed width. I.e. it has changed its width externally.
 */
boolean isFixedWidth() {
	return mFixedWidth;
}

/**
 * Sets the row as fixed width. Can be used together with {@link #setFillWidth(boolean)} so that the
 * row is actually bigger than the cells inside it.
 * @param fixedWidth set to true to make it fixed width
 * @return this for chaining
 */
public Row setFixedWidth(boolean fixedWidth) {
	mFixedWidth = fixedWidth;
	return this;
}

/**
 * @return true if this row has fixed height. I.e. it has changed its height externally.
 */
boolean isFixedHeight() {
	return mFixedHeight;
}

/**
 * Sets the row as fixed height. Can be used together with {@link #setFillHeight(boolean)} so that
 * the row is actually bigger than the cells inside it.
 * @param fixedHeight set to true to make it fixed height
 * @return this for chaining
 */
public Row setFixedHeight(boolean fixedHeight) {
	mFixedHeight = fixedHeight;
	return this;
}

/**
 * Sets if the row shall fill the remaining width of the table. This works for all rows in the
 * table.
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
public float getPrefHeight() {
	return mPrefHeight + getPadY();
}

/**
 * @return preferred width of the row
 */
public float getPrefWidth() {
	return mPrefWidth + getPadX();
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
 * Removes all empty cells
 */
public void removeEmptyCells() {
	Iterator<Cell> cellIt = mCells.iterator();
	while (cellIt.hasNext()) {
		Cell cell = cellIt.next();

		if (cell.isEmpty()) {
			cellIt.remove();
		}
	}
}

/**
 * @return number of cells in the row
 */
public int getCellCount() {
	return mCells.size();
}

/**
 * @return all cells
 */
public ArrayList<Cell> getCells() {
	return mCells;
}

/**
 * @return last added cell for this row
 */
public Cell getCell() {
	if (!mCells.isEmpty()) {
		return mCells.get(mCells.size() - 1);
	}
	return null;
}

/**
 * @return false if all cells are invisible. If a row exists without a cell or with an empty cell it
 * still returns true if the cell is set as visible.
 */
boolean isVisible() {
	if (!mCells.isEmpty()) {
		for (Cell cell : mCells) {
			if (cell.isVisible()) {
				return true;
			}
		}
	} else {
		return true;
	}

	return false;
}

/**
 * Recalculates the preferred width and preferred height
 */
void calculatePreferredSize() {
	mPrefHeight = 0;
	mPrefWidth = 0;
	mMinHeight = 0;
	mMinWidth = 0;
	if (!mFixedWidth) {
		mWidth = 0;
	}
	if (!mFixedHeight) {
		mHeight = 0;
	}

	for (Cell cell : mCells) {
		if (cell.isVisible()) {
			cell.calculatePreferredSize();
			addSize(cell);
		}
	}

	if (mFixedWidth && mMinWidth < mWidth) {
		mMinWidth = mWidth;
	}
	if (mFixedHeight && mMinHeight < mHeight) {
		mMinHeight = mHeight;
	}
}

/**
 * Adds the width/height, preferred width/height to the total count of this rows size.
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
		if (!mFixedWidth) {
			mWidth += cell.getWidth();
		}
		mMinWidth += cell.getPrefWidth() > cell.getWidth() ? cell.getPrefWidth() : cell.getWidth();
	}

	if (cell.getPrefHeight() > mPrefHeight) {
		mPrefHeight = cell.getPrefHeight();
	}
	float minHeight = cell.getPrefHeight() > cell.getHeight() ? cell.getPrefHeight() : cell.getHeight();
	if (minHeight > mMinHeight) {
		mMinHeight = minHeight;
	}
	if (!mFixedHeight && cell.getHeight() > mHeight) {
		mHeight = cell.getHeight();
	}
}

/**
 * Calculates the actual size
 */
void calculateActualSize() {
	if (!mFillWidth && !mFixedWidth) {
		mWidth = 0;
	}
	if (!mFillHeight && !mFixedHeight) {
		mHeight = 0;
	}

	for (Cell cell : mCells) {
		if (cell.isVisible()) {
			cell.calculateActualSize();

			if (!mFillWidth && !mFixedWidth) {
				mWidth += cell.getWidth();
			}

			if (!mFillHeight && !mFixedHeight && cell.getHeight() > mHeight) {
				mHeight = cell.getHeight();
			}
		}
	}
}

/**
 * Update row and cell sizes
 * @param width new width of the row
 * @param height new height of the row
 */
void updateSize(float width, float height) {
	float realWidth = width - getPadLeft() - getPadRight();
	if (!mFixedWidth) {
		mWidth = realWidth;
	}

	float realHeight = height - getPadLeft() - getPadRight();
	if (!mFixedHeight) {
		mHeight = realHeight;
	}

	int cVisibleCells = getVisibleCellCount();
	if (mEqualSize && cVisibleCells > 0) {
		float equalCellWidth = mWidth / cVisibleCells;

		for (Cell cell : mCells) {
			cell.updateSize(equalCellWidth, mHeight);
		}
	} else {
		// Calculate total cell width
		float cellWidthTotal = 0;
		float cCellFillWidth = 0;
		for (Cell cell : mCells) {
			if (cell.isVisible()) {
				cellWidthTotal += cell.getWidth();

				if (cell.shallFillWidth()) {
					cCellFillWidth++;
				}
			}
		}

		float extraWidthPerFillWidthCell = 0;
		if (cCellFillWidth > 0) {
			extraWidthPerFillWidthCell = (mWidth - cellWidthTotal) / cCellFillWidth;
		}

		// Update cell sizes
		for (Cell cell : mCells) {
			if (cell.isVisible()) {
				float newCellWidth = cell.getWidth();
				if (cell.shallFillWidth()) {
					newCellWidth += extraWidthPerFillWidthCell;
				}

				float newCellHeight = cell.getHeight();
				if (cell.shallFillHeight()) {
					newCellHeight = mHeight;
				}

				cell.updateSize(newCellWidth, newCellHeight);
			}
		}
	}
}

/**
 * @return visible cells in the row
 */
private int getVisibleCellCount() {
	int cVisibleCells = 0;
	for (Cell cell : mCells) {
		if (cell.isVisible()) {
			cVisibleCells++;
		}
	}

	return cVisibleCells;
}

/**
 * Call this to layout the row.
 * @param startPos starting position of the row
 * @param availableSize available size for the row
 */
void layout(Vector2 startPos, Vector2 availableSize) {
	Vector2 offset = new Vector2();
	offset.set(startPos);


	// Check if there's a cell that wants to fill the width
	boolean cellFillWidth = false;
	for (Cell cell : mCells) {
		if (cell.isVisible() && cell.shallFillWidth()) {
			cellFillWidth = true;
			break;
		}
	}

	// Horizontal
	if (!cellFillWidth) {
		if (mAlign.horizontal == Horizontal.LEFT) {
			offset.x += getPadLeft();
		} else if (mAlign.horizontal == Horizontal.RIGHT) {
			offset.x += getPadLeft() + availableSize.x - getWidth();
		} else if (mAlign.horizontal == Horizontal.CENTER) {
			offset.x += (availableSize.x - getWidth()) * 0.5f;
		}
	}

	// Vertical
	if (mAlign.vertical == Vertical.BOTTOM) {
		offset.y = startPos.y + getPadBottom();
	} else if (mAlign.vertical == Vertical.TOP) {
		offset.y = startPos.y + availableSize.y - mHeight - getPadTop();
	} else if (mAlign.vertical == Vertical.MIDDLE) {
		offset.y = startPos.y + (availableSize.y - getHeight()) * 0.5f;
	}

	Vector2 availableCellSize = new Vector2();
	availableCellSize.y = mHeight;

	for (Cell cell : mCells) {
		if (cell.isVisible()) {
			availableCellSize.x = cell.getWidth();
			cell.layout(offset, availableCellSize);
			offset.x += availableCellSize.x;
		}

	}
}

/**
 * @return width of the row
 */
public float getWidth() {
	return mWidth + getPadX();
}

/**
 * Sets the width of the row, will automatically set it as fixed width
 * @param width
 * @return this Row for chaining
 */
public Row setWidth(float width) {
	mWidth = width;
	mFixedWidth = true;
	return this;
}

/**
 * @return height of the row
 */
public float getHeight() {
	return mHeight + getPadY();
}

/**
 * Sets the height of the row, will automatically set it as fixed height
 * @param height
 * @return this Row for chaining
 */
public Row setHeight(float height) {
	mHeight = height;
	mFixedHeight = true;
	return this;
}

/**
 * Add a cell to the row. Sets the alignment for the actor
 * @param cell new cell with actor to append
 * @param index where to add the cell
 */
void add(Cell cell, int index) {
	mCells.add(index, cell);
	cell.setAlign(mAlign.horizontal, mAlign.vertical);
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












// Padding

}

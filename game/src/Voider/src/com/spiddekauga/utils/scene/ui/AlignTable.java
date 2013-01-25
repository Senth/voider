package com.spiddekauga.utils.scene.ui;

import java.util.ArrayList;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.Layout;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Pools;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;

/**
 * Table that allows for aligning inside the widgets.
 * This table also fixes scaling of the widgets inside.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class AlignTable extends WidgetGroup implements Disposable {
	/**
	 * Constructor, creates an empty first row
	 */
	public AlignTable() {
		setTouchable(Touchable.enabled);
	}

	@Override
	public void dispose() {
		for (Row row : mRows) {
			Pools.free(row);
		}
		mRows.clear();
	}

	/**
	 * Sets the table alignment
	 * @param horizontal horizontal alignment of the table
	 * @param vertical vertical alignment of the table
	 * @return this table for chaining
	 * @see #setRowAlign(Horizontal,Vertical) sets the row alignment instead
	 */
	public AlignTable setTableAlign(Horizontal horizontal, Vertical vertical) {
		mTableAlign.horizontal = horizontal;
		mTableAlign.vertical = vertical;
		return this;
	}

	/**
	 * Sets the default row alignment for new rows that are added
	 * @param horizontal default horizontal alignment for new rows
	 * @param vertical default vertical alignment for new rows
	 * @return this table for chaining
	 * @see #setTableAlign(Horizontal,Vertical) sets table alignment instead
	 */
	public AlignTable setRowAlign(Horizontal horizontal, Vertical vertical) {
		mRowAlign.horizontal = horizontal;
		mRowAlign.vertical = vertical;
		return this;
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

		Row row = mRows.get(mRows.size() -1);

		// Subtract old height from this height, we will add the new height
		// later
		mPrefHeight -= row.getPrefHeight();

		actor.setVisible(true);

		if (actor instanceof Layout) {
			((Layout) actor).invalidate();
		}

		Cell newCell = Pools.obtain(Cell.class).setActor(actor);
		newCell.setScaleX(mScaleX);
		newCell.setScaleY(mScaleY);
		row.add(newCell);

		// Update size
		mPrefHeight += row.getPrefHeight();

		if (row.getPrefWidth() > mPrefWidth) {
			mPrefWidth = row.getPrefWidth();
		}

		addActor(actor);

		return newCell;
	}

	/**
	 * Adds another row to the table.
	 * Uses the default row alignment
	 * @see #row(Horizontal,Vertical) for adding a new row with the specified alignment
	 * @see #setRowAlign(Horizontal,Vertical) for setting the default row alignment
	 * @return the created row
	 */
	public Row row() {
		return row(mRowAlign.horizontal, mRowAlign.vertical);
	}

	/**
	 * Adds another row to the table with a specified layout. This does not set
	 * the default row alignment.
	 * @param horizontal horizontal alignment for this new row
	 * @param vertical vertical alignment for this new row
	 * @see #row() for adding a row with the default row alignment
	 * @see #setRowAlign(Horizontal,Vertical) for setting the default row alignment
	 * @return the created row
	 */
	public Row row(Horizontal horizontal, Vertical vertical) {
		Row row = Pools.obtain(Row.class);
		row.setAlign(horizontal, vertical);
		row.setScaleX(mScaleX);
		row.setScaleY(mScaleY);
		mRows.add(row);
		return row;
	}

	/**
	 * Sets the width of the table, including scales all the actors inside if necessary
	 * @param width new width of the table
	 */
	@Override
	public void setWidth(float width) {
		float oldWidth = getWidth();
		super.setWidth(width);

		// Scaling needed?
		if (getWidth() < getPrefWidth()) {
			scaleToFit();
		}
		// Scale to 1
		else if (oldWidth < getPrefWidth() && getHeight() >= getPrefHeight()) {
			setScale(1);
		}
	}

	/**
	 * Sets the height of the table, including scales all the actors inside if necessary
	 * @param height new height of the table
	 */
	@Override
	public void setHeight(float height) {
		float oldHeight = getHeight();
		super.setHeight(height);

		// Scaling needed?
		if (getHeight() < getPrefHeight()) {
			scaleToFit();
		}
		else if (oldHeight < getPrefHeight() && getWidth() >= getPrefWidth()) {
			setScale(1);
		}
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
		return mPrefHeight;
	}

	@Override
	public float getPrefWidth() {
		return mPrefWidth;
	}

	@Override
	public void setScale(float scale) {
		setScaleX(scale);
		setScaleY(scale);
	}

	@Override
	public void setScale(float scaleX, float scaleY) {
		setScaleX(scaleX);
		setScaleY(scaleY);
	}

	@Override
	public void scale(float scale) {
		setScale(scale);
	}

	@Override
	public void setScaleX(float scale) {
		mScaleX = scale;

		for (Row row : mRows) {
			row.setScaleX(scale);
		}
		invalidate();
	}

	@Override
	public void setScaleY(float scale) {
		mScaleY = scale;

		for (Row row : mRows) {
			row.setScaleY(scale);
		}
		invalidate();
	}

	@Override
	public void layout() {
		calculateSize();

		Vector2 offset = Pools.obtain(Vector2.class);
		// Horizontal offset
		// Calculate initial offset and offset between rows
		if (mTableAlign.horizontal == Horizontal.LEFT) {
			offset.x = getX();
		} else if (mTableAlign.horizontal == Horizontal.RIGHT) {
			offset.x = getX() + getWidth() - getPrefWidth();
		} else if (mTableAlign.horizontal == Horizontal.CENTER) {
			offset.x = getX() + getWidth() * 0.5f - getPrefWidth() * 0.5f;
		}

		// Vertical
		if (mTableAlign.vertical == Vertical.BOTTOM) {
			offset.y = getY();
		} else if (mTableAlign.vertical == Vertical.TOP) {
			offset.y = getY() + getHeight() - getPrefHeight();
		} else if (mTableAlign.vertical == Vertical.MIDDLE) {
			offset.y = getY() + getHeight() * 0.5f - getPrefHeight() * 0.5f;
		}


		// Layout the rows
		Vector2 size = Pools.obtain(Vector2.class);
		size.x = getPrefWidth() > getMinWidth() ? getPrefWidth() : getMinWidth();
		for (int i = mRows.size() - 1; i >= 0; --i) {
			Row row = mRows.get(i);
			size.y = row.getHeight();
			row.layout(offset, size);
			offset.y += size.y;
		}

		Pools.free(size);
		Pools.free(offset);
	}

	@Override
	public void setTransform(boolean transform) {
		for (Row row : mRows) {
			row.setTransform(transform);
		}
	}

	/**
	 * Recalculates the preferred width and height
	 */
	private void calculateSize() {
		mPrefHeight = 0;
		mPrefWidth = 0;
		mMinHeight = 0;
		mMinWidth = 0;
		mExtraHeight = 0;
		mExtraWidth = 0;


		for (Row row : mRows) {
			row.calculateSize();
			mPrefHeight += row.getPrefHeight();
			mMinHeight += row.getMinHeight();

			// Add extra height
			if (row.getMinHeight() < row.getPrefHeight()) {
				mExtraHeight += row.getPrefHeight() - row.getMinHeight();
			}

			if (row.getPrefWidth() > mPrefWidth) {
				mPrefWidth = row.getPrefWidth();
			}

			if (row.getMinWidth() > mMinWidth) {
				mMinWidth = row.getMinWidth();
			}
		}

		if (getMinWidth() < getPrefWidth()) {
			mExtraWidth = getPrefWidth() - getMinWidth();
		}
	}

	/**
	 * Scales the rows and cells to fit the table
	 */
	private void scaleToFit() {
		// Division by 0 check
		if (getPrefWidth() == 0 || getWidth() == 0 || getPrefHeight() == 0 || getHeight() == 0) {
			return;
		}

		float widthScale = 1;
		if (getPrefWidth() - getWidth() > mExtraWidth) {
			widthScale = (getWidth() - getMinWidth()) / (getPrefWidth() - getMinWidth());
		} else {
			widthScale = getWidth() / getPrefWidth();
		}

		float heightScale = 1;
		if (getPrefHeight() - getHeight() > mExtraHeight) {
			heightScale = (getHeight() - getMinHeight()) / (getPrefHeight() - getMinHeight());
		} else {
			heightScale = getHeight() / getPrefHeight();
		}

		if (widthScale < heightScale && widthScale < 1) {
			setScale(widthScale);
		} else if (heightScale < widthScale && heightScale < 1) {
			setScale(heightScale);
		}
	}


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
	/** Extra height for scaling, i.e. non-scalable cells' are not the highest one */
	private float mExtraHeight = 0;
	/** Extra width for scaling, i.e. non-scalable cells' (rows) are not the widest ones */
	private float mExtraWidth = 0;
	/** Scale X value */
	private float mScaleX = 1;
	/** Scale Y value */
	private float mScaleY = 1;
}

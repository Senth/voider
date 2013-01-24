package com.spiddekauga.utils.scene.ui;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Pools;

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
		mRows.add(Pools.obtain(Row.class));
	}

	@Override
	public void dispose() {
		for (Row row : mRows) {
			Pools.free(row);
		}
		mRows.clear();
		mChildren.clear();
	}

	/**
	 * Sets the table alignment
	 * @param align the table alignment
	 * @see #setRowAlign(int) sets the row alignment instead
	 * @See Align for alignment variables
	 */
	public void setTableAlign(int align) {
		mTableAlign = align;
	}

	/**
	 * Sets the default row alignment for new rows that are added
	 * @param align cell alignment for new cells
	 * @see #setTableAlign(int) sets table alignment instead
	 * @see Align for alignment variables
	 */
	public void setRowAlign(int align) {
		mRowAlign = align;
	}

	/**
	 * Adds a new actor to the current row.
	 * @param actor new actor to add
	 * @return cell that was added
	 */
	public Cell add(Actor actor) {
		Row row = mRows.get(mRows.size() -1);

		// Subtract old height from this height, we will add the new height
		// later
		mPrefHeight -= row.getPrefHeight();

		Cell newCell = Pools.obtain(Cell.class).setActor(actor);
		row.add(newCell);

		// Update size
		mPrefHeight += row.getPrefHeight();

		if (row.getPrefWidth() > mPrefWidth) {
			mPrefWidth = row.getPrefWidth();
		}

		mChildren.add(actor);

		return newCell;
	}

	/**
	 * Adds another row to the table.
	 * Uses the default row alignment
	 * @see #row(int) for adding a new row with the specified alignment
	 * @see #setRowAlign(int) for setting the default row alignment
	 * @return the created row
	 */
	public Row row() {
		return row(mRowAlign);
	}

	/**
	 * Adds another row to the table with a specified layout. This does not set
	 * the default row alignment.
	 * @param align the row alignment to be used for this row.
	 * @see #row() for adding a row with the default row alignment
	 * @see #setRowAlign(int) for setting the default row alignment
	 * @return the created row
	 */
	public Row row(int align) {
		Row row = Pools.obtain(Row.class);
		mRows.add(row);
		return row;
	}

	/**
	 * @return preferred height of the row
	 */
	@Override
	public float getPrefHeight() {
		return mPrefHeight;
	}

	/**
	 * @return preferred width of the row
	 */
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
		/** @TODO scale */
	}

	@Override
	public void setScaleY(float scale) {
		/** @TODO scale */
	}

	@Override
	public void act(float delta) {
		for (Actor actor : mChildren) {
			actor.act(delta);
		}
	}

	@Override
	public void draw (SpriteBatch batch, float parentAlpha) {
		for (Actor actor : mChildren) {
			actor.draw(batch, parentAlpha);
		}
	}

	@Override
	public void layout() {
		calculateSize();

		Vector2 offset = Pools.obtain(Vector2.class);
		// Horizontal offset
		// Calculate initial offset and offset between rows
		if ((mTableAlign & Align.LEFT) > 0) {
			offset.x = getX();
		} else if ((mTableAlign & Align.RIGHT) > 0) {
			offset.x = getX() + getWidth() - getPrefWidth();
		} else if ((mTableAlign & Align.CENTER) > 0) {
			offset.x = getX() + getWidth() * 0.5f - getPrefWidth() * 0.5f;
		}

		// Vertical
		if ((mTableAlign & Align.BOTTOM) > 0) {
			offset.y = getY();
		} else if ((mTableAlign & Align.TOP) > 0) {
			offset.y = getY() + getHeight() - getPrefHeight();
		} else if ((mTableAlign & Align.MIDDLE) > 0) {
			offset.y = getY() + getHeight() * 0.5f - getPrefHeight() * 0.5f;
		}


		// Layout the rows
		Vector2 size = Pools.obtain(Vector2.class);
		size.x = getPrefWidth();
		for (int i = mRows.size() - 1; i >= 0; --i) {
			Row row = mRows.get(i);
			size.y = row.getPrefHeight();
			row.layout(offset, size);
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

		for (Row row : mRows) {
			row.calculateSize();
			mPrefHeight += row.getPrefHeight();

			if (row.getPrefWidth() > mPrefWidth) {
				mPrefWidth = row.getPrefWidth();
			}
		}
	}


	/** All actor children */
	private ArrayList<Actor> mChildren = new ArrayList<Actor>();
	/** All the rows of the table */
	private ArrayList<Row> mRows = new ArrayList<Row>();
	/** Table alignment (not cell alignment) */
	private int mTableAlign = Align.CENTER | Align.MIDDLE;
	/** Standard row alignment */
	private int mRowAlign = Align.LEFT | Align.MIDDLE;
	/** Preferred width of the actors, this is set to the row with highest preferred width */
	private float mPrefWidth = 0;
	/** Preferred height of the table, adds all rows preferred width */
	private float mPrefHeight = 0;
}

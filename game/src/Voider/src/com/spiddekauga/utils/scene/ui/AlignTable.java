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

	//	@Override
	//	public void act(float delta) {
	//		for (Actor actor : mChildren) {
	//			actor.act(delta);
	//		}
	//	}
	//
	//	@Override
	//	public void draw (SpriteBatch batch, float parentAlpha) {
	//		for (Actor actor : mChildren) {
	//			actor.draw(batch, parentAlpha);
	//		}
	//	}

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
		size.x = getPrefWidth();
		for (int i = mRows.size() - 1; i >= 0; --i) {
			Row row = mRows.get(i);
			size.y = row.getHeight();
			row.layout(offset, size);
			offset.y += row.getHeight();
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
}

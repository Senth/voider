package com.spiddekauga.utils.scene.ui;

import java.util.ArrayList;
import java.util.Iterator;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.Layout;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.SnapshotArray;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.voider.utils.Pools;

/**
 * Table that allows for aligning inside the widgets.
 * This table also fixes scaling of the widgets inside.
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class AlignTable extends WidgetGroup implements Disposable {
	/**
	 * Constructor, creates an empty first row
	 */
	public AlignTable() {
		setTouchable(Touchable.childrenOnly);
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
	 * @param disposeActors true if you want to call dispose() on
	 * the actors.
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
	}

	@Override
	public void invalidate() {
		super.invalidate();

		mValidLayout = false;
	}

	/**
	 * @return true if the table has a valid layout
	 */
	public boolean isLayoutValid() {
		return mValidLayout;
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
	 * Sets both the table and row alignment. I.e. same as calling {@link #setAlignRow(Horizontal, Vertical)}
	 * and {@link #setAlignTable(Horizontal, Vertical)}
	 * @param horizontal default horizontal alignment for table and new rows
	 * @param vertical default vertical alignment for table and new rows
	 * @return this tabale for chaining
	 * @see #setAlignRow(Horizontal, Vertical) for only setting row alignment (on new rows)
	 * @see #setAlignTable(Horizontal, Vertical) for only setting table alignment
	 */
	public AlignTable setAlign(Horizontal horizontal, Vertical vertical) {
		setAlignRow(horizontal, vertical);
		setAlignTable(horizontal, vertical);
		return this;
	}

	/**
	 * Sets the default padding for all new cells in the table. By default this is
	 * 0 for all sides. This only affects new cells added to the table.
	 * To change the padding of a Cell, you need to retrieve it from the {@link #add(Actor)}
	 * method and then call the appropriate padding method.
	 * @param top padding at the top
	 * @param right padding to the right
	 * @param bottom padding at the bottom
	 * @param left padding to the left
	 * @return this table for chaining
	 */
	public AlignTable setCellPaddingDefault(float top, float right, float bottom, float left) {
		mCellPaddingDefault.top = top;
		mCellPaddingDefault.right = right;
		mCellPaddingDefault.bottom = bottom;
		mCellPaddingDefault.left = left;
		return this;
	}

	/**
	 * Sets the default padding for all new cells in the table. By default this is
	 * 0 for all sides. This only affects new cells added to the table.
	 * To change the padding of a Cell, you need to retrieve it from the {@link #add(Actor)}
	 * method and then call the appropriate padding method.
	 * @param padding padding for all sides
	 * @return this table for chaining
	 */
	public AlignTable setCellPaddingDefault(float padding) {
		mCellPaddingDefault.top = padding;
		mCellPaddingDefault.right = padding;
		mCellPaddingDefault.bottom = padding;
		mCellPaddingDefault.left = padding;
		return this;
	}

	/**
	 * Sets the default padding for all new rows in the table. By default this
	 * is 0 for all sides. Only affects new rows created after this call.
	 * @param top padding at the top
	 * @param right padding to the right
	 * @param bottom padding at the bottom
	 * @param left padding to the left
	 * @return this table for chaining
	 */
	public AlignTable setRowPaddingDefault(float top, float right, float bottom, float left) {
		mRowPaddingDefault.top = top;
		mRowPaddingDefault.right = right;
		mRowPaddingDefault.bottom = bottom;
		mRowPaddingDefault.left = left;
		return this;
	}

	/**
	 * Sets the default padding for all new rows in the table. By default this
	 * is 0 for all sides. Only affects new rows created after this call.
	 * @param padding padding to the left, right, top, and bottom
	 * @return this table for chaining
	 */
	public AlignTable setRowPaddingDefault(float padding) {
		mRowPaddingDefault.top = padding;
		mRowPaddingDefault.right = padding;
		mRowPaddingDefault.bottom = padding;
		mRowPaddingDefault.left = padding;
		return this;
	}

	/**
	 * Gets all actors in the table
	 * @param onlyVisible set to true to get visible actors, false to get all
	 * @return array with all actors. Don't forget to free the ArrayList after
	 * it has been used.
	 */
	public ArrayList<Actor> getActors(boolean onlyVisible) {
		@SuppressWarnings("unchecked")
		ArrayList<Actor> actors = Pools.arrayList.obtain();

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
					} else if (actor.getClass() == Table.class) {
						SnapshotArray<Actor> tableActors = ((Table) actor).getChildren();
						for (int i = 0; i < tableActors.size; ++i) {
							Actor tableActor = tableActors.get(i);
							if (!onlyVisible || cell.isVisible()) {
								actors.add(tableActor);
							}
						}
					} else if (actor != null) {
						actors.add(actor);
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
		newCell.setPadding(mCellPaddingDefault);

		Row row = mRows.get(mRows.size() -1);
		row.add(newCell);

		if (actor != null) {
			addActor(actor);
		}

		return newCell;
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
	 * Adds another row to the table.
	 * Uses the default row alignment
	 * @see #row(Horizontal,Vertical) for adding a new row with the specified alignment
	 * @see #setAlignRow(Horizontal,Vertical) for setting the default row alignment
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
	 * @see #setAlignRow(Horizontal,Vertical) for setting the default row alignment
	 * @return the created row
	 */
	public Row row(Horizontal horizontal, Vertical vertical) {
		Row row = Pools.row.obtain();
		row.setAlign(horizontal, vertical);
		row.setPadding(mRowPaddingDefault);
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
	 * Sets the width of the table, including scales all the actors inside if necessary
	 * @param width new width of the table
	 */
	@Override
	public void setWidth(float width) {
		super.setWidth(width);

		invalidate();
	}

	@Override
	public void setSize(float width, float height) {
		setWidth(width);
		setHeight(height);
	}

	/**
	 * Sets the height of the table, including scales all the actors inside if necessary
	 * @param height new height of the table
	 */
	@Override
	public void setHeight(float height) {
		super.setHeight(height);

		invalidate();
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
		if (mKeepSize) {
			return getHeight();
		} else {
			return mPrefHeight;
		}
	}

	@Override
	public float getPrefWidth() {
		if (mKeepSize) {
			return getWidth();
		} else {
			return mPrefWidth;
		}
	}

	@Override
	public void layout() {
		calculateSize();

		float rowHeight = 0;
		float rowWidth = 0;
		int cRowFillHeight = 0;
		boolean rowFillWidth = false;
		for (Row row : mRows) {
			rowHeight += row.getHeight();
			if (row.getWidth() > rowWidth) {
				rowWidth = row.getWidth();
			}

			if (row.shallFillHeight()) {
				cRowFillHeight++;
			}
			if (row.shallfillWidth()) {
				rowFillWidth = true;
			}
		}

		Vector2 position = Pools.vector2.obtain();


		// Set custom position if we don't have any table parent
		// Horizontal offset
		// If fill row, the x offset will always be 0
		if (rowFillWidth) {
			position.x = 0;
		}
		// Calculate offset depending on alignment
		else {
			if (mTableAlign.horizontal == Horizontal.LEFT) {
				position.x = 0;
			} else if (mTableAlign.horizontal == Horizontal.RIGHT) {
				position.x = getAvailableWidth() - rowWidth;
			} else if (mTableAlign.horizontal == Horizontal.CENTER) {
				position.x = getAvailableWidth() * 0.5f - rowWidth * 0.5f;
			}
		}

		// Vertical
		// If fill height, the y offset will always be 0
		if (cRowFillHeight > 0) {
			position.y = 0;
		}
		// Calculate offset depending on alignment
		else {
			if (mTableAlign.vertical == Vertical.BOTTOM) {
				position.y = 0;
			} else if (mTableAlign.vertical == Vertical.TOP) {
				position.y = getAvailableHeight() - rowHeight;
			} else if (mTableAlign.vertical == Vertical.MIDDLE) {
				position.y = getAvailableHeight() * 0.5f - rowHeight * 0.5f;
			}
		}

		if (getParent() instanceof AlignTable) {
			position.x = getX() - position.x;
		}

		setPosition((int)position.x, (int)position.y);

		Vector2 offset = Pools.vector2.obtain();
		offset.set(0,0);

		// Layout the rows
		Vector2 rowSize = Pools.vector2.obtain();
		if (rowFillWidth) {
			rowSize.x = getAvailableWidth();
		} else {
			rowSize.x = rowWidth < mPrefWidth ? rowWidth : mPrefWidth;
		}
		for (int i = mRows.size() - 1; i >= 0; --i) {
			Row row = mRows.get(i);


			// If row shall fill height, give it the extra height
			if (row.shallFillHeight()) {
				float fillHeight = getAvailableHeight() - mPrefHeight;
				fillHeight /= cRowFillHeight;
				rowSize.y = row.getPrefHeight() + fillHeight;
			} else {
				rowSize.y = row.getHeight();
			}

			row.layout(offset, rowSize);
			offset.y += rowSize.y;
		}

		// Update size as the table can be larger if we have fill height/width
		// enabled
		super.setWidth(rowSize.x);
		super.setHeight(offset.y);


		Pools.vector2.freeAll(rowSize, position, offset);

		updateBackgroundSize();

		mValidLayout = true;
	}

	@Override
	public void setTransform(boolean transform) {
		for (Row row : mRows) {
			row.setTransform(transform);
		}
	}

	/**
	 * @return available width of the table
	 */
	protected float getAvailableWidth() {
		if ((getParent() == null || getParent().getParent() == null) && getStage() != null) {
			return getStage().getWidth();
		} else {
			return getWidth();
		}
	}

	/**
	 * @return available height of the table
	 */
	protected float getAvailableHeight() {
		if ((getParent() == null || getParent().getParent() == null) && getStage() != null) {
			return getStage().getHeight();
		} else {
			return getHeight();
		}
	}

	/**
	 * Makes the table keep its size (that was set through #setSize(float,float), #setWidth(float), or
	 * #setHeight(float)) after the table has been invalidated. I.e. it will not resize the table to fit
	 * the contents.
	 * @param keepSize set to true to keep the size of the table. If set to false table will not always be
	 * the same size as getPrefWidth/Height. If the table's content have been scaled down/up the table size
	 * will be smaller/larger than the preferred size.
	 * @return this table for chaining
	 */
	public AlignTable setKeepSize(boolean keepSize) {
		mKeepSize = keepSize;
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

			layout();
		}

		return removed;
	}

	/**
	 * Recalculates the preferred width and height
	 */
	private void calculateSize() {
		mPrefHeight = 0;
		mPrefWidth = 0;
		mMinHeight = 0;
		mMinWidth = 0;

		float width = 0;
		float height = 0;

		for (Row row : mRows) {
			row.calculateSize();
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

		// Change size of table to fit the content
		if (!mKeepSize) {
			super.setWidth(width);
			super.setHeight(height);
		}
	}

	/**
	 * Set a background image of the table
	 * @param image background image of this table, null to remove an existing background image
	 */
	public void setBackgroundImage(Image image) {
		// Remove old background
		if (mBackground != null) {
			removeActor(mBackground);
		}

		mBackground = image;

		if (mBackground != null) {
			updateBackgroundSize();
			addActorAt(0, mBackground);
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

	/** Background image */
	private Image mBackground = null;
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
	/** If the table shall keep size after layout, or it shall resize itself */
	private boolean mKeepSize = false;
	/** If we're currently disposing */
	private boolean mDisposing = false;
	/** Default cell padding */
	private Padding mCellPaddingDefault = new Padding();
	/** Default row padding */
	private Padding mRowPaddingDefault = new Padding();
}

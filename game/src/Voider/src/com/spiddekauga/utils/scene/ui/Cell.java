package com.spiddekauga.utils.scene.ui;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.utils.Layout;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;
import com.spiddekauga.voider.utils.Pools;

/**
 * Wrapper for a cell.
 * Contains both the actor in the cell and align information
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class Cell implements Poolable {
	/**
	 * Sets the alignment of this cell
	 * @param horizontal the horizontal alignment
	 * @param vertical the vertical alignment
	 * @return this cell for chaining
	 */
	public Cell setAlign(Horizontal horizontal, Vertical vertical) {
		mAlign.horizontal = horizontal;
		mAlign.vertical = vertical;
		return this;
	}

	/**
	 * Disposes the cell, the actor can be saved
	 * @param disposeActor true if you want to call dispose() on the actor
	 */
	public void dispose(boolean disposeActor) {
		if (mActor != null) {
			mActor.remove();

			if (disposeActor) {
				mActor.clearActions();
				if (mActor instanceof Disposable) {
					((Disposable) mActor).dispose();
				}
			}
		}

		mActor = null;
	}

	@Override
	public void reset() {
		dispose(true);
		mAlign.horizontal = Horizontal.LEFT;
		mAlign.vertical = Vertical.MIDDLE;

		mPadding.reset();

		mFillHeight = false;
		mFillWidth = false;
		mWidthBeforeFill = 0;
		mHeightBeforeFill = 0;
	}

	/**
	 * Sets if the cell should be of box shape
	 * @param boxShaped set to true to make the actor be shaped as a box
	 * @return this cell for chaining
	 */
	public Cell setBoxShaped(boolean boxShaped) {
		mBoxShape = boxShaped;
		return this;
	}

	/**
	 * Sets the padding for left, right, top, bottom
	 * @param pad how much padding should be on the sides
	 * @return this cell for chaining
	 */
	public Cell setPadding(float pad) {
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
	public Cell setPadding(float top, float right, float bottom, float left) {
		setPadLeft(left);
		setPadRight(right);
		setPadTop(top);
		setPadBottom(bottom);
		return this;
	}

	/**
	 * Sets the padding of the cell
	 * @param padding padding information
	 * @return this cell for chaining
	 */
	Cell setPadding(Padding padding) {
		setPadLeft(padding.left);
		setPadRight(padding.right);
		setPadTop(padding.top);
		setPadBottom(padding.bottom);
		return this;
	}

	/**
	 * Sets the padding to the left of the cell
	 * @param padLeft how much padding should be to the left of the cell
	 * @return this cell for chaining
	 */
	public Cell setPadLeft(float padLeft) {
		mPadding.left = padLeft;

		return this;
	}

	/**
	 * Sets the padding to the right of the cell
	 * @param padRight how much padding should be on the right of the cell
	 * @return this cell for chaining
	 */
	public Cell setPadRight(float padRight) {
		mPadding.right = padRight;

		return this;
	}

	/**
	 * Sets the padding at the top of the cell
	 * @param padTop how much padding should be at the top of the cell
	 * @return this cell for chaining
	 */
	public Cell setPadTop(float padTop) {
		mPadding.top = padTop;

		return this;
	}

	/**
	 * Sets the padding at teh bottom of the cell
	 * @param padBottom how much padding should be at the bottom of the cell
	 * @return this cell for chaining
	 */
	public Cell setPadBottom(float padBottom) {
		mPadding.bottom = padBottom;

		return this;
	}

	/**
	 * @return padding to the left of this cell. If dynamic padding is on this
	 * will return the scaled padding instead.
	 */
	public float getPadLeft() {
		return mPadding.left;
	}

	/**
	 * @return padding to the right of this cell. If dynamic padding is on this
	 * will return the scaled padding instead.
	 */
	public float getPadRight() {
		return mPadding.right;
	}

	/**
	 * @return padding to the top of this cell. If dynamic padding is on this
	 * will return the scaled padding instead.
	 */
	public float getPadTop() {
		return mPadding.top;
	}

	/**
	 * @return padding to the bottom of this cell. If dynamic padding is on this
	 * will return the scaled padding instead.
	 */
	public float getPadBottom() {
		return mPadding.bottom;
	}

	/**
	 * Sets the size of the cell.
	 * @note That this will change the preferred size of the cell. Use #resetSize() to reset
	 * the size to the actual preferred size
	 * @param width new fixed width of the cell
	 * @param height new fixed height of the cell
	 * @return this cell for chaining
	 * @see #setWidth(float)
	 * @see #setHeight(float)
	 * @see #resetSize()
	 */
	public Cell setSize(float width, float height) {
		mActor.setSize(width, height);
		mFixedSize = true;
		return this;
	}

	/**
	 * Sets the width of the cell.
	 * Does not change the preferred width.
	 * @note That this will change the preferred size of the cell. Use #resetSize() to reset
	 * the size to the actual preferred size
	 * @param width new width of the cell.
	 * @return this cell for chaining.
	 * @see #setSize(float, float)
	 * @see #setHeight(float)
	 * @see #resetSize()
	 */
	public Cell setWidth(float width) {
		if (mActor != null) {
			mActor.setWidth(width);
			mFixedSize = true;
		}
		return this;
	}

	/**
	 * Sets the height of the cell. Don't use this together with scaling!
	 * Does not change the preferred height.
	 * @note That this will change the preferred size of the cell. Use #resetSize() to reset
	 * the size to the actual preferred size
	 * @param height new height of the cell.
	 * @return this cell for chaining
	 * @see #setSize(float, float)
	 * @see #setWidth(float)
	 * @see #resetSize()
	 */
	public Cell setHeight(float height) {
		if (mActor != null) {
			mActor.setHeight(height);
			mFixedSize = true;
		}
		return this;
	}

	/**
	 * Resets the size of the cell to the original preferred size
	 * @return this cell for chaining
	 * @see #setSize(float, float)
	 * @see #setWidth(float)
	 * @see #setHeight(float)
	 */
	public Cell resetSize() {
		mFixedSize = false;
		if (mActor instanceof Layout) {
			mActor.setSize(((Layout) mActor).getPrefWidth(), ((Layout) mActor).getPrefHeight());
		}
		return this;
	}

	/**
	 * @return true if this cell is of fixed size. I.e. it has changed
	 * its size externally.
	 */
	boolean isFixedSize() {
		return mFixedSize;
	}

	/**
	 * @return true if the cell is visible
	 */
	public boolean isVisible() {
		if (mActor != null) {
			return mActor.isVisible();
		} else {
			return true;
		}
	}

	/**
	 * Sets if the cell shall fill the remaining width of the row.
	 * @todo only works with on cell per row for now.
	 * @param fillWidth set to true if the cell shall fill the remaining width of the row
	 * @return this cell for chaining
	 */
	public Cell setFillWidth(boolean fillWidth) {
		mFillWidth = true;

		if (mFillWidth && mActor != null) {
			mWidthBeforeFill = mActor.getWidth();
		}

		return this;
	}

	/**
	 * @return true if the cell shall fill the remaining width of the row
	 */
	public boolean shallfillWidth() {
		return mFillWidth;
	}

	/**
	 * Sets if the cell shall fill the remaining height of the row.
	 * This works for all cells in the row.
	 * @param fillHeight true if the cell shall fill the remaining height of the row.
	 * @return this cell for chaining
	 */
	public Cell setFillHeight(boolean fillHeight) {
		mFillHeight = fillHeight;

		if (mFillHeight && mActor != null) {
			mWidthBeforeFill = mActor.getHeight();
		}

		return this;
	}

	/**
	 * @return true if the cell shall fill the height of the row
	 */
	public boolean shallFillHeight() {
		return mFillHeight;
	}

	/**
	 * Sets transform for the cell
	 * @param transform true if the cell shall be able to transform
	 * @return this cell for chaining
	 */
	Cell setTransform(boolean transform) {
		if (mActor instanceof Group) {
			((Group) mActor).setTransform(transform);
		}
		return this;
	}

	/**
	 * Sets the actor for the cell. Resizes the actor to the preferred
	 * size if its of size 0.
	 * @param actor the actor for the cell
	 * @return this cell for chaining
	 */
	Cell setActor(Actor actor) {
		mActor = actor;

		if (mActor instanceof Layout) {
			if (mActor.getHeight() == 0) {
				mActor.setHeight(((Layout) mActor).getPrefHeight());
			}
			if (mActor.getWidth() == 0) {
				mActor.setWidth(((Layout) mActor).getPrefWidth());
			}
		}

		return this;
	}

	/**
	 * @return preferred width of the cell
	 */
	float getPrefWidth() {
		if (mActor instanceof Layout) {
			if (mFixedSize) {
				return mActor.getWidth() + mPadding.left + mPadding.right;
			} else {
				return ((Layout)mActor).getPrefWidth() + mPadding.left + mPadding.right;
			}
		} else {
			return mPadding.left + mPadding.right;
		}
	}

	/**
	 * @return preferred height of the cell
	 */
	float getPrefHeight() {
		if (mActor instanceof Layout) {
			if (mFixedSize) {
				return mActor.getHeight() + mPadding.top + mPadding.bottom;
			}
			else {
				return ((Layout)mActor).getPrefHeight() + mPadding.top + mPadding.bottom;
			}
		} else {
			return mPadding.top + mPadding.bottom;
		}
	}

	/**
	 * Updates the size of the cell. Used when setting size depending for fill
	 * width or height. This does not set it as fixed, in fact it will not update
	 * the size if the cell is set as fixed.
	 * @param width new width of the cell
	 * @param height new height of the cell
	 */
	void updateSize(float width, float height) {
		if (!mFixedSize && mActor != null) {
			float actorWidth = width - getPadLeft() - getPadRight();
			float actorHeight = height - getPadBottom() - getPadTop();

			if (mActor instanceof AlignTable) {
				((AlignTable) mActor).updateSize(actorWidth, actorHeight);
			} else {
				setSize(actorWidth, actorHeight);

				if (mActor instanceof Layout) {
					((Layout) mActor).invalidate();
				}

				if (mBoxShape && !mFillWidth && !mFillHeight) {
					mWidthBeforeFill = mActor.getWidth();
					mHeightBeforeFill = mActor.getHeight();

					if (mActor.getWidth() < mActor.getHeight()) {
						mActor.setWidth(mActor.getHeight());
					} else if (mActor.getHeight() < mActor.getWidth()) {
						mActor.setHeight(mActor.getWidth());
					}
				}
			}
		}
	}


	/**
	 * Call this to layout the cell
	 * @param startPos starting position of the cell
	 * @param size available size for the cell
	 */
	void layout(Vector2 startPos, Vector2 size) {
		if (mActor == null) {
			return;
		}

		Vector2 offset = Pools.vector2.obtain();
		offset.set(startPos);
		offset.x += getPadLeft();

		// Horizontal
		if (mAlign.horizontal == Horizontal.RIGHT) {
			offset.x += size.x - getWidth();
		} else if (mAlign.horizontal == Horizontal.CENTER) {
			offset.x += size.x * 0.5f - getWidth() * 0.5f;
		}

		// Vertical
		if (mAlign.vertical == Vertical.BOTTOM) {
			offset.y = startPos.y + getPadBottom();
		} else if (mAlign.vertical == Vertical.TOP) {
			offset.y = startPos.y + size.y - mActor.getHeight() - getPadTop();
		} else if (mAlign.vertical == Vertical.MIDDLE) {
			offset.y = startPos.y + (size.y - mActor.getHeight() + getPadBottom() - getPadTop()) * 0.5f;
		}

		mActor.setPosition((int)offset.x, (int)offset.y);

		if (mActor instanceof AlignTable) {
			((AlignTable) mActor).layout();
		}

		Pools.vector2.free(offset);

	}

	/**
	 * @return width of the cell
	 */
	public float getWidth() {
		if (mActor != null) {
			return mActor.getWidth() + getPadLeft() + getPadRight();
		} else {
			return getPadLeft() + getPadRight();
		}
	}

	/**
	 * @return height of the cell
	 */
	public float getHeight() {
		if (mActor != null) {
			return mActor.getHeight() + getPadTop() + getPadBottom();
		} else {
			return getPadTop() + getPadBottom();
		}
	}

	/**
	 * Calculates the size of the cell
	 */
	void calculateSize() {
		if (mActor == null) {
			return;
		}

		if (mBoxShape && !mFillWidth && !mFillHeight) {
			mActor.setWidth(mWidthBeforeFill);
			mActor.setHeight(mHeightBeforeFill);
		}

		if (mActor instanceof AlignTable) {
			((AlignTable)mActor).calculateSize();
		}
		else if (mActor instanceof Layout) {
			((Layout)mActor).validate();
		}
	}

	/**
	 * @return the name of the actor inside, null if no actor is inside or no name has been set
	 */
	String getName() {
		if (mActor != null) {
			return mActor.getName();
		} else {
			return null;
		}
	}

	/**
	 * @param actor the actor to test if it's here
	 * @return true if the cell contains the specified actor
	 */
	boolean containsActor(Actor actor) {
		return mActor == actor;
	}

	/**
	 * @return actor of the cell
	 */
	Actor getActor() {
		return mActor;
	}


	/** Actor in the cell */
	private Actor mActor = null;
	/** Alignment of the cell */
	private Align mAlign = new Align(Horizontal.LEFT, Vertical.MIDDLE);
	/** If the cell is scalable */
	/** If the cell shall fill the width of the row */
	private boolean mFillWidth = false;
	/** Old width before filling the width */
	private float mWidthBeforeFill = 0;
	/** If the cell shall fill the height of the row */
	private boolean mFillHeight = false;
	/** Old height before filling the height */
	private float mHeightBeforeFill = 0;
	/** If the cell uses fixed size */
	private boolean mFixedSize = false;
	/** If the cell should be of box shape */
	private boolean mBoxShape = false;

	// Padding
	/** Padding for this cell */
	private Padding mPadding = new Padding();
}

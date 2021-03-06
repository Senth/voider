package com.spiddekauga.utils.scene.ui;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.Layout;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;

/**
 * Wrapper for a cell. Contains both the actor in the cell and align information
 */
public class Cell implements Poolable, IPadding<Cell> {
private Actor mActor = null;
private Align mAlign = new Align(Horizontal.LEFT, Vertical.MIDDLE);
/** If the cell shall fill the width of the row */
private boolean mFillWidth = false;
/** Old width before filling the width */
private float mWidthBeforeFill = 0;
/** If the cell shall fill the height of the row */
private boolean mFillHeight = false;
/** Old height before filling the height */
private float mHeightBeforeFill = 0;
/** If the cell uses fixed height */
private boolean mFixedHeight = false;
/** If the cell uses fixed width */
private boolean mFixedWidth = false;
/** If the cell should be of box shape */
private boolean mBoxShape = false;
/** If the cell should keep the aspect ratio when resizing */
private boolean mKeepAspectRatio = false;
/** Width of the cell, only used when mFillWidth and mFixedWidth is true */
private float mCellWidth = 0;
/** Height of the cell, only used when mFillHeight and mFixedHeight is true */
private float mCellHeight = 0;
/** Aspect ratio of the cell */
private float mAspectRatio = 1f;
private Padding mPadding = new Padding();

/**
 * Sets the horizontal alignment of this cell. If applicable it will also set the alignment of the
 * actor inside.
 * @param horizontal the horizontal alignment
 * @return this cell for chaining
 */

public Cell setAlign(Horizontal horizontal) {
	return setAlign(horizontal, mAlign.vertical);
}

/**
 * Sets the alignment of this cell. If applicable it will also set the alignment of the actor
 * inside.
 * @param horizontal the horizontal alignment
 * @param vertical the vertical alignment
 * @return this cell for chaining
 */
public Cell setAlign(Horizontal horizontal, Vertical vertical) {
	mAlign.horizontal = horizontal;
	mAlign.vertical = vertical;
	if (mActor instanceof Label) {
		((Label) mActor).setAlignment(Align.toLibgdxAlign(horizontal, vertical));
	}
	return this;
}

/**
 * Sets the vertical alignment of this cell. If applicable it will also set the alignment of the
 * actor inside.
 * @param vertical the vertical alignment
 * @return this cell for chaining
 */
public Cell setAlign(Vertical vertical) {
	return setAlign(mAlign.horizontal, vertical);
}

/**
 * Sets if the cell should be of box shape
 * @param boxShaped set to true to make the actor be shaped as a box
 * @return this cell for chaining
 * @throws UnsupportedOperationException if {@link #setKeepAspectRatio(boolean)} is used
 */

public Cell setBoxShaped(boolean boxShaped) {
	if (mKeepAspectRatio) {
		throw new UnsupportedOperationException("box shape cannot be used together with keep aspect ratio!");
	} else if (mFillHeight && mFillWidth) {
		throw new UnsupportedOperationException("box shape cannot be used when both fill height and fill width is used");
	}

	mBoxShape = boxShaped;
	return this;
}


/**
 * Sets the size of the cell. This will change the preferred size of the cell. Use #resetSize() to
 * reset the size to the actual preferred size
 * @param width new fixed width of the cell
 * @param height new fixed height of the cell
 * @return this cell for chaining
 * @see #setWidth(float)
 * @see #setHeight(float)
 * @see #resetSize()
 */
public Cell setSize(float width, float height) {
	setWidth(width);
	setHeight(height);
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
	resetHeight();
	resetWidth();
	return this;
}

/**
 * Resets the height of the cell to the original preferred size
 * @return this cell for chaining
 */
public Cell resetHeight() {
	mFixedHeight = false;
	if (mActor instanceof Layout) {
		mActor.setHeight((int) ((Layout) mActor).getPrefHeight());

		if (mActor instanceof AlignTable) {
			((AlignTable) mActor).setKeepHeight(false);
		}
	}
	return this;
}

/**
 * Resets the width of the cell to the original preferred size
 * @return this cell for chaining
 */
public Cell resetWidth() {
	mFixedWidth = false;
	if (mActor instanceof Layout) {
		mActor.setWidth((int) ((Layout) mActor).getPrefWidth());

		if (mActor instanceof AlignTable) {
			((AlignTable) mActor).setKeepWidth(false);
		}
	}
	return this;
}

/**
 * @return true if this cell has fixed width. I.e. it has changed its width externally.
 */
boolean isFixedWidth() {
	return mFixedWidth;
}

/**
 * Sets the cell as fixed width. Can be used together with {@link #setFillWidth(boolean)} so that
 * the cell is actually bigger than the actor inside it.
 * @param fixedWidth set to true to make it fixed width
 * @return this for chaining
 */
public Cell setFixedWidth(boolean fixedWidth) {
	mFixedWidth = fixedWidth;
	return this;
}

/**
 * @return true if this cell has fixed height. I.e. it has changed its height externally.
 */
boolean isFixedHeight() {
	return mFixedHeight;
}

/**
 * Sets the cell as fixed height. Can be used together with {@link #setFillHeight(boolean)} so that
 * the cell is actually bigger than the actor inside it.
 * @param fixedHeight set to true to make it fixed height
 * @return this for chaining
 */
public Cell setFixedHeight(boolean fixedHeight) {
	mFixedHeight = fixedHeight;
	return this;
}

/**
 * @return true if the cell is visible
 */
public boolean isVisible() {
	return mActor == null || mActor.isVisible();
}

/**
 * Sets if the cell shall fill the remaining width of the row.
 * @param fillWidth set to true if the cell shall fill the remaining width of the row
 * @return this cell for chaining
 */
public Cell setFillWidth(boolean fillWidth) {
	if (mKeepAspectRatio && mFillHeight) {
		throw new UnsupportedOperationException("Cannot use fill width while both fill height and keep aspect ratio is used");
	} else if (mBoxShape && mFillHeight) {
		throw new UnsupportedOperationException("Cannot use fill width while both fill height and box shape is used");
	}

	mFillWidth = fillWidth;

	if (mFillWidth && mActor != null) {
		mWidthBeforeFill = mActor.getWidth();
	}

	return this;
}

/**
 * @return true if the cell shall fill the remaining width of the row
 */
public boolean shallFillWidth() {
	return mFillWidth;
}

/**
 * Sets if the cell shall fill the remaining height of the row. This works for all cells in the
 * row.
 * @param fillHeight true if the cell shall fill the remaining height of the row.
 * @return this cell for chaining
 */
public Cell setFillHeight(boolean fillHeight) {
	if (mKeepAspectRatio && mFillWidth) {
		throw new UnsupportedOperationException("Cannot use fill height while both fill width and keep aspect ratio is used");
	} else if (mBoxShape && mFillWidth) {
		throw new UnsupportedOperationException("Cannot use fill height while both fill width and box shape is used");
	}

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
 * @return preferred width of the cell
 */
public float getPrefWidth() {
	if (mActor instanceof Layout) {
		if (mFixedWidth) {
			return mActor.getWidth() + getPadX();
		} else {
			return ((Layout) mActor).getPrefWidth() + getPadX();
		}
	} else {
		return getPadX();
	}
}

/**
 * @return preferred height of the cell
 */
public float getPrefHeight() {
	if (mActor instanceof Layout) {
		if (mFixedHeight) {
			return mActor.getHeight() + getPadY();
		} else {
			return ((Layout) mActor).getPrefHeight() + getPadY();
		}
	} else {
		return getPadY();
	}
}

/**
 * Sets the cell to keep it's aspect ratio
 * @param keepAspectRatio true if the cell should keep it's aspect ratio, false otherwise
 * @return this for chaining
 * @throws UnsupportedOperationException if {@link #setBoxShaped(boolean)}, or both {@link
 *                                       #setFillHeight(boolean)} and {@link #setFillWidth(boolean)}
 *                                       is enabled.
 */
public Cell setKeepAspectRatio(boolean keepAspectRatio) {
	if (mBoxShape) {
		throw new UnsupportedOperationException("Cannot keep aspect ratio while box shape is enabled");
	} else if (mFillHeight && mFillWidth) {
		throw new UnsupportedOperationException("Cannot keep aspect ratio while both fill height and fill width is enabled");
	}

	mKeepAspectRatio = keepAspectRatio;
	return this;
}

/**
 * Calculates the size of the cell
 */
void calculatePreferredSize() {
	if (mActor == null) {
		return;
	}

	if (mActor instanceof AlignTable) {
		((AlignTable) mActor).calculatePreferredSize();
	} else if (mActor instanceof Layout) {
		try {
			((Layout) mActor).validate();
		} catch (ArrayIndexOutOfBoundsException e) {
			// Do nothing...
		}
	}

	if (mBoxShape && !mFillWidth && !mFillHeight) {
		mActor.setWidth((int) mWidthBeforeFill);
		mActor.setHeight((int) mHeightBeforeFill);
	}

	if (mKeepAspectRatio) {
		if (mActor instanceof Layout) {
			mAspectRatio = ((Layout) mActor).getPrefWidth() / ((Layout) mActor).getPrefHeight();
		} else {
			mAspectRatio = mActor.getWidth() / mActor.getHeight();
		}
	}
}

/**
 * Calculates the actual size
 */
void calculateActualSize() {
	if (mActor == null) {
		return;
	}

	if (mActor instanceof AlignTable) {
		((AlignTable) mActor).calculateActualSize();
	}
}

/**
 * Updates the size of the cell. Used when setting size depending for fill width or height. This
 * does not set it as fixed, in fact it will not update the size if the cell is set as fixed.
 * @param width new width of the cell
 * @param height new height of the cell
 */
void updateSize(float width, float height) {
	if (mActor != null) {
		float availableWidth = width - getPadX();
		float availableHeight = height - getPadY();
		float actorWidth = mFixedWidth ? mActor.getWidth() : availableWidth;
		float actorHeight = mFixedHeight ? mActor.getHeight() : availableHeight;

		// When cell is larger than the actual actor
		if (mFixedWidth && mFillWidth) {
			mCellWidth = availableWidth;
		}
		if (mFixedHeight && mFillHeight) {
			mCellHeight = availableHeight;
		}

		if (mActor instanceof AlignTable) {
			((AlignTable) mActor).updateSize(actorWidth, actorHeight);
		} else {
			mActor.setSize((int) actorWidth, (int) actorHeight);

			if (mBoxShape && !mFillWidth && !mFillHeight) {
				mWidthBeforeFill = mActor.getWidth();
				mHeightBeforeFill = mActor.getHeight();

				if (mActor.getWidth() < mActor.getHeight()) {
					mActor.setWidth((int) mActor.getHeight());
				} else if (mActor.getHeight() < mActor.getWidth()) {
					mActor.setHeight((int) mActor.getWidth());
				}
			} else if (mKeepAspectRatio) {
				mWidthBeforeFill = mActor.getWidth();
				mHeightBeforeFill = mActor.getHeight();

				float correctWidth = width;
				float correctHeight = height;

				float newAspectRatio = width / height;

				// Correct height
				if (mFillWidth) {
					// Wider than it should be
					if (newAspectRatio < mAspectRatio || newAspectRatio > mAspectRatio) {
						correctHeight = correctWidth / mAspectRatio;
					}
				}
				// Correct width
				else if (mFillHeight) {
					// Wider than it should be
					if (newAspectRatio < mAspectRatio || newAspectRatio > mAspectRatio) {
						correctWidth = mAspectRatio * correctHeight;
					}
				}
				// Correct height
				else if (mFixedWidth) {
					correctHeight = mActor.getWidth() / mAspectRatio;
				}
				// Correct width
				else if (mFixedHeight) {
					correctWidth = mActor.getHeight() * mAspectRatio;
				}

				mActor.setWidth((int) correctWidth);
				mActor.setHeight((int) correctHeight);
			}
		}
	} else {
		mCellWidth = width;
		mCellHeight = height;
	}
}

/**
 * Call this to layout the cell
 * @param startPos starting position of the cell
 * @param availableSize available size for the cell
 */
void layout(Vector2 startPos, Vector2 availableSize) {
	if (mActor == null) {
		return;
	}

	Vector2 offset = new Vector2();
	offset.set(startPos);

	// Horizontal
	if (mAlign.horizontal == Horizontal.LEFT) {
		offset.x += getPadLeft();
	} else if (mAlign.horizontal == Horizontal.RIGHT) {
		offset.x += availableSize.x - mActor.getWidth() - getPadRight();
	} else if (mAlign.horizontal == Horizontal.CENTER) {
		offset.x += (availableSize.x - mActor.getWidth() - getPadLeft() + getPadRight()) * 0.5f;
		offset.x += getPadLeft();
	}

	// Vertical
	if (mAlign.vertical == Vertical.BOTTOM) {
		offset.y += getPadBottom();
	} else if (mAlign.vertical == Vertical.TOP) {
		offset.y += availableSize.y - mActor.getHeight() - getPadTop();
	} else if (mAlign.vertical == Vertical.MIDDLE) {
		offset.y += (availableSize.y - mActor.getHeight() - getPadY()) * 0.5f;
		offset.y += getPadBottom();
	}

	mActor.setPosition((int) offset.x, (int) offset.y);

	if (mActor instanceof AlignTable) {
		((AlignTable) mActor).layout();
	}
}

/**
 * @return width of the cell
 */
public float getWidth() {
	if (mFillWidth && mFixedWidth) {
		return mCellWidth;
	}
	// Special case for labels as they need to be packed
	else if (mActor instanceof Label && !mFixedWidth && !mFillWidth) {
		return ((Label) mActor).getPrefWidth() + getPadX();
	} else if (mActor != null) {
		return mActor.getWidth() + getPadX();
	} else {
		return mCellWidth + getPadX();
	}
}

/**
 * Sets the width of the cell. Does not change the preferred width.
 * @param width new width of the cell. This will change the preferred size of the cell. Use
 * #resetSize() to reset the size to the actual preferred size.
 * @return this cell for chaining.
 * @see #setSize(float, float)
 * @see #setHeight(float)
 * @see #resetSize()
 */
public Cell setWidth(float width) {
	if (mActor != null) {
		mActor.setWidth((int) width);
		mFixedWidth = true;

		if (mActor instanceof AlignTable) {
			((AlignTable) mActor).setKeepWidth(true);
		}
	}
	return this;
}

/**
 * @return height of the cell
 */
public float getHeight() {
	if (mFillHeight && mFixedHeight) {
		return mCellHeight;
	}
	// Special case for labels as they need to be packed
	else if (mActor instanceof Label && !mFixedHeight) {
		return ((Label) mActor).getPrefHeight() + getPadY();
	} else if (mActor != null) {
		return mActor.getHeight() + getPadY();
	} else {
		return mCellHeight + getPadY();
	}
}

/**
 * Sets the height of the cell. Does not change the preferred height.
 * @param height new height of the cell. This will change the preferred size of the cell. Use
 * #resetSize() to reset the size to the actual preferred size.
 * @return this cell for chaining
 * @see #setSize(float, float)
 * @see #setWidth(float)
 * @see #resetSize()
 */
public Cell setHeight(float height) {
	if (mActor != null) {
		mActor.setHeight((int) height);
		mFixedHeight = true;

		if (mActor instanceof AlignTable) {
			((AlignTable) mActor).setKeepHeight(true);
		}
	}
	return this;
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
public Actor getActor() {
	return mActor;
}

/**
 * Sets the actor for the cell. Resizes the actor to the preferred size if its of size 0.
 * @param actor the actor for the cell
 * @return this cell for chaining
 */
Cell setActor(Actor actor) {
	mActor = actor;

	if (mActor instanceof Layout) {
		if (mActor.getHeight() == 0) {
			mActor.setHeight((int) ((Layout) mActor).getPrefHeight());
		}
		if (mActor.getWidth() == 0) {
			mActor.setWidth((int) ((Layout) mActor).getPrefWidth());
		}
	}

	return this;
}

/**
 * @return true if this cell is empty, i.e. doesn't have any actor
 */
boolean isEmpty() {
	return mActor == null;
}

@Override
public Cell setPad(float top, float right, float bottom, float left) {
	setPadLeft(left);
	setPadRight(right);
	setPadTop(top);
	setPadBottom(bottom);
	return this;
}

@Override
public Cell setPad(float pad) {
	setPadLeft(pad);
	setPadRight(pad);
	setPadTop(pad);
	setPadBottom(pad);

	return this;
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
	mFixedHeight = false;
	mFixedWidth = false;
	mKeepAspectRatio = false;
	mBoxShape = false;
	mCellHeight = 0;
	mCellWidth = 0;
	mAspectRatio = 1;
	mActor = null;
}

/**
 * Disposes the cell, the actor can be saved
 * @param disposeActor true if you want to call onDestroy() on the actor
 */
public void dispose(boolean disposeActor) {
	if (mActor != null) {
		mActor.remove();

		if (disposeActor) {
			mActor.clearActions();
			if (mActor instanceof AlignTable) {
				((AlignTable) mActor).dispose(true);
			} else if (mActor instanceof Disposable) {
				((Disposable) mActor).dispose();
			}
		}
	}

	mActor = null;
}

@Override
public Cell setPadLeft(float padLeft) {
	mPadding.left = padLeft;
	return this;
}

@Override
public Cell setPadRight(float padRight) {
	mPadding.right = padRight;
	return this;
}

@Override
public Cell setPad(Padding padding) {
	setPadLeft(padding.left);
	setPadRight(padding.right);
	setPadTop(padding.top);
	setPadBottom(padding.bottom);
	return this;
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
public Cell setPadTop(float padTop) {
	mPadding.top = padTop;
	return this;
}

@Override
public Padding getPad() {
	return mPadding;
}

@Override
public float getPadBottom() {
	return mPadding.bottom;
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
public float getPadLeft() {
	return mPadding.left;
}

@Override
public Cell setPadBottom(float padBottom) {
	mPadding.bottom = padBottom;
	return this;
}


// !!! Don't forget to add to reset() !!!
}

package com.spiddekauga.utils.scene.ui;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.utils.Layout;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.badlogic.gdx.utils.Pools;
import com.spiddekauga.utils.scene.ui.Align.Horizontal;
import com.spiddekauga.utils.scene.ui.Align.Vertical;

/**
 * Wrapper for a cell.
 * Contains both the actor in the cell and align information
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
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

	@Override
	public void reset() {
		if (mActor instanceof Disposable) {
			((Disposable) mActor).dispose();
		}
		mAlign.horizontal = Horizontal.LEFT;
		mAlign.vertical = Vertical.MIDDLE;
		mScalable = true;

		mPadding.reset();
		mScaledPadding.reset();
		mDynamicPadding = true;

		mFillHeight = false;
		mFillWidth = false;
		mWidthBeforeFill = 0;
		mHeightBeforeFill = 0;
	}

	/**
	 * Sets the padding values to be dynamic. I.e. they will scale depending on the
	 * current scale factor of the table. E.g. setPadLeft(10) and table.setScale(0.5f)
	 * will result in setPadLeft(5).
	 * @param dynamicPadding set to true to activate dynamic padding
	 * @return this cell for chaining
	 * @note Setting this to false does not work with scaling AlignTable
	 * depending on the size.
	 */
	public Cell setDynamicPadding(boolean dynamicPadding) {
		mDynamicPadding = dynamicPadding;

		if (mDynamicPadding) {
			mScaledPadding.left = mPadding.left * mActor.getScaleX();
			mScaledPadding.right = mPadding.right * mActor.getScaleX();
			mScaledPadding.top = mPadding.top * mActor.getScaleY();
			mScaledPadding.bottom = mPadding.bottom * mActor.getScaleY();
		}
		return this;
	}

	/**
	 * Sets the padding for left, right, top, bottom
	 * @param pad how much padding should be on the sides
	 * @return this cell for chaining
	 * @see #setDynamicPadding(boolean)
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
	 * @see #setDynamicPadding(boolean)
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
	 * @see #setDynamicPadding(boolean)
	 */
	public Cell setPadLeft(float padLeft) {
		mPadding.left = padLeft;

		if (mDynamicPadding) {
			mScaledPadding.left = mPadding.left * mActor.getScaleX();
		}

		return this;
	}

	/**
	 * Sets the padding to the right of the cell
	 * @param padRight how much padding should be on the right of the cell
	 * @return this cell for chaining
	 * @see #setDynamicPadding(boolean)
	 */
	public Cell setPadRight(float padRight) {
		mPadding.right = padRight;

		if (mDynamicPadding) {
			mScaledPadding.right = mPadding.right * mActor.getScaleX();
		}

		return this;
	}

	/**
	 * Sets the padding at the top of the cell
	 * @param padTop how much padding should be at the top of the cell
	 * @return this cell for chaining
	 * @see #setDynamicPadding(boolean)
	 */
	public Cell setPadTop(float padTop) {
		mPadding.top = padTop;

		if (mDynamicPadding) {
			mScaledPadding.top = mPadding.top * mActor.getScaleY();
		}

		return this;
	}

	/**
	 * Sets the padding at teh bottom of the cell
	 * @param padBottom how much padding should be at the bottom of the cell
	 * @return this cell for chaining
	 * @see #setDynamicPadding(boolean)
	 */
	public Cell setPadBottom(float padBottom) {
		mPadding.bottom = padBottom;

		if (mDynamicPadding) {
			mScaledPadding.bottom = mPadding.bottom * mActor.getScaleY();
		}

		return this;
	}

	/**
	 * @return padding to the left of this cell. If dynamic padding is on this
	 * will return the scaled padding instead.
	 * @see #setDynamicPadding(boolean)
	 */
	public float getPadLeft() {
		return mDynamicPadding ? mScaledPadding.left : mPadding.left;
	}

	/**
	 * @return padding to the right of this cell. If dynamic padding is on this
	 * will return the scaled padding instead.
	 * @see #setDynamicPadding(boolean)
	 */
	public float getPadRight() {
		return mDynamicPadding ? mScaledPadding.right : mPadding.right;
	}

	/**
	 * @return padding to the top of this cell. If dynamic padding is on this
	 * will return the scaled padding instead.
	 * @see #setDynamicPadding(boolean)
	 */
	public float getPadTop() {
		return mDynamicPadding ? mScaledPadding.top : mPadding.top;
	}

	/**
	 * @return padding to the bottom of this cell. If dynamic padding is on this
	 * will return the scaled padding instead.
	 * @see #setDynamicPadding(boolean)
	 */
	public float getPadBottom() {
		return mDynamicPadding ? mScaledPadding.bottom : mPadding.bottom;
	}

	/**
	 * Sets if the cell can be scaled or not. Cells are scalable by default.
	 * Can be good to turn off for buttons with text.
	 * @param scalable true if the cell should be scalable. If set to false this
	 * will reset any scale.
	 * @return this cell for chaining
	 */
	public Cell setScalable(boolean scalable) {
		if (!scalable) {
			setScaleX(1);
			setScaleY(1);
		}

		mScalable = scalable;

		return this;
	}

	/**
	 * @return true if the cell is scalable
	 */
	public boolean isScalable() {
		return mScalable;
	}

	/**
	 * @return true if the cell is visible
	 */
	public boolean isVisible() {
		return mActor.isVisible();
	}

	/**
	 * Sets if the cell shall fill the remaining width of the row.
	 * @todo only works with on cell per row for now.
	 * @param fillWidth set to true if the cell shall fill the remaining width of the row
	 * @return this cell for chaining
	 */
	public Cell setFillWidth(boolean fillWidth) {
		mFillWidth = true;

		if (mFillWidth) {
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

		if (mFillHeight) {
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

		if (mActor instanceof AlignTable) {
			mScalable = ((AlignTable) mActor).isScalable();
		}

		return this;
	}

	/**
	 * @return preferred width of the cell
	 */
	float getPrefWidth() {
		if (mActor instanceof Layout) {
			return ((Layout)mActor).getPrefWidth() + mPadding.left + mPadding.right;
		}
		return 0;
	}

	/**
	 * @return preferred height of the cell
	 */
	float getPrefHeight() {
		if (mActor instanceof Layout) {
			return ((Layout)mActor).getPrefHeight() + mPadding.top + mPadding.bottom;
		}
		return 0;
	}

	/**
	 * Call this to layout the cell
	 * @param startPos starting positino of the cell
	 * @param size available size for the cell
	 */
	void layout(Vector2 startPos, Vector2 size) {
		Vector2 offset = Pools.obtain(Vector2.class);
		offset.set(startPos);
		offset.x += getPadLeft();

		if (mFillWidth) {
			setWidth(size.x - getPadLeft() - getPadRight());
		}
		if (mFillHeight) {
			setHeight(size.y - getPadTop() - getPadBottom());
		}

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


		Pools.free(offset);
	}

	/**
	 * @return width of the cell
	 */
	float getWidth() {
		return mActor.getWidth() + getPadLeft() + getPadRight();
	}

	/**
	 * @return height of the cell
	 */
	float getHeight() {
		return mActor.getHeight() + getPadTop() + getPadBottom();
	}

	/**
	 * Calculates the size of the cell
	 */
	void calculateSize() {
		// Reset width/height if it is set fill width/height
		if (mFillHeight) {
			mActor.setHeight(mHeightBeforeFill);
		}
		if (mFillWidth) {
			mActor.setWidth(mWidthBeforeFill);
		}
		if (mActor instanceof Layout) {
			((Layout)mActor).validate();
		}
		if (mActor instanceof AlignTable) {
			mScalable = ((AlignTable) mActor).isScalable();
		}
	}

	/**
	 * Sets the width of the cell. Don't use this together with scaling!
	 * Does not change the preferred width.
	 * @param width new width of the cell.
	 */
	void setWidth(float width) {
		mActor.setWidth(width);
	}

	/**
	 * Sets the height of the cell. Don't use this together with scaling!
	 * Does not change the preferred height.
	 * @param height new height of the cell.
	 */
	void setHeight(float height) {
		mActor.setHeight(height);
	}

	/**
	 * Sets the scaling factor for x.
	 * @param scaleX the x scaling factor
	 * @return this cell for chaining
	 * @note Scaling only works if this cell is scalable (on by default)
	 */
	Cell setScaleX(float scaleX) {
		if (mScalable) {
			if (mDynamicPadding) {
				mScaledPadding.left = mPadding.left * scaleX;
				mScaledPadding.right = mPadding.right * scaleX;
			}

			if (mActor instanceof Layout) {
				mActor.setWidth(((Layout) mActor).getPrefWidth() * scaleX);
			}
			mActor.setScaleX(scaleX);
		}

		return this;
	}

	/**
	 * Sets the scaling factor for y
	 * @param scaleY the y scaling factor
	 * @return this cell for chaining
	 * @note Scaling only works if this cell is scalable (on by default)
	 */
	Cell setScaleY(float scaleY) {
		if (mScalable) {
			if (mDynamicPadding) {
				mScaledPadding.top = mPadding.top * scaleY;
				mScaledPadding.bottom = mPadding.bottom * scaleY;
			}

			if (mActor instanceof Layout) {
				mActor.setHeight(((Layout) mActor).getPrefHeight() * scaleY);
			}
			mActor.setScaleY(scaleY);
		}

		return this;
	}

	/** Actor in the cell */
	private Actor mActor = null;
	/** Alignment of the cell */
	private Align mAlign = new Align(Horizontal.LEFT, Vertical.MIDDLE);
	/** If the cell is scalable */
	private boolean mScalable = true;
	/** If the cell shall fill the width of the row */
	private boolean mFillWidth = false;
	/** Old width before filling the width */
	private float mWidthBeforeFill = 0;
	/** If the cell shall fill the height of the row */
	private boolean mFillHeight = false;
	/** Old height before filling the height */
	private float mHeightBeforeFill = 0;

	// Padding
	/** If the padding can be scaled */
	private boolean mDynamicPadding = true;
	/** Padding for this cell */
	private Padding mPadding = new Padding();
	/** Scaled padding */
	private Padding mScaledPadding = new Padding();
}

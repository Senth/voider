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

		mPadLeft = 0;
		mPadRight = 0;
		mPadTop = 0;
		mPadBottom = 0;
		mPadScaleLeft = 0;
		mPadScaleRight = 0;
		mPadScaleTop = 0;
		mPadScaleBottom = 0;
		mDynamicPadding = false;
	}

	/**
	 * Sets the padding values to be dynamic. I.e. they will scale depending on the
	 * current scale factor of the table. E.g. setPadLeft(10) and table.setScale(0.5f)
	 * will result in setPadLeft(5).
	 * @param dynamicPadding set to true to activate dynamic padding
	 * @return this cell for chaining
	 */
	public Cell setDynamicPadding(boolean dynamicPadding) {
		mDynamicPadding = dynamicPadding;
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
	 * Sets the padding to the left of the cell
	 * @param padLeft how much padding should be to the left of the cell
	 * @return this cell for chaining
	 * @see #setDynamicPadding(boolean)
	 */
	public Cell setPadLeft(float padLeft) {
		mPadLeft = padLeft;

		if (mDynamicPadding) {
			mPadScaleLeft = mPadLeft * mActor.getScaleX();
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
		mPadRight = padRight;

		if (mDynamicPadding) {
			mPadScaleRight = mPadRight * mActor.getScaleX();
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
		mPadTop = padTop;

		if (mDynamicPadding) {
			mPadScaleTop = mPadTop * mActor.getScaleY();
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
		mPadBottom = padBottom;

		if (mDynamicPadding) {
			mPadScaleBottom = mPadBottom * mActor.getScaleY();
		}

		return this;
	}

	/**
	 * @return padding to the left of this cell. If dynamic padding is on this
	 * will return the scaled padding instead.
	 * @see #setDynamicPadding(boolean)
	 */
	public float getPadLeft() {
		return mDynamicPadding ? mPadScaleLeft : mPadLeft;
	}

	/**
	 * @return padding to the right of this cell. If dynamic padding is on this
	 * will return the scaled padding instead.
	 * @see #setDynamicPadding(boolean)
	 */
	public float getPadRight() {
		return mDynamicPadding ? mPadScaleRight : mPadRight;
	}

	/**
	 * @return padding to the top of this cell. If dynamic padding is on this
	 * will return the scaled padding instead.
	 * @see #setDynamicPadding(boolean)
	 */
	public float getPadTop() {
		return mDynamicPadding ? mPadScaleTop : mPadTop;
	}

	/**
	 * @return padding to the bottom of this cell. If dynamic padding is on this
	 * will return the scaled padding instead.
	 * @see #setDynamicPadding(boolean)
	 */
	public float getPadBottom() {
		return mDynamicPadding ? mPadScaleBottom : mPadBottom;
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
	 * Sets the actor for the cell
	 * @param actor the actor for the cell
	 * @return this cell for chaining
	 */
	Cell setActor(Actor actor) {
		this.mActor = actor;
		return this;
	}

	/**
	 * @return preferred width of the cell
	 */
	float getPrefWidth() {
		if (mActor instanceof Layout) {
			return ((Layout)mActor).getPrefWidth() + mPadLeft + mPadRight;
		}
		return 0;
	}

	/**
	 * @return preferred height of the cell
	 */
	float getPrefHeight() {
		if (mActor instanceof Layout) {
			return ((Layout)mActor).getPrefHeight() + mPadTop + mPadBottom;
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
		if (mActor instanceof Layout) {
			((Layout)mActor).validate();
		}
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
				mPadScaleLeft = mPadLeft * scaleX;
				mPadScaleRight = mPadRight * scaleX;
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
				mPadScaleTop = mPadTop * scaleY;
				mPadScaleBottom = mPadBottom * scaleY;
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
	/** If the cell is scaleable */
	private boolean mScalable = true;

	// Padding
	private boolean mDynamicPadding = false;
	/** Padding to the left of the cell */
	private float mPadLeft = 0;
	/** Padding to the right of the cell */
	private float mPadRight = 0;
	/** Padding at the top of the cell */
	private float mPadTop = 0;
	/** Padding at the bottom of the cell */
	private float mPadBottom = 0;
	/** Scale padding to the left, when dynamic padding is on */
	private float mPadScaleLeft = 0;
	/** Scale padding to the right, when dynamic padding is on */
	private float mPadScaleRight = 0;
	/** Scale padding at the top, when dynamic padding is on */
	private float mPadScaleTop = 0;
	/** Scale padding at the bottom, when dynamic padding is on */
	private float mPadScaleBottom = 0;
}

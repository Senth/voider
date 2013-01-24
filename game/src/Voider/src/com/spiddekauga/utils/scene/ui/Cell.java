package com.spiddekauga.utils.scene.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.Layout;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Pool.Poolable;

/**
 * Wrapper for a cell.
 * Contains both the actor in the cell and align information
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class Cell implements Poolable {
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
	 * Sets the alignment of this cell
	 * @param align how the cell shall be aligned.
	 * @return this cell for chaining
	 * @note this will only have an effect if a row is equallySpaced.
	 */
	public Cell setAlign(int align) {
		mAlign = align;
		return this;
	}

	/**
	 * @return preferred width of the cell
	 */
	float getPrefWidth() {
		if (mActor instanceof Layout) {
			return ((Layout)mActor).getPrefWidth();
		}
		return 0;
	}

	/**
	 * @return preferred height of the cell
	 */
	float getPrefHeight() {
		if (mActor instanceof Layout) {
			return ((Layout)mActor).getPrefHeight();
		}
		return 0;
	}

	/**
	 * @return width of the cell
	 */
	float getWidth() {
		return mActor.getWidth();
	}

	/**
	 * @return height of the cell
	 */
	float getHeight() {
		return mActor.getHeight();
	}

	@Override
	public void reset() {
		if (mActor instanceof Disposable) {
			((Disposable) mActor).dispose();
		}
		mAlign = Align.LEFT | Align.MIDDLE;
	}

	/** Actor in the cell */
	Actor mActor = null;
	/** Align information */
	int mAlign = Align.LEFT | Align.MIDDLE;
}

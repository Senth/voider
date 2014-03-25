package com.spiddekauga.utils.scene.ui;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Disposable;
import com.spiddekauga.voider.utils.Pools;

/**
 * Widget for displaying ratings that can be changed if set
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class RatingWidget extends WidgetGroup implements Disposable {

	/**
	 * Constructor which sets the filled and empty star drawables and
	 * the number of stars to show. Rating can be changed by pressing
	 * on the stars
	 * @param filledStar how a filled star looks
	 * @param emptyStar how an empty star looks
	 * @param stars number of stars used in the rating
	 * @see #RatingWidget(Drawable, Drawable, int, Touchable)
	 */
	public RatingWidget(Drawable filledStar, Drawable emptyStar, int stars) {
		this(filledStar, emptyStar, stars, Touchable.enabled);
	}

	/**
	 * Constructor which sets the filled and empty star drawables and
	 * the number of stars to show
	 * @param filledStar how a filled star looks
	 * @param emptyStar how an empty star looks
	 * @param ratingMax number of stars used in the rating
	 * @param touchable if the rating can be changed by pressing on the stars
	 */
	public RatingWidget(Drawable filledStar, Drawable emptyStar, int ratingMax, Touchable touchable) {
		if (filledStar == null) {
			throw new IllegalArgumentException("filledStar cannot be null");
		} else if (emptyStar == null) {
			throw new IllegalArgumentException("emptyStar cannot be null");
		} else if (ratingMax <= 0) {
			throw new IllegalArgumentException("the max rating has to be more than 0");
		} else if (filledStar.getMinWidth() != emptyStar.getMinWidth() && filledStar.getMinHeight() != emptyStar.getMinHeight()) {
			throw new IllegalArgumentException("filled and empty star should be of the same size");
		}

		mFilledStar = filledStar;
		mEmptyStar = emptyStar;
		mStars = new Image[ratingMax];
		mClickListeners = new ClickListener[ratingMax];

		addActor(mTable);
		setTouchable(touchable);
		createImageButtons();
	}

	@Override
	public void dispose() {
		if (mChangeListeners != null) {
			Pools.arrayList.free(mChangeListeners);
		}
	}

	/**
	 * Sets the rating/number of stars. This will send a change event for all IRatingListener
	 * @param rating new rating of the widget
	 */
	public void setRating(int rating) {
		if (rating < 0 || rating > mStars.length) {
			throw new IllegalArgumentException("rating has to be between 0 and " + mStars.length);
		}


		// Special case, if we want to set the rating to which it already is, set it to 0 instead
		if (mRatingCurrent == rating) {
			mRatingCurrent = 0;
		} else {
			mRatingCurrent = rating;
		}

		sendChangeEvent();
	}

	/**
	 * Updates the number of shown stars. This does not change the rating
	 * just how many stars are currently filled
	 * @param cFilledStars number of stars to show.
	 */
	private void updateFilledStars(int cFilledStars) {
		for (int i = 0; i < mStars.length; ++i) {
			if (i < cFilledStars) {
				mStars[i].setDrawable(mFilledStar);
			} else {
				mStars[i].setDrawable(mEmptyStar);
			}
		}
		mTable.invalidateHierarchy();
	}

	@Override
	public void draw(Batch batch, float parentAlpha) {
		int mouseOverRating = getRatingMouseOver();
		if (mouseOverRating != -1) {
			updateFilledStars(mouseOverRating);
		} else {
			updateFilledStars(mRatingCurrent);
		}

		super.draw(batch, parentAlpha);
	}

	/**
	 * @return the rating the mouse is currently over, -1 if not over any rating
	 */
	private int getRatingMouseOver() {
		if (getTouchable() == Touchable.enabled || getTouchable() == Touchable.childrenOnly) {
			for (int i = 0; i < mClickListeners.length; ++i) {
				if (mClickListeners[i].isOver()) {
					return i+1;
				}
			}
		}

		return -1;
	}

	/**
	 * Add a rating changed listener. These are only called when the rating
	 * is changed when a star is pressed, i.e. not when {@link #setRating(int)} is called.
	 * @param listener the listener to add
	 */
	public void addListener(IRatingListener listener) {
		mChangeListeners.add(listener);
	}

	/**
	 * Removes a listener
	 * @param listener removes this listener if it exists
	 */
	public void removeListener(IRatingListener listener) {
		mChangeListeners.remove(listener);
	}

	/**
	 * Sends on change event to all rating listeners
	 */
	private void sendChangeEvent() {
		for (IRatingListener ratingListener : mChangeListeners) {
			ratingListener.onRatingChange(mRatingCurrent);
		}
	}

	/**
	 * Creates all the star image buttons
	 */
	private void createImageButtons() {
		for (int i = 0; i < mStars.length; ++i) {
			mStars[i] = new Image();
			mStars[i].setSize(mFilledStar.getMinWidth(), mFilledStar.getMinHeight());
			mTable.add(mStars[i]);
			mClickListeners[i] = new ClickListenerImage(i+1);
		}
	}

	/**
	 * Click listener
	 */
	private class ClickListenerImage extends ClickListener {
		/**
		 * @param rating the rating the star is equal to
		 */
		ClickListenerImage(int rating) {
			mRating = rating;
		}

		@Override
		public void clicked(InputEvent event, float x, float y) {
			setRating(mRating);
		}

		/** Index of the star */
		int mRating;
	}

	/** Current rating */
	private int mRatingCurrent = 0;
	/** Drawable used for filled stars */
	private Drawable mFilledStar = null;
	/** Drawable used for empty stars */
	private Drawable mEmptyStar = null;
	/** All stars */
	private Image[] mStars = null;
	/** Click listeners for determining hover over and clicking on a star */
	private ClickListener[] mClickListeners = null;
	/** Table where the stars are located */
	private AlignTable mTable = new AlignTable();
	/** Rating listeners */
	@SuppressWarnings("unchecked")
	private ArrayList<IRatingListener> mChangeListeners = Pools.arrayList.obtain();
}

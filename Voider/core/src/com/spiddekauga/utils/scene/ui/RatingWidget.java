package com.spiddekauga.utils.scene.ui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

import java.util.ArrayList;

/**
 * Widget for displaying ratings that can be changed if set
 */
public class RatingWidget extends WidgetGroup {

/** Rating style */
private RatingWidgetStyle mStyle = null;
/** Current rating */
private int mRatingCurrent = 0;
/** All stars */
private Image[] mStars = null;
/** Click listeners for determining hover over and clicking on a star */
private ClickListener[] mClickListeners = null;
/** Table where the stars are located */
private AlignTable mTable = new AlignTable();
/** Rating listeners */
private ArrayList<IRatingListener> mChangeListeners = new ArrayList<>();

/**
 * Constructor which sets the style for the rating and number of stars. Rating can be changed by
 * pressing on the stars
 * @param style how the empty and filled stars will look like
 * @param ratingMax number of stars used in the rating
 * @see #RatingWidget(Drawable, Drawable, int, Touchable)
 */
public RatingWidget(RatingWidgetStyle style, int ratingMax) {
	this(style, ratingMax, Touchable.enabled);
}

/**
 * Constructor which sets the style for the rating and number of stars.
 * @param style how the empty and filled stars will look like
 * @param ratingMax number of stars used in the rating
 * @param touchable if the rating can be changed by pressing on the stars
 * @see #RatingWidget(Drawable, Drawable, int, Touchable)
 */
public RatingWidget(RatingWidgetStyle style, int ratingMax, Touchable touchable) {
	if (style.checked == null) {
		throw new IllegalArgumentException("filledStar cannot be null");
	} else if (style.empty == null) {
		throw new IllegalArgumentException("emptyStar cannot be null");
	} else if (ratingMax <= 0) {
		throw new IllegalArgumentException("the max rating has to be more than 0");
	} else if (style.checked.getMinWidth() != style.empty.getMinWidth() && style.checked.getMinHeight() != style.empty.getMinHeight()) {
		throw new IllegalArgumentException("filled and empty star should be of the same size");
	}

	mStyle = style;
	mStars = new Image[ratingMax];
	mClickListeners = new ClickListener[ratingMax];

	addActor(mTable);
	setTouchable(touchable);
	createImageButtons();
}

/**
 * Creates all the star image buttons
 */
private void createImageButtons() {
	for (int i = 0; i < mStars.length; ++i) {
		mStars[i] = new Image(mStyle.empty);
		mStars[i].setSize(mStyle.checked.getMinWidth(), mStyle.checked.getMinHeight());
		mTable.add(mStars[i]);
		mClickListeners[i] = new ClickListenerImage(i + 1);
		mStars[i].addListener(mClickListeners[i]);
	}
}

/**
 * Constructor which sets the filled and empty star drawables and the number of stars to show.
 * Rating can be changed by pressing on the stars
 * @param checked how a filled star looks
 * @param empty how an empty star looks
 * @param ratingMax number of stars used in the rating
 * @see #RatingWidget(Drawable, Drawable, int, Touchable)
 */
public RatingWidget(Drawable checked, Drawable empty, int ratingMax) {
	this(new RatingWidgetStyle(checked, empty), ratingMax, Touchable.enabled);
}

/**
 * Constructor which sets the filled and empty star drawables and the number of stars to show
 * @param checked how a filled star looks
 * @param empty how an empty star looks
 * @param ratingMax number of stars used in the rating
 * @param touchable if the rating can be changed by pressing on the stars
 */
public RatingWidget(Drawable checked, Drawable empty, int ratingMax, Touchable touchable) {
	this(new RatingWidgetStyle(checked, empty), ratingMax, touchable);
}

/**
 * Sets the rating/number of stars. This will send a change event for all IRatingListener
 * @param rating new rating of the widget
 */
public void setRating(int rating) {
	if (rating < 0 || rating > mStars.length) {
		throw new IllegalArgumentException("rating has to be between 0 and " + mStars.length);
	}

	if (mRatingCurrent != rating) {
		mRatingCurrent = rating;
		sendChangeEvent();
	}
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
 * Add a rating changed listener. These are only called when the rating is changed when a star is
 * pressed, i.e. not when {@link #setRating(int)} is called.
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
 * @return current style of this rating widget
 */
public RatingWidgetStyle getStyle() {
	return mStyle;
}

/**
 * Set the style
 * @param style new style of this rating widget
 */
public void setStyle(RatingWidgetStyle style) {
	mStyle = style;
}

@Override
public float getWidth() {
	return mTable.getWidth();
}

@Override
public float getHeight() {
	return mTable.getHeight();
}

@Override
public float getMinWidth() {
	return mTable.getMinWidth();
}

@Override
public float getMinHeight() {
	return mTable.getMinHeight();
}

@Override
public float getPrefWidth() {
	return mTable.getPrefWidth();
}

@Override
public float getPrefHeight() {
	return mTable.getPrefHeight();
}

@Override
public float getMaxWidth() {
	return mTable.getMaxWidth();
}

@Override
public float getMaxHeight() {
	return mTable.getMaxHeight();
}

@Override
public void layout() {
	mTable.layout();
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
				return i + 1;
			}
		}
	}

	return -1;
}

/**
 * Updates the number of shown stars. This does not change the rating just how many stars are
 * currently filled
 * @param cFilledStars number of stars to show.
 */
private void updateFilledStars(int cFilledStars) {
	for (int i = 0; i < mStars.length; ++i) {
		if (i < cFilledStars) {
			mStars[i].setDrawable(mStyle.checked);
		} else {
			mStars[i].setDrawable(mStyle.empty);
		}
	}
}

/**
 * Style for rating widget
 */
public static class RatingWidgetStyle {
	/** Drawable used for filled stars */
	private Drawable checked = null;
	/** Drawable used for empty stars */
	private Drawable empty = null;

	/**
	 * Default constructor
	 */
	public RatingWidgetStyle() {
		// Does nothing
	}
	/**
	 * Sets the filled and empty star
	 * @param checked drawable for filled stars
	 * @param empty drawable for empty stars
	 */
	public RatingWidgetStyle(Drawable checked, Drawable empty) {
		this.checked = checked;
		this.empty = empty;
	}
}


/**
 * Click listener
 */
private class ClickListenerImage extends ClickListener {
	/** Index of the star */
	int mRating;

	/**
	 * @param rating the rating the star is equal to
	 */
	ClickListenerImage(int rating) {
		mRating = rating;
	}

	@Override
	public void clicked(InputEvent event, float x, float y) {
		// Special case, if we want to set the rating to which it already is, set it
		// to 0
		// instead
		if (mRatingCurrent == mRating) {
			mRatingCurrent = 0;
		} else {
			mRatingCurrent = mRating;
		}

		sendChangeEvent();
	}
}}

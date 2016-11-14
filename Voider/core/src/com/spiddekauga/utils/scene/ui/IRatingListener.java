package com.spiddekauga.utils.scene.ui;

/**
 * Listens to rating changes
 */
public interface IRatingListener {
/**
 * Called when the rating is changed
 * @param newRating the new rating
 */
void onRatingChange(int newRating);
}

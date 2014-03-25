package com.spiddekauga.utils.scene.ui;

/**
 * Listens to rating changes
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public interface IRatingListener {
	/**
	 * Called when the rating is changed
	 * @param newRating the new rating
	 */
	void onRatingChange(int newRating);
}

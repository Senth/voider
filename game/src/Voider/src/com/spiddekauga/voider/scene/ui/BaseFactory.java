package com.spiddekauga.voider.scene.ui;

/**
 * Base class for all UI factories
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
abstract class BaseFactory {
	/**
	 * Sets the styles and return variable
	 * @param createdActors wrapper class for created actors
	 * @param styles all UI styles
	 */
	void init(CreatedActors createdActors, UiStyles styles) {
		mCreatedActors = createdActors;
		mStyles = styles;
	}

	/** Created actors */
	protected CreatedActors mCreatedActors = null;
	/** UI styles */
	protected UiStyles mStyles = null;
}

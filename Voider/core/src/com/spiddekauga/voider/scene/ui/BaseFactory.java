package com.spiddekauga.voider.scene.ui;

/**
 * Base class for all UI factories
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
abstract class BaseFactory {
	/**
	 * Sets the styles and return variable
	 * @param styles all UI styles
	 */
	void init(UiStyles styles) {
		mStyles = styles;
		mUiFactory = UiFactory.getInstance();
	}

	/** UI styles */
	protected UiStyles mStyles = null;
	/** UiFactory */
	protected UiFactory mUiFactory = null;
}

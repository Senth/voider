package com.spiddekauga.voider.scene.ui;

/**
 * Base class for all UI factories
 */
abstract class BaseFactory {
/** UI styles */
protected UiStyles mStyles = null;
/** UiFactory */
protected UiFactory mUiFactory = null;

/**
 * Sets the styles and return variable
 * @param styles all UI styles
 */
void init(UiStyles styles) {
	mStyles = styles;
	mUiFactory = UiFactory.getInstance();
}
}

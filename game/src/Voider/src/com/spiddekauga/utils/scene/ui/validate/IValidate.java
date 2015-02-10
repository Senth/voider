package com.spiddekauga.utils.scene.ui.validate;

/**
 * Validate fields or something else before sending the values. The instance has to show
 * the validation error itself.
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public interface IValidate {
	/**
	 * @return true if the object is valid
	 */
	boolean isValid();

	/**
	 * Reset the error message
	 */
	void resetError();

	/**
	 * Print the error
	 */
	void printError();
}

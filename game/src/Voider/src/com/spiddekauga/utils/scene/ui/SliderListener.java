package com.spiddekauga.utils.scene.ui;

import java.math.BigDecimal;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.spiddekauga.utils.Maths;

/**
 * Listener that binds a slider with a textfield.
 * Override #onChange(float) to implement some action  when the value is changed
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public abstract class SliderListener implements EventListener {
	/**
	 * Constructor which sets the slider and text field.
	 * @param slider slider to bind with textField
	 * @param textField textField to bind with slider
	 */
	public SliderListener(Slider slider, TextField textField) {
		mSlider = slider;
		mTextField = textField;
		mTextField.setText(Float.toString(mSlider.getValue()));
		mOldValue = slider.getValue();

		// Calculate precision
		float stepSize = mSlider.getStepSize();
		while (stepSize < 1) {
			stepSize *= 10;
			mPrecision++;
		}

		mSlider.addListener(this);
		mTextField.addListener(this);
	}

	/**
	 * Sets the validating object
	 * @param validatingObject object to use for validating later
	 */
	public void setValidatingObject(Object validatingObject) {
		mValidingObject = validatingObject;
	}

	@Override
	public boolean handle(Event event) {
		if (mSlider == null && mTextField == null) {
			return false;
		}

		// Slider changed the value
		if (event.getTarget() == mSlider) {
			int cursorPosition = mTextField.getCursorPosition();
			float rounded = (float) Maths.round(mSlider.getValue(), mPrecision, BigDecimal.ROUND_HALF_UP);
			if (isValidValue(rounded)) {
				mTextField.setText(Float.toString(rounded));
				mTextField.setCursorPosition(cursorPosition);
			} else {
				mSlider.setValue(mOldValue);
			}
		} else if (event.getTarget() == mTextField) {
			try {
				// Skip if text wasn't changed
				if (Float.parseFloat(mTextField.getText()) == mOldValue) {
					return true;
				}

				// Remove text after cursor position
				int cursorPosition = mTextField.getCursorPosition();
				String cutText = mTextField.getText().substring(0, cursorPosition);

				float newValue = Float.parseFloat(cutText);

				// Clamp value
				newValue = MathUtils.clamp(newValue, mSlider.getMinValue(), mSlider.getMaxValue());
				float rounded = (float) Maths.round(newValue, mPrecision, BigDecimal.ROUND_HALF_UP);

				if (isValidValue(rounded)) {
					mSlider.setValue(newValue);
					mTextField.setText(Float.toString(rounded));
					mTextField.setCursorPosition(cursorPosition);
				} else {
					mTextField.setText(Float.toString(mOldValue));
					mTextField.setCursorPosition(cursorPosition);
				}
			}
			// Not a valid format, reset to old value
			catch (NumberFormatException e) {
				int cursorPosition = mTextField.getCursorPosition();
				mTextField.setText(Float.toString(mOldValue));
				mTextField.setCursorPosition(cursorPosition);
			}
		}

		if (mOldValue != mSlider.getValue()) {
			onChange(mSlider.getValue());
			mOldValue = mSlider.getValue();
		}


		return true;
	}

	/**
	 * Called whenever the value in either slider or text has changed
	 * @param newValue the new value for slider and text.
	 */
	public abstract void onChange(float newValue);

	/** Called before the new value is set, this validates the new value
	 * one extra time. The value has already been clamped and rounded
	 * @param newValue the new value that is to be validated
	 * @return true if the new range is valid
	 * @see #setValidatingObject(Object)
	 */
	protected boolean isValidValue(float newValue) {
		return true;
	}

	/** Extra optional object used for validating the change */
	protected Object mValidingObject = null;
	/**	The slider that was bound */
	protected Slider mSlider = null;
	/** The text the value is bound with */
	protected TextField mTextField = null;

	/** Old value of the slider */
	private float mOldValue = 0;
	/** Precision of the slider */
	private int mPrecision = 0;
}

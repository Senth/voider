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

	@Override
	public boolean handle(Event event) {
		if (mSlider == null && mTextField == null) {
			return false;
		}

		// Slider changed the value
		if (event.getTarget() == mSlider) {
			int cursorPosition = mTextField.getCursorPosition();
			float rounded = (float) Maths.round(mSlider.getValue(), mPrecision, BigDecimal.ROUND_HALF_UP);
			mTextField.setText(Float.toString(rounded));
			mTextField.setCursorPosition(cursorPosition);
		} else if (event.getTarget() == mTextField) {
			/** @TODO check for valid value */

			try {
				float newValue = Float.parseFloat(mTextField.getText());

				// Clamp value
				newValue = MathUtils.clamp(newValue, mSlider.getMinValue(), mSlider.getMaxValue());
				mSlider.setValue(newValue);
				int cursorPosition = mTextField.getCursorPosition();
				float rounded = (float) Maths.round(mSlider.getValue(), mPrecision, BigDecimal.ROUND_HALF_UP);
				mTextField.setText(Float.toString(rounded));
				mTextField.setCursorPosition(cursorPosition);
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

	/**	The slider that was bound */
	private Slider mSlider = null;
	/** The text the value is bound with */
	private TextField mTextField = null;
	/** Old value of the slider */
	private float mOldValue = 0;
	/** Precision of the slider */
	private int mPrecision = 0;
}

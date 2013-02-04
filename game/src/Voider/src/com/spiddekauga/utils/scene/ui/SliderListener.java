package com.spiddekauga.utils.scene.ui;

import java.math.BigDecimal;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputEvent.Type;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldFilter;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener.FocusEvent;
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
		mOldText = mTextField.getText();

		mTextField.setTextFieldFilter(new TextFieldFilter() {
			@Override
			public boolean acceptChar(TextField textField, char key) {
				return Character.isDigit(key) || key == '.';
			}
		});

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

		// Skip certain input events
		if (event instanceof InputEvent) {
			InputEvent inputEvent = (InputEvent)event;
			if (inputEvent.getType() == Type.mouseMoved ||
					inputEvent.getType() == Type.enter ||
					inputEvent.getType() == Type.exit) {
				return true;
			}
		}



		// Slider changed the value
		if (event.getTarget() == mSlider) {
			if (isValidValue(mSlider.getValue())) {
				if (!mEditingText) {
					setTextFieldFromSlider();
				}
			} else {
				mSlider.setValue(mOldValue);
			}
		} else if (event.getTarget() == mTextField) {
			try {
				// Is this a focus out event?
				if (event instanceof FocusEvent) {
					if (!((FocusEvent) event).isFocused()) {
						setTextFieldFromSlider();
						return true;
					}
				}
				// Enter was pressed
				else if (event instanceof InputEvent) {
					InputEvent inputEvent = (InputEvent)event;
					if (inputEvent.getType() == Type.keyDown && inputEvent.getKeyCode() == Input.Keys.ENTER) {
						setTextFieldFromSlider();
						return true;
					}
				}
				// Skip if text wasn't changed
				else if (mOldText.equals(mTextField.getText())) {
					return true;
				}


				mEditingText = true;


				float newValue = Float.parseFloat(mTextField.getText());

				// Clamp value
				newValue = MathUtils.clamp(newValue, mSlider.getMinValue(), mSlider.getMaxValue());
				float rounded = (float) Maths.round(newValue, mPrecision, BigDecimal.ROUND_HALF_UP);
				boolean validValue = false;

				if (isValidValue(rounded)) {
					validValue = true;
					mSlider.setValue(newValue);
				}

				mOldText = mTextField.getText();
				mEditingText = false;
			}
			// Not a valid format, reset to old value
			catch (NumberFormatException e) {
				int cursorPosition = mTextField.getCursorPosition();
				mTextField.setText(mOldText);
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

	/**
	 * Sets the text field from the slider value. Rounds correctly
	 */
	private void setTextFieldFromSlider() {
		int cursorPosition = mTextField.getCursorPosition();
		float rounded = (float) Maths.round(mSlider.getValue(), mPrecision, BigDecimal.ROUND_HALF_UP);
		mTextField.setText(Float.toString(rounded));
		mTextField.setCursorPosition(cursorPosition);
	}

	/** Extra optional object used for validating the change */
	protected Object mValidingObject = null;
	/**	The slider that was bound */
	protected Slider mSlider = null;
	/** The text the value is bound with */
	protected TextField mTextField = null;


	/** If changing text atm */
	private boolean mEditingText = false;
	/** Old text value */
	private String mOldText;
	/** Old value of the slider */
	private float mOldValue = 0;
	/** Precision of the slider */
	private int mPrecision = 0;
}

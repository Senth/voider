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
import com.spiddekauga.utils.CDelimiter;
import com.spiddekauga.utils.Invoker;
import com.spiddekauga.utils.Maths;
import com.spiddekauga.voider.Config.Editor;
import com.spiddekauga.voider.editor.commands.CGuiSlider;

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
	 * @param textField textField to bind with slider, set to null to skip
	 * @param invoker used for undoing commands, set to null to skip
	 */
	public SliderListener(Slider slider, TextField textField, Invoker invoker) {
		mSlider = slider;
		mInvoker = invoker;
		mTextField = textField;

		mOldValue = slider.getValue();

		// Calculate precision
		float stepSize = mSlider.getStepSize();
		while (stepSize < 1) {
			stepSize *= 10;
			mPrecision++;
		}

		mSlider.addListener(this);

		if(mTextField != null) {
			mTextField.setText(Float.toString(mSlider.getValue()));
			mOldText = mTextField.getText();

			mTextField.setTextFieldFilter(new TextFieldFilter() {
				@Override
				public boolean acceptChar(TextField textField, char key) {
					return Character.isDigit(key) || key == '.' || key == '-';
				}
			});

			mTextField.addListener(this);
		}
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

			// Add delimiter when pressing up to avoid combining all
			// changes at the same time
			if (mInvoker != null) {
				if (inputEvent.getType() == Type.touchUp) {
					mInvoker.execute(new CDelimiter());
				}
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

				if (isValidValue(rounded)) {
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

			// Execute slider command if not an invoker changed the slider's value
			if (mInvoker != null) {
				if (mSlider.getName() == null || !mSlider.getName().equals(Editor.GUI_INVOKER_TEMP_NAME)) {
					mInvoker.execute(new CGuiSlider(mSlider, mSlider.getValue(), mOldValue));
				}
			}

			if (mLesserSlider != null && mLesserSlider.getValue() > mSlider.getValue()) {
				if (mInvoker != null) {
					mInvoker.execute(new CGuiSlider(mLesserSlider, mSlider.getValue(), mLesserSlider.getValue()), true);
				} else {
					mLesserSlider.setValue(mSlider.getValue());
				}
			}

			if (mGreaterSlider != null && mGreaterSlider.getValue() < mSlider.getValue()) {
				if (mInvoker != null) {
					mInvoker.execute(new CGuiSlider(mGreaterSlider, mSlider.getValue(), mGreaterSlider.getValue()), true);
				}
				else {
					mGreaterSlider.setValue(mSlider.getValue());
				}
			}

			mOldValue = mSlider.getValue();
		}


		return true;
	}

	/**
	 * Sets another slider that always should be less or equal to this slider.
	 * This will change the other slider's value if this condition isn't met
	 * @param lesserSlider slider that always shall be less or equal to the internal slider.
	 */
	public void setLesserSlider(Slider lesserSlider) {
		mLesserSlider = lesserSlider;
	}

	/** Sets another slider that always should be greater or equal to this slider.
	 * This will change the other slider's value if this condition isn't met
	 * @param greaterSlider slider that always shall be greater or equal to the internal slider.
	 */
	public void setGreaterSlider(Slider greaterSlider) {
		mGreaterSlider = greaterSlider;
	}

	/**
	 * Called whenever the value in either slider or text has changed
	 * @param newValue the new value for slider and text.
	 */
	protected abstract void onChange(float newValue);

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
		mOldText = mTextField.getText();
	}

	/** Extra optional object used for validating the change */
	protected Object mValidingObject = null;
	/**	The slider that was bound */
	protected Slider mSlider = null;
	/** The text the value is bound with */
	protected TextField mTextField = null;


	/** Shall always be less or equal to mSlider */
	private Slider mLesserSlider = null;
	/** Shall always be greater or equal to mSlider */
	private Slider mGreaterSlider = null;
	/** If changing text atm */
	private boolean mEditingText = false;
	/** Old text value */
	private String mOldText;
	/** Old value of the slider */
	private float mOldValue = 0;
	/** Precision of the slider */
	private int mPrecision = 0;
	/** Invoker used for undo */
	private Invoker mInvoker = null;
}

package com.spiddekauga.utils.scene.ui;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputEvent.Type;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldFilter;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener.FocusEvent;
import com.spiddekauga.utils.KeyHelper;
import com.spiddekauga.utils.Maths;
import com.spiddekauga.utils.commands.CGuiSlider;
import com.spiddekauga.utils.commands.Invoker;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

/**
 * Listener that binds a slider with a textfield. Override #onChange(float) to implement some action
 * when the value is changed
 */
public abstract class SliderListener implements EventListener {
/** Extra optional object used for validating the change */
protected Object mValidingObject = null;
/** All sliders */
protected Set<Slider> mSliders = new HashSet<>();
/** All text fields */
protected Set<TextField> mTextFields = new HashSet<>();
/** True if we're editing text */
private int mEditingText = 0;
/** Shall always be less or equal to mSlider */
private SliderListener mLesserSlider = null;
/** Shall always be greater or equal to mSlider */
private SliderListener mGreaterSlider = null;
/** Old text value */
private String mOldText;
/** Old value of the slider */
private float mOldValue = 0;
private Invoker mInvoker = null;
/** True if we're currently changing a value */
private boolean mChangingValue = false;
private boolean mInitialized = false;
private float mValueMin = 0;
private float mValueMax = 0;
private int mPrecision = 0;
/** True if the invoker is changing the value */
private boolean mChangingValueInvoker = false;

/**
 * Constructor which automatically calls {@link #add(Slider, TextField)}
 * @param slider slider to bind with textField
 * @param textField textField to bind with slider, set to null to skip
 * @param invoker used for undoing commands, set to null to skip
 */
public SliderListener(Slider slider, TextField textField, Invoker invoker) {
	this(invoker);
	add(slider, textField);
}

/**
 * Creates an invalid slider listener with an invoker. Call {@link #add(Slider, TextField)} to
 * initialize the slider listener
 * @param invoker used for undoing commands, may be null
 */
public SliderListener(Invoker invoker) {
	mInvoker = invoker;
}

/**
 * Add a slider and a text field to this listener
 * @param slider slider to bind with textField
 * @param textField textField to bind with slider, set to null to skip
 */
public void add(Slider slider, TextField textField) {
	mOldValue = slider.getValue();

	// Calculate precision
	if (!mInitialized) {
		float stepSize = slider.getStepSize();
		while (stepSize < 1) {
			stepSize *= 10;
			mPrecision++;
		}

		mValueMin = slider.getMinValue();
		mValueMax = slider.getMaxValue();

		mInitialized = true;
	}

	slider.addListener(this);
	mSliders.add(slider);

	if (textField != null) {
		textField.setText(Float.toString(slider.getValue()));
		mOldText = textField.getText();

		textField.setTextFieldFilter(new TextFieldFilter() {
			@Override
			public boolean acceptChar(TextField textField, char key) {
				return Character.isDigit(key) || key == '.' || key == '-';
			}
		});

		textField.addListener(this);
		mTextFields.add(textField);
	}
}

/**
 * Constructor which automatically calls {@link #add(Slider, TextField)}
 * @param slider slider to bind with the text field
 */
public SliderListener(Slider slider) {
	this(slider, null);
}

/**
 * Constructor which automatically calls {@link #add(Slider, TextField)}
 * @param slider slider to bind with textField
 * @param textField textField to bind with slider, set to null to skip
 */
public SliderListener(Slider slider, TextField textField) {
	add(slider, textField);
}
/**
 * Default constructor, call {@link #add(Slider, TextField)} to initialize the slider listener.
 */
public SliderListener() {
	// Does nothing
}

/**
 * Add a slider (without any text field) to this listener
 * @param slider slider to bind with this listener
 */
public void addSlider(Slider slider) {
	add(slider, null);
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
	if (mSliders.isEmpty() || mChangingValue) {
		return false;
	}


	// Skip certain input events
	if (event instanceof InputEvent) {
		InputEvent inputEvent = (InputEvent) event;
		if (inputEvent.getType() == Type.mouseMoved || inputEvent.getType() == Type.enter || inputEvent.getType() == Type.exit) {
			return true;
		}
	}

	float newValue = mOldValue;

	// Slider was changed
	boolean sliderChangedValue = false;
	if (event.getTarget() instanceof Slider) {
		Slider slider = (Slider) event.getTarget();
		newValue = slider.getValue();
		sliderChangedValue = true;
	}
	// Text field was changed
	else if (event.getTarget() instanceof TextField) {
		TextField textField = (TextField) event.getTarget();

		// Focus out -> Set clamped value
		if (event instanceof FocusEvent) {
			FocusEvent focusEvent = (FocusEvent) event;
			if (focusEvent.isFocused()) {
				mEditingText++;
			} else {
				setTextFieldValues(mOldValue);
				mEditingText--;
			}
			return true;
		}
		// key was was pressed
		else if (event instanceof InputEvent) {
			InputEvent inputEvent = (InputEvent) event;
			if (inputEvent.getType() == Type.keyDown) {
				// ESC, BACK, ENTER -> Unfocus
				if (inputEvent.getKeyCode() == Input.Keys.ENTER || inputEvent.getKeyCode() == Input.Keys.ESCAPE
						|| inputEvent.getKeyCode() == Input.Keys.BACK) {
					setTextFieldValues(mOldValue);
					textField.getStage().setKeyboardFocus(null);
					return true;
				}
				// Redo
				else if (KeyHelper.isRedoPressed(inputEvent.getKeyCode())) {
					if (mInvoker != null) {
						mInvoker.redo();
					}
				}
				// Undo
				else if (KeyHelper.isUndoPressed(inputEvent.getKeyCode())) {
					if (mInvoker != null) {
						mInvoker.undo();
					}
				}
			}
		}
		// Skip if text wasn't changed
		else if (mOldText.equals(textField.getText())) {
			return true;
		}


		// Get new value from the text field
		try {
			String newText = textField.getText();
			if (!newText.equals("")) {
				newValue = Float.parseFloat(newText);
			} else {
				newValue = 0;
			}

			// Clamp value
			newValue = MathUtils.clamp(newValue, mValueMin, mValueMax);
			mOldText = newText;
		} catch (NumberFormatException e) {
			int cursorPosition = textField.getCursorPosition();
			textField.setText(mOldText);
			textField.setCursorPosition(cursorPosition);
		}
	}

	// New value
	if (!MathUtils.isEqual(mOldValue, newValue)) {
		// Create commands
		if (!mChangingValueInvoker) {
			// Execute slider command if not an invoker changed the slider's value
			if (mInvoker != null) {
				mInvoker.execute(new CGuiSlider(this, newValue, mOldValue));
			}

			// Lesser slider
			if (mLesserSlider != null && mLesserSlider.getValue() > newValue) {
				if (mInvoker != null) {
					mInvoker.execute(new CGuiSlider(mLesserSlider, newValue, mLesserSlider.getValue(), false), true);
				}
			}

			// Greater slider
			if (mGreaterSlider != null && mGreaterSlider.getValue() < newValue) {
				if (mInvoker != null) {
					mInvoker.execute(new CGuiSlider(mGreaterSlider, newValue, mGreaterSlider.getValue(), false), true);
				}
			}
		}

		int currentEditingText = mEditingText;
		TextField focusedTextField = getFocusedTextField();
		int cursorPos = (focusedTextField != null) ? focusedTextField.getCursorPosition() : 0;
		if (sliderChangedValue) {
			mEditingText = 0;
		}
		setValue(newValue);
		if (sliderChangedValue) {
			mEditingText = currentEditingText;
			if (focusedTextField != null) {
				focusedTextField.setCursorPosition(cursorPos);
			}
		}
	}


	return true;
}

/**
 * Sets a new value of all sliders and text fields. This doesn't create any commands. Changes lesser
 * or greater slider if the value is below/above the that value. Causes {@link #onChange(float)} to
 * be called
 * @param value new value to set
 * @param byInvoker true if the invoker caused this change
 */
public void setValue(float value, boolean byInvoker) {
	mChangingValue = true;
	mChangingValueInvoker = byInvoker;

	// Sliders
	for (Slider slider : mSliders) {
		slider.setValue(value);
	}

	// Text fields
	setTextFieldValues(value);

	// Lesser
	if (mLesserSlider != null && mLesserSlider.getValue() > value) {
		mLesserSlider.setValue(value);
	}

	// Greater
	if (mGreaterSlider != null && mGreaterSlider.getValue() < value) {
		mGreaterSlider.setValue(value);
	}

	mOldValue = value;
	onChange(value);
	mChangingValue = false;
	mChangingValueInvoker = false;
}

/**
 * @return current value of this slider listener
 */
public float getValue() {
	Slider slider = getFirstSlider();

	if (slider != null) {
		return slider.getValue();
	}

	return 0;
}

/**
 * Sets a new value of all sliders and text fields. This doesn't create any commands. Changes lesser
 * or greater slider if the value is below/above the that value. Causes {@link #onChange(float)} to
 * be called
 * @param value new value to set
 */
public void setValue(float value) {
	setValue(value, false);
}

/**
 * @return true if an invoker is currently changing the value of the sliders
 */
public boolean isInvokerChangingValue() {
	return mChangingValueInvoker;
}

/**
 * Sets another slider that always should be less or equal to this slider. This will change the
 * other slider's value if this condition isn't met
 * @param lesserSlider slider that always shall be less or equal to the internal slider.
 */
public void setLesserSlider(SliderListener lesserSlider) {
	mLesserSlider = lesserSlider;
}

/**
 * Sets another slider that always should be greater or equal to this slider. This will change the
 * other slider's value if this condition isn't met
 * @param greaterSlider slider that always shall be greater or equal to the internal slider.
 */
public void setGreaterSlider(SliderListener greaterSlider) {
	mGreaterSlider = greaterSlider;
}

/**
 * Called whenever the value in either slider or text has changed
 * @param newValue the new value for slider and text.
 */
protected abstract void onChange(float newValue);

/**
 * Set all text fields from the slider
 * @param newValue the new value
 */
private void setTextFieldValues(float newValue) {
	float rounded = (float) Maths.round(newValue, mPrecision, BigDecimal.ROUND_HALF_UP);
	String newText = "";
	if (mPrecision > 0) {
		newText = Float.toString(rounded);
	} else {
		newText = Integer.toString((int) rounded);
	}

	TextField focusedTextField = getFocusedTextField();
	for (TextField textField : mTextFields) {
		if (mEditingText == 0 || textField != focusedTextField) {
			textField.setText(newText);
		}
	}
}

/**
 * @return first slider
 */
private Slider getFirstSlider() {
	if (!mSliders.isEmpty()) {
		return mSliders.iterator().next();
	}

	return null;
}

/**
 * @return focused text field if one exists, null if none exists
 */
private TextField getFocusedTextField() {
	for (TextField textField : mTextFields) {
		Stage stage = textField.getStage();
		if (stage != null && stage.getKeyboardFocus() == textField) {
			return textField;
		}
	}

	return null;
}
}

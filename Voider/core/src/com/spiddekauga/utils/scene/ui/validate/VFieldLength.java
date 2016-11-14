package com.spiddekauga.utils.scene.ui.validate;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.spiddekauga.utils.scene.ui.TextFieldListener;

/**
 * Test the length of the field
 */
public class VFieldLength extends VField {
private int mLengthMin;

/**
 * @param fieldListener
 * @param errorLabel
 * @param length minimum field length
 */
public VFieldLength(TextFieldListener fieldListener, Label errorLabel, int length) {
	super(fieldListener, errorLabel);
	mLengthMin = length;
}

@Override
public boolean isValid() {
	return getText().trim().length() >= mLengthMin;
}

@Override
protected String getErrorText() {
	int length = getText().trim().length();
	if (length == 0) {
		return "is empty";
	} else if (length < mLengthMin) {
		return "too short (min " + mLengthMin + ")";
	}
	return "";
}
}

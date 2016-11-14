package com.spiddekauga.utils.scene.ui.validate;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.spiddekauga.utils.scene.ui.TextFieldListener;

/**
 * Match another field. Useful for confirm password or email
 */
public class VFieldMatch extends VField {
private TextFieldListener mMatch = null;
private String mErrorText = "doesn't match";

/**
 * Sets the field to match
 * @param fieldListener Check the validity of this field
 * @param errorLabel where to post the error message
 * @param match text field to match
 */
public VFieldMatch(TextFieldListener fieldListener, Label errorLabel, TextFieldListener match) {
	this(fieldListener, errorLabel, match, null);
}

/**
 * Sets the field to match
 * @param fieldListener Check the validity of this field
 * @param errorLabel where to post the error message
 * @param match text field to match
 * @param errorText the text to display when the fields doesn't match
 */
public VFieldMatch(TextFieldListener fieldListener, Label errorLabel, TextFieldListener match, String errorText) {
	super(fieldListener, errorLabel);
	mMatch = match;

	if (errorText != null) {
		mErrorText = errorText;
	}
}

@Override
public boolean isValid() {
	return getText().equals(mMatch.getText());
}

@Override
protected String getErrorText() {
	return mErrorText;
}
}

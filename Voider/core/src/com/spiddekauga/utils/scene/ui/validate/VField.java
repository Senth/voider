package com.spiddekauga.utils.scene.ui.validate;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.spiddekauga.utils.scene.ui.TextFieldListener;

/**
 * Common class for validating text fields
 */
abstract class VField implements IValidate {
private TextFieldListener mFieldListener;
private Label mErrorLabel;

/**
 * @param fieldListener the field to test
 * @param errorLabel error label to print the error to
 */
protected VField(TextFieldListener fieldListener, Label errorLabel) {
	mFieldListener = fieldListener;
	mErrorLabel = errorLabel;
}

@Override
public void resetError() {
	mErrorLabel.setText("");
}

@Override
public void printError() {
	mErrorLabel.setText(getErrorText());
}

/**
 * @return get error for the field
 */
protected abstract String getErrorText();

/**
 * @return text in the field
 */
protected String getText() {
	return mFieldListener.getText();
}
}

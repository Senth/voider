package com.spiddekauga.utils.scene.ui;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener.FocusEvent;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener.FocusEvent.Type;

/**
 * Listens to a text field. When a value has been changed #onChange() is called
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class TextFieldListener implements EventListener {
	/**
	 * Creates a text field listener for the specified text field. This automatically
	 * adds this listener as a listener to text field.
	 * @param textField the text field to listen to.
	 * @param defaultText if not set as null it will display this text in the text field
	 * whenever the field is empty and not focused.
	 */
	public TextFieldListener(TextField textField, String defaultText) {
		mTextField = textField;
		mDefaultText = defaultText;

		mOldText = mTextField.getText();

		if (mDefaultText != null && isTextFieldEmpty()) {
			mTextField.setText(mDefaultText);
		}

		mTextField.addListener(this);
	}

	@Override
	public boolean handle(Event event) {
		// When getting focus, loosing focus. Remove default text if applicable
		if (event instanceof FocusEvent) {
			FocusEvent focusEvent = (FocusEvent) event;
			if (focusEvent.getType() == Type.keyboard) {
				if (focusEvent.isFocused()) {
					if (isTextFieldDefault()) {
						mTextField.setText("");
					}
				} else {
					onDone(mTextField.getText());

					if (mDefaultText != null && isTextFieldEmpty()) {
						mTextField.setText(mDefaultText);
					}
				}
			}
		}
		else if (event instanceof InputEvent) {
			InputEvent inputEvent = (InputEvent)event;

			if (inputEvent.getType() == InputEvent.Type.keyDown) {
				// Enter, Esc, Back -> Unfocus
				if (inputEvent.getKeyCode() == Keys.ENTER ||
						inputEvent.getKeyCode() == Keys.ESCAPE ||
						inputEvent.getKeyCode() == Keys.BACK) {
					mTextField.getStage().setKeyboardFocus(null);
				}
			}
			if (inputEvent.getType() == InputEvent.Type.keyTyped) {
				if (!mTextField.getText().equals(mOldText)){
					onChange(mTextField.getText());
				}
			}
		}

		return true;
	}

	/**
	 * Called when the value inside the text field is changed. This
	 * is never called when the text is changed to the default text.
	 * @param newText the new text value for the text field
	 */
	protected void onChange(String newText) {
		// Does nothing
	}

	/**
	 * Called when the text field looses focus, or when enter is pressed.
	 * If the text field is empty when this happens, it is called <b>before</b>
	 * the field is set to the default text.
	 * @param newText the new text value for the text field
	 */
	protected void onDone(String newText) {
		// Does nothing
	}

	/**
	 * @return true if the text field is empty
	 */
	private boolean isTextFieldEmpty() {
		return mTextField.getText().equals("");
	}

	/**
	 * @return true if the text field contains the default text
	 */
	private boolean isTextFieldDefault() {
		return mTextField.getText().equals(mDefaultText);
	}

	/** Text field of the text field listener */
	protected TextField mTextField = null;

	/** Default text for the text field when it's empty */
	private String mDefaultText = null;
	/** Old text */
	private String mOldText;
}

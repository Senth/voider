package com.spiddekauga.utils.scene.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener.FocusEvent;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener.FocusEvent.Type;
import com.spiddekauga.utils.KeyHelper;
import com.spiddekauga.utils.commands.CGuiTextField;
import com.spiddekauga.utils.commands.Invoker;
import com.spiddekauga.voider.Config.Gui;

/**
 * Listens to a text field. When a value has been changed #onChange() is called
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class TextFieldListener implements EventListener {
	/**
	 * Creates an invalid text field listener. Be sure to call
	 * {@link #setTextField(TextField)} to use this listener
	 */
	public TextFieldListener() {
		this(null, null, null);
	}

	/**
	 * Creates an invalid text field listener. Be sure to call
	 * {@link #setTextField(TextField)} to use this listener
	 * @param invoker used for undoing commands, set to null to skip
	 */
	public TextFieldListener(Invoker invoker) {
		this(null, null, invoker);
	}

	/**
	 * Creates a text field listener for the specified text field. This automatically adds
	 * this listener as a listener to text field.
	 * @param textField the text field to listen to. whenever the field is empty and not
	 *        focused.
	 * @param invoker used for undoing commands, set to null to skip
	 */
	public TextFieldListener(TextField textField, Invoker invoker) {
		this(textField, null, invoker);
	}

	/**
	 * Creates a text field listener for the specified text field. This automatically adds
	 * this listener as a listener to text field.
	 * @param textField the text field to listen to.
	 * @param defaultText if not set as null it will display this text in the text field
	 *        whenever the field is empty and not focused.
	 * @param invoker used for undoing commands, set to null to skip
	 */
	public TextFieldListener(TextField textField, String defaultText, Invoker invoker) {
		mDefaultText = defaultText;
		mInvoker = invoker;

		setTextField(textField);
	}

	/**
	 * Sets the text field to listen to
	 * @param textField the text field to listen to, to remove a text field pass a null
	 *        value.
	 */
	public void setTextField(TextField textField) {
		if (mTextField != null) {
			mTextField.removeListener(this);
		}

		mTextField = textField;

		if (mTextField != null) {
			mPrevKeystrokeText = mTextField.getText();
			mIsPassword = mTextField.isPasswordMode();
			setDefaultText();
			mTextField.addListener(this);
		} else {
			mPrevKeystrokeText = "";
			mIsPassword = false;
		}
	}

	/**
	 * Sets the default text of the text field listener
	 * @param defaultText the default text to show when the text field is empty. Set to
	 *        null to disable this functionality
	 */
	public void setDefaultText(String defaultText) {
		mDefaultText = defaultText;
		setDefaultText();
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

						if (mIsPassword) {
							mTextField.setPasswordMode(true);
						}
					}
				} else {
					onDone(mTextField.getText());

					setDefaultText();
				}
			}
		} else if (event instanceof InputEvent) {
			InputEvent inputEvent = (InputEvent) event;

			if (inputEvent.getType() == InputEvent.Type.keyDown) {
				// Enter, Esc, Back -> Unfocus
				if (inputEvent.getKeyCode() == Keys.ENTER || inputEvent.getKeyCode() == Keys.ESCAPE || inputEvent.getKeyCode() == Keys.BACK) {
					mTextField.getStage().setKeyboardFocus(null);

					if (inputEvent.getKeyCode() == Keys.ENTER) {
						onEnter(mTextField.getText());
					}
				}
				// Erase previous word
				else if (inputEvent.getKeyCode() == Keys.BACKSPACE) {
					if (Gdx.input.isKeyPressed(Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Keys.CONTROL_RIGHT)) {
						String text = mTextField.getText();
						int removedCharacters = text.length();
						text = text.trim();

						int index = text.lastIndexOf(' ');

						if (index != -1) {
							text = text.substring(0, index + 2);
							removedCharacters -= index + 2;
						} else {
							text = "";
						}

						int cursorPosition = mTextField.getCursorPosition();
						mTextField.setText(text);
						mTextField.setCursorPosition(cursorPosition - removedCharacters);
					}
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

				// This needs to be here as there is no ChangeEvent in TextField
				// Thus if the text is change manually through setText(...) this
				// will catch that change.
				mPrevKeystrokeText = mTextField.getText();
			} else if (inputEvent.getType() == InputEvent.Type.keyTyped) {
				if (!mTextField.getText().equals(mPrevKeystrokeText)) {
					onChange(mTextField.getText());
					sendCommand();
					mPrevKeystrokeText = mTextField.getText();
				}
			}
		} else if (event instanceof ChangeEvent) {
			onChange(mTextField.getText());
			mPrevKeystrokeText = mTextField.getText();
		}

		return true;
	}

	/**
	 * Sets the default text
	 */
	private void setDefaultText() {
		if (mDefaultText != null && isTextFieldOnlyEmpty()) {
			if (mIsPassword) {
				mTextField.setPasswordMode(false);
			}
			mTextField.setText(mDefaultText);
		}
	}

	/**
	 * Called when the value inside the text field is changed. This is never called when
	 * the text is changed to the default text.
	 * @param newText the new text value for the text field
	 */
	protected void onChange(String newText) {
		// Does nothing
	}

	/**
	 * Called when the text field looses focus, or when enter is pressed. If the text
	 * field is empty when this happens, it is called <b>before</b> the field is set to
	 * the default text.
	 * @param newText the new text value for the text field
	 */
	protected void onDone(String newText) {
		// Does nothing
	}

	/**
	 * Called when enter is pressed
	 * @param newText the new text value for the text field
	 */
	protected void onEnter(String newText) {
		// Does nothing
	}

	/**
	 * @return true if the text field is empty or contains the default text
	 */
	public boolean isTextFieldEmpty() {
		return isTextFieldOnlyEmpty() || isTextFieldDefault();
	}

	/**
	 * @return true if the text field is empty
	 */
	private boolean isTextFieldOnlyEmpty() {
		return mTextField.getText().equals("");
	}

	/**
	 * @return true if the text field contains the default text
	 */
	private boolean isTextFieldDefault() {
		return mTextField.getText().equals(mDefaultText);
	}

	/**
	 * Sends an invoker command so the field can be undone
	 */
	private void sendCommand() {
		if (mInvoker != null) {
			// Execute text field command if it wasn't a command that changed the
			// text in the first place
			if (!Gui.GUI_INVOKER_TEMP_NAME.equals(mTextField.getName())) {
				mInvoker.execute(new CGuiTextField(mTextField, mPrevKeystrokeText, mTextField.getText()));
			}
		}
	}

	/** Text field of the text field listener */
	protected TextField mTextField;

	/** True if the text field is a password field by default */
	private boolean mIsPassword;
	/** Default text for the text field when it's empty */
	private String mDefaultText;
	/** Old text before previous keystroke */
	private String mPrevKeystrokeText;
	/** Used for undoing commands */
	private Invoker mInvoker;
}

package com.spiddekauga.utils.scene.ui;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Field;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.esotericsoftware.tablelayout.Cell;
import com.spiddekauga.utils.commands.Command;
import com.spiddekauga.utils.scene.ui.validate.IValidate;
import com.spiddekauga.voider.scene.ui.UiFactory;
import com.spiddekauga.voider.scene.ui.UiStyles.TextButtonStyles;

/**
 * Message box wrapper for dialog. This allows other content than just text.
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class MsgBox extends Dialog {
	/**
	 * Creates a message box with a skin to use for the message box
	 * @param skin skin for the window, buttons, and text.
	 */
	public MsgBox(Skin skin) {
		super("", skin);
		mSkin = skin;
		init();
	}

	/**
	 * Creates a message box with a window style to use from the specified skin
	 * @param skin the skin to find the window style in.
	 * @param windowStyleName name of the window style found in skin
	 */
	public MsgBox(Skin skin, String windowStyleName) {
		super("", skin, windowStyleName);
		mSkin = skin;
		init();
	}

	/**
	 * Creates a message box with a window style
	 * @param windowStyle the window style to use for the message box
	 */
	public MsgBox(WindowStyle windowStyle) {
		super("", windowStyle);
		init();
	}

	/**
	 * Initializes the MsgBox
	 */
	private void init() {
		float paddingInner = mUiFactory.getStyles().vars.paddingInner;

		getButtonTable().add(mButtonTable);
		getButtonTable().pad(paddingInner);
		getContentTable().pad(paddingInner);
		getContentTable().align(Align.center);

		defaults().space(0);
		defaults().pad(0);
		getContentTable().defaults().space(0);
		getContentTable().defaults().pad(0);
		getButtonTable().defaults().space(0);
		getButtonTable().defaults().pad(0);

		try {
			mfCancelHide = ClassReflection.getDeclaredField(Dialog.class, "cancelHide");
			mfCancelHide.setAccessible(true);
		} catch (ReflectionException e) {
			e.printStackTrace();
			throw new GdxRuntimeException(e);
		}


		final MsgBox msgBox = this;
		mButtonTable.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				boolean valid = validate(actor);
				if (!valid) {
					return;
				}
				result(mValues.get(actor));
				try {
					if (!(boolean) mfCancelHide.get(msgBox)) {
						hide();
					}
					mfCancelHide.set(msgBox, false);
				} catch (ReflectionException e) {
					e.printStackTrace();
					throw new GdxRuntimeException(e);
				}
			}

			/**
			 * Validate the actor
			 * @param actor the actor to validate
			 * @return true if the actor is valid and should continue
			 */
			private boolean validate(Actor actor) {
				IValidate[] validations = mValidations.get(actor);
				boolean valid = true;
				if (validations != null) {
					for (IValidate validation : validations) {
						validation.resetError();

						if (!validation.isValid()) {
							validation.printError();
							valid = false;
						}
					}
				}

				return valid;
			}
		});
	}

	@Override
	public void setObject(Actor actor, Object object) {
		if (object != null) {
			mValues.put(actor, object);
		}
	}

	/**
	 * Associate an actor with object and possible validations
	 * @param actor
	 * @param object
	 * @param validations
	 * @return this for chaining
	 */
	public MsgBox setObject(Actor actor, Object object, IValidate... validations) {
		setObject(actor, object);
		if (validations != null && validations.length > 0) {
			mValidations.put(actor, validations);
		}
		return this;
	}

	/**
	 * Clears the content and button tables. Sets message box as modal, non-movable, and
	 * keep within stage.
	 */
	@Override
	public void clear() {
		setMovable(false);
		setModal(true);
		setKeepWithinStage(true);
		setTitle("");
		setTitleAlignment(Align.center);
		getContentTable().clearChildren();
		mButtonTable.dispose();
		mValues.clear();
		clearActions();
		clearListeners();
	}

	/**
	 * Clears the content
	 */
	public void clearContent() {
		getContentTable().clearChildren();
	}

	/**
	 * Adds nothing to the content table
	 * @return cell for this content
	 */
	public Cell<?> content() {
		return getContentTable().add();
	}

	/**
	 * Adds an actor to the content table
	 * @param actor will be added to the content table
	 * @return cell for this content
	 */
	public Cell<? extends Actor> content(Actor actor) {
		@SuppressWarnings("unchecked")
		Cell<? extends Actor> cell = getContentTable().add(actor);
		if (actor instanceof AlignTable) {
			((AlignTable) actor).layout();
		}
		return cell;
	}

	/**
	 * Adds a label to the content table. The dialog must have been constructed with a
	 * skin to use this method.
	 * @param text the text to write in the content
	 * @see #text(String) does exactly the same thing
	 * @return this message box for chaining
	 */
	public Cell<? extends Actor> content(String text) {
		return content(text, (Integer) null);
	}

	/**
	 * Adds a label to the content table. The dialog must have been constructed with a
	 * skin to use this method.
	 * @param text the text to write in the content
	 * @param alignment text alignment, null to use default
	 * @see #text(String) does exactly the same thing
	 * @return this message box for chaining
	 */
	public Cell<? extends Actor> content(String text, Integer alignment) {
		if (mSkin == null) {
			throw new IllegalStateException("This method may only be used if the message box was constructed with a Skin.");
		}
		return content(text, mSkin.get(LabelStyle.class));
	}


	/**
	 * Adds a label to the content table.
	 * @param text text to write in the content
	 * @param labelStyle what label style to use for the label
	 * @return this message box for chaining
	 */
	public Cell<? extends Actor> content(String text, LabelStyle labelStyle) {
		return content(text, null, labelStyle);
	}

	/**
	 * Adds a label to the content table.
	 * @param text text to write in the content
	 * @param alignment text alignment, null ot use default
	 * @param labelStyle what label style to use for the label
	 * @return this message box for chaining
	 */
	public Cell<? extends Actor> content(String text, Integer alignment, LabelStyle labelStyle) {
		Label label = new Label(text, labelStyle);
		if (alignment != null) {
			label.setAlignment(alignment);
		}
		return content(new Label(text, labelStyle));
	}

	/**
	 * Adds a row to the content
	 * @return this message box for chaining
	 */
	public MsgBox contentRow() {
		getContentTable().row();
		return this;
	}

	/**
	 * Invalid, don't call this. Throws an exception. Use {@link #content(String)}
	 * instead.
	 */
	@Override
	@Deprecated
	public MsgBox text(String text) {
		throw new IllegalAccessError("This method should never be called, use context() instead");
	}

	/**
	 * Invalid, don't call this. Throws an exception. Use
	 * {@link #content(String, LabelStyle)} instead
	 */
	@Override
	@Deprecated
	public MsgBox text(String text, com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle labelStyle) {
		throw new IllegalAccessError("This method should never be called, use context() instead");
	}

	/**
	 * Invalid, don't call this. Throws an exception. Use {@link #content(Actor)} instead
	 */
	@Override
	@Deprecated
	public MsgBox text(com.badlogic.gdx.scenes.scene2d.ui.Label label) {
		throw new IllegalAccessError("This method should never be called, use context() instead");
	}

	/**
	 * Adds a new row to the button table
	 * @return this message box for chaining
	 */
	public MsgBox buttonRow() {
		mButtonTable.row().setPadTop(mUiFactory.getStyles().vars.paddingButton);
		return this;
	}


	@Override
	public MsgBox button(String text) {
		return button(text, null);
	}

	@Override
	public MsgBox button(String text, Object object) {
		Button button = addButton(text);
		setObject(button, object);
		return this;
	}

	/**
	 * Creates a new button with the specified text
	 * @param text text of the button using the default style
	 * @param object The object that will be passed to {@link #result(Object)} if this
	 *        button is clicked. May be null.
	 * @param validations optional validations. The object is only passed if all
	 *        validations are valid
	 * @return this for chaining
	 */
	public MsgBox button(String text, Object object, IValidate... validations) {
		Button button = addButton(text);
		setObject(button, object, validations);
		return this;
	}

	/**
	 * Create a text button
	 * @param text the text of the button
	 * @return the created and added button
	 */
	private Button addButton(String text) {
		com.spiddekauga.utils.scene.ui.Cell cell = mUiFactory.button.addText(text, TextButtonStyles.FILLED_PRESS, mButtonTable, null, null, null);

		// Pad every cell except first
		if (mButtonTable.getRow().getCellCount() > 1) {
			cell.setPadLeft(mButtonPad);
		}

		return (Button) cell.getActor();
	}


	/**
	 * @deprecated This method should never be called. Create the button outside of this
	 *             class instead then use {@link #button(Button)} or
	 *             {@link #button(Button, Object)}.
	 */
	@Deprecated
	@Override
	public MsgBox button(String text, Object object, TextButtonStyle buttonStyle) {
		throw new IllegalAccessError("This method should never be called, use button(String) instead");
	}

	@Override
	public MsgBox button(Button button) {
		return button(button, null);
	}

	@Override
	public MsgBox button(Button button, Object object) {
		mButtonTable.add(button);
		setObject(button, object);
		return this;
	}

	/**
	 * Adds the given button to the button table.
	 * @param button the button to add.
	 * @param object The object that will be passed to {@link #result(Object)} if this
	 *        button is clicked. May be null.
	 * @param validations optional validations. The object is only passed if all
	 *        validations are valid
	 * @return this for chaining
	 */
	public MsgBox button(Button button, Object object, IValidate... validations) {
		mButtonTable.add(button);
		setObject(button, object, validations);
		return this;
	}

	/**
	 * @return get last button cell, null if no buttons have been added in the current
	 *         row.
	 */
	public com.spiddekauga.utils.scene.ui.Cell getButtonCell() {
		return mButtonTable.getCell();
	}

	@Override
	public MsgBox show(Stage stage) {
		mButtonTable.layout();
		super.show(stage);
		mHiding = false;
		return this;
	}

	@Override
	public void layout() {
		mButtonTable.layout();
		super.layout();
		pack();
		centerPosition();
	}

	/**
	 * Center it's position
	 */
	private void centerPosition() {
		if (getStage() != null) {
			setPosition(Math.round((getStage().getWidth() - getWidth()) / 2), Math.round((getStage().getHeight() - getHeight()) / 2));
		}
	}

	@Override
	public void hide() {
		super.hide();
		mHiding = true;
	}


	/**
	 * Only add cancel keys
	 * @param cancelCommand command to execute if canceled
	 * @return this message box for chaining
	 */
	public MsgBox addCancelKeys(Command cancelCommand) {
		key(Keys.BACK, cancelCommand);
		key(Keys.ESCAPE, cancelCommand);
		return this;
	}

	/**
	 * Only add cancel keys
	 * @return this message box for chaining
	 */
	public MsgBox addCancelKeys() {
		return addCancelKeys(null);
	}

	/**
	 * Add cancel button and keys. Text for the cancel button is "Cancel".
	 * @return this message box for chaining
	 * @see #addCancelButtonAndKeys(String) for using a custom text for the cancel button
	 */
	public MsgBox addCancelButtonAndKeys() {
		return addCancelButtonAndKeys(CANCEL_TEXT);
	}

	/**
	 * Add a cancel button and keys.
	 * @param buttonText text for the cancel button
	 * @return this message box for chaining
	 * @see #addCancelButtonAndKeys()
	 */
	public MsgBox addCancelButtonAndKeys(String buttonText) {
		return addCancelButtonAndKeys(buttonText, null);
	}

	/**
	 * Add a cancel button and keys.
	 * @param cancelCommand this command shall return true in execute if the message box
	 *        is allowed to cancel.
	 * @return this message box for chaining
	 * @see #addCancelButtonAndKeys()
	 * @see #addCancelButtonAndKeys(String)
	 * @see #addCancelButtonAndKeys(String, Command)
	 * @see #addCancelOkButtonAndKeys(String, Command)
	 */
	public MsgBox addCancelButtonAndKeys(Command cancelCommand) {
		return addCancelButtonAndKeys(CANCEL_TEXT, cancelCommand);
	}

	/**
	 * Add a cancel button and keys.
	 * @param buttonText text for the cancel button
	 * @param cancelCommand this command shall return true in execute if the message box
	 *        is allowed to cancel.
	 * @return this message box for chaining
	 * @see #addCancelButtonAndKeys()
	 */
	public MsgBox addCancelButtonAndKeys(String buttonText, Command cancelCommand) {
		button(buttonText, cancelCommand);
		addCancelKeys();
		return this;
	}

	/**
	 * Add a cancel button and keys.
	 * @param buttonText text for the cancel button
	 * @param cancelCommand this command shall return true in execute if the message box
	 *        is allowed to cancel.
	 * @return this message box for chaining
	 * @see #addCancelButtonAndKeys()
	 */
	public MsgBox addCancelOkButtonAndKeys(String buttonText, Command cancelCommand) {
		addCancelButtonAndKeys(buttonText, cancelCommand);
		key(Keys.ENTER, cancelCommand);
		return this;
	}

	/**
	 * @return true if the message box is hidden
	 */
	public boolean isHidden() {
		return mHiding && !isHideInProgress();
	}

	/**
	 * @return true if hiding is in progress
	 */
	public boolean isHideInProgress() {
		if (mHiding) {
			return getStage() != null;
		} else {
			return false;
		}
	}

	/**
	 * Set button padding
	 * @param pad padding between buttons
	 */
	public void setButtonPad(float pad) {
		mButtonPad = pad;
	}

	@Override
	public void setSkin(Skin skin) {
		mSkin = skin;
		super.setSkin(skin);
	}

	/** Button padding */
	private float mButtonPad = 0;
	/** Pointer to cancelHide in Dialog */
	Field mfCancelHide = null;
	/** Objects associated with a button */
	private ObjectMap<Actor, Object> mValues = new ObjectMap<>();
	/** Validations when a button is pressed */
	private ObjectMap<Actor, IValidate[]> mValidations = new ObjectMap<>();
	/** Default cancel text */
	private static final String CANCEL_TEXT = "Cancel";
	/** Button table */
	private AlignTable mButtonTable = new AlignTable();
	/** If the message box is hiding */
	protected boolean mHiding = false;
	/** Skin of the message box */
	protected Skin mSkin = null;
	/** UiFactory */
	private static UiFactory mUiFactory = UiFactory.getInstance();
}

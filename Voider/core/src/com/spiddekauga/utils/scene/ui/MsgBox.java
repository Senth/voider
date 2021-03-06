package com.spiddekauga.utils.scene.ui;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
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
import com.spiddekauga.utils.commands.Command;
import com.spiddekauga.utils.scene.ui.validate.IValidate;
import com.spiddekauga.voider.repo.resource.SkinNames;
import com.spiddekauga.voider.scene.ui.UiFactory;
import com.spiddekauga.voider.scene.ui.UiStyles.TextButtonStyles;

/**
 * Message box wrapper for dialog. This allows other content than just text.
 */
public class MsgBox extends Dialog implements IDialog {
private static final String CANCEL_TEXT = "Cancel";
private static final float mFadeInTime;
private static final float mFadeOutTime;
private static final UiFactory mUiFactory = UiFactory.getInstance();

static {
	mFadeInTime = SkinNames.getResource(SkinNames.GeneralVars.MSGBOX_FADE_IN);
	mFadeOutTime = SkinNames.getResource(SkinNames.GeneralVars.MSGBOX_FADE_OUT);
}

/** If the message box is hiding */
private boolean mHidden = false;
private Skin mSkin = null;
private Field mfCancelHide = null;
private float mButtonPad = 0;
/** Objects associated with a button */
private ObjectMap<Actor, Object> mValues = new ObjectMap<>();
/** Validations when a button is pressed */
private ObjectMap<Actor, IValidate[]> mValidations = new ObjectMap<>();
private AlignTable mButtonTable = new AlignTable();

/**
 * Creates a message box with a skin to use for the message box
 * @param skin skin for the window, buttons, and text.
 */
MsgBox(Skin skin) {
	super("", skin);
	mSkin = skin;
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

/**
 * @return new default fade out action for message boxes
 */
private static Action fadeOutActionDefault() {
	return Actions.fadeOut(mFadeOutTime, Interpolation.linear);
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
 * Clears the content and button tables. Sets message box as modal, non-movable, and keep within
 * stage.
 */
@Override
public void clear() {
	setMovable(false);
	setModal(true);
	setKeepWithinStage(true);
	setTitle("");
	getTitleTable().align(Align.center);
	getContentTable().clearChildren();
	mButtonTable.dispose();
	mValues.clear();
	clearActions();
	clearListeners();
}

/**
 * Set title of the message box
 */
public void setTitle(String title) {
	Label label = getTitleLabel();
	label.setText(title);
	label.setAlignment(Align.center);
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
 * Adds a label to the content table. The dialog must have been constructed with a skin to use this
 * method.
 * @param text the text to write in the content
 * @return this message box for chaining
 * @see #text(String) does exactly the same thing
 */
public Cell<? extends Actor> content(String text) {
	return content(text, (Integer) null);
}

/**
 * Adds a label to the content table. The dialog must have been constructed with a skin to use this
 * method.
 * @param text the text to write in the content
 * @param alignment text alignment, null to use default
 * @return this message box for chaining
 * @see #text(String) does exactly the same thing
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
	return content(label);
}

/**
 * Adds an actor to the content table
 * @param actor will be added to the content table
 * @return cell for this content
 */
public Cell<? extends Actor> content(Actor actor) {
	Cell<? extends Actor> cell = getContentTable().add(actor);
	if (actor instanceof AlignTable) {
		((AlignTable) actor).layout();
	}
	return cell;
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
 * Adds a row to the content with specific padding
 * @param padTop top padding
 * @param padBottom bottom padding
 * @return this for chaining
 */
public MsgBox contentRow(float padTop, float padBottom) {
	Cell<?> cell = getContentTable().row();
	cell.padTop(padTop).padBottom(padBottom);
	return this;
}

/**
 * Invalid, don't call this. Throws an exception. Use {@link #content(String)} instead.
 */
@Override
@Deprecated
public MsgBox text(String text) {
	throw new IllegalAccessError("This method should never be called, use context() instead");
}

/**
 * Invalid, don't call this. Throws an exception. Use {@link #content(String, LabelStyle)} instead
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
 * @deprecated This method should never be called. Create the button outside of this class instead
 * then use {@link #button(Button)} or {@link #button(Button, Object)}.
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

@Override
public MsgBox show(Stage stage, Action action) {
	mButtonTable.layout();
	getColor().a = 0;
	super.show(stage, Actions.sequence(action, new DialogEvent.PostEventAction(DialogEvent.EventTypes.SHOW)));
	mHidden = false;
	return this;
}

@Override
public MsgBox show(Stage stage) {
	show(stage, fadeInActionDefault());
	return this;
}

@Override
public void hide(Action action) {
	Action sequenceAction = Actions.sequence(action,
			new DialogEvent.PostEventAction(DialogEvent.EventTypes.REMOVE));
	super.hide(sequenceAction);
	mHidden = true;
}

@Override
public void hide() {
	hide(fadeOutActionDefault());
}

@Override
public void setObject(Actor actor, Object object) {
	if (object != null) {
		mValues.put(actor, object);
	}
}

/**
 * Called when a button is clicked. The dialog will be hidden after this method returns unless
 * {@link #cancel()} is called.
 * @param object The object specified when the button was added. If the object is a {@link Command}
 * {@link Command#execute()} will be called, if the execution failed {@link #cancel()} will be
 * called. {@link Command#dispose()} is called at the end.
 */
@Override
public void result(Object object) {
	if (object instanceof Command) {
		boolean success = ((Command) object).execute();
		if (!success) {
			cancel();
		}

		((Command) object).dispose();
	}
}

/**
 * @return a new default fade in action for message boxes
 */
public static Action fadeInActionDefault() {
	return Actions.fadeIn(mFadeInTime, Interpolation.linear);
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
 * Show the message box
 */
public MsgBox show() {
	DialogShower.show(this);
	return this;
}

/**
 * Adds a new row to the button table
 * @return this message box for chaining
 */
public MsgBox buttonRow() {
	mButtonTable.row().setPadTop(mUiFactory.getStyles().vars.paddingButton);
	return this;
}

/**
 * Creates a new button with the specified text
 * @param text text of the button using the default style
 * @param object The object that will be passed to {@link #result(Object)} if this button is
 * clicked. May be null.
 * @param validations optional validations. The object is only passed if all validations are valid
 * @return this for chaining
 */
public MsgBox button(String text, Object object, IValidate... validations) {
	Button button = addButton(text);
	setObject(button, object, validations);
	return this;
}

/**
 * Associate an actor with object and possible validations
 * @return this for chaining
 */
private MsgBox setObject(Actor actor, Object object, IValidate... validations) {
	setObject(actor, object);
	if (validations != null && validations.length > 0) {
		mValidations.put(actor, validations);
	}
	return this;
}

/**
 * Adds the given button to the button table.
 * @param button the button to add.
 * @param object The object that will be passed to {@link #result(Object)} if this button is
 * clicked. May be null.
 * @param validations optional validations. The object is only passed if all validations are valid
 * @return this for chaining
 */
public MsgBox button(Button button, Object object, IValidate... validations) {
	mButtonTable.add(button);
	setObject(button, object, validations);
	return this;
}

/**
 * @return get last button cell, null if no buttons have been added in the current row.
 */
public com.spiddekauga.utils.scene.ui.Cell getButtonCell() {
	return mButtonTable.getCell();
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
 * @param buttonText text for the cancel button
 * @param cancelCommand this command shall return true in execute if the message box is allowed to
 * cancel.
 * @return this message box for chaining
 * @see #addCancelButtonAndKeys()
 */
public MsgBox addCancelButtonAndKeys(String buttonText, Command cancelCommand) {
	button(buttonText, cancelCommand);
	addCancelKeys();
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
 * Add a cancel button and keys.
 * @param cancelCommand this command shall return true in execute if the message box is allowed to
 * cancel.
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
 * @param cancelCommand this command shall return true in execute if the message box is allowed to
 * cancel.
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
	return mHidden && !isHideInProgress();
}

/**
 * @return true if hiding is in progress
 */
private boolean isHideInProgress() {
	return mHidden && getStage() != null;
}

/**
 * @return current button padding
 */
public float getButtonPad() {
	return mButtonPad;
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

/**
 * Fade out the message box. This will sort of hide the message box.
 */
public void fadeOut() {
	clearActions();
	addAction(fadeOutActionDefault());
}

/**
 * Fade in a message box
 */
public void fadeIn() {
	clearActions();
	getStage().setKeyboardFocus(this);
	addAction(fadeInActionDefault());
}
}

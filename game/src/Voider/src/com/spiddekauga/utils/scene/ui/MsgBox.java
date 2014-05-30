package com.spiddekauga.utils.scene.ui;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Field;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.spiddekauga.utils.commands.Command;
import com.spiddekauga.utils.scene.ui.UiFactory.TextButtonStyles;

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
		mButtonTable.setPadding(paddingInner);

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
				if (!mValues.containsKey(actor)) {
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
		});
	}

	@Override
	public void setObject(Actor actor, Object object) {
		mValues.put(actor, object);
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
	}

	/**
	 * Clears the content
	 */
	public void clearContent() {
		getContentTable().clearChildren();
	}

	/**
	 * Adds an actor to the content table
	 * @param actor will be added to the content table
	 * @return this message box for chaining
	 */
	public MsgBox content(Actor actor) {
		getContentTable().add(actor);
		if (actor instanceof AlignTable) {
			((AlignTable) actor).layout();
		}
		return this;
	}

	/**
	 * Adds a label to the content table. The dialog must have been constructed with a
	 * skin to use this method.
	 * @param text the text to write in the content
	 * @see #text(String) does exactly the same thing
	 * @return this message box for chaining
	 */
	public MsgBox content(String text) {
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
	public MsgBox content(String text, LabelStyle labelStyle) {
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
		mButtonTable.row();
		return this;
	}


	/**
	 * Adds a text button to the button table. Null will be passed to
	 * {@link #result(Object)} if this button is clicked. Will use UiFactory to create the
	 * button
	 */
	@Override
	public MsgBox button(String text) {
		return button(text, null);
	}

	/**
	 * Adds a text button to the button table. Will use UiFactory to create the button
	 * @param object The object that will be passed to {@link #result(Object)} if this
	 *        button is clicked. May be null.
	 */
	@Override
	public MsgBox button(String text, Object object) {
		Cell cell = mUiFactory.addTextButton(text, TextButtonStyles.PRESS, mButtonTable, null, null);
		setObject(cell.getActor(), object);
		return this;
	}

	/**
	 * Adds a text button to the button table.
	 * @param object The object that will be passed to {@link #result(Object)} if this
	 *        button is clicked. May be null.
	 */
	@Override
	public MsgBox button(String text, Object object, TextButtonStyle buttonStyle) {
		return button(new TextButton(text, buttonStyle), object);
	}

	/** Adds the given button to the button table. */
	@Override
	public MsgBox button(Button button) {
		return button(button, null);
	}

	/**
	 * Adds the given button to the button table.
	 * @param object The object that will be passed to {@link #result(Object)} if this
	 *        button is clicked. May be null.
	 */
	@Override
	public MsgBox button(Button button, Object object) {
		mButtonTable.add(button);
		setObject(button, object);
		return this;
	}

	/** {@link #pack() Packs} the dialog and adds it to the stage, centered. */
	@Override
	public MsgBox show(Stage stage) {
		mButtonTable.layout();
		super.show(stage);
		mHiding = false;
		return this;
	}

	@Override
	public void hide() {
		super.hide();
		mHiding = true;
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
		key(Keys.BACK, cancelCommand);
		key(Keys.ESCAPE, cancelCommand);
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

	/** Pointer to cancelHide in Dialog */
	Field mfCancelHide = null;
	/** Objects associated with a button */
	ObjectMap<Actor, Object> mValues = new ObjectMap<>();
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

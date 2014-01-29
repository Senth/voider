package com.badlogic.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.spiddekauga.utils.commands.Command;
import com.spiddekauga.utils.scene.ui.AlignTable;
import com.spiddekauga.utils.scene.ui.Label;
import com.spiddekauga.utils.scene.ui.Label.LabelStyle;

/**
 * Message box wrapper for dialog. This allows other content than just text.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class MsgBox extends Dialog {
	/**
	 * Creates a message box with a skin to use for the message box
	 * @param skin skin for the window, buttons, and text.
	 */
	public MsgBox(Skin skin) {
		super("", skin);
		mSkin = skin;
	}

	/**
	 * Creates a message box with a window style to use from
	 * the specified skin
	 * @param skin the skin to find the window style in.
	 * @param windowStyleName name of the window style found in skin
	 */
	public MsgBox(Skin skin, String windowStyleName) {
		super("", skin, windowStyleName);
		mSkin = skin;
	}

	/**
	 * Creates a message box with a window style
	 * @param windowStyle the window style to use for the message box
	 */
	public MsgBox(WindowStyle windowStyle) {
		super("", windowStyle);
	}

	/**
	 * Clears the content and button tables.
	 * Sets message box as modal, non-movable, and keep within stage.
	 */
	@Override
	public void clear() {
		setMovable(false);
		setModal(true);
		setKeepWithinStage(true);
		setTitle("");
		setTitleAlignment(Align.center);
		contentTable.clearChildren();
		buttonTable.clearChildren();
		clearActions();
	}

	/**
	 * Clears the content
	 */
	public void clearContent() {
		contentTable.clearChildren();
	}

	/**
	 * Adds an actor to the content table
	 * @param actor will be added to the content table
	 * @return this message box for chaining
	 */
	public MsgBox content(Actor actor) {
		contentTable.add(actor);
		if (actor instanceof AlignTable) {
			((AlignTable) actor).layout();
		}
		return this;
	}

	/**
	 * Adds a label to the content table.
	 * The dialog must have been constructed with a skin to use this method.
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
		contentTable.row();
		return this;
	}

	/**
	 * Adds a label to the content table.
	 * The dialog must have been constructed with a skin to use this method.
	 */
	@Override
	@Deprecated
	public MsgBox text (String text) {
		return content(text);
	}

	/**
	 * Invalid, don't call this. Throws an exception
	 */
	@Override
	@Deprecated
	public MsgBox text (String text, com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle labelStyle) {
		throw new IllegalAccessError("This method should never be called");
	}

	/**
	 * Invalid, don't call this. Throws an exception
	 */
	@Override
	@Deprecated
	public MsgBox text (com.badlogic.gdx.scenes.scene2d.ui.Label label) {
		throw new IllegalAccessError("This method should never be called");
	}

	/**
	 * Adds a new row to the button table
	 * @return this message box for chaining
	 */
	public MsgBox buttonRow() {
		buttonTable.row();
		return this;
	}


	/** Adds a text button to the button table. Null will be passed to {@link #result(Object)} if this button is clicked. The dialog
	 * must have been constructed with a skin to use this method. */
	@Override
	public MsgBox button (String text) {
		return button(text, null);
	}

	/** Adds a text button to the button table. The dialog must have been constructed with a skin to use this method.
	 * @param object The object that will be passed to {@link #result(Object)} if this button is clicked. May be null. */
	@Override
	public MsgBox button (String text, Object object) {
		super.button(text, object);
		return this;
	}

	/** Adds a text button to the button table.
	 * @param object The object that will be passed to {@link #result(Object)} if this button is clicked. May be null. */
	@Override
	public MsgBox button (String text, Object object, TextButtonStyle buttonStyle) {
		return button(new TextButton(text, buttonStyle), object);
	}

	/** Adds the given button to the button table. */
	@Override
	public MsgBox button (Button button) {
		return button(button, null);
	}

	/** Adds the given button to the button table.
	 * @param object The object that will be passed to {@link #result(Object)} if this button is clicked. May be null. */
	@Override
	public MsgBox button (Button button, Object object) {
		super.button(button, object);
		return this;
	}

	/** {@link #pack() Packs} the dialog and adds it to the stage, centered. */
	@Override
	public MsgBox show (Stage stage) {
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
		return addCancelButtonAndKeys("Cancel");
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
	 * @param cancelCommand this command shall return true in execute if the message box
	 * is allowed to cancel.
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
	 * is allowed to cancel.
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

	/** If the message box is hiding */
	protected boolean mHiding = false;
	/** Skin of the message box */
	protected Skin mSkin = null;
}

package com.badlogic.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;

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
	}

	/**
	 * Creates a message box with a window style to use from
	 * the specified skin
	 * @param skin the skin to find the window style in.
	 * @param windowStyleName name of the window style found in skin
	 */
	public MsgBox(Skin skin, String windowStyleName) {
		super("", skin, windowStyleName);
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
		contentTable.clear();
		buttonTable.clear();
	}

	/**
	 * Adds an actor to the content table
	 * @param actor will be added to the content table
	 * @return this message box for chaining
	 */
	public MsgBox content(Actor actor) {
		contentTable.add(actor);
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
		super.text(text);
		return this;
	}

	/**
	 * Adds a label to the content table.
	 * @param text text to write in the content
	 * @param labelStyle what label style to use for the label
	 * @see #text(String, LabelStyle) does exactly the same thing
	 * @return this message box for chaining
	 */
	public MsgBox content(String text, LabelStyle labelStyle) {
		super.text(text, labelStyle);
		return this;
	}

	/**
	 * Adds a label to the content table.
	 * The dialog must have been constructed with a skin to use this method.
	 */
	@Override
	public MsgBox text (String text) {
		super.text(text);
		return this;
	}

	/** Adds a label to the content table. */
	@Override
	public MsgBox text (String text, LabelStyle labelStyle) {
		super.text(text, labelStyle);
		return this;
	}

	/** Adds the given Label to the content table */
	@Override
	public MsgBox text (Label label) {
		contentTable.add(label);
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
		return this;
	}
}

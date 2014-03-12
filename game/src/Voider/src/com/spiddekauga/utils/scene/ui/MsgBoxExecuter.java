package com.spiddekauga.utils.scene.ui;

import com.badlogic.gdx.scenes.scene2d.ui.MsgBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.spiddekauga.utils.commands.Command;

/**
 * Wrapper for the message box. This message box executes the commands passed
 * for the buttons.
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class MsgBoxExecuter extends MsgBox {
	/**
	 * Creates a message box with a skin to use for the message box
	 * @param skin skin for the window, buttons, and text.
	 */
	public MsgBoxExecuter(Skin skin) {
		super(skin);
	}

	/**
	 * Creates a message box with a window style to use from
	 * the specified skin
	 * @param skin the skin to find the window style in.
	 * @param windowStyleName name of the window style found in skin
	 */
	public MsgBoxExecuter(Skin skin, String windowStyleName) {
		super(skin, windowStyleName);
	}

	/**
	 * Creates a message box with a window style
	 * @param windowStyle the window style to use for the message box
	 */
	public MsgBoxExecuter(WindowStyle windowStyle) {
		super(windowStyle);
	}

	@Override
	public void result(Object object) {
		if (object instanceof Command) {
			((Command) object).execute();
			((Command) object).dispose();
		}
	}
}

package com.spiddekauga.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;

/**
 * Helper functions for keys
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class KeyHelper {
	/**
	 * Checks if an undo button has been pressed. As of now this will
	 * return true if Ctrl+Z.
	 * @param keycode the key to check
	 * @return true if the undo key has been pressed, will return false if isRedoPressed()
	 * returns true
	 */
	public static boolean isUndoPressed(int keycode) {
		// Undo - Ctrl + Z
		if (keycode == Keys.Z && isCtrlPressed()) {
			if (!isRedoPressed(keycode)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Checks if the redo button has bene pressed. As of now it will
	 * return true if Ctrl+Shift+Z or Ctrl+Y.
	 * @param keycode the key to check
	 * @return true if the redo key has been pressed
	 */
	public static boolean isRedoPressed(int keycode) {
		// Redo - Ctrl + Shift + Z || Ctrl + Y
		if (keycode == Keys.Z && isCtrlPressed() && isShiftPressed()) {
			return true;
		} else if (keycode == Keys.Y && isCtrlPressed()) {
			return true;
		}
		return false;
	}

	/**
	 * @return true if any control key is pressed
	 */
	public static boolean isCtrlPressed() {
		return Gdx.input.isKeyPressed(Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Keys.CONTROL_RIGHT);
	}

	/**
	 * @return true if any shift key is pressed
	 */
	public static boolean isShiftPressed() {
		return Gdx.input.isKeyPressed(Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Keys.SHIFT_RIGHT);
	}
}

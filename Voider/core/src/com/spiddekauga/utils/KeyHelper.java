package com.spiddekauga.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;

/**
 * Helper functions for keys
 */
public class KeyHelper {
/**
 * Checks if delete is pressed
 * @param keycode the key to check
 * @return true if a delete key was pressed
 */
public static boolean isDeletePressed(int keycode) {
	return keycode == Keys.BACKSPACE || keycode == Keys.FORWARD_DEL;
}

/**
 * Checks if back/Escape is pressed
 * @param keycode the key to check
 * @return true if the back or escape key is pressed
 */
public static boolean isBackPressed(int keycode) {
	return keycode == Keys.BACK || keycode == Keys.ESCAPE;
}

/**
 * Checks if an undo button has been pressed. As of now this will return true if Ctrl+Z.
 * @param keycode the key to check
 * @return true if the undo key has been pressed, will return false if isRedoPressed() returns true
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
 * Checks if the redo button has bene pressed. As of now it will return true if Ctrl+Shift+Z or
 * Ctrl+Y.
 * @param keycode the key to check
 * @return true if the redo key has been pressed
 */
public static boolean isRedoPressed(int keycode) {
	// Redo - Ctrl + Shift + Z || Ctrl + Y
	if (keycode == Keys.Z && isCtrlShiftPressed()) {
		return true;
	} else if (keycode == Keys.Y && isCtrlPressed()) {
		return true;
	}
	return false;
}

/**
 * @return true if any control key is pressed (but not shift or alt)
 */
public static boolean isCtrlPressed() {
	return isCtrl() && !isShift() && !isAlt() && !isCtrlAlt();
}

/**
 * @return true if any shift key is pressed (but not ctrl or alt)
 */
public static boolean isShiftPressed() {
	return isShift() && !isCtrl() && !isAlt() && !isCtrlAlt();
}

/**
 * @return true if any shift key is pressed
 */
private static boolean isShift() {
	return isKeyPressed(Keys.SHIFT_LEFT) || isKeyPressed(Keys.SHIFT_RIGHT);
}

/**
 * @return true if any control key is pressed
 */
private static boolean isCtrl() {
	return isKeyPressed(Keys.CONTROL_LEFT) || isKeyPressed(Keys.CONTROL_RIGHT);
}

/**
 * @return true if any alt key is pressed
 */
private static boolean isAlt() {
	return isKeyPressed(Keys.ALT_LEFT);
}

/**
 * @return true if ctrl + alt is pressed (or Alt Gr)
 */
private static boolean isCtrlAlt() {
	return (isCtrl() && isAlt()) || isKeyPressed(Keys.ALT_RIGHT);
}

/**
 * If a keys is pressed
 * @param keycode the keycode to check
 * @return true if key is pressed
 */
private static boolean isKeyPressed(int keycode) {
	return Gdx.input.isKeyPressed(keycode);
}

/**
 * @return true if any alt key is pressed (but not ctrl or shift)
 */
public static boolean isAltPressed() {
	return isAlt() && !isCtrl() && !isShift() && !isCtrlAlt();
}

/**
 * @return true if both control + shift is pressed (but not alt)
 */
public static boolean isCtrlShiftPressed() {
	return isCtrl() && isShift() && !isAlt() && !isCtrlAlt();
}

/**
 * @return true if both control + alt is pressed (but not shift)
 */
public static boolean isCtrlAltPressed() {
	return isCtrlAlt() && !isShift();
}

/**
 * @return true if both shift + alt is pressed (but not control)
 */
public static boolean isShiftAltPressed() {
	return isShift() && isAlt() && !isCtrl() && !isCtrlAlt();
}

/**
 * @return true if control + alt + shift is pressed
 */
public static boolean isCtrlAltShiftPressed() {
	return isShift() && isCtrlAlt();
}

/**
 * @return true if no modifier is pressed
 */
public static boolean isNoModifiersPressed() {
	return !isCtrl() && !isAlt() && !isShift() && !isCtrlAlt();
}

/**
 * @param button the current button pressed on the mouse
 * @return true if we shall start scrolling
 */
public static boolean isScrolling(int button) {
	// Middle mouse button
	if (button == 2) {
		return true;
	}
	// Touched with two fingers
	else if (Gdx.app.getInput().isTouched(0) && Gdx.app.getInput().isTouched(1)) {
		return true;
	}
	// Holding space with first button or one finger
	else if (button == 0 && Gdx.app.getInput().isKeyPressed(Keys.SPACE)) {
		return true;
	}

	return false;
}
}

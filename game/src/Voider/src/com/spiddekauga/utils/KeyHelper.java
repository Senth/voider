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
	 * Checks if delete is pressed
	 * @param keycode the key to check
	 * @return true if a delete key was pressed
	 */
	public static boolean isDeletePressed(int keycode) {
		return keycode == Keys.DEL || keycode == Keys.BACKSPACE || keycode == Keys.FORWARD_DEL;
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

	/**
	 * @return true if we shall start scrolling
	 * @param button the current button pressed on the mouse
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

	/**
	 * Tooltip helper keys
	 */
	public static class Tooltip {
		/**
		 * @param keycode the key to check
		 * @return true if we shall show YouTube
		 */
		public static boolean isShowYoutubePressed(int keycode) {
			return keycode == Keys.Y;
		}

		/**
		 * @param keycode the key to check
		 * @return true if we shall toggle the animation window in the tooltip
		 */
		public static boolean isToggleAnimationPressed(int keycode) {
			return keycode == Keys.A;
		}

		/**
		 * @param keycode the key to check
		 * @return true if we shall go to the next animation in the tooltip
		 */
		public static boolean isNextAnimationPressed(int keycode) {
			return keycode == Keys.SPACE;
		}

		/**
		 * @param keycode the key to check
		 * @return true if we shall toggle the text description
		 */
		public static boolean isToggleTextPressed(int keycode) {
			return keycode == Keys.T;
		}
	}
}

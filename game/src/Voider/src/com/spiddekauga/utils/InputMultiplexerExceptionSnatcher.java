package com.spiddekauga.utils;

import com.badlogic.gdx.InputMultiplexer;

/**
 * Snatches all exceptions thrown by the input multiplexer and sends them to
 * the appropriate exception handler
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class InputMultiplexerExceptionSnatcher extends InputMultiplexer {
	/**
	 * Creates an input multiplexer with an exception handler
	 * @param exceptionHandler the class to handle the exceptions
	 */
	public InputMultiplexerExceptionSnatcher(IExceptionHandler exceptionHandler) {
		mExceptionHandler = exceptionHandler;
	}

	@Override
	public boolean keyDown (int keycode) {
		try {
			return super.keyDown(keycode);
		} catch (Exception e) {
			mExceptionHandler.handleException(e);
		}
		return false;
	}

	@Override
	public boolean keyUp (int keycode) {
		try {
			return super.keyUp(keycode);
		} catch (Exception e) {
			mExceptionHandler.handleException(e);
		}
		return false;
	}

	@Override
	public boolean keyTyped (char character) {
		try {
			return super.keyTyped(character);
		} catch (Exception e) {
			mExceptionHandler.handleException(e);
		}
		return false;
	}

	@Override
	public boolean touchDown (int screenX, int screenY, int pointer, int button) {
		try {
			return super.touchDown(screenX, screenY, pointer, button);
		} catch (Exception e) {
			mExceptionHandler.handleException(e);
		}
		return false;
	}

	@Override
	public boolean touchUp (int screenX, int screenY, int pointer, int button) {
		try {
			return super.touchUp(screenX, screenY, pointer, button);
		} catch (Exception e) {
			mExceptionHandler.handleException(e);
		}
		return false;
	}

	@Override
	public boolean touchDragged (int screenX, int screenY, int pointer) {
		try {
			return super.touchDragged(screenX, screenY, pointer);
		} catch (Exception e) {
			mExceptionHandler.handleException(e);
		}
		return false;
	}

	@Override
	public boolean mouseMoved (int screenX, int screenY) {
		try {
			return super.mouseMoved(screenX, screenY);
		} catch (Exception e) {
			mExceptionHandler.handleException(e);
		}
		return false;
	}

	@Override
	public boolean scrolled (int amount) {
		try {
			return super.scrolled(amount);
		} catch (Exception e) {
			mExceptionHandler.handleException(e);
		}
		return false;
	}

	/** The class to send all exceptions too */
	IExceptionHandler mExceptionHandler;
}

package com.spiddekauga.utils;

import com.badlogic.gdx.InputMultiplexer;
import com.spiddekauga.voider.Config;

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
		} catch (RuntimeException e) {
			if (Config.Debug.EXCEPTION_HANDLER) {
				mExceptionHandler.handleException(e);
			} else {
				throw e;
			}
		}
		return false;
	}

	@Override
	public boolean keyUp (int keycode) {
		try {
			return super.keyUp(keycode);
		} catch (RuntimeException e) {
			if (Config.Debug.EXCEPTION_HANDLER) {
				mExceptionHandler.handleException(e);
			} else {
				throw e;
			}
		}
		return false;
	}

	@Override
	public boolean keyTyped (char character) {
		try {
			return super.keyTyped(character);
		} catch (RuntimeException e) {
			if (Config.Debug.EXCEPTION_HANDLER) {
				mExceptionHandler.handleException(e);
			} else {
				throw e;
			}
		}
		return false;
	}

	@Override
	public boolean touchDown (int screenX, int screenY, int pointer, int button) {
		try {
			return super.touchDown(screenX, screenY, pointer, button);
		} catch (RuntimeException e) {
			if (Config.Debug.EXCEPTION_HANDLER) {
				mExceptionHandler.handleException(e);
			} else {
				throw e;
			}
		}
		return false;
	}

	@Override
	public boolean touchUp (int screenX, int screenY, int pointer, int button) {
		try {
			return super.touchUp(screenX, screenY, pointer, button);
		} catch (RuntimeException e) {
			if (Config.Debug.EXCEPTION_HANDLER) {
				mExceptionHandler.handleException(e);
			} else {
				throw e;
			}
		}
		return false;
	}

	@Override
	public boolean touchDragged (int screenX, int screenY, int pointer) {
		try {
			return super.touchDragged(screenX, screenY, pointer);
		} catch (RuntimeException e) {
			if (Config.Debug.EXCEPTION_HANDLER) {
				mExceptionHandler.handleException(e);
			} else {
				throw e;
			}
		}
		return false;
	}

	@Override
	public boolean mouseMoved (int screenX, int screenY) {
		try {
			return super.mouseMoved(screenX, screenY);
		} catch (RuntimeException e) {
			if (Config.Debug.EXCEPTION_HANDLER) {
				mExceptionHandler.handleException(e);
			} else {
				throw e;
			}
		}
		return false;
	}

	@Override
	public boolean scrolled (int amount) {
		try {
			return super.scrolled(amount);
		} catch (RuntimeException e) {
			if (Config.Debug.EXCEPTION_HANDLER) {
				mExceptionHandler.handleException(e);
			} else {
				throw e;
			}
		}
		return false;
	}

	/** The class to send all exceptions too */
	IExceptionHandler mExceptionHandler;
}

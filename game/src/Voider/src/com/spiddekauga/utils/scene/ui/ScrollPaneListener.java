package com.spiddekauga.utils.scene.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputEvent.Type;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;

/**
 * Listens to a scroll pane events. Current events are hits a side
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class ScrollPaneListener implements EventListener {
	/**
	 * Creates a scroll pane listener that will send events when
	 * the scroll pane hits a side.
	 * @param scrollPane the scroll pane to listen to.
	 */
	public ScrollPaneListener(ScrollPane scrollPane) {
		mScrollPane = scrollPane;
		mScrollPane.addListener(this);
	}

	@Override
	public boolean handle(Event event) {

		float maxScrollX = mScrollPane.getWidget().getWidth() - mScrollPane.getWidth();
		if (maxScrollX < 0) {
			maxScrollX = 0;
		}
		float maxScrollY = mScrollPane.getWidget().getHeight() - mScrollPane.getHeight();
		if (maxScrollY < 0) {
			maxScrollY = 0;
		}

		// Input
		if (event instanceof InputEvent) {
			InputEvent inputEvent = (InputEvent) event;

			// Return true for touch down, so we get touch dragged events.
			if (inputEvent.getType() == Type.touchDown) {
				return true;
			}


			// Scrolling
			else if (inputEvent.getType() == Type.scrolled) {
				boolean scrolledUp = inputEvent.getScrollAmount() < 0;

				if (scrolledUp) {
					if (mScrollPane.getVisualScrollY() <= 0) {
						onHitTop();
					}
				} else {
					if (mScrollPane.getVisualScrollY() >= maxScrollY) {
						onHitBottom();
					}
				}
			}


			// Dragging
			else if (inputEvent.getType() == Type.touchDragged) {
				debug("" + inputEvent.getScrollAmount() + ", max (" + maxScrollX + ";" + maxScrollY + ") -- ");
				// Left
				if (mHitLeft) {
					if (mScrollPane.getVisualScrollX() >= 0) {
						mHitLeft = false;
					}
				} else {
					if (mScrollPane.getVisualScrollX() < 0) {
						mHitLeft = true;
						onHitLeft();
					}
				}

				// Right
				if (mHitRight) {
					if (mScrollPane.getVisualScrollX() <= maxScrollX) {
						mHitRight = false;
					}
				} else {
					if (mScrollPane.getVisualScrollX() > maxScrollX) {
						mHitRight = true;
						onHitRight();
					}
				}

				// Top
				if (mHitTop) {
					if (mScrollPane.getVisualScrollY() <= maxScrollY) {
						mHitTop = false;
					}
				} else {
					if (mScrollPane.getVisualScrollY() > maxScrollY) {
						mHitTop = true;
						onHitTop();
					}
				}

				// Bottom
				if (mHitBottom) {
					if (mScrollPane.getVisualScrollY() >= 0) {
						mHitBottom = false;
					}
				} else {
					if (mScrollPane.getVisualScrollY() < 0) {
						mHitBottom = true;
						onHitBottom();
					}
				}
			}
		}
		return false;
	}

	/**
	 * Print debug
	 * @param method the method that was called
	 */
	private void debug(String method) {
		Gdx.app.debug("ScrollPaneListener", method + ", "
				+ "amount (" + mScrollPane.getScrollX() + ";" + mScrollPane.getScrollY() + ")");
	}

	/**
	 * Called when left side is hit
	 */
	protected void onHitLeft() {
		// Does nothing
		debug("onHitLeft()");
	}

	/**
	 * Called when right side is hit
	 */
	protected void onHitRight() {
		// Does nothing
		debug("onHitRight()");
	}

	/**
	 * Called when top side is hit
	 */
	protected void onHitTop() {
		// Does nothing
		debug("onHitTop()");
	}

	/**
	 * Called when bottom side is hit
	 */
	protected void onHitBottom() {
		// Does nothing
		debug("onHitBottom()");
	}

	/** Scroll pane we're listening to */
	protected ScrollPane mScrollPane;
	/** If we're currently hittitng the top */
	private boolean mHitTop = false;
	/** If we're currently hitting the bottom */
	private boolean mHitBottom = false;
	/** If we're currently hitting the left */
	private boolean mHitLeft = false;
	/** If we're currently hitting the right */
	private boolean mHitRight = false;
}

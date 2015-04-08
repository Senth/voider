package com.spiddekauga.voider.editor.tools;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.spiddekauga.utils.KeyHelper;
import com.spiddekauga.utils.Screens;
import com.spiddekauga.utils.Scroller;
import com.spiddekauga.utils.Scroller.ScrollAxis;
import com.spiddekauga.voider.editor.IResourceChangeEditor;
import com.spiddekauga.voider.editor.commands.CCameraMove;
import com.spiddekauga.voider.resources.IResource;
import com.spiddekauga.voider.utils.event.EventDispatcher;
import com.spiddekauga.voider.utils.event.EventTypes;
import com.spiddekauga.voider.utils.event.GameEvent;

/**
 * Pan tool
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class PanTool extends TouchTool {
	/**
	 * @param editor
	 */
	public PanTool(IResourceChangeEditor editor) {
		super(editor, null);

		// Add all resources so that they don't get deselected
		// This tool hijacks all event messages anyway
		mSelectableResourceTypes.add(IResource.class);
	}

	@Override
	protected boolean down(int button) {
		boolean willScroll = false;

		// When active always scroll
		if (isActive()) {
			willScroll = true;
		}
		// Only scroll with specific button
		else {
			willScroll = KeyHelper.isScrolling(button);
		}


		if (willScroll) {
			// Already scrolling, create scroll command to stop the scrolling
			if (mScroller.isScrolling()) {
				createCameraMoveCommand();
			}

			mScroller.touchDown((int) mScreenCurrent.x, (int) mScreenCurrent.y);
			mScrollCameraOrigin.set(mCamera.position.x, mCamera.position.y);
			mCreatedScrollCommand = false;

			return true;
		}

		return mScroller.isScrolling();
	}

	@Override
	protected boolean dragged() {
		if (mScroller.isScrollingByHand()) {
			mScroller.touchDragged((int) mScreenCurrent.x, (int) mScreenCurrent.y);
			return true;
		}

		return false;
	}

	@Override
	protected boolean up(int button) {
		if (mScroller.isScrollingByHand()) {
			if (isActive() || KeyHelper.isScrolling(button)) {
				mScroller.touchUp((int) mScreenCurrent.x, (int) mScreenCurrent.y);

				if (!mScroller.isScrolling()) {
					// mCreatedScrollCommand = false;
				}

				return true;
			}
		}

		return false;
	}

	/**
	 * Updates the pan tool
	 * @param deltaTime time elapsed since last frame
	 */
	public void update(float deltaTime) {
		// Scrolling
		if (mScroller.isScrolling()) {
			mScroller.update(deltaTime);

			Vector2 diffScroll = new Vector2(mScroller.getOriginScroll()).sub(mScroller.getCurrentScroll());
			float scale = getWorldWidth() / Gdx.graphics.getWidth();

			mCamera.position.x = diffScroll.x * scale + mScrollCameraOrigin.x;
			mCamera.position.y = -diffScroll.y * scale + mScrollCameraOrigin.y;
			Screens.clampCamera(mCamera, mWorldMin, mWorldMax);

			mCamera.update();
			EventDispatcher.getInstance().fire(new GameEvent(EventTypes.CAMERA_MOVED));
		} else if (!mCreatedScrollCommand) {
			createCameraMoveCommand();
			mCreatedScrollCommand = true;
		}
	}

	/**
	 * Create camera move command in the current location
	 */
	private void createCameraMoveCommand() {
		Vector2 scrollCameraCurrent = new Vector2(mCamera.position.x, mCamera.position.y);
		mInvoker.execute(new CCameraMove(mCamera, scrollCameraCurrent, mScrollCameraOrigin));
	}

	/**
	 * @return true if currently panning
	 */
	public boolean isPanning() {
		return mScroller.isScrolling();
	}

	/**
	 * Force pan to stop
	 */
	public void stop() {
		mScroller.stop();
	}

	/**
	 * @return world width
	 */
	private float getWorldWidth() {
		return mCamera.viewportWidth * mCamera.zoom;
	}

	/**
	 * Set minimum camera/world position. Clamps zoom to this value
	 * @param x minimum x world position
	 * @param y minimum y world position
	 */
	public void setWorldMin(float x, float y) {
		mWorldMin.set(x, y);
	}

	/**
	 * Sets minimum x camera/world position. Clamps zoom to this value
	 * @param x minimum x world position
	 */
	public void setWorldMinX(float x) {
		mWorldMin.x = x;
	}

	/**
	 * Sets minimum y camera/world position. Clamps zoom to this value
	 * @param y minimum y world position
	 */
	public void setWorldMinY(float y) {
		mWorldMin.y = y;
	}

	/**
	 * Set maximum camera/world position. Clamps zoom to this value
	 * @param x maximum x world position
	 * @param y maximum y world position
	 */
	public void setWorldMax(float x, float y) {
		mWorldMax.set(x, y);
	}

	/**
	 * Sets maximum x camera/world position. Clamps zoom to this value
	 * @param x maximum x world position
	 */
	public void setWorldMaxX(float x) {
		mWorldMax.x = x;
	}

	/**
	 * Sets maximum y camera/world position. Clamps zoom to this value
	 * @param y maximum y world position
	 */
	public void setWorldMaxY(float y) {
		mWorldMax.y = y;
	}

	/** Minimum world coordinates to show */
	private Vector2 mWorldMin = new Vector2(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);
	/** Maximum world coordinates to show */
	private Vector2 mWorldMax = new Vector2(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
	/** Created a scroll command */
	private boolean mCreatedScrollCommand = true;
	/** Origin of camera scroll */
	private Vector2 mScrollCameraOrigin = new Vector2();
	/** Logic for scrolling */
	private Scroller mScroller = new Scroller(50, 2000, 10, 200, ScrollAxis.ALL);
}

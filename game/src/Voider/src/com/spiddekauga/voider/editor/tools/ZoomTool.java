package com.spiddekauga.voider.editor.tools;

import com.badlogic.gdx.math.Vector2;
import com.spiddekauga.utils.KeyHelper;
import com.spiddekauga.utils.Screens;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.editor.IResourceChangeEditor;
import com.spiddekauga.voider.editor.commands.CCameraZoom;
import com.spiddekauga.voider.resources.IResource;
import com.spiddekauga.voider.utils.Pools;

/**
 * Tool for zooming in/out in the level editor
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class ZoomTool extends TouchTool {
	/**
	 * @param editor
	 * @param zoomMin minimum zoom
	 * @param zoomMax maximum zoom
	 */
	public ZoomTool(IResourceChangeEditor editor, float zoomMin, float zoomMax) {
		super(editor, null);

		// Add all resources so that they don't get deselected
		// This tool hijacks all event messages anyway
		mSelectableResourceTypes.add(IResource.class);

		mZoomMin = zoomMin;
		mZoomMax = zoomMax;
	}

	@Override
	protected boolean down(int button) {
		if (isActive()) {
			if (mZoomInOnClick) {
				zoom(1);
			} else {
				zoom(-1);
			}

			return true;
		}
		return false;
	}

	@Override
	protected boolean dragged() {
		// Does nothing
		return false;
	}

	@Override
	protected boolean up(int button) {
		// Does nothing
		return false;
	}

	@Override
	public boolean scroll(int amount) {
		if (KeyHelper.isCtrlPressed() || isActive()) {
			zoom(-amount);
		}

		return false;
	}

	/**
	 * @return get current zoom
	 */
	public float getZoomAmount() {
		return mZoom;
	}

	/**
	 * Set zoom click state
	 * @param zoomIn true if we want to zoom in on clicks, false if zoom out
	 */
	public void setZoomStateOnClick(boolean zoomIn) {
		mZoomInOnClick = zoomIn;
	}

	/**
	 * Reset the zoom
	 */
	public void resetZoom() {
		Vector2 zoomPos = calculateZoomPos(1, mZoom);
		mZoom = 1;
		mInvoker.execute(new CCameraZoom(mCamera, mZoom, zoomPos));
		Pools.vector2.free(zoomPos);
	}

	/**
	 * Zoom in or out
	 * @param amount
	 */
	private void zoom(int amount) {
		float oldZoom = mZoom;

		// Zoom in
		if (amount < 0) {
			mZoom *= Math.pow(mZoomInAmount, -amount);
		}
		// Zoom out
		else {
			mZoom *= Math.pow(mZoomOutAmount, amount);
		}

		// Test limits
		if (mZoom < mZoomMin) {
			mZoom = mZoomMin;
		} else if (mZoom > mZoomMax) {
			mZoom = mZoomMax;
		}

		// Actually zoom
		if (mZoom != oldZoom) {
			// Make cursor stay at same position when zooming in
			Vector2 cameraPos = calculateZoomPos(mZoom, oldZoom);

			// Call zoom command
			mInvoker.execute(new CCameraZoom(mCamera, mZoom, cameraPos));

			Pools.vector2.free(cameraPos);
		}
	}

	/**
	 * Calculates the zoom position so we stay inside the 'view' bounds and so the point
	 * stays at the same position while zooming in
	 * @param newZoom new zoom amount
	 * @param oldZoom old zoom amount
	 * @return new camera position so we don't go out of bounds and
	 */
	private Vector2 calculateZoomPos(float newZoom, float oldZoom) {
		Vector2 cameraPos = calculateCameraPos(newZoom, oldZoom);
		Screens.clampCamera(mCamera, mWorldMin, mWorldMax, cameraPos, mZoom);

		return cameraPos;
	}

	/**
	 * Calculate the camera position
	 * @param newZoom new zoom amount
	 * @param oldZoom old zoom amount
	 * @return camera position with correct pointer position
	 */
	private Vector2 calculateCameraPos(float newZoom, float oldZoom) {
		Vector2 pointerDiff = Pools.vector2.obtain();
		pointerDiff.set(mTouchCurrent);
		pointerDiff.sub(mCamera.position.x, mCamera.position.y);

		Vector2 pointerRatio = Pools.vector2.obtain();

		float oldWidth = mCamera.viewportWidth * oldZoom;
		float oldHeight = mCamera.viewportHeight * oldZoom;
		float newWidth = mCamera.viewportWidth * mZoom;
		float newHeight = mCamera.viewportHeight * mZoom;

		pointerRatio.set(pointerDiff.x / oldWidth, pointerDiff.y / oldHeight);

		Vector2 cameraPos = Pools.vector2.obtain();
		cameraPos.set(mTouchCurrent);
		cameraPos.sub(pointerRatio.x * newWidth, pointerRatio.y * newHeight);

		Pools.vector2.freeAll(pointerDiff, pointerRatio);

		return cameraPos;
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

	/** True if will zoom in on click */
	private boolean mZoomInOnClick = true;
	/** Current zoom value */
	private float mZoom = 1;
	/** Min zoom value */
	private float mZoomMin;
	/** Max zoom value */
	private float mZoomMax;
	/** Minimum world coordinates to show */
	private Vector2 mWorldMin = new Vector2(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);
	/** Maximum world coordinates to show */
	private Vector2 mWorldMax = new Vector2(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
	/** Zoom in amount on click/scroll */
	private static float mZoomInAmount = Config.Editor.ZOOM_AMOUNT;
	/** Zoom out amount on click/scroll */
	private static float mZoomOutAmount = 1 / mZoomInAmount;
}

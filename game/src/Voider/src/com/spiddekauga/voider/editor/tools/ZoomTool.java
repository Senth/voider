package com.spiddekauga.voider.editor.tools;

import com.badlogic.gdx.math.Vector2;
import com.spiddekauga.utils.KeyHelper;
import com.spiddekauga.utils.commands.Command;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.editor.IResourceChangeEditor;
import com.spiddekauga.voider.editor.commands.CCameraZoom;
import com.spiddekauga.voider.resources.IResource;
import com.spiddekauga.voider.utils.Pools;

/**
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
				zoom(mZoomAmount);
			} else {
				zoom(-mZoomAmount);
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
			float zoomAmount = amount * mZoomAmount;
			zoom(-zoomAmount);
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
		mZoom = 1;
		Command command = new CCameraZoom(mCamera, mZoom);
		command.addObserver(mEditor);
		mInvoker.execute(command);
	}

	/**
	 * Zoom in or out
	 * @param amount
	 */
	private void zoom(float amount) {
		float oldZoom = mZoom;

		mZoom += amount;

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
			Command command = new CCameraZoom(mCamera, mZoom, cameraPos);
			command.addObserver(mEditor);
			mInvoker.execute(command);

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
		clampCameraPos(cameraPos);

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
	 * Clamp the camera position to min/max world coordinates
	 * @param cameraPos the new camera position to clamp
	 */
	private void clampCameraPos(Vector2 cameraPos) {
		float widthHalf = mCamera.viewportWidth * mZoom * 0.5f;
		float heightHalf = mCamera.viewportHeight * mZoom * 0.5f;

		Vector2 cameraMin = Pools.vector2.obtain();
		cameraMin.set(cameraPos).sub(widthHalf, heightHalf);
		Vector2 cameraMax = Pools.vector2.obtain();
		cameraMax.set(cameraPos).add(widthHalf, heightHalf);


		// Clamp X
		// Both are out of bounds -> Center
		if (cameraMin.x < mWorldMin.x && cameraMax.x > mWorldMax.x) {
			cameraPos.x = (mWorldMin.x + mWorldMax.x) / 2;
		}
		// Left out of bounds
		else if (cameraMin.x < mWorldMin.x) {
			cameraPos.x = mWorldMin.x + widthHalf;
		}
		// Right out of bounds
		else if (cameraMax.x > mWorldMax.x) {
			cameraPos.x = mWorldMax.x - widthHalf;
		}

		// Clamp Y
		// Both are out of bound -> Center
		if (cameraMin.y < mWorldMin.y && cameraMax.y > mWorldMax.y) {
			cameraPos.y = (mWorldMin.y + mWorldMax.y) / 2;
		}
		// Top out of bounds
		else if (cameraMin.y < mWorldMin.y) {
			cameraPos.y = mWorldMin.y + heightHalf;
		}
		// Bottom out of bounds
		else if (cameraMax.y > mWorldMax.y) {
			cameraPos.y = mWorldMax.y - heightHalf;
		}
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
	/** Zoom amount on click/scroll */
	private static float mZoomAmount = Config.Editor.ZOOM_AMOUNT;
}

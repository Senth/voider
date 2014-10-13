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

		mMinZoom = zoomMin;
		mMaxZoom = zoomMax;
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
		if (mZoom < mMinZoom) {
			mZoom = mMinZoom;
		} else if (mZoom > mMaxZoom) {
			mZoom = mMaxZoom;
		}

		// Actually zoom
		if (mZoom != oldZoom) {
			// Make cursor stay at same position when zooming in
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

			// Call zoom command
			Command command = new CCameraZoom(mCamera, mZoom, cameraPos);
			command.addObserver(mEditor);
			mInvoker.execute(command);

			Pools.vector2.freeAll(cameraPos, pointerRatio, pointerDiff);
		}
	}

	/** True if will zoom in on click */
	private boolean mZoomInOnClick = true;
	/** Current zoom value */
	private float mZoom = 1;
	/** Min zoom value */
	private float mMinZoom;
	/** Max zoom value */
	private float mMaxZoom;
	/** Zoom amount on click/scroll */
	private static float mZoomAmount = Config.Editor.ZOOM_AMOUNT;
}

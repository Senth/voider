package com.spiddekauga.voider.editor.tools;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.spiddekauga.utils.KeyHelper;
import com.spiddekauga.utils.commands.Invoker;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.editor.commands.CCameraZoom;
import com.spiddekauga.voider.resources.IResource;

/**
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class ZoomTool extends TouchTool {
	/**
	 * @param camera used for determining where in the world the pointer is
	 * @param world used for picking
	 * @param invoker used for undo/redo of some commands
	 * @param zoomMin minimum zoom
	 * @param zoomMax maximum zoom
	 */
	public ZoomTool(Camera camera, World world, Invoker invoker, float zoomMin, float zoomMax) {
		super(camera, world, invoker, null, null);

		// Add all resources so that they don't get deselected
		// This tool hijacks all event messages anyway
		mSelectableResourceTypes.add(IResource.class);

		mMinZoom = zoomMin;
		mMaxZoom = zoomMax;
		mViewportInitial.x = camera.viewportHeight;
		mViewportInitial.y = camera.viewportWidth;
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
			zoom(zoomAmount);
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
			// TODO
			// Get cursor world position

			// Make cursor position the center

			// Clamp so we don't go out of bounds

			// Call zoom command
			mInvoker.execute(new CCameraZoom(mCamera, mViewportInitial, mZoom));
		}
	}

	/** Original viewport */
	private Vector2 mViewportInitial = new Vector2();
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

package com.spiddekauga.voider.editor.tools;

import com.badlogic.gdx.math.Vector2;
import com.spiddekauga.utils.KeyHelper;
import com.spiddekauga.utils.Screens;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.editor.IResourceChangeEditor;
import com.spiddekauga.voider.editor.commands.CCameraZoom;
import com.spiddekauga.voider.resources.IResource;

/**
 * Tool for zooming in/out in the level editor
 */
public class ZoomTool extends TouchTool {
/** Zoom in amount on click/scroll */
private static float mZoomInAmount = Config.Editor.ZOOM_AMOUNT;
/** Zoom out amount on click/scroll */
private static float mZoomOutAmount = 1 / mZoomInAmount;
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

	setCanBeActive(false);
}

@Override
protected boolean down(int button) {
	// Does nothing
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
	}
}

/**
 * Calculates the zoom position so we stay inside the 'view' bounds and so the point stays at the
 * same position while zooming in
 * @param newZoom new zoom amount
 * @param oldZoom old zoom amount
 * @return new camera position so we don't go out of bounds and
 */
private Vector2 calculateZoomPos(float newZoom, float oldZoom) {
	Vector2 cameraPos = calculateCameraPos(newZoom, oldZoom);
	Screens.clampCamera(mCamera, mWorldMin, mWorldMax, cameraPos, newZoom);

	return cameraPos;
}

/**
 * Calculate the camera position
 * @param newZoom new zoom amount
 * @param oldZoom old zoom amount
 * @return camera position with correct pointer position
 */
private Vector2 calculateCameraPos(float newZoom, float oldZoom) {
	Vector2 pointerDiff = new Vector2(mTouchCurrent);
	pointerDiff.sub(mCamera.position.x, mCamera.position.y);

	float oldWidth = mCamera.viewportWidth * oldZoom;
	float oldHeight = mCamera.viewportHeight * oldZoom;
	float newWidth = mCamera.viewportWidth * mZoom;
	float newHeight = mCamera.viewportHeight * mZoom;

	Vector2 pointerRatio = new Vector2();
	pointerRatio.set(pointerDiff.x / oldWidth, pointerDiff.y / oldHeight);

	Vector2 cameraPos = new Vector2(mTouchCurrent);
	cameraPos.sub(pointerRatio.x * newWidth, pointerRatio.y * newHeight);

	return cameraPos;
}

/**
 * @return get current zoom
 */
public float getZoomAmount() {
	return mZoom;
}

/**
 * Reset the zoom
 */
public void resetZoom() {
	mTouchCurrent.set(mCamera.position.x, mCamera.position.y);
	Vector2 zoomPos = calculateZoomPos(1, mZoom);
	mZoom = 1;
	mInvoker.execute(new CCameraZoom(mCamera, mZoom, zoomPos));
}

/**
 * Zoom in
 */
public void zoomIn() {
	zoom(-1);
}

/**
 * Zoom out
 */
public void zoomOut() {
	zoom(1);
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
}

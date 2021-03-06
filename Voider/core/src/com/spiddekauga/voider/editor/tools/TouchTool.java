package com.spiddekauga.voider.editor.tools;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.spiddekauga.utils.commands.Invoker;
import com.spiddekauga.utils.scene.ui.NotificationShower;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.Config.Editor;
import com.spiddekauga.voider.editor.IResourceChangeEditor;
import com.spiddekauga.voider.resources.IResource;
import com.spiddekauga.utils.scene.ui.Scene;
import com.spiddekauga.utils.scene.ui.SceneSwitcher;

import java.util.ArrayList;

/**
 * Abstract tool for handling touch events
 */
public abstract class TouchTool extends InputAdapter {
/** Send notifications */
protected NotificationShower mNotification = NotificationShower.getInstance();
/** If the player double clicked */
protected boolean mDoubleClick = false;
/** Last time the player clicked */
protected float mClickTimeLast = 0;
/** Current screen coordinates */
protected Vector2 mScreenCurrent = new Vector2();
/** Current position of the touch, in world coordinates */
protected Vector2 mTouchCurrent = new Vector2();
/** Original position of the touch, in world coordinates */
protected Vector2 mTouchOrigin = new Vector2();
/** World used for picking */
protected World mWorld;
/** Camera of the tool, used to get world coordinates of the click */
protected OrthographicCamera mCamera;
/** Invoker */
protected Invoker mInvoker;
/** Current selection */
protected ISelection mSelection = null;
/** Editor */
protected IResourceChangeEditor mEditor = null;
/** Selectable resource types */
protected ArrayList<Class<? extends IResource>> mSelectableResourceTypes = new ArrayList<Class<? extends IResource>>();
/** True if this tool is allowed to be active */
private boolean mCanBeActive = true;
/** If this tool is currently active */
private boolean mActive = false;
/** If the tool is currently drawing */
private boolean mDrawing = false;
/** Temporary callback variable, used for testing points */
private QueryCallback mTempPointCallback = null;
/** Callback for testing points */
private QueryCallback mCallbackPoint = new QueryCallback() {
	@Override
	public boolean reportFixture(Fixture fixture) {
		if (fixture.testPoint(mTouchCurrent)) {
			return mTempPointCallback.reportFixture(fixture);
		}
		return true;
	}
};

/**
 * Constructs a touch tool with a camera
 * @param editor the editor used, can be null
 * @param selection current selected resources, can be null
 */
public TouchTool(IResourceChangeEditor editor, ISelection selection) {
	mEditor = editor;
	mCamera = editor.getCamera();
	mWorld = editor.getWorld();
	mInvoker = editor.getInvoker();
	mSelection = selection;
}

@Override
public final boolean touchDown(int x, int y, int pointer, int button) {

	// Only do something for the first pointer
	if (pointer == 0) {
		mScreenCurrent.set(x, y);

		if (mClickTimeLast + Config.Input.DOUBLE_CLICK_TIME > SceneSwitcher.getGameTime().getTotalTimeElapsed()) {
			mClickTimeLast = 0;
			mDoubleClick = true;
		} else {
			mDoubleClick = false;
			mClickTimeLast = SceneSwitcher.getGameTime().getTotalTimeElapsed();
		}

		Scene.screenToWorldCoord(mCamera, x, y, mTouchOrigin, true);
		mTouchCurrent.set(mTouchOrigin);

		return down(button);
	}


	return false;
}

@Override
public final boolean touchUp(int x, int y, int pointer, int button) {
	if (pointer == 0) {
		mScreenCurrent.set(x, y);
		Scene.screenToWorldCoord(mCamera, x, y, mTouchCurrent, true);

		return up(button);
	}

	return false;
}

@Override
public final boolean touchDragged(int x, int y, int pointer) {
	if (pointer == 0) {
		mScreenCurrent.set(x, y);
		Scene.screenToWorldCoord(mCamera, x, y, mTouchCurrent, true);

		return dragged();
	}


	return false;
}

@Override
public final boolean scrolled(int amount) {
	mScreenCurrent.set(Gdx.input.getX(), Gdx.input.getY());
	Scene.screenToWorldCoord(mCamera, mScreenCurrent, mTouchCurrent, true);

	return scroll(amount);
}

/**
 * Called on scroll event
 * @param amount how much was scrolled
 * @return false if event should continue to be handled downstream
 */
protected boolean scroll(int amount) {
	// Does nothing
	return false;
}

/**
 * Called on touchDragged event
 * @return false if event should continue to be handled downstream
 */
protected abstract boolean dragged();

/**
 * Called on touchUp event
 * @param button the button that was released
 * @return false if event should continue to be handled downstream
 */
protected abstract boolean up(int button);

/**
 * Called on touchDown event
 * @param button the button that was pressed
 * @return false if event should continue to be handled downstream
 */
protected abstract boolean down(int button);

/**
 * Activates the tool. This method does nothing by itself except when overridden. Usually this will
 * make selected actors add extra corners and/or draw them differently
 */
public void activate() {
	if (canBeActive()) {
		mActive = true;
	} else {
		throw new IllegalStateException(getClass().getSimpleName() + " isn't allowed to be activated");
	}
}

/**
 * @return true if this tool is allowed to be active
 */
public boolean canBeActive() {
	return mCanBeActive;
}

/**
 * Deactivates the tool. This method does nothing by itself except when overridden. Usually this
 * will make selected actors to be drawn regularly again
 */
public void deactivate() {
	if (canBeActive()) {
		mActive = false;
	} else {
		throw new IllegalStateException(getClass().getSimpleName() + " isn't allowed to be deactivated");
	}
}

/**
 * @return true if the tool is currently active
 */
public boolean isActive() {
	return mActive;
}

/**
 * Clears the current tool to the initial state
 */
public void clear() {
	// Does nothing
}

/**
 * @return true if the tool is currently drawing something
 */
public boolean isDrawing() {
	return mDrawing;
}

/**
 * Sets the tool as drawing
 * @param drawing true if the tool is drawing
 */
protected void setDrawing(boolean drawing) {
	mDrawing = drawing;
}

/**
 * Tests to pick a body from the current touch. The hit body is set to mHitBody. Uses the default
 * pick size
 * @param callback method to use for getting picks
 */
protected void testPickAabb(QueryCallback callback) {
	testPickAabb(callback, Editor.PICK_SIZE_DEFAULT);
}

/**
 * Tests to pick a body from the current touch. The hit body will be set to mHitBody
 * @param callback method to use for getting picks
 * @param halfSize how far the touch shall test
 */
protected void testPickAabb(QueryCallback callback, float halfSize) {
	mWorld.QueryAABB(callback, mTouchCurrent.x - halfSize, mTouchCurrent.y - halfSize, mTouchCurrent.x + halfSize, mTouchCurrent.y + halfSize);
}

/**
 * Tests to pick bodies within the specified AABB box
 * @param callback method to use for getting picks
 * @param startPosition the start (AA) of the box
 * @param endPosition the end (BB) of the box
 */
protected void testPickAabb(QueryCallback callback, Vector2 startPosition, Vector2 endPosition) {
	float lowX, lowY, highX, highY;

	if (startPosition.x < endPosition.x) {
		lowX = startPosition.x;
		highX = endPosition.x;
	} else {
		lowX = endPosition.x;
		highX = startPosition.x;
	}

	if (startPosition.y < endPosition.y) {
		lowY = startPosition.y;
		highY = endPosition.y;
	} else {
		lowY = endPosition.y;
		highY = startPosition.y;
	}

	mWorld.QueryAABB(callback, lowX, lowY, highX, highY);
}

/**
 * Tests to pick a body from the current touch point. The hit body will be set to mHitBody
 * @param callback callback method used when picking
 */
protected void testPickPoint(QueryCallback callback) {
	mTempPointCallback = callback;
	mWorld.QueryAABB(mCallbackPoint, mTouchCurrent.x, mTouchCurrent.y, mTouchCurrent.x, mTouchCurrent.y);
	mTempPointCallback = null;
}

/**
 * What type of resources the selection tool is allowed to select when this tool is active
 * @return array of resources the selection tool is allowed to select
 */
public final ArrayList<Class<? extends IResource>> getSelectableResourceTypes() {
	return mSelectableResourceTypes;
}

/**
 * Override this method if you want the selection tool to only select resources of the currently
 * selected resource
 * @return true if the selection tool is allowed to change between resources types independent of
 * the current selection.
 */
public boolean isSelectionToolAllowedToChangeResourceType() {
	return true;
}

/**
 * Set this to false to make the tool unable to activate (it throws an exception if it's activated)
 * @param canBeActive
 */
protected void setCanBeActive(boolean canBeActive) {
	mCanBeActive = canBeActive;
}
}

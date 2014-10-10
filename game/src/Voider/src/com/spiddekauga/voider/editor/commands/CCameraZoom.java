package com.spiddekauga.voider.editor.commands;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.spiddekauga.utils.commands.Command;
import com.spiddekauga.utils.commands.ICommandCombinable;
import com.spiddekauga.voider.utils.Pools;

/**
 * Zooms and optionally sets a new position of the camera
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class CCameraZoom extends Command implements ICommandCombinable {
	/**
	 * Creates a zoom command. This will also move the camera to a new position
	 * @param camera the camera to zoom
	 * @param viewportOriginal the original viewport
	 * @param zoomAmount how much we want the zoom to be
	 * @param pos new position of the camera
	 */
	public CCameraZoom(Camera camera, Vector2 viewportOriginal, float zoomAmount, Vector2 pos) {
		mCamera = camera;
		mPosOld.set(camera.position.x, camera.position.y);
		mPosNew.set(pos);

		// Calculate new viewport
		mViewportNew.set(viewportOriginal);
		mViewportNew.x *= zoomAmount;
		mViewportOld.y *= zoomAmount;
	}

	/**
	 * Creates a zoom command. Keeps same position
	 * @param camera the camera to zoom
	 * @param viewportOriginal the original viewport
	 * @param zoomAmount how much we want the zoom to be
	 */
	public CCameraZoom(Camera camera, Vector2 viewportOriginal, float zoomAmount) {
		this(camera, viewportOriginal, zoomAmount, new Vector2(camera.position.x, camera.position.y));
	}

	@Override
	public boolean combine(ICommandCombinable otherCommand) {
		if (otherCommand instanceof CCameraZoom) {
			CCameraZoom command = (CCameraZoom) otherCommand;

			if (command.mCamera == mCamera) {
				boolean executeSuccess = command.execute();

				if (executeSuccess) {
					mViewportNew.set(mViewportNew);
					mPosNew.set(command.mPosNew);
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean execute() {
		mCamera.position.x = mPosNew.x;
		mCamera.position.y = mPosNew.y;
		mCamera.viewportWidth = mViewportNew.x;
		mCamera.viewportHeight = mViewportNew.y;
		mCamera.update();

		return true;
	}

	@Override
	public boolean undo() {
		mCamera.position.x = mPosOld.x;
		mCamera.position.y = mPosOld.y;
		mCamera.viewportWidth = mViewportOld.x;
		mCamera.viewportHeight = mViewportOld.y;
		mCamera.update();

		return true;
	}

	@Override
	public void dispose() {
		Pools.vector2.freeAll(mPosNew, mPosOld, mViewportOld, mViewportNew);
	}

	private Camera mCamera;
	private Vector2 mPosOld = Pools.vector2.obtain();
	private Vector2 mPosNew = Pools.vector2.obtain();
	private Vector2 mViewportOld = Pools.vector2.obtain();
	private Vector2 mViewportNew = Pools.vector2.obtain();
}

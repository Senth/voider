package com.spiddekauga.voider.editor.commands;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.spiddekauga.utils.commands.Command;
import com.spiddekauga.utils.commands.ICommandCombinable;
import com.spiddekauga.voider.utils.GameEvent;
import com.spiddekauga.voider.utils.Pools;

/**
 * Zooms and optionally sets a new position of the camera
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class CCameraZoom extends Command implements ICommandCombinable {
	/**
	 * Creates a zoom command. This will also move the camera to a new position
	 * @param camera the camera to zoom
	 * @param zoomAmount how much we want the zoom to be
	 * @param pos new position of the camera
	 */
	public CCameraZoom(OrthographicCamera camera, float zoomAmount, Vector2 pos) {
		mCamera = camera;
		mPosOld.set(camera.position.x, camera.position.y);
		mPosNew.set(pos);
		mZoomOld = camera.zoom;
		mZoomNew = zoomAmount;
	}

	/**
	 * Creates a zoom command. Keeps same position
	 * @param camera the camera to zoom
	 * @param zoomAmount how much we want the zoom to be
	 */
	public CCameraZoom(OrthographicCamera camera, float zoomAmount) {
		this(camera, zoomAmount, new Vector2(camera.position.x, camera.position.y));
	}

	@Override
	public boolean combine(ICommandCombinable otherCommand) {
		if (otherCommand instanceof CCameraZoom) {
			CCameraZoom command = (CCameraZoom) otherCommand;

			if (command.mCamera == mCamera) {
				boolean executeSuccess = command.execute();

				if (executeSuccess) {
					mZoomNew = command.mZoomNew;
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
		mCamera.zoom = mZoomNew;
		mCamera.update();
		notifyObservers(new GameEvent(GameEvent.Events.CAMERA_ZOOM_CHANGE));
		return true;
	}

	@Override
	public boolean undo() {
		mCamera.position.x = mPosOld.x;
		mCamera.position.y = mPosOld.y;
		mCamera.zoom = mZoomOld;
		mCamera.update();
		notifyObservers(new GameEvent(GameEvent.Events.CAMERA_ZOOM_CHANGE));
		return true;
	}

	@Override
	public void dispose() {
		Pools.vector2.freeAll(mPosNew, mPosOld);
		deleteObservers();
	}

	private OrthographicCamera mCamera;
	private float mZoomOld;
	private float mZoomNew;
	private Vector2 mPosOld = Pools.vector2.obtain();
	private Vector2 mPosNew = Pools.vector2.obtain();
}

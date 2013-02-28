package com.spiddekauga.voider.editor.commands;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.spiddekauga.utils.Command;
import com.spiddekauga.utils.ICommandCombinable;
import com.spiddekauga.voider.utils.Vector2Pool;

/**
 * Command for moving a camera. This can undo and redo a scroll.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class CCameraMove extends Command implements ICommandCombinable {
	/**
	 * Creates a move camera command. This will move the camera to the new position
	 * @param camera the camera to move
	 * @param newPos the new position of the camera
	 * @param oldPos old/previous position of the camera
	 */
	public CCameraMove(Camera camera, Vector2 newPos, Vector2 oldPos) {
		mNewPos.set(newPos);
		mOldPos.set(oldPos);
		mCamera = camera;
	}

	@Override
	public boolean combine(ICommandCombinable otherCommand) {
		if (otherCommand instanceof CCameraMove) {
			if (((CCameraMove) otherCommand).mCamera == mCamera) {
				boolean executeSuccess = ((CCameraMove) otherCommand).execute();

				if (executeSuccess) {
					mNewPos.set(((CCameraMove) otherCommand).mNewPos);
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public boolean execute() {
		mCamera.position.x = mNewPos.x;
		mCamera.position.y = mNewPos.y;
		mCamera.update();
		return true;
	}

	@Override
	public boolean undo() {
		mCamera.position.x = mOldPos.x;
		mCamera.position.y = mOldPos.y;
		mCamera.update();
		return true;
	}

	@Override
	public void dispose() {
		Vector2Pool.free(mNewPos);
		Vector2Pool.free(mOldPos);
	}

	/** The camera to move */
	private Camera mCamera;
	/** New position of the camera (execute()) */
	private Vector2 mNewPos = Vector2Pool.obtain();
	/** Old position of the camera (undo()) */
	private Vector2 mOldPos = Vector2Pool.obtain();
}

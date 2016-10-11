package com.spiddekauga.voider.editor.commands;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.spiddekauga.utils.commands.Command;
import com.spiddekauga.utils.commands.ICommandCombinable;
import com.spiddekauga.voider.utils.event.EventDispatcher;
import com.spiddekauga.voider.utils.event.EventTypes;
import com.spiddekauga.voider.utils.event.GameEvent;

/**
 * Command for moving a camera. This can undo and redo a scroll.
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class CCameraMove extends Command implements ICommandCombinable {
	/**
	 * Creates a move camera command. This will move the camera to the new position
	 * @param camera the camera to move
	 * @param newPos the new position of the camera
	 * @param oldPos old/previous position of the camera
	 */
	public CCameraMove(OrthographicCamera camera, Vector2 newPos, Vector2 oldPos) {
		mNewPos.set(newPos);
		mCamera = camera;
		mOldPos.set(oldPos);
	}

	@Override
	public boolean combine(ICommandCombinable otherCommand) {
		if (otherCommand instanceof CCameraMove) {
			CCameraMove cameraMove = (CCameraMove) otherCommand;
			if (cameraMove.mCamera == mCamera) {
				// Are they close enough to each other?
				Vector2 diff = new Vector2(mNewPos).sub(cameraMove.mNewPos);
				if (diff.len2() <= COMBINE_DIST_SQ * mCamera.zoom) {
					boolean executeSuccess = cameraMove.execute();
					if (executeSuccess) {
						mNewPos.set(cameraMove.mNewPos);
						return true;
					}
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
		sendEvent();
		return true;
	}

	@Override
	public boolean undo() {
		mCamera.position.x = mOldPos.x;
		mCamera.position.y = mOldPos.y;
		mCamera.update();
		sendEvent();
		return true;
	}

	/**
	 * Send event
	 */
	private void sendEvent() {
		EventDispatcher.getInstance().fire(new GameEvent(EventTypes.CAMERA_MOVED));
	}

	/** The camera to move */
	private OrthographicCamera mCamera;
	/** New position of the camera (execute()) */
	private Vector2 mNewPos = new Vector2();
	/** Old position of the camera (undo()) */
	private Vector2 mOldPos = new Vector2();
	/** How close the camera positions should be to be be combined */
	private static final float COMBINE_DIST = 6;
	private static final float COMBINE_DIST_SQ = COMBINE_DIST * COMBINE_DIST;
}

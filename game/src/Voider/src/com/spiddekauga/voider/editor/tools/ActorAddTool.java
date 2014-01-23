package com.spiddekauga.voider.editor.tools;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.spiddekauga.utils.Invoker;
import com.spiddekauga.voider.editor.IResourceChangeEditor;
import com.spiddekauga.voider.editor.commands.CResourceMove;
import com.spiddekauga.voider.game.actors.Actor;
import com.spiddekauga.voider.utils.Pools;

/**
 * Tool for adding an actor
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class ActorAddTool extends ActorTool {
	/**
	 * @param camera used for picking point on screen to world
	 * @param world used for converting screen coordinates to world coordinates
	 * @param invoker used for undo/redo
	 * @param selection all selected resources
	 * @param editor editor this tool is bound to
	 * @param actorType the type of actor this tool uses
	 */
	public ActorAddTool(Camera camera, World world, Invoker invoker, ISelection selection, IResourceChangeEditor editor, Class<? extends Actor> actorType) {
		super(camera, world, invoker, selection, editor, actorType);
	}

	@Override
	protected boolean down() {
		testPickPoint(mCallback);

		// Hit an actor, move it
		if (mMovingActor != null) {
			mDragOrigin.set(mMovingActor.getPosition());
			return true;
		}
		// Create a new actor here (if we have selected a definition)
		else if (mActorDef != null) {
			mMovingActor = createNewSelectedActor();
			mDragOrigin.set(mTouchOrigin);
			mCreatedThisEvent = true;
			return true;
		}

		if (mMovingActor != null) {
			mMovingActor.setIsBeingMoved(true);
		}

		return false;
	}

	@Override
	protected boolean dragged() {
		if (mMovingActor != null) {
			Vector2 newPosition = getNewPosition();
			mMovingActor.setPosition(newPosition);
			Pools.vector2.free(newPosition);
		}
		return false;
	}

	@Override
	protected boolean up() {
		if (mMovingActor != null) {
			Vector2 newPosition = getNewPosition();
			// Just set the new position
			if (mCreatedThisEvent) {
				mMovingActor.setPosition(newPosition);
			}
			// If not new actor, reset to old position and move using command
			else {
				mMovingActor.setPosition(mDragOrigin);
				mInvoker.execute(new CResourceMove(mMovingActor, newPosition, mEditor));
			}

			mMovingActor.setIsBeingMoved(false);

			mMovingActor = null;
			mCreatedThisEvent = false;
		}
		return false;
	}

	/**
	 * @return new position of the actor
	 */
	protected Vector2 getNewPosition() {
		Vector2 newPosition = Pools.vector2.obtain();
		newPosition.set(mTouchCurrent).sub(mTouchOrigin);
		newPosition.add(mDragOrigin);
		return newPosition;
	}

	/** Test if hit an actor of this type */
	private QueryCallback mCallback = new QueryCallback() {
		@Override
		public boolean reportFixture(Fixture fixture) {
			Object userData = fixture.getBody().getUserData();
			if (userData != null && userData.getClass() == mActorType) {
				mMovingActor = (Actor) userData;
			}
			return false;
		}
	};

	/** If the actor was created this down */
	protected boolean mCreatedThisEvent = false;
	/** Moving actor */
	protected Actor mMovingActor = null;
	/** Original position of the actor */
	protected Vector2 mDragOrigin = new Vector2();
}

package com.spiddekauga.voider.scene;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Pools;
import com.spiddekauga.utils.Invoker;
import com.spiddekauga.voider.editor.IActorChangeEditor;
import com.spiddekauga.voider.editor.commands.CActorAdd;
import com.spiddekauga.voider.editor.commands.CActorMove;
import com.spiddekauga.voider.editor.commands.CActorRemove;
import com.spiddekauga.voider.game.actors.Actor;

/**
 * Tool for adding and removing actors
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class AddActorTool extends ActorTool {
	/**
	 * Creates an add actor tool for the specified type of actor.
	 * You still need to set an actor definition via {@link #setNewActorDef(com.spiddekauga.voider.game.actors.ActorDef)}
	 * to add actors.
	 * @param camera used for determining where the pointer is in the world
	 * @param world used for picking
	 * @param actorType the actor type to add/move/remove
	 * @param invoker used for undo/redo the actions by this tool
	 * @param editor will be called when actors are added/removed.
	 */
	public AddActorTool(Camera camera, World world, Class<?> actorType, Invoker invoker, IActorChangeEditor editor) {
		super(camera, world, actorType);
		mInvoker = invoker;
		mEditor = editor;
	}

	/**
	 * Sets the current state of the tool
	 * @param state new state
	 */
	public void setState(States state) {
		mState = state;
	}

	/**
	 * @return current state of the tool
	 */
	public States getState() {
		return mState;
	}

	/**
	 * All the states for the tool
	 */
	public enum States {
		/** Creates a new actor when pressed */
		ADD,
		/** Removes the actor that was hit */
		REMOVE,
		/** Moves the actor thata was hit */
		MOVE
	}

	@Override
	protected void down() {
		switch (mState) {
		case ADD:
			if (getNewActorDef() != null) {
				Actor actor = newActor();
				actor.setPosition(mTouchOrigin);
				mInvoker.execute(new CActorAdd(actor, mEditor));
				mMovingActor = actor;
				mActorOrigin.set(mTouchOrigin);
			}
			break;

		case REMOVE:
			testPick();

			if (mHitBody != null) {
				if (mHitBody.getUserData() instanceof Actor) {
					mInvoker.execute(new CActorRemove((Actor)mHitBody.getUserData(), mEditor));
				}
			}
			break;

		case MOVE:
			testPick();

			if (mHitBody != null) {
				if (mHitBody.getUserData() instanceof Actor) {
					mMovingActor = (Actor) mHitBody.getUserData();
					mActorOrigin.set(mMovingActor.getPosition());
				}
			}
			break;
		}
	}

	@Override
	protected void dragged() {
		switch (mState) {
		case ADD:
		case MOVE:
			if (mMovingActor != null) {
				Vector2 newPosition = getNewMovePosition();
				mMovingActor.setPosition(newPosition);
				Pools.free(newPosition);
			}
			break;

		case REMOVE:
			// Does nothing
			break;
		}
	}

	@Override
	protected void up() {
		boolean chained = false;
		switch (mState) {
		case ADD:
			chained = true;
		case MOVE:
			if (mMovingActor != null) {
				// Reset actor to old position first
				mMovingActor.setPosition(mActorOrigin);

				// Set new position through command
				Vector2 newPosition = getNewMovePosition();
				mInvoker.execute(new CActorMove(mMovingActor, newPosition, mEditor), chained);
				Pools.free(newPosition);
				mMovingActor = null;
			}
			break;

		case REMOVE:
			// Does nothing
			break;
		}
	}

	/**
	 * @return new position to move the actor to. Don't forget to free
	 * this position using Pools.free(newPos)
	 */
	private Vector2 getNewMovePosition() {
		// Get diff movement
		Vector2 newPosition = Pools.obtain(Vector2.class);
		newPosition.set(mTouchCurrent).sub(mTouchOrigin);

		// Add original position
		newPosition.add(mActorOrigin);

		return newPosition;
	}

	/** Invoker for undo/redo */
	private Invoker mInvoker;
	/** Editor to add/remove the actors from */
	private IActorChangeEditor mEditor;
	/** Current state of the tool */
	private States mState = States.ADD;
	/** The actor we're currently moving */
	private Actor mMovingActor = null;
	/** Original position of the actor */
	private Vector2 mActorOrigin = new Vector2();
}

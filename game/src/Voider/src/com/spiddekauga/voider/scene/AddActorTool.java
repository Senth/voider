package com.spiddekauga.voider.scene;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Pools;
import com.spiddekauga.utils.Invoker;
import com.spiddekauga.voider.editor.HitWrapper;
import com.spiddekauga.voider.editor.IActorChangeEditor;
import com.spiddekauga.voider.editor.commands.CActorAdd;
import com.spiddekauga.voider.editor.commands.CActorMove;
import com.spiddekauga.voider.editor.commands.CActorRemove;
import com.spiddekauga.voider.editor.commands.CActorSelect;
import com.spiddekauga.voider.game.actors.Actor;

/**
 * Tool for adding and removing actors
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class AddActorTool extends ActorTool implements ISelectTool {
	/**
	 * Creates an add actor tool for the specified type of actor.
	 * You still need to set an actor definition via {@link #setNewActorDef(com.spiddekauga.voider.game.actors.ActorDef)}
	 * to add actors.
	 * @param camera used for determining where the pointer is in the world
	 * @param world used for picking
	 * @param actorType the actor type to add/move/remove
	 * @param invoker used for undo/redo the actions by this tool
	 * @param addMoveSelects set to true if add and move states shall be able
	 * to select actors, otherwise only the select state will be able to select actors
	 * @param editor will be called when actors are added/removed.
	 */
	public AddActorTool(Camera camera, World world, Class<?> actorType, Invoker invoker, boolean addMoveSelects, IActorChangeEditor editor) {
		super(camera, world, actorType);
		mInvoker = invoker;
		mEditor = editor;
		mAddMoveSelects = addMoveSelects;
	}

	/**
	 * Sets the current state of the tool
	 * @param state new state
	 */
	public void setState(States state) {
		mState = state;

		// TODO deselect actor?
	}

	/**
	 * @return current state of the tool
	 */
	public States getState() {
		return mState;
	}

	@Override
	public void addListener(ISelectListener listener) {
		mSelectListeners.add(listener);
	}

	@Override
	public void addListeners(List<ISelectListener> listeners) {
		mSelectListeners.addAll(listeners);
	}

	@Override
	public void removeListener(ISelectListener listener) {
		mSelectListeners.remove(listener);
	}

	@Override
	public void removeListeners(List<ISelectListener> listeners) {
		mSelectListeners.removeAll(listeners);
	}

	@Override
	public void setSelectedActor(Actor selectedActor) {
		/** @todo set actor as deselected */

		for (ISelectListener selectListener : mSelectListeners) {
			selectListener.onActorSelect(mSelectedActor, selectedActor);
		}

		mSelectedActor = selectedActor;
		mSelectedSinceUp = true;

		/** @todo set actor as selected */
	}

	@Override
	public Actor getSelectedActor() {
		return mSelectedActor;
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
		MOVE,
		/** Selects an actor */
		SELECT
	}

	@Override
	protected void down() {
		switch (mState) {
		case ADD:
			boolean addActor = false;
			if (mAddMoveSelects) {
				testPick();
				selectActor(false);

				if (mSelectedActor != null) {
					mMovingActor = mSelectedActor;
					mActorOrigin.set(mMovingActor.getPosition());
				} else {
					addActor = true;
				}
			} else if (getNewActorDef() != null) {
				addActor = true;
			}

			if (addActor) {
				Actor actor = newActor();
				actor.setPosition(mTouchOrigin);
				mInvoker.execute(new CActorAdd(actor, mEditor), mSelectedSinceUp);
				mMovingActor = actor;
				mActorOrigin.set(mTouchOrigin);
				mInvoker.execute(new CActorSelect(actor, this), true);
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

			if (mAddMoveSelects) {
				selectActor(false);
			}

			if (mHitBody != null) {
				if (mHitBody.getUserData() instanceof Actor) {
					mMovingActor = (Actor) mHitBody.getUserData();
					mActorOrigin.set(mMovingActor.getPosition());
				}
			}
			break;

		case SELECT:
			testPick();
			selectActor(false);
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

		case SELECT:
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

				if (mSelectedSinceUp) {
					chained = true;
				}

				// Set new position through command
				Vector2 newPosition = getNewMovePosition();
				mInvoker.execute(new CActorMove(mMovingActor, newPosition, mEditor), chained);
				Pools.free(newPosition);
				mMovingActor = null;
			}
			break;

		case SELECT:
		case REMOVE:
			// Does nothing
			break;
		}

		mSelectedSinceUp = false;
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

	/**
	 * Selects the currently picked actor if one exist
	 * @param chained set to true if the command shall be chained
	 */
	private void selectActor(boolean chained) {
		Actor actorToSelect = null;
		if (mHitBody != null) {
			if (mHitBody.getUserData() instanceof Actor) {
				actorToSelect = (Actor) mHitBody.getUserData();
			}
			else if (mHitBody.getUserData() instanceof HitWrapper) {
				actorToSelect = ((HitWrapper)mHitBody.getUserData()).actor;
			}
		}

		if (actorToSelect != mSelectedActor) {
			mInvoker.execute(new CActorSelect(actorToSelect, this), chained);
		}
	}

	/** Invoker for undo/redo */
	protected Invoker mInvoker;
	/** Editor to add/remove the actors from */
	protected IActorChangeEditor mEditor;
	/** The actor we're currently moving */
	protected Actor mMovingActor = null;
	/** Original position of the actor */
	protected Vector2 mActorOrigin = new Vector2();
	/** If add and move can select actors */
	protected boolean mAddMoveSelects;
	/** If another actor has been selected since up was called. */
	protected boolean mSelectedSinceUp = false;
	/** Selected actor */
	protected Actor mSelectedActor = null;

	/** Current state of the tool */
	private States mState = States.ADD;
	/** Select listeners */
	private ArrayList<ISelectListener> mSelectListeners = new ArrayList<ISelectListener>();
}

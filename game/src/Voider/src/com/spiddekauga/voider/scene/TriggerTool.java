package com.spiddekauga.voider.scene;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.spiddekauga.utils.Invoker;
import com.spiddekauga.voider.Config.Editor;
import com.spiddekauga.voider.editor.LevelEditor;
import com.spiddekauga.voider.editor.commands.CResourceAdd;
import com.spiddekauga.voider.editor.commands.CResourceMove;
import com.spiddekauga.voider.editor.commands.CResourceRemove;
import com.spiddekauga.voider.editor.commands.CResourceSelect;
import com.spiddekauga.voider.game.actors.Actor;
import com.spiddekauga.voider.game.actors.EnemyActor;
import com.spiddekauga.voider.game.triggers.TActorActivated;
import com.spiddekauga.voider.game.triggers.TScreenAt;
import com.spiddekauga.voider.game.triggers.Trigger;
import com.spiddekauga.voider.resources.IResource;
import com.spiddekauga.voider.resources.IResourcePosition;
import com.spiddekauga.voider.utils.Pools;

/**
 * Tool for adding/removing/changing triggers
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class TriggerTool extends TouchTool implements ISelectTool {
	/**
	 * Creates the trigger tool with all necessary variables
	 * @param camera used for determining pointer location
	 * @param world used for picking
	 * @param invoker for undo/redo
	 * @param levelEditor sending command to it
	 */
	public TriggerTool(Camera camera, World world, Invoker invoker, LevelEditor levelEditor) {
		super(camera, world);

		mInvoker = invoker;
		mLevelEditor = levelEditor;
	}

	/**
	 * States of the trigger tool, note not all states
	 * are available for all kinds of triggers
	 */
	public enum States {
		/** Adds a new trigger, or selects one if pressed on it */
		ADD,
		/** Selects a trigger */
		SELECT,
		/** Removes a trigger */
		REMOVE,
		/** Moves a trigger that is movable (implements IPosition) */
		MOVE,
	}

	/**
	 * Sets the state of the trigger tool
	 * @param state new state of the trigger tool
	 */
	public void setState(States state) {
		mState = state;
	}

	@Override
	public void activate() {
		enableSelectedDrawing();
	}

	@Override
	public void deactivate() {
		disableSelectedDrawing();
	}

	/**
	 * @return current state of this trigger tool
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
	public void setSelectedResource(IResource selectedResource) {
		disableSelectedDrawing();

		Trigger oldSelected = mSelectedTrigger;
		mSelectedTrigger = (Trigger) selectedResource;

		for (ISelectListener selectListener : mSelectListeners) {
			selectListener.onResourceSelected(oldSelected, mSelectedTrigger);
		}

		mChangedSinceUp = true;

		enableSelectedDrawing();
	}

	@Override
	public IResource getSelectedResource() {
		return mSelectedTrigger;
	}

	@Override
	protected void down() {
		testPick(Editor.PICK_TRIGGER_SIZE);
		Object hitObject = null;
		if (mHitBody != null) {
			hitObject = mHitBody.getUserData();
		}

		switch (mState) {
		case ADD:
			// Just select the trigger
			if (hitObject instanceof Trigger) {
				if (hitObject != mSelectedTrigger) {
					mInvoker.execute(new CResourceSelect((IResource) hitObject, this));
				}

				if (hitObject instanceof IResourcePosition) {
					mDragOrigin.set(((IResourcePosition) hitObject).getPosition());
				}
			}
			// Create TriggerActorActivated
			else if (hitObject instanceof EnemyActor) {
				TActorActivated newTrigger = new TActorActivated((Actor) hitObject);
				mInvoker.execute(new CResourceAdd(newTrigger, mLevelEditor));
				mInvoker.execute(new CResourceSelect(newTrigger, this), true);
			}
			// Create TriggerScreenAt
			else {
				TScreenAt newTrigger = new TScreenAt(mLevelEditor.getLevel(), mTouchCurrent.x);
				mInvoker.execute(new CResourceAdd(newTrigger, mLevelEditor));
				mInvoker.execute(new CResourceSelect(newTrigger, this), true);

				mDragOrigin.set(mTouchOrigin);
			}
			break;

		case MOVE:
		case SELECT:
			if (hitObject instanceof Trigger) {
				if (hitObject != mSelectedTrigger) {
					mInvoker.execute(new CResourceSelect((IResource) hitObject, this));
				}

				if (hitObject instanceof IResourcePosition) {
					mDragOrigin.set(((IResourcePosition) hitObject).getPosition());
				}

			} else {
				if (mSelectedTrigger != null) {
					mInvoker.execute(new CResourceSelect(null, this));
				}
			}
			break;


		case REMOVE:
			if (hitObject instanceof Trigger) {
				// Hit same twice, remove it
				if (hitObject == mSelectedTrigger) {
					mInvoker.execute(new CResourceRemove(mSelectedTrigger, mLevelEditor));
					mInvoker.execute(new CResourceSelect(null, this), true);
				} else {
					mInvoker.execute(new CResourceSelect((IResource) hitObject, this));
				}
			} else {
				if (mSelectedTrigger != null) {
					mInvoker.execute(new CResourceSelect(null, this));
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
			// Can only move triggers that have positions
			if (mSelectedTrigger instanceof IResourcePosition) {
				Vector2 newPosition = getNewMovePosition();
				((IResourcePosition) mSelectedTrigger).setPosition(newPosition);
				Pools.vector2.free(newPosition);
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
		switch (mState) {
		case ADD:
		case MOVE:
			if (mSelectedTrigger instanceof IResourcePosition) {
				// Reset trigger to original position
				((IResourcePosition) mSelectedTrigger).setPosition(mDragOrigin);

				// Move the trigger using a command
				Vector2 newPosition = getNewMovePosition();
				mInvoker.execute(new CResourceMove((IResourcePosition) mSelectedTrigger, newPosition, mLevelEditor));
				Pools.vector2.free(newPosition);

			}
			break;


		case SELECT:
		case REMOVE:
			// Does nothing
			break;
		}

		mChangedSinceUp = false;
	}

	@Override
	protected QueryCallback getCallback() {
		return mCallback;
	}

	@Override
	protected Body filterPick(ArrayList<Body> hitBodies) {
		if (!mHitBodies.isEmpty()) {
			return mHitBodies.get(0);
		} else {
			return null;
		}
	}

	/**
	 * Disables the special drawing on the selected trigger.
	 */
	protected void disableSelectedDrawing() {
		if (mSelectedTrigger != null) {
			mSelectedTrigger.setSelected(false);
		}
	}

	/**
	 * Enables the special drawing on the selected trigger.
	 */
	protected void enableSelectedDrawing() {
		if (mSelectedTrigger != null) {
			mSelectedTrigger.setSelected(true);
		}
	}

	/**
	 * @return new position to move the path to. Don't forget to free this position
	 * using Pools.free(newPos).
	 */
	private Vector2 getNewMovePosition() {
		Vector2 newPosition = Pools.vector2.obtain();
		newPosition.set(mTouchCurrent).sub(mTouchOrigin);
		newPosition.add(mDragOrigin);

		return newPosition;
	}


	/** Callback for only picking correct shapes */
	private QueryCallback mCallback = new QueryCallback() {
		@Override
		public boolean reportFixture(Fixture fixture) {
			Body body = fixture.getBody();
			Object userData = body.getUserData();
			if (userData instanceof Trigger) {
				mHitBodies.clear();
				mHitBodies.add(body);
				return false;
			}
			else if (userData instanceof EnemyActor) {
				mHitBodies.add(body);
			}

			return true;
		}
	};

	/** Original position of the currently dragging body */
	private Vector2 mDragOrigin = new Vector2();
	/** Changed trigger since up */
	private boolean mChangedSinceUp = false;
	/** Selection listeners */
	private ArrayList<ISelectListener> mSelectListeners = new ArrayList<ISelectListener>();
	/** Current state of the tool */
	private States mState = States.ADD;
	/** Currently selected trigger */
	private Trigger mSelectedTrigger = null;
	/** Invoker to send the command to */
	private Invoker mInvoker;
	/** Level editor */
	private LevelEditor mLevelEditor;
}

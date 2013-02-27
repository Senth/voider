package com.spiddekauga.voider.scene;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.spiddekauga.utils.Invoker;
import com.spiddekauga.voider.editor.LevelEditor;
import com.spiddekauga.voider.game.actors.EnemyActor;
import com.spiddekauga.voider.game.triggers.Trigger;
import com.spiddekauga.voider.resources.IResource;

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
		disableSelectedDrawing();

		mState = state;

		enableSelectedDrawing();
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
		// TODO Auto-generated method stub
	}

	@Override
	protected void dragged() {
		// TODO Auto-generated method stub
	}

	@Override
	protected void up() {
		// TODO Auto-generated method stub
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
		// TODO
	}

	/**
	 * Enables the special drawing on the selected trigger.
	 */
	protected void enableSelectedDrawing() {
		// TODO
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

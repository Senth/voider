package com.spiddekauga.voider.scene;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.spiddekauga.utils.Invoker;
import com.spiddekauga.voider.editor.HitWrapper;
import com.spiddekauga.voider.editor.IResourceChangeEditor;
import com.spiddekauga.voider.editor.commands.CActorCenterMove;
import com.spiddekauga.voider.editor.commands.CResourceAdd;
import com.spiddekauga.voider.editor.commands.CResourceCornerAdd;
import com.spiddekauga.voider.editor.commands.CResourceCornerMove;
import com.spiddekauga.voider.editor.commands.CResourceCornerRemove;
import com.spiddekauga.voider.editor.commands.CResourceCornerRemoveAll;
import com.spiddekauga.voider.editor.commands.CResourceMove;
import com.spiddekauga.voider.editor.commands.CResourceRemove;
import com.spiddekauga.voider.editor.commands.CResourceSelect;
import com.spiddekauga.voider.game.actors.Actor;
import com.spiddekauga.voider.game.actors.ActorDef;
import com.spiddekauga.voider.game.actors.BulletActor;
import com.spiddekauga.voider.resources.IResource;
import com.spiddekauga.voider.resources.IResourceCorner.PolygonComplexException;
import com.spiddekauga.voider.resources.IResourceCorner.PolygonCornerTooCloseException;
import com.spiddekauga.voider.utils.Vector2Pool;

/**
 * Abstract class that can draw actors
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class DrawActorTool extends ActorTool implements ISelectTool {
	/**
	 * Creates a draw actor tool.
	 * @param camera used for determining where the pointer is in the world
	 * @param world used for picking
	 * @param actorType the actor type to create/use
	 * @param invoker used for undoing/redoing some commands
	 * @param actorEditor editor for the actor, will call some methods in here
	 */
	public DrawActorTool(Camera camera, World world, Class<?> actorType, Invoker invoker, IResourceChangeEditor actorEditor) {
		super(camera, world, actorType);
		mInvoker = invoker;
		mActorEditor = actorEditor;
		mOnlyOneActor = false;
	}

	/**
	 * Creates a draw actor tool. This will only create one actor with the specified
	 * actor definition.
	 * @param camera used for determining where the pointer is in the world
	 * @param world used for picking
	 * @param actorType the actor type to create/use
	 * @param invoker used for undoing/redoing some commands
	 * @param actorEditor editor for the actor, will call some methods in here
	 * @param actorDef the actor definition to use for the only actor
	 */
	public DrawActorTool(Camera camera, World world, Class<?> actorType, Invoker invoker, IResourceChangeEditor actorEditor, ActorDef actorDef) {
		super(camera, world, actorType);
		mInvoker = invoker;
		mActorEditor = actorEditor;
		mOnlyOneActor = true;
		mActorDef = actorDef;
	}

	/**
	 * Sets the specified actor definition to use for the actors. This
	 * will deselect any actor. Will force the draw tool to only draw one actor
	 * @param actorDef the new actor definition to use.
	 */
	public void setActorDef(ActorDef actorDef) {
		mActorDef = actorDef;
		mOnlyOneActor = true;
		mHitBody = null;

		deactivate();

		if (actorDef.getCornerCount() > 0) {
			if (mActor == null) {
				mActor = new BulletActor();
				mActorEditor.onResourceAdded(mActor);
			}
			mActor.setDef(actorDef);
		} else {
			mActorEditor.onResourceRemoved(mActor);
			mActor = null;
		}
	}

	/**
	 * Clears the tool. This will remove any selected actor.
	 * @note the actor definition will remain if it has been set
	 */
	@Override
	public void clear() {
		if (mActor != null) {
			mActor.destroyBodyCenter();
			mActor.destroyBodyCorners();
		}

		mActor = null;
	}

	/**
	 * Activates the tool. I.e. it will recreate any temporary bodies that were
	 * destroyed.
	 */
	@Override
	public void activate() {
		if (mActor != null) {
			/** @todo set actor to be draw differently */

			if (mOnlyOneActor) {
				mActor.createBody();
			}

			switch (mState) {
			case ADD_CORNER:
			case REMOVE:
				mActor.createBodyCorners();
				break;

			case MOVE:
				// Does nothing
				break;

			case SET_CENTER:
				mActor.createBodyCenter();
				break;
			}
		}
	}

	/**
	 * Deactivates the tool. I.e. it will remove the temporary bodies that has
	 * been created.
	 */
	@Override
	public void deactivate() {
		if (mActor != null) {
			/** @todo set actor to be draw differently */

			if (mOnlyOneActor) {
				mActor.destroyBody();
			}

			switch (mState) {
			case ADD_CORNER:
			case REMOVE:
				mActor.destroyBodyCorners();
				break;

			case MOVE:
				// Does nothing
				break;

			case SET_CENTER:
				mActor.destroyBodyCenter();
				break;
			}
		}
	}

	/**
	 * Sets the state of the tool
	 * @param state which state the tool is actively in
	 */
	public void setState(States state) {
		// Delete old state values
		deactivate();

		mState = state;

		activate();
	}

	/**
	 * @return current state of the draw actor tool
	 */
	public States getState() {
		return mState;
	}

	/**
	 * All the states the tool can be in
	 */
	public enum States {
		/** Adds corners to the actor */
		ADD_CORNER,
		/** Removes either corners the whole actor */
		REMOVE,
		/** Moves the whole actor, corners can be moved in ADD_CORNER state */
		MOVE,
		/** Sets the center of the actor */
		SET_CENTER,
	}

	@Override
	public void setSelectedResource(IResource selectedActor) {
		deactivate();

		Actor oldSelected = mActor;
		mActor = (Actor) selectedActor;

		for (ISelectListener listener : mSelectListeners) {
			listener.onResourceSelected(oldSelected, mActor);
		}

		activate();
	}

	@Override
	public Actor getSelectedResource() {
		return mActor;
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
	protected void down() {
		switch (mState) {
		case ADD_CORNER:
			// Double click inside current actor finishes/closes it, but only if we can have more than
			// one actor
			if (mDoubleClick && hitSelectedActor() && !mOnlyOneActor) {
				// Remove the last corner if we accidentally added one when double clicking
				if (mCornerIndexLast != -1) {
					mInvoker.undo(false);
				}
				mInvoker.execute(new CResourceSelect(null, this));
				return;
			}

			// Test if we hit a body or corner
			testPick();

			// If we didn't change actor, do something
			if (mHitBody != null) {
				// Hit the body (no corner), create corner
				if (hitSelectedActor()) {
					if (!mChangedActorSinceUp) {
						createTempCorner();
					}
				}
				// Hit another actor
				else if (mHitBody.getUserData() instanceof Actor) {
					// We can only hit our actor, create a temp corner
					if (mOnlyOneActor) {
						createTempCorner();
					}
					// Select the other actor if we can create multiple actors
					else {
						mInvoker.execute(new CResourceSelect((Actor)mHitBody.getUserData(), this));
					}
				}
				// Else hit a corner, start moving it
				else {
					mCornerIndexCurrent = mActor.getCornerIndex(mHitBody.getPosition());
					mDragOrigin.set(mActor.getDef().getCornerPosition(mCornerIndexCurrent));
					mCornerAddedNow = false;
				}
			}
			// Else create a new corner
			else {
				// No actor, create a new actor
				if (mActor == null /** @todo !mLevel.containsActor(mActor) */) {
					Actor actor = newActor();
					if (mOnlyOneActor) {
						actor.setDef(mActorDef);
					}
					actor.setPosition(mTouchOrigin);
					mInvoker.execute(new CResourceAdd(actor, mActorEditor));
					mInvoker.execute(new CResourceSelect(actor, this), true);
				}

				if (mActor != null) {
					createTempCorner();
				}
			}
			break;


		case MOVE:
			testPick();

			// If hit actor (no corner), start dragging the actor
			if (mHitBody != null && mHitBody.getUserData() instanceof Actor) {
				// Select the actor
				if (mActor != mHitBody.getUserData()) {
					mInvoker.execute(new CResourceSelect((Actor) mHitBody.getUserData(), this));
				}
				mDragOrigin.set(mHitBody.getPosition());
			} else {
				if (mActor != null) {
					mInvoker.execute(new CResourceSelect(null, this));
				}
			}
			break;


		case REMOVE:
			testPick();

			// If we hit the actor's body twice (no corners) we delete the actor along with
			// all the corners. If we hit a corner that corner is deleted.
			if (mHitBody != null) {
				// Hit actor body (no corner) and it's the second time -> Remove actor
				if (mHitBody.getUserData() == mActor) {
					// Only do something if we didn't hit the actor the first time
					if (!mChangedActorSinceUp) {
						mInvoker.execute(new CResourceRemove(mActor, mActorEditor));
						mInvoker.execute(new CResourceCornerRemoveAll(mActor.getDef(), mActorEditor), true);
						mInvoker.execute(new CResourceSelect(null, this), true);
					}
				}
				// Else hit a corner, delete it
				else if (mHitBody.getUserData() instanceof HitWrapper){
					mCornerIndexCurrent = mActor.getCornerIndex(mHitBody.getPosition());
					mInvoker.execute(new CResourceCornerRemove(mActor.getDef(), mCornerIndexCurrent, mActorEditor));

					// Was it the last corner? Remove actor too then
					if (mActor.getDef().getCornerCount() == 0) {
						mInvoker.execute(new CResourceRemove(mActor, mActorEditor), true);
						mInvoker.execute(new CResourceSelect(null, this), true);
					}
				}
				// Hit another actor
				else if (mHitBody.getUserData() instanceof Actor) {
					mInvoker.execute(new CResourceSelect((Actor)mHitBody.getUserData(), this));
				}

			}
			break;


		case SET_CENTER:
			if (mActor != null) {
				mDragOrigin.set(mActor.getPosition());
				mCenterOffsetOrigin.set(mActor.getDef().getCenterOffset());
				Vector2 centerOffset = Vector2Pool.obtain();
				centerOffset.set(mDragOrigin).sub(mTouchCurrent);
				centerOffset.add(mActor.getDef().getCenterOffset());
				mActor.getDef().setCenterOffset(centerOffset);
				mActor.destroyBody();
				mActor.setPosition(mTouchOrigin);
				mActor.createBody();
				Vector2Pool.free(centerOffset);
			}
			break;
		}
	}

	@Override
	protected void dragged() {
		switch (mState) {
		case ADD_CORNER:
			if (mCornerIndexCurrent != -1) {
				try {
					Vector2 newCornerPos = Vector2Pool.obtain();
					newCornerPos.set(mTouchCurrent).sub(mActor.getPosition()).sub(mActor.getDef().getCenterOffset());
					mActor.getDef().moveCorner(mCornerIndexCurrent, newCornerPos);
					Vector2Pool.free(newCornerPos);
				} catch (Exception e) {
					// Does nothing
				}
			}
			break;


		case MOVE:
			if (mActor != null) {
				Vector2 newPosition = getNewMovePosition();
				mActor.setPosition(newPosition);
				Vector2Pool.free(newPosition);
			}
			break;


		case REMOVE:
			// Does nothing
			break;


		case SET_CENTER:
			if (mActor != null) {
				Vector2 centerOffset = Vector2Pool.obtain();
				centerOffset.set(mDragOrigin).sub(mTouchCurrent);
				centerOffset.add(mCenterOffsetOrigin);
				mActor.getDef().setCenterOffset(centerOffset);
				mActor.destroyBody();
				mActor.setPosition(mTouchCurrent);
				mActor.createBody();
				Vector2Pool.free(centerOffset);
			}
			break;
		}
	}

	@Override
	protected void up() {
		switch (mState) {
		case ADD_CORNER:
			if (mActor != null && mCornerIndexCurrent != -1) {
				// New corner
				if (mCornerAddedNow) {
					createCornerFromTemp();
				}
				// Move corner
				else {
					// Reset to original position
					Vector2 newPos = Vector2Pool.obtain();

					newPos.set(mActor.getDef().getCornerPosition(mCornerIndexCurrent));
					try {
						mActor.getDef().moveCorner(mCornerIndexCurrent, mDragOrigin);
						mInvoker.execute(new CResourceCornerMove(mActor.getDef(), mCornerIndexCurrent, newPos, mActorEditor));
					} catch (Exception e) {
						// Does nothing
					}

					Vector2Pool.free(newPos);
				}
			}

			mCornerIndexLast = mCornerIndexCurrent;
			mCornerIndexCurrent = -1;
			mCornerAddedNow = false;
			break;


		case MOVE:
			// Set the new position of the actor
			if (mActor != null) {
				// Reset actor to original position
				mActor.setPosition(mDragOrigin);

				Vector2 newPos = getNewMovePosition();
				mInvoker.execute(new CResourceMove(mActor, newPos, mActorEditor), mChangedActorSinceUp);
				Vector2Pool.free(newPos);
			}
			break;


		case REMOVE:
			// Does nothing
			break;


		case SET_CENTER:
			if (mActor != null) {
				Vector2 centerOffset = Vector2Pool.obtain();
				centerOffset.set(mDragOrigin).sub(mTouchCurrent);
				centerOffset.add(mCenterOffsetOrigin);

				mActor.destroyBody();
				mActor.setPosition(mDragOrigin);

				mInvoker.execute(new CActorCenterMove(mActor.getDef(), centerOffset, mCenterOffsetOrigin, mActorEditor, mActor));

				Vector2Pool.free(centerOffset);
			}
			break;
		}
	}

	/**
	 * @return true if we hit the selected actor
	 */
	private boolean hitSelectedActor() {
		return mHitBody != null && mHitBody.getUserData() == mActor;
	}

	/**
	 * Creates a temporary corner
	 */
	private void createTempCorner() {
		Vector2 localPos = Vector2Pool.obtain();

		localPos.set(mTouchOrigin).sub(mActor.getPosition()).sub(mActor.getDef().getCenterOffset());

		try {
			mActor.getDef().addCorner(localPos);
			mCornerIndexCurrent = mActor.getDef().getCornerCount() - 1;
			mDragOrigin.set(mTouchOrigin);
			mCornerAddedNow = true;
			mActor.reloadFixtures();
		} catch (PolygonComplexException e) {
			/** @TODO print some error message on screen, cannot add corner here */
		} catch (PolygonCornerTooCloseException e) {
			/** @TODO print error message on screen */
		}

		Vector2Pool.free(localPos);
	}

	/**
	 * Creates a corner from the temporary corner
	 */
	private void createCornerFromTemp() {
		// Get the position of the corner then remove it
		Vector2 cornerPos = mActor.getDef().getCornerPosition(mCornerIndexCurrent);
		mActor.getDef().removeCorner(mCornerIndexCurrent);

		// Set the command as chained if no corner exist in the actor
		boolean chained = mActor.getDef().getCornerCount() == 0;

		mInvoker.execute(new CResourceCornerAdd(mActor.getDef(), cornerPos, mActorEditor), chained);
	}

	/**
	 * @return new position to move the actor to. Don't forget to free
	 * this position using Pools.free(newPos)
	 */
	private Vector2 getNewMovePosition() {
		// Get diff movement
		Vector2 newPosition = Vector2Pool.obtain();
		newPosition.set(mTouchCurrent).sub(mTouchOrigin);

		// Add original position
		newPosition.add(mDragOrigin);

		return newPosition;
	}


	/** Invoker used for undoing/redoing commands */
	protected Invoker mInvoker = null;
	/** Current state of the tool */
	protected States mState = States.ADD_CORNER;
	/** Current actor we're editing */
	protected Actor mActor = null;

	/** Origin of the drag */
	private Vector2 mDragOrigin = new Vector2();
	/** Origin of center offset */
	private Vector2 mCenterOffsetOrigin = new Vector2();
	/** Current corner index */
	private int mCornerIndexCurrent = -1;
	/** Last corner index */
	private int mCornerIndexLast = -1;
	/** If we changed actor since the last up */
	private boolean mChangedActorSinceUp = false;
	/** True if the current corner was added during the down() event */
	private boolean mCornerAddedNow = false;
	/** The actor editor */
	private IResourceChangeEditor mActorEditor;
	/** All select listeners */
	private ArrayList<ISelectListener> mSelectListeners = new ArrayList<ISelectListener>();

	/** If only one actor shall be able to be created simultaneously */
	private boolean mOnlyOneActor;
	/** Actor definition, only set if only one actor */
	private ActorDef mActorDef = null;
}

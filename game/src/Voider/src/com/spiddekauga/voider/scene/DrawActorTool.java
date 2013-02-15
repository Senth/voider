package com.spiddekauga.voider.scene;

import java.lang.reflect.Constructor;
import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Pools;
import com.spiddekauga.utils.Invoker;
import com.spiddekauga.voider.editor.HitWrapper;
import com.spiddekauga.voider.editor.IActorDrawEditor;
import com.spiddekauga.voider.editor.commands.CActorAdd;
import com.spiddekauga.voider.editor.commands.CActorCenterMove;
import com.spiddekauga.voider.editor.commands.CActorCornerAdd;
import com.spiddekauga.voider.editor.commands.CActorCornerMove;
import com.spiddekauga.voider.editor.commands.CActorCornerRemove;
import com.spiddekauga.voider.editor.commands.CActorCornerRemoveAll;
import com.spiddekauga.voider.editor.commands.CActorMove;
import com.spiddekauga.voider.editor.commands.CActorRemove;
import com.spiddekauga.voider.editor.commands.CActorSelect;
import com.spiddekauga.voider.editor.commands.CActorSelect.IActorSelect;
import com.spiddekauga.voider.game.actors.Actor;
import com.spiddekauga.voider.game.actors.ActorDef;
import com.spiddekauga.voider.game.actors.ActorDef.PolygonComplexException;
import com.spiddekauga.voider.game.actors.ActorDef.PolygonCornerTooCloseException;

/**
 * Abstract class that can draw actors
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class DrawActorTool extends TouchTool implements IActorSelect {
	/**
	 * Creates a draw actor tool.
	 * @param camera used for determining where the pointer is in the world
	 * @param world used for picking
	 * @param invoker used for undoing/redoing some commands
	 * @param actorType the actor type to create/use
	 * @param actorEditor editor for the actor, will call some methods in here
	 */
	public DrawActorTool(Camera camera, World world, Invoker invoker, Class<?> actorType, IActorDrawEditor actorEditor) {
		super(camera, world);
		mInvoker = invoker;
		mActorEditor = actorEditor;
		mOnlyOneActor = false;
		mActorType = actorType;
	}

	/**
	 * Creates a draw actor tool. This will only create one actor with the specified
	 * actor definition.
	 * @param camera used for determining where the pointer is in the world
	 * @param world used for picking
	 * @param invoker used for undoing/redoing some commands
	 * @param actorType the actor type to create/use
	 * @param actorEditor editor for the actor, will call some methods in here
	 * @param actorDef the actor definition to use for the only actor
	 */
	public DrawActorTool(Camera camera, World world, Invoker invoker, Class<?> actorType, IActorDrawEditor actorEditor, ActorDef actorDef) {
		super(camera, world);
		mInvoker = invoker;
		mActorEditor = actorEditor;
		mOnlyOneActor = true;
		mActorType = actorType;
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
		mActor = null;
		mHitBody = null;
	}

	/**
	 * Activates the tool. I.e. it will recreate any temporary bodies that were
	 * destroyed.
	 */
	public void activate() {
		if (mActor != null) {
			mActor.createBody();

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
	public void deactivate() {
		if (mActor != null) {
			mActor.destroyBody();

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
	public void setSelectedActor(Actor actor) {
		deactivate();

		mActor = actor;

		activate();
	}

	@Override
	public Actor getSelectedActor() {
		return mActor;
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
				mInvoker.execute(new CActorSelect(this, null));
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
						mInvoker.execute(new CActorSelect(this, (Actor)mHitBody.getUserData()));
					}
				}
				// Else hit a corner, start moving it
				else {
					mCornerIndexCurrent = mActor.getCornerIndex(mHitBody.getPosition());
					mDragOrigin.set(mHitBody.getPosition());
					mCornerAddedNow = false;
				}
			}
			// Else create a new corner
			else {
				// No actor, create a new actor
				if (mActor == null /** @todo !mLevel.containsActor(mActor) */) {
					Actor actor = newActor();
					actor.setPosition(mTouchOrigin);
					mInvoker.execute(new CActorAdd(actor, mActorEditor));
					mInvoker.execute(new CActorSelect(this, actor), true);
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
					mInvoker.execute(new CActorSelect(this, (Actor) mHitBody.getUserData()));
				}
				mDragOrigin.set(mHitBody.getPosition());
			} else {
				if (mActor != null) {
					mInvoker.execute(new CActorSelect(this, null));
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
						mInvoker.execute(new CActorRemove(mActor, mActorEditor));
						mInvoker.execute(new CActorCornerRemoveAll(mActor.getDef()), true);
						mInvoker.execute(new CActorSelect(this, null), true);
					}
				}
				// Else hit a corner, delete it
				else {
					mCornerIndexCurrent = mActor.getCornerIndex(mHitBody.getPosition());
					mInvoker.execute(new CActorCornerRemove(mActor.getDef(), mCornerIndexCurrent));

					// Was it the last corner? Remove actor too then
					if (mActor.getDef().getCornerCount() == 0) {
						mInvoker.execute(new CActorRemove(mActor, mActorEditor), true);
						mInvoker.execute(new CActorSelect(this, null), true);
					}
				}
			}
			break;


		case SET_CENTER:
			if (mActor != null) {
				mDragOrigin.set(mActor.getPosition());
				mCenterOffsetOrigin.set(mActor.getDef().getCenterOffset());
				Vector2 centerOffset = Pools.obtain(Vector2.class);
				centerOffset.set(mDragOrigin).sub(mTouchCurrent);
				centerOffset.add(mActor.getDef().getCenterOffset());
				mActor.getDef().setCenterOffset(centerOffset);
				mActor.destroyBody();
				mActor.setPosition(mTouchOrigin);
				mActor.createBody();
				Pools.free(centerOffset);
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
					Vector2 newCornerPos = Pools.obtain(Vector2.class);
					newCornerPos.set(mTouchCurrent).sub(mActor.getPosition()).sub(mActor.getDef().getCenterOffset());
					mActor.getDef().moveCorner(mCornerIndexCurrent, newCornerPos);
					Pools.free(newCornerPos);
				} catch (Exception e) {
					// Does nothing
				}
			}
			break;


		case MOVE:
			if (mActor != null) {
				Vector2 newPosition = getNewMovePosition();
				mActor.setPosition(newPosition);
				Pools.free(newPosition);
			}
			break;


		case REMOVE:
			// Does nothing
			break;


		case SET_CENTER:
			if (mActor != null) {
				Vector2 centerOffset = Pools.obtain(Vector2.class);
				centerOffset.set(mDragOrigin).sub(mTouchCurrent);
				centerOffset.add(mCenterOffsetOrigin);
				mActor.getDef().setCenterOffset(centerOffset);
				mActor.destroyBody();
				mActor.setPosition(mTouchCurrent);
				mActor.createBody();
				Pools.free(centerOffset);
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
					Vector2 newPos = Pools.obtain(Vector2.class);

					newPos.set(mActor.getDef().getCornerPosition(mCornerIndexCurrent));
					try {
						mActor.getDef().moveCorner(mCornerIndexCurrent, mDragOrigin);
						mInvoker.execute(new CActorCornerMove(mActor.getDef(), mCornerIndexCurrent, newPos));
					} catch (Exception e) {
						// Does nothing
					}

					Pools.free(newPos);
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
				mInvoker.execute(new CActorMove(mActor, newPos), mChangedActorSinceUp);
				Pools.free(newPos);
			}
			break;


		case REMOVE:
			// Does nothing
			break;


		case SET_CENTER:
			if (mActor != null) {
				Vector2 centerOffset = Pools.obtain(Vector2.class);
				centerOffset.set(mDragOrigin).sub(mTouchCurrent);
				centerOffset.add(mCenterOffsetOrigin);

				mActor.destroyBody();
				mActor.setPosition(mDragOrigin);

				mInvoker.execute(new CActorCenterMove(mActor.getDef(), centerOffset, mCenterOffsetOrigin, mActor));

				Pools.free(centerOffset);
			}
			break;
		}
	}

	/**
	 * Called when a new actor shall be created.
	 * @return new actor to be used
	 */
	protected Actor newActor() {
		try {
			Constructor<?> constructor = mActorType.getConstructor();
			Actor actor = (Actor) constructor.newInstance();
			actor.setSkipRotating(true);

			if (mOnlyOneActor) {
				actor.setDef(mActorDef);
			}
			return actor;

		} catch (Exception e) {
			Gdx.app.error("DrawActorTool", e.toString());
		}

		return null;
	}

	@Override
	protected QueryCallback getCallback() {
		return mCallback;
	}

	@Override
	protected Body filterPick(ArrayList<Body> hitBodies) {
		// Because the query only will return one body, set the hit body
		// as the first from hit bodies
		if (!hitBodies.isEmpty()) {
			return hitBodies.get(0);
		} else {
			return null;
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
		Vector2 localPos = Pools.obtain(Vector2.class);

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

		Pools.free(localPos);
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

		mInvoker.execute(new CActorCornerAdd(mActor.getDef(), cornerPos), chained);
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
		newPosition.add(mDragOrigin);

		return newPosition;
	}

	/** Picking for current actor type */
	private QueryCallback mCallback = new QueryCallback() {
		@Override
		public boolean reportFixture(Fixture fixture) {
			if (fixture.testPoint(mTouchCurrent)) {
				Body body = fixture.getBody();
				// Hit a corner
				if (body.getUserData() instanceof HitWrapper) {
					HitWrapper hitWrapper = (HitWrapper) body.getUserData();
					if (hitWrapper.actor != null && hitWrapper.actor.getClass() == mActorType) {
						mHitBodies.clear();
						mHitBodies.add(fixture.getBody());
						return false;
					}
				}
				// Hit an actor
				else if (body.getUserData() != null && body.getUserData().getClass() == mActorType) {
					mHitBodies.add(body);
				}
			}
			return true;
		}
	};

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
	private IActorDrawEditor mActorEditor;
	/** The actor type that will be created */
	private Class<?> mActorType;
	/** If only one actor shall be able to be created simultaneously */
	private boolean mOnlyOneActor;
	/** Actor definition, only set if only one actor */
	private ActorDef mActorDef = null;
}

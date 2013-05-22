package com.spiddekauga.voider.scene;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.spiddekauga.utils.Collections;
import com.spiddekauga.utils.Invoker;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.editor.HitWrapper;
import com.spiddekauga.voider.editor.IResourceChangeEditor;
import com.spiddekauga.voider.editor.commands.CActorCenterMove;
import com.spiddekauga.voider.editor.commands.CActorDefFixCustomFixtures;
import com.spiddekauga.voider.editor.commands.CResourceAdd;
import com.spiddekauga.voider.editor.commands.CResourceCornerAdd;
import com.spiddekauga.voider.editor.commands.CResourceCornerMove;
import com.spiddekauga.voider.editor.commands.CResourceCornerRemove;
import com.spiddekauga.voider.editor.commands.CResourceCornerRemoveAll;
import com.spiddekauga.voider.editor.commands.CResourceCornerRemoveExcessive;
import com.spiddekauga.voider.editor.commands.CResourceMove;
import com.spiddekauga.voider.editor.commands.CResourceRemove;
import com.spiddekauga.voider.editor.commands.CResourceSelect;
import com.spiddekauga.voider.game.actors.Actor;
import com.spiddekauga.voider.game.actors.ActorDef;
import com.spiddekauga.voider.resources.IResource;
import com.spiddekauga.voider.utils.Geometry;
import com.spiddekauga.voider.utils.Geometry.PolygonComplexException;
import com.spiddekauga.voider.utils.Geometry.PolygonCornersTooCloseException;
import com.spiddekauga.voider.utils.Pools;

/**
 * Abstract class that can draw actors
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class DrawActorTool extends ActorTool implements ISelectListener {
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

		addListener(this);
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

		if (actorDef.getVisualVars().getCornerCount() > 0) {
			if (mSelectedActor == null) {
				mSelectedActor = newActor();
				mActorEditor.onResourceAdded(mSelectedActor);
			}
			mSelectedActor.setDef(actorDef);
			mSelectedActor.setSkipRotating(true);
		} else {
			mActorEditor.onResourceRemoved(mSelectedActor);
			mSelectedActor = null;
		}

		activate();
	}

	/**
	 * Clears the tool. This will remove any selected actor.
	 * @note the actor definition will remain if it has been set
	 */
	@Override
	public void clear() {
		if (mSelectedActor != null) {
			mSelectedActor.destroyBodyCenter();
			mSelectedActor.destroyBodyCorners();
		}

		mSelectedActor = null;
	}

	/**
	 * Activates the tool. I.e. it will recreate any temporary bodies that were
	 * destroyed.
	 */
	@Override
	public void activate() {
		super.activate();

		if (mSelectedActor != null) {

			if (mOnlyOneActor) {
				mSelectedActor.createBody();
			}

			switch (mState) {
			case ADJUST_MOVE_CORNER:
			case ADJUST_REMOVE_CORNER:
			case ADJUST_ADD_CORNER:
				mSelectedActor.createBodyCorners();
				mCornerIndexCurrent = -1;
				mCornerIndexLast = -1;
				// No break

			case DRAW_APPEND:
				mSelectedActor.setDrawOnlyOutline(true);
				break;

			case DRAW_ERASE:
			case MOVE:
				// Does nothing
				break;

			case SET_CENTER:
				mSelectedActor.createBodyCenter();
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
		super.deactivate();

		if (mSelectedActor != null) {

			if (mOnlyOneActor) {
				mSelectedActor.destroyBody();
			}

			switch (mState) {
			case ADJUST_MOVE_CORNER:
			case ADJUST_REMOVE_CORNER:
			case ADJUST_ADD_CORNER:
				mSelectedActor.destroyBodyCorners();
				// No break

			case DRAW_APPEND:
				mSelectedActor.setDrawOnlyOutline(false);
				break;

			case DRAW_ERASE:
			case MOVE:
				// Does nothing
				break;

			case SET_CENTER:
				mSelectedActor.destroyBodyCenter();
				break;
			}
		}
	}

	/**
	 * Sets the state of the tool
	 * @param state which state the tool is actively in
	 */
	public void setState(States state) {
		if (state == mState) {
			return;
		}

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
		/** Adds new corners to the actor, only one corner at the time.*/
		ADJUST_ADD_CORNER,
		/** Moves corners one at the time */
		ADJUST_MOVE_CORNER,
		/** Removes corners one at the time */
		ADJUST_REMOVE_CORNER,
		/** Append drawing */
		DRAW_APPEND,
		/** Adds or removes parts to the actor */
		DRAW_ERASE,
		/** Moves the whole actor */
		MOVE,
		/** Sets the center of the actor */
		SET_CENTER,
	}

	@Override
	public void onResourceSelected(IResource deselectedResource, IResource selectedResource) {
		mChangedActorThisEvent = true;
	}

	@Override
	protected void down() {
		mChangedActorThisEvent = false;

		// Double click inside current actor finishes/closes it, but only if we can have more than
		// one actor
		if (shallDeselectActor()) {
			deselectActor();
			return;
		}

		// Test if we hit a body or corner
		testPick();

		if (hitAnotherActor()) {
			mInvoker.execute(new CResourceSelect((Actor)mHitBody.getUserData(), this));
		}

		switch (mState) {
		case ADJUST_ADD_CORNER:
			// Only do stuff if we didn't change actor
			if (!mChangedActorThisEvent && mSelectedActor != null) {
				Vector2 localPos = getLocalPosition(mTouchCurrent);
				mCornerIndexCurrent = getIndexOfPosBetweenCorners(localPos);

				if (mCornerIndexCurrent != -1) {
					mSelectedActor.getDef().getVisualVars().addCorner(localPos, mCornerIndexCurrent);
					mCornerAddedNow = true;
				}

				Pools.vector2.free(localPos);

			}
			break;


		case ADJUST_MOVE_CORNER:
			if (mHitBody != null) {
				if (mHitBody.getUserData() instanceof HitWrapper) {
					mCornerIndexCurrent = mSelectedActor.getCornerIndex(mHitBody.getPosition());
					mDragOrigin.set(mSelectedActor.getDef().getVisualVars().getCornerPosition(mCornerIndexCurrent));
				}
			}
			break;


		case ADJUST_REMOVE_CORNER:
			// If we hit the actor's body twice (no corners) we delete the actor along with
			// all the corners. If we hit a corner that corner is deleted.
			if (mHitBody != null) {
				// Hit actor body (no corner) and it's the second time -> Remove actor
				if (hitSelectedActor()) {
					// Only do something if we didn't hit the actor the first time
					if (!mChangedActorThisEvent) {
						mInvoker.execute(new CActorDefFixCustomFixtures(mSelectedActor.getDef(), false));
						mInvoker.execute(new CResourceCornerRemoveAll(mSelectedActor.getDef().getVisualVars(), mActorEditor), true);
						mInvoker.execute(new CResourceRemove(mSelectedActor, mActorEditor), true);
						mInvoker.execute(new CResourceSelect(null, this), true);
					}
				}
				// Else hit a corner, delete it
				else if (mHitBody.getUserData() instanceof HitWrapper){
					int removeIndex = mSelectedActor.getCornerIndex(mHitBody.getPosition());
					if (removeIndex != -1) {
						mInvoker.execute(new CActorDefFixCustomFixtures(mSelectedActor.getDef(), false));
						mInvoker.execute(new CResourceCornerRemove(mSelectedActor.getDef().getVisualVars(), removeIndex, mActorEditor), true);

						// Was it the last corner? Remove actor too then
						if (mSelectedActor.getDef().getVisualVars().getCornerCount() == 0) {
							mInvoker.execute(new CResourceRemove(mSelectedActor, mActorEditor), true);
							mInvoker.execute(new CResourceSelect(null, this), true);
						} else {
							try {
								mInvoker.execute(new CActorDefFixCustomFixtures(mSelectedActor.getDef(), true), true);
							} catch (PolygonComplexException e) {
								/** @todo print some error message */
								mInvoker.undo();
								mInvoker.clearRedo();
							} catch (PolygonCornersTooCloseException e) {
								/** @todo print som error message */
								mInvoker.undo();
								mInvoker.clearRedo();
							}
						}
					}
				}
			}
			break;


		case DRAW_APPEND:
			if (!mChangedActorThisEvent) {
				// Create an actor if we don't have one selected
				if (mSelectedActor == null) {
					createNewSelectedActor();
					mChangedActorThisEvent = false;
				} else {
					mInvoker.execute(new CActorDefFixCustomFixtures(mSelectedActor.getDef(), false));
				}

				// Create corner here
				appendCorner(true);
			}
			break;


		case DRAW_ERASE:
			// TODO
			break;


		case MOVE:
			// If hit actor (no corner), start dragging the actor
			if (mHitBody != null && mHitBody.getUserData() instanceof Actor) {
				mDragOrigin.set(mHitBody.getPosition());
			} else {
				if (mSelectedActor != null) {
					mInvoker.execute(new CResourceSelect(null, this));
				}
			}
			break;


		case SET_CENTER:
			if (mSelectedActor != null) {
				mDragOrigin.set(mSelectedActor.getPosition());
				mCenterOffsetOrigin.set(mSelectedActor.getDef().getVisualVars().getCenterOffset());
				Vector2 centerOffset = Pools.vector2.obtain();
				centerOffset.set(mDragOrigin).sub(mTouchCurrent);
				centerOffset.add(mSelectedActor.getDef().getVisualVars().getCenterOffset());
				mSelectedActor.getDef().getVisualVars().setCenterOffset(centerOffset);
				mSelectedActor.destroyBody();
				mSelectedActor.setPosition(mTouchOrigin);
				mSelectedActor.createBody();
				Pools.vector2.free(centerOffset);
			}
			break;
		}
	}

	@Override
	protected void dragged() {
		switch (mState) {
		case ADJUST_ADD_CORNER:
		case ADJUST_MOVE_CORNER:
			if (mSelectedActor != null && mCornerIndexCurrent != -1) {
				Vector2 newCornerPos = getLocalPosition(mTouchCurrent);
				mSelectedActor.getDef().getVisualVars().moveCorner(mCornerIndexCurrent, newCornerPos);
				Pools.vector2.free(newCornerPos);
			}
			break;


		case ADJUST_REMOVE_CORNER:
			// Does nothing
			break;


		case DRAW_APPEND:
			if (!mChangedActorThisEvent) {
				// If has drawn more than minimum distance, add another corner here
				Vector2 diffVector = Pools.vector2.obtain();
				diffVector.set(mTouchCurrent).sub(mDragOrigin);
				if (diffVector.len2() >= Config.Editor.Actor.Visual.DRAW_NEW_CORNER_MIN_DIST_SQ) {
					appendCorner(true);
				}
				Pools.vector2.free(diffVector);
			}
			break;


		case DRAW_ERASE:
			// TODO
			break;


		case MOVE:
			if (mSelectedActor != null) {
				Vector2 newPosition = getNewMovePosition();
				mSelectedActor.setPosition(newPosition);
				Pools.vector2.free(newPosition);
			}
			break;


		case SET_CENTER:
			if (mSelectedActor != null) {
				Vector2 centerOffset = Pools.vector2.obtain();
				centerOffset.set(mDragOrigin).sub(mTouchCurrent);
				centerOffset.add(mCenterOffsetOrigin);
				mSelectedActor.getDef().getVisualVars().setCenterOffset(centerOffset);
				mSelectedActor.destroyBody();
				mSelectedActor.setPosition(mTouchCurrent);
				mSelectedActor.createBody();
				Pools.vector2.free(centerOffset);
			}
			break;
		}
	}

	@Override
	protected void up() {
		switch (mState) {
		case ADJUST_ADD_CORNER:
			if (mSelectedActor != null && mCornerIndexCurrent != -1) {
				// Remove temporary corner
				Vector2 removedCorner = mSelectedActor.getDef().getVisualVars().removeCorner(mCornerIndexCurrent);

				// Add the corner via invoker instead
				mInvoker.execute(new CActorDefFixCustomFixtures(mSelectedActor.getDef(), false));
				mInvoker.execute(new CResourceCornerAdd(mSelectedActor.getDef().getVisualVars(), removedCorner, mCornerIndexCurrent, mActorEditor), true);
				try {
					mInvoker.execute(new CActorDefFixCustomFixtures(mSelectedActor.getDef(), true), true);
				} catch (PolygonComplexException e) {
					/** @todo write some error message */
					handleBadCornerPosition(null);
				} catch (PolygonCornersTooCloseException e) {
					handleBadCornerPosition(null);
				}

				Pools.vector2.free(removedCorner);
			}
			break;


		case ADJUST_MOVE_CORNER:
			if (mSelectedActor != null && mCornerIndexCurrent != -1) {
				// Reset to original position
				Vector2 newPos = Pools.vector2.obtain();
				newPos.set(mSelectedActor.getDef().getVisualVars().getCornerPosition(mCornerIndexCurrent));
				mSelectedActor.getDef().getVisualVars().moveCorner(mCornerIndexCurrent, mDragOrigin);

				// Execute
				mInvoker.execute(new CActorDefFixCustomFixtures(mSelectedActor.getDef(), false), mCornerAddedNow);
				mInvoker.execute(new CResourceCornerMove(mSelectedActor.getDef().getVisualVars(), mCornerIndexCurrent, newPos, mActorEditor), true);
				try {
					mInvoker.execute(new CActorDefFixCustomFixtures(mSelectedActor.getDef(), true), true);
				} catch (PolygonComplexException e) {
					/** @todo print some error message */
					handleBadCornerPosition(null);
				} catch (PolygonCornersTooCloseException e) {
					/** @todo print some error message */
					handleBadCornerPosition(null);
				}

				Pools.vector2.free(newPos);
			}
			break;


		case ADJUST_REMOVE_CORNER:
			// Does nothing
			break;


		case DRAW_APPEND:
			if (!mChangedActorThisEvent) {
				if (mSelectedActor != null) {
					// Add a final corner when released
					appendCorner(true);

					mInvoker.execute(new CResourceCornerRemoveExcessive(mSelectedActor.getDef().getVisualVars()), true);

					try {
						mInvoker.execute(new CActorDefFixCustomFixtures(mSelectedActor.getDef(), true), true);
					} catch (PolygonComplexException e) {
						/** @todo print pop up error message */
						handleBadCornerPosition(null);
					} catch (PolygonCornersTooCloseException e) {
						/** @todo print pop up error message */
						handleBadCornerPosition(null);
					}
				}
			}
			break;


		case DRAW_ERASE:
			// TODO
			break;


		case MOVE:
			// Set the new position of the actor
			if (mSelectedActor != null) {
				// Reset actor to original position
				mSelectedActor.setPosition(mDragOrigin);

				Vector2 newPos = getNewMovePosition();
				mInvoker.execute(new CResourceMove(mSelectedActor, newPos, mActorEditor), mChangedActorThisEvent);
				Pools.vector2.free(newPos);
			}
			break;


		case SET_CENTER:
			if (mSelectedActor != null) {
				Vector2 centerOffset = Pools.vector2.obtain();
				centerOffset.set(mDragOrigin).sub(mTouchCurrent);
				centerOffset.add(mCenterOffsetOrigin);

				mSelectedActor.destroyBody();
				mSelectedActor.setPosition(mDragOrigin);

				mInvoker.execute(new CActorCenterMove(mSelectedActor.getDef(), centerOffset, mCenterOffsetOrigin, mActorEditor, mSelectedActor));

				Pools.vector2.free(centerOffset);
			}
			break;
		}

		mCornerIndexLast = mCornerIndexCurrent;
		mCornerIndexCurrent = -1;
		mCornerAddedNow = false;
	}

	/**
	 * @return true if we hit the selected actor
	 */
	private boolean hitSelectedActor() {
		return mHitBody != null && mHitBody.getUserData() == mSelectedActor;
	}

	/**
	 * @return true if we hit another actor
	 */
	private boolean hitAnotherActor() {
		return mHitBody != null && mHitBody.getUserData() instanceof Actor && mHitBody.getUserData() != mSelectedActor;
	}


	/**
	 * Appends a temporary corner in the current position
	 * @param chained if the command shall be chained or not.
	 */
	private void appendCorner(boolean chained) {
		Vector2 localPos = getLocalPosition(mTouchCurrent);

		mInvoker.execute(new CResourceCornerAdd(mSelectedActor.getDef().getVisualVars(), localPos, mActorEditor), chained);
		mDragOrigin.set(mTouchCurrent);
		mCornerIndexCurrent = mSelectedActor.getDef().getVisualVars().getCornerCount() - 1;

		Pools.vector2.free(localPos);
	}

	/**
	 * @return new position to move the actor to. Don't forget to free
	 * this position using Pools.free(newPos)
	 */
	private Vector2 getNewMovePosition() {
		// Get diff movement
		Vector2 newPosition = Pools.vector2.obtain();
		newPosition.set(mTouchCurrent).sub(mTouchOrigin);

		// Add original position
		newPosition.add(mDragOrigin);

		return newPosition;
	}

	/**
	 * @return true if we shall close the actor (double clicked inside it)
	 */
	private boolean shallDeselectActor() {
		return mDoubleClick && mSelectedActor != null && !mOnlyOneActor;
	}

	/**
	 * Deselects the current actor, will undo added corner if one was added.
	 */
	private void deselectActor() {
		// Remove the last corner if we accidentally added one when double clicking
		if (mCornerIndexLast != -1) {
			mInvoker.undo(false);
		}

		mInvoker.execute(new CResourceSelect(null, this));
	}

	/**
	 * Creates a new selected actor
	 */
	private void createNewSelectedActor() {
		Actor actor = newActor();
		if (mOnlyOneActor) {
			actor.setDef(mActorDef);
		}
		actor.setPosition(mTouchOrigin);
		mInvoker.execute(new CResourceAdd(actor, mActorEditor));
		mInvoker.execute(new CResourceSelect(actor, this), true);
	}

	/**
	 * Checks if a position is between two corners of the currently selected actor
	 * @param pos check if this is between two corners
	 * @return index of the second corner, i.e. if we hit between corner 19 and 20
	 * 20 will be returned, because that's where the new position would be placed.
	 * -1 if we didn't hit between two corners.
	 */
	private int getIndexOfPosBetweenCorners(Vector2 pos) {
		if (mSelectedActor == null) {
			return -1;
		}

		int bestCorner = -1;
		float bestDist = Config.Editor.Actor.Visual.NEW_CORNER_DIST_MAX_SQ;

		ArrayList<Vector2> corners = mSelectedActor.getDef().getVisualVars().getCorners();
		for (int i = 0; i < corners.size(); ++i) {
			int nextIndex = Collections.computeNextIndex(corners, i);
			float distance = Geometry.distBetweenPointLineSegmentSq(corners.get(i), corners.get(nextIndex), pos);

			if (distance < bestDist) {
				bestCorner = nextIndex;
				bestDist = distance;
			}
		}

		return bestCorner;
	}

	/**
	 * Handles a bad corner position
	 * @param message the message to print
	 */
	private void handleBadCornerPosition(String message) {
		/** @todo implement error messaage */
		mInvoker.undo();
		mInvoker.clearRedo();
		mCornerIndexCurrent = -1;
	}

	/**
	 * Get local position for the selected actor from the specified world position
	 * @param worldPos world position
	 * @return Local position of the currently selected actor, copy of worldPos if no actor was selected.
	 * Don't forget to free the localPosition using Pools.vector2.free(localPos)
	 */
	private Vector2 getLocalPosition(Vector2 worldPos) {
		Vector2 localPos = Pools.vector2.obtain();
		localPos.set(worldPos);

		if (mSelectedActor != null) {
			localPos.sub(mSelectedActor.getPosition()).sub(mSelectedActor.getDef().getVisualVars().getCenterOffset());
		}

		return localPos;
	}

	/** Invoker used for undoing/redoing commands */
	protected Invoker mInvoker = null;
	/** Current state of the tool */
	protected States mState = States.ADJUST_ADD_CORNER;

	/** Origin of the drag */
	private Vector2 mDragOrigin = new Vector2();
	/** Origin of center offset */
	private Vector2 mCenterOffsetOrigin = new Vector2();
	/** Current corner index */
	private int mCornerIndexCurrent = -1;
	/** Last corner index */
	private int mCornerIndexLast = -1;
	/** If we changed actor since the last up */
	private boolean mChangedActorThisEvent = false;
	/** True if the current corner was added during the down() event */
	private boolean mCornerAddedNow = false;
	/** The actor editor */
	private IResourceChangeEditor mActorEditor;

	/** If only one actor shall be able to be created simultaneously */
	private boolean mOnlyOneActor;
	/** Actor definition, only set if only one actor */
	private ActorDef mActorDef = null;
}

package com.spiddekauga.voider.scene;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.spiddekauga.utils.Collections;
import com.spiddekauga.utils.Command;
import com.spiddekauga.utils.Invoker;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.editor.HitWrapper;
import com.spiddekauga.voider.editor.IResourceChangeEditor;
import com.spiddekauga.voider.editor.VectorBrush;
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
import com.spiddekauga.voider.game.actors.BulletActorDef;
import com.spiddekauga.voider.resources.IResource;
import com.spiddekauga.voider.resources.IResourceCorner;
import com.spiddekauga.voider.utils.Geometry;
import com.spiddekauga.voider.utils.Geometry.Intersections;
import com.spiddekauga.voider.utils.Geometry.PolygonComplexException;
import com.spiddekauga.voider.utils.Geometry.PolygonCornersTooCloseException;
import com.spiddekauga.voider.utils.Messages;
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
				setDrawing(true);

				Pools.vector2.free(localPos);

			}
			break;


		case ADJUST_MOVE_CORNER:
			if (mHitBody != null) {
				if (mHitBody.getUserData() instanceof HitWrapper) {
					mCornerIndexCurrent = mSelectedActor.getCornerIndex(mHitBody.getPosition());
					mDragOrigin.set(mSelectedActor.getDef().getVisualVars().getCornerPosition(mCornerIndexCurrent));
					setDrawing(true);
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
								SceneSwitcher.showErrorMessage(Messages.Error.POLYGON_COMPLEX_REMOVE);
								mInvoker.undo();
								mInvoker.clearRedo();
							} catch (PolygonCornersTooCloseException e) {
								Gdx.app.error("DrawActorTool", "PolygonCornerTooClose, should not happen when removing corner...");
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
				setDrawing(true);

				// Create corner here
				appendCorner(true);
			}
			break;


		case DRAW_ERASE:
			if (!mChangedActorThisEvent) {
				testPick(0.00001f);
				if (hitSelectedActor()) {
					mDrawEraseBrush = new VectorBrush(true);
				} else {
					mDrawEraseBrush = new VectorBrush(false);
				}
				mActorEditor.onResourceAdded(mDrawEraseBrush);
				mDrawEraseBrush.addCorner(mTouchCurrent);
				mDragOrigin.set(mTouchCurrent);
				setDrawing(true);
			}
			break;


		case MOVE:
			if (mHitBody != null && mHitBody.getUserData() == mSelectedActor) {
				mDragOrigin.set(mHitBody.getPosition());
				mMovingShape = true;
				setDrawing(true);
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
				setDrawing(true);
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
				if (haveMovedEnoughToAddAnotherCorner()) {
					appendCorner(true);
				}
			}
			break;


		case DRAW_ERASE:
			if (!mChangedActorThisEvent) {
				if (haveMovedEnoughToAddAnotherCorner()) {
					mDrawEraseBrush.addCorner(mTouchCurrent);
					mDragOrigin.set(mTouchCurrent);
				}
			}
			break;


		case MOVE:
			if (mSelectedActor != null && mMovingShape) {
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
		if (mSelectedActor == null) {
			return;
		}


		switch (mState) {
		case ADJUST_ADD_CORNER:
			if (mCornerIndexCurrent != -1) {
				// Remove temporary corner
				Vector2 removedCorner = mSelectedActor.getDef().getVisualVars().removeCorner(mCornerIndexCurrent);

				// Add the corner via invoker instead
				mInvoker.execute(new CActorDefFixCustomFixtures(mSelectedActor.getDef(), false));
				mInvoker.execute(new CResourceCornerAdd(mSelectedActor.getDef().getVisualVars(), removedCorner, mCornerIndexCurrent, mActorEditor), true);
				try {
					mInvoker.execute(new CActorDefFixCustomFixtures(mSelectedActor.getDef(), true), true);
				} catch (PolygonComplexException e) {
					SceneSwitcher.showErrorMessage(Messages.Error.POLYGON_COMPLEX_ADD);
					handleBadCornerPosition(null);
				} catch (PolygonCornersTooCloseException e) {
					Gdx.app.error("DrawActorTool", "PolygonCornersTooClose! Should never happen!");
					handleBadCornerPosition(null);
				}

				Pools.vector2.free(removedCorner);
			}
			break;


		case ADJUST_MOVE_CORNER:
			if (mCornerIndexCurrent != -1) {
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
					SceneSwitcher.showErrorMessage(Messages.Error.POLYGON_COMPLEX_MOVE);
					handleBadCornerPosition(null);
				} catch (PolygonCornersTooCloseException e) {
					Gdx.app.error("DrawActorTool", "PolygonCornersTooClose! Should never happen!");
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
				// Add a final corner when released
				appendCorner(true);

				mInvoker.execute(new CResourceCornerRemoveExcessive(mSelectedActor.getDef().getVisualVars()), true);

				try {
					mInvoker.execute(new CActorDefFixCustomFixtures(mSelectedActor.getDef(), true), true);
				} catch (PolygonComplexException e) {
					SceneSwitcher.showErrorMessage(Messages.Error.POLYGON_COMPLEX_DRAW_APPEND);
					handleBadCornerPosition(null);
				} catch (PolygonCornersTooCloseException e) {
					Gdx.app.error("DrawActorTool", "PolygonCornersTooClose! Should never happen!");
					handleBadCornerPosition(null);
				}
			}
			break;


		case DRAW_ERASE:
			if (!mChangedActorThisEvent) {
				mActorEditor.onResourceRemoved(mDrawEraseBrush);
				mDrawEraseBrush.addCorner(mTouchCurrent);

				Command removeExcessiveCorners = new CResourceCornerRemoveExcessive(mDrawEraseBrush);
				removeExcessiveCorners.execute();

				// Check for intersections in draw erase brush...
				if (Geometry.intersectionExists(mDrawEraseBrush.getCorners()) != Intersections.INTERSECTS) {
					ArrayList<BrushActorIntersection> intersections = getBrushActorIntersections();

					// If odd, remove added last intersection...
					if (intersections.size() % 2 == 1) {
						if (intersections.get(0).brushIndex > intersections.get(intersections.size() - 1).brushIndex) {
							intersections.remove(0);
						} else {
							intersections.remove(intersections.size() - 1);
						}

					}

					mInvoker.execute(new CActorDefFixCustomFixtures(mSelectedActor.getDef(), false));

					// Change shape of actor to use the brush corners.
					IResourceCorner actorCorner = mSelectedActor.getDef().getVisualVars();
					while (intersections.size() >= 2) {
						boolean intersectionSame = false;
						int actorIndexHigh;
						int actorIndexLow;

						// Between
						if (intersections.get(0).actorIndex != intersections.get(1).actorIndex) {
							actorIndexLow = intersections.get(0).actorIndex;
							actorIndexHigh = intersections.get(1).actorIndex;
						}
						// Same
						else {
							intersectionSame = true;
							actorIndexLow = intersections.get(0).actorIndex;
							actorIndexHigh = intersections.get(1).actorIndex;
						}

						// Check if we shall remove corners between or wrapped...
						boolean removeBetween = false;
						boolean removeWrapped = false;
						if (!intersectionSame) {
							int fromIndex = 0;
							int toIndex = 1;
							removeBetween = isShortestBetweenIndices(actorCorner.getCorners(), intersections.get(fromIndex), intersections.get(toIndex));
							removeWrapped = !removeBetween;
						}


						int addIndex;
						/** @todo save the number of corners we removed to update index values */
						// No corners to be removed, all new are between two corners
						if (intersectionSame) {
							addIndex = actorIndexLow + 1;
						}
						// Remove actor corners between intersections
						else if (removeBetween) {
							addIndex = actorIndexLow + 1;
							int cornersToRemove = actorIndexHigh - actorIndexLow;
							for (int i = 0; i < cornersToRemove; ++i) {
								mInvoker.execute(new CResourceCornerRemove(actorCorner, addIndex, mActorEditor), true);
							}
						}
						// Remove actor corners between last and first intersection (wrapped)
						else {
							// Remove at the back
							int cornersToRemoveAtBack = actorCorner.getCornerCount() - actorIndexHigh - 1;
							for (int i = 0; i < cornersToRemoveAtBack; ++i) {
								mInvoker.execute(new CResourceCornerRemove(actorCorner, actorCorner.getCornerCount() - 1, mActorEditor), true);
							}

							// Remove at the front
							for (int i = 0; i <= actorIndexLow; ++i) {
								mInvoker.execute(new CResourceCornerRemove(actorCorner, 0, mActorEditor), true);
							}

							// Fix actorIndices (because we removed some at the front)
							for (int i = 2; i < intersections.size(); ++i) {
								intersections.get(i).actorIndex -= actorIndexLow + 1;
							}

							addIndex = actorCorner.getCornerCount();
						}


						// Calculate begin and end intersections
						BrushActorIntersection lowIntersection;
						BrushActorIntersection highIntersection;
						if (removeBetween) {
							lowIntersection = intersections.get(0);
							highIntersection = intersections.get(1);
						} else if (removeWrapped) {
							lowIntersection = intersections.get(1);
							highIntersection = intersections.get(0);
						}
						// same intersection, i.e. none was removed. Calculate which
						// is closest to the lowest actor index
						else {
							Vector2 lowestActorIndexCorner = actorCorner.getCornerPosition(actorIndexLow);
							Vector2 intersection0Diff = getLocalPosition(intersections.get(0).intersection);
							intersection0Diff.sub(lowestActorIndexCorner);
							Vector2 intersection1Diff = getLocalPosition(intersections.get(1).intersection);
							intersection1Diff.sub(lowestActorIndexCorner);

							if (intersection0Diff.len2() < intersection1Diff.len2()) {
								lowIntersection = intersections.get(0);
								highIntersection = intersections.get(1);
							} else {
								lowIntersection = intersections.get(1);
								highIntersection = intersections.get(0);
							}

							Pools.vector2.freeAll(intersection0Diff, intersection1Diff);
						}

						// Add end intersection
						Vector2 localPos = getLocalPosition(highIntersection.intersection);
						mInvoker.execute(new CResourceCornerAdd(actorCorner, localPos, addIndex, mActorEditor), true);
						Pools.vector2.free(localPos);

						// Add brush corners
						if (highIntersection.brushIndex < lowIntersection.brushIndex) {
							int fromIndex = highIntersection.brushIndex + 1;
							int toIndex = lowIntersection.brushIndex;
							for (int i = fromIndex; i < toIndex; ++i) {
								localPos = getLocalPosition(mDrawEraseBrush.getCornerPosition(i));
								mInvoker.execute(new CResourceCornerAdd(actorCorner, localPos, addIndex, mActorEditor), true);
								Pools.vector2.free(localPos);
							}
						} else {
							int fromIndex = highIntersection.brushIndex;
							int toIndex = lowIntersection.brushIndex + 1;
							for (int i = fromIndex; i > toIndex; --i) {
								localPos = getLocalPosition(mDrawEraseBrush.getCornerPosition(i));
								mInvoker.execute(new CResourceCornerAdd(actorCorner, localPos, addIndex, mActorEditor), true);
								Pools.vector2.free(localPos);
							}
						}

						// Add begin intersection
						localPos = getLocalPosition(lowIntersection.intersection);
						mInvoker.execute(new CResourceCornerAdd(actorCorner, localPos, addIndex, mActorEditor), true);
						Pools.vector2.free(localPos);

						intersections.remove(1).dispose();
						intersections.remove(0).dispose();

						// Correct other intersections after we added the corners...
						if (intersections.size() >= 2) {
							ArrayList<Vector2> drawEraseCorners = mDrawEraseBrush.getCorners();
							for (int intersectionIndex = 0; intersectionIndex < 2; ++intersectionIndex) {
								BrushActorIntersection currentIntersection = intersections.get(intersectionIndex);

								Vector2 brushBegin = getLocalPosition(drawEraseCorners.get(currentIntersection.brushIndex));
								int nextBrushIndex = Collections.nextIndex(drawEraseCorners, currentIntersection.brushIndex);
								Vector2 brushEnd = getLocalPosition(drawEraseCorners.get(nextBrushIndex));
								int actorIndex = 0;
								boolean foundIntersection = false;
								boolean looped = false;
								while (!looped && !foundIntersection) {
									int nextActorIndex = Collections.nextIndex(actorCorner.getCorners(), actorIndex);
									Vector2 actorBegin = actorCorner.getCornerPosition(actorIndex);
									Vector2 actorEnd = actorCorner.getCornerPosition(nextActorIndex);

									if (Geometry.linesIntersect(brushBegin, brushEnd, actorBegin, actorEnd)) {
										foundIntersection = true;
									} else {
										actorIndex = nextActorIndex;
										if (nextActorIndex == 0) {
											looped = true;
										}
									}
								}

								Pools.vector2.freeAll(brushBegin, brushEnd);

								if (foundIntersection) {
									currentIntersection.actorIndex = actorIndex;
								} else {
									Gdx.app.error("DrawActorTool", "Could not find the intersection again!");
									throw new GdxRuntimeException("DrawActorTool: Could not find the intersection again!");
								}
							}
						}
					}

					Pools.arrayList.free(intersections);
					intersections = null;

					try {
						mInvoker.execute(new CResourceCornerRemoveExcessive(mSelectedActor.getDef().getVisualVars()), true);
						mInvoker.execute(new CActorDefFixCustomFixtures(mSelectedActor.getDef(), true), true);
					} catch (PolygonComplexException e) {
						SceneSwitcher.showErrorMessage(Messages.Error.POLYGON_COMPLEX_DRAW_ERASE);
						handleBadCornerPosition(null);
					} catch (PolygonCornersTooCloseException e) {
						Gdx.app.error("DrawActorTool", "PolygonCornersTooClose! Should never happen!");
						handleBadCornerPosition(null);
					}

				} else {
					SceneSwitcher.showErrorMessage(Messages.Error.POLYGON_DRAW_ERASE_LINE_COMPLEX);
				}

				mDrawEraseBrush.dispose();
				mDrawEraseBrush = null;
			}
			break;


		case MOVE:
			// Set the new position of the actor
			if (mMovingShape) {
				// Reset actor to original position
				mSelectedActor.setPosition(mDragOrigin);

				Vector2 newPos = getNewMovePosition();
				mInvoker.execute(new CResourceMove(mSelectedActor, newPos, mActorEditor), mChangedActorThisEvent);
				Pools.vector2.free(newPos);

				mMovingShape = false;
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
		setDrawing(false);
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
		return mHitBody != null && mHitBody.getUserData() instanceof Actor && mHitBody.getUserData() != mSelectedActor && !mOnlyOneActor;
	}


	/**
	 * Appends a temporary corner in the current position
	 * @param chained if the command shall be chained or not.
	 */
	private void appendCorner(boolean chained) {
		if (mSelectedActor != null) {
			Vector2 localPos = getLocalPosition(mTouchCurrent);

			mInvoker.execute(new CResourceCornerAdd(mSelectedActor.getDef().getVisualVars(), localPos, mActorEditor), chained);
			mDragOrigin.set(mTouchCurrent);
			mCornerIndexCurrent = mSelectedActor.getDef().getVisualVars().getCornerCount() - 1;

			Pools.vector2.free(localPos);
		}
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
		// Because a corner is added both on down and up we need to undo twice.
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
			int nextIndex = Collections.nextIndex(corners, i);
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
		mInvoker.undo(false);
		mInvoker.clearRedo();
		mCornerIndexCurrent = -1;
	}

	/**
	 * Get local position for the selected actor from the specified world position
	 * @param worldPos world position
	 * @return Local position of the currently selected actor, copy of worldPos if no actor was selected.
	 * Don't forget to free the localPos using Pools.vector2.free(localPos)
	 */
	private Vector2 getLocalPosition(Vector2 worldPos) {
		Vector2 localPos = Pools.vector2.obtain();
		localPos.set(worldPos);

		if (mSelectedActor != null) {
			localPos.sub(mSelectedActor.getPosition()).sub(mSelectedActor.getDef().getVisualVars().getCenterOffset());
		}

		return localPos;
	}

	/**
	 * Get world position from the selected actor
	 * @param localPos local position
	 * @return world position of the currently selected actor, copy of localPos if no actor was selected.
	 * Don't forget to free the worldPos using Pools.vector2.free(worldPos);
	 */
	private Vector2 getWorldPosition(Vector2 localPos) {
		Vector2 worldPos = Pools.vector2.obtain();
		worldPos.set(localPos);

		if (mSelectedActor != null) {
			worldPos.add(mSelectedActor.getPosition()).add(mSelectedActor.getDef().getVisualVars().getCenterOffset());
		}

		return worldPos;
	}

	/**
	 * Tests whether the pointer have moved enough to add another corner
	 * @return true if we shall add another corner.
	 */
	private boolean haveMovedEnoughToAddAnotherCorner() {
		boolean movedEnough = false;

		float drawNewCornerMinDistSq = Config.Editor.Actor.Visual.DRAW_NEW_CORNER_MIN_DIST_SQ;
		if (mActorDef instanceof BulletActorDef) {
			drawNewCornerMinDistSq = Config.Editor.Bullet.Visual.DRAW_NEW_CORNER_MIN_DIST_SQ;
		}

		// If has drawn more than minimum distance, add another corner here
		Vector2 diffVector = Pools.vector2.obtain();
		diffVector.set(mTouchCurrent).sub(mDragOrigin);
		if (diffVector.len2() >= drawNewCornerMinDistSq) {
			movedEnough = true;
		}
		Pools.vector2.free(diffVector);

		return movedEnough;
	}

	/**
	 * Checks for intersections between selected actor and draw erase brush
	 * @return array list with all intersections including indices for both
	 * actor and brush.
	 */
	private ArrayList<BrushActorIntersection> getBrushActorIntersections() {
		@SuppressWarnings("unchecked")
		ArrayList<BrushActorIntersection> intersections = Pools.arrayList.obtain();

		ArrayList<Vector2> actorCorners = mSelectedActor.getDef().getVisualVars().getCorners();
		ArrayList<Vector2> brushCorners = mDrawEraseBrush.getCorners();
		for (int actorIndex = 0; actorIndex < actorCorners.size(); ++actorIndex) {
			Vector2 actorLineStart = getWorldPosition(actorCorners.get(actorIndex));
			Vector2 actorLineEnd = getWorldPosition(actorCorners.get(Collections.nextIndex(actorCorners, actorIndex)));

			// Don't loop the brush...
			for (int brushIndex = 0; brushIndex < brushCorners.size() - 1; ++brushIndex) {
				Vector2 brushLineStart = brushCorners.get(brushIndex);
				Vector2 brushLineEnd = brushCorners.get(Collections.nextIndex(brushCorners, brushIndex));

				// Save intersection
				if (Geometry.linesIntersect(actorLineStart, actorLineEnd, brushLineStart, brushLineEnd)) {
					Vector2 intersection = Geometry.getLineLineIntersection(actorLineStart, actorLineEnd, brushLineStart, brushLineEnd);

					intersections.add(new BrushActorIntersection(intersection, actorIndex, brushIndex));
				}
			}

			Pools.vector2.freeAll(actorLineStart, actorLineEnd);
		}


		return intersections;
	}

	/**
	 * Calculate if shortest path between to actorIndices are between the indices and not wrapped.
	 * @param vertices current vertices of the actor
	 * @param fromIntersection the intersection to calculate from
	 * @param toIntersection the intersection to calculate to
	 * @return true if the shortest length between two actorIndices are between the indices, false if wrapped
	 */
	private boolean isShortestBetweenIndices(ArrayList<Vector2> vertices, BrushActorIntersection fromIntersection, BrushActorIntersection toIntersection) {
		// BETWEEN
		float betweenLengthSq = 0;
		// vertices between
		if (fromIntersection.actorIndex != toIntersection.actorIndex) {
			// Calculate from before intersection to first between index
			Vector2 localPos = getLocalPosition(fromIntersection.intersection);
			betweenLengthSq += localPos.dst2(vertices.get(fromIntersection.actorIndex + 1));
			Pools.vector2.free(localPos);

			// Calculate between
			for (int i = fromIntersection.actorIndex + 1; i < toIntersection.actorIndex; ++i) {
				betweenLengthSq += vertices.get(i).dst2(vertices.get(i+1));
			}

			// Calculate from last between index to after intersection
			localPos = getLocalPosition(toIntersection.intersection);
			betweenLengthSq += vertices.get(toIntersection.actorIndex).dst2(localPos);
			Pools.vector2.free(localPos);
		}
		// No vertices between
		else {
			return true;
		}


		// WRAPPED
		float wrappedLengthSq = 0;
		// Calculate from after intersection to first after index
		Vector2 localPos = getLocalPosition(toIntersection.intersection);
		int nextIndex = Collections.nextIndex(vertices,toIntersection.actorIndex);
		wrappedLengthSq += localPos.dst2(vertices.get(nextIndex));
		Pools.vector2.free(localPos);

		// Calculate wrapped indices
		int i = nextIndex;
		while (i != fromIntersection.actorIndex && wrappedLengthSq <= betweenLengthSq) {
			nextIndex = Collections.nextIndex(vertices, i);
			wrappedLengthSq += vertices.get(i).dst2(vertices.get(nextIndex));

			i = nextIndex;
		}

		return betweenLengthSq <= wrappedLengthSq;
	}

	/**
	 * Wrapper class for intersection, actor corner index, brush index.
	 */
	private class BrushActorIntersection implements Disposable {
		/**
		 * Initializes with initial parameters
		 * @param intersection where the intersection is
		 * @param actorIndex index of the intersection in the actor
		 * @param brushIndex index of the intersection in the brush
		 */
		BrushActorIntersection(Vector2 intersection, int actorIndex, int brushIndex) {
			this.intersection = intersection;
			this.actorIndex = actorIndex;
			this.brushIndex = brushIndex;
		}

		@Override
		public void dispose() {
			Pools.vector2.free(intersection);
		}

		/** Intersection point */
		Vector2 intersection;
		/** Actor index */
		int actorIndex;
		/** Brush index */
		int brushIndex;
	}

	/** Invoker used for undoing/redoing commands */
	protected Invoker mInvoker = null;
	/** Current state of the tool */
	protected States mState = States.DRAW_APPEND;

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
	/** Vector brush for draw/erase */
	private VectorBrush mDrawEraseBrush = null;
	/** True when moving the shape */
	private boolean mMovingShape = false;
}

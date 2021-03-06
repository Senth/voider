package com.spiddekauga.voider.editor.tools;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.spiddekauga.utils.Collections;
import com.spiddekauga.utils.commands.Command;
import com.spiddekauga.utils.scene.ui.NotificationShower.NotificationTypes;
import com.spiddekauga.voider.editor.IResourceChangeEditor;
import com.spiddekauga.voider.editor.brushes.VectorBrush;
import com.spiddekauga.voider.editor.brushes.VectorBrush.VectorBrushModes;
import com.spiddekauga.voider.editor.commands.CActorDefFixCustomFixtures;
import com.spiddekauga.voider.editor.commands.CResourceCornerAdd;
import com.spiddekauga.voider.editor.commands.CResourceCornerRemove;
import com.spiddekauga.voider.editor.commands.CResourceCornerRemoveExcessive;
import com.spiddekauga.voider.game.actors.Actor;
import com.spiddekauga.voider.resources.IResource;
import com.spiddekauga.voider.resources.IResourceCorner;
import com.spiddekauga.voider.utils.Geometry;
import com.spiddekauga.voider.utils.Geometry.Intersections;
import com.spiddekauga.voider.utils.Geometry.PolygonAreaTooSmallException;
import com.spiddekauga.voider.utils.Geometry.PolygonComplexException;
import com.spiddekauga.voider.utils.Messages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

/**
 * Tool for adding or removing from a shape
 */
public class DrawEraseTool extends ActorTool {
private Actor mHitActor = null;
private VectorBrush mDrawEraseBrush = null;
private Vector2 mDragOrigin = new Vector2();
private QueryCallback mCallback = new QueryCallback() {
	@Override
	public boolean reportFixture(Fixture fixture) {
		Body body = fixture.getBody();
		// Hit an actor
		if (body.getUserData() != null && body.getUserData().getClass() == mActorType) {
			if (mSelection.isSelected((IResource) body.getUserData())) {
				mHitActor = (Actor) body.getUserData();
				return false;
			}
		}
		return true;
	}
};


/**
 * @param editor editor this tool is bound to
 * @param selection which enemies are selected
 * @param actorType the type of actor to draw
 */
public DrawEraseTool(IResourceChangeEditor editor, ISelection selection, Class<? extends Actor> actorType) {
	super(editor, selection, actorType);
}

@Override
protected boolean dragged() {
	if (mDrawEraseBrush != null) {
		if (haveMovedEnoughToAddAnotherCorner(mDragOrigin)) {
			mDrawEraseBrush.addCorner(mTouchCurrent);
			mDragOrigin.set(mTouchCurrent);
		}
	}
	return false;
}

@Override
protected boolean up(int button) {
	if (mDrawEraseBrush != null) {
		mDrawEraseBrush.addCorner(mTouchCurrent);

		float distMinSq = getVisualConfig().getDrawNewCornerDistMinSq();
		float angleMin = getVisualConfig().getDrawCornerAngleMin();
		Command removeExcessiveCorners = new CResourceCornerRemoveExcessive(mDrawEraseBrush, distMinSq, angleMin);
		removeExcessiveCorners.execute();

		// Check for intersections in draw erase brush...
		if (Geometry.intersectionExists(mDrawEraseBrush.getCorners()) != Intersections.INTERSECTS) {
			List<BrushActorIntersection> intersections = getBrushActorIntersections();

			try {
				updateShapesForAllIntersectionActors(intersections);
			} catch (PolygonComplexException e) {
				mNotification.show(NotificationTypes.ERROR, Messages.Error.POLYGON_COMPLEX_DRAW_APPEND);
				handleBadCornerPosition(null);
			} catch (PolygonAreaTooSmallException e) {
				mNotification.show(NotificationTypes.ERROR, Messages.Error.POLYGON_AREA_TOO_SMALL);
				handleBadCornerPosition(null);
			}
		}

		mEditor.onResourceRemoved(mDrawEraseBrush);
		mDrawEraseBrush = null;
		mHitActor = null;
		setDrawing(false);
	}
	return false;
}

@Override
protected boolean down(int button) {
	// Skip if selected resource was changed
	if (mSelection.isSelectionChangedDuringDown()) {
		return false;
	}

	if (!mSelection.isEmpty()) {
		testPickPoint(mCallback);
		// Hit a selected actor
		if (mHitActor != null) {
			mDrawEraseBrush = new VectorBrush(VectorBrushModes.ADD);
		} else {
			mDrawEraseBrush = new VectorBrush(VectorBrushModes.ERASE);
		}
		mEditor.onResourceAdded(mDrawEraseBrush, true);
		mDrawEraseBrush.addCorner(mTouchCurrent);
		mDragOrigin.set(mTouchCurrent);
		setDrawing(true);
	}
	return false;
}

/**
 * Checks for intersections between selected actor and draw erase brush
 * @return array list with all intersections including indices for both actor and brush.
 */
@SuppressWarnings("unchecked")
private List<BrushActorIntersection> getBrushActorIntersections() {
	List<BrushActorIntersection> intersections = new ArrayList<>();
	List<Vector2> brushCorners = mDrawEraseBrush.getCorners();


	// Which actors should we iterate through?
	List<Actor> selectedActors;

	// Only the actor we hit
	if (mHitActor != null) {
		selectedActors = new ArrayList<>();
		selectedActors.add(mHitActor);
	}
	// Hit outside of any selected actor, iterate through all
	else {
		selectedActors = (List<Actor>) mSelection.getSelectedResourcesOfType(mActorType);
	}


	// Iterate through all actors
	List<Vector2> actorCorners = new ArrayList<>();
	for (Actor selectedActor : selectedActors) {
		// Convert to world positions
		for (Vector2 corner : selectedActor.getDef().getShape().getCorners()) {
			actorCorners.add(getWorldPosition(corner, selectedActor));
		}


		// Don't loop the brush
		for (int brushIndex = 0; brushIndex < brushCorners.size() - 1; ++brushIndex) {
			Vector2 brushLineStart = brushCorners.get(brushIndex);
			Vector2 brushLineEnd = brushCorners.get(Collections.nextIndex(brushCorners, brushIndex));

			for (int actorIndex = 0; actorIndex < actorCorners.size(); ++actorIndex) {
				Vector2 actorLineStart = actorCorners.get(actorIndex);
				Vector2 actorLineEnd = actorCorners.get(Collections.nextIndex(actorCorners, actorIndex));

				// Save intersection
				if (Geometry.linesIntersect(actorLineStart, actorLineEnd, brushLineStart, brushLineEnd)) {
					Vector2 intersection = Geometry.getLineLineIntersection(actorLineStart, actorLineEnd, brushLineStart, brushLineEnd);

					intersections.add(new BrushActorIntersection(selectedActor, intersection, actorIndex, brushIndex));
				}
			}
		}
		actorCorners.clear();
	}

	return intersections;
}

/**
 * Change shape for all actors inside the intersections
 * @param intersections all intersections between actors and the brush
 */
private void updateShapesForAllIntersectionActors(List<BrushActorIntersection> intersections) {
	HashMap<Actor, List<BrushActorIntersection>> splitIntersections = splitIntersectionsByActor(intersections);

	for (Entry<Actor, List<BrushActorIntersection>> entry : splitIntersections.entrySet()) {
		updateShapeForActor(entry.getValue());
	}
}

/**
 * Handles a bad corner position
 * @param message the message to print
 */
private void handleBadCornerPosition(String message) {
	mInvoker.undo(false);
	mInvoker.clearRedo();
}

/**
 * Split intersections by actor.
 * @param intersections all intersections
 * @return hash map of array of intersections. Where each inner array has intersections for the same
 * actor. Don't forget to free both the hash map and all inner arrays
 */
private HashMap<Actor, List<BrushActorIntersection>> splitIntersectionsByActor(List<BrushActorIntersection> intersections) {
	HashMap<Actor, List<BrushActorIntersection>> splitIntersections = new HashMap<>();

	for (BrushActorIntersection intersection : intersections) {
		List<BrushActorIntersection> actorIntersections = splitIntersections.get(intersection.actor);

		if (actorIntersections == null) {
			actorIntersections = new ArrayList<>();
			splitIntersections.put(intersection.actor, actorIntersections);
		}

		actorIntersections.add(intersection);
	}

	return splitIntersections;
}

/**
 * Update shape for actor
 * @param intersections should only contain one type of actor
 */
private void updateShapeForActor(List<BrushActorIntersection> intersections) {
	Actor actor = intersections.get(0).actor;

	mInvoker.execute(new CActorDefFixCustomFixtures(actor.getDef(), false));

	// Change shape of actor to use the brush corners.
	IResourceCorner actorCorners = actor.getDef().getShape();
	while (intersections.size() >= 2) {
		boolean intersectionSame = false;

		// Same actor corner index
		if (intersections.get(0).actorIndex == intersections.get(1).actorIndex) {
			intersectionSame = true;
		}

		// Set high/low actor index
		// Calculate begin and end intersections
		BrushActorIntersection lowIntersection;
		BrushActorIntersection highIntersection;
		if (intersections.get(0).actorIndex >= intersections.get(1).actorIndex) {
			lowIntersection = intersections.get(1);
			highIntersection = intersections.get(0);
		} else {
			lowIntersection = intersections.get(0);
			highIntersection = intersections.get(1);
		}

		// Check if we shall remove corners between or wrapped...
		boolean removeBetween = false;
		boolean removeWrapped = false;
		if (!intersectionSame) {
			removeBetween = isShortestBetweenIndices(actorCorners.getCorners(), lowIntersection, highIntersection);
			removeWrapped = !removeBetween;
		}


		// Remove actor corners
		int addIndex;
		// No corners to be removed, all new are between two corners
		if (intersectionSame) {
			addIndex = lowIntersection.actorIndex + 1;
		}
		// Remove actor corners between intersections
		else if (removeBetween) {
			addIndex = lowIntersection.actorIndex + 1;
			int cornersToRemove = highIntersection.actorIndex - lowIntersection.actorIndex;
			for (int i = 0; i < cornersToRemove; ++i) {
				mInvoker.execute(new CResourceCornerRemove(actorCorners, addIndex, mEditor), true);
			}
		}
		// Remove actor corners between last and first intersection (wrapped)
		else {
			// Remove at the back
			int cornersToRemoveAtBack = actorCorners.getCornerCount() - highIntersection.actorIndex - 1;
			for (int i = 0; i < cornersToRemoveAtBack; ++i) {
				mInvoker.execute(new CResourceCornerRemove(actorCorners, actorCorners.getCornerCount() - 1, mEditor), true);
			}

			// Remove at the front
			for (int i = 0; i <= lowIntersection.actorIndex; ++i) {
				mInvoker.execute(new CResourceCornerRemove(actorCorners, 0, mEditor), true);
			}

			// Fix actorIndices (because we removed some at the front)
			for (int i = 2; i < intersections.size(); ++i) {
				intersections.get(i).actorIndex -= lowIntersection.actorIndex + 1;
			}

			addIndex = actorCorners.getCornerCount();
		}

		// Switch high/low for wrapped cases
		if (removeWrapped) {
			BrushActorIntersection tempIntersection = lowIntersection;
			lowIntersection = highIntersection;
			highIntersection = tempIntersection;
		}
		// same intersection, i.e. none was removed. Calculate which
		// is closest to the lowest actor index
		else if (!removeBetween) {
			Vector2 lowestActorIndexCorner = actorCorners.getCornerPosition(lowIntersection.actorIndex);
			Vector2 intersection0Diff = getLocalPosition(intersections.get(0).intersection, actor);
			intersection0Diff.sub(lowestActorIndexCorner);
			Vector2 intersection1Diff = getLocalPosition(intersections.get(1).intersection, actor);
			intersection1Diff.sub(lowestActorIndexCorner);

			if (intersection0Diff.len2() < intersection1Diff.len2()) {
				lowIntersection = intersections.get(0);
				highIntersection = intersections.get(1);
			} else {
				lowIntersection = intersections.get(1);
				highIntersection = intersections.get(0);
			}
		}

		// Add new corners
		// Add end intersection
		Vector2 localPos = getLocalPosition(highIntersection.intersection, actor);
		mInvoker.execute(new CResourceCornerAdd(actorCorners, localPos, addIndex, mEditor), true);

		// Add brush corners
		if (highIntersection.brushIndex < lowIntersection.brushIndex) {
			int fromIndex = highIntersection.brushIndex + 1;
			int toIndex = lowIntersection.brushIndex;
			for (int i = fromIndex; i < toIndex; ++i) {
				localPos = getLocalPosition(mDrawEraseBrush.getCornerPosition(i), actor);
				mInvoker.execute(new CResourceCornerAdd(actorCorners, localPos, addIndex, mEditor), true);
			}
		} else {
			int fromIndex = highIntersection.brushIndex;
			int toIndex = lowIntersection.brushIndex + 1;
			for (int i = fromIndex; i > toIndex; --i) {
				localPos = getLocalPosition(mDrawEraseBrush.getCornerPosition(i), actor);
				mInvoker.execute(new CResourceCornerAdd(actorCorners, localPos, addIndex, mEditor), true);
			}
		}

		// Add begin intersection
		localPos = getLocalPosition(lowIntersection.intersection, actor);
		mInvoker.execute(new CResourceCornerAdd(actorCorners, localPos, addIndex, mEditor), true);

		intersections.remove(1);
		intersections.remove(0);


		// Correct other intersections after we added the corners...
		if (intersections.size() >= 2) {
			List<Vector2> drawEraseCorners = mDrawEraseBrush.getCorners();
			for (int intersectionIndex = 0; intersectionIndex < 2; ++intersectionIndex) {
				BrushActorIntersection currentIntersection = intersections.get(intersectionIndex);

				Vector2 brushBegin = getLocalPosition(drawEraseCorners.get(currentIntersection.brushIndex), actor);
				int nextBrushIndex = Collections.nextIndex(drawEraseCorners, currentIntersection.brushIndex);
				Vector2 brushEnd = getLocalPosition(drawEraseCorners.get(nextBrushIndex), actor);
				int actorIndex = 0;
				boolean foundIntersection = false;
				boolean looped = false;
				while (!looped && !foundIntersection) {
					int nextActorIndex = Collections.nextIndex(actorCorners.getCorners(), actorIndex);
					Vector2 actorBegin = actorCorners.getCornerPosition(actorIndex);
					Vector2 actorEnd = actorCorners.getCornerPosition(nextActorIndex);

					if (Geometry.linesIntersect(brushBegin, brushEnd, actorBegin, actorEnd)) {
						foundIntersection = true;
					} else {
						actorIndex = nextActorIndex;
						if (nextActorIndex == 0) {
							looped = true;
						}
					}
				}

				if (foundIntersection) {
					currentIntersection.actorIndex = actorIndex;
				}
				// We probably removed the intersections, remove these rest
				else {
					intersections.clear();
					break;
				}
			}
		}
	}


	// Remove all excess corners
	float distMinSq = getVisualConfig().getDrawNewCornerDistMinSq();
	float angleMin = getVisualConfig().getDrawCornerAngleMin();
	mInvoker.execute(new CResourceCornerRemoveExcessive(actorCorners, distMinSq, angleMin), true);


	// Create fixtures (and maybe fix area)
	boolean success = true;
	do {
		try {
			mInvoker.execute(new CActorDefFixCustomFixtures(actor.getDef(), true), true);
			success = true;
		} catch (PolygonAreaTooSmallException e) {
			success = false;

			// Fix area, remove corners
			List<Integer> indicesToRemove = Geometry.fixPolygonArea(actorCorners.getCorners(), e.getVertices());

			if (!indicesToRemove.isEmpty()) {
				// Sort so we remove highest index first (otherwise the indices aren't
				// correct)
				java.util.Collections.sort(indicesToRemove);

				for (int i = indicesToRemove.size() - 1; i >= 0; --i) {
					int indexToRemove = indicesToRemove.get(i);
					mInvoker.execute(new CResourceCornerRemove(actorCorners, indexToRemove, mEditor), true);
				}
			}
			// Didn't find any solution, throw again
			else {
				throw e;
			}

		}
	} while (!success);
}

/**
 * Calculate if shortest path between to actorIndices are between the indices and not wrapped.
 * @param vertices current vertices of the actor
 * @param fromIntersection the intersection to calculate from
 * @param toIntersection the intersection to calculate to
 * @return true if the shortest length between two actorIndices are between the indices, false if
 * wrapped
 */
private boolean isShortestBetweenIndices(List<Vector2> vertices, BrushActorIntersection fromIntersection, BrushActorIntersection toIntersection) {
	// BETWEEN
	float betweenLength = 0;
	// vertices between
	if (fromIntersection.actorIndex != toIntersection.actorIndex) {
		// Calculate from before intersection to first between index
		betweenLength += fromIntersection.intersection.dst(vertices.get(fromIntersection.actorIndex + 1));

		// Calculate between
		for (int i = fromIntersection.actorIndex + 1; i < toIntersection.actorIndex; ++i) {
			betweenLength += vertices.get(i).dst(vertices.get(i + 1));
		}

		// Calculate from last between index to after intersection
		betweenLength += vertices.get(toIntersection.actorIndex).dst(toIntersection.intersection);
	}
	// No vertices between
	else {
		return true;
	}


	// WRAPPED
	float wrappedLength = 0;
	// To intersection
	int nextIndex = Collections.nextIndex(vertices, toIntersection.actorIndex);
	wrappedLength += toIntersection.intersection.dst(vertices.get(nextIndex));

	// From intersection
	wrappedLength += fromIntersection.intersection.dst(vertices.get(fromIntersection.actorIndex));

	// Calculate wrapped indices
	int i = nextIndex;
	while (i != fromIntersection.actorIndex && wrappedLength <= betweenLength) {
		nextIndex = Collections.nextIndex(vertices, i);
		wrappedLength += vertices.get(i).dst(vertices.get(nextIndex));

		i = nextIndex;
	}

	return betweenLength <= wrappedLength;
}

/**
 * Wrapper class for intersection, actor corner index, brush index.
 */
private class BrushActorIntersection {
	/** Intersection point */
	Vector2 intersection;
	/** Actor index */
	int actorIndex;
	/** Brush index */
	int brushIndex;
	/** The actor the intersection is in */
	Actor actor;

	/**
	 * Initializes with initial parameters
	 * @param actor the actor the intersion is in
	 * @param intersection where the intersection is
	 * @param actorIndex index of the intersection in the actor
	 * @param brushIndex index of the intersection in the brush
	 */
	BrushActorIntersection(Actor actor, Vector2 intersection, int actorIndex, int brushIndex) {
		this.actor = actor;
		this.intersection = intersection;
		this.actorIndex = actorIndex;
		this.brushIndex = brushIndex;
	}
}
}

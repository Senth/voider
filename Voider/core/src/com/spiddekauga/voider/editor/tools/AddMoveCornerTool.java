package com.spiddekauga.voider.editor.tools;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.spiddekauga.utils.Collections;
import com.spiddekauga.utils.commands.Command;
import com.spiddekauga.utils.scene.ui.NotificationShower.NotificationTypes;
import com.spiddekauga.voider.config.ConfigIni;
import com.spiddekauga.voider.editor.HitWrapper;
import com.spiddekauga.voider.editor.IResourceChangeEditor;
import com.spiddekauga.voider.editor.LevelEditor;
import com.spiddekauga.voider.editor.commands.CActorDefFixCustomFixtures;
import com.spiddekauga.voider.editor.commands.CResourceCornerAdd;
import com.spiddekauga.voider.editor.commands.CResourceCornerMove;
import com.spiddekauga.voider.game.Path;
import com.spiddekauga.voider.game.actors.Actor;
import com.spiddekauga.voider.game.actors.StaticTerrainActor;
import com.spiddekauga.voider.resources.IResource;
import com.spiddekauga.voider.resources.IResourceCorner;
import com.spiddekauga.utils.scene.ui.Scene;
import com.spiddekauga.voider.utils.Geometry;
import com.spiddekauga.voider.utils.Geometry.PolygonComplexException;
import com.spiddekauga.voider.utils.Messages;

import java.util.List;

/**
 * Tool for adding or moving a corner
 */
public class AddMoveCornerTool extends TouchTool implements ISelectionListener {
/** If we're adding a corner, if false we're moving a corner */
private boolean mAddingCorner = false;
/** Body of first corner we hit */
private Body mHitCornerBody = null;
/** First corner we hit */
private IResourceCorner mHitResource = null;
/** Callback for selecting corners */
QueryCallback mCallback = new QueryCallback() {
	@Override
	public boolean reportFixture(Fixture fixture) {
		Object userData = fixture.getBody().getUserData();
		if (userData instanceof HitWrapper) {
			mHitCornerBody = fixture.getBody();

			if (((HitWrapper) userData).resource instanceof IResourceCorner) {
				mHitResource = (IResourceCorner) ((HitWrapper) mHitCornerBody.getUserData()).resource;
			} else {
				Gdx.app.error("AddMoveCorner", "HitWrapper resources was not a corner!");
			}

			return false;
		}
		return true;
	}
};
/** Corner index */
private int mCornerIndexCurrent = -1;
/** Where we started dragging from */
private Vector2 mDragOrigin = new Vector2();

/**
 * @param editor the editor this tool is bound to
 * @param selection all selected resources
 */
public AddMoveCornerTool(IResourceChangeEditor editor, ISelection selection) {
	super(editor, selection);

	if (editor instanceof LevelEditor) {
		mSelectableResourceTypes.add(StaticTerrainActor.class);
		mSelectableResourceTypes.add(Path.class);
	} else {
		mSelectableResourceTypes.add(IResource.class);
	}
}

@Override
protected boolean dragged() {
	if (mCornerIndexCurrent != -1) {
		Vector2 newCornerPos = getLocalPosition(mTouchCurrent, mHitResource);

		mHitResource.moveCorner(mCornerIndexCurrent, newCornerPos);
	}
	return false;
}

@Override
protected boolean up(int button) {
	if (mCornerIndexCurrent != -1) {
		// Add or move?
		Command addOrMoveCommand;
		if (mAddingCorner) {
			Vector2 removedCorner = mHitResource.removeCorner(mCornerIndexCurrent);
			addOrMoveCommand = new CResourceCornerAdd(mHitResource, removedCorner, mCornerIndexCurrent, mEditor);
		} else {
			Vector2 newPos = new Vector2(mHitResource.getCornerPosition(mCornerIndexCurrent));
			mHitResource.moveCorner(mCornerIndexCurrent, mDragOrigin);
			addOrMoveCommand = new CResourceCornerMove(mHitResource, mCornerIndexCurrent, newPos, mEditor);
		}

		// Add corner via invoker instead
		if (mHitResource instanceof Actor) {
			mInvoker.execute(new CActorDefFixCustomFixtures(((Actor) mHitResource).getDef(), false));
			mInvoker.execute(addOrMoveCommand, true);

			try {
				mInvoker.execute(new CActorDefFixCustomFixtures(((Actor) mHitResource).getDef(), true), true);
			} catch (PolygonComplexException e) {
				mNotification.show(NotificationTypes.ERROR, Messages.Error.POLYGON_COMPLEX_ADD);
				handleBadCornerPosition(null);
			}
		} else {
			mInvoker.execute(addOrMoveCommand);
		}

		mCornerIndexCurrent = -1;
	}
	mHitResource = null;
	mHitCornerBody = null;

	return false;
}

@Override
protected boolean down(int button) {
	// Skip if selected resource was changed
	if (mSelection.isSelectionChangedDuringDown()) {
		return false;
	}

	testPickAabb(mCallback);

	// Hit a corner move it
	if (mHitResource != null) {
		mAddingCorner = false;
		mCornerIndexCurrent = mHitResource.getCornerIndex(mHitCornerBody.getPosition());
		mDragOrigin.set(mHitResource.getCornerPosition(mCornerIndexCurrent));
		setDrawing(true);
		return true;
	}
	// Try and see if we can add a corner between two existing corners
	else {
		mAddingCorner = true;
		calculateIndexOfPosBetweenCorners(mTouchCurrent);

		if (mCornerIndexCurrent != -1) {
			Vector2 localPos = getLocalPosition(mTouchCurrent, mHitResource);
			mHitResource.addCorner(localPos, mCornerIndexCurrent);

			setDrawing(true);
		}
	}

	return false;
}

/**
 * Calculates what index a position has between two corners. This method takes into account all
 * actors and will always set it to the closest one
 * @param worldPos the world position
 */
private void calculateIndexOfPosBetweenCorners(Vector2 worldPos) {
	List<IResourceCorner> resources = mSelection.getSelectedResourcesOfType(IResourceCorner.class);
	float bestDist = ConfigIni.getInstance().editor.actor.getVisual((Scene) mEditor).getNewCornerDistMaxSq();

	for (IResourceCorner resource : resources) {
		List<Vector2> corners = resource.getCorners();
		Vector2 localPos = getLocalPosition(worldPos, resource);

		for (int i = 0; i < corners.size(); ++i) {
			int nextIndex = Collections.nextIndex(corners, i);
			float distance = Geometry.distBetweenPointLineSegmentSq(corners.get(i), corners.get(nextIndex), localPos);

			if (distance < bestDist) {
				mCornerIndexCurrent = nextIndex;
				mHitResource = resource;
				bestDist = distance;
			}
		}
	}
}

@Override
public void activate() {
	super.activate();
	mSelection.addListener(this);

	// Draw corners for all resource actors
	for (IResource resource : mSelection.getSelectedResources()) {
		onResourceSelected(resource);
	}
}

@Override
public void deactivate() {
	super.deactivate();
	mSelection.removeListener(this);

	// Remove corners for all resource actors
	for (IResource resource : mSelection.getSelectedResources()) {
		onResourceDeselected(resource);
	}
}

@Override
public void onResourceSelected(IResource resource) {
	if (resource instanceof IResourceCorner) {
		((IResourceCorner) resource).createBodyCorners();
	}
	if (resource instanceof Actor) {
		((Actor) resource).setDrawOnlyOutline(true);
	}
}

@Override
public void onResourceDeselected(IResource resource) {
	if (resource instanceof IResourceCorner) {
		((IResourceCorner) resource).destroyBodyCorners();
	}
	if (resource instanceof Actor) {
		((Actor) resource).setDrawOnlyOutline(false);
	}
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
 * Converts the position to local coordinates depending on the resource type
 * @param worldPos the world position
 * @param resource the resource
 * @return local resource coordinates (can still be world coordinates if the resource shares the
 * same coordinates as the world).
 */
private static Vector2 getLocalPosition(Vector2 worldPos, IResource resource) {
	Vector2 localPos = null;

	// Use local position not world
	if (resource instanceof Actor) {
		localPos = ActorTool.getLocalPosition(worldPos, (Actor) resource);
	}
	// Use world position
	else {
		localPos = new Vector2(worldPos);
	}

	return localPos;
}
}

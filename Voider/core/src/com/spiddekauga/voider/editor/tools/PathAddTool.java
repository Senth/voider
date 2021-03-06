package com.spiddekauga.voider.editor.tools;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.spiddekauga.voider.editor.HitWrapper;
import com.spiddekauga.voider.editor.IResourceChangeEditor;
import com.spiddekauga.voider.editor.commands.CResourceAdd;
import com.spiddekauga.voider.editor.commands.CResourceCornerAdd;
import com.spiddekauga.voider.editor.commands.CResourceCornerMove;
import com.spiddekauga.voider.editor.commands.CSelectionSet;
import com.spiddekauga.voider.game.Path;
import com.spiddekauga.voider.resources.IResource;
import com.spiddekauga.voider.utils.Pool;

import java.util.ArrayList;

/**
 * Tool for adding paths
 */
public class PathAddTool extends TouchTool implements ISelectionListener {
/** True if we added the corner this turn, false if we're moving */
private boolean mAddedCorner = false;
/** All paths and corner's we're currently moving (when moving several) */
private ArrayList<PathCornerIndexWrapper> mMovingCorners = new ArrayList<PathAddTool.PathCornerIndexWrapper>();
/** The hit path */
private Path mHitPath = null;
/** The corner we hit */
private Body mHitCornerBody = null;
/** Callback for checking if we hit a corner */
private QueryCallback mCallback = new QueryCallback() {
	@Override
	public boolean reportFixture(Fixture fixture) {
		Body body = fixture.getBody();

		if (body.getUserData() instanceof HitWrapper) {

			HitWrapper hitWrapper = (HitWrapper) body.getUserData();

			if (hitWrapper.resource instanceof Path) {
				mHitPath = (Path) hitWrapper.resource;
				mHitCornerBody = body;
			}
		}
		return true;
	}
};
/** Pool for path and corner index wrapper */
private Pool<PathCornerIndexWrapper> mPathCornerIndexPool = new Pool<PathAddTool.PathCornerIndexWrapper>(PathCornerIndexWrapper.class, 5, 16);

/**
 * @param editor editor this tool is bound to
 * @param selection all selected resources
 */
public PathAddTool(IResourceChangeEditor editor, ISelection selection) {
	super(editor, selection);

	mSelectableResourceTypes.add(Path.class);
}

@Override
protected boolean dragged() {
	for (PathCornerIndexWrapper movingCorner : mMovingCorners) {
		movingCorner.path.moveCorner(movingCorner.cornerIndex, mTouchCurrent);
	}

	return false;
}

@Override
protected boolean up(int button) {
	if (!mMovingCorners.isEmpty()) {
		// Added corner, no need to move back the corner
		if (mAddedCorner) {
			for (PathCornerIndexWrapper movingCorner : mMovingCorners) {
				movingCorner.path.moveCorner(movingCorner.cornerIndex, mTouchCurrent);
			}
		}
		// Moving corners, revert to old position and then move using command instead
		else {
			boolean chained = false;
			for (PathCornerIndexWrapper movingCorner : mMovingCorners) {
				movingCorner.path.moveCorner(movingCorner.cornerIndex, movingCorner.originPosition);
				mInvoker.execute(new CResourceCornerMove(movingCorner.path, movingCorner.cornerIndex, mTouchCurrent, mEditor), chained);
				chained = true;
			}
		}

		mPathCornerIndexPool.freeAll(mMovingCorners);
		mMovingCorners.clear();
	}

	return false;
}

@Override
protected boolean down(int button) {
	// Skip if selected resource was changed
	if (mSelection.isSelectionChangedDuringDown()) {
		return false;
	}

	testPickPoint(mCallback);

	// Hit corner -> move it
	if (mHitCornerBody != null) {
		PathCornerIndexWrapper movingCorner = mPathCornerIndexPool.obtain();
		mMovingCorners.add(movingCorner);
		movingCorner.path = mHitPath;

		movingCorner.cornerIndex = mHitPath.getCornerIndex(mHitCornerBody.getPosition());
		movingCorner.originPosition.set(mHitCornerBody.getPosition());
		mAddedCorner = false;

		mHitCornerBody = null;
		mHitPath = null;
	}
	// Else create a new corner :)
	else {
		createCorner();
	}

	return false;
}

/**
 * Creates a new corner for all selected paths. If no paths are selected it creates a new path with
 * a corner.
 */
private void createCorner() {
	ArrayList<Path> selectedPaths = mSelection.getSelectedResourcesOfType(Path.class);

	// Create new path
	if (selectedPaths.isEmpty()) {
		PathCornerIndexWrapper movingCorner = mPathCornerIndexPool.obtain();
		mMovingCorners.add(movingCorner);

		movingCorner.path = new Path();
		movingCorner.path.setWorld(mWorld);

		mInvoker.execute(new CResourceAdd(movingCorner.path, mEditor));
		mInvoker.execute(new CSelectionSet(mSelection, movingCorner.path), true);
		mInvoker.execute(new CResourceCornerAdd(movingCorner.path, mTouchCurrent, mEditor), true);
		movingCorner.cornerIndex = movingCorner.path.getCornerCount() - 1;
	}
	// Create corner for existing paths
	else {
		boolean chained = false;
		for (Path path : selectedPaths) {
			PathCornerIndexWrapper movingCorner = mPathCornerIndexPool.obtain();
			mMovingCorners.add(movingCorner);
			movingCorner.path = path;

			mInvoker.execute(new CResourceCornerAdd(path, mTouchCurrent, mEditor), chained);
			movingCorner.cornerIndex = path.getCornerCount() - 1;

			chained = true;
		}
	}
	mAddedCorner = true;
}

@Override
public void activate() {
	super.activate();
	mSelection.addListener(this);

	// Draw corners for all paths
	for (IResource resource : mSelection.getSelectedResources()) {
		onResourceSelected(resource);
	}
}

@Override
public void deactivate() {
	super.deactivate();
	mSelection.removeListener(this);

	// Remove body corners from the paths
	for (IResource resource : mSelection.getSelectedResources()) {
		onResourceDeselected(resource);
	}
}

@Override
public void onResourceSelected(IResource resource) {
	if (resource instanceof Path) {
		((Path) resource).createBodyCorners();
	}
}

@Override
public void onResourceDeselected(IResource resource) {
	if (resource instanceof Path) {
		((Path) resource).destroyBodyCorners();
	}
}

/**
 * Wrapper for a path and current corner we're moving
 */
private static class PathCornerIndexWrapper {
	/** The path */
	Path path = null;
	/** Index of the corner we're moving */
	int cornerIndex = -1;
	/** original position */
	Vector2 originPosition = new Vector2();
}
}

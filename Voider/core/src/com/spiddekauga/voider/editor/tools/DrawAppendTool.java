package com.spiddekauga.voider.editor.tools;

import com.badlogic.gdx.math.Vector2;
import com.spiddekauga.utils.scene.ui.NotificationShower.NotificationTypes;
import com.spiddekauga.voider.config.IC_Editor.IC_Actor.IC_Visual;
import com.spiddekauga.voider.editor.IActorEditor;
import com.spiddekauga.voider.editor.IResourceChangeEditor;
import com.spiddekauga.voider.editor.commands.CActorDefFixCustomFixtures;
import com.spiddekauga.voider.editor.commands.CActorEditorCenterReset;
import com.spiddekauga.voider.editor.commands.CResourceCornerAdd;
import com.spiddekauga.voider.editor.commands.CResourceCornerRemoveExcessive;
import com.spiddekauga.voider.game.actors.Actor;
import com.spiddekauga.voider.resources.IResource;
import com.spiddekauga.voider.utils.Geometry.PolygonAreaTooSmallException;
import com.spiddekauga.voider.utils.Geometry.PolygonComplexException;
import com.spiddekauga.voider.utils.Messages;

/**
 * Tool for draw append on the selected actor
 */
public class DrawAppendTool extends ActorTool implements ISelectionListener {
/** Temporary variable for selected actor */
private Actor mSelectedActor = null;
/** Origin of the drag */
private Vector2 mDragOrigin = new Vector2();

/**
 * @param editor the actual editor
 * @param selection current selection in the editor, can be null
 * @param actorType the actor to draw
 */
public DrawAppendTool(IResourceChangeEditor editor, ISelection selection, Class<? extends Actor> actorType) {
	super(editor, selection, actorType);
	mActorType = actorType;
}

@Override
protected boolean dragged() {
	if (mSelectedActor != null) {
		if (haveMovedEnoughToAddAnotherCorner(mDragOrigin)) {
			appendCorner(true);
		}
	}
	return false;
}

@Override
protected boolean up(int button) {
	if (mSelectedActor != null) {
		// Add a final corner when released
		appendCorner(true);

		IC_Visual icVisual = getVisualConfig();
		mInvoker.execute(new CResourceCornerRemoveExcessive(mSelectedActor.getDef().getShape(), icVisual.getDrawNewCornerDistMinSq(),
				icVisual.getDrawCornerAngleMin()), true);

		try {
			// Reset center if the actor was just created
			if (mCreatedActorThisDown) {
				if (mEditor instanceof IActorEditor) {
					mInvoker.execute(new CActorEditorCenterReset((IActorEditor) mEditor), true);
				}
				mCreatedActorThisDown = false;
			}

			mInvoker.execute(new CActorDefFixCustomFixtures(mSelectedActor.getDef(), true), true);
		} catch (PolygonComplexException e) {
			mNotification.show(NotificationTypes.ERROR, Messages.Error.POLYGON_COMPLEX_DRAW_APPEND);
			handleBadCornerPosition(null);
		} catch (PolygonAreaTooSmallException e) {
			mNotification.show(NotificationTypes.ERROR, Messages.Error.POLYGON_AREA_TOO_SMALL);
			handleBadCornerPosition(null);
		}
	}
	return false;
}

@Override
protected boolean down(int button) {
	// Skip if selected resource was changed
	if (mSelection.isSelectionChangedDuringDown()) {
		return false;
	}

	mSelectedActor = mSelection.getFirstSelectedResourceOfType(mActorType);

	if (mSelectedActor == null) {
		mSelectedActor = createNewSelectedActor();
	} else {
		mInvoker.execute(new CActorDefFixCustomFixtures(mSelectedActor.getDef(), false));
		appendCorner(true);
	}
	setDrawing(true);

	appendCorner(true);

	return false;
}

@Override
public void activate() {
	mSelection.addListener(this);

	// Draw the resource as lines
	for (IResource resource : mSelection.getSelectedResources()) {
		onResourceSelected(resource);
	}
}

@Override
public void deactivate() {
	mSelection.removeListener(this);

	// Remove drawing as lines
	for (IResource resource : mSelection.getSelectedResources()) {
		onResourceDeselected(resource);
	}
}

@Override
public void onResourceSelected(IResource resource) {
	if (resource.getClass() == mActorType) {
		((Actor) resource).setDrawOnlyOutline(true);
	}
}

@Override
public void onResourceDeselected(IResource resource) {
	if (resource.getClass() == mActorType) {
		((Actor) resource).setDrawOnlyOutline(false);
	}

	if (mSelectedActor == resource) {
		mSelectedActor = null;
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
 * Appends a temporary corner in the current position
 * @param chained if the command shall be chained or not.
 */
private void appendCorner(boolean chained) {
	if (mSelectedActor != null) {
		Vector2 localPos = getLocalPosition(mTouchCurrent, mSelectedActor);

		mInvoker.execute(new CResourceCornerAdd(mSelectedActor.getDef().getShape(), localPos, mEditor), chained);
		mDragOrigin.set(mTouchCurrent);
	}
}
}

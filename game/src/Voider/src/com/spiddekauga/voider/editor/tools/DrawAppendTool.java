package com.spiddekauga.voider.editor.tools;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.spiddekauga.utils.Invoker;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.editor.IActorEditor;
import com.spiddekauga.voider.editor.IResourceChangeEditor;
import com.spiddekauga.voider.editor.commands.CActorDefFixCustomFixtures;
import com.spiddekauga.voider.editor.commands.CActorEditorCenterReset;
import com.spiddekauga.voider.editor.commands.CResourceCornerAdd;
import com.spiddekauga.voider.editor.commands.CResourceCornerRemoveExcessive;
import com.spiddekauga.voider.game.actors.Actor;
import com.spiddekauga.voider.game.actors.BulletActorDef;
import com.spiddekauga.voider.resources.IResource;
import com.spiddekauga.voider.scene.SceneSwitcher;
import com.spiddekauga.voider.utils.Geometry.PolygonComplexException;
import com.spiddekauga.voider.utils.Geometry.PolygonCornersTooCloseException;
import com.spiddekauga.voider.utils.Messages;
import com.spiddekauga.voider.utils.Pools;

/**
 * Tool for draw append on the selected actor
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class DrawAppendTool extends ActorTool implements ISelectionListener {
	/**
	 * @param camera camera used for the level
	 * @param world the actual world
	 * @param invoker used for undo/redo
	 * @param selection current selection in the editor, can be null
	 * @param editor the actual editor
	 * @param actorType the actor to draw
	 */
	public DrawAppendTool(Camera camera, World world, Invoker invoker, ISelection selection, IResourceChangeEditor editor, Class<? extends Actor> actorType) {
		super(camera, world, invoker, selection, editor, actorType);
		mActorType = actorType;
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
	protected boolean dragged() {
		if (mSelectedActor != null) {
			if (haveMovedEnoughToAddAnotherCorner()) {
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

			mInvoker.execute(new CResourceCornerRemoveExcessive(mSelectedActor.getDef().getVisualVars()), true);

			// Reset center if the actor was just created
			if (mCreatedActorThisDown) {
				if (mEditor instanceof IActorEditor) {
					mInvoker.execute(new CActorEditorCenterReset((IActorEditor) mEditor), true);
				}
				mCreatedActorThisDown = false;
			}

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
			((Actor)resource).setDrawOnlyOutline(true);
		}
	}

	@Override
	public void onResourceDeselected(IResource resource) {
		if (resource.getClass() == mActorType) {
			((Actor)resource).setDrawOnlyOutline(false);
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
	 * Appends a temporary corner in the current position
	 * @param chained if the command shall be chained or not.
	 */
	private void appendCorner(boolean chained) {
		if (mSelectedActor != null) {
			Vector2 localPos = getLocalPosition(mTouchCurrent, mSelectedActor);

			mInvoker.execute(new CResourceCornerAdd(mSelectedActor.getDef().getVisualVars(), localPos, mEditor), chained);
			mDragOrigin.set(mTouchCurrent);

			Pools.vector2.free(localPos);
		}
	}

	/** Temporary variable for selected actor */
	private Actor mSelectedActor = null;
	/** Origin of the drag */
	private Vector2 mDragOrigin = new Vector2();
}

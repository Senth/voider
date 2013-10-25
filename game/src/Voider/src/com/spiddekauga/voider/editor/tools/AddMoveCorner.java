package com.spiddekauga.voider.editor.tools;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.spiddekauga.utils.Command;
import com.spiddekauga.utils.Invoker;
import com.spiddekauga.voider.editor.HitWrapper;
import com.spiddekauga.voider.editor.IResourceChangeEditor;
import com.spiddekauga.voider.editor.commands.CActorDefFixCustomFixtures;
import com.spiddekauga.voider.editor.commands.CResourceCornerAdd;
import com.spiddekauga.voider.editor.commands.CResourceCornerMove;
import com.spiddekauga.voider.game.actors.Actor;
import com.spiddekauga.voider.resources.IResource;
import com.spiddekauga.voider.resources.IResourceCorner;
import com.spiddekauga.voider.scene.SceneSwitcher;
import com.spiddekauga.voider.utils.Geometry.PolygonComplexException;
import com.spiddekauga.voider.utils.Geometry.PolygonCornersTooCloseException;
import com.spiddekauga.voider.utils.Messages;
import com.spiddekauga.voider.utils.Pools;

/**
 * Tool for adding or moving a corner
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class AddMoveCorner extends TouchTool implements ISelectionListener {
	/**
	 * @param camera the camera
	 * @param world the world where the objects are in
	 * @param invoker used for undo/redo
	 * @param selection all selected resources
	 * @param editor the editor this tool is bound to
	 */
	public AddMoveCorner(Camera camera, World world, Invoker invoker, ISelection selection, IResourceChangeEditor editor) {
		super(camera, world, invoker, selection, editor);
	}

	@Override
	protected boolean down() {
		testPickAabb();

		// Hit a corner move it
		if (mHitCorner != null) {
			mAddingCorner = false;
			mCornerIndexCurrent = mHitCorner.getCornerIndex(mHitCornerBody.getPosition());
			mDragOrigin.set(mHitCorner.getCornerPosition(mCornerIndexCurrent));
			setDrawing(true);
			return true;
		}
		// Try and se if we can add a corner between two existing corners
		else {
			mAddingCorner = true;
			// TODO
		}

		return false;
	}

	@Override
	protected boolean dragged() {
		if (mCornerIndexCurrent != -1) {
			Vector2 newCornerPos;

			// Use local position not world
			if (mHitCorner instanceof Actor) {
				newCornerPos = ActorTool.getLocalPosition(mTouchCurrent, (Actor) mHitCorner);
			}
			// Use world position
			else {
				newCornerPos = Pools.vector2.obtain();
				newCornerPos.set(mTouchCurrent);
			}

			mHitCorner.moveCorner(mCornerIndexCurrent, newCornerPos);
			Pools.vector2.free(newCornerPos);
		}
		return false;
	}

	@Override
	protected boolean up() {
		if (mCornerIndexCurrent != -1) {
			// Add or move?
			Command addOrMoveCommand;
			if (mAddingCorner) {
				Vector2 removedCorner = mHitCorner.removeCorner(mCornerIndexCurrent);
				addOrMoveCommand = new CResourceCornerAdd(mHitCorner, removedCorner, mCornerIndexCurrent, mEditor);
				Pools.vector2.free(removedCorner);
			} else {
				Vector2 newPos = Pools.vector2.obtain();
				newPos.set(mHitCorner.getCornerPosition(mCornerIndexCurrent));
				mHitCorner.moveCorner(mCornerIndexCurrent, mDragOrigin);
				addOrMoveCommand = new CResourceCornerMove(mHitCorner, mCornerIndexCurrent, newPos, mEditor);
				Pools.vector2.free(newPos);
			}

			// Add corner via invoker instead
			if (mHitCorner instanceof Actor) {
				mInvoker.execute(new CActorDefFixCustomFixtures(((Actor) mHitCorner).getDef(), false));
				mInvoker.execute(addOrMoveCommand, true);

				try {
					mInvoker.execute(new CActorDefFixCustomFixtures(((Actor) mHitCorner).getDef(), true), true);
				} catch (PolygonComplexException e) {
					SceneSwitcher.showErrorMessage(Messages.Error.POLYGON_COMPLEX_ADD);
					handleBadCornerPosition(null);
				} catch (PolygonCornersTooCloseException e) {
					Gdx.app.error("DrawActorTool", "PolygonCornersTooClose! Should never happen!");
					handleBadCornerPosition(null);
				}
			} else {
				mInvoker.execute(addOrMoveCommand);
			}

			mCornerIndexCurrent = -1;
		}

		return false;
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

	@Override
	protected QueryCallback getCallback() {
		return mCallback;
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
			// TODO
		}
		if (resource instanceof Actor) {
			((Actor) resource).setDrawOnlyOutline(true);
		}
	}

	@Override
	public void onResourceDeselected(IResource resource) {
		if (resource instanceof IResourceCorner) {
			// TODO
		}
		if (resource instanceof Actor) {
			((Actor) resource).setDrawOnlyOutline(false);
		}
	}

	/** Callback for selecting corners */
	QueryCallback mCallback = new QueryCallback() {
		@Override
		public boolean reportFixture(Fixture fixture) {
			Object userData = fixture.getBody().getUserData();
			if (userData instanceof HitWrapper) {
				mHitCornerBody = fixture.getBody();

				if (((HitWrapper) userData).resource instanceof IResourceCorner) {
					mHitCorner = (IResourceCorner) mHitCornerBody.getUserData();
				} else {
					Gdx.app.error("AddMoveCorner", "HitWrapper resources was not a corner!");
				}

				return false;
			}
			return true;
		}
	};

	/** If we're adding a corner, if false we're moving a corner */
	private boolean mAddingCorner = false;
	/** Body of first corner we hit */
	private Body mHitCornerBody = null;
	/** First corner we hit */
	private IResourceCorner mHitCorner = null;
	/** Corner index */
	private int mCornerIndexCurrent = -1;
	/** Where we started dragging from */
	private Vector2 mDragOrigin = new Vector2();
}

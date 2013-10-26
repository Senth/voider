package com.spiddekauga.voider.editor.tools;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.spiddekauga.utils.Invoker;
import com.spiddekauga.voider.editor.HitWrapper;
import com.spiddekauga.voider.editor.IResourceChangeEditor;
import com.spiddekauga.voider.editor.commands.CActorDefFixCustomFixtures;
import com.spiddekauga.voider.editor.commands.CResourceCornerRemove;
import com.spiddekauga.voider.editor.commands.CResourceCornerRemoveAll;
import com.spiddekauga.voider.editor.commands.CResourceRemove;
import com.spiddekauga.voider.editor.commands.CSelectionRemove;
import com.spiddekauga.voider.game.actors.Actor;
import com.spiddekauga.voider.resources.IResource;
import com.spiddekauga.voider.resources.IResourceCorner;
import com.spiddekauga.voider.scene.SceneSwitcher;
import com.spiddekauga.voider.utils.Geometry.PolygonComplexException;
import com.spiddekauga.voider.utils.Geometry.PolygonCornersTooCloseException;
import com.spiddekauga.voider.utils.Messages;

/**
 * 
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class RemoveCorner extends TouchTool implements ISelectionListener {
	/**
	 * @param camera the camera
	 * @param world world where the objects are in
	 * @param invoker used for undo/redo
	 * @param selection all selected resources
	 * @param editor the editor this tool is bound to
	 */
	public RemoveCorner(
			Camera camera, World world, Invoker invoker, ISelection selection, IResourceChangeEditor editor) {
		super(camera, world, invoker, selection, editor);
	}

	@Override
	protected boolean down() {
		testPickPoint();

		// Hit a corner -> remove it
		if (mHitCornerBody != null) {
			int removeIndex = mHitResource.getCornerIndex(mHitCornerBody.getPosition());
			if (removeIndex != -1) {
				boolean chained = false;
				if (mHitResource instanceof Actor) {
					mInvoker.execute(new CActorDefFixCustomFixtures(((Actor)mHitResource).getDef(), false));
					chained = true;
				}
				mInvoker.execute(new CResourceCornerRemove(mHitResource, removeIndex, mEditor), chained);

				// Was it the last corner? Remove resource too then
				if (mHitResource.getCornerCount() == 0) {
					mInvoker.execute(new CResourceRemove(mHitResource, mEditor), true);
					mInvoker.execute(new CSelectionRemove(mSelection, mHitResource), true);
				}
				// Else update fixture def if actor
				else if (mHitResource instanceof Actor) {
					try {
						mInvoker.execute(new CActorDefFixCustomFixtures(((Actor)mHitResource).getDef(), true), true);
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
		// Hit a resource -> remove it
		else {
			boolean chained = false;
			if (mHitResource instanceof Actor) {
				mInvoker.execute(new CActorDefFixCustomFixtures(((Actor)mHitResource).getDef(), false));
				chained = true;
			}
			mInvoker.execute(new CResourceCornerRemoveAll(mHitResource, mEditor), chained);
			mInvoker.execute(new CResourceRemove(mHitResource, mEditor), true);
			mInvoker.execute(new CSelectionRemove(mSelection, mHitResource), true);
		}

		mHitCornerBody = null;
		mHitResource = null;

		return false;
	}

	@Override
	protected boolean dragged() {
		// Does nothing
		return false;
	}

	@Override
	protected boolean up() {
		// Does nothing
		return false;
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

	@Override
	protected QueryCallback getCallback() {
		return mCallback;
	}

	/** Callback for testing to hit the resource or a corner */
	private QueryCallback mCallback = new QueryCallback() {
		@Override
		public boolean reportFixture(Fixture fixture) {
			Object userData = fixture.getBody().getUserData();

			// Hit a corner -> just exit as we will delete it
			if (userData instanceof HitWrapper) {
				mHitCornerBody = fixture.getBody();
				mHitResource = (IResourceCorner) ((HitWrapper)userData).resource;
				return false;
			}
			// Hit a resource
			else if (userData instanceof IResourceCorner) {
				mHitResource = (IResourceCorner) userData;
			}

			return true;
		}
	};

	/** Hit resources */
	private IResourceCorner mHitResource = null;
	/** Hit corner */
	private Body mHitCornerBody = null;
}

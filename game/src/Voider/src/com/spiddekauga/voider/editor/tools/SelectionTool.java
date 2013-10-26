package com.spiddekauga.voider.editor.tools;

import java.util.ArrayList;
import java.util.Iterator;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.spiddekauga.utils.Invoker;
import com.spiddekauga.utils.KeyHelper;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.editor.IResourceChangeEditor;
import com.spiddekauga.voider.editor.brushes.RectangleBrush;
import com.spiddekauga.voider.editor.commands.CSelectionAdd;
import com.spiddekauga.voider.editor.commands.CSelectionRemove;
import com.spiddekauga.voider.editor.commands.CSelectionSet;
import com.spiddekauga.voider.resources.IResource;
import com.spiddekauga.voider.utils.Pools;

/**
 * Container class for all the selected actors in the level editor
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class SelectionTool extends TouchTool {
	/**
	 * @param camera camera used for determining where in the world the pointer i
	 * @param world used for picking
	 * @param invoker used for undo/redo
	 * @param selection the selection to use
	 * @param editor the editor the selection tool uses
	 */
	public SelectionTool(Camera camera, World world, Invoker invoker, ISelection selection, IResourceChangeEditor editor) {
		super(camera, world, invoker, selection, editor);
	}

	@Override
	protected boolean down() {
		// Draw selection box
		if (mActive) {
			mRectangleBrush = new RectangleBrush(Config.Editor.BRUSH_SELECTION_COLOR, mTouchCurrent);
			mEditor.onResourceAdded(mRectangleBrush);
		}
		// Test if we shall add or remove any actors
		else {
			testPickPoint();

			if (!mHitResources.isEmpty()) {
				boolean handled = false;

				for (IResource resource : mHitResources) {
					// Add to selection
					if (KeyHelper.isShiftPressed()) {
						if (!mSelection.isSelected(resource)) {
							mInvoker.execute(new CSelectionAdd(mSelection, resource));
							handled = true;
						}
					}
					// Remove from selection
					else if (KeyHelper.isCtrlPressed()) {
						if (mSelection.isSelected(resource)) {
							mInvoker.execute(new CSelectionRemove(mSelection, resource));
							handled = true;
						}
					}
					// Set selection
					else {
						if (!mSelection.isSelected(resource)) {
							mInvoker.execute(new CSelectionSet(mSelection, resource));
							handled = true;
						}
					}
				}

				return handled;
			}
		}

		return mActive;
	}

	@Override
	protected boolean dragged() {
		if (mRectangleBrush != null) {
			mRectangleBrush.setEndPosition(mTouchCurrent);
		}
		return mActive;
	}

	@Override
	protected boolean up() {
		if (mRectangleBrush != null) {
			// Check with AABB which actors were inside the selection box
			testPickAabb(mTouchOrigin, mTouchCurrent);

			// Add resources to selection
			if (KeyHelper.isShiftPressed()) {
				addPickedResources(false);
			}
			// Remove resources from selection
			else if (KeyHelper.isCtrlPressed()) {
				removePickedResources(false);
			}
			// Set selected resources
			else {
				mInvoker.execute(new CSelectionSet(mSelection));
				addPickedResources(true);
			}


			mEditor.onResourceRemoved(mRectangleBrush);
			mRectangleBrush = null;
		}

		mHitResources.clear();

		return mActive;
	}



	/**
	 * Adds all the picked resources
	 * @param chained if the command shall be chained
	 */
	private void addPickedResources(boolean chained) {
		@SuppressWarnings("unchecked")
		ArrayList<IResource> toSelect = Pools.arrayList.obtain();

		for (IResource resource : mHitResources) {
			toSelect.add(resource);
		}

		mInvoker.execute(new CSelectionAdd(mSelection, toSelect), chained);
		Pools.arrayList.free(toSelect);
	}

	/**
	 * Removes all picked actors from the selection
	 * @param chained if the command shall be chained
	 */
	private void removePickedResources(boolean chained) {
		@SuppressWarnings("unchecked")
		ArrayList<IResource> toDeselect = Pools.arrayList.obtain();

		for (IResource resource : mHitResources) {
			toDeselect.add(resource);
		}

		mInvoker.execute(new CSelectionRemove(mSelection, toDeselect), chained);
		Pools.arrayList.free(toDeselect);
	}

	@Override
	protected QueryCallback getCallback() {
		return mCallback;
	}

	@Override
	protected void filterPick() {
		if (!mHitResources.isEmpty()) {
			// We keep all if no resources is selected
			// If other resources are selected we only keep those of the same type
			if (!mSelection.isEmpty()) {
				Class<? extends IResource> mostCommonType = mSelection.getMostCommonSelectedResourceType();
				Iterator<IResource> iterator = mHitResources.iterator();
				while (iterator.hasNext()) {
					if (iterator.next().getClass() != mostCommonType) {
						iterator.remove();
					}
				}
			}
		}
	}

	@Override
	public void activate() {
		mActive = true;
	}

	@Override
	public void deactivate() {
		mActive = false;
	}

	/** Picking for resources */
	private QueryCallback mCallback = new QueryCallback() {
		@Override
		public boolean reportFixture(Fixture fixture) {
			Body body = fixture.getBody();

			if (body.getUserData() instanceof IResource) {
				mHitResources.add((IResource)body.getUserData());
			}

			return true;
		}
	};

	/** Rectangle brush */
	private RectangleBrush mRectangleBrush = null;
	/** If the selection tool is active */
	private boolean mActive = false;
	/** All hit resources */
	private ArrayList<IResource> mHitResources = new ArrayList<IResource>();


}

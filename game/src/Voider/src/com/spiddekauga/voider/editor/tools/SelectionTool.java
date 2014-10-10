package com.spiddekauga.voider.editor.tools;

import java.util.ArrayList;
import java.util.Iterator;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.spiddekauga.utils.KeyHelper;
import com.spiddekauga.utils.commands.Invoker;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.Config.Editor;
import com.spiddekauga.voider.editor.IResourceChangeEditor;
import com.spiddekauga.voider.editor.brushes.RectangleBrush;
import com.spiddekauga.voider.editor.commands.CSelectionAdd;
import com.spiddekauga.voider.editor.commands.CSelectionRemove;
import com.spiddekauga.voider.editor.commands.CSelectionSet;
import com.spiddekauga.voider.game.Path;
import com.spiddekauga.voider.game.triggers.Trigger;
import com.spiddekauga.voider.resources.IResource;

/**
 * Container class for all the selected actors in the level editor
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
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

	/**
	 * Adds, removes or sets the hit resources
	 * @return true if the selection list was changed
	 */
	private boolean addRemoveOrSetHitResources() {
		boolean handled = false;

		if (!mHitResources.isEmpty()) {
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
		}

		return handled;
	}

	/**
	 * @param resourceType the resource type to test if it can be selected
	 * @return true if the resource type can be selected
	 */
	private boolean isResourceSelectable(Class<? extends IResource> resourceType) {
		boolean isSelectable = false;

		// Check if we are allowed to select this
		Iterator<Class<? extends IResource>> iterator = mSelectableResourceTypes.iterator();
		while (iterator.hasNext() && !isSelectable) {
			if (iterator.next().isAssignableFrom(resourceType)) {
				isSelectable = true;
			}
		}

		// Check the current selection and if we are allowed to select this
		if (isSelectable && !mCanChangeResourceTypeSelection) {
			if (!mSelection.isSelected(resourceType) && !mSelection.isEmpty()) {
				isSelectable = false;
			}
		}

		return isSelectable;
	}

	/**
	 * Removes all resources that aren't allowed to be selected
	 */
	private void removeUnallowedResources() {
		Iterator<IResource> iterator = mHitResources.iterator();
		while (iterator.hasNext()) {
			if (!isResourceSelectable(iterator.next().getClass())) {
				iterator.remove();
			}
		}
	}

	@Override
	protected boolean down(int button) {
		// Draw selection box
		if (isActive()) {
			mRectangleBrush = new RectangleBrush(Config.Editor.BRUSH_SELECTION_COLOR, mTouchCurrent);
			mEditor.onResourceAdded(mRectangleBrush);
		}
		// Test if we shall add or remove any actors
		else {
			if (isResourceSelectable(Path.class)) {
				testPickAabb(mCallbackPaths, Editor.PICK_TRIGGER_SIZE);
			}
			if (isResourceSelectable(Trigger.class)) {
				testPickAabb(mCallbackTriggers, Editor.PICK_PATH_SIZE);
			}
			testPickPoint(mCallbackResources);

			removeUnallowedResources();

			if (addRemoveOrSetHitResources()) {
				mSelection.setSelectionChangedDuringDown(true);
			}
		}

		return isActive();
	}

	@Override
	protected boolean dragged() {
		if (mRectangleBrush != null) {
			mRectangleBrush.setEndPosition(mTouchCurrent);
		}
		return isActive();
	}

	@Override
	protected boolean up(int button) {
		mSelection.setSelectionChangedDuringDown(false);

		if (mRectangleBrush != null) {
			// Check with AABB which actors were inside the selection box
			testPickAabb(mCallbackResources, mTouchOrigin, mTouchCurrent);

			// Add resources to selection
			if (KeyHelper.isShiftPressed()) {
				mInvoker.execute(new CSelectionAdd(mSelection, mHitResources));
			}
			// Remove resources from selection
			else if (KeyHelper.isCtrlPressed()) {
				mInvoker.execute(new CSelectionRemove(mSelection, mHitResources));
			}
			// Set selected resources
			else {
				mInvoker.execute(new CSelectionSet(mSelection, mHitResources));
			}


			mEditor.onResourceRemoved(mRectangleBrush);
			mRectangleBrush = null;
		}

		mHitResources.clear();

		return isActive();
	}

	/**
	 * Set the current selectable resource
	 * @param selectableResourceTypes what kind of resource the selection tool is allowed
	 *        to select
	 * @param canChangeResourceType if the selection tool is allowed to change the
	 *        selection independent of the current selection. E.g. if it is allowed to
	 *        change the selection to an enemy from a trigger.
	 */
	public void setSelectableResourceTypes(ArrayList<Class<? extends IResource>> selectableResourceTypes, boolean canChangeResourceType) {
		mSelectableResourceTypes = selectableResourceTypes;
		mCanChangeResourceTypeSelection = canChangeResourceType;

		clearSelectionFromUnallowedResources();
	}

	/**
	 * Clears the current selection from the resources that aren't allowed to be selected.
	 */
	private void clearSelectionFromUnallowedResources() {
		mHitResources.clear();
		mHitResources.addAll(mSelection.getSelectedResources());
		removeUnallowedResources();
		mInvoker.execute(new CSelectionSet(mSelection, mHitResources));
		mHitResources.clear();
	}

	/** Picking for paths */
	private QueryCallback mCallbackPaths = new QueryCallback() {
		@Override
		public boolean reportFixture(Fixture fixture) {
			Body body = fixture.getBody();

			if (body.getUserData() instanceof Path) {
				mHitResources.add((IResource) body.getUserData());
			}

			return true;
		}
	};

	/** Picking for paths */
	private QueryCallback mCallbackTriggers = new QueryCallback() {
		@Override
		public boolean reportFixture(Fixture fixture) {
			Body body = fixture.getBody();

			if (body.getUserData() instanceof Trigger) {
				mHitResources.add((IResource) body.getUserData());
			}

			return true;
		}
	};

	/** Picking for resources */
	private QueryCallback mCallbackResources = new QueryCallback() {
		@Override
		public boolean reportFixture(Fixture fixture) {
			Body body = fixture.getBody();

			if (body.getUserData() instanceof IResource) {
				mHitResources.add((IResource) body.getUserData());
			}

			return true;
		}
	};

	/** If we are allowed to change resource types independent of the current selection */
	private boolean mCanChangeResourceTypeSelection = true;
	/** Rectangle brush */
	private RectangleBrush mRectangleBrush = null;
	/** All hit resources */
	private ArrayList<IResource> mHitResources = new ArrayList<IResource>();


}

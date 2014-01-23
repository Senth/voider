package com.spiddekauga.voider.editor.tools;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.spiddekauga.utils.Invoker;
import com.spiddekauga.utils.KeyHelper;
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

	@Override
	protected boolean down() {
		// Draw selection box
		if (mActive) {
			mRectangleBrush = new RectangleBrush(Config.Editor.BRUSH_SELECTION_COLOR, mTouchCurrent);
			mEditor.onResourceAdded(mRectangleBrush);
		}
		// Test if we shall add or remove any actors
		else {
			boolean addRegularResources = false;
			boolean addTriggers = false;
			boolean addPaths = false;

			Class<? extends IResource> mostCommonSelectedResourceType = mSelection.getMostCommonSelectedResourceType();

			// Add all resources (including triggers and paths)
			if (mSelection.isEmpty()) {
				addRegularResources = true;
				addTriggers = true;
				addPaths = true;
			}
			// Add only triggers
			else if (Trigger.class.isAssignableFrom(mostCommonSelectedResourceType)) {
				addTriggers = true;
			}
			// Add only paths
			else if (Path.class.isAssignableFrom(mostCommonSelectedResourceType)) {
				addPaths = true;
			}
			// Only regular
			else {
				addRegularResources = true;
			}

			if (addTriggers) {
				testPickAabb(mCallbackTriggers, Editor.PICK_TRIGGER_SIZE);
			}
			if (addPaths) {
				testPickAabb(mCallbackPaths, Editor.PICK_PATH_SIZE);
			}
			if (addRegularResources) {
				testPickPoint(mCallbackResources);
			}

			if (addRemoveOrSetHitResources()) {
				mSelection.setSelectionChangedDuringDown(true);
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

		return mActive;
	}

	@Override
	public void activate() {
		mActive = true;
	}

	@Override
	public void deactivate() {
		mActive = false;
	}

	/** Picking for paths */
	private QueryCallback mCallbackPaths = new QueryCallback() {
		@Override
		public boolean reportFixture(Fixture fixture) {
			Body body = fixture.getBody();

			if (body.getUserData() instanceof Path) {
				mHitResources.add((IResource)body.getUserData());
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
				mHitResources.add((IResource)body.getUserData());
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

package com.spiddekauga.voider.editor.tools;

import java.util.ArrayList;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.spiddekauga.utils.Invoker;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.editor.IResourceChangeEditor;
import com.spiddekauga.voider.editor.brushes.RectangleBrush;
import com.spiddekauga.voider.resources.IResource;
import com.spiddekauga.voider.resources.IResourceSelectable;
import com.spiddekauga.voider.utils.Pools;

/**
 * Container class for all the selected actors in the level editor
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class SelectionTool extends TouchTool implements ISelection {
	/**
	 * @param camera camera used for determining where in the world the pointer i
	 * @param world used for picking
	 * @param invoker used for undo/redo
	 * @param editor the editor the selection tool uses
	 */
	public SelectionTool(Camera camera, World world, Invoker invoker, IResourceChangeEditor editor) {
		super(camera, world, invoker);
		mEditor = editor;
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
			// TODO
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
			if (keyDown(Input.Keys.SHIFT_LEFT | Input.Keys.SHIFT_RIGHT)) {
				addPickedResources();
			}
			// Remove resources from selection
			else if (keyDown(Input.Keys.CONTROL_LEFT | Input.Keys.CONTROL_RIGHT)) {
				removePickedResources();
			}
			// Set selected resources
			else {
				clearSelection();
				addPickedResources();
			}


			mEditor.onResourceRemoved(mRectangleBrush);
			mRectangleBrush = null;
		}

		return mActive;
	}

	/**
	 * Adds all the picked resources
	 */
	private void addPickedResources() {
		for (Body body : mHitBodies) {
			if (body.getUserData() instanceof IResource) {
				IResource resource = (IResource) body.getUserData();
				mSelectedResources.add(resource);

				if (resource instanceof IResourceSelectable) {
					((IResourceSelectable) resource).setSelected(true);
				}
			}
		}
	}

	/**
	 * Removes all picked actors from the selection
	 */
	private void removePickedResources() {
		for (Body body : mHitBodies) {
			if (body.getUserData() instanceof IResource) {
				IResource resource = (IResource) body.getUserData();
				boolean removed = mSelectedResources.remove(resource);

				if (removed) {
					if (resource instanceof IResourceSelectable) {
						((IResourceSelectable) resource).setSelected(false);
					}
				}
			}
		}
	}

	@Override
	protected QueryCallback getCallback() {
		return mCallback;
	}

	@Override
	protected Body filterPick(ArrayList<Body> hitBodies) {
		return null;
	}

	@Override
	public void activate() {
		mActive = true;
	}

	@Override
	public void deactivate() {
		mActive = false;
	}

	@Override
	public ArrayList<IResource> getSelectedResources() {
		return mSelectedResources;
	}

	@Override
	public Class<? extends IResource> getMostCommonSelectedResourceType() {
		// TODO
		return null;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <ResourceType extends IResource> ArrayList<ResourceType> getSelectedResourcesOfType(Class<ResourceType> type) {
		ArrayList<ResourceType> selectedActors = Pools.arrayList.obtain();

		for (IResource selectedResource : mSelectedResources) {
			if (selectedResource.getClass() == type) {
				selectedActors.add((ResourceType) selectedResource);
			}
		}

		return selectedActors;
	}

	@Override
	public void deleteSelectedActors() {
		// TODO
	}

	@Override
	public void clearSelection() {
		for (IResource resource : mSelectedResources) {
			if (resource instanceof IResourceSelectable) {
				((IResourceSelectable) resource).setSelected(false);
			}
		}

		mSelectedResources.clear();
	}

	/** Editor that uses the tool */
	private IResourceChangeEditor mEditor;
	/** Rectangle brush */
	private RectangleBrush mRectangleBrush = null;
	/** If the selection tool is active */
	private boolean mActive = false;
	/** Current resource selection */
	private ArrayList<IResource> mSelectedResources = new ArrayList<IResource>();

	/** Picking for resources */
	private QueryCallback mCallback = new QueryCallback() {
		@Override
		public boolean reportFixture(Fixture fixture) {
			Body body = fixture.getBody();

			if (body.getUserData() instanceof IResource) {
				mHitBodies.add(body);
			}

			return true;
		}
	};
}

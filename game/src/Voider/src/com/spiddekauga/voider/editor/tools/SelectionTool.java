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
		super(camera, world, invoker, null, editor);
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
			if (mDoubleClick) {
				clearSelection();
				return true;
			}

			testPickPoint();

			if (mHitBody != null) {
				IResource resource = (IResource) mHitBody.getUserData();
				if (!isSelected(resource)) {
					clearSelection();
					addResource(resource);
					return true;
				}
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

	@Override
	public void addResource(IResource resource) {
		mSelectedResources.add(resource);

		if (resource instanceof IResourceSelectable) {
			((IResourceSelectable) resource).setSelected(true);
		}

		for (ISelectionListener listener : mListeners) {
			listener.onResourceSelected(resource);
		}
	}

	@Override
	public void removeResource(IResource resource) {
		boolean removed = mSelectedResources.remove(resource);

		if (removed) {
			if (resource instanceof IResourceSelectable) {
				((IResourceSelectable) resource).setSelected(false);
			}

			for (ISelectionListener listener : mListeners) {
				listener.onResourceDeselected(resource);
			}
		}
	}

	@Override
	public void addResources(ArrayList<IResource> resources) {
		for (IResource resource : resources) {
			addResource(resource);
		}
	}

	/**
	 * Adds all the picked resources
	 */
	private void addPickedResources() {
		for (Body body : mHitBodies) {
			if (body.getUserData() instanceof IResource) {
				IResource resource = (IResource) body.getUserData();
				addResource(resource);
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
				removeResource(resource);
			}
		}
	}

	@Override
	protected QueryCallback getCallback() {
		return mCallback;
	}

	@Override
	protected Body filterPick(ArrayList<Body> hitBodies) {
		if (hitBodies.isEmpty() || mActive) {
			return null;
		}

		// Just pick first if none are selected
		if (mSelectedResources.isEmpty()) {
			return hitBodies.get(0);
		}
		// Only return a resource that is the same as the most type
		else {
			Class<? extends IResource> mostCommonType = getMostCommonSelectedResourceType();
			for (Body body : hitBodies) {
				if (body.getUserData().getClass() == mostCommonType) {
					return body;
				}
			}
		}

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

			for (ISelectionListener listener : mListeners) {
				listener.onResourceDeselected(resource);
			}
		}

		mSelectedResources.clear();
	}

	@Override
	public boolean isSelected(IResource resource) {
		return mSelectedResources.contains(resource);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <ResourceType extends IResource> ResourceType getFirstSelectedResourceOfType(Class<ResourceType> type) {
		for (IResource resource : mSelectedResources) {
			if (resource.getClass() == type) {
				return (ResourceType) resource;
			}
		}

		return null;
	}

	@Override
	public void addListener(ISelectionListener listener) {
		mListeners.add(listener);
	}

	@Override
	public void removeListener(ISelectionListener listener) {
		mListeners.remove(listener);
	}

	/** All listeners */
	private ArrayList<ISelectionListener> mListeners = new ArrayList<ISelectionListener>();
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

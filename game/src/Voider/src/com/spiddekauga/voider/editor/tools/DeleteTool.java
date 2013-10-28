package com.spiddekauga.voider.editor.tools;

import java.util.ArrayList;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.spiddekauga.utils.Invoker;
import com.spiddekauga.voider.editor.IResourceChangeEditor;
import com.spiddekauga.voider.editor.commands.CResourceRemove;
import com.spiddekauga.voider.editor.commands.CSelectionSet;
import com.spiddekauga.voider.resources.IResource;
import com.spiddekauga.voider.utils.Pools;

/**
 * Tool for deleting resources
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class DeleteTool extends TouchTool {
	/**
	 * @param camera used for pointer coordinates
	 * @param world used for transferring camera coordinates to world coordinates
	 * @param invoker used for undo/redo
	 * @param selection all selected resources
	 * @param editor editor bound to this tool
	 */
	public DeleteTool(Camera camera, World world, Invoker invoker, ISelection selection, IResourceChangeEditor editor) {
		super(camera, world, invoker, selection, editor);
	}

	@Override
	protected boolean down() {
		if (mActive) {
			removeSelectedResources();
		}
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

		removeSelectedResources();
		mActive = true;
	}

	@Override
	public void deactivate() {
		super.deactivate();
		mActive = false;
	}

	@Override
	public boolean keyDown(int keycode) {
		if (keycode == Keys.DEL || keycode == Keys.BACKSPACE) {
			removeSelectedResources();
		}
		return false;
	}

	/**
	 * Removes all selected resources
	 */
	private void removeSelectedResources() {
		// Delete all selected resources
		if (!mSelection.isEmpty()) {
			@SuppressWarnings("unchecked")
			ArrayList<IResource> copySelectedResources = Pools.arrayList.obtain();
			copySelectedResources.addAll(mSelection.getSelectedResources());

			mInvoker.execute(new CSelectionSet(mSelection));
			for (IResource resource : copySelectedResources) {
				mInvoker.execute(new CResourceRemove(resource, mEditor));
			}

			Pools.arrayList.free(copySelectedResources);
		}
	}

	@Override
	protected QueryCallback getCallback() {
		return null;
	}

	/** True if the tool is active */
	private boolean mActive = false;
}

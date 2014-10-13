package com.spiddekauga.voider.editor.tools;

import java.util.ArrayList;

import com.spiddekauga.utils.KeyHelper;
import com.spiddekauga.voider.editor.IResourceChangeEditor;
import com.spiddekauga.voider.editor.commands.CActorDefFixCustomFixtures;
import com.spiddekauga.voider.editor.commands.CResourceRemove;
import com.spiddekauga.voider.editor.commands.CSelectionSet;
import com.spiddekauga.voider.game.actors.Actor;
import com.spiddekauga.voider.resources.IResource;
import com.spiddekauga.voider.utils.Pools;

/**
 * Tool for deleting resources
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class DeleteTool extends TouchTool {
	/**
	 * @param editor editor bound to this tool
	 * @param selection all selected resources
	 */
	public DeleteTool(IResourceChangeEditor editor, ISelection selection) {
		super(editor, selection);

		mSelectableResourceTypes.add(IResource.class);
	}

	@Override
	protected boolean down(int button) {
		if (isActive()) {
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
	protected boolean up(int button) {
		// Does nothing
		return false;
	}

	@Override
	public void activate() {
		super.activate();
		removeSelectedResources();
	}

	@Override
	public boolean keyDown(int keycode) {
		if (KeyHelper.isDeletePressed(keycode)) {
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
				if (resource instanceof Actor) {
					mInvoker.execute(new CActorDefFixCustomFixtures(((Actor) resource).getDef(), false), true);
				}
				mInvoker.execute(new CResourceRemove(resource, mEditor), true);
			}

			Pools.arrayList.free(copySelectedResources);
		}
	}
}

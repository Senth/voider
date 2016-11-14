package com.spiddekauga.voider.editor.tools;

import com.spiddekauga.utils.KeyHelper;
import com.spiddekauga.voider.editor.IResourceChangeEditor;
import com.spiddekauga.voider.editor.commands.CResourceRemove;
import com.spiddekauga.voider.editor.commands.CSelectionSet;
import com.spiddekauga.voider.resources.IResource;

import java.util.ArrayList;

/**
 * Tool for deleting resources
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
protected boolean down(int button) {
	if (isActive()) {
		removeSelectedResources();
	}
	return false;
}

@Override
public void activate() {
	super.activate();
	removeSelectedResources();
}

/**
 * Removes all selected resources
 */
private void removeSelectedResources() {
	// Delete all selected resources
	if (!mSelection.isEmpty()) {
		ArrayList<IResource> copySelectedResources = new ArrayList<>();
		copySelectedResources.addAll(mSelection.getSelectedResources());

		mInvoker.execute(new CSelectionSet(mSelection));
		for (IResource resource : copySelectedResources) {
			mInvoker.execute(new CResourceRemove(resource, mEditor), true);
		}
	}
}

@Override
public boolean keyDown(int keycode) {
	if (KeyHelper.isDeletePressed(keycode)) {
		removeSelectedResources();
		return true;
	}
	return false;
}
}

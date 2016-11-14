package com.spiddekauga.voider.editor.commands;

import com.badlogic.gdx.utils.Disposable;
import com.spiddekauga.utils.commands.Command;
import com.spiddekauga.voider.editor.tools.ISelection;
import com.spiddekauga.voider.resources.IResource;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Sets the current selection
 */
public class CSelectionSet extends Command implements Disposable {
/** The resources to select */
private IResource[] mResources;
/** Old selection */
private ArrayList<IResource> mOldSelection = new ArrayList<>();
/** The selection */
private ISelection mSelection;

/**
 * Creates a command that will select an actor in the specified tool
 * @param selection the selection container
 * @param resources the resources to select
 */
public CSelectionSet(ISelection selection, IResource... resources) {
	mSelection = selection;
	mResources = resources;
}

/**
 * Creates a command that will select an actor in the specified tool
 * @param selection the selection container
 * @param resources the resources to select
 */
public CSelectionSet(ISelection selection, Collection<IResource> resources) {
	mSelection = selection;

	mResources = new IResource[resources.size()];
	resources.toArray(mResources);
}

@Override
public boolean execute() {
	if (mSelection != null) {
		ArrayList<IResource> oldSelection = mSelection.getSelectedResources();
		mOldSelection.addAll(oldSelection);
		mSelection.clearSelection();
		mSelection.selectResources(mResources);
		return true;
	} else {
		return false;
	}
}

@Override
public boolean undo() {
	if (mSelection != null) {
		mSelection.clearSelection();
		mSelection.selectResources(mOldSelection);
		return true;
	} else {
		return false;
	}
}
}

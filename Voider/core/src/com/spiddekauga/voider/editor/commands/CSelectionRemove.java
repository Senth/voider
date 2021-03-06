package com.spiddekauga.voider.editor.commands;

import com.spiddekauga.utils.commands.Command;
import com.spiddekauga.voider.editor.tools.ISelection;
import com.spiddekauga.voider.resources.IResource;

import java.util.Collection;

/**
 * Removes resources from the current selection
 */
public class CSelectionRemove extends Command {
/** The resources to select */
private IResource[] mResources;
/** The selection */
private ISelection mSelection;

/**
 * Creates a command that will remove the specified resources from the current selection
 * @param selection the selection to remove from
 * @param resources the resources to deselect
 */
public CSelectionRemove(ISelection selection, IResource... resources) {
	mSelection = selection;
	mResources = resources;
}

/**
 * Creates a command that will remove the specified resources from the current selection
 * @param selection the selection to remove from
 * @param resources the resources to deselect
 */
public CSelectionRemove(ISelection selection, Collection<IResource> resources) {
	mSelection = selection;

	mResources = new IResource[resources.size()];
	resources.toArray(mResources);
}

@Override
public boolean execute() {
	mSelection.deselectResources(mResources);
	return true;
}

@Override
public boolean undo() {
	mSelection.selectResources(mResources);
	return true;
}
}

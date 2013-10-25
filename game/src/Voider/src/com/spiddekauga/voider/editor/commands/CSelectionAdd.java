package com.spiddekauga.voider.editor.commands;

import java.util.ArrayList;

import com.spiddekauga.utils.Command;
import com.spiddekauga.voider.editor.tools.ISelection;
import com.spiddekauga.voider.resources.IResource;

/**
 * Adds resources to the current selection
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class CSelectionAdd extends Command {
	/**
	 * Creates a command that will add the specified resources to the current selection
	 * @param selection the selection to add to
	 * @param resources the resources to select
	 */
	public CSelectionAdd(ISelection selection, IResource... resources) {
		mSelection = selection;
		mResources = resources;
	}

	/**
	 * Creates a command that will add the specified resources to the current selection
	 * @param selection the selection to add to
	 * @param resources the resources to select
	 */
	public CSelectionAdd(ISelection selection, ArrayList<IResource> resources) {
		mSelection = selection;

		mResources = new IResource[resources.size()];
		resources.toArray(mResources);
	}

	@Override
	public boolean execute() {
		mSelection.selectResources(mResources);
		return true;
	}

	@Override
	public boolean undo() {
		mSelection.deselectResources(mResources);
		return true;
	}

	/** The resources to select */
	private IResource[] mResources;
	/** The selection */
	private ISelection mSelection;
}

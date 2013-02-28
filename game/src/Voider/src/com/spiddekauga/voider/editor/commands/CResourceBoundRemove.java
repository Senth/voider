package com.spiddekauga.voider.editor.commands;

import com.spiddekauga.utils.Command;
import com.spiddekauga.voider.resources.IResource;

/**
 * Removes a bound resource from the specified resource
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class CResourceBoundRemove extends Command {
	/**
	 * Creates a command that will remove a bound resource from another resource
	 * @param removeFrom the resource to remove from
	 * @param resourceToRemove the resource to remove
	 */
	public CResourceBoundRemove(IResource removeFrom, IResource resourceToRemove) {
		mRemoveFrom = removeFrom;
		mResourceToRemove = resourceToRemove;
	}

	@Override
	public boolean execute() {
		mRemoveFrom.removeBoundResource(mResourceToRemove);
		return true;
	}

	@Override
	public boolean undo() {
		mRemoveFrom.addBoundResource(mResourceToRemove);
		return true;
	}

	/** The resource to remove from */
	private IResource mRemoveFrom;
	/** The resource to remove */
	private IResource mResourceToRemove;
}

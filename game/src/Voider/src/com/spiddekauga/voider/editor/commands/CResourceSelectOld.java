package com.spiddekauga.voider.editor.commands;

import com.spiddekauga.utils.Command;
import com.spiddekauga.voider.editor.tools.ISelectTool;
import com.spiddekauga.voider.resources.IResource;

/**
 * Selects a resource in the specified select tool
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
@Deprecated
public class CResourceSelectOld extends Command {
	/**
	 * Creates a command that will select an actor in the specified tool
	 * @param resource the resource to select, if null it deselects any resource
	 * @param tool the tool to select the actor in
	 */
	public CResourceSelectOld(IResource resource, ISelectTool tool) {
		mTool = tool;
		mResource = resource;
	}

	@Override
	public boolean execute() {
		if (mTool != null) {
			mOldResource = mTool.getSelectedResource();
			mTool.setSelectedResource(mResource);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean undo() {
		if (mTool != null) {
			mTool.setSelectedResource(mOldResource);
			return true;
		} else {
			return false;
		}
	}

	/** The actor to select */
	public IResource mResource;
	/** Old selected actor */
	public IResource mOldResource = null;
	/** The tool to select the actor in */
	public ISelectTool mTool;
}

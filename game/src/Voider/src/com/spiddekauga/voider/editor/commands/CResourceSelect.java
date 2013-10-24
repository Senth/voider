package com.spiddekauga.voider.editor.commands;

import java.util.ArrayList;

import com.badlogic.gdx.utils.Disposable;
import com.spiddekauga.utils.Command;
import com.spiddekauga.voider.editor.tools.ISelection;
import com.spiddekauga.voider.resources.IResource;
import com.spiddekauga.voider.utils.Pools;

/**
 * 
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class CResourceSelect extends Command implements Disposable {
	/**
	 * Creates a command that will select an actor in the specified tool
	 * @param resource the resource to select, if null it deselects any resource
	 * @param selection the selection container
	 */
	public CResourceSelect(IResource resource, ISelection selection) {
		mSelection = selection;
		mResource = resource;
	}

	@Override
	public boolean execute() {
		if (mSelection != null) {
			ArrayList<IResource> oldSelection = mSelection.getSelectedResources();
			mOldSelection.addAll(oldSelection);

			mSelection.clearSelection();
			mSelection.addResource(mResource);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean undo() {
		if (mSelection != null) {
			mSelection.clearSelection();
			mSelection.addResources(mOldSelection);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void dispose() {
		Pools.arrayList.free(mOldSelection);
		mOldSelection = null;
	}

	/** The actor to select */
	private IResource mResource;
	/** Old selection */
	@SuppressWarnings("unchecked")
	private ArrayList<IResource> mOldSelection = Pools.arrayList.obtain();
	/** The selection */
	private ISelection mSelection;
}

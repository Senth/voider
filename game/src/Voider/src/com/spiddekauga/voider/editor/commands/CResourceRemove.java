package com.spiddekauga.voider.editor.commands;

import java.util.ArrayList;

import com.spiddekauga.utils.Command;
import com.spiddekauga.voider.editor.IResourceChangeEditor;
import com.spiddekauga.voider.resources.IResource;
import com.spiddekauga.voider.resources.IResourceBody;
import com.spiddekauga.voider.utils.Pools;

/**
 * Removes an resource from the specified resource editor
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class CResourceRemove extends Command {
	/**
	 * Creates a command which will remove the resource and notify the
	 * resource editor about it.
	 * @param resource the resource to remove
	 * @param editor the editor to remove the resource from
	 */
	public CResourceRemove(IResource resource, IResourceChangeEditor editor) {
		mResource = resource;
		mEditor = editor;

		//		// Save resources that uses the resource to be removed
		//		if (mEditor instanceof LevelEditor) {
		//			mBoundResources = Pools.arrayList.obtain();
		//
		//			((LevelEditor) mEditor).usesResource(resource, mBoundResources);
		//		}
	}

	@Override
	public boolean execute() {
		mEditor.onResourceRemoved(mResource);

		if (mResource instanceof IResourceBody) {
			((IResourceBody) mResource).destroyBody();
		}
		return true;
	}

	@Override
	public boolean undo() {
		if (mResource instanceof IResourceBody) {
			((IResourceBody) mResource).createBody();
		}

		mEditor.onResourceAdded(mResource);

		// Add bound resources once again
		if (mBoundResources != null) {
			for (IResource boundResource : mBoundResources) {
				boundResource.addBoundResource(mResource);
			}
		}

		return true;
	}

	@Override
	public void dispose() {
		Pools.arrayList.free(mBoundResources);
	}


	/** The resource to remove */
	private IResource mResource;
	/** The editor to remove the resource from */
	private IResourceChangeEditor mEditor;
	/** All resources that uses the removed resources, will be bound again
	 * after undo, only used if editor is a level editor */
	@Deprecated
	private ArrayList<IResource> mBoundResources = null;
}

package com.spiddekauga.voider.editor.commands;

import com.spiddekauga.utils.Command;
import com.spiddekauga.voider.editor.IResourceChangeEditor;
import com.spiddekauga.voider.game.IResourceBody;
import com.spiddekauga.voider.resources.IResource;

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
	}

	@Override
	public boolean execute() {
		mEditor.onResourceRemoved(mResource);



		//		mUsesBodyCorners = mResource.hasBodyCorners();
		//		if (mUsesBodyCorners) {
		//			mResource.destroyBodyCorners();
		//		}
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
		//		if (mUsesBodyCorners) {
		//			mResource.createBodyCorners();
		//		}
		mEditor.onResourceAdded(mResource);
		return true;
	}

	/** True if we shall create the body corners on undo */
	private boolean mUsesBodyCorners = false;
	/** The resource to remove */
	private IResource mResource;
	/** The editor to remove the resource from */
	private IResourceChangeEditor mEditor;
}

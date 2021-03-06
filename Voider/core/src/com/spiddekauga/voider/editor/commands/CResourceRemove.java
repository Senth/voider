package com.spiddekauga.voider.editor.commands;

import com.spiddekauga.utils.commands.Command;
import com.spiddekauga.voider.editor.IResourceChangeEditor;
import com.spiddekauga.voider.resources.IResource;
import com.spiddekauga.voider.resources.IResourceBody;

/**
 * Removes an resource from the specified resource editor
 */
public class CResourceRemove extends Command {
/** True if a body should be created on undo */
private boolean mCreateBody = false;
/** The resource to remove */
private IResource mResource;
/** The editor to remove the resource from */
private IResourceChangeEditor mEditor;

/**
 * Creates a command which will remove the resource and notify the resource editor about it.
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

	if (mResource instanceof IResourceBody) {
		IResourceBody resourceBody = (IResourceBody) mResource;
		mCreateBody = resourceBody.hasBody();
		if (mCreateBody) {
			resourceBody.destroyBody();
		}
	}
	return true;
}

@Override
public boolean undo() {
	if (mCreateBody) {
		if (mResource instanceof IResourceBody) {
			((IResourceBody) mResource).createBody();
		}
	}

	mEditor.onResourceAdded(mResource, false);

	return true;
}
}

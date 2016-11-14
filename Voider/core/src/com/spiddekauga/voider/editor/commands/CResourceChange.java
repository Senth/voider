package com.spiddekauga.voider.editor.commands;

import com.spiddekauga.utils.commands.Command;
import com.spiddekauga.voider.editor.IResourceChangeEditor;
import com.spiddekauga.voider.resources.IResource;

/**
 * Wrapper class for all commands that changes a resource
 */
public abstract class CResourceChange extends Command {
/** The resource that will be changed */
protected IResource mResource;
/** IResource editor to notify of the change */
private IResourceChangeEditor mResourceEditor;

/**
 * Sets the resource and the resource editor to send the change command to
 * @param resource the resource that will be changed
 * @param resourceEditor editor which will receive the #onIResourceChange(IResource) command.
 */
public CResourceChange(IResource resource, IResourceChangeEditor resourceEditor) {
	mResource = resource;
	mResourceEditor = resourceEditor;
}

/**
 * Sends an onIResourceChange(IResource) command to the resource editor
 */
protected void sendOnChange() {
	mResourceEditor.onResourceChanged(mResource);
}
}

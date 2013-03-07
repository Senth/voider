package com.spiddekauga.voider.editor.commands;

import com.spiddekauga.utils.Command;
import com.spiddekauga.voider.editor.IResourceChangeEditor;
import com.spiddekauga.voider.resources.IResource;
import com.spiddekauga.voider.resources.IResourceBody;

/**
 * Adds a new resource and calls the resource editor about the notification
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class CResourceAdd extends Command {
	/**
	 * Creates a command which will add and resource and notify the resource editor
	 * about it.
	 * @param resource the resource to add
	 * @param editor the editor to add the resource to
	 */
	public CResourceAdd(IResource resource, IResourceChangeEditor editor) {
		mResource = resource;
		mEditor = editor;
	}

	@Override
	public boolean execute() {
		if (mResource instanceof IResourceBody) {
			((IResourceBody) mResource).createBody();
		}
		mEditor.onResourceAdded(mResource);
		return true;
	}

	@Override
	public boolean undo() {
		mEditor.onResourceRemoved(mResource);
		if (mResource instanceof IResourceBody) {
			((IResourceBody) mResource).destroyBody();
		}
		return true;
	}

	/** The resource to add */
	private IResource mResource;
	/** The editor to add the resource to */
	private IResourceChangeEditor mEditor;
}

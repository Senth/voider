package com.spiddekauga.voider.editor;

import com.spiddekauga.voider.resources.IResource;


/**
 * Interface for all tools that can create and destroy resources
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public interface IResourceChangeEditor {
	/**
	 * Called when an resource is added
	 * @param resource the resource that was created
	 */
	void onResourceAdded(IResource resource);

	/**
	 * Called when an resource is removed
	 * @param resource the resource that was removed
	 */
	void onResourceRemoved(IResource resource);

	/**
	 * Called whenever an resource has been changed
	 * @param resource the resource that was changed
	 */
	void onResourceChanged(IResource resource);
}

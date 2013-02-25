package com.spiddekauga.voider.scene;

import com.spiddekauga.voider.resources.IResource;

/**
 * Interface for listening to when a tool selects/deselects an actor
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public interface ISelectListener {
	/**
	 * Called when an actor is selected
	 * @param deselectedResource the previous selected resource that will be deselected.
	 * null if no actor was previously selected
	 * @param selectedResource the selected resource, null if an actor was deselected
	 */
	void onResourceSelect(IResource deselectedResource, IResource selectedResource);
}

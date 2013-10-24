package com.spiddekauga.voider.editor.tools;

import com.spiddekauga.voider.resources.IResource;

/**
 * Interface for listening to when a tool selects/deselects an actor
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
@Deprecated
public interface ISelectListener {
	/**
	 * Called when an actor is selected
	 * @param deselectedResource the previous selected resource that will be deselected.
	 * null if no actor was previously selected
	 * @param selectedResource the selected resource, null if an actor was deselected
	 */
	void onResourceSelected(IResource deselectedResource, IResource selectedResource);
}

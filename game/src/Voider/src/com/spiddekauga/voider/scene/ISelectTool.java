package com.spiddekauga.voider.scene;

import java.util.List;

import com.spiddekauga.voider.resources.IResource;

/**
 * Tool that can select a resource, and thus add select listeners.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public interface ISelectTool {
	/**
	 * Adds one select listener
	 * @param listener called when an actor is selected
	 */
	void addListener(ISelectListener listener);

	/**
	 * Adds many select listeners
	 * @param listeners list of listeners to call when an actor is selected
	 */
	void addListeners(List<ISelectListener> listeners);

	/**
	 * Removes the specified listener. Does nothing if the listener hasn't been
	 * added.
	 * @param listener the listener to remove.
	 */
	void removeListener(ISelectListener listener);

	/**
	 * Removes all the specified listeners. If a listener in the list hasn't been
	 * added nothing will happen for that listener.
	 * @param listeners list of listeners to remove
	 */
	void removeListeners(List<ISelectListener> listeners);

	/**
	 * Selects the specified actor
	 * @param selectedResource the new resource to select
	 */
	void setSelectedResource(IResource selectedResource);

	/**
	 * @return the selected resource
	 */
	IResource getSelectedResource();
}

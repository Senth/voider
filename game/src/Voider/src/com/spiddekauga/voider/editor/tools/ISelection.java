package com.spiddekauga.voider.editor.tools;

import java.util.ArrayList;

import com.spiddekauga.voider.resources.IResource;

/**
 * Contains the current selection
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public interface ISelection {
	/**
	 * @return all selected resources
	 */
	public ArrayList<IResource> getSelectedResources();

	/**
	 * @return the most common type of selected resources
	 */
	public Class<? extends IResource> getMostCommonSelectedResourceType();

	/**
	 * @param <ResourceType> type of resource to get
	 * @param type what type of selected resources to get
	 * @return all selected resources of the specified type. Don't forget to free the arraylist
	 */
	public <ResourceType extends IResource> ArrayList<ResourceType> getSelectedResourcesOfType(Class<ResourceType> type);

	/**
	 * Delete selected actors
	 */
	public void deleteSelectedActors();

	/**
	 * Clears the selection, i.e. no resources will be selected
	 */
	public void clearSelection();
}

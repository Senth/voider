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
	 * @param resource the resource to check if it's selected
	 * @return true if the specified resource is selected
	 */
	boolean isSelected(IResource resource);
	/**
	 * @return all selected resources
	 */
	ArrayList<IResource> getSelectedResources();

	/**
	 * @return the most common type of selected resources, null if no resources are selected
	 */
	Class<? extends IResource> getMostCommonSelectedResourceType();

	/**
	 * @param <ResourceType> type of resource to get
	 * @param type what type of selected resources to get
	 * @return all selected resources of the specified type. Don't forget to free the arraylist
	 */
	<ResourceType extends IResource> ArrayList<ResourceType> getSelectedResourcesOfType(Class<ResourceType> type);

	/**
	 * Clears the selection, i.e. no resources will be selected
	 */
	void clearSelection();

	/**
	 * Gets the first resource of the specified type
	 * @param <ResourceType> type of resource to get
	 * @param type what type of selected resource to get
	 * @return first resource of the specified type, null if no resource of this type was found
	 */
	<ResourceType extends IResource> ResourceType getFirstSelectedResourceOfType(Class<ResourceType> type);

	/**
	 * Adds a resource to the selection
	 * @param resource the resource to add to the selection
	 */
	void selectResource(IResource resource);

	/**
	 * Adds several resources to the selection
	 * @param resources all resources to add to the current selection
	 */
	void selectResources(ArrayList<IResource> resources);

	/**
	 * Adds several resource to the selection
	 * @param resources all resources to add to the current selection
	 */
	void selectResources(IResource[] resources);

	/**
	 * Removes a resource from the selection
	 * @param resource the resource to remove from the selection
	 */
	void deselectResource(IResource resource);

	/**
	 * Removes the resources from the selection
	 * @param resources all resources to deselect. Resources inside this array
	 * that aren't currently selected are just ignored
	 */
	void deselectResources(ArrayList<IResource> resources);

	/**
	 * Removes the resources from the selection
	 * @param resources all resources to deselect. Resources inside this array
	 * that aren't currently selected are just ignored
	 */
	void deselectResources(IResource[] resources);

	/**
	 * Adds a listener to the selection. This listener is called when a resources is
	 * selected or deselected
	 * @param listener the listener to add
	 */
	void addListener(ISelectionListener listener);

	/**
	 * Removes a listener from the selection. This listener was called when a resource was
	 * selected or deselected
	 * @param listener the listener to remove
	 */
	void removeListener(ISelectionListener listener);

	/**
	 * @return true if no resources are selected
	 */
	boolean isEmpty();

	/**
	 * @return the total number of selected resources
	 */
	int getSize();
}

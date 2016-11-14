package com.spiddekauga.voider.editor.tools;

import com.spiddekauga.voider.resources.IResource;

import java.util.ArrayList;

/**
 * Contains the current selection
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
 * @param resources all resources to deselect. Resources inside this array that aren't currently
 * selected are just ignored
 */
void deselectResources(ArrayList<IResource> resources);

/**
 * Removes the resources from the selection
 * @param resources all resources to deselect. Resources inside this array that aren't currently
 * selected are just ignored
 */
void deselectResources(IResource[] resources);

/**
 * Adds a listener to the selection. This listener is called when a resources is selected or
 * deselected
 * @param listener the listener to add
 */
void addListener(ISelectionListener listener);

/**
 * Removes a listener from the selection. This listener was called when a resource was selected or
 * deselected
 * @param listener the listener to remove
 */
void removeListener(ISelectionListener listener);

/**
 * @return true if the selection was changed during the last down() event
 */
boolean isSelectionChangedDuringDown();

/**
 * Sets if the selection was changed during a touch down() event. This should always be called as
 * setSelectionChangeDuringDown(false) after the up() event
 * @param changed set to true if this was changed
 */
void setSelectionChangedDuringDown(boolean changed);

/**
 * @return true if no resources are selected
 */
boolean isEmpty();

/**
 * @param type the type to check if it's selected
 * @return true if at least one of the specified resource type is selected
 */
boolean isSelected(Class<? extends IResource> type);

/**
 * @return the total number of selected resources
 */
int getSize();

/**
 * Sets whether the selected resource should be set as selected (if the resource is an instance of
 * IResourceSelectable)
 * @param setAsSelected true if the resource should be set as selected
 */
void setAsSelectedOnSelection(boolean setAsSelected);

/**
 * @return true if resources of type IResourceSelectable should be set as selected when the resource
 * is selected.
 */
boolean isSetAsSelectedOnSelection();
}

package com.spiddekauga.voider.editor.tools;

import com.spiddekauga.voider.resources.IResource;

/**
 * Listens to selection changes
 */
public interface ISelectionListener {
/**
 * Called when a resource is selected
 * @param resource the selected resource
 */
void onResourceSelected(IResource resource);

/**
 * Called when a resource is deselected
 * @param resource the deselected resource
 */
void onResourceDeselected(IResource resource);
}

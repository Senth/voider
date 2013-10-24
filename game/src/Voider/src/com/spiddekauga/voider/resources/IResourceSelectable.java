package com.spiddekauga.voider.resources;

/**
 * Implement this if the resource is selectable
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public interface IResourceSelectable {
	/**
	 * Sets if the resource is currently selected. This will probably make it draw
	 * differently when in the editor.
	 * @param selected true if the resource is selected
	 */
	void setSelected(boolean selected);

	/**
	 * @return true if the resources is currently selected
	 */
	boolean isSelected();
}

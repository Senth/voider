package com.spiddekauga.voider.resources;

/**
 * The resource has revision numbering. This will allow the resource
 * to save past revisions
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public interface IResourceRevision extends IResource {
	/**
	 * @return current revision of the resource
	 */
	int getRevision();

	/**
	 * Sets the revision of the resource
	 * @param revision new revision of the resource
	 */
	void setRevision(int revision);
}

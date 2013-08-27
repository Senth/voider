package com.spiddekauga.voider.resources;

/**
 * The resource has revision numbering. This will allow the resource
 * to save past revisions
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public interface IResourceRevision {
	/**
	 * @return current revision of the resource
	 */
	long getRevision();
}

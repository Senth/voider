package com.spiddekauga.voider.resources;

/**
 * Resource has PNG image
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public interface IResourcePng extends IResource {
	/**
	 * Sets the PNG image for this resource.
	 * @param pngBytes bytes for the PNG image
	 */
	public void setPngImage(byte[] pngBytes);

	/**
	 * @return the PNG image of the resource
	 */
	public byte[] getPngImage();
}

package com.spiddekauga.voider.resources;

/**
 * All static resources. Name and a corresponding filename
 * This includes:
 * \li Textures
 * \li Music
 * \li Sound
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public enum ResourceNames {
	/** Texture player */
	TEXTURE_PLAYER("player");

	/**
	 * Initializes the enum with a filename
	 * @param filename the filename (not full path)
	 */
	private ResourceNames(String filename) {
		this.filename = filename;
	}

	/** Filename of the resource */
	public final String filename;
}

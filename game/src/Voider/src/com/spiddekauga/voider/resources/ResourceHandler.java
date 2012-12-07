package com.spiddekauga.voider.resources;


/**
 * Saves and loads all resources from files. This class takes care of
 * where files are located and loads them on demand.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class ResourceHandler {
	/**
	 * Loads all files of the specified type
	 * @param <T> class type to return
	 * @param type the type of resource to load
	 * @return all resources of that type
	 */
	public static <T> T[] loadAllOf(Class<T> type) {

		return null;
	}

	/**
	 * Loads a resources of static type. Usually those in internal assets,
	 * such as textures, music, etc.
	 * @param <T> Template for the type
	 * @param resourceName the name of the resource to load
	 * @param type the class type the resource has, e.g.
	 * Texture, Music, etc.
	 */
	public static <T> void load(ResourceNames resourceName, Class<T> type) {

	}


	/**
	 * Private constructor to enforce singleton pattern.
	 */
	private ResourceHandler() {}

	/** Instance of class */
	public static ResourceHandler mInstance;
}

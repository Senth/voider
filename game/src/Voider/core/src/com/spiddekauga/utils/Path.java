package com.spiddekauga.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * Various path help methods
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class Path {
	/**
	 * @return the executable directory
	 */
	public static String getExecDir() {
		String encodedPath = Path.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		String path = null;
		try {
			path = URLDecoder.decode(encodedPath, "UTF-8");

			// Remove jar if one exists
			if (path.contains(".jar")) {
				int directoryIndex = path.lastIndexOf("/");
				if (directoryIndex == -1) {
					directoryIndex = path.lastIndexOf("\\");
				}
				directoryIndex += 1;

				path = path.substring(0, directoryIndex);
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return path;
	}

}

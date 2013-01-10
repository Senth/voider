package com.spiddekauga.utils;

import java.io.File;

import com.badlogic.gdx.tools.imagepacker.TexturePacker2;

/**
 * Packs all the textures in a specified directory to seperate atlases.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class Main {
	/** UI directory where all the sub-ui directories are located */
	private static final String UI_PARENT_DIR = "../../ui";
	/** Output directory where all the atlases should be stored */
	private static final String ATLAS_OUTPUT_DIR = "../Voider-android/assets/ui";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		File folder = new File(UI_PARENT_DIR);
		if (folder.exists()) {
			File[] listOfFiles = folder.listFiles();

			for (int i = 0; i < listOfFiles.length; i++)
			{
				if (listOfFiles[i].isDirectory())
				{
					String filename = listOfFiles[i].getName();
					String fullPath = listOfFiles[i].getAbsolutePath();
					TexturePacker2.processIfModified(fullPath, ATLAS_OUTPUT_DIR, filename);
				}
			}
		}
	}

}

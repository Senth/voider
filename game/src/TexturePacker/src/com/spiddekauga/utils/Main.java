package com.spiddekauga.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.badlogic.gdx.tools.imagepacker.TexturePacker2;
import com.esotericsoftware.minlog.Log;
import com.esotericsoftware.minlog.Log.Logger;

/**
 * Packs all the textures in a specified directory to seperate atlases.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class Main {
	/** Set to true to compile packer for dropbox */
	private static final boolean DROPBOX_PACKER = false;
	/** UI directory where all the sub-ui directories are located */
	private static final String UI_PARENT_DIR = DROPBOX_PACKER ? "ui-editable" : "../../ui";
	/** Output directory where all the atlases should be stored */
	private static final String ATLAS_OUTPUT_DIR = DROPBOX_PACKER ? "ui" : "../Voider-android/assets/ui";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String path = getExecDir();

		Log.setLogger(new FileLogger(path));
		Log.NONE();
		Log.debug(path + "\n\n");

		String imagesDir = DROPBOX_PACKER ? path + UI_PARENT_DIR : UI_PARENT_DIR;
		String atlasOutDir = DROPBOX_PACKER ? path + ATLAS_OUTPUT_DIR : ATLAS_OUTPUT_DIR;
		Log.debug("Images dir: " + imagesDir);
		Log.debug("Atlas dir: " + atlasOutDir);


		File folder = new File(imagesDir);
		if (folder.exists()) {
			File[] listOfFiles = folder.listFiles();

			for (int i = 0; i < listOfFiles.length; i++)
			{
				if (listOfFiles[i].isDirectory())
				{
					String filename = listOfFiles[i].getName();
					String fullPath = listOfFiles[i].getAbsolutePath();
					TexturePacker2.processIfModified(fullPath, atlasOutDir, filename);
				}
			}
		}
	}

	/**
	 * @return path of the current directory
	 */
	static private String getExecDir() {
		String encodedPath = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		String path = null;
		try {
			path = URLDecoder.decode(encodedPath, "UTF-8");

			int directoryIndex = path.lastIndexOf("/");
			if (directoryIndex == -1) {
				directoryIndex = path.lastIndexOf("\\");
			}
			directoryIndex += 1;

			path = path.substring(0, directoryIndex);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return path;
	}

	/**
	 * File logger
	 * 
	 * 
	 * @author Matteus Magnusson <senth.wallace@gmail.com>
	 */
	static public class FileLogger extends Logger {
		@Override
		public void log (int level, String category, String message, Throwable ex) {
			StringBuilder builder = new StringBuilder(256);
			builder.append(mDateFormat.format(new Date()));
			builder.append(' ');
			builder.append(level);
			builder.append('[');
			builder.append(category);
			builder.append("] ");
			builder.append(message);
			if (ex != null) {
				StringWriter writer = new StringWriter(256);
				ex.printStackTrace(new PrintWriter(writer));
				builder.append('\n');
				builder.append(writer.toString().trim());
			}


			mFileOut.println(builder.toString());
			mFileOut.flush();
		}

		/**
		 * Creates a new log file for this directory
		 * @param directory the directory to create the log in
		 */
		public FileLogger(String directory) {
			Date date = new Date();
			String logFileString = directory + "log-" + mDateFormat.format(date) + ".log";

			try {
				mFileOut = new PrintWriter(logFileString, "UTF-8");
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		/** Dateformat */
		DateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		/** File output */
		PrintWriter mFileOut = null;
	}
}

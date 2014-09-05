package com.spiddekauga.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.JOptionPane;

import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.tools.imagepacker.TexturePacker2;
import com.badlogic.gdx.tools.imagepacker.TexturePacker2.Settings;
import com.esotericsoftware.minlog.Log;
import com.esotericsoftware.minlog.Log.Logger;

/**
 * Packs all the textures in a specified directory to seperate atlases.
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class Main {
	/** Set to true to compile packer for dropbox */
	private static final boolean DROPBOX_PACKER = false;
	/** UI directory where all the sub-ui directories are located */
	private static final String UI_PNG_DIR = DROPBOX_PACKER ? "ui-editable" : "ui-editable";
	/** Output directory where all the atlases should be stored */
	private static final String UI_ATLAS_DIR = DROPBOX_PACKER ? "ui" : "ui";
	/** Theme directory where all the theme png files are located */
	private static final String THEME_PNG_DIR = DROPBOX_PACKER ? "themes-editable" : "themes-editable";
	/** Theme atlas output directory */
	private static final String THEME_ATLAS_DIR = DROPBOX_PACKER ? "themes" : "themes";

	/** All directories to pack */
	private static ArrayList<TextureInOutWrapper> mDirs = new ArrayList<Main.TextureInOutWrapper>();

	/**
	 * Wrapper class for texture input and output directories
	 */
	private static class TextureInOutWrapper {
		/**
		 * Sets the input and output directory
		 * @param inputDir where png-files are kept
		 * @param outputDir where atlases are output to
		 */
		TextureInOutWrapper(String inputDir, String outputDir) {
			this.inputDir = inputDir;
			this.outputDir = outputDir;
		}

		/** The input directory (where png-files are kept) */
		final String inputDir;
		/** The output directory (where atlases are output to */
		final String outputDir;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		initPackDirs();

		String path = getExecDir();

		// Log.setLogger(new FileLogger(path));
		Log.DEBUG();
		Log.debug(path + "\n\n");

		ArrayList<Thread> threads = new ArrayList<Thread>();

		for (TextureInOutWrapper textureInOutWrapper : mDirs) {
			String imagesDir = "";
			String atlasDir = "";
			if (DROPBOX_PACKER) {
				imagesDir = path;
				atlasDir = path;
			}

			imagesDir += textureInOutWrapper.inputDir;
			atlasDir += textureInOutWrapper.outputDir;

			Log.debug("Images dir: " + imagesDir);
			Log.debug("Atlas dir: " + atlasDir);


			File folder = new File(imagesDir);
			if (folder.exists()) {
				File[] listOfFiles = folder.listFiles();

				for (int i = 0; i < listOfFiles.length; i++) {
					if (listOfFiles[i].isDirectory()) {
						String filename = listOfFiles[i].getName();
						String fullPath = listOfFiles[i].getAbsolutePath();
						PackThread packThread = new PackThread(fullPath, atlasDir, filename);
						packThread.start();
						threads.add(packThread);
					}
				}
			}
		}

		while (!threads.isEmpty()) {
			int lastIndex = threads.size() - 1;
			Thread thread = threads.get(lastIndex);

			if (!thread.isAlive()) {
				threads.remove(lastIndex);
			}
		}

		if (DROPBOX_PACKER) {
			JOptionPane.showMessageDialog(null, "All images have been packed!", "Done!", JOptionPane.INFORMATION_MESSAGE);
		}
	}

	/**
	 * Initializes the directory to pack
	 */
	static private void initPackDirs() {
		mDirs.add(new TextureInOutWrapper(UI_PNG_DIR, UI_ATLAS_DIR));
		mDirs.add(new TextureInOutWrapper(THEME_PNG_DIR, THEME_ATLAS_DIR));
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
	 * Runnable thread
	 */
	@SuppressWarnings("javadoc")
	private static class PackThread extends Thread {
		PackThread(String inputDir, String outputDir, String name) {
			mInputDir = inputDir;
			mOutputDir = outputDir;
			mName = name;
		}

		@Override
		public void run() {
			TexturePacker2.processIfModified(mSettings, mInputDir, mOutputDir, mName);
		};

		String mInputDir;
		String mOutputDir;
		String mName;
	}

	/** Texture packer settings */
	private static Settings mSettings = new Settings();

	static {
		mSettings.alias = true;
		mSettings.maxHeight = 4096;
		mSettings.maxWidth = 4096;
		mSettings.format = Format.RGBA8888;
	}

	/**
	 * File logger
	 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
	 */
	static public class FileLogger extends Logger {
		@Override
		public void log(int level, String category, String message, Throwable ex) {
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
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}

		/** Dateformat */
		DateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		/** File output */
		PrintWriter mFileOut = null;
	}
}

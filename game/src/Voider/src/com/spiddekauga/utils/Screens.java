package com.spiddekauga.utils;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;

/**
 * Screen utilities
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class Screens {
	/**
	 * Saves a screenshot to the specified file
	 * @param file where to save the screen shot
	 */
	public static void saveScreenshot(FileHandle file) {
		byte[] bytes = getScreenshotInPng(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);

		boolean append = false;
		file.writeBytes(bytes, append);
	}

	/**
	 * Gets a screenshot of the file
	 * @param x position on the screen
	 * @param y position on the screen
	 * @param width width
	 * @param height height
	 * @param flipY if we shall flip Y, use sparingly as it takes up quite a lot of
	 * working memory.
	 * @return PNG image in bytes
	 */
	public static byte[] getScreenshotInPng(int x, int y, int width, int height, boolean flipY) {
		Pixmap pixmap = getScreenshot(x, y, width, height, true);
		byte[] bytes;

		try {
			bytes = PngExport.toPNG(pixmap);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		return bytes;
	}

	/**
	 * Gets a screenshot of the file
	 * @param x position on the screen
	 * @param y position on the screen
	 * @param width width
	 * @param height height
	 * @param flipY if we shall flip Y, use sparingly as it takes up quite a lot of
	 * working memory.
	 * @return pixmap
	 */
	public static Pixmap getScreenshot(int x, int y, int width, int height, boolean flipY) {
		Gdx.gl.glPixelStorei(GL20.GL_PACK_ALIGNMENT, 1);

		final Pixmap pixmap = new Pixmap(width, height, Format.RGBA8888);
		ByteBuffer pixels = pixmap.getPixels();
		Gdx.gl.glReadPixels(x, y, width, height, GL20.GL_RGBA, GL20.GL_UNSIGNED_BYTE, pixels);

		final int numBytes = width * height * 4;
		byte[] lines = new byte[numBytes];
		if (flipY) {
			final int numBytesPerLine = width * 4;
			for (int i = 0; i < height; i++) {
				pixels.position((height - i - 1) * numBytesPerLine);
				pixels.get(lines, i * numBytesPerLine, numBytesPerLine);
			}
			pixels.clear();
			pixels.put(lines);
		} else {
			pixels.clear();
			pixels.get(lines);
		}

		return pixmap;
	}
}

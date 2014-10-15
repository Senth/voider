package com.spiddekauga.utils;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.math.Vector2;
import com.spiddekauga.voider.utils.Pools;

/**
 * Screen utilities
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
	 *        working memory.
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
	 *        working memory.
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
			// pixels.clear();
			// pixels.get(lines);
		}

		return pixmap;
	}

	/**
	 * Clamp the current camera position to min/max coordinates
	 * @param camera
	 * @param min minimum position
	 * @param max maximum position
	 * @return true if clamped
	 */
	public static boolean clampCamera(OrthographicCamera camera, Vector2 min, Vector2 max) {
		Vector2 cameraPos = new Vector2(camera.position.x, camera.position.y);
		boolean clamped = clampCamera(camera, min, max, cameraPos, camera.zoom);
		camera.position.x = cameraPos.x;
		camera.position.y = cameraPos.y;
		return clamped;
	}

	/**
	 * Clamp the specific camera position depending on custom position and zoom. I.e.
	 * neither uses the camera's position nor the camera's zoom.
	 * @param camera
	 * @param min minimum position
	 * @param max maximum position
	 * @param cameraPos position of the camera
	 * @param zoom current zoom
	 * @return true if clamped
	 */
	public static boolean clampCamera(OrthographicCamera camera, Vector2 min, Vector2 max, Vector2 cameraPos, float zoom) {
		float widthHalf = camera.viewportWidth * zoom * 0.5f;
		float heightHalf = camera.viewportHeight * zoom * 0.5f;

		Vector2 cameraMin = Pools.vector2.obtain();
		cameraMin.set(cameraPos).sub(widthHalf, heightHalf);
		Vector2 cameraMax = Pools.vector2.obtain();
		cameraMax.set(cameraPos).add(widthHalf, heightHalf);

		boolean clamped = false;

		// Clamp X
		// Both are out of bounds -> Center
		if (cameraMin.x < min.x && cameraMax.x > max.x) {
			cameraPos.x = (min.x + max.x) / 2;
			clamped = true;
		}
		// Left out of bounds
		else if (cameraMin.x < min.x) {
			cameraPos.x = min.x + widthHalf;
			clamped = true;
		}
		// Right out of bounds
		else if (cameraMax.x > max.x) {
			cameraPos.x = max.x - widthHalf;
			clamped = true;
		}

		// Clamp Y
		// Both are out of bound -> Center
		if (cameraMin.y < min.y && cameraMax.y > max.y) {
			cameraPos.y = (min.y + max.y) / 2;
			clamped = true;
		}
		// Top out of bounds
		else if (cameraMin.y < min.y) {
			cameraPos.y = min.y + heightHalf;
			clamped = true;
		}
		// Bottom out of bounds
		else if (cameraMax.y > max.y) {
			cameraPos.y = max.y - heightHalf;
			clamped = true;
		}

		return clamped;
	}
}

package com.spiddekauga.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.Vector2;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.config.ConfigIni;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Screen utilities
 */
public class Screens {
private static SimpleDateFormat mFileTimestampFormat = null;

/**
 * Saves a screenshot into the user's directory
 */
public static void saveScreenshot() {
	String screenshotDirPath = Config.File.getScreenshotStorage();
	FileHandle screenshotDir = Gdx.files.external(screenshotDirPath);
	if (!screenshotDir.exists()) {
		screenshotDir.mkdirs();
	}

	if (mFileTimestampFormat == null) {
		String timestampFormat = ConfigIni.getInstance().setting.general.getFileTimestampFormat();
		mFileTimestampFormat = new SimpleDateFormat(timestampFormat, Locale.ENGLISH);
	}

	String timestamp = mFileTimestampFormat.format(new Date());
	String screenshotName = "Voider Screenshot - " + timestamp + ".png";
	FileHandle screenshotFile = screenshotDir.child(screenshotName);
	saveScreenshot(screenshotFile);
}

/**
 * Saves a screenshot to the specified file
 * @param file where to save the screen shot
 */
public static void saveScreenshot(FileHandle file) {
	byte[] bytes = getScreenshotInPng(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
	file.writeBytes(bytes, false);
}

/**
 * Gets a screenshot of the file
 * @param x position on the screen
 * @param y position on the screen
 * @param width width
 * @param height height
 * @param flipY if we shall flip Y, use sparingly as it takes up quite a lot of working memory.
 * @return PNG image in bytes
 */
public static byte[] getScreenshotInPng(int x, int y, int width, int height, boolean flipY) {
	Pixmap pixmap = getScreenshot(x, y, width, height, flipY);
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
 * @param flipY if we shall flip Y, use sparingly as it takes up quite a lot of working memory.
 * @return pixmap
 */
public static Pixmap getScreenshot(int x, int y, int width, int height, boolean flipY) {
	Gdx.gl.glPixelStorei(GL20.GL_PACK_ALIGNMENT, 1);

	final Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGB888);
	ByteBuffer pixels = pixmap.getPixels();
	Gdx.gl.glReadPixels(x, y, width, height, GL20.GL_RGB, GL20.GL_UNSIGNED_BYTE, pixels);

	final int numBytes = width * height * 3;
	byte[] lines = new byte[numBytes];
	if (flipY) {
		final int numBytesPerLine = width * 3;
		for (int i = 0; i < height; i++) {
			pixels.position((height - i - 1) * numBytesPerLine);
			pixels.get(lines, i * numBytesPerLine, numBytesPerLine);
		}
		pixels.clear();
		pixels.put(lines);
	}

	return pixmap;
}

/**
 * Clamp the current camera position to min/max coordinates
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
 * Clamp the specific camera position depending on custom position and zoom. I.e. neither uses the
 * camera's position nor the camera's zoom.
 * @param min minimum position
 * @param max maximum position
 * @param cameraPos position of the camera
 * @param zoom current zoom
 * @return true if clamped
 */
public static boolean clampCamera(OrthographicCamera camera, Vector2 min, Vector2 max, Vector2 cameraPos, float zoom) {
	float widthHalf = camera.viewportWidth * zoom * 0.5f;
	float heightHalf = camera.viewportHeight * zoom * 0.5f;

	Vector2 cameraMin = new Vector2(cameraPos).sub(widthHalf, heightHalf);
	Vector2 cameraMax = new Vector2(cameraPos).add(widthHalf, heightHalf);

	float widthMax = max.x - min.x;
	float heightMax = max.y - min.y;

	boolean clamped = false;

	// Clamp X
	// Width is too great -> Center
	// Both are out of bounds -> Center
	if ((widthMax <= cameraMax.x - cameraMin.x) || (cameraMin.x < min.x && cameraMax.x > max.x)) {
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
	// Height is too great -> Center
	// Both are out of bound -> Center
	if ((heightMax <= cameraMax.y - cameraMin.y) || (cameraMin.y < min.y && cameraMax.y > max.y)) {
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

package com.spiddekauga.utils.scene.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

/**
 * Extension of the image that create a background image from a color
 */
public class Background extends Image {
/**
 * Creates a background image of the specified color
 * @param color the color of the background
 */
public Background(Color color) {
	this(color, 1, 1, 0, 0);
}

/**
 * Creates a background image of the specified color
 * @param color the color of the background
 * @param width width of the background
 * @param height height of the background
 * @param x horizontal position of the background
 * @param y vertical position of the background
 */
public Background(Color color, float width, float height, float x, float y) {
	Pixmap pixmap = new Pixmap(1, 1, Format.RGBA8888);
	pixmap.drawPixel(0, 0, colorToRgbaInt(color));
	Texture texture = new Texture(pixmap);
	pixmap.dispose();

	setDrawable(new TextureRegionDrawable(new TextureRegion(texture)));
	setHeight(height);
	setWidth(width);
	setPosition(x, y);
}

/**
 * Converts a color class to a RGBA integer
 * @param color the color to convert to RGBA integer
 * @return RGBA integer of the color
 */
private static int colorToRgbaInt(Color color) {
	int argbColor = color.toIntBits();
	int rgbaColor = (argbColor << 8) | (argbColor >>> (32 - 8));
	return rgbaColor;
}

/**
 * Creates a background image of the specified color
 * @param color the color of the background
 * @param width width of the background
 * @param height height of the background
 */
public Background(Color color, float width, float height) {
	this(color, width, height, 0, 0);
}

@Override
public float getPrefWidth() {
	return getWidth();
}

@Override
public float getPrefHeight() {
	return getHeight();
}
}

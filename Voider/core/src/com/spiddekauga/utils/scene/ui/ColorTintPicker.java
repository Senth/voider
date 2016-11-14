package com.spiddekauga.utils.scene.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Blending;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector4;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * Slider for picking a tint of a color
 */
public class ColorTintPicker extends Slider implements Disposable {
/** True if vertical bar instead of horizontal */
protected final boolean mVertical;
private Color mColor = new Color();
/** All colors to pick between */
private Color[] mColorIntervals = null;
private Vector4[] mVectors = null;
/** Length of each color interval */
private float mIntervalLength;

/**
 * Creates a new color tint picker
 * @param vertical true if it should be vertical
 * @param style slider style with optional knob. The background will be the color
 * @param colors all the color intervals to use, should be at least 2 in length
 */
public ColorTintPicker(boolean vertical, SliderStyle style, Color... colors) {
	super(0, 1, 0.0039f, vertical, new SliderStyle(style));

	if (colors == null) {
		throw new GdxRuntimeException("colors is null");
	}
	if (colors.length < 2) {
		throw new GdxRuntimeException("number of colors to pick between are less than 2");
	}

	mVertical = vertical;
	mColorIntervals = colors;
	mIntervalLength = 1f / (mColorIntervals.length - 1);

	mVectors = new Vector4[colors.length];
	for (int i = 0; i < mVectors.length; ++i) {
		Color color = mColorIntervals[i];
		mVectors[i] = new Vector4(color.r, color.g, color.b, color.a);
	}

	mColor.set(colors[0]);

	updateBackground();
}

/**
 * Create/Update background color
 */
private void updateBackground() {
	// Get right length of the pixmap
	int length;
	int height;
	if (mVertical) {
		length = (int) getHeight();
		height = (int) getWidth();
	} else {
		length = (int) getWidth();
		height = (int) getHeight();
	}
	Pixmap.setBlending(Blending.None);
	Pixmap pixmap = new Pixmap(length, 1, Format.RGBA8888);
	pixmap.setColor(0xff0000ff);

	// Calculate lerp step between pixels
	float lerpStepSize = 1f / (length - 1);
	setStepSize(lerpStepSize);
	lerpStepSize /= mIntervalLength;

	// Color the pixmap accordingly
	Color lerpColor = new Color();
	int pixelsPerInterval = (int) ((length * mIntervalLength) + 0.5f);
	int step = 0;
	int index = 0;
	for (int x = 0; x < length; ++x) {
		if (step == pixelsPerInterval) {
			step = 0;
			index++;
		}

		Color fromColor = mColorIntervals[index];
		Color toColor = mColorIntervals[index + 1];

		lerpColor.set(fromColor).lerp(toColor, lerpStepSize * step);
		pixmap.drawPixel(x, 0, Color.rgba8888(lerpColor));

		step++;
	}

	// Dispose old drawable
	dispose();

	// Create and use texture
	Texture texture = new Texture(pixmap);
	pixmap.dispose();
	TextureRegion textureRegion = new TextureRegion(texture);
	textureRegion.setRegionHeight(height);
	getStyle().background = new TextureRegionDrawable(textureRegion);
}

@Override
public void dispose() {
	if (getStyle().background != null) {
		TextureRegionDrawable oldDrawable = (TextureRegionDrawable) getStyle().background;
		oldDrawable.getRegion().getTexture().dispose();
	}
}

/**
 * @return current color
 */
public Color getPickColor() {
	return mColor;
}

/**
 * Set current color. This clamps to a valid value
 * @param color new color
 */
public void setPickColor(Color color) {
	// Project onto the color line
	Vector4 originPoint = new Vector4(color.r, color.g, color.b, color.a);
	Vector4 closestPoint = new Vector4();
	Vector4 projPoint = new Vector4();
	Vector4 distVector = new Vector4();
	float closestDist2 = Float.MAX_VALUE;
	int closestIndex = -1;
	for (int i = 0; i < mVectors.length - 1; ++i) {
		projPoint.set(originPoint);
		projPoint.prj(mVectors[i], mVectors[i + 1]);

		distVector.set(projPoint).sub(originPoint);
		float dist2 = distVector.len2();

		if (dist2 < closestDist2) {
			closestDist2 = dist2;
			closestPoint.set(projPoint);
			closestIndex = i;
		}
	}

	mColor.set(closestPoint.x, closestPoint.y, closestPoint.z, closestPoint.w);


	// Calculate set value position
	float indexOffsetLength = closestIndex * mIntervalLength;

	float normalizeValue = new Vector4(mVectors[closestIndex]).sub(mVectors[closestIndex + 1]).len();
	normalizeValue /= mIntervalLength;
	Vector4 diff = new Vector4(mVectors[closestIndex]);
	diff.sub(closestPoint);
	float offsetLength = diff.len() / normalizeValue;

	super.setValue(offsetLength + indexOffsetLength);
}

@Override
public boolean setValue(float value) {
	boolean changed = super.setValue(value);

	if (changed) {
		if (value < 1f) {
			int index = (int) (value / mIntervalLength);
			float lerpValue = getValue();
			lerpValue -= mIntervalLength * index;
			lerpValue /= mIntervalLength;
			mColor.set(mColorIntervals[index]).lerp(mColorIntervals[index + 1], lerpValue);
		} else {
			mColor.set(mColorIntervals[mColorIntervals.length - 1]);
		}
	}

	return changed;
}

@Override
public float getPrefWidth() {
	if (mVertical) {
		final Drawable knob = (isDisabled() && getStyle().disabledKnob != null) ? getStyle().disabledKnob : getStyle().knob;
		return Math.max(knob == null ? 0 : knob.getMinWidth(), getWidth());
	} else {
		return 140;
	}
}

@Override
public float getPrefHeight() {
	if (mVertical) {
		return 140;
	} else {
		final Drawable knob = (isDisabled() && getStyle().disabledKnob != null) ? getStyle().disabledKnob : getStyle().knob;
		return Math.max(knob == null ? 0 : knob.getMinHeight(), getHeight());
	}
}

@Override
public void setWidth(float width) {
	float oldWidth = getWidth();
	super.setWidth(width);

	if (!mVertical && oldWidth != width) {
		updateBackground();
	}
}

@Override
public void setHeight(float height) {
	float oldHegiht = getHeight();
	super.setHeight(height);

	if (mVertical && oldHegiht != height) {
		updateBackground();
	}
}
}

package com.spiddekauga.utils.scene.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector4;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;

/**
 * Slider for picking a tint of a color
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class ColorTintPicker extends Slider {
	/**
	 * Creates a new color tint picker
	 * @param fromColor pick from this color
	 * @param toColor pick to this color
	 * @param horizontal true if it should be horizontal
	 * @param style slider style with optional knob. The background will be the color
	 */
	public ColorTintPicker(Color fromColor, Color toColor, boolean horizontal, SliderStyle style) {
		super(0, 1, 0.0039f, horizontal, new SliderStyle(style));

		mFromColor = fromColor;
		mToColor = toColor;

		mFromVec = new Vector4(fromColor.r, fromColor.g, fromColor.b, fromColor.a);
		mToVec = new Vector4(toColor.r, toColor.g, toColor.b, toColor.a);
		mColor.set(mFromColor);

		updateBackground();
	}

	/**
	 * Set current color. This clamps to a valid value
	 * @param color new color
	 */
	@Override
	public void setColor(Color color) {
		Vector4 point = new Vector4(color.r, color.g, color.b, color.a);
		point.prj(mFromVec, mToVec);
		mColor.set(point.x, point.y, point.z, point.w);

		float len = point.sub(mFromVec).len();

		super.setValue(len);
	}

	/**
	 * @return current color
	 */
	@Override
	public Color getColor() {
		return mColor;
	}

	@Override
	public boolean setValue(float value) {
		mColor.set(mFromColor).lerp(mToColor, value);

		return super.setValue(value);
	}

	/**
	 * Create/Update background color
	 */
	private void updateBackground() {
		// TODO
	}

	private Color mColor = new Color();
	private Color mFromColor;
	private Color mToColor;
	private Vector4 mFromVec;
	private Vector4 mToVec;
}

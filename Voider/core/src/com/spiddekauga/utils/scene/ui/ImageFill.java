package com.spiddekauga.utils.scene.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.utils.Scaling;

/**
 * Image for {@link ImageButtonFill}.
 */
public class ImageFill extends Image {

public ImageFill() {
	setScaling(Scaling.stretch);
}

/**
 * @return original image width
 */
public float getOriginalWidth() {
	return super.getPrefWidth();
}

/**
 * @return original image height
 */
public float getOriginHeight() {
	return super.getPrefHeight();
}

@Override
public float getPrefWidth() {
	float prefWidth = super.getPrefWidth();

	if (getParent() instanceof ImageButtonFill) {
		ImageButtonFill imageButtonFill = (ImageButtonFill) getParent();
		ImageButton.ImageButtonStyle imageButtonStyle = imageButtonFill.getStyle();

		if (imageButtonStyle.imageUp != null) {
			float borderWidth = 0;

			if (imageButtonStyle.up != null) {
				borderWidth = imageButtonStyle.up.getLeftWidth() + imageButtonStyle.up.getRightWidth();
			}

			prefWidth = ((int) imageButtonFill.getWidth()) - borderWidth;
		}
	}

	return prefWidth;
}

@Override
public float getPrefHeight() {
	float prefHeight = super.getPrefHeight();

	if (getParent() instanceof ImageButtonFill) {
		ImageButtonFill imageButtonFill = (ImageButtonFill) getParent();
		ImageButton.ImageButtonStyle imageButtonStyle = imageButtonFill.getStyle();

		if (imageButtonStyle.imageUp != null) {
			float borderHeight = 0;

			if (imageButtonStyle.up != null) {
				borderHeight = imageButtonStyle.up.getTopHeight() + imageButtonStyle.up.getBottomHeight();
			}

			prefHeight = ((int) imageButtonFill.getHeight()) - borderHeight;
		}
	}

	return prefHeight;
}
}

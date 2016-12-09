package com.spiddekauga.utils.scene.ui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

/**
 * Image button that makes it possible to increase the size of the mImage button and the mImage
 * inside it will be resized accordingly.
 */
public class ImageButtonFill extends Button {
private final ImageFill mImage;
private ImageButton.ImageButtonStyle mStyle;

public ImageButtonFill(Skin skin) {
	this(skin.get(ImageButton.ImageButtonStyle.class));
}

public ImageButtonFill(ImageButton.ImageButtonStyle style) {
	super(style);
	mImage = new ImageFill();
	add(mImage);
	setStyle(style);
	setSize(mImage.getOriginalWidth(), mImage.getOriginHeight());
}

private void updateImage() {
	Drawable drawable = null;
	if (isDisabled() && mStyle.imageDisabled != null) {
		drawable = mStyle.imageDisabled;
	} else if (isPressed() && mStyle.imageDown != null) {
		drawable = mStyle.imageDown;
	} else if (isChecked() && mStyle.imageChecked != null) {
		drawable = (mStyle.imageCheckedOver != null && isOver()) ? mStyle.imageCheckedOver : mStyle.imageChecked;
	} else if (isOver() && mStyle.imageOver != null) {
		drawable = mStyle.imageOver;
	} else if (mStyle.imageUp != null) //
	{
		drawable = mStyle.imageUp;
	}
	mImage.setDrawable(drawable);
}

public ImageButtonFill(Skin skin, String styleName) {
	this(skin.get(styleName, ImageButton.ImageButtonStyle.class));
}

public ImageButtonFill(Drawable imageUp) {
	this(new ImageButton.ImageButtonStyle(null, null, null, imageUp, null, null));
}

public ImageButtonFill(Drawable imageUp, Drawable imageDown) {
	this(new ImageButton.ImageButtonStyle(null, null, null, imageUp, imageDown, null));
}

public ImageButtonFill(Drawable imageUp, Drawable imageDown, Drawable imageChecked) {
	this(new ImageButton.ImageButtonStyle(null, null, null, imageUp, imageDown, imageChecked));
}

public Image getImage() {
	return mImage;
}

public com.badlogic.gdx.scenes.scene2d.ui.Cell getImageCell() {
	return getCell(mImage);
}

public void setStyle(ButtonStyle style) {
	if (!(style instanceof ImageButton.ImageButtonStyle)) {
		throw new IllegalArgumentException("mStyle must be an ImageButtonStyle.");
	}
	super.setStyle(style);
	this.mStyle = (ImageButton.ImageButtonStyle) style;
	if (mImage != null) {
		updateImage();
	}
}


public ImageButton.ImageButtonStyle getStyle() {
	return mStyle;
}

public void draw(Batch batch, float parentAlpha) {
	updateImage();
	super.draw(batch, parentAlpha);
}


}

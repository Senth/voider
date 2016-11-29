package com.spiddekauga.utils.scene.ui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

/**
 * Image button that makes it possible to increase the size of the image button and the image inside
 * it will be resized accordingly.
 */
public class ImageButtonFill extends Button {
private final ImageFill image;
private ImageButton.ImageButtonStyle style;

public ImageButtonFill(Skin skin) {
	this(skin.get(ImageButton.ImageButtonStyle.class));
}

public ImageButtonFill(ImageButton.ImageButtonStyle style) {
	super(style);
	image = new ImageFill();
	add(image);
	setStyle(style);
	setSize(image.getOriginalWidth(), image.getOriginHeight());
}

private void updateImage() {
	Drawable drawable = null;
	if (isDisabled() && style.imageDisabled != null) {
		drawable = style.imageDisabled;
	} else if (isPressed() && style.imageDown != null) {
		drawable = style.imageDown;
	} else if (isChecked() && style.imageChecked != null) {
		drawable = (style.imageCheckedOver != null && isOver()) ? style.imageCheckedOver : style.imageChecked;
	} else if (isOver() && style.imageOver != null) {
		drawable = style.imageOver;
	} else if (style.imageUp != null) //
	{
		drawable = style.imageUp;
	}
	image.setDrawable(drawable);
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
	return image;
}

public com.badlogic.gdx.scenes.scene2d.ui.Cell getImageCell() {
	return getCell(image);
}

public void setStyle(ButtonStyle style) {
	if (!(style instanceof ImageButton.ImageButtonStyle)) {
		throw new IllegalArgumentException("style must be an ImageButtonStyle.");
	}
	super.setStyle(style);
	this.style = (ImageButton.ImageButtonStyle) style;
	if (image != null) {
		updateImage();
	}
}


public ImageButton.ImageButtonStyle getStyle() {
	return style;
}


public void draw(Batch batch, float parentAlpha) {
	updateImage();
	super.draw(batch, parentAlpha);
}


}

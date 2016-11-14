package com.spiddekauga.utils.scene.ui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.spiddekauga.voider.resources.IResourceTexture;

/**
 * Scene2D image that draws a resource in the specified size.
 */
public class ResourceTextureImage extends Image {
private IResourceTexture mResource;

/**
 * Constructs the resource texture image with an invalid image
 */
public ResourceTextureImage() {
	setResource(null);
}

/**
 * Sets the correct image style
 * @param resource the resource to get the texture from
 */
public void setResource(IResourceTexture resource) {
	mResource = resource;
	updateImage();
}

/**
 * Update the current texture of the image. If the resource was changed the texture can be invalid.
 */
private void updateImage() {
	Drawable drawable = getDrawable();

	if (mResource != null) {
		if (drawable != mResource.getTextureRegionDrawable()) {
			setDrawable(mResource.getTextureRegionDrawable());
		}
	}
}

/**
 * Constructs the image with a resource containing a texture
 * @param resource the resource with the texture
 */
public ResourceTextureImage(IResourceTexture resource) {
	setResource(resource);
}

@Override
public void draw(Batch batch, float parentAlpha) {
	updateImage();
	super.draw(batch, parentAlpha);
}
}

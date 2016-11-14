package com.spiddekauga.voider.game.actors;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.spiddekauga.voider.repo.resource.SkinNames;
import com.spiddekauga.voider.repo.resource.SkinNames.GeneralImages;
import com.spiddekauga.voider.repo.resource.SkinNames.IImageNames;

/**
 * Which images should be available when selecting an image for an actor
 */
public enum DrawImages {
// NEVER CHANGE ORDER OF THESE
	/** Large player shuttle */
	SHUTTLE_LARGE(GeneralImages.SHUTTLE_LARGE),;

private IImageNames mImageName;

/**
 * Sets which image name this enumeration is bound to
 * @param imageName the image name
 */
private DrawImages(IImageNames imageName) {
	mImageName = imageName;
}

/**
 * @return texture region of this draw image, null if the resource isn't loaded
 */
public TextureRegion getRegion() {
	return SkinNames.getRegion(mImageName);
}
}

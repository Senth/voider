package com.spiddekauga.utils.scene.ui;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

/**
 * Wrapper for rendering an animation in scene2d
 */
public class AnimationWidget extends Image {
/** The current animation */
private Animation mAnimation = null;
private float mTimeElapsed = 0;
/** Current drawable texture region */
private TextureRegionDrawable mDrawableTextureRegion = new TextureRegionDrawable();

/**
 * Creates an empty animation
 */
public AnimationWidget() {
	setAnimation(null);
}

/**
 * Create an animation widget from a AnimationWidgetStyle
 * @param animationStyle the animation style to create a new animation from
 */
public AnimationWidget(AnimationWidgetStyle animationStyle) {
	int tileWidth = animationStyle.image.getRegionWidth() / animationStyle.frameColumns;
	int tileHeight = animationStyle.image.getRegionHeight() / animationStyle.frameRows;

	int cFrames = animationStyle.frames != -1 ? animationStyle.frames : animationStyle.frameColumns * animationStyle.frameRows;
	TextureRegion[] walkFrames = new TextureRegion[cFrames];

	int frameIndex = 0;
	for (int row = 0; row < animationStyle.frameRows; ++row) {
		int y = row * tileHeight;
		for (int col = 0; col < animationStyle.frameColumns; ++col) {
			int x = col * tileWidth;
			walkFrames[frameIndex] = new TextureRegion(animationStyle.image, x, y, tileWidth, tileHeight);
			frameIndex++;

			if (frameIndex == animationStyle.frames) {
				break;
			}
		}
	}

	Animation animation = new Animation(animationStyle.secondsPerFrame, walkFrames);
	animation.setPlayMode(Animation.PlayMode.LOOP);
	setAnimation(animation);
}

/**
 * Create an animation widget with the specified animation
 * @param animation the animation to show
 */
public AnimationWidget(Animation animation) {
	setAnimation(animation);
}

/**
 * @return animation that is rendered
 */
public Animation getAnimation() {
	return mAnimation;
}

/**
 * Sets the animation for the widget
 * @param animation the animation to show
 */
public void setAnimation(Animation animation) {
	mAnimation = animation;

	if (mAnimation != null) {
		mTimeElapsed = 0;
		mDrawableTextureRegion.setRegion(mAnimation.getKeyFrame(0));
		setDrawable(mDrawableTextureRegion);
	} else {
		setDrawable(null);
	}
}

@Override
public void act(float delta) {
	super.act(delta);

	if (mAnimation != null) {
		mTimeElapsed += delta;
		mDrawableTextureRegion.setRegion(mAnimation.getKeyFrame(mTimeElapsed));
	}
}

/**
 * Reset the animation to the start
 */
public void reset() {
	setAnimation(mAnimation);
}

/**
 * Animation widget style
 */
public static class AnimationWidgetStyle {
	/** The texture with all animation frames */
	public TextureRegion image = null;
	/** Number of frame columns in the texture */
	public int frameColumns = 0;
	/** Number of frame rows in the texture */
	public int frameRows = 0;
	/** Number of seconds to show each frame */
	public float secondsPerFrame = 0;
	/**
	 * Optional total number of frames. Use this when the last row isn't filled with frames,
	 * otherwise all 'square' frames will be used
	 */
	public int frames = -1;
}
}

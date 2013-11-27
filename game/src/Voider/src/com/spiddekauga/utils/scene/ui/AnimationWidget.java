package com.spiddekauga.utils.scene.ui;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

/**
 * Wrapper for rendering an animation in scene2d
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class AnimationWidget extends Image {
	/**
	 * Creates an empty animation
	 */
	public AnimationWidget() {
		this(null);
	}

	/**
	 * Create an animation widget with the specified animation
	 * @param animation the animation to show
	 */
	public AnimationWidget(Animation animation) {
		setAnimation(animation);
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

	/**
	 * @return animation that is rendered
	 */
	public Animation getAnimation() {
		return mAnimation;
	}

	@Override
	public void act(float delta) {
		super.act(delta);

		if (mAnimation != null) {
			mTimeElapsed += delta;
			mDrawableTextureRegion.setRegion(mAnimation.getKeyFrame(mTimeElapsed));
		}
	}

	/** The current animation */
	private Animation mAnimation = null;
	/** Total elapsed time */
	private float mTimeElapsed = 0;
	/** Current drawable texture region */
	private TextureRegionDrawable mDrawableTextureRegion = new TextureRegionDrawable();
}

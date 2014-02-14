package com.spiddekauga.voider.app;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.resources.InternalNames;
import com.spiddekauga.voider.scene.Gui;

/**
 * GUI for the splash screen.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
class SplashScreenGui extends Gui {
	@Override
	public void initGui() {
		super.initGui();

		if (ResourceCacheFacade.isLoaded(InternalNames.IMAGE_SPLASH_SCREEN)) {
			Texture splashScreenTexture = ResourceCacheFacade.get(InternalNames.IMAGE_SPLASH_SCREEN);

			if (splashScreenTexture != null) {
				mSplashScreenImage = new Image(splashScreenTexture);
				getStage().addActor(mSplashScreenImage);

				// Scale
				float scaling = Gdx.graphics.getWidth() / mSplashScreenImage.getPrefWidth();
				mSplashScreenImage.setScale(scaling);

				// Center
				float yOffset = (Gdx.graphics.getHeight() - mSplashScreenImage.getHeight() * scaling) * 0.5f;
				mSplashScreenImage.setPosition(0, yOffset);

				// Fade in
				mSplashScreenImage.setColor(1, 1, 1, 0);
				mSplashScreenImage.addAction(Actions.sequence(Actions.delay(Config.Menu.SPLASH_SCREEN_ENTER_TIME), Actions.fadeIn(Config.Menu.SPLASH_SCREEN_FADE_IN)));
			}
		}
	}

	/**
	 * Fade out the splash screen
	 */
	public void fadeOut() {
		if (mSplashScreenImage != null) {
			mSplashScreenImage.addAction(Actions.sequence(Actions.fadeOut(Config.Menu.SPLASH_SCREEN_FADE_OUT), Actions.delay(Config.Menu.SPLASH_SCREEN_EXIT_TIME), Actions.removeActor()));
		}
	}

	/**
	 * @return true when the splash screen has faded
	 */
	public boolean hasFaded() {
		return mSplashScreenImage == null || mSplashScreenImage.getStage() == null;
	}

	/** Splash screen image */
	private Image mSplashScreenImage = null;
}

package com.spiddekauga.voider.app;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.spiddekauga.voider.config.ConfigIni;
import com.spiddekauga.voider.config.IC_Menu.IC_Time;
import com.spiddekauga.voider.repo.resource.InternalNames;
import com.spiddekauga.voider.repo.resource.ResourceCacheFacade;
import com.spiddekauga.utils.scene.ui.Gui;

/**
 * GUI for the splash screen.
 */
class SplashScreenGui extends Gui {
/** Splash screen image */
private Image mSplashScreenImage = null;

/**
 * Fade out the splash screen
 */
public void fadeOut() {
	if (mSplashScreenImage != null) {
		IC_Time icTime = ConfigIni.getInstance().menu.time;
		mSplashScreenImage.addAction(Actions.sequence(Actions.fadeOut(icTime.getSplashScreenFadeOut()), Actions.delay(icTime.getSceneExitTime()),
				Actions.removeActor()));
	}
}

@Override
public void onDestroy() {
	super.onDestroy();
	if (mSplashScreenImage != null) {
		mSplashScreenImage.remove();
	}
}

@Override
public void onCreate() {
	super.onCreate();

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
			IC_Time icTime = ConfigIni.getInstance().menu.time;
			mSplashScreenImage.setColor(1, 1, 1, 0);
			mSplashScreenImage.addAction(Actions.sequence(Actions.delay(icTime.getSceneEnterTime()),
					Actions.fadeIn(icTime.getSplashScreenFadeIn())));
		}
	}
}

/**
 * @return true when the splash screen has faded
 */
public boolean hasFaded() {
	return mSplashScreenImage == null || mSplashScreenImage.getStage() == null;
}
}

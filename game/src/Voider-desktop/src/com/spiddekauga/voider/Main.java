package com.spiddekauga.voider;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.spiddekauga.voider.repo.resource.InternalNames;

public class Main {
	public static void main(String[] args) {
		// Create window from settings
		// Preferences preferences = Gdx.app.getPreferences(PREFERENCES_NAME);
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "Voider";
		// config.fullscreen = preferences.getBoolean(DISPLAY__FULLSCREEN, false);
		// if (config.fullscreen) {
		// try {
		// Resolution resolution = new
		// Resolution(preferences.getString(DISPLAY__RESOLUTION_FULLSCREEN));
		// config.width = resolution.getWidth();
		// config.height = resolution.getHeight();
		// } catch (IllegalArgumentException e) {
		// config.width = Gdx.graphics.getDesktopDisplayMode().width;
		// config.height = Gdx.graphics.getDesktopDisplayMode().height;
		// }
		// } else {
		// try {
		// Resolution resolution = new
		// Resolution(preferences.getString(DISPLAY__RESOLUTION_WINDOWED));
		// config.width = resolution.getWidth();
		// config.height = resolution.getHeight();
		// } catch (IllegalArgumentException e) {
		config.width = Config.Graphics.WIDTH_START;
		config.height = Config.Graphics.HEIGHT_START;
		// }
		// }

		// Add icons
		FileType fileType = Config.File.USE_EXTERNAL_RESOURCES ? FileType.Absolute : FileType.Internal;
		config.addIcon(InternalNames.ICON_256.getFilePath(), fileType);
		config.addIcon(InternalNames.ICON_128.getFilePath(), fileType);
		config.addIcon(InternalNames.ICON_64.getFilePath(), fileType);
		config.addIcon(InternalNames.ICON_32.getFilePath(), fileType);
		// config.useGL30 = true;

		new LwjglApplication(new VoiderGame(), config);
	}

	// private static final String DISPLAY__RESOLUTION_WINDOWED =
	// "display_resolutionWindowed";
	// private static final String DISPLAY__RESOLUTION_FULLSCREEN =
	// "display_resolutionFullscreen";
	// private static final String DISPLAY__FULLSCREEN = "display_fullscreen";
	// private static final String PREFERENCES_NAME = Config.File.PREFERENCE_PREFIX +
	// "_setting";
}

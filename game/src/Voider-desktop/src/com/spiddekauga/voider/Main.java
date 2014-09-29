package com.spiddekauga.voider;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.spiddekauga.voider.repo.resource.InternalNames;

public class Main {
	public static void main(String[] args) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "Voider";
		config.width = Config.Graphics.WIDTH_START;
		config.height = Config.Graphics.HEIGHT_START;

		// Add icons
		FileType fileType = Config.File.USE_EXTERNAL_RESOURCES ? FileType.Absolute : FileType.Internal;
		config.addIcon(InternalNames.ICON_256.getFilePath(), fileType);
		config.addIcon(InternalNames.ICON_128.getFilePath(), fileType);
		config.addIcon(InternalNames.ICON_64.getFilePath(), fileType);
		config.addIcon(InternalNames.ICON_32.getFilePath(), fileType);

		new LwjglApplication(new VoiderGame(), config);
	}
}

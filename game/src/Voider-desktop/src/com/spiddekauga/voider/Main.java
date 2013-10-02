package com.spiddekauga.voider;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class Main {
	public static void main(String[] args) {
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "Voider";
		cfg.useGL20 = true;
		cfg.width = Config.Graphics.WIDTH_START;
		cfg.height = Config.Graphics.HEIGHT_START;

		new LwjglApplication(new VoiderGame(), cfg);
	}
}

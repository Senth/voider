package com.spiddekauga.voider;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class Main {
	public static void main(String[] args) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "Voider";
		config.width = Config.Graphics.WIDTH_START;
		config.height = Config.Graphics.HEIGHT_START;

		new LwjglApplication(new VoiderGame(), config);
	}
}

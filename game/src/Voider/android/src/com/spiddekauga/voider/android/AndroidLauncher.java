package com.spiddekauga.voider.android;

import android.os.Bundle;
import android.view.WindowManager;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.spiddekauga.voider.VoiderGame;

/**
 * Launches the game on the Android
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class AndroidLauncher extends AndroidApplication {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		initialize(new VoiderGame(), config);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}
}

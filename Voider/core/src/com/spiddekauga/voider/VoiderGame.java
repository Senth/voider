package com.spiddekauga.voider;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.esotericsoftware.minlog.Log;
import com.spiddekauga.utils.EventBus;
import com.spiddekauga.utils.GameTime;
import com.spiddekauga.utils.Resolution;
import com.spiddekauga.utils.Strings;
import com.spiddekauga.utils.scene.ui.Scene;
import com.spiddekauga.utils.scene.ui.SceneSwitcher;
import com.spiddekauga.voider.Config.Debug;
import com.spiddekauga.voider.Config.Debug.Builds;
import com.spiddekauga.voider.app.SplashScreen;
import com.spiddekauga.voider.config.ConfigIni;
import com.spiddekauga.voider.menu.LoginScene;
import com.spiddekauga.voider.network.entities.IEntity;
import com.spiddekauga.voider.network.entities.IMethodEntity;
import com.spiddekauga.voider.repo.IResponseListener;
import com.spiddekauga.voider.repo.analytics.AnalyticsRepo;
import com.spiddekauga.voider.settings.SettingRepo;
import com.spiddekauga.voider.settings.SettingRepo.SettingDisplayRepo;
import com.spiddekauga.voider.repo.resource.ResourceCacheFacade;
import com.spiddekauga.voider.repo.resource.ResourceChecker;
import com.spiddekauga.voider.repo.user.User;
import com.spiddekauga.voider.scene.ui.InfoDisplayer;
import com.spiddekauga.voider.server.ServerMessageReciever;
import com.spiddekauga.voider.sound.MusicPlayer;
import com.spiddekauga.voider.utils.Synchronizer;
import com.squareup.otto.ThreadEnforcer;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.concurrent.Semaphore;

/**
 * The main application, i.e. start point
 */
public class VoiderGame implements ApplicationListener {
private static long mMainThreadId = 0;
private Semaphore mAnalyticsSyncSemaphore = new Semaphore(1);
private IResponseListener mAnalyticsResponseListener = new IResponseListener() {
	@Override
	public void handleWebResponse(IMethodEntity method, IEntity response) {
		mAnalyticsSyncSemaphore.release();
	}
};
private MusicPlayer mMusicPlayer = null;

/**
 * @return true if the current thread is the main thread
 */
public static boolean isMainThread() {
	return Thread.currentThread().getId() == mMainThreadId;
}

@Override
public void create() {
	mMainThreadId = Thread.currentThread().getId();
	EventBus.setThreadEnforcer(ThreadEnforcer.ANY);
//	EventBus.setMainThreadId(Thread.currentThread().getId());

	Gdx.app.setLogLevel(Config.Debug.LOG_VERBOSITY);
	Box2D.init();
	CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
	Gdx.input.setCatchBackKey(true);

	if (Debug.isBuildOrBelow(Builds.NIGHTLY_DEV)) {
		Log.INFO();
	} else {
		Log.ERROR();
	}

	// Init various classes
	ConfigIni.getInstance();
	ServerMessageReciever.getInstance();
	Synchronizer.getInstance();
	ResourceChecker.init();
	InfoDisplayer.getInstance();

	updateResolution();

	ShaderProgram.pedantic = false;

	if (Debug.isBuildOrBelow(Builds.NIGHTLY_RELEASE)) {
		showLogin();
	} else {
		showSplashScreen();
	}

	mMusicPlayer = MusicPlayer.getInstance();
	AnalyticsRepo.getInstance().newSession();
}

/**
 * Set correct resolution for desktop
 */
private void updateResolution() {
	if (Gdx.app.getType() == ApplicationType.Desktop) {
		SettingDisplayRepo displayRepo = SettingRepo.getInstance().display();
		Resolution resolution = null;
		boolean fullscreen = displayRepo.isFullscreen();
		if (fullscreen) {
			resolution = displayRepo.getResolutionFullscreen();
		} else {
			resolution = displayRepo.getResolutionWindowed();
		}
		Gdx.graphics.setDisplayMode(resolution.getWidth(), resolution.getHeight(), fullscreen);
	}
}

/**
 * Test login
 */
private void showLogin() {
	SceneSwitcher.switchTo(new LoginScene());
}

/**
 * Test splash screen
 */
private void showSplashScreen() {
	SceneSwitcher.switchTo(new LoginScene(), new SplashScreen());
}

@Override
public void resize(int width, int height) {
	SceneSwitcher.resize(width, height);
}

@Override
public void render() {
	Gdx.gl.glClearColor(0, 0, 0, 1);
	Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

	GameTime.updateGlobal(Gdx.graphics.getDeltaTime());

	try {
		Gdx.gl.glEnable(GL20.GL_TEXTURE_2D);
		SceneSwitcher.update();
		mMusicPlayer.update();
	} catch (RuntimeException e) {
		// Print where in the serialization it failed
		if (Gdx.app.getType() == ApplicationType.Desktop && Config.Debug.isBuildOrAbove(Builds.NIGHTLY_DEV)) {
			String stackTrace = Strings.exceptionToString(e);

			// File error
			int fileIndexStart = stackTrace.indexOf("Error reading file: ");
			if (fileIndexStart != -1) {
				int fileIndexEnd = stackTrace.indexOf(".json", fileIndexStart);
				String fileWithError = stackTrace.substring(fileIndexStart, fileIndexEnd + 5);

				// Syntax error
				String syntaxErrorSearchFor = "GdxRuntimeException: ";
				int syntaxIndexStart = stackTrace.indexOf(syntaxErrorSearchFor, fileIndexEnd);
				if (syntaxIndexStart != -1) {
					int syntaxIndexEnd = stackTrace.indexOf('\n', syntaxIndexStart);
					String syntaxError = "Error: ";
					syntaxError += stackTrace.substring(syntaxIndexStart + syntaxErrorSearchFor.length(), syntaxIndexEnd);

					String errorString = fileWithError + "\n" + syntaxError;

					FileHandle file = new FileHandle("json_error.txt");
					file.writeString(errorString, false);
				}
			}
		}
		throw e;
	}
}

@Override
public void pause() {
	if (Gdx.app.getType() == ApplicationType.Android || Gdx.app.getType() == ApplicationType.iOS) {
		AnalyticsRepo.getInstance().endSession();
		syncAnalytics();
		if (User.getGlobalUser().isOnline()) {
			User.getGlobalUser().disconnect();
		}
	}
}

@Override
public void resume() {
	if (Gdx.app.getType() == ApplicationType.Android || Gdx.app.getType() == ApplicationType.iOS) {
		AnalyticsRepo.getInstance().newSession();
		if (User.getGlobalUser().isLoggedIn()) {
			User.getGlobalUser().login();
		}
	}
}

@Override
public void dispose() {
	AnalyticsRepo.getInstance().endSession();
	syncAnalytics();
	ServerMessageReciever.getInstance().disconnect();

	SceneSwitcher.dispose();
	ResourceCacheFacade.unload((Scene) null);
	ResourceCacheFacade.dispose();
	Config.dispose();
	ResourceChecker.dispose();
}

/**
 * Sync analytics
 */
private void syncAnalytics() {
	if (User.getGlobalUser().isOnline()) {
		try {
			mAnalyticsSyncSemaphore.acquire();
			AnalyticsRepo.getInstance().sync(mAnalyticsResponseListener);
			mAnalyticsSyncSemaphore.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
}

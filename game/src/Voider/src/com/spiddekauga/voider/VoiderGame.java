package com.spiddekauga.voider;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.esotericsoftware.minlog.Log;
import com.spiddekauga.utils.GameTime;
import com.spiddekauga.utils.Strings;
import com.spiddekauga.voider.Config.Debug.Builds;
import com.spiddekauga.voider.app.SplashScreen;
import com.spiddekauga.voider.game.Collectibles;
import com.spiddekauga.voider.game.actors.PickupActorDef;
import com.spiddekauga.voider.menu.LoginScene;
import com.spiddekauga.voider.menu.MainMenu;
import com.spiddekauga.voider.repo.ResourceLocalRepo;
import com.spiddekauga.voider.repo.UserLocalRepo;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.resources.ResourceChecker;
import com.spiddekauga.voider.scene.SceneSwitcher;
import com.spiddekauga.voider.server.MessageGateway;
import com.spiddekauga.voider.utils.Synchronizer;

/**
 * The main application, i.e. start point
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class VoiderGame implements ApplicationListener {

	@Override
	public void create() {
		Gdx.app.setLogLevel(Config.Debug.LOG_VERBOSITY);
		Log.ERROR();

		// Init various classes
		MessageGateway.getInstance();
		Synchronizer.getInstance();
		ResourceChecker.init();


		ShaderProgram.pedantic = false;

		/** @TODO set splash screen as start screen */

		testLogin();
		//		testMainMenu();
		//		testSavePickups();
		//		testSplashScreen();
	}

	/**
	 * Testing to save pickups
	 */
	@SuppressWarnings({ "unused" })
	private void testSavePickups() {
		PickupActorDef def = new PickupActorDef();
		def.setCollectible(Collectibles.HEALTH_25);
		def.setName("+25 Health");
		ResourceLocalRepo.save(def);

		def = new PickupActorDef();
		def.setCollectible(Collectibles.HEALTH_50);
		def.setName("+50 Health");
		ResourceLocalRepo.save(def);
	}

	/**
	 * Test login
	 */
	private void testLogin() {
		SceneSwitcher.switchTo(new LoginScene());
	}

	/**
	 * Testing the main menu
	 */
	@SuppressWarnings("unused")
	private void testMainMenu() {
		SceneSwitcher.switchTo(new MainMenu());
	}

	/**
	 * Test splash screen
	 */
	@SuppressWarnings("unused")
	private void testSplashScreen() {
		SceneSwitcher.switchTo(new MainMenu(), new SplashScreen());
	}

	@Override
	public void dispose() {
		SceneSwitcher.dispose();
		ResourceCacheFacade.dispose();
		Config.dispose();
		UserLocalRepo.dispose();
		ResourceChecker.dispose();
	}

	@Override
	public void render() {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		GameTime.updateGlobal(Gdx.graphics.getDeltaTime());

		try {
			SceneSwitcher.update();
		} catch (RuntimeException e) {
			// Print where in the serialization it failed
			if (Gdx.app.getType() == ApplicationType.Desktop && Config.Debug.isBuildOrAbove(Builds.NIGHTLY)) {
				String stackTrace = Strings.stackTraceToString(e);

				// File error
				int fileIndexStart = stackTrace.indexOf("Error reading file: ");
				if (fileIndexStart != -1) {
					int fileIndexEnd = stackTrace.indexOf(".json", fileIndexStart);
					String fileWithError = stackTrace.substring(fileIndexStart, fileIndexEnd+5);

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
	public void resize(int width, int height) {
		SceneSwitcher.resize(width, height);
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}
}

package com.spiddekauga.voider;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.esotericsoftware.minlog.Log;
import com.spiddekauga.utils.GameTime;
import com.spiddekauga.utils.scene.ui.AlignTable;
import com.spiddekauga.voider.app.SplashScreen;
import com.spiddekauga.voider.game.Collectibles;
import com.spiddekauga.voider.game.actors.PickupActorDef;
import com.spiddekauga.voider.menu.LoginScene;
import com.spiddekauga.voider.menu.MainMenu;
import com.spiddekauga.voider.repo.UserLocalRepo;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.resources.ResourceChecker;
import com.spiddekauga.voider.resources.ResourceSaver;
import com.spiddekauga.voider.scene.SceneSwitcher;

/**
 * The main application, i.e. start point
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class VoiderGame implements ApplicationListener {

	@Override
	public void create() {
		Gdx.app.setLogLevel(Config.Debug.LOG_VERBOSITY);
		Log.ERROR();

		// Init various classes
		Config.init();
		ResourceSaver.init();
		ResourceChecker.checkAndCreateResources();

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
		ResourceSaver.save(def);

		def = new PickupActorDef();
		def.setCollectible(Collectibles.HEALTH_50);
		def.setName("+50 Health");
		ResourceSaver.save(def);
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
	}

	@Override
	public void render() {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

		GameTime.updateGlobal(Gdx.graphics.getDeltaTime());

		if (mStage != null) {
			mStage.act();
			mStage.draw();
		}

		SceneSwitcher.update();
	}

	@Override
	public void resize(int width, int height) {
		if (mStage != null) {
			mStage.setViewport(width, height, true);
			table.setWidth(width);
			table.setHeight(height);
		}
		SceneSwitcher.resize(width, height);
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	/** Testing stage */
	private Stage mStage = null;
	/** Testing table */
	private AlignTable table = null;
}

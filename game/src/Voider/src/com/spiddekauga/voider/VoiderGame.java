package com.spiddekauga.voider;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.spiddekauga.utils.GameTime;
import com.spiddekauga.utils.scene.ui.AlignTable;
import com.spiddekauga.voider.app.MainMenu;
import com.spiddekauga.voider.app.SplashScreen;
import com.spiddekauga.voider.editor.LevelEditor;
import com.spiddekauga.voider.game.Collectibles;
import com.spiddekauga.voider.game.Level;
import com.spiddekauga.voider.game.LevelDef;
import com.spiddekauga.voider.game.actors.PickupActorDef;
import com.spiddekauga.voider.game.actors.PlayerActorDef;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.resources.ResourceNames;
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
		// Init various classes
		Config.init();
		ResourceNames.init();
		ResourceSaver.init();
		ResourceCacheFacade.init();
		Gdx.app.setLogLevel(Application.LOG_DEBUG);

		ShaderProgram.pedantic = false;

		/** @TODO set splash screen as start screen */

		//		testSavePlayerShip();
		testMainMenu();
		//		testSavePickups();
		//		testSplashScreen();
	}

	/**
	 * Test to save player ship
	 */
	@SuppressWarnings("unused")
	private void testSavePlayerShip() {
		ResourceSaver.save(new PlayerActorDef());
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
	 * Testing the editor
	 */
	private void testMainMenu() {
		SceneSwitcher.switchTo(new MainMenu());
	}

	/**
	 * Testing editor
	 */
	@SuppressWarnings("unused")
	private void testEditor() {
		LevelEditor levelEditor = new LevelEditor();

		LevelDef levelDef = new LevelDef();
		Level level = new Level(levelDef);
		levelEditor.setLevel(level);
		SceneSwitcher.switchTo(levelEditor);
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
	}

	@Override
	public void render() {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

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

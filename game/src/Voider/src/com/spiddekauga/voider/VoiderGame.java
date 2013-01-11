package com.spiddekauga.voider;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.spiddekauga.voider.editor.LevelEditor;
import com.spiddekauga.voider.game.GameScene;
import com.spiddekauga.voider.game.GameTime;
import com.spiddekauga.voider.game.Level;
import com.spiddekauga.voider.game.LevelDef;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
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
		ResourceSaver.init();
		ResourceCacheFacade.init();


		/** @TODO display splash screen */

		//testGame();
		testEditor();
	}

	/**
	 * Testing the editor
	 */
	private void testEditor() {
		LevelEditor levelEditor = new LevelEditor();

		LevelDef levelDef = new LevelDef();
		Level level = new Level(levelDef);
		levelEditor.setLevel(level);
		SceneSwitcher.switchTo(levelEditor);
	}

	/**
	 * testing to start a game
	 */
	private void testGame() {
		GameScene gameScene = new GameScene(false);

		LevelDef levelDef = new LevelDef();
		Level level = new Level(levelDef);
		gameScene.setLevel(level);
		SceneSwitcher.switchTo(gameScene);
	}

	@Override
	public void dispose() {
		ResourceCacheFacade.dispose();
		Config.dispose();
	}

	@Override
	public void render() {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

		GameTime.update(Gdx.graphics.getDeltaTime());
		SceneSwitcher.update();
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

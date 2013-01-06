package com.spiddekauga.voider;

import java.util.LinkedList;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.spiddekauga.voider.editor.LevelEditor;
import com.spiddekauga.voider.game.GameScene;
import com.spiddekauga.voider.game.Level;
import com.spiddekauga.voider.game.LevelDef;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.resources.ResourceSaver;

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
		mActiveScene.push(levelEditor);

		Gdx.input.setInputProcessor(levelEditor.getInputMultiplexer());
	}

	/**
	 * testing to start a game
	 */
	private void testGame() {
		GameScene gameScene = new GameScene(false);

		LevelDef levelDef = new LevelDef();
		Level level = new Level(levelDef);
		gameScene.setLevel(level);
		mActiveScene.push(gameScene);

		Gdx.input.setInputProcessor(gameScene.getInputMultiplexer());
	}

	@Override
	public void dispose() {

	}

	@Override
	public void render() {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

		if (!mActiveScene.isEmpty()) {
			mActiveScene.getFirst().update();
			mActiveScene.getFirst().render();
		}
	}

	@Override
	public void resize(int width, int height) {
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	/**
	 * A stack of the active scenes. This makes it easier to make back
	 * key, return to the previous scene.
	 */
	private LinkedList<Scene> mActiveScene = new LinkedList<Scene>();
}

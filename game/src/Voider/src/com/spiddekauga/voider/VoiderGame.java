package com.spiddekauga.voider;

import java.util.LinkedList;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.spiddekauga.voider.game.GameScene;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.resources.ResourceSaver;

/**
 * The main application, i.e. start point
 * 
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

		mScenes = new Scene[Scenes.values().length];

		mScenes[Scenes.GAME.ordinal()] = new GameScene();

		/** @TODO display splash screen */
		mActiveScene.push(Scenes.GAME.ordinal());

	}

	@Override
	public void dispose() {

	}

	@Override
	public void render() {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

		if (mScenes[mActiveScene.getFirst()] != null) {
			//mScenes[mActiveScene.getFirst()].run();
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
	 * All the different scenes
	 */
	private Scene[] mScenes;
	/**
	 * A stack of the active scenes. This makes it easier to make back
	 * key, return to the previous scene.
	 */
	private LinkedList<Integer> mActiveScene = new LinkedList<Integer>();
}

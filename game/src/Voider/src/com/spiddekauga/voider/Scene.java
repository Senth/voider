package com.spiddekauga.voider;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Stage;

/**
 * Base class for all scenes that should be rendered. Examples of scenes:
 * Game, Menus, Editors. It extends InputAdapter which makes it able to
 * handle all input events through derived classes (if necessary).
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public abstract class Scene extends InputAdapter {
	/**
	 * Runs the scene. Don't Override this method as this method clears the screen,
	 * renders it, and updates the scene elements.
	 */
	public void run() {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		render();
		update();
	}

	/**
	 * Updates the scene
	 */
	public abstract void update();

	/**
	 * Called when the scene activates
	 * @param action the action the scene shall take
	 */
	public void onActivate(String action) {
		// Does nothing
	}

	/**
	 * Called when the scene deactivates (another one is activated)
	 */
	public void onDeactivate() {
		// Does nothing
	}

	/**
	 * Called when the scene is deleted
	 */
	public void onDisposed() {
		// Does nothing
	}

	/**
	 * Renders the scene
	 */
	public void render() {
		mUi.act(Gdx.graphics.getDeltaTime());
		mUi.draw();
	}

	/**
	 * @return the world of the scene (if it has any), otherwise it returns null.
	 */
	public World getWorld() {
		return null;
	}

	/** Handles user interfaces for the scene */
	protected Stage mUi = new Stage();
	/** Sprite Batch used for rendering stuff */
	protected SpriteBatch mSpriteBatch = new SpriteBatch();
}
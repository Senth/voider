package com.spiddekauga.voider.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.spiddekauga.voider.DebugOptions;
import com.spiddekauga.voider.Scene;
/**
 * The main game
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class GameScene extends Scene {
	/**
	 * Initializes the game scene.
	 */
	public GameScene() {
		super();

		/** TODO fix aspect ratio */
		mWorld = new World(new Vector2(0, 0), true);
		mCamera = new OrthographicCamera(80, 48);
	}

	@Override
	public void onActivate(String action) {

	}

	@Override
	/**
	 * @TODO save the game
	 */
	public void onDeactivate() {

	}

	@Override
	public void update() {
		mWorld.step(Gdx.graphics.getDeltaTime(), 6, 2);

		/** @TODO Move the camera */
	}

	@Override
	public void render() {
		if (DebugOptions.Graphics.USE_DEBUG_RENDERER) {
			mDebugRenderer.render(mWorld, mCamera.combined);
		} else {
			super.render();
		}
	}

	/**
	 * The Box2D physical world
	 */
	private final World mWorld;
	/**
	 * Displays nice render graphics for all physical objects.
	 */
	private Box2DDebugRenderer mDebugRenderer;
	/**
	 * Camera for the world
	 */
	private final Camera mCamera;

}

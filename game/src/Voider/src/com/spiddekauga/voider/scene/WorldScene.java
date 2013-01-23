package com.spiddekauga.voider.scene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.game.Actor;

/**
 * Common class for all world scenes
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public abstract class WorldScene extends Scene {
	/**
	 * Default constructor
	 */
	public WorldScene() {
		mWorld = new World(new Vector2(), true);
		fixCamera();
		Actor.setWorld(mWorld);
	}

	@Override
	public void onResize(int width, int height) {
		super.onResize(width, height);
		fixCamera();
	}

	@Override
	public void update() {
		mWorld.step(1/60f, 6, 2);
	}

	@Override
	public void render() {
		super.render();
		if (Config.Graphics.USE_DEBUG_RENDERER) {
			mDebugRenderer.render(mWorld, mCamera.combined);
		}
	}

	/**
	 * Fixes the camera resolution
	 */
	protected void fixCamera() {
		float width = Gdx.graphics.getWidth() * Config.Graphics.WORLD_SCALE;
		// Decrease scale of width depending on height scaled
		float heightScale = Config.Graphics.HEIGHT / Gdx.graphics.getHeight();
		width *= heightScale;
		mCamera = new OrthographicCamera(width , Config.Graphics.HEIGHT * Config.Graphics.WORLD_SCALE);
	}

	/** Physics world */
	protected World mWorld = null;
	/** Camera for the editor */
	protected Camera mCamera = null;

	/** Debug renderer */
	private Box2DDebugRenderer mDebugRenderer = new Box2DDebugRenderer();
}

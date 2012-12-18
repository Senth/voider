package com.spiddekauga.voider.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.Scene;
/**
 * The main game. Starts with a level and could either be in regular or
 * testing mode. Testing mode will set the player to unlimited lives.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class GameScene extends Scene {
	/**
	 * Initializes the game scene.
	 * @param testing if we're just testing the level, i.e. unlimited lives
	 * but still has health. Scoring will still be used (player could be testing
	 * scoring).
	 */
	public GameScene(boolean testing) {
		super();

		mTesting = testing;

		/** TODO fix aspect ratio */
		mWorld = new World(new Vector2(0, 0), true);
		mCamera = new OrthographicCamera(80, 48);

		if (Config.Graphics.USE_DEBUG_RENDERER) {
			mDebugRenderer = new Box2DDebugRenderer();
		}

		/** TODO use different shaders */
	}

	/**
	 * Sets the level that shall be played
	 * @param level level to play
	 */
	public void setLevel(Level level) {
		mLevel = level;
	}

	@Override
	public void onActivate(String action) {

	}

	/**
	 * Saves the game if we're not testing
	 */
	@Override
	public void onDeactivate() {
		if (!mTesting) {
			/** @TODO save the game */
		}
	}

	@Override
	public void update() {
		mWorld.step(Gdx.graphics.getDeltaTime(), 6, 2);
		mLevel.update(true);

		/** @TODO Move the camera relative to the level */
		mCamera.position.x = mLevel.getXCoord();
		mCamera.update();
	}

	@Override
	public void render() {
		if (Config.Graphics.USE_DEBUG_RENDERER) {
			mDebugRenderer.render(mWorld, mCamera.combined);
		} else {
			mLevel.render(mSpriteBatch);
			super.render();
		}
	}

	/**
	 * @return physics world of the game scene
	 */
	@Override
	public World getWorld() {
		return mWorld;
	}

	/** The Box2D physical world */
	private final World mWorld;
	/** Displays nice render graphics for all physical objects. */
	private Box2DDebugRenderer mDebugRenderer = null;
	/** The current level used in the game */
	private Level mLevel = null;
	/** If we're just testing */
	private boolean mTesting;
	/** Camera for the world */
	private final Camera mCamera;

}

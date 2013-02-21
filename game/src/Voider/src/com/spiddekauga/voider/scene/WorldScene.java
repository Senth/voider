package com.spiddekauga.voider.scene;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.ChainShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Pools;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.game.BulletDestroyer;
import com.spiddekauga.voider.game.actors.Actor;
import com.spiddekauga.voider.game.actors.ActorFilterCategories;

/**
 * Common class for all world scenes
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public abstract class WorldScene extends Scene {
	/**
	 * @param gui the GUI to use for the scene
	 */
	public WorldScene(Gui gui) {
		super(gui);
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

		// Remove unwanted bullets
		Vector2 minScreenPos = Pools.obtain(Vector2.class);
		Vector2 maxScreenPos = Pools.obtain(Vector2.class);

		screenToWorldCoord(mCamera, 0, Gdx.graphics.getHeight(), minScreenPos, false);
		screenToWorldCoord(mCamera, Gdx.graphics.getWidth(), 0, maxScreenPos, false);
		mBulletDestroyer.update(Gdx.graphics.getDeltaTime());
		mBulletDestroyer.removeOutOfBondsBullets(minScreenPos, maxScreenPos);

		Pools.free(minScreenPos);
		Pools.free(maxScreenPos);
	}

	@Override
	public void render() {
		super.render();
		if (Config.Graphics.USE_DEBUG_RENDERER) {
			mDebugRenderer.render(mWorld, mCamera.combined);
		}
	}

	@Override
	BulletDestroyer getBulletDestroyer() {
		return mBulletDestroyer;
	}

	@Override
	public void onDisposed() {
		mBulletDestroyer.dispose();

		super.onDisposed();
	}

	/**
	 * Fixes the camera resolution
	 */
	protected void fixCamera() {
		float width = Gdx.graphics.getWidth() * Config.Graphics.WORLD_SCALE;
		// Decrease scale of width depending on height scaled
		float heightScale = Config.Graphics.HEIGHT / Gdx.graphics.getHeight();
		width *= heightScale;
		float height = Config.Graphics.HEIGHT * Config.Graphics.WORLD_SCALE;

		if (mCamera != null) {
			mCamera.viewportHeight = height;
			mCamera.viewportWidth = width;
			mCamera.update();
		} else {
			mCamera = new OrthographicCamera(width , height);
		}
	}

	/**
	 * Creates the border around the screen so the player can't escape
	 */
	protected void createBorder() {
		// If body already exists, just delete existing fixtures
		if (mBorderBody != null) {
			ArrayList<Fixture> fixtures = mBorderBody.getFixtureList();
			for (Fixture fixture : fixtures) {
				mBorderBody.destroyFixture(fixture);
			}
		}
		// No body, create one
		else {
			BodyDef bodyDef = new BodyDef();
			bodyDef.type = BodyType.KinematicBody;
			mBorderBody = mWorld.createBody(bodyDef);
		}


		// Get world coordinates for the screen's corners
		Vector2[] corners = new Vector2[4];
		for (int i = 0; i < corners.length; ++i) {
			corners[i] = Pools.obtain(Vector2.class);
		}
		screenToWorldCoord(mCamera, 0, 0, corners[0], false);
		screenToWorldCoord(mCamera, Gdx.graphics.getWidth(), 0, corners[1], false);
		screenToWorldCoord(mCamera, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), corners[2], false);
		screenToWorldCoord(mCamera, 0, Gdx.graphics.getHeight(), corners[3], false);


		// Create fixture
		ChainShape shape = new ChainShape();
		shape.createLoop(corners);
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = shape;
		fixtureDef.filter.categoryBits = ActorFilterCategories.SCREEN_BORDER;
		fixtureDef.filter.maskBits = ActorFilterCategories.PLAYER;
		mBorderBody.createFixture(fixtureDef);


		// Free stuff
		for (int i = 0; i < corners.length; ++i) {
			Pools.free(corners[i]);
		}
		shape.dispose();
	}

	/** Physics world */
	protected World mWorld = null;
	/** Camera for the editor */
	protected Camera mCamera = null;
	/** Border around the screen so the player can't "escape" */
	protected Body mBorderBody = null;
	/** Bullet destroyer for this scene */
	protected BulletDestroyer mBulletDestroyer = new BulletDestroyer();

	/** Debug renderer */
	private Box2DDebugRenderer mDebugRenderer = new Box2DDebugRenderer();
}

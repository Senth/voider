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
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.game.BulletDestroyer;
import com.spiddekauga.voider.game.actors.Actor;
import com.spiddekauga.voider.game.actors.ActorFilterCategories;
import com.spiddekauga.voider.utils.Geometry;
import com.spiddekauga.voider.utils.Pools;

/**
 * Common class for all world scenes
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public abstract class WorldScene extends Scene {
	/**
	 * @param gui the GUI to use for the scene
	 * @param pickRadius picking radius for editors
	 */
	public WorldScene(Gui gui, float pickRadius) {
		super(gui);
		fixCamera();
		mWorld = new World(new Vector2(), true);
		Actor.setWorld(mWorld);

		if (pickRadius > 0) {
			initPickingCircle(pickRadius);
		}
	}

	@Override
	public void onResize(int width, int height) {
		super.onResize(width, height);
		fixCamera();
		mShapeRenderer.setProjectionMatrix(mCamera.combined);
	}

	@Override
	protected void update() {
		mWorld.step(1/60f, 6, 2);

		// Remove unwanted bullets
		Vector2 minScreenPos = Pools.vector2.obtain();
		Vector2 maxScreenPos = Pools.vector2.obtain();

		screenToWorldCoord(mCamera, 0, Gdx.graphics.getHeight(), minScreenPos, false);
		screenToWorldCoord(mCamera, Gdx.graphics.getWidth(), 0, maxScreenPos, false);
		mBulletDestroyer.update(Gdx.graphics.getDeltaTime());
		mBulletDestroyer.removeOutOfBondsBullets(minScreenPos, maxScreenPos);

		Pools.vector2.free(minScreenPos);
		Pools.vector2.free(maxScreenPos);
	}

	@Override
	protected void render() {
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
	public void onDispose() {
		mBulletDestroyer.dispose();

		super.onDispose();
	}

	/**
	 * Return screen width in world coordinates, but only if this scene
	 * is a world scene.
	 * @return screen width in world coordinates, if scene is not a world it return 0.
	 */
	@Override
	public float getWorldWidth() {
		return mCamera.viewportWidth;
	}

	/**
	 * Return screen height in world coordinates, but only if this scene is
	 * a world scene.
	 * @return screen height in world coordinates, if scene is not a world it return 0.
	 */
	@Override
	public float getWorldHeight() {
		return mCamera.viewportHeight;
	}

	/**
	 * @return 0,0 of screen in world coordinates, null if current scene isn't a world
	 * scene. Remember to free the returned vector with
	 * Pools.vector2.free(returnedVector);
	 */
	@Override
	public Vector2 getWorldMinCoordinates() {
		Vector2 minPos = Pools.vector2.obtain();
		screenToWorldCoord(mCamera, 0, Gdx.graphics.getHeight(), minPos, false);
		return minPos;
	}

	/**
	 * @return screenWidth,screenHeight in world coordinates, null if current scene
	 * isn't a world scene. Remember to free the returned vector with
	 * Pools.vector2.free(returnedVector);
	 */
	@Override
	public Vector2 getWorldMaxCoordinates() {
		Vector2 maxPos = Pools.vector2.obtain();
		screenToWorldCoord(mCamera, Gdx.graphics.getWidth(), 0, maxPos, false);
		return maxPos;
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
			ArrayList<Fixture> fixtures = new ArrayList<Fixture>();
			fixtures.addAll(mBorderBody.getFixtureList());
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
			corners[i] = Pools.vector2.obtain();
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
			Pools.vector2.free(corners[i]);
		}
		shape.dispose();
	}

	@Override
	public FixtureDef getPickingFixtureDef() {
		return mPickingFixtureDef;
	}

	@Override
	public ArrayList<Vector2> getPickingVertices() {
		return mPickingVertices;
	}

	/**
	 * Initializes the picking fixture and vertices
	 * @param pickRadius size of the picking circle
	 */
	public void initPickingCircle(float pickRadius) {
		CircleShape circleShape = new CircleShape();
		circleShape.setRadius(pickRadius);
		mPickingFixtureDef = new FixtureDef();
		mPickingFixtureDef.filter.categoryBits = ActorFilterCategories.NONE;
		mPickingFixtureDef.filter.maskBits = ActorFilterCategories.NONE;
		mPickingFixtureDef.shape = circleShape;
		mPickingVertices = Geometry.createCircle(pickRadius);
	}

	@Override
	public void write(Json json) {
		super.write(json);

		json.writeValue("mBulletDestroyer", mBulletDestroyer);
	}

	@Override
	public void read(Json json, JsonValue jsonData) {
		super.read(json, jsonData);

		mBulletDestroyer = json.readValue("mBulletDestroyer", BulletDestroyer.class, jsonData);
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
	/** Picking fixture definition (for body) */
	private FixtureDef mPickingFixtureDef = null;
	/** Picking vertices (for drawing) */
	private ArrayList<Vector2> mPickingVertices = null;
}

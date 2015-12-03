package com.spiddekauga.voider.scene;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.spiddekauga.utils.Collections;
import com.spiddekauga.utils.Maths;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.game.BulletDestroyer;
import com.spiddekauga.voider.game.actors.Actor;
import com.spiddekauga.voider.game.actors.ActorFilterCategories;
import com.spiddekauga.voider.utils.BoundingBox;
import com.spiddekauga.voider.utils.Geometry;
import com.spiddekauga.voider.utils.event.EventDispatcher;
import com.spiddekauga.voider.utils.event.EventTypes;
import com.spiddekauga.voider.utils.event.GameEvent;
import com.spiddekauga.voider.utils.event.IEventListener;

/**
 * Common class for all world scenes
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public abstract class WorldScene extends Scene {
	/**
	 * @param gui the GUI to use for the scene
	 * @param pickRadius picking radius for editors
	 */
	public WorldScene(Gui gui, float pickRadius) {
		super(gui);

		EventDispatcher.getInstance().connect(EventTypes.CAMERA_ZOOM_CHANGE, mZoomListener);
		mPickingRadius = pickRadius;
	}

	@Override
	protected void onInit() {
		super.onInit();

		fixCamera();

		if (mPickingRadius > 0) {
			createPickingCircle(mPickingRadius);
		}

		resetScreenToWorldScale();
	}

	@Override
	protected void onActivate(Outcomes outcome, Object message, Outcomes loadingOutcome) {
		super.onActivate(outcome, message, loadingOutcome);

		Actor.setWorld(mWorld);
	}

	@Override
	protected void onDeactivate() {
		super.onDeactivate();

		Actor.setWorld(null);
	}

	@Override
	protected void onResize(int width, int height) {
		super.onResize(width, height);
		fixCamera();
		mShapeRenderer.setProjectionMatrix(mCamera.combined);
		resetScreenToWorldScale();

		// Update size of border
		if (mBorderBody != null) {
			createBorder();
		}
	}

	/**
	 * Resets screen to world scale
	 */
	private void resetScreenToWorldScale() {
		mScreenToWorldScale = mCamera.viewportWidth / Gdx.graphics.getWidth();
	}

	/**
	 * @return screen to world scale
	 */
	public float getScreenToWorldScale() {
		float cameraZoom = 1;
		if (mCamera != null) {
			cameraZoom = mCamera.zoom;
		}
		return mScreenToWorldScale * cameraZoom;
	}

	@Override
	protected void update(float deltaTime) {
		super.update(deltaTime);
		mWorld.step(1 / 60f, 6, 2);

		// Remove unwanted bullets
		updateBoundingBox();
		mBulletDestroyer.update(deltaTime);
		mBulletDestroyer.removeOutOfBondsBullets(getBoundingBoxWorld());
	}

	/**
	 * Synchronize collision border
	 * @param border the border to synchronize
	 */
	protected void synchronizeBorder(Body border) {
		float borderDiffPosition = border.getPosition().x - mCamera.position.x;
		if (!Maths.approxCompare(borderDiffPosition, Config.Game.BORDER_SYNC_THRESHOLD)) {
			border.setTransform(mCamera.position.x, mCamera.position.y, 0);
		}
	}

	/**
	 * Calls {@link #synchronizeBorder(Body)} with the correct body
	 */
	protected final void synchronizeBorder() {
		synchronizeBorder(mBorderBody);
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
	protected void onDispose() {
		mBulletDestroyer.dispose();
		EventDispatcher.getInstance().disconnect(EventTypes.CAMERA_ZOOM_CHANGE, mZoomListener);

		super.onDispose();
	}

	/**
	 * Return screen width in world coordinates, but only if this scene is a world scene.
	 * @return screen width in world coordinates, if scene is not a world it return 0.
	 */
	@Override
	protected float getWorldWidth() {
		return mCamera.viewportWidth;
	}

	/**
	 * Return screen height in world coordinates, but only if this scene is a world scene.
	 * @return screen height in world coordinates, if scene is not a world it return 0.
	 */
	@Override
	protected float getWorldHeight() {
		return mCamera.viewportHeight;
	}

	/**
	 * @return bounding box off the screen in world coordinates
	 */
	public BoundingBox getBoundingBoxWorld() {
		return mWindowBox;
	}

	/**
	 * @return 0,0 (lower left corner) of screen in world coordinates, null if current
	 *         scene isn't a world scene.
	 */
	@Override
	public Vector2 getWorldMinCoordinates() {
		Vector2 minPos = new Vector2();
		screenToWorldCoord(mCamera, 0, Gdx.graphics.getHeight(), minPos, false);
		return minPos;
	}

	/**
	 * @return screenWidth,screenHeight (upper right corner) in world coordinates, null if
	 *         current scene isn't a world scene.
	 */
	@Override
	public Vector2 getWorldMaxCoordinates() {
		Vector2 maxPos = new Vector2();
		screenToWorldCoord(mCamera, Gdx.graphics.getWidth(), 0, maxPos, false);
		return maxPos;
	}

	/**
	 * Fixes the camera resolution
	 */
	protected void fixCamera() {
		float width = calculateDefaultWorldWidth();
		float height = Config.Graphics.HEIGHT_DEFAULT * Config.Graphics.WORLD_SCALE;

		if (mCamera != null) {
			mCamera.viewportHeight = height;
			mCamera.viewportWidth = width;
			mCamera.update();
		} else {
			mCamera = new OrthographicCamera(width, height);
		}
	}

	/**
	 * Calculate the default world width. As the height needs to be scaled to fit the
	 * window the width isn't just scaled with the world scale
	 * @return default world width when the height is scaled
	 */
	protected float calculateDefaultWorldWidth() {
		float heightScale = ((float) Config.Graphics.HEIGHT_DEFAULT) / Gdx.graphics.getHeight();
		float width = Gdx.graphics.getWidth() * Config.Graphics.WORLD_SCALE * heightScale;
		return width;
	}

	/**
	 * Creates the border around the screen so the player can't escape
	 */
	protected void createBorder() {
		// If body already exists, just delete existing fixtures
		if (mBorderBody != null) {
			ArrayList<Fixture> fixtures = new ArrayList<Fixture>();
			Collections.addAll(mBorderBody.getFixtureList(), fixtures);
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


		Vector2[][] boxes = getBorderBoxes();

		for (Vector2[] box : boxes) {
			FixtureDef fixtureDef = createBorderFixture(box);
			mBorderBody.createFixture(fixtureDef);
			fixtureDef.shape.dispose();
		}

		// Set position
		synchronizeBorder(mBorderBody);
	}

	/**
	 * Create border fixture
	 * @param vertices all the vertices for a fixture, should be of length 4
	 * @return new fixture definition from the vertices
	 */
	protected FixtureDef createBorderFixture(Vector2[] vertices) {
		if (vertices.length != 4) {
			throw new IllegalArgumentException("vertices not of length 4");
		}
		Geometry.makePolygonCounterClockwise(vertices);

		PolygonShape polygonShape = new PolygonShape();
		polygonShape.set(vertices);

		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = polygonShape;
		fixtureDef.filter.categoryBits = ActorFilterCategories.SCREEN_BORDER;
		fixtureDef.filter.maskBits = ActorFilterCategories.PLAYER;
		fixtureDef.density = 1;

		return fixtureDef;
	}

	/**
	 * Called when creating a border. Can be overridden to specify other border
	 * dimensions.
	 * @return all 4 border corners/vertices
	 */
	protected Vector2[][] getBorderBoxes() {
		Vector2[][] boxes = new Vector2[4][4];

		updateBoundingBox();
		BoundingBox boundingBox = getBoundingBoxWorld();
		Vector2 cameraPos = getCameraPos();

		float left = boundingBox.getLeft() - cameraPos.x;
		float right = boundingBox.getRight() - cameraPos.x;
		float top = boundingBox.getTop() - cameraPos.y;
		float bottom = boundingBox.getBottom() - cameraPos.y;

		float width = 10;

		// Left side
		boxes[0][0] = new Vector2(left, bottom - width);
		boxes[0][1] = new Vector2(left - width, bottom - width);
		boxes[0][2] = new Vector2(left - width, top + width);
		boxes[0][3] = new Vector2(left, top + width);

		// Right side
		boxes[1][0] = new Vector2(right + width, bottom - width);
		boxes[1][1] = new Vector2(right, bottom - width);
		boxes[1][2] = new Vector2(right, top + width);
		boxes[1][3] = new Vector2(right + width, top + width);

		// Bottom side
		boxes[2][0] = new Vector2(right, bottom - width);
		boxes[2][1] = new Vector2(left, bottom - width);
		boxes[2][2] = new Vector2(left, bottom);
		boxes[2][3] = new Vector2(right, bottom);

		// Top side
		boxes[3][0] = new Vector2(right, top);
		boxes[3][1] = new Vector2(left, top);
		boxes[3][2] = new Vector2(left, top + width);
		boxes[3][3] = new Vector2(right, top + width);

		return boxes;
	}

	@Override
	protected FixtureDef getPickingFixtureDef() {
		return mPickingFixtureDef;
	}

	@Override
	protected List<Vector2> getPickingVertices() {
		return mPickingVertices;
	}

	/**
	 * Initializes the picking fixture and vertices
	 * @param pickRadius size of the picking circle
	 */
	public void createPickingCircle(float pickRadius) {
		CircleShape circleShape = new CircleShape();
		circleShape.setRadius(pickRadius);
		mPickingFixtureDef = new FixtureDef();
		mPickingFixtureDef.filter.categoryBits = ActorFilterCategories.NONE;
		mPickingFixtureDef.filter.maskBits = ActorFilterCategories.NONE;
		mPickingFixtureDef.shape = circleShape;
		mPickingVertices = Geometry.createCircle(pickRadius, mCamera.zoom);
	}

	/**
	 * @return world of the current scene
	 */
	public World getWorld() {
		return mWorld;
	}

	/**
	 * @return camera of the current scene
	 */
	public OrthographicCamera getCamera() {
		return mCamera;
	}

	/**
	 * Called when zoom has been changed
	 */
	private void onZoom() {
		createPickingCircle(mPickingRadius * mCamera.zoom);
	}

	/** Listens to zoom changes */
	private IEventListener mZoomListener = new IEventListener() {
		@Override
		public void handleEvent(GameEvent event) {
			onZoom();
		}
	};

	/**
	 * Update the bounding box to the current world
	 */
	private void updateBoundingBox() {
		Vector2 min = getWorldMinCoordinates();
		Vector2 max = getWorldMaxCoordinates();

		mWindowBox.setLeft(min.x);
		mWindowBox.setRight(max.x);
		mWindowBox.setTop(max.y);
		mWindowBox.setBottom(min.y);
	}

	/**
	 * @return camera position
	 */
	protected Vector2 getCameraPos() {
		return new Vector2(mCamera.position.x, mCamera.position.y);
	}

	/** Window bounding box */
	private BoundingBox mWindowBox = new BoundingBox();
	/** Physics world */
	protected World mWorld = new World(new Vector2(), true);
	/** Camera for the editor */
	protected OrthographicCamera mCamera = null;
	/** Border around the screen so the player ship can't escape */
	protected Body mBorderBody = null;
	/** Bullet destroyer for this scene */
	protected BulletDestroyer mBulletDestroyer = new BulletDestroyer();
	/** Screen to world scale */
	private float mScreenToWorldScale = 0;
	/** Picking radius */
	private float mPickingRadius;
	/** Debug renderer */
	private Box2DDebugRenderer mDebugRenderer = new Box2DDebugRenderer();
	/** Picking fixture definition (for body) */
	private FixtureDef mPickingFixtureDef = null;
	/** Picking vertices (for drawing) */
	private List<Vector2> mPickingVertices = null;
}

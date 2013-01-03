package com.spiddekauga.voider.game;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.Scene;
import com.spiddekauga.voider.game.actors.PlayerActor;
import com.spiddekauga.voider.game.actors.PlayerActorDef;
import com.spiddekauga.voider.ui.UiEvent;
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

		/** @TODO fix aspect ratio */
		mWorld = new World(new Vector2(0, 0), true);
		mCamera = new OrthographicCamera(80, 48);


		// Initialize player
		Actor.setWorld(mWorld);
		Actor.setEditorActive(false);

		FixtureDef fixtureDef = new FixtureDef();
		CircleShape circleShape = new CircleShape();
		circleShape.setRadius(1.0f);
		fixtureDef.shape = circleShape;
		PlayerActorDef def = new PlayerActorDef(100.0f, null, "Normal", fixtureDef);
		mPlayerActor = new PlayerActor(def);


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
	public void onReActivate(Outcomes outcome, String message) {
		/** @TODO loading done */

		/** @TODO game completed, aborted? */
	}

	/**
	 * Saves the game if we're not testing from the Editor.
	 */
	@Override
	public void onDisposed() {
		if (!mTesting) {
			/** @TODO save the game */
		}
	}

	@Override
	public void update() {
		// Set player velocity (so it moves along with the screen)
		mPlayerActor.getBody().setLinearVelocity(mLevel.getSpeed(), 0.0f);
		mWorld.step(1/60f, 6, 2);
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
			mPlayerActor.render(mSpriteBatch);
			super.render();
		}
	}


	// --------------------------------
	//				EVENTS
	// --------------------------------
	/* (non-Javadoc)
	 * @see com.spiddekauga.voider.ui.IUiListener#onUiEvent(com.spiddekauga.voider.ui.UiEvent)
	 */
	@Override
	public void onUiEvent(UiEvent event) {
		/** @TODO handle ui events */
	}

	@Override
	public boolean touchDown(int x, int y, int pointer, int button) {
		// Test if touching player
		if (mPlayerPointer == INVALID_POINTER) {
			mTestPoint.set(x, y, 0);
			mCamera.unproject(mTestPoint);

			mHitBody = null;
			mWorld.QueryAABB(mCallback, mTestPoint.x - 0.0001f, mTestPoint.y - 0.0001f, mTestPoint.x + 0.0001f, mTestPoint.y + 0.0001f);

			if (mHitBody == mPlayerActor.getBody()) {
				mPlayerPointer = pointer;
				Body playerBody = mPlayerActor.getBody();
				playerBody.setTransform(mTestPoint.x, mTestPoint.y, playerBody.getAngle());
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean touchDragged(int x, int y, int pointer) {
		if (mPlayerPointer == pointer) {
			mTestPoint.set(x, y, 0);
			mCamera.unproject(mTestPoint);
			Body playerBody = mPlayerActor.getBody();
			playerBody.setTransform(mTestPoint.x, mTestPoint.y, playerBody.getAngle());
			return true;
		}

		return false;
	}

	@Override
	public boolean touchUp(int x, int y, int pointer, int button) {
		if (mPlayerPointer == pointer) {
			mPlayerPointer = INVALID_POINTER;
		}

		return false;
	}


	/** Invalid pointer id */
	private static final int INVALID_POINTER = -1;
	/** The Box2D physical world */
	private final World mWorld;
	/** Displays nice render graphics for all physical objects. */
	private Box2DDebugRenderer mDebugRenderer = new Box2DDebugRenderer();
	/** The current level used in the game */
	private Level mLevel = null;
	/** If we're just testing */
	private boolean mTesting;
	/** Camera for the world */
	private final Camera mCamera;
	/** Player ship actor, plays the game */
	private PlayerActor mPlayerActor;
	/** Current pointer that moves the player */
	private int mPlayerPointer = INVALID_POINTER;
	/** Last known location of the pointer (on the screen) */
	private Vector2 mLastPointer;


	// Temporary variables
	/** For ray testing on player ship when touching it */
	private Vector3 mTestPoint = new Vector3();
	/** Body that was hit */
	private Body mHitBody = null;
	/** Callback for "ray testing" */
	private QueryCallback mCallback = new QueryCallback() {
		@Override
		public boolean reportFixture(Fixture fixture) {
			if (fixture.testPoint(mTestPoint.x, mTestPoint.y)) {
				mHitBody = fixture.getBody();
				return false;
			} else {
				return true;
			}
		}
	};
}

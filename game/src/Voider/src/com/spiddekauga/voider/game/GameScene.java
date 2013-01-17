package com.spiddekauga.voider.game;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.MouseJoint;
import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.game.actors.PlayerActor;
import com.spiddekauga.voider.game.actors.PlayerActorDef;
import com.spiddekauga.voider.scene.Scene;
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


		/** @TODO remove the player creation, it shall be created in level instead */
		FixtureDef fixtureDef = new FixtureDef();
		CircleShape circleShape = new CircleShape();
		circleShape.setRadius(1.0f);
		fixtureDef.friction = 0.0f;
		fixtureDef.restitution = 0.0f;
		fixtureDef.density = 0.0001f;
		fixtureDef.shape = circleShape;
		PlayerActorDef def = new PlayerActorDef(100.0f, null, "Normal", fixtureDef);
		mPlayerActor = new PlayerActor(def);
		mPlayerActor.createBody();


		// Create mouse joint
		BodyDef bodyDef = new BodyDef();
		mMouseBody = mWorld.createBody(bodyDef);
		mMouseJointDef.bodyA = mMouseBody;
		mMouseJointDef.bodyB = mPlayerActor.getBody(); // TODO REMOVE, set in onActivate instead
		mMouseJointDef.collideConnected = true;
		mMouseJointDef.maxForce = 10000000000000.0f;

		/** TODO use different shaders */
	}

	/**
	 * Sets the level that shall be played
	 * @param level level to play
	 */
	public void setLevel(Level level) {
		mLevel = level;
		mLevel.setPlayer(mPlayerActor);
	}

	@Override
	public void onActivate(Outcomes outcome, String message) {
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
		mWorld.step(1/60f, 6, 2);
		mLevel.update(true);

		if (mMouseBody != null) {
			mMouseBody.setLinearVelocity(mLevel.getSpeed(), 0.0f);
		}

		/** @TODO Move the camera relative to the level */
		mCamera.position.x = mLevel.getXCoord() + mCamera.viewportWidth * 0.5f;
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

	// --------------------------------
	//		Resource loading etc.
	// --------------------------------
	@Override
	public boolean hasResources() {
		return true;
	}


	// --------------------------------
	//				EVENTS
	// --------------------------------
	@Override
	public boolean touchDown(int x, int y, int pointer, int button) {
		// Test if touching player
		if (mPlayerPointer == INVALID_POINTER) {
			mTestPoint.set(x, y, 0);
			mCamera.unproject(mTestPoint);

			mWorld.QueryAABB(mCallback, mTestPoint.x - 0.0001f, mTestPoint.y - 0.0001f, mTestPoint.x + 0.0001f, mTestPoint.y + 0.0001f);

			if (mMovingPlayer) {
				mPlayerPointer = pointer;

				mMouseJointDef.target.set(mTestPoint.x, mTestPoint.y);
				mMouseJoint = (MouseJoint) mWorld.createJoint(mMouseJointDef);
				mPlayerActor.getBody().setAwake(true);

				//				Body playerBody = mPlayerActor.getBody();
				//				playerBody.setTransform(mTestPoint.x, mTestPoint.y, playerBody.getAngle());
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean touchDragged(int x, int y, int pointer) {
		if (mPlayerPointer == pointer && mMovingPlayer) {
			mTestPoint.set(x, y, 0);
			mCamera.unproject(mTestPoint);

			mJointTarget.set(mTestPoint.x, mTestPoint.y);
			mMouseJoint.setTarget(mJointTarget);

			//			Body playerBody = mPlayerActor.getBody();
			//			playerBody.setTransform(mTestPoint.x, mTestPoint.y, playerBody.getAngle());
			return true;
		}

		return false;
	}

	@Override
	public boolean touchUp(int x, int y, int pointer, int button) {
		if (mPlayerPointer == pointer && mMovingPlayer) {
			mPlayerPointer = INVALID_POINTER;
			mMovingPlayer = false;

			mWorld.destroyJoint(mMouseJoint);
			mMouseJoint = null;

			return true;
		}

		return false;
	}

	@Override
	public boolean keyDown(int keycode) {
		// Set level as complete if we want to go back while testing
		if (mTesting && (keycode == Keys.ESCAPE || keycode == Keys.BACK)) {
			setOutcome(Outcomes.LEVEL_QUIT);
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
	/** If we're currently moving the player */
	private boolean mMovingPlayer = false;

	// MOUSE JOINT
	/** Mouse joint definition */
	private MouseJointDef mMouseJointDef = new MouseJointDef();
	/** Mouse joint for player */
	private MouseJoint mMouseJoint = null;
	/** Target for the mouse joint */
	private Vector2 mJointTarget = new Vector2();
	/** Body of the mouse, for mouse joint */
	private Body mMouseBody = null;


	// Temporary variables
	/** For ray testing on player ship when touching it */
	private Vector3 mTestPoint = new Vector3();
	/** Body that was hit */
	//	private Body mHitBody = null;
	/** Callback for "ray testing" */
	private QueryCallback mCallback = new QueryCallback() {
		@Override
		public boolean reportFixture(Fixture fixture) {
			if (fixture.testPoint(mTestPoint.x, mTestPoint.y)) {
				if (fixture.getBody().getUserData() instanceof PlayerActor) {
					mMovingPlayer = true;
					return false;
				}
			}
			return true;
		}
	};
}

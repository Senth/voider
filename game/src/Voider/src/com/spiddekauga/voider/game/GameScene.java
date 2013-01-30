package com.spiddekauga.voider.game;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.joints.MouseJoint;
import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.game.actors.PlayerActor;
import com.spiddekauga.voider.scene.WorldScene;
/**
 * The main game. Starts with a level and could either be in regular or
 * testing mode. Testing mode will set the player to unlimited lives.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class GameScene extends WorldScene {
	/**
	 * Initializes the game scene.
	 * @param testing if we're just testing the level, i.e. unlimited lives
	 * but still has health. Scoring will still be used (player could be testing
	 * scoring).
	 */
	public GameScene(boolean testing) {
		super(null);
		mTesting = testing;

		Actor.setEditorActive(false);
		mWorld.setContactListener(mCollisionResolver);


		mPlayerActor = new PlayerActor();
		mPlayerActor.createBody();


		// Create mouse joint
		BodyDef bodyDef = new BodyDef();
		mMouseBody = mWorld.createBody(bodyDef);
		mMouseJointDef.frequencyHz = Config.Game.MouseJoint.FREQUENCY;
		mMouseJointDef.bodyA = mMouseBody;
		mMouseJointDef.bodyB = mPlayerActor.getBody(); // TODO REMOVE, set in onActivate instead
		mMouseJointDef.collideConnected = true;
		mMouseJointDef.maxForce = Config.Game.MouseJoint.FORCE_MAX;

		/** TODO use different shaders */
	}

	/**
	 * Sets the level that shall be played
	 * @param level level to play
	 */
	public void setLevel(Level level) {
		mLevel = level;
		mLevel.setPlayer(mPlayerActor);

		createBorder();
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
		// Make sure border maintains same speed as level
		if (mBorderBody != null) {
			mBorderBody.setLinearVelocity(mLevel.getSpeed(), 0.0f);
		}

		// Update mouse position even when still
		if (mMouseBody != null && mMovingPlayer) {
			screenToWorldCoord(mCamera, mCursorScreen, mCursorWorld, true);
			mMouseJoint.setTarget(mCursorWorld);
		}
		super.update();
		mLevel.update(true);

		/** @TODO Move the camera relative to the level */
		mCamera.position.x = mLevel.getXCoord() + mCamera.viewportWidth * 0.5f;
		mCamera.update();

		// Is the player dead?
		if (mPlayerActor.getLife() <= 0 && !mTesting) {
			setOutcome(Outcomes.LEVEL_PLAYER_DIED);
		}
	}

	@Override
	public void render() {
		super.render();

		if (!Config.Graphics.USE_DEBUG_RENDERER) {
			mLevel.render(mSpriteBatch);
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
			mCursorScreen.set(x, y);
			screenToWorldCoord(mCamera, x, y, mCursorWorld, true);

			mWorld.QueryAABB(mCallback, mCursorWorld.x - 0.0001f, mCursorWorld.y - 0.0001f, mCursorWorld.x + 0.0001f, mCursorWorld.y + 0.0001f);

			if (mMovingPlayer) {
				mPlayerPointer = pointer;

				mMouseJointDef.target.set(mPlayerActor.getBody().getPosition());
				mMouseJoint = (MouseJoint) mWorld.createJoint(mMouseJointDef);
				mPlayerActor.getBody().setAwake(true);

				return true;
			}
		}

		return false;
	}

	@Override
	public boolean touchDragged(int x, int y, int pointer) {
		if (mPlayerPointer == pointer && mMovingPlayer) {
			mCursorScreen.set(x, y);
			return true;
		}

		return false;
	}

	@Override
	public boolean touchUp(int x, int y, int pointer, int button) {
		if (mPlayerPointer == pointer && mMovingPlayer) {
			mCursorScreen.set(x, y);
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
	/** The current level used in the game */
	private Level mLevel = null;
	/** If we're just testing */
	private boolean mTesting;
	/** Player ship actor, plays the game */
	private PlayerActor mPlayerActor;
	/** Current pointer that moves the player */
	private int mPlayerPointer = INVALID_POINTER;
	/** If we're currently moving the player */
	private boolean mMovingPlayer = false;
	/** Handles collision between actors/bodies */
	private CollisionResolver mCollisionResolver = new CollisionResolver();

	// MOUSE JOINT
	/** Screen coordinate for the cursor */
	private Vector2 mCursorScreen = new Vector2();
	/** Mouse joint definition */
	private MouseJointDef mMouseJointDef = new MouseJointDef();
	/** Mouse joint for player */
	private MouseJoint mMouseJoint = null;
	/** Body of the mouse, for mouse joint */
	private Body mMouseBody = null;

	/** Callback for "ray testing" */
	private QueryCallback mCallback = new QueryCallback() {
		@Override
		public boolean reportFixture(Fixture fixture) {
			if (fixture.testPoint(mCursorWorld.x, mCursorWorld.y)) {
				if (fixture.getBody().getUserData() instanceof PlayerActor) {
					mMovingPlayer = true;
					return false;
				}
			}
			return true;
		}
	};

	// Temporary variables
	/** World coordinate for the cursor */
	private Vector2 mCursorWorld = new Vector2();
}

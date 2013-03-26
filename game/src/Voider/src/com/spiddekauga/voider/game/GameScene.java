package com.spiddekauga.voider.game;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.joints.MouseJoint;
import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.game.actors.Actor;
import com.spiddekauga.voider.game.actors.PlayerActor;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.resources.UndefinedResourceTypeException;
import com.spiddekauga.voider.scene.WorldScene;
import com.spiddekauga.voider.utils.Vector2Pool;
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
		super(new GameSceneGui());
		((GameSceneGui)mGui).setGameScene(this);

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
		mLevel.addResource(mLevel);
		mLevel.bindResources();
		mLevel.run();

		ArrayList<Actor> actors = mLevel.getResources(Actor.class);
		for (Actor actor : actors) {
			actor.createBody();
		}

		updateCameraPosition();
		createBorder();
		resetPlayerPosition();

		Actor.setLevelSpeed(mLevel.getSpeed());
	}

	/**
	 * Sets the level to load
	 * @param levelDef definition with all the information
	 */
	public void setLevelToLoad(LevelDef levelDef) {
		mLevelToLoad = levelDef;
	}

	@Override
	public void onResize(int width, int height) {
		super.onResize(width, height);
		updateCameraPosition();
		createBorder();
	}

	@Override
	public void onActivate(Outcomes outcome, String message) {
		Actor.setEditorActive(false);
		Actor.setPlayerActor(mPlayerActor);
		Actor.setWorld(mWorld);

		/** @TODO loading done */
		if (outcome == Outcomes.LOADING_SUCCEEDED) {
			mGui.initGui();

			if (mLevelToLoad != null) {
				try {
					Level level = ResourceCacheFacade.get(mLevelToLoad.getLevelId(), Level.class);
					setLevel(level);
				} catch (UndefinedResourceTypeException e) {
					Gdx.app.error("GameScene", e.toString());
				}
			}
		}

		/** @TODO game completed, aborted? */
	}

	@Override
	public void onDeactivate() {
		// Always reset level speed, only game scene uses it
		Actor.setLevelSpeed(0);
	}

	/**
	 * Saves the game if we're not testing from the Editor.
	 */
	@Override
	public void onDisposed() {
		if (!mTesting) {
			/** @TODO save the game */
		}

		if (mLevel != null) {
			mLevel.dispose();
		}
	}

	@Override
	protected void update() {
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
		mLevel.update();
		updateCameraPosition();

		// Is the player dead?
		if (mPlayerActor.getLife() <= 0 && !mTesting) {
			setOutcome(Outcomes.LEVEL_PLAYER_DIED);
		}
	}

	@Override
	protected void render() {
		super.render();

		if (!Config.Graphics.USE_DEBUG_RENDERER) {
			mLevel.render(mShapeRenderer);
		}
	}

	// --------------------------------
	//		Resource loading etc.
	// --------------------------------
	@Override
	public boolean hasResources() {
		return true;
	}

	@Override
	public void loadResources() {
		if (mLevelToLoad != null) {
			try {
				ResourceCacheFacade.load(mLevelToLoad.getLevelId(), Level.class, mLevelToLoad);
			} catch (UndefinedResourceTypeException e) {
				Gdx.app.error("GameScene", e.toString());
			}
		}
	}

	@Override
	public void unloadResources() {
		if (mLevelToLoad != null) {
			ResourceCacheFacade.unload(mLevel, mLevel.getDef());
			ResourceCacheFacade.unload(mLevelToLoad, true);
		}
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

	/**
	 * Updates the camera's position depending on where on the level location
	 */
	private void updateCameraPosition() {
		mCamera.position.x = mLevel.getXCoord() - mCamera.viewportWidth * 0.5f;
		mCamera.update();
	}

	/**
	 * Resets the player position
	 */
	private void resetPlayerPosition() {
		if (mPlayerActor != null && mPlayerActor.getBody() != null) {
			Vector2 playerPosition = Vector2Pool.obtain();
			playerPosition.set(mCamera.position.x - mCamera.viewportWidth * 0.5f, 0);

			// Get radius of player and offset it with the width
			ArrayList<Fixture> playerFixtures = mPlayerActor.getBody().getFixtureList();

			if (playerFixtures.size() > 0) {
				float radius = playerFixtures.get(0).getShape().getRadius();
				playerPosition.x += radius * 2;

				mPlayerActor.getBody().setTransform(playerPosition, 0.0f);
			}
			Vector2Pool.free(playerPosition);
		}
	}

	/** Invalid pointer id */
	private static final int INVALID_POINTER = -1;
	/** Level to load */
	private LevelDef mLevelToLoad = null;
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

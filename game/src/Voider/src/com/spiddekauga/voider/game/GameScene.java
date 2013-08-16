package com.spiddekauga.voider.game;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.joints.MouseJoint;
import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;
import com.spiddekauga.utils.ShapeRendererEx.ShapeType;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.game.actors.Actor;
import com.spiddekauga.voider.game.actors.PlayerActor;
import com.spiddekauga.voider.game.actors.PlayerActorDef;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.resources.ResourceNames;
import com.spiddekauga.voider.resources.ResourceSaver;
import com.spiddekauga.voider.resources.UndefinedResourceTypeException;
import com.spiddekauga.voider.scene.SceneSwitcher;
import com.spiddekauga.voider.scene.WorldScene;
import com.spiddekauga.voider.utils.Pools;
/**
 * The main game. Starts with a level and could either be in regular or
 * testing mode. Testing mode will set the player to unlimited lives.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class GameScene extends WorldScene {
	/**
	 * Initializes the game scene.
	 * @param testing if we're testing the level. This allows the player to go back using ESC or BACK key
	 * @param invulnerable set to true to make the player invulnerable while testing.
	 * Scoring will still be used (player could be testing scoring.
	 */
	public GameScene(boolean testing, boolean invulnerable) {
		super(new GameSceneGui(), Config.Editor.PICKING_CIRCLE_RADIUS_EDITOR);
		((GameSceneGui)mGui).setGameScene(this);

		mTesting = testing;
		mInvulnerable = invulnerable;

		Actor.setEditorActive(false);
		mWorld.setContactListener(mCollisionResolver);
	}

	/**
	 * Sets the level that shall be played
	 * @param level level to play
	 */
	public void setLevel(Level level) {
		mLevel = level;
		mLevel.addResource(mLevel);
		mLevel.bindResources();
		mLevel.run();
		mLevel.createDefaultTriggers();

		mBodyShepherd.setActors(mLevel.getResources(Actor.class));


		updateCameraPosition();
		createBorder();

		Actor.setLevel(mLevel);
	}

	/**
	 * Sets the level to load
	 * @param levelDef definition with all the information
	 */
	public void setLevelToLoad(LevelDef levelDef) {
		mLevelToLoad = levelDef;
	}

	/**
	 * Sets the game to resume
	 * @param gameSaveDef definition of the game save to resume
	 */
	public void setGameToResume(GameSaveDef gameSaveDef) {
		mGameSaveDef = gameSaveDef;
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

			// Load level
			if (mLevelToLoad != null) {
				try {
					Level level = ResourceCacheFacade.get(mLevelToLoad.getLevelId(), Level.class);
					setLevel(level);
				} catch (UndefinedResourceTypeException e) {
					Gdx.app.error("GameScene", e.toString());
				}
			}

			// Resume a level
			if (mGameSaveDef != null) {
				try {
					mGameSave = ResourceCacheFacade.get(mGameSaveDef.getGameSaveId(), GameSave.class);

					mPlayerActor = mGameSave.getPlayerActor();
					mPlayerStats = mGameSave.getPlayerStats();
					mBulletDestroyer = mGameSave.getBulletDestroyer();
					setLevel(mGameSave.getLevel());
				} catch (UndefinedResourceTypeException e) {
					Gdx.app.error("GameScene", e.toString());
				}
			}

			createPlayerShip();
			createMouseJoint();
		}

		/** @TODO game completed, aborted? */
	}


	@Override
	public void onDispose() {
		// Save the level
		if (!mTesting) {
			ResourceSaver.clearResources(GameSave.class);
			ResourceSaver.clearResources(GameSaveDef.class);

			if (getOutcome() == Outcomes.LEVEL_QUIT) {
				GameSave gameSave = new GameSave(mLevel, mPlayerActor, mPlayerStats, mBulletDestroyer);
				GameSaveDef gameSaveDef = new GameSaveDef(gameSave);
				ResourceSaver.save(gameSave);
				ResourceSaver.save(gameSaveDef);
			}
		}

		if (mLevel != null) {
			mLevel.dispose();
		}

		super.onDispose();
	}

	@Override
	protected void update() {
		// Make sure border maintains same speed as level
		if (mBorderBody != null) {
			mBorderBody.setLinearVelocity(mLevel.getSpeed(), 0.0f);

			/** @todo Update the border when it's too far from the original place */
		}

		// Update mouse position even when still
		if (mMouseBody != null && mMovingPlayer) {
			screenToWorldCoord(mCamera, mCursorScreen, mCursorWorld, true);
			mMouseJoint.setTarget(mCursorWorld);
		}
		super.update();

		updateBodyShepherdPositions();
		mBodyShepherd.update(mBodyShepherdMinPos, mBodyShepherdMaxPos);

		mLevel.update();
		updateCameraPosition();

		// Is the player dead? Loose a life or game over
		if (mPlayerActor.getLife() <= 0 && !mInvulnerable) {

			if (mPlayerStats.getExtraLives() > 0) {
				mPlayerActor.resetLife();
				mPlayerStats.decreaseExtraLives();
				mPlayerLifeShips.remove(mPlayerLifeShips.size() - 1).dispose();
			} else {
				setOutcome(Outcomes.LEVEL_PLAYER_DIED);
			}
		}

		// Have we reached the end of the level?
		if (mLevel.hasCompletedLevel()) {
			setOutcome(Outcomes.LEVEL_COMPLETED);
		}


		// GUI
		mGui.resetValues();
		updateLifePosition();
	}

	@Override
	protected void render() {
		super.render();

		if (Config.Graphics.USE_RELEASE_RENDERER) {
			ShaderProgram defaultShader = ResourceCacheFacade.get(ResourceNames.SHADER_DEFAULT);
			if (defaultShader != null) {
				mShapeRenderer.setShader(defaultShader);
			}
			mShapeRenderer.setProjectionMatrix(mCamera.combined);
			mShapeRenderer.push(ShapeType.Filled);
			Gdx.gl.glEnable(GL20.GL_BLEND);
			Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

			mLevel.render(mShapeRenderer);
			mBulletDestroyer.render(mShapeRenderer);
			mPlayerActor.render(mShapeRenderer);


			// GUI
			renderHealth();
			renderLives();


			mShapeRenderer.pop();
		}
	}

	/**
	 * Renders the life as on overlay on the whole map
	 */
	private void renderHealth() {
		mShapeRenderer.setColor(Config.Game.HEALTH_COLOR);

		// Calculate how big part of the window should be covered
		float healthWidth = mPlayerActor.getLife() / mPlayerActor.getDef().getMaxLife();
		healthWidth *= SceneSwitcher.getWorldWidth();

		float startPoint = mLevel.getXCoord() - SceneSwitcher.getWorldWidth();

		// Cover remaining place with bar
		if (SceneSwitcher.getWorldWidth() - healthWidth >= 1f) {
			mShapeRenderer.rect(startPoint + healthWidth, -SceneSwitcher.getWorldHeight()/2f, SceneSwitcher.getWorldWidth()-healthWidth, SceneSwitcher.getWorldHeight());
		}
	}

	/**
	 * Update life positions
	 */
	private void updateLifePosition() {
		Vector2 position = Pools.vector2.obtain();
		position.x = mLevel.getXCoord() - mPlayerActor.getBoundingRadius() - Config.Game.LIVES_OFFSET_POSITION;
		position.y = -SceneSwitcher.getWorldHeight()/2 + Config.Game.LIVES_OFFSET_POSITION + mPlayerActor.getBoundingRadius();
		for (PlayerActor lifeActor : mPlayerLifeShips) {
			lifeActor.setPosition(position);
			position.y += mPlayerActor.getBoundingRadius()*2 + Config.Game.LIVES_OFFSET_POSITION;
		}
		Pools.vector2.free(position);
	}

	/**
	 * Render lives
	 */
	private void renderLives() {
		for (PlayerActor lifeActor : mPlayerLifeShips) {
			lifeActor.render(mShapeRenderer);
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
		ResourceCacheFacade.load(ResourceNames.UI_GENERAL);
		ResourceCacheFacade.load(ResourceNames.SHADER_DEFAULT);
		ResourceCacheFacade.loadAllOf(PlayerActorDef.class, true);

		if (mLevelToLoad != null) {
			ResourceCacheFacade.load(mLevelToLoad, false);
			ResourceCacheFacade.load(mLevelToLoad.getLevelId(), Level.class, mLevelToLoad);
		}

		if (mGameSaveDef != null) {
			ResourceCacheFacade.load(mGameSaveDef.getGameSaveId(), GameSave.class, mGameSaveDef);
		}
	}

	@Override
	public void unloadResources() {
		ResourceCacheFacade.unload(ResourceNames.UI_GENERAL);
		ResourceCacheFacade.unload(ResourceNames.SHADER_DEFAULT);
		ResourceCacheFacade.unloadAllOf(PlayerActorDef.class, true);

		// Loaded level
		if (mLevelToLoad != null) {
			ResourceCacheFacade.unload(mLevel, mLevel.getDef());
			ResourceCacheFacade.unload(mLevelToLoad, false);
		}

		// Resumed game
		if (mGameSave != null) {
			ResourceCacheFacade.unload(mGameSave, mGameSaveDef);
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
		if (keycode == Keys.ESCAPE || keycode == Keys.BACK) {
			setOutcome(Outcomes.LEVEL_QUIT);
		}

		return false;
	}

	/**
	 * @return player score with leading zeros
	 */
	String getPlayerScore() {
		if (mPlayerStats != null) {
			return mPlayerStats.getScoreStringLeadingZero();
		} else {
			return "";
		}
	}

	/**
	 * @return player multiplier
	 */
	String getPlayerMultiplier() {
		if (mPlayerStats != null) {
			return mPlayerStats.getMultiplierString();
		} else {
			return "";
		}
	}

	/**
	 * Creates the mouse joint
	 */
	private void createMouseJoint() {
		BodyDef bodyDef = new BodyDef();
		mMouseBody = mWorld.createBody(bodyDef);
		mMouseJointDef.frequencyHz = Config.Game.MouseJoint.FREQUENCY;
		mMouseJointDef.bodyA = mMouseBody;
		mMouseJointDef.bodyB = mPlayerActor.getBody(); // TODO REMOVE, set in onActivate instead
		mMouseJointDef.collideConnected = true;
		mMouseJointDef.maxForce = Config.Game.MouseJoint.FORCE_MAX;
	}

	/**
	 * Create player ship
	 */
	private void createPlayerShip() {
		// Create a new ship when we're not resuming a game
		if (mGameSaveDef == null) {
			// Find first available player ship
			java.util.List<PlayerActorDef> ships = ResourceCacheFacade.get(PlayerActorDef.class);
			if (ships.isEmpty()) {
				setOutcome(Outcomes.LOADING_FAILED_MISSING_FILE, "Could not find any ships");
				return;
			}

			mPlayerActor = new PlayerActor(ships.get(0));
			mPlayerActor.createBody();
			resetPlayerPosition();

			mPlayerStats = new PlayerStats(mLevel.getDef().getStartXCoord(), mLevel.getSpeed());
		} else {
			mPlayerActor.createBody();
		}

		// Set lives
		mLevel.setPlayer(mPlayerActor);
		mGui.resetValues();

		// Create life ships
		for (int i = 0; i < mPlayerStats.getExtraLives(); ++i) {
			mPlayerLifeShips.add((PlayerActor) mPlayerActor.copy());
		}
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
			Vector2 playerPosition = Pools.vector2.obtain();
			playerPosition.set(mCamera.position.x - mCamera.viewportWidth * 0.5f, 0);

			// Get radius of player and offset it with the width
			ArrayList<Fixture> playerFixtures = mPlayerActor.getBody().getFixtureList();

			if (playerFixtures.size() > 0) {
				float radius = playerFixtures.get(0).getShape().getRadius();
				playerPosition.x += radius * 2;

				mPlayerActor.getBody().setTransform(playerPosition, 0.0f);
			}
			Pools.vector2.free(playerPosition);
		}
	}

	/**
	 * Updates the shepherd positions
	 */
	private void updateBodyShepherdPositions() {
		screenToWorldCoord(mCamera, 0, Gdx.graphics.getHeight(), mBodyShepherdMinPos, false);
		screenToWorldCoord(mCamera, Gdx.graphics.getWidth(), 0, mBodyShepherdMaxPos, false);
		mBodyShepherdMinPos.x -= getWorldWidth();
		mBodyShepherdMinPos.y -= getWorldHeight();
		mBodyShepherdMaxPos.x += getWorldWidth();
		mBodyShepherdMaxPos.y += getWorldHeight();
	}

	/** Invalid pointer id */
	private static final int INVALID_POINTER = -1;
	/** Level to load */
	private LevelDef mLevelToLoad = null;
	/** The current level used in the game */
	private Level mLevel = null;
	/** If we're just testing the level */
	private boolean mTesting = false;
	/** Makes the player invulnerable, useful for testing */
	private boolean mInvulnerable = false;
	/** Resumed game save definition, will only be set if resumed a game */
	private GameSaveDef mGameSaveDef = null;
	/** Resumed game save, will only be set if resumed a game */
	private GameSave mGameSave = null;
	/** Player ship actor, plays the game */
	private PlayerActor mPlayerActor;
	/** Current pointer that moves the player */
	private int mPlayerPointer = INVALID_POINTER;
	/** If we're currently moving the player */
	private boolean mMovingPlayer = false;
	/** Handles collision between actors/bodies */
	private CollisionResolver mCollisionResolver = new CollisionResolver();
	/** Body shepherd, creates and destroys bodies */
	private BodyShepherd mBodyShepherd = new BodyShepherd();
	/** Body shepherd min position */
	private Vector2 mBodyShepherdMinPos = new Vector2();
	/** Body shepherd max position */
	private Vector2 mBodyShepherdMaxPos = new Vector2();
	/** Player score */
	private PlayerStats mPlayerStats = null;
	/** Player life ships */
	private ArrayList<PlayerActor> mPlayerLifeShips = new ArrayList<PlayerActor>();

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

package com.spiddekauga.voider.game;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Filter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.joints.MouseJoint;
import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;
import com.badlogic.gdx.utils.Array;
import com.spiddekauga.utils.Maths;
import com.spiddekauga.utils.PngExport;
import com.spiddekauga.utils.Screens;
import com.spiddekauga.utils.ShapeRendererEx.ShapeType;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.Config.Debug.Builds;
import com.spiddekauga.voider.game.actors.Actor;
import com.spiddekauga.voider.game.actors.PlayerActor;
import com.spiddekauga.voider.game.actors.PlayerActorDef;
import com.spiddekauga.voider.menu.HighscoreScene;
import com.spiddekauga.voider.menu.ScoreScene;
import com.spiddekauga.voider.menu.TagScene;
import com.spiddekauga.voider.repo.resource.ExternalTypes;
import com.spiddekauga.voider.repo.resource.InternalNames;
import com.spiddekauga.voider.repo.resource.ResourceCacheFacade;
import com.spiddekauga.voider.repo.resource.ResourceLocalRepo;
import com.spiddekauga.voider.repo.resource.ResourceRepo;
import com.spiddekauga.voider.repo.stat.HighscoreRepo;
import com.spiddekauga.voider.repo.stat.StatLocalRepo;
import com.spiddekauga.voider.resources.SkinNames;
import com.spiddekauga.voider.scene.LoadingScene;
import com.spiddekauga.voider.scene.Scene;
import com.spiddekauga.voider.scene.WorldScene;
import com.spiddekauga.voider.utils.Pools;
import com.spiddekauga.voider.utils.User;

/**
 * The main game. Starts with a level and could either be in regular or testing mode.
 * Testing mode will set the player to unlimited lives.
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class GameScene extends WorldScene {
	/**
	 * Initializes the game scene.
	 * @param testing if we're testing the level. This allows the player to go back using
	 *        ESC or BACK key
	 * @param invulnerable set to true to make the player invulnerable while testing.
	 *        Scoring will still be used (player could be testing scoring.
	 */
	public GameScene(boolean testing, boolean invulnerable) {
		super(new GameSceneGui(), Config.Editor.PICKING_CIRCLE_RADIUS_EDITOR);
		((GameSceneGui) mGui).setGameScene(this);

		mTesting = testing;
		mInvulnerable = invulnerable;
		mSpriteBatch = new SpriteBatch();
		mSpriteBatch.setShader(SpriteBatch.createDefaultShader());
		mSpriteBatch.enableBlending();
		mSpriteBatch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
	}

	@Override
	protected void onInit() {
		super.onInit();

		mWorld.setContactListener(mCollisionResolver);
	}

	@Override
	protected void fixCamera() {
		if (isTestRun()) {
			float width = Gdx.graphics.getWidth() * Config.Graphics.WORLD_SCALE;

			// Decrease scale of width depending on height scaled
			float heightScale = ((float) Config.Graphics.HEIGHT_DEFAULT) / Gdx.graphics.getHeight();
			width *= heightScale;

			// Scale height depending on bar height
			float barHeight = getBarHeightInWorldCoordinates();
			float height = (Config.Graphics.HEIGHT_DEFAULT + barHeight) * Config.Graphics.WORLD_SCALE;


			if (mCamera != null) {
				mCamera.viewportHeight = height;
				mCamera.viewportWidth = width;
				mCamera.update();
			} else {
				mCamera = new OrthographicCamera(width, height);
			}
			mCamera.position.y += barHeight * Config.Graphics.WORLD_SCALE * 0.5f;
		}
		// Just use
		else {
			super.fixCamera();
		}
	}

	/**
	 * @return height of the option bar in world coordinates
	 */
	private float getBarHeightInWorldCoordinates() {
		if (mBarHeight == -1) {
			float heightScale = ((float) Config.Graphics.HEIGHT_DEFAULT) / Gdx.graphics.getHeight();
			float barHeight = SkinNames.getResource(SkinNames.GeneralVars.BAR_UPPER_LOWER_HEIGHT);
			mBarHeight = heightScale * barHeight;

		}
		return mBarHeight;
	}

	/**
	 * Takes a screenshot of the level
	 */
	void takeScreenshot() {
		mTakeScreenshot = true;
	}

	/**
	 * Takes the actual screenshot of the level
	 */
	private void takeScreenshotNow() {
		mTakeScreenshot = false;

		int barHeight = ((int) ((float) SkinNames.getResource(SkinNames.GeneralVars.BAR_UPPER_LOWER_HEIGHT)));

		// Screenshot size
		int ssHeight = Gdx.graphics.getHeight() - barHeight;
		int ssWidth = (int) (ssHeight * Config.Level.SAVE_TEXTURE_RATIO);

		Pixmap ssPixmap = Screens.getScreenshot(0, 0, ssWidth, ssHeight, true);


		// Scale image to the appropriate export size
		Pixmap scaledPixmap = new Pixmap(Config.Level.SAVE_TEXTURE_WIDTH, Config.Level.SAVE_TEXTURE_HEIGHT, ssPixmap.getFormat());
		Pixmap.setFilter(Filter.BiLinear);
		scaledPixmap.drawPixmap(ssPixmap, 0, 0, ssPixmap.getWidth(), ssPixmap.getHeight(), 0, 0, scaledPixmap.getWidth(), scaledPixmap.getHeight());


		// Convert to PNG
		try {
			byte[] pngBytes = PngExport.toPNG(scaledPixmap);
			mLevel.getDef().setPngImage(pngBytes);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	/**
	 * @return true if the level is test running
	 */
	boolean isTestRun() {
		return mTesting;
	}

	/**
	 * @return true if the level is published.
	 */
	boolean isPublished() {
		UUID levelDefId = getLevelId();

		if (levelDefId != null) {
			return ResourceLocalRepo.isPublished(levelDefId);
		} else {
			return false;
		}
	}

	/**
	 * @return level id
	 */
	private UUID getLevelId() {
		UUID levelDefId = null;

		if (mLevel != null) {
			levelDefId = mLevel.getDef().getId();
		} else if (mLevelToLoad != null) {
			levelDefId = mLevelToLoad.getId();
		} else if (mLevelToRun != null) {
			levelDefId = mLevelToRun.getDef().getId();
		}

		return levelDefId;
	}

	/**
	 * @return true if the player is invulnerable
	 */
	boolean isPlayerInvulnerable() {
		return mInvulnerable;
	}

	/**
	 * Sets the level that shall be played
	 * @param level level to play
	 */
	private void setLevel(Level level) {
		mLevel = level;
		mLevel.run();
		mLevel.createDefaultTriggers();

		mBodyShepherd.setActors(mLevel.getResources(Actor.class));

		updateCameraPosition();
		createBorder();
		Actor.setLevel(mLevel);
	}

	/**
	 * Sets the level to start running once the game scene is activated
	 * @param level the level to start running once the scene is activated
	 * @see #setLevelToLoad(LevelDef)
	 */
	public void setLevelToRun(Level level) {
		mLevelToRun = level;
	}

	/**
	 * Sets the level to load
	 * @param levelDef definition with all the information
	 * @see #setLevelToRun(Level)
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
	protected void onResize(int width, int height) {
		super.onResize(width, height);
		updateCameraPosition();
		createBorder();
	}

	@Override
	protected void onActivate(Outcomes outcome, Object message, Outcomes loadingOutcome) {
		super.onActivate(outcome, message, loadingOutcome);
		Actor.setEditorActive(false);

		if (loadingOutcome == Outcomes.LOADING_SUCCEEDED) {
			fixCamera();

			// Start a level
			if (mLevelToRun != null) {
				setLevel(mLevelToRun);
			}

			// Loaded level
			else if (mLevelToLoad != null) {
				Level level = ResourceCacheFacade.get(mLevelToLoad.getLevelId());
				level.setStartPosition(level.getLevelDef().getStartXCoord());
				setLevel(level);
			}

			// Resume a level
			else if (mGameSaveDef != null) {
				mGameSave = ResourceCacheFacade.get(mGameSaveDef.getGameSaveId());
				mGameSave.reinitialize();

				mPlayerActor = mGameSave.getPlayerActor();
				mBulletDestroyer = mGameSave.getBulletDestroyer();
				setGameTime(mGameSave.getGameTime());
				setLevel(mGameSave.getLevel());

				// Get player stats from level
				ArrayList<PlayerStats> playerStats = mLevel.getResources(PlayerStats.class);
				if (!playerStats.isEmpty()) {
					mPlayerStats = playerStats.get(0);
				} else {
					Gdx.app.error("GameSave", "Could not find player stats in level!");
				}
			}

			createPlayerShip();
			createMouseJoint();
			mGui.resetValues();

			// Set last played
			if (isPublished()) {
				StatLocalRepo.getInstance().updateLastPlayed(getLevelId());
			}
		}

		Actor.setPlayerActor(mPlayerActor);
	}

	@Override
	protected void onDispose() {
		// Save the level
		if (!mTesting) {
			// Remove old saved game
			ResourceLocalRepo.removeAll(ExternalTypes.GAME_SAVE, true);
			ResourceLocalRepo.removeAll(ExternalTypes.GAME_SAVE_DEF, true);

			// We quit -> Save the game state
			if (getOutcome() == Outcomes.LEVEL_QUIT) {
				GameSave gameSave = new GameSave(mLevel, mPlayerActor, mBulletDestroyer, getGameTime());
				GameSaveDef gameSaveDef = new GameSaveDef(gameSave);
				gameSave.setDef(gameSaveDef);
				mResourceRepo.save(gameSaveDef, gameSave);
			}
		}

		if (mLevel != null && (mTesting || mGameSave != null)) {
			mLevel.dispose();
		}


		super.onDispose();
	}

	@Override
	protected void update(float deltaTime) {
		// Make sure border maintains same speed as level
		if (mBorderBody != null) {
			mBorderBody.setLinearVelocity(mLevel.getSpeed(), 0.0f);
		}

		// Update mouse position even when still
		if (mMouseBody != null && mMovingPlayer) {
			screenToWorldCoord(mCamera, mCursorScreen, mCursorWorld, true);
			mMouseJoint.setTarget(mCursorWorld);
		}

		updateBodyShepherdPositions();
		mBodyShepherd.update(mBodyShepherdMinPos, mBodyShepherdMaxPos);

		mLevel.update(deltaTime);
		super.update(deltaTime);

		updateCameraPosition();

		// Is the player dead? Loose a life or game over
		if (mPlayerActor.getHealth() <= 0 && !mInvulnerable) {
			if (mPlayerStats.getExtraLives() > 0) {
				mPlayerActor.resetLife();
				mPlayerStats.decreaseExtraLives();
				updateLives();
			} else {
				setOutcome(Outcomes.LEVEL_PLAYER_DIED);
			}
		}

		// Have we reached the end of the level?
		if (mLevel.isCompletedLevel()) {
			setOutcome(Outcomes.LEVEL_COMPLETED);
		}

		mPlayerStats.updateScore(mLevel.getXCoord());

		// End of level, try to set player highscore
		if (isDone() && isPublished()) {
			setNewHighscore();
			updatePlayCount();
		}

		// GUI
		mGui.resetValues();
	}

	/**
	 * Updates the play count for the player
	 */
	private void updatePlayCount() {
		StatLocalRepo statLocalRepo = StatLocalRepo.getInstance();
		boolean cleared = Outcomes.LEVEL_COMPLETED == getOutcome();
		statLocalRepo.increasePlayCount(getLevelId(), cleared);
	}

	/**
	 * Tries to set a new highscore for the level
	 */
	private void setNewHighscore() {
		HighscoreRepo highscoreRepo = HighscoreRepo.getInstance();
		if (highscoreRepo.isNewHighscore(mLevel.getDef().getId(), mPlayerStats.getScore())) {
			highscoreRepo.setHighscoreAndSync(mLevel.getDef().getId(), mPlayerStats.getScore());
			mPlayerStats.setIsNewHighscore(true);
		}
	}

	@Override
	protected void render() {
		super.render();

		if (Config.Graphics.USE_RELEASE_RENDERER) {
			ShaderProgram defaultShader = ResourceCacheFacade.get(InternalNames.SHADER_DEFAULT);
			if (defaultShader != null) {
				mShapeRenderer.setShader(defaultShader);
			}
			mShapeRenderer.setProjectionMatrix(mCamera.combined);

			// Render sprites
			mSpriteBatch.begin();
			mLevel.renderBackground(mSpriteBatch);
			mSpriteBatch.end();

			// Render actors
			mShapeRenderer.push(ShapeType.Filled);
			Gdx.gl.glEnable(GL20.GL_BLEND);
			Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

			mLevel.render(mShapeRenderer);
			mBulletDestroyer.render(mShapeRenderer);

			if (mTakeScreenshot) {
				takeScreenshotNow();
			}

			mShapeRenderer.pop();
		}
	}

	/**
	 * Updates the lives
	 */
	private void updateLives() {
		((GameSceneGui) mGui).updateLives(mPlayerStats.getExtraLives(), PlayerStats.getStartLives());
	}

	@Override
	protected void synchronizeBorder(Body border) {
		float borderDiffPosition = border.getPosition().x - mLevel.getXCoord();
		if (!Maths.approxCompare(borderDiffPosition, Config.Game.BORDER_SYNC_THRESHOLD)) {
			border.setTransform(mLevel.getXCoord(), border.getPosition().y, 0);
		}
	}

	@Override
	protected Scene getNextScene() {
		if (mTesting) {
			return null;
		}

		if (super.getNextScene() == null) {
			ScoreScene scoreScene = new ScoreScene(mPlayerStats, mLevel.getLevelDef());
			Scene nextScene = scoreScene;

			// Display highscores and tags
			boolean online = User.getGlobalUser().isOnline();
			if (online && isPublished()) {
				HighscoreScene highscoreScene = new HighscoreScene();
				HighscoreRepo.getInstance().getPlayerServerScore(getLevelId(), highscoreScene);
				highscoreScene.setNextScene(nextScene);
				nextScene = highscoreScene;

				if (StatLocalRepo.getInstance().isTaggable(getLevelId())) {
					TagScene tagScene = new TagScene(getLevelId());
					tagScene.setNextScene(nextScene);
					nextScene = tagScene;
				}
			}


			switch (getOutcome()) {
			case LEVEL_COMPLETED:
				scoreScene.setLevelCompleted(true);
				LevelDef levelDef = null;
				if (mLevelToLoad != null || mGameSave != null) {
					levelDef = mLevel.getDef();
					if (levelDef != null && !levelDef.getEpilogue().equals("")) {
						Scene epilogeScene = new LoadingTextScene(levelDef.getEpilogue());
						epilogeScene.setNextScene(nextScene);
						nextScene = epilogeScene;
					}
				}
				break;

			case LEVEL_PLAYER_DIED:
				// Does nothing
				break;

			case LEVEL_QUIT:
			default:
				nextScene = null;
			}
			setNextScene(nextScene);
		}

		return super.getNextScene();
	}

	// --------------------------------
	// Resource loading etc.
	// --------------------------------
	@Override
	protected void loadResources() {
		super.loadResources();

		ResourceCacheFacade.load(InternalNames.UI_GENERAL);
		ResourceCacheFacade.load(InternalNames.UI_GAME);
		ResourceCacheFacade.load(InternalNames.SHADER_DEFAULT);
		ResourceCacheFacade.loadAllOf(this, ExternalTypes.PLAYER_DEF, true);
		if (mTesting) {
			ResourceCacheFacade.load(InternalNames.UI_EDITOR);
		}

		if (mLevelToRun != null) {
			ResourceCacheFacade.load(this, mLevelToRun.getDef().getId(), true);
		}

		else if (mLevelToLoad != null) {
			ResourceCacheFacade.load(this, mLevelToLoad.getLevelId(), mLevelToLoad.getId());
		}

		else if (mGameSaveDef != null) {
			ResourceCacheFacade.load(this, mGameSaveDef.getGameSaveId(), mGameSaveDef.getId());
		}
	}

	@Override
	protected void unloadResources() {
		super.unloadResources();
		ResourceCacheFacade.unload(InternalNames.UI_GENERAL);
		ResourceCacheFacade.unload(InternalNames.UI_GAME);
		ResourceCacheFacade.unload(InternalNames.SHADER_DEFAULT);

		if (mTesting) {
			ResourceCacheFacade.unload(InternalNames.UI_EDITOR);
		}
	}

	@Override
	protected LoadingScene getLoadingScene() {
		LoadingScene loadingScene = null;

		// Loading text scene as there is a prologue
		if (mLevelToLoad != null && !mLevelToLoad.getPrologue().equals("")) {
			loadingScene = new LoadingTextScene(mLevelToLoad.getPrologue());
		}
		// TODO regular loading screen
		else {

		}

		return loadingScene;
	}

	// --------------------------------
	// EVENTS
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
	public boolean onKeyDown(int keycode) {
		// Set level as complete if we want to go back while testing
		if (keycode == Keys.ESCAPE || keycode == Keys.BACK) {
			setOutcome(Outcomes.LEVEL_QUIT);
		}

		// Testing
		if (Config.Debug.isBuildOrBelow(Builds.NIGHTLY_DEV)) {
			if (keycode == Keys.F12) {
				setOutcome(Outcomes.LEVEL_COMPLETED);
				if (isPublished()) {
					setNewHighscore();
					updatePlayCount();
				}
			}
		}

		return false;
	}

	/**
	 * @return player score with leading zeros
	 */
	String getPlayerScore() {
		if (mPlayerStats != null) {
			return mPlayerStats.getScoreString();
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
	 * @return current percentage of health of the player
	 */
	float getPercentageHealth() {
		if (mPlayerActor != null) {
			return mPlayerActor.getHealth() / mPlayerActor.getDef().getHealthMax();
		} else {
			return 0;
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
		mMouseJointDef.bodyB = mPlayerActor.getBody();
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
			ArrayList<PlayerActorDef> ships = ResourceCacheFacade.getAll(ExternalTypes.PLAYER_DEF);
			if (ships.isEmpty()) {
				setOutcome(Outcomes.LOADING_FAILED_MISSING_FILE, "Could not find any ships");
				Pools.arrayList.free(ships);
				ships = null;
				return;
			}

			mPlayerActor = new PlayerActor(ships.get(0));
			mPlayerActor.createBody();
			resetPlayerPosition();

			mPlayerStats = new PlayerStats(mLevel.getLevelDef().getStartXCoord(), mLevel.getSpeed(), mPlayerActor);
			mLevel.addResource(mPlayerStats);

			Pools.arrayList.free(ships);
			ships = null;
		} else {
			mPlayerActor.createBody();
		}

		// Set lives
		mLevel.setPlayer(mPlayerActor);
		updateLives();
		mGui.resetValues();
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
			Array<Fixture> playerFixtures = mPlayerActor.getBody().getFixtureList();

			if (playerFixtures.size > 0) {
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

	/** Resource repository */
	private ResourceRepo mResourceRepo = ResourceRepo.getInstance();
	/** Bar height in world coordinates */
	private float mBarHeight = -1;
	/** Sprite renderer */
	private SpriteBatch mSpriteBatch = null;
	/** Invalid pointer id */
	private static final int INVALID_POINTER = -1;
	/** Level to load */
	private LevelDef mLevelToLoad = null;
	/** Level to run */
	private Level mLevelToRun = null;
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
	/** Take screenshot */
	private boolean mTakeScreenshot = false;

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

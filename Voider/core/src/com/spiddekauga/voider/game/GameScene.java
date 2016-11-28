package com.spiddekauga.voider.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Filter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.spiddekauga.utils.KeyHelper;
import com.spiddekauga.utils.PngExport;
import com.spiddekauga.utils.Screens;
import com.spiddekauga.utils.ShapeRendererEx.ShapeType;
import com.spiddekauga.utils.scene.ui.LoadingScene;
import com.spiddekauga.utils.scene.ui.LoadingTextScene;
import com.spiddekauga.utils.scene.ui.Scene;
import com.spiddekauga.utils.scene.ui.WorldScene;
import com.spiddekauga.voider.Config;
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
import com.spiddekauga.voider.repo.resource.SkinNames;
import com.spiddekauga.voider.repo.stat.HighscoreRepo;
import com.spiddekauga.voider.repo.stat.StatLocalRepo;
import com.spiddekauga.voider.repo.user.User;
import com.spiddekauga.voider.resources.InternalDeps;
import com.spiddekauga.voider.sound.Music;
import com.spiddekauga.voider.sound.MusicInterpolations;
import com.spiddekauga.voider.sound.SoundPlayer;
import com.spiddekauga.voider.utils.Geometry;
import com.spiddekauga.voider.utils.Synchronizer;
import com.spiddekauga.voider.utils.Synchronizer.SyncTypes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

/**
 * The main game. Starts with a level and could either be in regular or testing mode. Testing mode
 * will set the player to unlimited lives.
 */
public class GameScene extends WorldScene {
/** Invalid pointer id */
private static final int INVALID_POINTER = -1;
private static final ResourceRepo mResourceRepo = ResourceRepo.getInstance();
private static final StatLocalRepo mStatRepo = StatLocalRepo.getInstance();
private static final SoundPlayer mSoundPlayer = SoundPlayer.getInstance();
/** Bar height in world coordinates */
private float mBarHeight = -1;
private SpriteBatch mSpriteBatch = null;
/** Default projection matrix for SpriteBatch */
private Matrix4 mProjectionMatrixDefault = new Matrix4();
private LevelDef mLevelToLoad = null;
private Level mLevelToRun = null;
private Level mLevel = null;
private boolean mRunningFromEditor = false;
private boolean mInvulnerable = false;
/** Resumed game save definition, will only be set if resumed a game */
private GameSaveDef mGameSaveDef = null;
/** Resumed game save, will only be set if resumed a game */
private GameSave mGameSave = null;
private PlayerActor mPlayerActor;
private int mPlayerPointer = INVALID_POINTER;
private CollisionResolver mCollisionResolver = new CollisionResolver();
private BodyShepherd mBodyShepherd = new BodyShepherd();
private Vector2 mBodyShepherdMinPos = new Vector2();
private Vector2 mBodyShepherdMaxPos = new Vector2();
private PlayerStats mPlayerStats = null;
private boolean mTakeScreenshot = false;
private SoundEffectListener mSoundEffectListener = null;
/** Used for next scene if online and the level is published */
private HighscoreScene mHighscoreScene = null;
/** Mouse Joint / Moving Ship */
private Vector2 mCursorScreen = new Vector2();
/** Temporary variables */
private Vector2 mCursorWorld = new Vector2();

/**
 * Initializes the game scene.
 * @param runningFromEditor if we're testing the level. This allows the player to go back using ESC
 * or BACK key
 * @param invulnerable set to true to make the player invulnerable while testing. Scoring will still
 * be used (player could be testing scoring.
 */
public GameScene(boolean runningFromEditor, boolean invulnerable) {
	super(new GameSceneGui(), Config.Editor.PICKING_CIRCLE_RADIUS_EDITOR);
	getGui().setGameScene(this);

	mRunningFromEditor = runningFromEditor;
	mInvulnerable = invulnerable;
	mSpriteBatch = new SpriteBatch();
	mSpriteBatch.setShader(SpriteBatch.createDefaultShader());
	mSpriteBatch.enableBlending();
	mSpriteBatch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
	mProjectionMatrixDefault.set(mSpriteBatch.getProjectionMatrix());
}

@Override
protected void onCreate() {
	super.onCreate();

	mWorld.setContactListener(mCollisionResolver);
	mSoundEffectListener = new SoundEffectListener();
}

@Override
protected void onResume(Outcomes outcome, Object message, Outcomes loadingOutcome) {
	super.onResume(outcome, message, loadingOutcome);

	if (loadingOutcome == Outcomes.LOADING_SUCCEEDED) {
		// Set last played
		if (isPublished()) {
			mStatRepo.updateLastPlayed(getLevelId());
		}

		fixCamera();

		// Start a level
		if (mLevelToRun != null) {
			setLevel(mLevelToRun);
		}

		// Loaded level
		else if (mLevelToLoad != null) {
			Level level = ResourceCacheFacade.get(mLevelToLoad.getLevelId());
			level.setStartPosition(level.getLevelDef().getStartXCoord());
			level.createDefaultTriggers();
			setLevel(level);

			if (isPublished()) {
				mStatRepo.increasePlayCount(getLevelId());
				Synchronizer.getInstance().synchronize(SyncTypes.STATS);
			}
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

		// Play music
		mMusicPlayer.play(mLevel.getLevelDef().getMusic(), MusicInterpolations.CROSSFADE);

		createPlayerShip();
		getGui().resetValues();
	}

	Actor.setPlayerActor(mPlayerActor);
}

/**
 * @return true if the level is published.
 */
boolean isPublished() {
	UUID levelDefId = getLevelId();

	return levelDefId != null && ResourceLocalRepo.isPublished(levelDefId);
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
 * Sets the level that shall be played
 * @param level level to play
 */
private void setLevel(Level level) {
	mLevel = level;
	mLevel.run();

	mBodyShepherd.setActors(mLevel.getResources(Actor.class));

	updateCameraPosition();
	createBorder();
	Actor.setLevel(mLevel);
}

/**
 * Create player ship
 */
private void createPlayerShip() {
	// Create a new ship when we're not resuming a game
	if (mGameSaveDef == null) {
		// Find player ship
		PlayerActorDef shipDefault = ResourceLocalRepo.getPlayerShipDefault();
		if (shipDefault == null) {
			setOutcome(Outcomes.LOADING_FAILED_MISSING_FILE, "Could not find default ships");
			return;
		}

		mPlayerActor = new PlayerActor(shipDefault);
		mPlayerActor.createBody();
		resetPlayerPosition();

		LevelDef levelDef = mLevel.getLevelDef();
		mPlayerStats = new PlayerStats(levelDef.getStartXCoord(), levelDef.getEndXCoord(), levelDef.getLengthInTime());
		mLevel.addResource(mPlayerStats);
	} else {
		mPlayerActor.createBody();
	}

	mPlayerActor.getBody().setLinearVelocity(new Vector2(mLevel.getSpeed(), 0));

	// Set lives
	mLevel.setPlayer(mPlayerActor);
	updateLives();
	getGui().resetValues();
}

/**
 * @return true if the level was started from the level editor
 */
boolean isRunningFromEditor() {
	return mRunningFromEditor;
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
		Vector2 playerPosition = new Vector2();
		playerPosition.set(mCamera.position.x - mCamera.viewportWidth * 0.5f, 0);

		// Get radius of player and offset it with the width
		float boundingRadius = mPlayerActor.getDef().getShape().getBoundingRadius();
		playerPosition.x += boundingRadius * 2;
		mPlayerActor.getBody().setTransform(playerPosition, 0.0f);
	}
}

/**
 * Updates the lives
 */
private void updateLives() {
	getGui().updateLives(mPlayerStats.getExtraLives(), PlayerStats.getStartLives());
}

@Override
protected void onPause() {
	super.onPause();

	mSoundPlayer.stopAll();
}

@Override
protected void onResize(int width, int height) {
	super.onResize(width, height);
	updateCameraPosition();
}

@Override
protected void update(float deltaTime) {
	super.update(deltaTime);
	updateCameraPosition();
	synchronizeBorder();

	updateBodyShepherdPositions();
	mBodyShepherd.update(mBodyShepherdMinPos, mBodyShepherdMaxPos);

	mLevel.update(deltaTime);

	checkAndResetPlayerPosition();
	checkPlayerLives();
	checkCompletedLevel();
	mPlayerStats.updateScore(mLevel.getXCoord());

	// GUI
	getGui().resetValues();
}

@Override
protected void synchronizeBorder(Body border) {
	super.synchronizeBorder(border);
	border.setLinearVelocity(mLevel.getSpeed(), 0);
}

@Override
protected void render() {
	super.render();

	if (Config.Graphics.USE_RELEASE_RENDERER) {
		ShaderProgram defaultShader = ResourceCacheFacade.get(InternalNames.SHADER_DEFAULT);
		if (defaultShader != null) {
			mShapeRenderer.setShader(defaultShader);
		}

		// Render Background
		mSpriteBatch.setProjectionMatrix(mProjectionMatrixDefault);
		mSpriteBatch.begin();
		mLevel.renderBackground(mSpriteBatch);
		mSpriteBatch.end();

		// Render shape actors
		mShapeRenderer.setProjectionMatrix(mCamera.combined);
		mShapeRenderer.push(ShapeType.Filled);
		enableBlendingWithDefaults();

		mLevel.render(mShapeRenderer, getBoundingBoxWorld());
		mBulletDestroyer.render(mShapeRenderer, getBoundingBoxWorld());
		mShapeRenderer.pop();

		// Render sprite actors
		mSpriteBatch.setProjectionMatrix(mCamera.combined);
		mSpriteBatch.begin();
		mSpriteBatch.setBlendFunction(Config.Graphics.BLEND_SRC_FACTOR, Config.Graphics.BLEND_DST_FACTOR);
		mLevel.render(mSpriteBatch, getBoundingBoxWorld());
		mSpriteBatch.end();

		if (mTakeScreenshot) {
			setupCameraForScreenshot();
			takeScreenshotNow();
			fixCamera();
		}
	}
}

@Override
protected void onDestroy() {
	// Save the level
	if (!mRunningFromEditor) {
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

	if (mLevel != null && (mRunningFromEditor || mGameSave != null)) {
		mLevel.dispose();
	}

	Actor.setPlayerActor(null);
	mSoundEffectListener.dispose();
	mPlayerStats.dispose();

	super.onDestroy();
}

@Override
protected void fixCamera() {
	if (mCamera != null) {
		mCamera.zoom = 1;
	}

	if (isRunningFromEditor() && !isPublished() && !mTakeScreenshot) {
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
		mCamera.position.y = barHeight * Config.Graphics.WORLD_SCALE * 0.5f;
	}
	// Just use regular fix camera
	else {
		super.fixCamera();
	}
}

@Override
protected Vector2[][] getBorderBoxes() {
	Vector2[][] boxes = super.getBorderBoxes();

	// If running from the editor (and not published) move the top box
	if (isRunningFromEditor() && !isPublished()) {
		float barHeight = getBarHeightInWorldCoordinates() * Config.Graphics.WORLD_SCALE;
		for (Vector2 vertex : boxes[3]) {
			vertex.y -= barHeight;
		}
	}

	return boxes;
}

/**
 * Update camera for a screen shot
 */
private void setupCameraForScreenshot() {
	// Is the screen more or equal (widescreen) to the save texture ratio?
	float screenRatioCurrent = ((float) Gdx.graphics.getWidth()) / Gdx.graphics.getHeight();

	// Reset to default camera
	fixCamera();

	// Zoom out the camera so we can see the whole width
	if (screenRatioCurrent < Config.Level.SAVE_TEXTURE_RATIO) {
		mCamera.zoom = screenRatioCurrent / Config.Level.SAVE_TEXTURE_RATIO;
	}
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

	// Screenshot size
	int ssHeight;
	int ssWidth;
	int y = 0;

	float screenRatioCurrent = ((float) Gdx.graphics.getWidth()) / Gdx.graphics.getHeight();

	// Widescreen enough, use full height
	if (screenRatioCurrent >= Config.Level.SAVE_TEXTURE_RATIO) {
		ssHeight = Gdx.graphics.getHeight();
		ssWidth = (int) (ssHeight * Config.Level.SAVE_TEXTURE_RATIO);
	}
	// Use full width
	else {
		ssWidth = Gdx.graphics.getWidth();
		ssHeight = (int) (ssWidth / Config.Level.SAVE_TEXTURE_RATIO);
		y = (Gdx.graphics.getHeight() - ssHeight) / 2;
	}

	Pixmap ssPixmap = Screens.getScreenshot(0, y, ssWidth, ssHeight, true);


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
 * @return true if the player is invulnerable
 */
boolean isPlayerInvulnerable() {
	return mInvulnerable;
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
 * Sets the game to resume
 * @param gameSaveDef definition of the game save to resume
 */
public void setGameToResume(GameSaveDef gameSaveDef) {
	mGameSaveDef = gameSaveDef;
}

/**
 * Check and reset player position
 */
private void checkAndResetPlayerPosition() {
	if (!Geometry.isPointWithinBox(mPlayerActor.getPosition(), getWorldMinCoordinates(), getWorldMaxCoordinates())) {
		resetPlayerPosition();
		mPlayerActor.kill();
	}

	// Update cursor world position and move the ship
	if (mPlayerActor.isMoving()) {
		moveShipTo((int) mCursorScreen.x, (int) mCursorScreen.y);
	}
	// Always force player velocity if we're not picking the player
	else {
		setPlayerVelocityToLevel();
	}
}

/**
 * Check if the player has completed the level
 */
private void checkCompletedLevel() {
	if (mLevel.isCompletedLevel()) {
		mPlayerStats.calculateEndScore();
		setOutcome(Outcomes.LEVEL_COMPLETED);
		mSoundPlayer.stopAll();

		if (!mRunningFromEditor) {
			mMusicPlayer.play(Music.LEVEL_COMPLETED_INTRO);
			mMusicPlayer.queue(Music.LEVEL_COMPLETED_LOOP);
		}
	}
}

/**
 * Check if the player is dead
 */
private void checkPlayerLives() {
	if (mPlayerActor.getHealth() <= 0 && !mInvulnerable) {
		if (mPlayerStats.getExtraLives() > 0) {
			mPlayerActor.resetHealth();
			mPlayerStats.decreaseExtraLives();
			updateLives();
		} else {
			mPlayerStats.calculateEndScore();
			setOutcome(Outcomes.LEVEL_PLAYER_DIED);
			mSoundPlayer.stopAll();

			if (!mRunningFromEditor) {
				mMusicPlayer.play(Music.GAME_OVER_INTRO);
				mMusicPlayer.queue(Music.GAME_OVER_LOOP);
			}
		}
	}
}

/**
 * Set player velocity to the level speed
 */
private void setPlayerVelocityToLevel() {
	mPlayerActor.getBody().setLinearVelocity(new Vector2(mLevel.getSpeed(), 0));
}

@Override
public boolean touchDown(int x, int y, int pointer, int button) {
// Set pointer if we're currently not moving the player
	if (mPlayerPointer == INVALID_POINTER) {
		startMovingShip(x, y, pointer);
		return true;
	}

	return false;
}

@Override
public boolean touchUp(int x, int y, int pointer, int button) {
	if (mPlayerPointer == pointer) {
		moveShipTo(x, y);
		stopMovingShip();
		return true;
	}

	return false;
}

@Override
public boolean touchDragged(int x, int y, int pointer) {
	if (mPlayerPointer == pointer) {
		moveShipTo(x, y);
		return true;
	}

	return false;
}

/**
 * Move the player ship to the specific location. Note that this will be relative to the mouse
 * offset
 */
private void moveShipTo(int x, int y) {
	mCursorScreen.set(x, y);
	screenToWorldCoord(mCamera, mCursorScreen, mCursorWorld, true);
	mPlayerActor.move(mCursorWorld);
}

/**
 * Stop moving the ship
 */
private void stopMovingShip() {
	mPlayerPointer = INVALID_POINTER;
	mPlayerActor.stopMoving();
}

/**
 * Set the ship offset to the mouse
 * @param pointer which pointer the ship is moving relative to
 */
private void startMovingShip(int x, int y, int pointer) {
	mPlayerPointer = pointer;

	mCursorScreen.set(x, y);
	screenToWorldCoord(mCamera, mCursorScreen, mCursorWorld, true);
	mPlayerActor.startMoving(mCursorWorld);
}

@Override
public boolean onKeyDown(int keycode) {
// Set level as complete if we want to go back while testing
	if (KeyHelper.isBackPressed(keycode)) {
		if (mRunningFromEditor) {
			setOutcome(Outcomes.LEVEL_QUIT);
		} else {
			getGui().showMenu();
		}
	}

	return super.onKeyDown(keycode);
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

	if (User.getGlobalUser().isOnline()) {
		mHighscoreScene = new HighscoreScene(getLevelId(), mPlayerStats.getScore(), mPlayerStats.isNewHighscore());
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

@Override
protected void loadResources() {
	super.loadResources();

	ResourceCacheFacade.load(this, InternalDeps.UI_GENERAL);
	ResourceCacheFacade.load(this, InternalDeps.UI_GAME);
	ResourceCacheFacade.load(this, InternalNames.SHADER_DEFAULT);
	ResourceCacheFacade.load(this, InternalDeps.GAME_MUSIC);
	ResourceCacheFacade.load(this, InternalDeps.GAME_SFX);
	ResourceCacheFacade.loadAllOf(this, ExternalTypes.PLAYER_DEF, true);
	if (mRunningFromEditor) {
		ResourceCacheFacade.load(this, InternalDeps.UI_EDITOR);
	}

	if (mLevelToRun != null) {
		ResourceCacheFacade.load(this, mLevelToRun.getDef().getId(), true);
	} else if (mLevelToLoad != null) {
		// Actually load this level too if it hasn't been loaded by this scene
		if (!ResourceCacheFacade.isLoaded(mLevelToLoad.getId(), this)) {
			ResourceCacheFacade.load(this, mLevelToLoad.getId(), false);
		}

		ResourceCacheFacade.load(this, mLevelToLoad.getLevelId(), mLevelToLoad.getId());

		if (ResourceCacheFacade.isLoaded(mLevelToLoad.getLevelId())) {
			ResourceCacheFacade.reload(mLevelToLoad.getLevelId());
		}
	} else if (mGameSaveDef != null) {
		ResourceCacheFacade.load(this, mGameSaveDef.getGameSaveId(), mGameSaveDef.getId());
	}
}

@Override
protected synchronized void setOutcome(Outcomes outcome) {
	super.setOutcome(outcome);

	if (!mRunningFromEditor && isPublished() && (outcome == Outcomes.LEVEL_COMPLETED || outcome == Outcomes.LEVEL_PLAYER_DIED)) {
		setNewHighscore();

		if (getOutcome() == Outcomes.LEVEL_COMPLETED) {
			mStatRepo.increaseClearCount(getLevelId());
		} else if (getOutcome() == Outcomes.LEVEL_PLAYER_DIED || getOutcome() == Outcomes.LEVEL_RESTART) {
			mStatRepo.increaseDeathCount(getLevelId());
		}
		Synchronizer.getInstance().synchronize(SyncTypes.STATS);
	}
}

@Override
protected Scene getNextScene() {
	if (mRunningFromEditor) {
		return null;
	}

	if (super.getNextScene() == null) {
		ScoreScene scoreScene = null;
		Scene nextScene = null;

		// Completed or died
		if (getOutcome() == Outcomes.LEVEL_COMPLETED || getOutcome() == Outcomes.LEVEL_PLAYER_DIED) {
			scoreScene = new ScoreScene(mPlayerStats, mLevel.getLevelDef());
			nextScene = scoreScene;

			// Display highscores and tags
			boolean online = User.getGlobalUser().isOnline();
			if (online && isPublished()) {
				HighscoreScene highscoreScene = mHighscoreScene;
				highscoreScene.setNextScene(nextScene);
				nextScene = highscoreScene;

				if (StatLocalRepo.getInstance().isTaggable(getLevelId())) {
					TagScene tagScene = new TagScene(getLevelId());
					tagScene.setNextScene(nextScene);
					nextScene = tagScene;
				}
			}
		}


		switch (getOutcome()) {
		case LEVEL_COMPLETED:
			if (scoreScene != null) {
				scoreScene.setLevelCompleted(true);
			}
			LevelDef levelDef;
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

		case LEVEL_RESTART: {
			GameScene gameScene = new GameScene(false, false);
			gameScene.setLevelToLoad(mLevel.getLevelDef());
			nextScene = gameScene;
			break;
		}

		case LEVEL_QUIT:
		default:
			nextScene = null;
		}
		setNextScene(nextScene);
	}

	return super.getNextScene();
}

/**
 * Sets the level to load
 * @param levelDef definition with all the information
 * @see #setLevelToRun(Level)
 */
public void setLevelToLoad(LevelDef levelDef) {
	mLevelToLoad = levelDef;
}

@Override
protected GameSceneGui getGui() {
	return (GameSceneGui) super.getGui();
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
}

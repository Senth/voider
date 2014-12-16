package com.spiddekauga.voider.scene;

import java.util.ArrayList;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.spiddekauga.utils.GameTime;
import com.spiddekauga.utils.IExceptionHandler;
import com.spiddekauga.utils.InputMultiplexerExceptionSnatcher;
import com.spiddekauga.utils.KeyHelper;
import com.spiddekauga.utils.ShapeRendererEx;
import com.spiddekauga.utils.commands.Invoker;
import com.spiddekauga.utils.scene.ui.NotificationShower;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.Config.Debug.Builds;
import com.spiddekauga.voider.config.ConfigIni;
import com.spiddekauga.voider.game.BulletDestroyer;
import com.spiddekauga.voider.repo.misc.SettingRepo;
import com.spiddekauga.voider.sound.MusicPlayer;

/**
 * Base class for all scenes that should be rendered. Examples of scenes: Game, Menus,
 * Editors. It extends InputAdapter which makes it able to handle all input events through
 * derived classes (if necessary).
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public abstract class Scene extends InputAdapter implements IExceptionHandler {
	/**
	 * Creates the input multiplexer. UI always has priority over everything else.
	 * @param gui the GUI to use for the scene
	 */
	protected Scene(Gui gui) {
		if (gui != null) {
			mGui = gui;
		}
	}

	@Override
	public final boolean keyDown(int keycode) {
		if (!mGui.isMsgBoxActive()) {
			return onKeyDown(keycode);
		} else {
			return false;
		}
	}

	@Override
	public final boolean keyUp(int keycode) {
		if (!mGui.isMsgBoxActive()) {
			return onKeyUp(keycode);
		} else {
			return false;
		}
	}

	@Override
	public final boolean keyTyped(char character) {
		if (!mGui.isMsgBoxActive()) {
			return onKeyTyped(character);
		} else {
			return false;
		}
	}

	/**
	 * Called when a key was pressed if no message box is active.
	 * @param keycode the key that was pressed
	 * @return true if handled, otherwise false
	 */
	protected boolean onKeyDown(int keycode) {
		// Toggle fullscreen on desktop
		if (Gdx.app.getType() == ApplicationType.Desktop && KeyHelper.isAltPressed() && keycode == Input.Keys.ENTER) {
			SettingRepo.getInstance().display().toggleFullscreen();
		}

		// Testing
		if (Config.Debug.isBuildOrBelow(Builds.NIGHTLY_DEV)) {
			// UI - reload
			if (KeyHelper.isShiftPressed() && keycode == Input.Keys.F3) {
				mGui.dispose();
				mGui.initGui();
			}
			// INI - reload
			if (KeyHelper.isShiftPressed() && keycode == Input.Keys.F2) {
				ConfigIni.getInstance().reload();
			}
		}

		return false;
	}

	/**
	 * Ends the scene. If a next scene isn't set this will be the same as going back. Same
	 * as calling {@link #setOutcome(Outcomes)} with NOT_APPLICABLE.
	 */
	public void endScene() {
		setOutcome(Outcomes.NOT_APPLICAPLE);
	}

	/**
	 * Called when a key was released if no message box is active.
	 * @param keycode the key that was released
	 * @return true if handled, otherwise false
	 */
	protected boolean onKeyUp(int keycode) {
		return false;
	}

	/**
	 * Called when a character was type if no message box is active
	 * @param character the character that was type
	 * @return true if handled, false if not
	 */
	protected boolean onKeyTyped(char character) {
		return false;
	}

	/**
	 * Runs the scene. Clears the screen, renders it, and updates the scene elements.
	 */
	final void run() {
		Gdx.gl.glClearColor(mClearColor.r, mClearColor.g, mClearColor.b, mClearColor.a);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		if (!mGui.isMsgBoxActive()) {
			mGameTime.update(Gdx.graphics.getDeltaTime());
			update(mGameTime.getDeltaTime());
		}

		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
		render();

		if (mGui.isVisible()) {
			mGui.update();
			mGui.render();
		}
		Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
	}

	/**
	 * Report an exception
	 * @param exception the exception that was thrown
	 */
	@Override
	public synchronized void handleException(Exception exception) {
		if (mGui.isInitialized()) {
			mGui.showBugReportWindow(exception);
		}
	}

	/**
	 * Called when the window has been resized
	 * @param width new width of the window
	 * @param height new height of the window
	 */
	protected void onResize(int width, int height) {
		mGui.resize(width, height);
	}


	/**
	 * Return screen width in world coordinates, but only if this scene is a world scene.
	 * @return screen width in world coordinates, if scene is not a world it return 0.
	 */
	protected float getWorldWidth() {
		return 0;
	}

	/**
	 * Return screen height in world coordinates, but only if this scene is a world scene.
	 * @return screen height in world coordinates, if scene is not a world it return 0.
	 */
	protected float getWorldHeight() {
		return 0;
	}

	/**
	 * @return 0,0 of screen in world coordinates, null if current scene isn't a world
	 *         scene. Remember to free the returned vector with
	 *         Pools.vector2.free(returnedVector);
	 */
	protected Vector2 getWorldMinCoordinates() {
		return null;
	}

	/**
	 * @return screenWidth,screenHeight in world coordinates, null if current scene isn't
	 *         a world scene. Remember to free the returned vector with
	 *         Pools.vector2.free(returnedVector);
	 */
	protected Vector2 getWorldMaxCoordinates() {
		return null;
	}

	/**
	 * @return scene's invoker if the scene uses one, null otherwise.
	 */
	public Invoker getInvoker() {
		return null;
	}

	/**
	 * @return picking fixture from editors, null otherwise.
	 */
	protected FixtureDef getPickingFixtureDef() {
		return null;
	}

	/**
	 * @return picking vertices from editors, null otherwise.
	 */
	protected ArrayList<Vector2> getPickingVertices() {
		return null;
	}

	/**
	 * Updates the scene
	 * @param deltaTime elapsed time since last frame
	 */
	protected void update(float deltaTime) {
		// Does nothing
	}

	/**
	 * Override this function if you want your scene to unload all of its resources when
	 * deactivated (not disposed, when disposed it will always unload its resources).
	 * NOTE: Due to making it more efficient, SceneSwitcher will load the resources of the
	 * next scene before unloading this scene's resources. So that the same resources
	 * aren't unloaded in this scene and loaded again in the very next. Due to obvious
	 * reasons it will not, however, unload the resources when loadResources supplied a
	 * loading scene.
	 * @return true if this scene shall unload all of its resources when deactivated.
	 *         Defaults to false.
	 */
	protected boolean unloadResourcesOnDeactivate() {
		return false;
	}

	/**
	 * @return a loading scene for this scene. This will scene will be displayed while
	 *         loading the resources for this scene. Defaults to null, which means no
	 *         loading scene will be displayed.
	 */
	protected LoadingScene getLoadingScene() {
		return null;
	}

	/**
	 * Loads the resources of the scene. Called before #onActivate(Outcome,String)
	 * @see #getLoadingScene() if this scene should have some sort of loading scene
	 */
	protected void loadResources() {
		mResourceLoaded = true;
	}

	/**
	 * Unloads the resources of the scene. If unloadResourcesOnDeactivate() returns true
	 * this function will get called whenever this scene is deactivated, except when it
	 * supplied a loading scene. Called after #onDispose() and #onDeactive().
	 */
	protected void unloadResources() {
		mResourceLoaded = false;
	}

	/**
	 * Resources to be reloaded when the scene is activated once again. This method only
	 * gets called if {@link #unloadResourcesOnDeactivate()} returns false.
	 * @param outcome the outcome of the previous scene
	 * @param message the outcome message provided with the outcome, null if none was
	 *        provided
	 */
	protected void reloadResourcesOnActivate(Outcomes outcome, Object message) {
		// Dose nothing
	}

	/**
	 * @return true if #loadResources() has been called, false if #unloadResources() has
	 *         been called.
	 */
	protected boolean isResourcesLoaded() {
		return mResourceLoaded;
	}

	/**
	 * Called first time the scene activates, before
	 * {@link #onActivate(Outcomes, Object, Outcomes)} .
	 */
	protected void onInit() {
		mInputMultiplexer = new InputMultiplexerExceptionSnatcher(this);

		if (!mGui.isInitialized()) {
			mGui.initGui();
			mGui.resetValues();
			mInputMultiplexer.addProcessor(mGui.getStage());

			if (mGui.mNotification != null) {
				mNotification = mGui.mNotification;
			}
		}

		mInputMultiplexer.addProcessor(this);

		mShapeRenderer = new ShapeRendererEx();

		if (getInvoker() != null) {
			getInvoker().dispose();
		}


		mInitialized = true;
	}

	/**
	 * @return true if the scene has been initialized
	 */
	public boolean isInitialized() {
		return mInitialized;
	}

	/**
	 * Called just before the scene activates.
	 * @param outcome the outcome of the previous scene that was on the stack if there was
	 *        any, else null.
	 * @param message the outcome message provided with the outcome, null if none was
	 *        provided.
	 * @param loadingOutcome outcome from a loading scene
	 */
	protected void onActivate(Outcomes outcome, Object message, Outcomes loadingOutcome) {
		if (!mGui.isInitialized()) {
			Gdx.app.error("Scene", "Failed to load scene!");
		} else {
			mGui.resetValues();
		}
	}

	/**
	 * Called when the scene deactivates (another one is activated, push onto the scene
	 * stack).
	 * @note #onDisposed() instead when this scene is deleted (popped from the scene
	 *       stack).
	 */
	protected void onDeactivate() {
	}

	/**
	 * Called when the scene is deleted. Called before #unloadResources() if this scene
	 * has resources.
	 */
	protected void onDispose() {
		mGui.dispose();
	}

	/**
	 * Renders the scene
	 */
	protected void render() {
		// Does nothing
	}

	/**
	 * @return true if the scene is done with it work, i.e. it should be popped from the
	 *         stack.
	 */
	protected final boolean isDone() {
		return mOutcome != null;
	}

	/**
	 * @return outcome of the scene, returns null if no outcome has been set (when
	 *         #isDone() returns true.
	 */
	protected synchronized final Outcomes getOutcome() {
		return mOutcome;
	}

	/**
	 * @return outcome message of the scene, if none exist it will return null.
	 */
	protected synchronized final Object getOutcomeMessage() {
		return mOutcomeMessage;
	}

	/**
	 * Sets the outcome of the scene, this also "completes" the scene making #isDone()
	 * return true. Outcomes can only be set once, this to prevent another later outcome
	 * to overwrite the original outcome.
	 * @param outcome the outcome of the scene
	 */
	protected synchronized final void setOutcome(Outcomes outcome) {
		if (mOutcome == null) {
			mOutcome = outcome;
		}
	}

	/**
	 * @return the input multiplexer, this determines who shall get the input
	 */
	protected final InputMultiplexer getInputMultiplexer() {
		return mInputMultiplexer;
	}

	/**
	 * Sets the outcome of the scene, including a description of the outcome. E.g.
	 * LOADING_FAILED_MISSING_FILE can specify the missing file in this message. Outcomes
	 * can only be set once, this to prevent another later outcome to overwrite the
	 * original outcome.
	 * @param outcome the outcome of the scene
	 * @param message a descriptive outcome message.
	 */
	protected synchronized final void setOutcome(Outcomes outcome, Object message) {
		if (mOutcome == null) {
			setOutcome(outcome);
			mOutcomeMessage = message;
		}
	}

	/**
	 * Different outcomes a scene can have
	 */
	protected enum Outcomes {
		/** Selected a definition */
		@Deprecated DEF_SELECTED,
		/** Canceled definition selection */
		@Deprecated DEF_SELECT_CANCEL,
		/** A resource to be loaded was selected */
		EXPLORE_LOAD,
		/** A resource was selected */
		EXPLORE_SELECT,
		/** Level was successfully completed */
		LEVEL_COMPLETED,
		/** Player died during the level */
		LEVEL_PLAYER_DIED,
		/** Player decided to quit the level */
		LEVEL_QUIT,
		/** Loading succeeded */
		LOADING_SUCCEEDED,
		/** Loading failed, missing file */
		LOADING_FAILED_MISSING_FILE,
		/** Loading failed, corrupt file */
		LOADING_FAILED_CORRUPT_FILE,
		/** Logged in */
		LOGGED_IN,
		/** Logged out */
		LOGGED_OUT,
		/** Theme selected */
		THEME_SELECTED,
		/** Theme selection canceled */
		THEME_SELECT_CANCEL,

		/** No outcome when an outcome isn't applicable, e.g. first time */
		NOT_APPLICAPLE,
	}

	/**
	 * Sets the game time. Useful when saving the game time and then restoring it
	 * @param gameTime the game to to use
	 */
	protected void setGameTime(GameTime gameTime) {
		mGameTime = gameTime;
	}

	/**
	 * @return Game time of the current scene
	 */
	protected GameTime getGameTime() {
		return mGameTime;
	}

	/**
	 * @return true if the scene is loading
	 */
	protected boolean isLoading() {
		return mLoading;
	}

	/**
	 * Enable blending and use the default blend function
	 */
	protected void enableBlendingWithDefaults() {
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(Config.Graphics.BLEND_SRC_FACTOR, Config.Graphics.BLEND_DST_FACTOR);
	}

	/**
	 * Set if the current scene is loading or not
	 * @param loading set to true if the scene is loading.
	 */
	void setLoading(boolean loading) {
		mLoading = loading;
	}

	/**
	 * @return bullet destroyer for the scene. Always returns null in Scene.
	 */
	BulletDestroyer getBulletDestroyer() {
		return null;
	}

	/**
	 * Clamps the X-coordinate of a pointer to the screen, i.e. if it's out of screen it
	 * will be clamped to the screen
	 * @param x the x value to clamp
	 * @return X in the range of [0, Gdx.graphics.getWidth()]
	 */
	protected static float clampX(float x) {
		if (x < 0.0f) {
			return 0.0f;
		} else if (x > Gdx.graphics.getWidth()) {
			return Gdx.graphics.getWidth();
		} else {
			return x;
		}
	}

	/**
	 * Clamps the Y-coordinate of a pointer to the screen, i.e. if it's out of screen it
	 * will be clamped to the screen
	 * @param y the y value to clamp
	 * @return X in the range of [0, Gdx.graphics.getHeight()]
	 */
	protected static float clampY(float y) {
		if (y < 0.0f) {
			return 0.0f;
		} else if (y > Gdx.graphics.getHeight()) {
			return Gdx.graphics.getHeight();
		} else {
			return y;
		}
	}

	/**
	 * Sets the clear color of the scene
	 * @param r red
	 * @param b blue
	 * @param g green
	 * @param a alpha
	 */
	protected final void setClearColor(float r, float g, float b, float a) {
		mClearColor.r = r;
		mClearColor.g = g;
		mClearColor.b = b;
		mClearColor.a = a;
	}

	/**
	 * Sets the clear color of the scene
	 * @param color new color to clear the screen with
	 */
	protected final void setClearColor(Color color) {
		mClearColor.set(color);
	}

	/**
	 * By default this method return null. If, however, it does not this next scene will
	 * be run instead of returning to the previous scene.
	 * @return next scene to be run, null by default
	 * @see #setNextScene(Scene)
	 */
	protected Scene getNextScene() {
		return mNextScene;
	}

	/**
	 * Sets the next scene to be run after this scene has been completed
	 * @param nextScene if not null nextScene will be run after this scene has completed.
	 *        If null SceneSwitcher will return to the previous scene
	 */
	public final void setNextScene(Scene nextScene) {
		mNextScene = nextScene;
	}

	/**
	 * Screen to world coordinate
	 * @param camera for the world coordinate
	 * @param screenPos screen position
	 * @param worldCoordinate the vector to set the world coordinate for
	 * @param clamp if the x and y coordinates should be clamped
	 */
	public static void screenToWorldCoord(Camera camera, Vector2 screenPos, Vector2 worldCoordinate, boolean clamp) {
		screenToWorldCoord(camera, screenPos.x, screenPos.y, worldCoordinate, clamp);
	}

	/**
	 * Screen to world coordinate
	 * @param camera for the world coordinate
	 * @param screenPositions all screen position
	 * @param worldCoordinates all the vectors to set the world coordinate for
	 * @param clamp if the x and y coordinates should be clamped
	 */
	public static void screenToWorldCoord(Camera camera, Vector2[] screenPositions, Vector2[] worldCoordinates, boolean clamp) {
		if (screenPositions.length != worldCoordinates.length) {
			throw new IllegalArgumentException("screenpositions.length != worldCoordinates.length");
		}
		for (int i = 0; i < screenPositions.length; ++i) {
			screenToWorldCoord(camera, screenPositions[i], worldCoordinates[i], clamp);
		}
	}

	/**
	 * Screen to world coordinate
	 * @param camera camera for the world coordinate
	 * @param x the X-coordinate of the screen
	 * @param y the Y-coordinate of the screen
	 * @param worldCoordinate the vector to set the world coordinate for
	 * @param clamp if the x and y coordinates should be clamped
	 */
	public static void screenToWorldCoord(Camera camera, float x, float y, Vector2 worldCoordinate, boolean clamp) {
		if (clamp) {
			mTestPoint.set(clampX(x), clampY(y), 0);
		} else {
			mTestPoint.set(x, y, 0);
		}
		camera.unproject(mTestPoint);
		worldCoordinate.x = mTestPoint.x;
		worldCoordinate.y = mTestPoint.y;
	}

	/**
	 * @param <GuiType> the GUI type to return
	 * @return GUI in the correct type
	 */
	@SuppressWarnings("unchecked")
	protected <GuiType extends Gui> GuiType getGui() {
		return (GuiType) mGui;
	}

	/** Shape Renderer used for rendering stuff */
	protected ShapeRendererEx mShapeRenderer = null;
	/** Input multiplexer */
	protected InputMultiplexerExceptionSnatcher mInputMultiplexer = null;
	/** GUI for the scene */
	protected Gui mGui = null;
	/** Notification messages */
	protected NotificationShower mNotification = null;
	/** The music player */
	protected MusicPlayer mMusicPlayer = MusicPlayer.getInstance();

	/** If the current scene is loading */
	private boolean mLoading = false;
	/** Game time of current scene */
	private GameTime mGameTime = new GameTime();
	/** Outcome of scene, this is set when a derived class calls setOutcome */
	private Outcomes mOutcome = null;
	/** Message of the outcome */
	private Object mOutcomeMessage = null;
	/** Clear color */
	private Color mClearColor = new Color(0, 0, 0, 1);
	/** If resource has been loaded */
	private boolean mResourceLoaded = false;
	/** The next scene to be run */
	private Scene mNextScene = null;
	/** If the scene has been initialized */
	private boolean mInitialized = false;

	// Temporary variables
	/** For ray testing on player ship when touching it */
	private static Vector3 mTestPoint = new Vector3();
}
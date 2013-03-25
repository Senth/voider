package com.spiddekauga.voider.scene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRendererEx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.spiddekauga.utils.GameTime;
import com.spiddekauga.utils.Invoker;
import com.spiddekauga.voider.game.BulletDestroyer;

/**
 * Base class for all scenes that should be rendered. Examples of scenes:
 * Game, Menus, Editors. It extends InputAdapter which makes it able to
 * handle all input events through derived classes (if necessary).
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public abstract class Scene extends InputAdapter {
	/**
	 * Creates the input multiplexer. UI always has priority over everything else.
	 * @param gui the GUI to use for the scene
	 */
	public Scene(Gui gui) {
		mGui = gui;
		mInputMultiplexer.addProcessor(0, mGui.getStage());
		mInputMultiplexer.addProcessor(1, this);
	}

	/**
	 * Runs the scene. Clears the screen, renders it, and updates the scene elements.
	 */
	public final void run() {
		Gdx.gl.glClearColor(0, 0, 0, 0);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		//		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);


		render();
		mGui.update();
		if (!mGui.isMsgBoxActive()) {
			mGameTime.update(Gdx.graphics.getDeltaTime());
			update();
		}
	}

	/**
	 * Called when the window has been resized
	 * @param width new width of the window
	 * @param height new height of the window
	 */
	public void onResize(int width, int height) {
		mGui.resize(width, height);
	}


	/**
	 * Return screen width in world coordinates, but only if this scene
	 * is a world scene.
	 * @return screen width in world coordinates, if scene is not a world it return 0.
	 */
	public float getWorldWidth() {
		return 0;
	}

	/**
	 * Return screen height in world coordinates, but only if this scene is
	 * a world scene.
	 * @return screen height in world coordinates, if scene is not a world it return 0.
	 */
	public float getWorldHeight() {
		return 0;
	}

	/**
	 * @return scene's invoker if the scene uses one, null otherwise.
	 */
	public Invoker getInvoker() {
		return null;
	}

	/**
	 * Updates the scene
	 */
	public abstract void update();

	/**
	 * Checks whether the derived class has any resources that needs to be loaded.
	 * If this method returns true. SceneSwitcher will call loadResources() before
	 * activating this resource and unloadResources() after this class has been
	 * deactivated.
	 * @return true if this scene has any resources that needs to be loaded.
	 */
	public abstract boolean hasResources();

	/**
	 * Override this function if you want your scene to unload all of its resources
	 * when deactivated (not disposed, when disposed it will always unload its
	 * resources). NOTE: Due to making it more efficient, SceneSwitcher will load the
	 * resources of the next scene before unloading this scene's resources. So that the
	 * same resources aren't unloaded in this scene and loaded again in the very next.
	 * Due to obvious reasons it will not, however, unload the resources when loadResources
	 * supplied a loading scene.
	 * @return true if this scene shall unload all of its resources when deactivated.
	 * Defaults to false.
	 */
	public boolean unloadResourcesOnDeactivate() {
		return false;
	}

	/**
	 * @return a loading scene for this scene. This will scene will be displayed while
	 * loading the resources for this scene. Defaults to null, which means no loading
	 * scene will be displayed.
	 */
	public LoadingScene getLoadingScene() {
		return null;
	}

	/**
	 * Loads the resources of the scene. Called before #onActivate(Outcome,String)
	 * @see #getLoadingScene() if this scene should have some sort of loading scene
	 */
	public void loadResources() {
		// Does nothing
	}

	/**
	 * Unloads the resources of the scene. If unloadResourcesOnDeactivate() returns true
	 * this function will get called whenever this scene is deactivated, except when it
	 * supplied a loading scene.
	 * Called after #onDispose() and #onDeactive().
	 */
	public void unloadResources() {
		// Does nothing
	}

	/**
	 * Called just before the scene activates.
	 * @param outcome the outcome of the previous scene that was on the stack if there was
	 * any, else null.
	 * @param message the outcome message provided with the outcome, null if none
	 * was provided.
	 */
	public void onActivate(Outcomes outcome, String message) {
		// Does nothing
	}

	/**
	 * Called when the scene deactivates (another one is activated, push onto the scene stack).
	 * @note #onDisposed() instead when this scene is deleted (popped from the scene stack).
	 */
	public void onDeactivate() {
		// Does nothing
	}

	/**
	 * Called when the scene is deleted. Called before #unloadResources() if this
	 * scene has resources.
	 */
	public void onDisposed() {
		// Does nothing
	}

	/**
	 * Renders the scene
	 */
	public void render() {
		mGui.render();
	}

	/**
	 * @return true if the scene is done with it work, i.e. it should be popped
	 * from the stack.
	 */
	public final boolean isDone() {
		return mOutcome != null;
	}

	/**
	 * @return outcome of the scene, returns null if no outcome has been set (when
	 * #isDone() returns true.
	 */
	public final Outcomes getOutcome() {
		return mOutcome;
	}

	/**
	 * @return outcome message of the scene, if none exist it will return null.
	 */
	public final String getOutcomeMessage() {
		return mOutcomeMessage;
	}

	/**
	 * Sets the outcome of the scene, this also "completes" the scene making #isDone()
	 * return true. Outcomes can only be set once, this to prevent another later
	 * outcome to overwrite the original outcome.
	 * @param outcome the outcome of the scene
	 */
	public final void setOutcome(Outcomes outcome) {
		if (mOutcome == null) {
			mOutcome = outcome;
		}
	}

	/**
	 * @return the input multiplexer, this determines who shall get the input
	 */
	public final InputMultiplexer getInputMultiplexer() {
		return mInputMultiplexer;
	}

	/**
	 * Sets the outcome of the scene, including a description of the outcome. E.g.
	 * LOADING_FAILED_MISSING_FILE can specify the missing file in this message.
	 * Outcomes can only be set once, this to prevent another later outcome
	 * to overwrite the original outcome.
	 * @param outcome the outcome of the scene
	 * @param message a descriptive outcome message.
	 */
	public void setOutcome(Outcomes outcome, String message) {
		if (mOutcome == null) {
			setOutcome(outcome);
			mOutcomeMessage = message;
		}
	}

	/**
	 * Different outcomes a scene can have
	 */
	public enum Outcomes {
		/** Selected a definition */
		DEF_SELECTED,
		/** Canceled definition selection */
		DEF_SELECT_CANCEL,
		/** Level was successfully completed */
		LEVEL_COMPLETED,
		/** Player died during the level */
		LEVEL_PLAYER_DIED,
		/** Player decided to quit the level */
		LEVEL_QUIT,
		/** Loading succeeded */
		LOADING_SUCCEEDED,
		/** Loading failed, undefined resource type */
		LOADING_FAILED_UNDEFINED_TYPE,
		/** Loading failed, missing file */
		LOADING_FAILED_MISSING_FILE,
		/** Loading failed, corrupt file */
		LOADING_FAILED_CORRUPT_FILE,

		/** No outcome when an outcome isn't applicable, e.g. first time */
		NOT_APPLICAPLE,
	}

	/**
	 * @return Game time of the current scene
	 */
	public GameTime getGameTime() {
		return mGameTime;
	}

	/**
	 * @return bullet destroyer for the scene. Always returns null in Scene.
	 */
	BulletDestroyer getBulletDestroyer() {
		return null;
	}

	/**
	 * Clamps the X-coordinate of a pointer to the screen, i.e. if it's out of
	 * screen it will be clamped to the screen
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
	 * Clamps the Y-coordinate of a pointer to the screen, i.e. if it's out of
	 * screen it will be clamped to the screen
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
	 * Screen to world coordinate
	 * @param camera for the world coordinate
	 * @param screenPos screen position
	 * @param worldCoordinate the vector to set the world coordinate for
	 * @param clamp if the x and y coordinates should be clamped
	 */
	protected static void screenToWorldCoord(Camera camera, Vector2 screenPos, Vector2 worldCoordinate, boolean clamp) {
		screenToWorldCoord(camera, screenPos.x, screenPos.y, worldCoordinate, clamp);
	}

	/**
	 * Screen to world coordinate
	 * @param camera camera for the world coordinate
	 * @param x the X-coordinate of the screen
	 * @param y the Y-coordinate of the screen
	 * @param worldCoordinate the vector to set the world coordinate for
	 * @param clamp if the x and y coordinates should be clamped
	 */
	protected static void screenToWorldCoord(Camera camera, float x, float y, Vector2 worldCoordinate, boolean clamp) {
		if (clamp) {
			mTestPoint.set(clampX(x), clampY(y), 0);
		} else {
			mTestPoint.set(x, y, 0);
		}
		camera.unproject(mTestPoint);
		worldCoordinate.x = mTestPoint.x;
		worldCoordinate.y = mTestPoint.y;
	}

	/** Shape Renderer used for rendering stuff */
	protected ShapeRendererEx mShapeRenderer = new ShapeRendererEx();
	/** Input multiplexer */
	protected InputMultiplexer mInputMultiplexer = new InputMultiplexer();
	/** GUI for the scene */
	protected Gui mGui = null;

	/** Game time of current scene */
	private GameTime mGameTime = new GameTime();
	/** Outcome of scene, this is set when a derived class calls setOutcome */
	private Outcomes mOutcome = null;
	/** Message of the outcome */
	private String mOutcomeMessage = null;

	// Temporary variables
	/** For ray testing on player ship when touching it */
	private static Vector3 mTestPoint = new Vector3();
}
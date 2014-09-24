package com.spiddekauga.voider.scene;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.spiddekauga.utils.GameTime;
import com.spiddekauga.utils.commands.Invoker;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.game.BulletDestroyer;
import com.spiddekauga.voider.repo.resource.ResourceCacheFacade;
import com.spiddekauga.voider.repo.resource.ResourceCorruptException;
import com.spiddekauga.voider.repo.resource.ResourceNotFoundException;
import com.spiddekauga.voider.scene.Scene.Outcomes;

/**
 * Switches between scenes in an game/app.
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class SceneSwitcher {
	/**
	 * Switches to the specified scene. This scene will not be removed until it self has
	 * decided that it has finished.
	 * @param scene the scene to switch to.
	 * @see #switchTo(Class) for switching to a scene that already exists.
	 * @see #switchTo(Scene, LoadingScene) for forcing another loading scene
	 */
	public static void switchTo(Scene scene) {
		switchTo(scene, null);
	}

	/**
	 * Switches to the specified scene. This scene will not be removed until it self has
	 * decided that it has finished.
	 * @param scene the scene to switch to.
	 * @param loadingScene this scene will override any other loading scene, i.e. while
	 *        the scene is loading the loading scene will be displayed in its place. If
	 *        loadingScene is null, it works exactly as {@link #switchTo(Scene)}.
	 * @see #switchTo(Scene)
	 * @see #switchTo(Class) for switching to a scene that already exists.
	 */
	public static void switchTo(Scene scene, LoadingScene loadingScene) {
		deactivateCurrentScene();

		mScenes.push(scene);
		loadActiveSceneResources(loadingScene);
	}

	/**
	 * Tries to switch to a scene that already exists. If a scene of the specified type
	 * exist it will move that scene to the top of the stack.
	 * @param sceneType the type of scene to switch to
	 * @return true if found the scene of the specified type and switched to it, false if
	 *         no scene of the specified type was found.
	 * @see #switchTo(Scene, LoadingScene)
	 * @see #switchTo(Scene)
	 * @see #returnTo(Class)
	 */
	public static boolean switchTo(Class<? extends Scene> sceneType) {
		Scene foundScene = null;
		Iterator<Scene> sceneIt = mScenes.iterator();

		while (sceneIt.hasNext() && foundScene == null) {
			Scene currentScene = sceneIt.next();

			if (currentScene.getClass() == sceneType) {
				foundScene = currentScene;
				sceneIt.remove();
			}
		}

		if (foundScene != null) {
			deactivateCurrentScene();

			mScenes.push(foundScene);
			// Does the current scene need loading resources?
			if (foundScene.unloadResourcesOnDeactivate()) {
				loadActiveSceneResources();
			} else {
				foundScene.reloadResourcesOnActivate(Outcomes.NOT_APPLICAPLE, null);
				activateCurrentScene(Outcomes.NOT_APPLICAPLE, null);
				Gdx.input.setInputProcessor(foundScene.getInputMultiplexer());
			}

			return true;
		}

		return false;
	}

	/**
	 * Tries to return to a scene that already exists. Will pop all scenes that are above
	 * the specified scene type.
	 * @param sceneType the type of scene to return to
	 * @return true if the scene was found and was returned to, false if no scene of this
	 *         type was found.
	 * @see #switchTo(Class)
	 * @see #switchTo(Scene)
	 * @see #switchTo(Scene, LoadingScene)
	 */
	public static boolean returnTo(Class<? extends Scene> sceneType) {
		boolean foundScene = false;
		Iterator<Scene> sceneIt = mScenes.descendingIterator();
		while (sceneIt.hasNext()) {
			Scene currentScene = sceneIt.next();

			if (!foundScene) {
				if (currentScene.getClass() == sceneType) {
					foundScene = true;
				}
			}
			// Remove all scenes after the found scene
			else {
				sceneIt.remove();

				// If it was the last scene, deactivate it too
				if (!sceneIt.hasNext()) {
					currentScene.onDeactivate();
					mScenesNeedUnloading.add(currentScene);
				}
				// Else check if needs to unload resources
				else {
					if (currentScene.isResourcesLoaded()) {
						mScenesNeedUnloading.add(currentScene);
					}
				}

				// Unload current scene
				ResourceCacheFacade.unload(currentScene);

				currentScene.setOutcome(Outcomes.NOT_APPLICAPLE);
				currentScene.onDispose();
			}
		}

		// Activate current scene
		if (foundScene) {
			Scene activateScene = mScenes.peek();
			// Does the current scene needs loading resources?
			if (activateScene.unloadResourcesOnDeactivate()) {
				loadActiveSceneResources();
			} else {
				activateScene.reloadResourcesOnActivate(Outcomes.NOT_APPLICAPLE, null);
				activateCurrentScene(Outcomes.NOT_APPLICAPLE, null);
				Gdx.input.setInputProcessor(activateScene.getInputMultiplexer());
			}
		}

		return foundScene;
	}

	/**
	 * Disposes the scene switcher
	 */
	public static void dispose() {
		if (mScenes != null) {
			for (Scene scene : mScenes) {
				scene.onDispose();
				ResourceCacheFacade.unload(scene);
			}
			mScenes.clear();
		}
	}

	/**
	 * Call when the window has been resized. This will resize all scenes
	 * @param width new width of the window
	 * @param height new height of the window
	 */
	public static void resize(int width, int height) {
		for (Scene scene : mScenes) {
			scene.onResize(width, height);
		}
	}

	/**
	 * Gets the GameTime object from the scene at the top of the stack.
	 * @return game time object, null if no scene exists
	 */
	public static GameTime getGameTime() {
		if (mScenes.isEmpty()) {
			return null;
		} else {
			return mScenes.peek().getGameTime();
		}
	}

	/**
	 * Gets the Bullet destroyer for the scene at the top of the stack.
	 * @return bullet destroy, null if the scene doesn't have a bullet destroyer or if no
	 *         scenes are on the stack.
	 */
	public static BulletDestroyer getBulletDestroyer() {
		if (mScenes.isEmpty()) {
			return null;
		} else {
			return mScenes.peek().getBulletDestroyer();
		}
	}

	/**
	 * @param skipLoadingScenes Set to true to return the scene to be activated after a
	 *        loading scene is done (if one is active)
	 * @return current active scene
	 */
	public static Scene getActiveScene(boolean skipLoadingScenes) {
		Scene activeScene = null;

		if (!mScenes.isEmpty()) {
			activeScene = mScenes.peek();

			if (skipLoadingScenes && activeScene instanceof LoadingScene) {
				Iterator<Scene> iterator = mScenes.iterator();
				// Skip first as we already know it's a loading scene
				iterator.next();

				while (activeScene instanceof LoadingScene && iterator.hasNext()) {
					activeScene = iterator.next();
				}

				// If we have iterated through all scenes and we still only find loading
				// scenes
				// return null
				if (activeScene instanceof LoadingScene) {
					activeScene = null;
				}
			}
		}

		return activeScene;
	}

	/**
	 * Return screen width in world coordinates, but only if the current scene is a world
	 * scene.
	 * @return screen width in world coordinates, if scene is not a world it return 0.
	 */
	public static float getWorldWidth() {
		if (mScenes.isEmpty()) {
			return 0;
		} else {
			return mScenes.peek().getWorldWidth();
		}
	}

	/**
	 * Return screen height in world coordinates, but only if the current scene is a world
	 * scene.
	 * @return screen height in world coordinates, if scene is not a world it return 0.
	 */
	public static float getWorldHeight() {
		if (mScenes.isEmpty()) {
			return 0;
		} else {
			return mScenes.peek().getWorldHeight();
		}
	}

	/**
	 * @return 0,0 of screen in world coordinates, null if current scene isn't a world
	 *         scene. Remember to free the returned vector with
	 *         Pools.vector2.free(returnedVector);
	 */
	public static Vector2 getWorldMinCoordinates() {
		if (mScenes.isEmpty()) {
			return null;
		} else {
			return mScenes.peek().getWorldMinCoordinates();
		}
	}

	/**
	 * @return screenWidth,screenHeight in world coordinates, null if current scene isn't
	 *         a world scene. Remember to free the returned vector with
	 *         Pools.vector2.free(returnedVector);
	 */
	public static Vector2 getWorldMaxCoordinates() {
		if (mScenes.isEmpty()) {
			return null;
		} else {
			return mScenes.peek().getWorldMaxCoordinates();
		}
	}

	/**
	 * @return world:screen ratio, i.e. how many worlds it goes to fill the screen, or
	 *         screen / world. 0 if the scene isn't a world scene.
	 */
	public static float getWorldScreenRatio() {
		if (mScenes.isEmpty()) {
			return 0;
		} else {
			float worldWidth = getWorldWidth();
			if (worldWidth != 0) {
				return Gdx.graphics.getWidth() / worldWidth;
			} else {
				return 0;
			}
		}
	}

	/**
	 * @return invoker of the current scene, null if the scene doesn't have an invoker or
	 *         if no scene exists
	 */
	public static Invoker getInvoker() {
		if (mScenes.isEmpty()) {
			return null;
		} else {
			return mScenes.peek().getInvoker();
		}
	}

	/**
	 * @return picking fixture for current editor scene, null if the scene doesn't have a
	 *         picking fixture definition or no scene exists
	 */
	public static FixtureDef getPickingFixtureDef() {
		if (mScenes.isEmpty()) {
			return null;
		} else {
			return mScenes.peek().getPickingFixtureDef();
		}
	}

	/**
	 * @return picking vertices for editor scenes, null if the scene doesn't have picking
	 *         vertices or no scene exists.
	 */
	public static ArrayList<Vector2> getPickingVertices() {
		if (mScenes.isEmpty()) {
			return null;
		} else {
			return mScenes.peek().getPickingVertices();
		}
	}

	/**
	 * @return GUI of the current scene, null if no scene exists
	 */
	public static Gui getGui() {
		if (mScenes.isEmpty()) {
			return null;
		} else {
			return mScenes.peek().mGui;
		}
	}

	/**
	 * @return Stage of the current scene, null if no scene exists
	 */
	public static Stage getStage() {
		if (mScenes.isEmpty()) {
			return null;
		} else {
			return mScenes.peek().mGui.getStage();
		}
	}

	/**
	 * Prints an error message to the current scene
	 * @param message the message to print
	 */
	public static void showMessage(String message) {
		if (!mScenes.isEmpty()) {
			mScenes.peek().mGui.showMessage(message);
		}
	}

	/**
	 * Displays a message in the message window with the specified style
	 * @param message the message to display
	 * @param style the label style of the message
	 */
	public void showMessage(String message, LabelStyle style) {
		if (!mScenes.isEmpty()) {
			mScenes.peek().mGui.showMessage(message, style);
		}
	}

	/**
	 * Displays a highlighted message
	 * @param message the message to display as highlighted
	 */
	public static void showHighlightMessage(String message) {
		if (!mScenes.isEmpty()) {
			mScenes.peek().mGui.showHighlightMessage(message);
		}
	}

	/**
	 * Displays an error message
	 * @param message the message to display as an error
	 */
	public static void showErrorMessage(String message) {
		if (!mScenes.isEmpty()) {
			mScenes.peek().mGui.showErrorMessage(message);
		}
	}

	/**
	 * Displays a successful message
	 * @param message the message to display as successful
	 */
	public static void showSuccessMessage(String message) {
		if (!mScenes.isEmpty()) {
			mScenes.peek().mGui.showSuccessMessage(message);
		}
	}

	/**
	 * Show conflict window
	 */
	public static void showConflictWindow() {
		if (!mScenes.isEmpty()) {
			mScenes.peek().mGui.showConflictWindow();
		}
	}

	/**
	 * Show wait window
	 * @param message optional message to display
	 */
	public static void showWaitWindow(String message) {
		if (!mScenes.isEmpty()) {
			mScenes.peek().mGui.showWaitWindow(message);
		}
	}

	/**
	 * Hides the wait window
	 */
	public static void hideWaitWindow() {
		if (!mScenes.isEmpty()) {
			mScenes.peek().mGui.hideWaitWindow();
		}
	}

	/**
	 * Shows the a progress bar for loading/downloading/uploading window
	 * @param message the message to display
	 */
	public static void showProgressBar(String message) {
		if (!mScenes.isEmpty()) {
			mScenes.peek().mGui.showProgressBar(message);
		}
	}

	/**
	 * Hides the progress bar
	 */
	public static void hideProgressBar() {
		if (!mScenes.isEmpty()) {
			mScenes.peek().mGui.hideProgressBar();
		}
	}

	/**
	 * Updates the progress bar, doesn't set the text
	 * @param percentage how many percentage that has been loaded
	 */
	public static void updateProgressBar(float percentage) {
		if (!mScenes.isEmpty()) {
			mScenes.peek().mGui.updateProgressBar(percentage);
		}
	}

	/**
	 * Updates the progress bar
	 * @param percentage how many percentage that has been loaded
	 * @param message optional message, keeps previous if null
	 */
	public static void updateProgressBar(float percentage, String message) {
		if (!mScenes.isEmpty()) {
			mScenes.peek().mGui.updateProgressBar(percentage, message);
		}
	}

	/**
	 * Add a listener to the scene's input multiplexor
	 * @param processor the listener to add
	 */
	public static void addListener(InputProcessor processor) {
		if (!mScenes.isEmpty()) {
			InputMultiplexer inputMultiplexer = mScenes.peek().getInputMultiplexer();
			inputMultiplexer.addProcessor(processor);
		}
	}

	/**
	 * Removes a listener from the scene's input multiplexor
	 * @param processor the listener to remove
	 */
	public static void removeListener(InputProcessor processor) {
		if (!mScenes.isEmpty()) {
			InputMultiplexer inputMultiplexer = mScenes.peek().getInputMultiplexer();
			inputMultiplexer.removeProcessor(processor);
		}
	}

	/**
	 * Updates (and renders) the scene switcher and the current scene.
	 */
	public static void update() {
		if (mScenes.isEmpty()) {
			return;
		}

		try {

			// Scene which resources needs to be unloaded after we have loaded all for the
			// next scene
			if (!mScenesNeedUnloading.isEmpty() && !ResourceCacheFacade.isLoading()) {
				Iterator<Scene> sceneIt = mScenesNeedUnloading.iterator();
				while (sceneIt.hasNext()) {
					Scene unloadScene = sceneIt.next();
					unloadScene.unloadResources();
					sceneIt.remove();
				}
			}


			Scene currentScene = mScenes.peek();

			// Loading using no loading scene
			if (currentScene.isLoading()) {
				try {
					boolean allLoaded = ResourceCacheFacade.update();

					// Loading done -> Activate scene
					if (allLoaded) {
						if (mOutcome != null) {
							activateCurrentScene(mOutcome, mOutcomeMessage);
							mOutcome = null;
							mOutcomeMessage = null;
						} else {
							activateCurrentScene(Outcomes.LOADING_SUCCEEDED, null);
						}
						currentScene.setLoading(false);
					}
				} catch (ResourceNotFoundException e) {
					e.printStackTrace();
					activateCurrentScene(Outcomes.LOADING_FAILED_MISSING_FILE, e.toString());
				} catch (ResourceCorruptException e) {
					e.printStackTrace();
					activateCurrentScene(Outcomes.LOADING_FAILED_CORRUPT_FILE, e.toString());
				}
				Gdx.input.setInputProcessor(currentScene.getInputMultiplexer());
			}
			// Scene is done, pop it
			else if (currentScene.isDone()) {
				popCurrentScene();
			}
			// Else -> Update it
			else {
				ResourceCacheFacade.update();
				currentScene.run();
			}

		} catch (RuntimeException e) {
			handleException(e);
		}
	}

	/**
	 * Handles the exception
	 * @param exception the exception to handle
	 */
	private static void handleException(RuntimeException exception) {
		boolean handleException = Config.Debug.EXCEPTION_HANDLER && !mScenes.isEmpty();
		if (handleException) {
			Scene currentScene = mScenes.peek();

			if (currentScene.isInitialized()) {
				currentScene.handleException(exception);
			} else {
				throw exception;
			}
		} else {
			throw exception;
		}
	}

	/**
	 * Activates the current scene
	 * @param outcome the outcome of the previous scene
	 * @param message message of the previous scene
	 */
	private static void activateCurrentScene(Outcomes outcome, Object message) {
		Scene currentScene = mScenes.peek();

		if (!currentScene.isInitialized()) {
			currentScene.onInit();
		}

		currentScene.onActivate(outcome, message);
	}

	/**
	 * Pops the current scene
	 */
	private static void popCurrentScene() {
		Scene poppedScene = mScenes.pop();
		Outcomes outcome = poppedScene.getOutcome();
		Object outcomeMessage = poppedScene.getOutcomeMessage();

		poppedScene.onDispose();
		Gdx.input.setInputProcessor(null);

		// Unload resources from the old scene
		if (poppedScene.isResourcesLoaded()) {
			mScenesNeedUnloading.add(poppedScene);
		}
		ResourceCacheFacade.unload(poppedScene);


		// Go to next scene, or return to the previous?
		// Go to next scene
		if (poppedScene.getNextScene() != null) {
			mScenes.push(poppedScene.getNextScene());
			loadActiveSceneResources();
		}
		// Activate previous scene
		else if (!mScenes.isEmpty()) {
			Scene previousScene = mScenes.getFirst();

			// Does the current scene need loading resources?
			// If the popped scene is a LoadingScene it should not try to load again...
			if (!previousScene.isResourcesLoaded()) {
				// Save outcome so that we don't get LOADING_SUCCESS
				mOutcome = outcome;
				mOutcomeMessage = outcomeMessage;
				loadActiveSceneResources();
			} else {
				// Use old outcome instead of LOADING_SUCCESS
				if (mOutcome != null) {
					outcome = mOutcome;
					outcomeMessage = mOutcomeMessage;
					mOutcome = null;
					mOutcomeMessage = null;
				}
				previousScene.reloadResourcesOnActivate(outcome, outcomeMessage);
				activateCurrentScene(outcome, outcomeMessage);
				Gdx.input.setInputProcessor(previousScene.getInputMultiplexer());
			}
		}
	}

	/**
	 * Loads the resources of the current active scene
	 */
	private static void loadActiveSceneResources() {
		loadActiveSceneResources(null);
	}

	/**
	 * Loads the resources of the current active scene
	 * @param forceLoadingScene forces this loading scene if not null.
	 */
	private static void loadActiveSceneResources(LoadingScene forceLoadingScene) {
		Scene currentScene = mScenes.peek();

		if (forceLoadingScene != null) {
			forceLoadingScene.setSceneToload(currentScene);
			switchTo(forceLoadingScene);
		} else {
			LoadingScene loadingScene = currentScene.getLoadingScene();

			if (loadingScene != null) {
				loadingScene.setSceneToload(currentScene);
				switchTo(loadingScene);
			} else {
				currentScene.loadResources();
				currentScene.setLoading(true);
			}
		}
	}

	/**
	 * Deactivates the current scene
	 */
	private static void deactivateCurrentScene() {
		if (!mScenes.isEmpty()) {
			Scene previousScene = mScenes.peek();
			previousScene.onDeactivate();

			if (Gdx.input != null) {
				Gdx.input.setInputProcessor(null);
			}

			// Should we unload resources?
			if (previousScene.isResourcesLoaded() && previousScene.unloadResourcesOnDeactivate()) {
				mScenesNeedUnloading.add(previousScene);
			}
		}
	}

	/**
	 * Report an exception
	 * @param exception the exception that was thrown
	 */
	public static void handleException(Exception exception) {
		if (!mScenes.isEmpty()) {
			mScenes.peek().handleException(exception);
		}
	}

	/**
	 * Private constructor to ensure no instance is created
	 */
	private SceneSwitcher() {
		// Does nothing
	}

	/** Stack with all the active scenes */
	private static LinkedList<Scene> mScenes = new LinkedList<Scene>();
	/** Generally last popped scene that needs unloading, but could be several */
	private static ArrayList<Scene> mScenesNeedUnloading = new ArrayList<Scene>();

	/** Outcome of last scene */
	private static Outcomes mOutcome = null;
	/** Message from the last outcome */
	private static Object mOutcomeMessage = null;
}

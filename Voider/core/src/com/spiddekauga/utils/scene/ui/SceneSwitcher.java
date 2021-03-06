package com.spiddekauga.utils.scene.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.spiddekauga.utils.GameTime;
import com.spiddekauga.utils.commands.Invoker;
import com.spiddekauga.utils.scene.ui.Scene.Outcomes;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.Config.Debug.Builds;
import com.spiddekauga.voider.game.BulletDestroyer;
import com.spiddekauga.voider.menu.LoginScene;
import com.spiddekauga.voider.menu.MainMenu;
import com.spiddekauga.voider.repo.resource.ResourceCacheFacade;
import com.spiddekauga.voider.repo.resource.ResourceCorruptException;
import com.spiddekauga.voider.repo.resource.ResourceNotFoundException;
import com.spiddekauga.voider.scene.ui.UiFactory;
import com.spiddekauga.voider.utils.event.EventDispatcher;
import com.spiddekauga.voider.utils.event.EventTypes;
import com.spiddekauga.voider.utils.event.GameEvent;
import com.spiddekauga.voider.utils.event.IEventListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Switches between scenes in an game/app.
 */
public class SceneSwitcher {
/** Stack with all the active scenes */
private static LinkedList<Scene> mScenes = new LinkedList<>();
/** Generally last popped scene that needs unloading, but could be several */
private static ArrayList<Scene> mScenesNeedUnloading = new ArrayList<>();
/** Outcome of last scene */
private static Outcomes mOutcome = Outcomes.NOT_APPLICAPLE;
/** Message from the last outcome */
private static Object mOutcomeMessage = null;

static {
	EventDispatcher.getInstance().connect(EventTypes.USER_LOGGING_OUT, new ForceUnloadListener());
}

/**
 * Private constructor to ensure no instance is created
 */
private SceneSwitcher() {
	// Does nothing
}

/**
 * Switches to the specified scene. This scene will not be removed until it self has decided that it
 * has finished.
 * @param scene the scene to switch to.
 * @see #switchTo(Class) for switching to a scene that already exists.
 * @see #switchTo(Scene, LoadingScene) for forcing another loading scene
 */
public static void switchTo(Scene scene) {
	switchTo(scene, null);
}

/**
 * Switches to the specified scene. This scene will not be removed until it self has decided that it
 * has finished.
 * @param scene the scene to switch to.
 * @param loadingScene this scene will override any other loading scene, i.e. while the scene is
 * loading the loading scene will be displayed in its place. If loadingScene is null, it works
 * exactly as {@link #switchTo(Scene)}.
 * @see #switchTo(Scene)
 * @see #switchTo(Class) for switching to a scene that already exists.
 */
public static void switchTo(Scene scene, LoadingScene loadingScene) {
	deactivateCurrentScene();

	mScenes.push(scene);
	loadActiveSceneResources(loadingScene);
}

/**
 * Tries to switch to a scene that already exists. If a scene of the specified type exist it will
 * move that scene to the top of the stack.
 * @param sceneType the type of scene to switch to
 * @return true if found the scene of the specified type and switched to it, false if no scene of
 * the specified type was found.
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
			foundScene.reloadResourcesOnActivate(mOutcome, mOutcomeMessage);
			activateCurrentScene();
		}

		return true;
	}

	return false;
}

/**
 * Tries to return to a scene that already exists. Will pop all scenes that are above the specified
 * scene type.
 * @param sceneType the type of scene to return to
 * @return true if the scene was found and was returned to, false if no scene of this type was
 * found.
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
				currentScene.pause();
			}

			// Unload loaded resources
			if (currentScene.isResourcesLoaded()) {
				// Unload directly if this is an exception
				if (mOutcome == Outcomes.EXCEPTION) {
					ResourceCacheFacade.unload(currentScene);
				} else {
					mScenesNeedUnloading.add(currentScene);
				}
			}
			currentScene.destroy();
		}
	}

	// Activate current scene
	if (foundScene) {
		Scene activateScene = mScenes.peek();
		// Does the current scene needs loading resources?
		if (activateScene.unloadResourcesOnDeactivate()) {
			loadActiveSceneResources();
		} else {
			activateScene.reloadResourcesOnActivate(mOutcome, mOutcomeMessage);
			activateCurrentScene();
		}
	}

	return foundScene;
}

/**
 * Clears all scenes and unloads the resources for them (during the next update call)
 * @see #dispose() Call this when you want to clear everything directly
 */
public static void clearScenes() {
	// Deactivate the current scene
	if (!mScenes.isEmpty()) {
		mScenes.peek().pause();
	}

	for (Scene scene : mScenes) {
		scene.destroy();
		mScenesNeedUnloading.add(scene);
	}

	mScenes.clear();
}

/**
 * Disposes the scene switcher
 * @see #clearScenes() if you just want to clear all the scenes
 */
public static void dispose() {
	if (mScenes != null) {
		for (Scene scene : mScenes) {
			scene.destroy();
			if (scene.isResourcesLoaded()) {
				scene.unloadResources();
			}
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
	if (!mScenes.isEmpty()) {
		NotificationShower.getInstance().resetPosition();
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
 * @return bullet destroy, null if the scene doesn't have a bullet destroyer or if no scenes are on
 * the stack.
 */
public static BulletDestroyer getBulletDestroyer() {
	if (mScenes.isEmpty()) {
		return null;
	} else {
		return mScenes.peek().getBulletDestroyer();
	}
}

/**
 * @param skipLoadingScenes Set to true to return the scene to be activated after a loading scene is
 * done (if one is active)
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
 * Checks if the active scene is of the specified type
 * @param clazz class of the active scene
 * @return true if the active scene is of the specified type
 */
public static boolean isActiveScene(Class<?> clazz) {
	return !mScenes.isEmpty() && mScenes.peek().getClass().equals(clazz);
}

/**
 * Return screen height in world coordinates, but only if the current scene is a world scene.
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
 * @return 0, 0 of screen in world coordinates, null if current scene isn't a world scene. Remember
 * to free the returned vector with Pools.vector2.free(returnedVector);
 */
public static Vector2 getWorldMinCoordinates() {
	if (mScenes.isEmpty()) {
		return null;
	} else {
		return mScenes.peek().getWorldMinCoordinates();
	}
}

/**
 * @return screenWidth, screenHeight in world coordinates, null if current scene isn't a world
 * scene. Remember to free the returned vector with Pools.vector2.free(returnedVector);
 */
public static Vector2 getWorldMaxCoordinates() {
	if (mScenes.isEmpty()) {
		return null;
	} else {
		return mScenes.peek().getWorldMaxCoordinates();
	}
}

/**
 * @return world:screen ratio, i.e. how many worlds it goes to fill the screen, or screen / world. 0
 * if the scene isn't a world scene.
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
 * Return screen width in world coordinates, but only if the current scene is a world scene.
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
 * @return invoker of the current scene, null if the scene doesn't have an invoker or if no scene
 * exists
 */
public static Invoker getInvoker() {
	if (mScenes.isEmpty()) {
		return null;
	} else {
		return mScenes.peek().getInvoker();
	}
}

/**
 * @return picking fixture for current editor scene, null if the scene doesn't have a picking
 * fixture definition or no scene exists
 */
public static FixtureDef getPickingFixtureDef() {
	if (mScenes.isEmpty()) {
		return null;
	} else {
		return mScenes.peek().getPickingFixtureDef();
	}
}

/**
 * @return picking vertices for editor scenes, null if the scene doesn't have picking vertices or no
 * scene exists.
 */
public static List<Vector2> getPickingVertices() {
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
		return mScenes.peek().getGui();
	}
}

/**
 * @return Stage of the current scene, null if no scene exists
 */
public static Stage getStage() {
	if (mScenes.isEmpty()) {
		return null;
	} else {
		return mScenes.peek().getGui().getStage();
	}
}

/**
 * Reinitialize all scenes' UI. Useful to call when UI has changed.
 */
public static void reloadUi() {
	UiFactory.getInstance().dispose();
	NotificationShower.getInstance().dispose();
	for (Scene scene : mScenes) {
		scene.getGui().destroy();
		scene.getGui().create();
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
				ResourceCacheFacade.unload(unloadScene);
				sceneIt.remove();
			}
		}


		Scene currentScene = mScenes.peek();

		// Loading using no loading scene
		if (currentScene.isLoading() && !(currentScene instanceof LoadingScene)) {
			try {
				boolean allLoaded = ResourceCacheFacade.update();

				// Loading done -> Activate scene
				if (allLoaded) {
					activateCurrentScene(Outcomes.LOADING_SUCCEEDED);
					currentScene.setLoading(false);
				}
			} catch (ResourceNotFoundException e) {
				e.printStackTrace();
				activateCurrentScene(Outcomes.LOADING_FAILED_MISSING_FILE);
			} catch (ResourceCorruptException e) {
				e.printStackTrace();
				activateCurrentScene(Outcomes.LOADING_FAILED_CORRUPT_FILE);
			}
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
 * Handles the exception. And goes back to {@link MainMenu} or {@link LoginScene}.
 * @param exception the exception to handle
 */
public static void handleException(RuntimeException exception) {
	if (mScenes.isEmpty()) {
		throw exception;
	}


	Scene currentScene = mScenes.peek();

	// Show exception in console
	if (Config.Debug.isBuildOrBelow(Builds.DEV_SERVER)) {
		exception.printStackTrace();
	}

	mOutcome = Outcomes.EXCEPTION;
	mOutcomeMessage = exception;


	if (currentScene instanceof LoginScene) {
		// TODO implement exception handling when not logged in
//		// Restart the scene and show the exception
//		if (currentScene.isInitialized()) {
//			currentScene.setNextScene(new LoginScene());
//			popCurrentScene();
//		}
//		// Quit the game if not initialized
//		else {
//			throw exception;
//		}
		Gdx.app.exit();
		return;
	}
	// Go back to Main Menu (or login scene)
	else {
		// Restart Main Menu or go back to Login
		if (currentScene instanceof MainMenu) {
			if (currentScene.isInitialized()) {
				currentScene.setNextScene(new MainMenu());
			} else {
				// TODO implement exception handling when not logged in
//				currentScene.setNextScene(new LoginScene());
				Gdx.app.exit();
				return;
			}
			popCurrentScene();

			// Logout user
//			if (!currentScene.isInitialized()) {
//				User.getGlobalUser().logout();
//			}
		}
		// Go back to Main Menu
		else {
			returnTo(MainMenu.class);
		}
	}
}

/**
 * Activates the current scene
 */
private static void activateCurrentScene() {
	activateCurrentScene(Outcomes.NOT_APPLICAPLE);
}

/**
 * Activates the current scene
 * @param loadingOutcome outcome from loading scene
 */
private static void activateCurrentScene(Outcomes loadingOutcome) {
	Scene currentScene = mScenes.peek();

	if (!currentScene.isInitialized()) {
		currentScene.create();
	}

	if (mOutcome == null) {
		Config.Debug.assertException(new IllegalStateException("Outcome has been set to null!"));
	}

	currentScene.resume(mOutcome, mOutcomeMessage, loadingOutcome);
	mOutcome = Outcomes.NOT_APPLICAPLE;
	mOutcomeMessage = null;
}

/**
 * Calls {@link Scene#pause()}, {@link Scene#destroy()}, and unloads the scene resources. Goes to
 * the next scene if {@link Scene#getNextScene()} doesn't return null. Otherwise it returns to the
 * previous scene.
 */
private static void popCurrentScene() {
	Scene poppedScene = mScenes.pop();
	Outcomes loadingOutcome = Outcomes.NOT_APPLICAPLE;

	poppedScene.pause();
	poppedScene.destroy();

	if (poppedScene instanceof LoadingScene) {
		loadingOutcome = poppedScene.getOutcome();
	}
	// Set outcome if we haven't set one to be applied
	else if (mOutcome == Outcomes.NOT_APPLICAPLE) {
		mOutcome = poppedScene.getOutcome();
		mOutcomeMessage = poppedScene.getOutcomeMessage();
	}

	// Unload resources from the old scene
	if (poppedScene.isResourcesLoaded()) {
		mScenesNeedUnloading.add(poppedScene);
	}

	// Go to next scene
	Scene nextScene = poppedScene.getNextScene();
	if (nextScene != null) {
		mScenes.push(nextScene);
		loadActiveSceneResources();
	}
	// Activate previous scene
	else if (!mScenes.isEmpty()) {
		Scene previousScene = mScenes.getFirst();

		// Does the current scene need loading resources?
		// If the popped scene is a LoadingScene it should not try to load again...
		if (!previousScene.isResourcesLoaded()) {
			// Save outcome so that we don't get LOADING_SUCCESS
			loadActiveSceneResources();
		} else {
			previousScene.reloadResourcesOnActivate(mOutcome, mOutcomeMessage);
			activateCurrentScene(loadingOutcome);
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
		switchTo(forceLoadingScene);
	} else {
		LoadingScene loadingScene = currentScene.getLoadingScene();

		if (loadingScene != null) {
			switchTo(loadingScene);
		} else {
			currentScene.setLoading(true);
		}
	}

	currentScene.loadResources();

	if (currentScene instanceof LoadingScene) {
		currentScene.create();
		currentScene.resume(Outcomes.NOT_APPLICAPLE, null, Outcomes.NOT_APPLICAPLE);
	}
}

/**
 * Deactivates the current scene
 */
private static void deactivateCurrentScene() {
	if (!mScenes.isEmpty()) {
		Scene previousScene = mScenes.peek();
		if (previousScene.isInitialized()) {
			previousScene.pause();
		}

		// Should we unload resources?
		if (previousScene.isResourcesLoaded() && previousScene.unloadResourcesOnDeactivate()) {
			mScenesNeedUnloading.add(previousScene);
		}
	}
}

/**
 * Force unload of all resources when the user logs out
 */
private static class ForceUnloadListener implements IEventListener {
	@Override
	public void handleEvent(GameEvent event) {
		if (event.type == EventTypes.USER_LOGGING_OUT) {
			for (Scene scene : mScenesNeedUnloading) {
				ResourceCacheFacade.unload(scene);
			}
			ResourceCacheFacade.finishLoading();
			mScenesNeedUnloading.clear();
		}
	}
}
}

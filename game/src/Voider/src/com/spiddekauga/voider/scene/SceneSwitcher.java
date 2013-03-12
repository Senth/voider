package com.spiddekauga.voider.scene;

import java.util.Iterator;
import java.util.LinkedList;

import com.badlogic.gdx.Gdx;
import com.spiddekauga.utils.GameTime;
import com.spiddekauga.utils.Invoker;
import com.spiddekauga.voider.game.BulletDestroyer;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.resources.ResourceCorruptException;
import com.spiddekauga.voider.resources.ResourceNotFoundException;
import com.spiddekauga.voider.resources.UndefinedResourceTypeException;
import com.spiddekauga.voider.scene.Scene.Outcomes;

/**
 * Switches between scenes in an game/app.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class SceneSwitcher {
	/**
	 * Switches to the specified scene. This scene will not be removed
	 * until it self has decided that it has finished.
	 * @param scene the scene to switch to.
	 */
	public static void switchTo(Scene scene) {
		deactivateCurrentScene();

		mScenes.push(scene);
		// Do we need to load resources for the scene?
		if (scene.hasResources()) {
			loadActiveSceneResources();
		}
		// No resources activate directly
		else {
			scene.onActivate(Outcomes.NOT_APPLICAPLE, null);
			Gdx.input.setInputProcessor(scene.getInputMultiplexer());
		}
	}

	/**
	 * Tries to switch to a scene that already exists. If a scene of the specified
	 * type exist it will move that scene to the top of the stack.
	 * @param sceneType the type of scene to switch to
	 * @return true if found the scene of the specified type and switched to it,
	 * false if no scene of the specified type was found.
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
			if (foundScene.hasResources() && foundScene.unloadResourcesOnDeactivate()) {
				loadActiveSceneResources();
			} else {
				foundScene.onActivate(Outcomes.NOT_APPLICAPLE, null);
				Gdx.input.setInputProcessor(foundScene.getInputMultiplexer());
			}

			return true;
		}

		return false;
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
	 * @return bullet destroy, null if the scene doesn't have a bullet destroyer or if
	 * no scenes are on the stack.
	 */
	public static BulletDestroyer getBulletDestroyer() {
		if (mScenes.isEmpty()) {
			return null;
		} else {
			return mScenes.peek().getBulletDestroyer();
		}
	}

	/**
	 * Return screen width in world coordinates, but only if the current scene
	 * is a world scene.
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
	 * Return screen height in world coordinates, but only if the current scene is
	 * a world scene.
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
	 * @return invoker of the current scene, null if the scene doesn't have an invoker or if
	 * no scene exists
	 */
	public static Invoker getInvoker() {
		if (mScenes.isEmpty()) {
			return null;
		} else {
			return mScenes.peek().getInvoker();
		}
	}

	/**
	 * Updates (and renders) the scene switcher and the current scene.
	 */
	public static void update() {
		if (mScenes.isEmpty()) {
			return;
		}


		// Scene which resources needs to be unloaded after we have loaded all for the next scene
		if (mSceneNeedsUnloading != null && !ResourceCacheFacade.isLoading()) {
			mSceneNeedsUnloading.unloadResources();
			mSceneNeedsUnloading = null;
		}


		Scene currentScene = mScenes.getFirst();

		// Loading using no loading scene
		if (mSwitcherLoading) {
			try {
				boolean allLoaded = ResourceCacheFacade.update();

				// Loading done -> Activate scene
				if (allLoaded) {
					currentScene.onActivate(Outcomes.LOADING_SUCCEEDED, null);
					Gdx.input.setInputProcessor(currentScene.getInputMultiplexer());
					mSwitcherLoading = false;
				}
			} catch (UndefinedResourceTypeException e) {
				currentScene.onActivate(Outcomes.LOADING_FAILED_UNDEFINED_TYPE, e.toString());
				Gdx.input.setInputProcessor(currentScene.getInputMultiplexer());
			} catch (ResourceNotFoundException e) {
				currentScene.onActivate(Outcomes.LOADING_FAILED_MISSING_FILE, e.toString());
				Gdx.input.setInputProcessor(currentScene.getInputMultiplexer());
			} catch (ResourceCorruptException e) {
				currentScene.onActivate(Outcomes.LOADING_FAILED_CORRUPT_FILE, e.toString());
				Gdx.input.setInputProcessor(currentScene.getInputMultiplexer());
			}
		}
		// Running a scene or a loading scene, either way run the scene...
		else {
			// Done -> Pop scene
			if (currentScene.isDone()) {
				popCurrentScene();
			}
			// Else -> Update it
			else {
				currentScene.run();
			}
		}
	}

	/**
	 * Pops the current scene
	 */
	private static void popCurrentScene() {
		Scene poppedScene = mScenes.pop();
		Outcomes outcome = poppedScene.getOutcome();
		String outcomeMessage = poppedScene.getOutcomeMessage();

		poppedScene.onDisposed();
		Gdx.input.setInputProcessor(null);

		// Unload resources from the old scene
		if (poppedScene.hasResources()) {
			mSceneNeedsUnloading = poppedScene;
		}

		// Activate new scene
		if (!mScenes.isEmpty()) {
			Scene currentScene = mScenes.getFirst();

			// Does the current scene need loading resources?
			// If the popped scene is a LoadingScene it should not try to load again...
			if (currentScene.hasResources() &&
					currentScene.unloadResourcesOnDeactivate() &&
					!(poppedScene instanceof LoadingScene)) {
				loadActiveSceneResources();
			} else {
				currentScene.onActivate(outcome, outcomeMessage);
				Gdx.input.setInputProcessor(currentScene.getInputMultiplexer());
			}
		}
	}

	/**
	 * Loads the resources of the current active scene
	 */
	private static void loadActiveSceneResources() {
		Scene currentsScene = mScenes.getFirst();
		currentsScene.loadResources();
		LoadingScene loadingScene = currentsScene.getLoadingScene();

		if (loadingScene != null) {
			switchTo(loadingScene);
		} else {
			mSwitcherLoading = true;
		}
	}

	/**
	 * Deactivates the current scene
	 */
	private static void deactivateCurrentScene() {
		if (!mScenes.isEmpty()) {
			Scene previouScene = mScenes.getFirst();
			previouScene.onDeactivate();
			Gdx.input.setInputProcessor(null);

			// Should we unload resources?
			if (previouScene.hasResources() && previouScene.unloadResourcesOnDeactivate()) {
				mSceneNeedsUnloading = previouScene;
			}
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
	/** If we're currently loading */
	private static boolean mSwitcherLoading = false;
	/** Last popped scene that has resoures that needs unloading */
	private static Scene mSceneNeedsUnloading = null;
}

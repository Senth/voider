package com.spiddekauga.voider.editor;

import java.util.ArrayList;
import java.util.UUID;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pools;
import com.spiddekauga.utils.Invoker;
import com.spiddekauga.utils.Scroller;
import com.spiddekauga.utils.Scroller.ScrollAxis;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.game.GameScene;
import com.spiddekauga.voider.game.Level;
import com.spiddekauga.voider.game.LevelDef;
import com.spiddekauga.voider.game.actors.Actor;
import com.spiddekauga.voider.game.actors.ActorDef;
import com.spiddekauga.voider.game.actors.EnemyActor;
import com.spiddekauga.voider.game.actors.EnemyActorDef;
import com.spiddekauga.voider.game.actors.EnemyGroup;
import com.spiddekauga.voider.game.actors.PickupActor;
import com.spiddekauga.voider.game.actors.PickupActorDef;
import com.spiddekauga.voider.game.actors.StaticTerrainActor;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.resources.ResourceNames;
import com.spiddekauga.voider.resources.ResourceSaver;
import com.spiddekauga.voider.resources.UndefinedResourceTypeException;
import com.spiddekauga.voider.scene.ActorTool;
import com.spiddekauga.voider.scene.AddActorTool;
import com.spiddekauga.voider.scene.AddEnemyTool;
import com.spiddekauga.voider.scene.DrawActorTool;
import com.spiddekauga.voider.scene.LoadingScene;
import com.spiddekauga.voider.scene.Scene;
import com.spiddekauga.voider.scene.SceneSwitcher;
import com.spiddekauga.voider.scene.SelectDefScene;
import com.spiddekauga.voider.scene.TouchTool;
import com.spiddekauga.voider.scene.WorldScene;

/**
 * The level editor scene
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class LevelEditor extends WorldScene implements IActorChangeEditor, IEditor {
	/**
	 * Constructor for the level editor
	 */
	public LevelEditor() {
		super(new LevelEditorGui());
		((LevelEditorGui)mGui).setLevelEditor(this);

		Actor.setEditorActive(true);

		mScroller = new Scroller(50, 2000, 10, 200, ScrollAxis.X);

		// Initialize all tools
		DrawActorTool terrainTool = new DrawActorTool(mCamera, mWorld, StaticTerrainActor.class, mInvoker, this);
		mTouchTools[Tools.STATIC_TERRAIN.ordinal()] = terrainTool ;
		AddActorTool pickupTool = new AddActorTool(mCamera, mWorld, PickupActor.class, mInvoker, true, this);
		mTouchTools[Tools.PICKUP.ordinal()] = pickupTool;
		AddEnemyTool enemyTool = new AddEnemyTool(mCamera, mWorld, mInvoker, this);
		mTouchTools[Tools.ENEMY.ordinal()] = enemyTool;

		switchTool(Tools.STATIC_TERRAIN);
	}

	@Override
	public void update() {
		super.update();
		mLevel.update(false);

		// Scrolling
		if (mScroller.isScrolling()) {
			mScroller.update(Gdx.graphics.getDeltaTime());

			// Update the camera
			Vector2 scrollCameraOrigin = Pools.obtain(Vector2.class);
			screenToWorldCoord(mCamera, mScroller.getOriginScroll(), scrollCameraOrigin, false);
			Vector2 scrollCameraCurrent = Pools.obtain(Vector2.class);
			screenToWorldCoord(mCamera, mScroller.getCurrentScroll(), scrollCameraCurrent, false);

			Vector2 diffScroll = Pools.obtain(Vector2.class);
			diffScroll.set(scrollCameraCurrent).sub(scrollCameraOrigin);

			mCamera.position.x = diffScroll.x + mScrollCameraOrigin.x;
			mCamera.update();

			Pools.free(scrollCameraCurrent);
			Pools.free(scrollCameraOrigin);
			Pools.free(diffScroll);
		}
	}

	@Override
	public void render() {
		super.render();

		if (!Config.Graphics.USE_DEBUG_RENDERER) {
			mLevel.render(mSpriteBatch);
			mLevel.renderEditor(mSpriteBatch);
		}
	}

	/**
	 * Sets the level that shall be played and resets tools, invoker, etc.
	 * @param level level to play
	 */
	public void setLevel(Level level) {
		if (mLevel != null) {
			if (ResourceCacheFacade.isLoaded(mLevel.getId(), Level.class)) {
				ResourceCacheFacade.unload(mLevel, mLevel.getDef());
			}
		}

		mLevel = level;
		mInvoker.dispose();

		clearTools();

		createActorBodies();
	}

	// --------------------------------
	//		Resource loading etc.
	// --------------------------------
	@Override
	public LoadingScene getLoadingScene() {
		/** @TODO create default loading scene */
		return null;
	}

	@Override
	public boolean hasResources() {
		return true;
	}

	@Override
	public void loadResources() {
		ResourceCacheFacade.load(ResourceNames.EDITOR_BUTTONS);
		try {
			ResourceCacheFacade.loadAllOf(EnemyActorDef.class, false);
			ResourceCacheFacade.loadAllOf(PickupActorDef.class, false);
			ResourceCacheFacade.loadAllOf(LevelDef.class, false);
		} catch (UndefinedResourceTypeException e) {
			Gdx.app.error("LevelEditor", e.toString());
		}
	}

	@Override
	public void unloadResources() {
		ResourceCacheFacade.unload(ResourceNames.EDITOR_BUTTONS);

		try {
			ResourceCacheFacade.unloadAllOf(EnemyActorDef.class, false);
			ResourceCacheFacade.unloadAllOf(PickupActorDef.class, false);
			ResourceCacheFacade.unloadAllOf(LevelDef.class, false);
		} catch (UndefinedResourceTypeException e) {
			Gdx.app.error("LevelEditor", e.toString());
		}
	}

	@Override
	public void onActivate(Outcomes outcome, String message) {
		Actor.setEditorActive(true);
		Actor.setWorld(mWorld);

		// Check so that all resources have been loaded
		if (outcome == Outcomes.LOADING_SUCCEEDED) {
			if (!mGui.isInitialized()) {
				mGui.initGui();
				mGui.resetValues();
			}

			// Loading a level
			if (mLoadingLevel != null) {
				Level loadedLevel;
				try {
					loadedLevel = ResourceCacheFacade.get(mLoadingLevel.getLevelId(), Level.class);
					setLevel(loadedLevel);
				} catch (UndefinedResourceTypeException e) {
					Gdx.app.error("LevelEditor", e.toString());
				}
			}
		}
		else if (outcome == Outcomes.LOADING_FAILED_CORRUPT_FILE) {
			// TODO
			mLoadingLevel = null;
		}
		else if (outcome == Outcomes.LOADING_FAILED_MISSING_FILE) {
			// TODO
			mLoadingLevel = null;
		}
		else if (outcome == Outcomes.LOADING_FAILED_UNDEFINED_TYPE) {
			// TODO
			mLoadingLevel = null;
		}
		else if (outcome == Outcomes.DEF_SELECTED) {
			switch (mSelectionAction) {
			case LEVEL:
				try {
					mLoadingLevel = ResourceCacheFacade.get(UUID.fromString(message), LevelDef.class);

					// Only load level if it's not the current level we selected
					if (!mLoadingLevel.equals(mLevel.getDef())) {
						ResourceCacheFacade.load(mLoadingLevel.getLevelId(), Level.class, false);
						Scene scene = getLoadingScene();
						if (scene != null) {
							SceneSwitcher.switchTo(scene);
						} else {
							/** @todo remove after we have a loading scene */
							ResourceCacheFacade.finishLoading();
							Level loadedLevel = ResourceCacheFacade.get(mLoadingLevel.getLevelId(), Level.class);
							setLevel(loadedLevel);
						}
					} else {
						mLoadingLevel = null;
					}
				} catch (Exception e) {
					Gdx.app.error("LevelEditor", e.toString());
				}
				break;

			case PICKUP:
				try {
					PickupActorDef pickupActorDef = ResourceCacheFacade.get(UUID.fromString(message), PickupActorDef.class);

					// Load dependencies
					ResourceCacheFacade.load(pickupActorDef, true);
					ResourceCacheFacade.finishLoading();

					// Update def
					PickupActorDef oldPickupActorDef = (PickupActorDef) ((ActorTool)mTouchTools[Tools.PICKUP.ordinal()]).getNewActorDef();
					((ActorTool)mTouchTools[Tools.PICKUP.ordinal()]).setNewActorDef(pickupActorDef);

					// Unload old dependencies
					ResourceCacheFacade.unload(oldPickupActorDef, true);
				} catch (Exception e) {
					Gdx.app.error("LevelEditor", e.toString());
				}
				break;

			case ENEMY:
				try {
					EnemyActorDef enemyActorDef = ResourceCacheFacade.get(UUID.fromString(message), EnemyActorDef.class);

					// Load dependencies
					ResourceCacheFacade.load(enemyActorDef, true);
					ResourceCacheFacade.finishLoading();

					// Update def
					EnemyActorDef oldEnemyActorDef = (EnemyActorDef) ((ActorTool)mTouchTools[Tools.PICKUP.ordinal()]).getNewActorDef();
					((ActorTool)mTouchTools[Tools.ENEMY.ordinal()]).setNewActorDef(enemyActorDef);

					// Unload old dependencies
					ResourceCacheFacade.unload(oldEnemyActorDef, true);
				} catch (Exception e) {
					Gdx.app.error("LevelEditor", e.toString());
				}
				break;
			}
		}
	}

	@Override
	public void onDisposed() {
		// Unload old dependencies for tools
		for (TouchTool touchTool : mTouchTools) {
			if (touchTool instanceof ActorTool) {
				ActorDef actorDef = ((ActorTool) touchTool).getNewActorDef();
				if (actorDef != null) {
					ResourceCacheFacade.unload(actorDef, true);
				}
			}
		}


		setLevel(null);
	}

	@Override
	public boolean touchDown(int x, int y, int pointer, int button) {
		// Scroll -> When press middle mouse or two fingers
		if (button == 2 || (Gdx.app.getInput().isTouched(0) && Gdx.app.getInput().isTouched(1))) {
			mScroller.touchDown(x, y);
			mScrollCameraOrigin.set(mCamera.position.x, mCamera.position.y);
			return true;
		} else if (mScroller.isScrolling()) {
			return true;
		}

		return false;
	}

	@Override
	public boolean touchDragged(int x, int y, int pointer) {
		// Scrolling, move the map
		if (mScroller.isScrolling() && pointer == 0) {
			mScroller.touchDragged(x, y);
			return true;
		}
		return false;
	}

	@Override
	public boolean touchUp(int x, int y, int pointer, int button) {

		// Not scrolling any more
		if (mScroller.isScrolling() && (button == 2 || !Gdx.app.getInput().isTouched(0) || !Gdx.app.getInput().isTouched(1))) {
			mScroller.touchUp(x, y);
			return true;
		}

		return false;
	}

	@Override
	public void onActorAdded(Actor actor) {
		mLevel.addActor(actor);
		mUnsaved = true;
	}

	@Override
	public void onActorRemoved(Actor actor) {
		mLevel.removeActor(actor.getId());
		mUnsaved = true;
	}

	@Override
	public void onActorChanged(Actor actor) {
		mUnsaved = true;
	}

	@Override
	public void onActorSelected(Actor actor) {
		if (actor instanceof EnemyActor) {
			((LevelEditorGui)mGui).showEnemyOptions();

			EnemyActor selectedEnemy = (EnemyActor) ((AddEnemyTool)mTouchTools[Tools.ENEMY.ordinal()]).getSelectedActor();
			EnemyGroup enemyGroup = selectedEnemy.getEnemyGroup();

			if (enemyGroup != null) {
				((LevelEditorGui)mGui).setEnemyOptions(enemyGroup.getEnemyCount(), enemyGroup.getSpawnTriggerDelay());
			} else {
				((LevelEditorGui)mGui).setEnemyOptions(1, -1);
			}

		} else {
			((LevelEditorGui)mGui).hideEnemyOptions();
		}
	}

	/**
	 * @return true if an enemy is currently selected
	 */
	boolean isEnemySelected() {
		if (mToolType == Tools.ENEMY) {
			return ((AddActorTool)mTouchTools[Tools.ENEMY.ordinal()]).getSelectedActor() != null;
		}

		return false;
	}

	/**
	 * Only for PC users...
	 */
	@Override
	public boolean keyDown(int keycode) {
		// Redo - Ctrl + Shift + Z || Ctrl + Y
		if ((keycode == Keys.Z && (Gdx.input.isKeyPressed(Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Keys.CONTROL_RIGHT)) && (Gdx.input.isKeyPressed(Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Keys.SHIFT_RIGHT))) ||
				(keycode == Keys.Y && (Gdx.input.isKeyPressed(Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Keys.CONTROL_RIGHT)))) {
			mInvoker.redo();
			return true;
		}

		// Undo - Ctrl + Z
		if (keycode == Keys.Z && (Gdx.input.isKeyPressed(Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Keys.CONTROL_RIGHT))) {
			mInvoker.undo();
			return true;
		}

		return false;
	}

	/**
	 * Switches the tool to the selected tool
	 * @param tool the new tool type
	 */
	void switchTool(Tools tool) {
		// Remove old tool
		if (mToolType != null) {
			mInputMultiplexer.removeProcessor(mTouchTools[mToolType.ordinal()]);
			mTouchTools[mToolType.ordinal()].deactivate();
		}

		// Set current tool
		mToolType = tool;

		// add new tool
		if (mToolType != null) {
			mInputMultiplexer.addProcessor(mTouchTools[mToolType.ordinal()]);
			mTouchTools[mToolType.ordinal()].activate();
		}
	}

	/**
	 * Tests to run a game from the current location
	 */
	void runFromHere() {
		GameScene testGame = new GameScene(true);
		Level copyLevel = mLevel.copy();
		copyLevel.setXCoord(mCamera.position.x - mCamera.viewportWidth * 0.5f);
		testGame.setLevel(copyLevel);

		SceneSwitcher.switchTo(testGame);
	}

	@Override
	public void saveDef() {
		ResourceSaver.save(mLevel.getDef());
		ResourceSaver.save(mLevel);

		// Load the saved actor and use it instead
		if (!ResourceCacheFacade.isLoaded(mLevel.getDef().getId(), LevelDef.class)) {
			try {
				ResourceCacheFacade.load(mLevel.getDef().getId(), LevelDef.class, false);
				ResourceCacheFacade.finishLoading();
				//
				//				Level level = ResourceCacheFacade.get(mLevel.getId(), Level.class);
				//				mLevel.dispose();
				//				mLevel = level;
				//
				//				createActorBodies();
			} catch (Exception e) {
				Gdx.app.error("LevelEditor", "Loading of saved level failed! " + e.toString());
			}
		}

		mUnsaved = false;
	}

	@Override
	public void loadDef() {
		mSelectionAction = SelectionActions.LEVEL;

		Scene scene = new SelectDefScene(LevelDef.class, true, true);
		SceneSwitcher.switchTo(scene);
	}

	@Override
	public void newDef() {
		mLevel.dispose();
		LevelDef levelDef = new LevelDef();
		Level level = new Level(levelDef);
		setLevel(level);

		clearTools();

		mUnsaved = false;
	}

	@Override
	public void duplicateDef() {
		Level level = mLevel.copy();

		setLevel(level);

		mGui.resetValues();
		mUnsaved = true;
		mInvoker.dispose();
	}

	@Override
	public boolean isUnsaved() {
		return mUnsaved;
	}

	/**
	 * Sets the number of enemies in one group
	 * @param cEnemies number of enemies in the group
	 */
	void setEnemyCount(int cEnemies) {
		EnemyActor selectedEnemy = (EnemyActor) ((AddEnemyTool)mTouchTools[Tools.ENEMY.ordinal()]).getSelectedActor();

		if (selectedEnemy != null) {
			EnemyGroup enemyGroup = selectedEnemy.getEnemyGroup();

			// We have an enemy group
			if (enemyGroup != null) {
				// Just change amount of enemies
				if (cEnemies > 1) {
					ArrayList<EnemyActor> addedEnemies = new ArrayList<EnemyActor>();
					ArrayList<EnemyActor> removedEnemies = new ArrayList<EnemyActor>();

					enemyGroup.setEnemyCount(cEnemies, addedEnemies, removedEnemies);

					for (EnemyActor addedEnemy : addedEnemies) {
						mLevel.addActor(addedEnemy);
					}

					for (EnemyActor removedEnemy : removedEnemies) {
						mLevel.removeActor(removedEnemy.getId());
					}
				}
				// Delete enemy group
				else {
					// Remove all excess enemies
					ArrayList<EnemyActor> removedEnemies = enemyGroup.clear();

					for (EnemyActor enemyActor : removedEnemies) {
						mLevel.removeActor(enemyActor.getId());
					}

					mLevel.removeEnemyGroup(enemyGroup.getId());
				}

			}
			// No enemy group, do we create one?
			else if (cEnemies > 1) {
				enemyGroup = new EnemyGroup();
				mLevel.addEnemyGroup(enemyGroup);

				enemyGroup.setOriginalEnemy(selectedEnemy);

				ArrayList<EnemyActor> addedEnemies = new ArrayList<EnemyActor>();
				enemyGroup.setEnemyCount(cEnemies, addedEnemies, null);

				for (EnemyActor addedEnemy : addedEnemies) {
					mLevel.addActor(addedEnemy);
				}
			}
		}
	}

	/**
	 * Sets the spawn delay between actors in the same group.
	 * @param delay seconds of delay between actors are activated.
	 */
	void setEnemySpawnDelay(float delay) {
		EnemyActor selectedEnemy = (EnemyActor) ((AddEnemyTool)mTouchTools[Tools.ENEMY.ordinal()]).getSelectedActor();

		if (selectedEnemy != null) {
			EnemyGroup enemyGroup = selectedEnemy.getEnemyGroup();

			// We have an enemy group
			if (enemyGroup != null) {
				enemyGroup.setSpawnTriggerDelay(delay);
			}
		}
	}

	/**
	 * Sets the current active pickup tool
	 * @param state current active pickup tool
	 */
	void setPickupState(AddActorTool.States state) {
		AddActorTool addActorTool = (AddActorTool) mTouchTools[Tools.PICKUP.ordinal()];
		addActorTool.setState(state);
	}

	/**
	 * @return current active pickup tool state
	 */
	AddActorTool.States getPickupState() {
		return ((AddActorTool)mTouchTools[Tools.PICKUP.ordinal()]).getState();
	}

	/**
	 * Sets the current active static terrain tool
	 * @param state current active static terrain tool
	 */
	void setStaticTerrainState(DrawActorTool.States state) {
		DrawActorTool drawActorTool = (DrawActorTool) mTouchTools[Tools.STATIC_TERRAIN.ordinal()];
		drawActorTool.setState(state);
	}

	/**
	 * @return current active static terrain tool state
	 */
	DrawActorTool.States getStaticTerrainState() {
		return ((DrawActorTool)mTouchTools[Tools.STATIC_TERRAIN.ordinal()]).getState();
	}

	/**
	 * Sets the active enemy tool state
	 * @param state current active enemy tool state
	 */
	void setEnemyState(AddActorTool.States state) {
		AddEnemyTool addEnemyTool = (AddEnemyTool) mTouchTools[Tools.ENEMY.ordinal()];
		addEnemyTool.setState(state);
	}

	/**
	 * @return current active enemy tool state
	 */
	AddActorTool.States getEnemyState() {
		return ((AddActorTool)mTouchTools[Tools.ENEMY.ordinal()]).getState();
	}

	/**
	 * Select pickup
	 */
	void selectPickup() {
		mSelectionAction = SelectionActions.PICKUP;

		Scene scene = new SelectDefScene(PickupActorDef.class, false, false);
		SceneSwitcher.switchTo(scene);
	}

	/**
	 * Select enemy
	 */
	void selectEnemy() {
		mSelectionAction = SelectionActions.ENEMY;

		Scene scene = new SelectDefScene(EnemyActorDef.class, false, true);
		SceneSwitcher.switchTo(scene);
	}

	/**
	 * @return the current active tool of the level editor
	 */
	Tools getSelectedTool() {
		return mToolType;
	}

	/**
	 * @return currently selected pickup
	 */
	ActorDef getSelectedPickup() {
		return ((ActorTool) mTouchTools[Tools.PICKUP.ordinal()]).getNewActorDef();
	}

	/**
	 * All the main tool buttons
	 */
	enum Tools {
		/** Add enemies */
		ENEMY,
		/** Add/Move/Remove pickups */
		PICKUP,
		/** Draws the terrain */
		STATIC_TERRAIN,
		/** Draw paths */
		PATH
	}

	/**
	 * Creates actor bodies for the current level
	 */
	private void createActorBodies() {
		if (mLevel != null) {
			if (ResourceCacheFacade.isLoaded(mLevel.getId(), Level.class)) {
				ArrayList<Actor> actors = mLevel.getActors();

				for (Actor actor : actors) {
					// Don't create bodies for enemy actors in a group and where
					// they aren't the leader.
					boolean createBody = true;
					if (actor instanceof EnemyActor) {
						if (((EnemyActor) actor).getEnemyGroup() != null && !((EnemyActor)actor).isGroupLeader()) {
							createBody = false;
						}
					}

					if (createBody) {
						actor.createBody();
					}
				}
			}
		}
	}

	/**
	 * Clears all the tools
	 */
	private void clearTools() {
		for (TouchTool touchTool : mTouchTools) {
			if (touchTool != null) {
				touchTool.clear();
			}
		}
	}

	/**
	 * All definition selection actions
	 */
	private enum SelectionActions {
		/** Select an enemy */
		ENEMY,
		/** Loads a level */
		LEVEL,
		/** Selects a pickup */
		PICKUP,
	}

	/** Level we're currently editing */
	private Level mLevel = null;
	/** Invoker for the level editor */
	private Invoker mInvoker = new Invoker();
	/** Scrolling for nice scrolling */
	private Scroller mScroller;
	/** Starting position for the camera scroll */
	private Vector2 mScrollCameraOrigin = new Vector2();
	/** Which definition we're currently selecting */
	private SelectionActions mSelectionAction = null;
	/** Currently loading level */
	private LevelDef mLoadingLevel = null;
	/** Current selected tool */
	private Tools mToolType = Tools.STATIC_TERRAIN;
	/** All the available tools */
	private TouchTool[] mTouchTools = new TouchTool[Tools.values().length];
	/** Is unsaved since last edit */
	private boolean mUnsaved = false;
}
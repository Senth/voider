package com.spiddekauga.voider.editor;

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
import com.spiddekauga.voider.game.actors.EnemyActorDef;
import com.spiddekauga.voider.game.actors.PickupActor;
import com.spiddekauga.voider.game.actors.PickupActorDef;
import com.spiddekauga.voider.game.actors.StaticTerrainActor;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.resources.ResourceNames;
import com.spiddekauga.voider.resources.ResourceSaver;
import com.spiddekauga.voider.resources.UndefinedResourceTypeException;
import com.spiddekauga.voider.scene.ActorTool;
import com.spiddekauga.voider.scene.AddActorTool;
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
		DrawActorTool drawActorTool = new DrawActorTool(mCamera, mWorld, StaticTerrainActor.class, mInvoker, this);
		mTouchTools[Tools.STATIC_TERRAIN.ordinal()] = drawActorTool;
		AddActorTool addActorTool = new AddActorTool(mCamera, mWorld, PickupActor.class, mInvoker, this);
		mTouchTools[Tools.PICKUP.ordinal()] = addActorTool;

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
	 * Sets the level that shall be played
	 * @param level level to play
	 */
	public void setLevel(Level level) {
		/** @todo unload level */

		mLevel = level;
		mInvoker.dispose();
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
			ResourceCacheFacade.loadAllOf(EnemyActorDef.class, true);
			ResourceCacheFacade.loadAllOf(PickupActorDef.class, true);
		} catch (UndefinedResourceTypeException e) {
			Gdx.app.error("LevelEditor", e.toString());
		}
	}

	@Override
	public void unloadResources() {
		ResourceCacheFacade.unload(ResourceNames.EDITOR_BUTTONS);

		try {
			ResourceCacheFacade.unloadAllOf(EnemyActorDef.class, true);
			ResourceCacheFacade.unloadAllOf(PickupActorDef.class, true);
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
			case LEVEL_LOAD:
				try {
					mLoadingLevel = ResourceCacheFacade.get(UUID.fromString(message), LevelDef.class);

					// Load the actual level
					ResourceCacheFacade.load(mLoadingLevel.getLevelId(), Level.class, false);
					Scene scene = getLoadingScene();
					SceneSwitcher.switchTo(scene);
				} catch (Exception e) {
					Gdx.app.error("LevelEditor", e.toString());
				}
				break;

			case PICKUP:
				try {
					PickupActorDef pickupActorDef = ResourceCacheFacade.get(UUID.fromString(message), PickupActorDef.class);
					((ActorTool)mTouchTools[Tools.PICKUP.ordinal()]).setNewActorDef(pickupActorDef);
				} catch (Exception e) {
					Gdx.app.error("LevelEditor", e.toString());
				}
				break;
			}
		}
	}

	@Override
	public boolean touchDown(int x, int y, int pointer, int button) {
		// Scroll -> When press middle mouse or two fingers
		if (button == 2 || (Gdx.app.getInput().isTouched(0) && Gdx.app.getInput().isTouched(1))) {
			mScroller.touchDown(x, y);
			mScrollCameraOrigin.set(mCamera.position.x, mCamera.position.y);
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
	}

	@Override
	public void onActorRemoved(Actor actor) {
		mLevel.removeActor(actor.getId());
	}

	@Override
	public void onActorChanged(Actor actor) {
		mUnsaved = true;
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
		switch (mToolType) {
		case PICKUP:
			mInputMultiplexer.removeProcessor(mTouchTools[Tools.PICKUP.ordinal()]);
			break;

		case STATIC_TERRAIN:
			mInputMultiplexer.removeProcessor(mTouchTools[Tools.STATIC_TERRAIN.ordinal()]);
			((DrawActorTool)mTouchTools[Tools.STATIC_TERRAIN.ordinal()]).deactivate();
			break;

		default:
			break;
		}

		// Set current tool
		mToolType = tool;

		// add new tool
		switch (mToolType) {
		case PICKUP:
			mInputMultiplexer.addProcessor(mTouchTools[Tools.PICKUP.ordinal()]);
			break;

		case STATIC_TERRAIN:
			mInputMultiplexer.addProcessor(mTouchTools[Tools.STATIC_TERRAIN.ordinal()]);
			((DrawActorTool)mTouchTools[Tools.STATIC_TERRAIN.ordinal()]).activate();
			break;

		default:
			Gdx.app.error("LevelEditor", "Switched to an unknown tool!");
			break;
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
		mUnsaved = false;
	}

	@Override
	public void loadDef() {
		mSelectionAction = SelectionActions.LEVEL_LOAD;

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
	 * Sets the current active pickup tool
	 * @param state current active pickup tool
	 */
	void setPickupState(AddActorTool.States state) {
		AddActorTool addActorTool = (AddActorTool) mTouchTools[Tools.PICKUP.ordinal()];
		addActorTool.setState(state);
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
	 * @return current active static terrain tool
	 */
	DrawActorTool.States getStaticTerrainState() {
		return ((DrawActorTool)mTouchTools[Tools.STATIC_TERRAIN.ordinal()]).getState();
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
	 * @return currently selected pickup
	 */
	ActorDef getSelectedPickup() {
		return ((ActorTool) mTouchTools[Tools.PICKUP.ordinal()]).getNewActorDef();
	}

	/**
	 * All the main tool buttons
	 */
	enum Tools {
		/** Add/Move/Remove pickups */
		PICKUP,
		/** Draws the terrain */
		STATIC_TERRAIN;
	}

	/**
	 * Clears all the tools
	 */
	private void clearTools() {
		for (TouchTool touchTool : mTouchTools) {
			touchTool.clear();
		}
	}

	/**
	 * All definition selection actions
	 */
	private enum SelectionActions {
		/** Selects a pickup */
		PICKUP,
		/** Loads a level */
		LEVEL_LOAD,
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
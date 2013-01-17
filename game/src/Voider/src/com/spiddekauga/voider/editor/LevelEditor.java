package com.spiddekauga.voider.editor;

import java.util.LinkedList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.utils.Pools;
import com.spiddekauga.utils.GameTime;
import com.spiddekauga.utils.Scroller;
import com.spiddekauga.utils.Scroller.ScrollAxis;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.editor.commands.ClActorAdd;
import com.spiddekauga.voider.editor.commands.ClActorMove;
import com.spiddekauga.voider.editor.commands.ClActorRemove;
import com.spiddekauga.voider.editor.commands.ClActorSelect;
import com.spiddekauga.voider.editor.commands.ClTerrainActorAddCorner;
import com.spiddekauga.voider.editor.commands.ClTerrainActorMoveCorner;
import com.spiddekauga.voider.editor.commands.ClTerrainActorRemoveCorner;
import com.spiddekauga.voider.editor.commands.LevelCommand;
import com.spiddekauga.voider.game.Actor;
import com.spiddekauga.voider.game.GameScene;
import com.spiddekauga.voider.game.Level;
import com.spiddekauga.voider.game.LevelDef;
import com.spiddekauga.voider.game.actors.StaticTerrainActor;
import com.spiddekauga.voider.game.actors.StaticTerrainActor.PolygonComplexException;
import com.spiddekauga.voider.game.actors.StaticTerrainActor.PolygonCornerTooCloseException;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.resources.ResourceNames;
import com.spiddekauga.voider.resources.ResourceSaver;
import com.spiddekauga.voider.resources.UndefinedResourceTypeException;
import com.spiddekauga.voider.scene.LoadingScene;
import com.spiddekauga.voider.scene.Scene;
import com.spiddekauga.voider.scene.SceneSwitcher;

/**
 * The level editor scene
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 * 
 * @TODO unload level
 */
public class LevelEditor extends Scene implements EventListener {
	/**
	 * Constructor for the level editor
	 */
	public LevelEditor() {
		mWorld = new World(new Vector2(), true);
		fixCamera();
		Actor.setWorld(mWorld);
		Actor.setEditorActive(true);

		mScroller = new Scroller(50, 2000, 10, 200, ScrollAxis.X);
	}

	@Override
	public void update() {
		mWorld.step(1/60f, 6, 2);
		mLevel.update(false);

		// Scrolling
		if (mScroller.isScrolling()) {
			mScroller.update(Gdx.graphics.getDeltaTime());

			// Update the camera
			Vector2 scrollCameraOrigin = Pools.obtain(Vector2.class);
			screenToWorldCoord(mScroller.getOriginScroll(), scrollCameraOrigin, false);
			Vector2 scrollCameraCurrent = Pools.obtain(Vector2.class);
			screenToWorldCoord(mScroller.getCurrentScroll(), scrollCameraCurrent, false);

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
		if (Config.Graphics.USE_DEBUG_RENDERER) {
			mDebugRenderer.render(mWorld, mCamera.combined);
			super.render();
		} else {
			mLevel.render(mSpriteBatch);
			mLevel.renderEditor(mSpriteBatch);
			super.render();
		}
	}

	/**
	 * Sets the level that shall be played
	 * @param level level to play
	 */
	public void setLevel(Level level) {
		mLevel = level;
		mLevelInvoker.setLevel(level, this);
	}

	/**
	 * Sets the currently selected actor
	 * @param actor the new selected actor
	 */
	public void setSelectedActor(Actor actor) {
		mSelectedActor = actor;
		mToolCurrent.setActor();
		mChangedActorSinceUp = true;
	}

	/**
	 * @return the currently selected actor
	 */
	public Actor getSelectedActor() {
		return mSelectedActor;
	}

	@Override
	public void onResize(int width, int height) {
		fixCamera();
		mUi.setViewport(width, height, true);
		if (mGuiInitialized) {
			scaleGui();
		}
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
	}

	@Override
	public void unloadResources() {
		ResourceCacheFacade.unload(ResourceNames.EDITOR_BUTTONS);
	}

	@Override
	public void onActivate(Outcomes outcome, String message) {
		// Check so that all resources have been loaded
		if (outcome == Outcomes.LOADING_SUCCEEDED) {
			if (!mGuiInitialized) {
				initGui();
				mGuiInitialized = true;
			} else {
				scaleGui();
			}

			if (mToolCurrent == null) {
				mToolCurrent = mNoneHandler;
			}
		}
		// Changed back to level editor from testing game, reset world etc
		else if (outcome == Outcomes.LEVEL_QUIT) {
			Actor.setEditorActive(true);
			Actor.setWorld(mWorld);
		}
	}

	// --------------------------------
	//		INPUT EVENTS (not gui)
	// --------------------------------
	@Override
	public boolean touchDown(int x, int y, int pointer, int button) {
		// Special case, scrolling the map. This is the case if pressed
		// middle mouse button, or two fingers are on the screen
		if (button == 2 || (Gdx.app.getInput().isTouched(0) && Gdx.app.getInput().isTouched(1))) {
			mScroller.touchDown(x, y);
			mScrollCameraOrigin.set(mCamera.position.x, mCamera.position.y);
			return true;
		}

		if (mClickTimeLast + Config.Input.DOUBLE_CLICK_TIME > GameTime.getTotalTimeElapsed()) {
			mClickTimeLast = 0f;
			mDoubleClick = true;
		} else {
			mDoubleClick = false;
			mClickTimeLast = GameTime.getTotalTimeElapsed();
		}


		// Only do something for the first pointer
		if (pointer == 0) {
			screenToWorldCoord(x, y, mTouchOrigin, true);
			mTouchCurrent.set(mTouchOrigin);

			if (mToolCurrent != null) {
				mToolCurrent.down();
			}

			return true;
		}

		// Disable GUI?

		return false;
	}

	@Override
	public boolean touchDragged(int x, int y, int pointer) {
		// Only do something for the first pointer
		if (!mScroller.isScrolling() && pointer == 0) {
			screenToWorldCoord(x, y, mTouchCurrent, true);

			/** @TODO check long click */

			mToolCurrent.dragged();

			return true;
		}
		// Scrolling, move the map
		else if (mScroller.isScrolling() && pointer == 0) {
			mScroller.touchDragged(x, y);
			return true;
		}
		return false;
	}

	@Override
	public boolean touchUp(int x, int y, int pointer, int button) {
		boolean handled = false;

		// Not scrolling any more
		if (mScroller.isScrolling() && (button == 2 || !Gdx.app.getInput().isTouched(0) || !Gdx.app.getInput().isTouched(1))) {
			mScroller.touchUp(x, y);
			handled = true;
		}

		// Only do something for the first pointer
		else if (pointer == 0) {
			screenToWorldCoord(x, y, mTouchCurrent, true);

			mToolCurrent.up();

			handled = true;
		}

		mChangedActorSinceUp = false;

		// Enable GUI?

		return handled;
	}

	/**
	 * Only for PC users...
	 */
	@Override
	public boolean keyDown(int keycode) {
		// Redo - Ctrl + Shift + Z || Ctrl + Y
		if ((keycode == Keys.Z && (Gdx.input.isKeyPressed(Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Keys.CONTROL_RIGHT)) && (Gdx.input.isKeyPressed(Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Keys.SHIFT_RIGHT))) ||
				(keycode == Keys.Y && (Gdx.input.isKeyPressed(Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Keys.CONTROL_RIGHT)))) {
			if (mLevelInvoker.canRedo()) {
				mLevelInvoker.redo();
			}
			return true;
		}

		// Undo - Ctrl + Z
		if (keycode == Keys.Z && (Gdx.input.isKeyPressed(Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Keys.CONTROL_RIGHT))) {
			if (mLevelInvoker.canUndo()) {
				mLevelInvoker.undo();
			}
			return true;
		}

		return false;
	}

	/**
	 * Runs a test for picking
	 * @param callback the callback function to use
	 */
	public void testPick(QueryCallback callback) {
		mHitBodies.clear();
		mHitBody = null;
		mWorld.QueryAABB(callback, mTouchCurrent.x - 0.0001f, mTouchCurrent.y - 0.0001f, mTouchCurrent.x + 0.0001f, mTouchCurrent.y + 0.0001f);
		mToolCurrent.filterPicks();
	}

	/**
	 * Tests to run a game from the current location
	 */
	private void runFromHere() {
		GameScene testGame = new GameScene(true);
		Level copyLevel = mLevel.copy();
		copyLevel.setXCoord(mCamera.position.x - mCamera.viewportWidth * 0.5f);
		testGame.setLevel(copyLevel);

		SceneSwitcher.switchTo(testGame);
	}

	/**
	 * Fixes the camera resolution
	 */
	private void fixCamera() {
		float width = Gdx.graphics.getWidth() * Config.Graphics.WORLD_SCALE;
		// Decrease scale of width depending on height scaled
		float heightScale = Config.Graphics.HEIGHT / Gdx.graphics.getHeight();
		width *= heightScale;
		mCamera = new OrthographicCamera(width , Config.Graphics.HEIGHT * Config.Graphics.WORLD_SCALE);
	}

	/** Physics world */
	private World mWorld = null;
	/** Camera for the editor */
	private Camera mCamera = null;
	/** Debug renderer */
	private Box2DDebugRenderer mDebugRenderer = new Box2DDebugRenderer();
	/** Level we're currently editing */
	private Level mLevel = null;
	/** Level invoker, sends all editing commands through this */
	private LevelInvoker mLevelInvoker = new LevelInvoker();
	/** Currently selected actor */
	private Actor mSelectedActor = null;

	// GUI
	/** Table the tool buttons */
	private Table mToolTable = null;
	/** Table for all gui */
	private Table mGui = null;
	/** If the GUI has been initialized */
	private boolean mGuiInitialized = false;


	// Event stuff
	/** Scrolling for nice scrolling */
	private Scroller mScroller;
	/** True if double clicked */
	private boolean mDoubleClick = false;
	/** Last click time */
	private float mClickTimeLast = 0f;
	/** Changed actor since last up */
	private boolean mChangedActorSinceUp = false;


	// Temporary variables for touch detection
	/** Original point when pressing */
	private Vector2 mTouchOrigin = new Vector2();
	/** Current point when pressing */
	private Vector2 mTouchCurrent = new Vector2();
	/** Starting position for the scroll */
	private Vector2 mScrollOrigin = new Vector2();
	/** Starting position for the camera scroll */
	private Vector2 mScrollCameraOrigin = new Vector2();
	/** For ray testing on player ship when touching it */
	private Vector3 mTestPoint = new Vector3();
	/** Body that was hit (and prioritized) */
	private Body mHitBody = null;
	/** Bodies that were hit and selected for filtering, before mHitBody is set */
	private LinkedList<Body> mHitBodies = new LinkedList<Body>();

	// -------------------------------------
	//				TOOLS
	// -------------------------------------
	/**
	 * All the main tool buttons
	 */
	private enum Tools {
		/** Terrain tool @TODO change style */
		STATIC_TERRAIN("toggle"),
		/** Tests to run the map from the current location @TODO change style */
		RUN("toggle"),

		/** No tool selected */
		NONE("");

		/**
		 * @return style of the tool
		 */
		public String getStyleName() {
			return mStyleName;
		}

		/**
		 * Constructor for terrain tool, binds the tool with a style
		 * @param style the style bound to the tool
		 */
		private Tools(String style) {
			mStyleName = style;
		}

		/** Name of the style the tool shall use */
		private String mStyleName;
	}

	/**
	 * Static terrain tools
	 */
	private enum StaticTerrainTools {
		/** Add/Create corners, can move corners too */
		ADD("add"),
		/** Move terrain @TODO change style*/
		MOVE_TERRAIN("toggle"),
		/** Remove corners and terrain @TODO change style */
		REMOVE("toggle"),

		/** No tool selected */
		NONE("");

		/**
		 * @return style of the tool
		 */
		public String getStyleName() {
			return mStyleName;
		}

		/**
		 * Constructor for terrain tool, binds the tool with a style
		 * @param style the style bound to the tool
		 */
		private StaticTerrainTools(String style) {
			mStyleName = style;
		}

		/** Name of the style the tool shall use */
		private String mStyleName;
	}

	// -------------------------------------
	//			GUI HANDLING
	// -------------------------------------
	@Override
	public boolean handle(Event event) {
		if (!(event instanceof ChangeEvent) || !(event.getTarget() instanceof Button)) {
			return false;
		}

		Button button = (Button)event.getTarget();

		String actionName = event.getTarget().getName();

		// Toggle buttons
		if (button.isChecked()) {
			// STATIC TERRAIN
			if (actionName.equals(Tools.STATIC_TERRAIN.toString())) {
				switchTool(mStaticTerrainHandler, Tools.STATIC_TERRAIN);
			}
		}

		// --- ACTIONS ---
		// RUN
		if (actionName.equals(Tools.RUN.toString())) {
			runFromHere();
		}


		/** @TODO remove */
		// SAVE
		else if (actionName.equals("save")) {
			ResourceSaver.save(mLevel.getDef());
			ResourceSaver.save(mLevel);
		}

		// LOAD - existing level (use first available
		else if (actionName.equals("load")) {
			// Load all level defs
			try {
				ResourceCacheFacade.loadAllOf(LevelDef.class, true);
				ResourceCacheFacade.finishLoading();

				List<LevelDef> levelDefs = ResourceCacheFacade.get(LevelDef.class);

				// Load first
				if (levelDefs.size() > 0) {
					ResourceCacheFacade.load(levelDefs.get(0).getLevelId(), Level.class, false);
					ResourceCacheFacade.finishLoading();
					Level loadedLevel = ResourceCacheFacade.get(levelDefs.get(0).getLevelId(), Level.class);
					if (loadedLevel != null) {
						mLevel.dispose();
						setLevel(loadedLevel);
					}
				}

			} catch (UndefinedResourceTypeException e) {
				e.printStackTrace();
			}
		}

		// NEW - add new level
		else if (actionName.equals("new")) {
			mLevel.dispose();
			LevelDef levelDef = new LevelDef();
			Level level = new Level(levelDef);
			setLevel(level);
		}

		// Select no tool
		else {
			switchTool(mNoneHandler, Tools.NONE);
		}

		return false;
	}

	/**
	 * Screen to world coordinate
	 * @param screenPos screen position
	 * @param worldCoordinate the vector to set the world coordinate for
	 * @param clamp if the x and y coordinates should be clamped
	 */
	private void screenToWorldCoord(Vector2 screenPos, Vector2 worldCoordinate, boolean clamp) {
		screenToWorldCoord(screenPos.x, screenPos.y, worldCoordinate, clamp);
	}

	/**
	 * Screen to world coordinate
	 * @param x the X-coordinate of the screen
	 * @param y the Y-coordinate of the screen
	 * @param worldCoordinate the vector to set the world coordinate for
	 * @param clamp if the x and y coordinates should be clamped
	 */
	private void screenToWorldCoord(float x, float y, Vector2 worldCoordinate, boolean clamp) {
		if (clamp) {
			mTestPoint.set(clampX(x), clampY(y), 0);
		} else {
			mTestPoint.set(x, y, 0);
		}
		mCamera.unproject(mTestPoint);
		worldCoordinate.x = mTestPoint.x;
		worldCoordinate.y = mTestPoint.y;
	}

	/**
	 * Switches the tool to the selected tool
	 * @param selectedTool the new main tool
	 * @param tool the new tool type
	 */
	private void switchTool(Tool selectedTool, Tools tool) {
		// Deselect cucrent tool
		com.badlogic.gdx.scenes.scene2d.Actor oldActor = mToolTable.findActor(mTool.toString());
		if (oldActor != null && oldActor instanceof Button) {
			Button oldButton = (Button)oldActor;
			oldButton.setChecked(false);
		}

		// Set current tool
		mTool = tool;
		mToolCurrent = selectedTool;


		// Clear GUI table and readd the correct GUI
		mGui.clear();
		Table toolGui = mToolCurrent.getGui();
		if (toolGui != null) {
			mGui.add(mToolCurrent.getGui());
		}
		mGui.add(mToolTable);
		mGui.invalidate();

		/** @TODO remove, scale appropriately the first time */
		scaleGui();

		if (toolGui != null) {
			toolGui.padBottom(mToolTable.getPrefHeight() - toolGui.getPrefHeight());
		}
	}


	/**
	 * Initializes all the buttons for the GUI
	 */
	private void initGui() {
		mGui = new Table();


		mGui.align(Align.top | Align.right);
		mUi.addActor(mGui);

		Skin editorSkin = ResourceCacheFacade.get(ResourceNames.EDITOR_BUTTONS);

		TextButtonStyle textToogleStyle = editorSkin.get("toggle", TextButtonStyle.class);
		TextButtonStyle textStyle = editorSkin.get("default", TextButtonStyle.class);
		ImageButtonStyle imageStyle = editorSkin.get(StaticTerrainTools.ADD.getStyleName(), ImageButtonStyle.class);

		mToolTable = new Table();
		mToolTable.setTransform(true);
		Button button = new TextButton("Static Terrain", textToogleStyle);
		button.addListener(this);
		button.setName(Tools.STATIC_TERRAIN.toString());
		mToolTable.add(button);
		mToolTable.row();

		button = new ImageButton(imageStyle);
		button.setTransform(true);
		button.setHeight(50);
		button.setWidth(50);
		button.invalidate();
		mToolTable.add(button);
		mToolTable.row();

		button = new TextButton("SAVE", textStyle);
		button.setName("save");
		button.addListener(this);
		mToolTable.add(button);
		mToolTable.row();

		button = new TextButton("LOAD", textStyle);
		button.setName("load");
		button.addListener(this);
		mToolTable.add(button);
		mToolTable.row();

		button = new TextButton("NEW", textStyle);
		button.setName("new");
		button.addListener(this);
		mToolTable.add(button);
		mToolTable.row();

		button = new TextButton("RUN", textStyle);
		button.setName(Tools.RUN.toString());
		button.addListener(this);
		mToolTable.add(button);

		mGui.add(mToolTable);
		mGui.setTransform(true);


		// Initialize all the tools' GUI
		mStaticTerrainHandler.initGui();


		scaleGui();
	}

	/**
	 * Scale GUI
	 */
	private void scaleGui() {
		float tableHeight = mGui.getPrefHeight();

		// Division by 0 check
		if (tableHeight == 0.0f || Gdx.graphics.getHeight() == 0.0f) {
			return;
		}

		float scale = Gdx.graphics.getHeight() / tableHeight;

		// Don't scale over 1?
		if (scale < 1.0f) {
			float negativeScale = 1 / scale;
			mGui.setHeight(Gdx.graphics.getHeight()*negativeScale);
			float screenWidth = Gdx.graphics.getWidth();
			mGui.setWidth(screenWidth*negativeScale);
			mGui.invalidate();
			mGui.setScale(scale);
		} else {
			mGui.setScale(1.0f);
			mGui.setWidth(Gdx.graphics.getWidth());
			mGui.setHeight(Gdx.graphics.getHeight());
			mGui.invalidate();
		}
	}

	// -------------------------------------
	//		EVENT HANDLING FOR TOOLS
	// -------------------------------------
	/** Event handler for the current tool */
	private Tool mToolCurrent = null;
	/** Current selected tool */
	private Tools mTool = Tools.NONE;

	/** Event handler for static terrain tool */
	private StaticTerrainHandler mStaticTerrainHandler = new StaticTerrainHandler();
	/** Event handler for when no tool is active */
	private NoneHandler mNoneHandler = new NoneHandler();

	/**
	 * Common interface for all event handlers
	 */
	private abstract class Tool extends ChangeListener {
		/**
		 * Handles touch down events
		 */
		public abstract void down();

		/**
		 * Handles touch dragged events
		 */
		public abstract void dragged();

		/**
		 * Handles touch up events
		 */
		public abstract void up();

		/**
		 * Filter picks
		 */
		public abstract void filterPicks();

		/**
		 * Sets the actor. This allows the class to filter which actors
		 * should be set and which should not be set...
		 */
		public abstract void setActor();

		/**
		 * Initializes the GUI for this tool
		 */
		public abstract void initGui();

		/**
		 * @return the table with the GUI for this tool
		 */
		public abstract Table getGui();
	}

	/**
	 * Handles all events when static terrain tool is active
	 * 
	 * @author Matteus Magnusson <senth.wallace@gmail.com>
	 */
	private class StaticTerrainHandler extends Tool {
		@Override
		public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
			// Buttons
			if (actor instanceof Button) {
				Button button = (Button)actor;

				// Checked means it was pressed down
				if (button.isChecked()) {

					// Deselect previous tool
					mGuiTable.findActor(StaticTerrainTools.ADD.toString());

					String actionName = actor.getName();

					// Find which tool was pressed
					for (StaticTerrainTools tool : StaticTerrainTools.values()) {
						if (actionName.equals(tool.toString())) {
							// Uncheck old tool
							com.badlogic.gdx.scenes.scene2d.Actor oldActor = mGuiTable.findActor(mTool.toString());
							if (oldActor != null && oldActor instanceof Button) {
								Button oldButton = (Button)oldActor;
								oldButton.setChecked(false);
							}

							// Select tool
							mTool = tool;
							break;
						}
					}
				}
				// Else it was unchecked, and thus select no tool
				else {
					mTool = StaticTerrainTools.NONE;
				}
			}
		}

		@Override
		public void down() {
			switch (mTool) {
			case ADD:
				// Double click inside current actor finishes/closes it
				if (mDoubleClick && mHitBody != null && mHitBody.getUserData() == mActor) {
					// Remove the last corner if we accidently added one when double clicking
					if (mCornerLastIndex != -1) {
						mLevelInvoker.undo();
					}
					mLevelInvoker.execute(new ClActorSelect(null, false));
					return;
				}


				// Test if we hit a body or corner
				testPick(mCallback);

				// If we didn't change actor, do something
				if (mHitBody != null) {
					// Hit the terrain body (no corner), create corner
					if (mHitBody.getUserData() == mActor) {
						if (!mChangedActorSinceUp) {
							createTempCorner();
						}
					}
					// Else - Hit a corner, start moving it
					else {
						mCornerCurrentIndex = mActor.getCornerIndex(mHitBody.getPosition());
						mDragOrigin.set(mHitBody.getPosition());
						mCornerCurrentAddedNow = false;
					}
				}
				// Else create a new corner
				else {
					// No actor, create terrain
					if (mActor == null || !mLevel.containsActor(mActor)) {
						Actor newActor = new StaticTerrainActor();
						newActor.setPosition(mTouchOrigin);
						mLevelInvoker.execute(new ClActorAdd(newActor));
						mLevelInvoker.execute(new ClActorSelect(newActor, true));
					}

					createTempCorner();
				}
				break;


			case MOVE_TERRAIN:
				testPick(mCallback);

				// If hit terrain (no corner), start dragging the terrain
				if (mHitBody != null && mHitBody.getUserData() instanceof StaticTerrainActor) {
					// Select the actor
					if (mActor != mHitBody.getUserData()) {
						mLevelInvoker.execute(new ClActorSelect(mActor, false));
					}
					mDragOrigin.set(mHitBody.getPosition());
				} else {
					if (mActor != null) {
						mLevelInvoker.execute(new ClActorSelect(null, false));
					}
				}

				break;


			case REMOVE:
				testPick(mCallback);

				// If we hit the actor's body twice (no corners) we delete the actor along with
				// all the corners. If we hit a corner that corner is deleted.
				if (mHitBody != null) {
					// Hit terrain body (no corner) and it's second time -> Delet actor
					if (mHitBody.getUserData() == mActor) {
						// Only do something if we didn't hit the actor the first time
						if (!mChangedActorSinceUp) {
							mLevelInvoker.execute(new ClActorRemove(mActor));
							mLevelInvoker.execute(new ClActorSelect(null, true));
						}
					}
					// Else hit corner, delete it
					else {
						mLevelInvoker.execute(new ClTerrainActorRemoveCorner(mActor, mHitBody.getPosition()));

						// Was it the last corner? Remove actor too then
						if (mActor.getCornerCount() == 0) {
							mLevelInvoker.execute(new ClActorRemove(mActor, true));
							mLevelInvoker.execute(new ClActorSelect(null, true));
						}
					}
				}

				break;
			}
		}

		@Override
		public void dragged() {
			switch (mTool) {
			case ADD:
				if (mCornerCurrentIndex != -1) {
					try {
						mActor.moveCorner(mCornerCurrentIndex, mTouchCurrent);
					} catch (Exception e) {
						// Does nothing
					}
				}
				break;


			case MOVE_TERRAIN:
				if (mActor != null) {
					// Get diff movement
					Vector2 newPosition = Pools.obtain(Vector2.class);
					newPosition.set(mTouchCurrent).sub(mTouchOrigin);

					// Add original position
					newPosition.add(mDragOrigin);
					mActor.setPosition(newPosition);

					Pools.free(newPosition);
				}
				break;


			case REMOVE:
				// Does nothing
				break;
			}
		}

		@Override
		public void up() {
			switch (mTool) {
			case ADD:
				// ACTIONS VIA COMMANDS INSTEAD
				if (mActor != null && mCornerCurrentIndex != -1) {
					// NEW CORNER
					if (mCornerCurrentAddedNow) {
						createCornerFromTemp();
					}
					// MOVE CORNER
					else {
						// Reset to original position
						Vector2 newPos = Pools.obtain(Vector2.class);
						newPos.set(mActor.getCorner(mCornerCurrentIndex));
						try {
							mActor.moveCorner(mCornerCurrentIndex, mDragOrigin);
							LevelCommand command = new ClTerrainActorMoveCorner(mActor, mCornerCurrentIndex, newPos);
							mLevelInvoker.execute(command);
						} catch (Exception e) {
							// Does nothing
						}
						Pools.free(newPos);
					}
				}

				mCornerLastIndex = mCornerCurrentIndex;
				mCornerCurrentIndex = -1;
				break;


			case MOVE_TERRAIN:
				if (mActor != null) {
					// Reset actor to original position
					mActor.setPosition(mDragOrigin);

					// Set the new position through a command
					// Get diff movement
					Vector2 newPosition = Pools.obtain(Vector2.class);
					newPosition.set(mTouchCurrent).sub(mTouchOrigin);

					// Add original position
					newPosition.add(mDragOrigin);

					mLevelInvoker.execute(new ClActorMove(mActor, newPosition, mChangedActorSinceUp));

					Pools.free(newPosition);
				}
				break;


			case REMOVE:
				// Does nothing
				break;
			}
		}

		/**
		 * This will filter so that corners of the active terrain will
		 * be selected first, then any click on terrain
		 */
		@Override
		public void filterPicks() {
			Actor newActor = mActor;
			StaticTerrainActor oldActor = mActor;
			for (Body body : mHitBodies) {
				// Only set hit terrain if no hit body has been set
				if (body.getUserData() instanceof StaticTerrainActor) {
					if (mHitBody == null) {
						mHitBody = body;
						newActor = (StaticTerrainActor) mHitBody.getUserData();
					}
				}
				// A corner - select it, and quick return
				else if (body.getUserData() instanceof HitWrapper) {
					mHitBody = body;
					return;
				}
			}

			if (newActor != oldActor) {
				mLevelInvoker.execute(new ClActorSelect(newActor, false));
			}
		}

		@Override
		public void setActor() {
			if (mSelectedActor == null || mSelectedActor instanceof StaticTerrainActor) {
				// Destroy corners of the old selected actor
				if (mActor != null) {
					mActor.destroyBodyCorners();
				}

				mActor = (StaticTerrainActor) mSelectedActor;

				// Create corners of the new selected actor
				if (mActor != null) {
					mActor.createBodyCorners();
				}
			}
		}

		@Override
		public void initGui() {
			mGuiTable.setName(Tools.STATIC_TERRAIN.toString() + "-table");

			Skin editorSkin = ResourceCacheFacade.get(ResourceNames.EDITOR_BUTTONS);
			TextButtonStyle textStyle = editorSkin.get(StaticTerrainTools.MOVE_TERRAIN.getStyleName(), TextButtonStyle.class);
			ImageButtonStyle imageStyle = editorSkin.get(StaticTerrainTools.ADD.getStyleName(), ImageButtonStyle.class);


			Button button = new ImageButton(imageStyle);
			button.setName(StaticTerrainTools.ADD.toString());
			button.addListener(this);
			mGuiTable.add(button);
			mGuiTable.row();

			button = new TextButton("Remove", textStyle);
			button.setName(StaticTerrainTools.REMOVE.toString());
			button.addListener(this);
			mGuiTable.add(button);
			mGuiTable.row();

			button = new TextButton("Move", textStyle);
			button.setName(StaticTerrainTools.MOVE_TERRAIN.toString());
			button.addListener(this);
			mGuiTable.add(button);
			mGuiTable.align(Align.top);
			mGuiTable.setTransform(true);
		}

		@Override
		public Table getGui() {
			return mGuiTable;
		}

		/**
		 * Creates a temporary corner (until touch up)
		 */
		private void createTempCorner() {
			try {
				mActor.addCorner(mTouchCurrent);
				mCornerCurrentIndex = mActor.getLastAddedCornerIndex();
				mDragOrigin.set(mTouchOrigin);
				mCornerCurrentAddedNow = true;
			} catch (PolygonComplexException e) {
				/** @TODO print some error message on screen, cannot add corner here */
			} catch (PolygonCornerTooCloseException e) {
				/** @TODO print error message on screen */
			}
		}

		/**
		 * Tries to create a new corner. Will print out
		 * an error message if it didn't work
		 */
		private void createCornerFromTemp() {
			// Get current position of the corner
			Vector2 cornerPos = mActor.getCorner(mCornerCurrentIndex);
			mActor.removeCorner(mCornerCurrentIndex);

			// Set as chained if no corner exist in the terrain
			boolean chained = mActor.getCornerCount() == 0;

			ClTerrainActorAddCorner command = new ClTerrainActorAddCorner(mActor, cornerPos, chained);
			boolean added = mLevelInvoker.execute(command);
			if (!added) {
				/** @TODO print some error message on screen, cannot add corner here */
			}
		}

		/** Origin of the corner, before dragging it */
		private Vector2 mDragOrigin = new Vector2();
		/** Index of the current corner */
		private int mCornerCurrentIndex = -1;
		/** Last corner index */
		private int mCornerLastIndex = -1;
		/** True if the current corner was added now */
		private boolean mCornerCurrentAddedNow = false;
		/** Current Static terrain actor */
		private StaticTerrainActor mActor = null;
		/** The current active tool for the static terrain tool */
		private StaticTerrainTools mTool = StaticTerrainTools.NONE;
		/** GUI buttons for the terrain */
		private Table mGuiTable = new Table();

		/** Picking for static terrains */
		private QueryCallback mCallback = new QueryCallback() {
			@Override
			public boolean reportFixture(Fixture fixture) {
				if (fixture.testPoint(mTouchCurrent)) {
					// Hit a terrain actor directly
					if (fixture.getBody().getUserData() instanceof StaticTerrainActor) {
						mHitBodies.add(fixture.getBody());
					}
					// Hit a corner
					else if (fixture.getBody().getUserData() instanceof HitWrapper) {
						HitWrapper hitWrapper = (HitWrapper) fixture.getBody().getUserData();
						if (hitWrapper.actor instanceof StaticTerrainActor) {
							if (mActor != null && mActor == hitWrapper.actor) {
								mHitBodies.add(fixture.getBody());
								return false;
							}
						}
					}
				}
				return true;
			}
		};
	}

	/**
	 * Handles all events for when no tool is active
	 */
	private class NoneHandler extends Tool {
		@Override
		public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
			// TODO Auto-generated method stub

		}

		@Override
		public void down() {
			// TODO Auto-generated method stub
		}

		@Override
		public void dragged() {
			// TODO Auto-generated method stub
		}

		@Override
		public void up() {
			// TODO Auto-generated method stub
		}

		@Override
		public void filterPicks() {
			// TODO Auto-generated method stub
		}

		@Override
		public void setActor() {
			// TODO Auto-generated method stub
		}

		@Override
		public void initGui() {
			/** @TODO init gui */
		}

		@Override
		public Table getGui() {
			/** @TODO return table */
			return null;
		}
	}
}
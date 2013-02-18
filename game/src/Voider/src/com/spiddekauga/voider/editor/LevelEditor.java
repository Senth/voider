package com.spiddekauga.voider.editor;

import java.util.LinkedList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.utils.Pools;
import com.spiddekauga.utils.Invoker;
import com.spiddekauga.utils.Scroller;
import com.spiddekauga.utils.Scroller.ScrollAxis;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.game.Collectibles;
import com.spiddekauga.voider.game.GameScene;
import com.spiddekauga.voider.game.Level;
import com.spiddekauga.voider.game.LevelDef;
import com.spiddekauga.voider.game.actors.Actor;
import com.spiddekauga.voider.game.actors.PickupActor;
import com.spiddekauga.voider.game.actors.PickupActorDef;
import com.spiddekauga.voider.game.actors.StaticTerrainActor;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.resources.ResourceNames;
import com.spiddekauga.voider.resources.ResourceSaver;
import com.spiddekauga.voider.resources.UndefinedResourceTypeException;
import com.spiddekauga.voider.scene.DrawActorTool;
import com.spiddekauga.voider.scene.LoadingScene;
import com.spiddekauga.voider.scene.SceneSwitcher;
import com.spiddekauga.voider.scene.TouchTool;
import com.spiddekauga.voider.scene.WorldScene;

/**
 * The level editor scene
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 * 
 * @TODO unload level
 */
public class LevelEditor extends WorldScene implements IActorDrawEditor {
	/**
	 * Constructor for the level editor
	 */
	public LevelEditor() {
		super(new LevelEditorGui());
		((LevelEditorGui)mGui).setLevelEditor(this);

		Actor.setEditorActive(true);

		mScroller = new Scroller(50, 2000, 10, 200, ScrollAxis.X);

		// Init all tools
		DrawActorTool drawActorTool = new DrawActorTool(mCamera, mWorld, mInvoker, StaticTerrainActor.class, this);
		mTouchTools[Tools.STATIC_TERRAIN.ordinal()] = drawActorTool;
		mTouchTools[Tools.PICKUP.ordinal()] = null;

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
		mLevel = level;
		mInvoker.dispose();
	}

	/**
	 * Sets the currently selected actor
	 * @param actor the new selected actor
	 */
	public void setSelectedActor(Actor actor) {
		mSelectedActor = actor;
	}

	/**
	 * @return the currently selected actor
	 */
	public Actor getSelectedActor() {
		return mSelectedActor;
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
			if (!mGui.isInitialized()) {
				mGui.initGui();
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

	/**
	 * Saves the current level
	 */
	void save() {
		ResourceSaver.save(mLevel.getDef());
		ResourceSaver.save(mLevel);
	}

	/**
	 * Loads a level
	 */
	void load() {
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

	/**
	 * Creates a new empty level
	 */
	void newLevel() {
		mLevel.dispose();
		LevelDef levelDef = new LevelDef();
		Level level = new Level(levelDef);
		setLevel(level);
	}

	/**
	 * Sets the current active pickup tool
	 * @param pickupTool the new active pickup tool
	 */
	void setPickupTool(PickupTools pickupTool) {
		mPickupTool.setTool(pickupTool);
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

	/** Level we're currently editing */
	private Level mLevel = null;
	/** Currently selected actor */
	private Actor mSelectedActor = null;
	/** Invoker for the level editor */
	private Invoker mInvoker = new Invoker();

	// Event stuff
	/** Scrolling for nice scrolling */
	private Scroller mScroller;

	// -------------------------------------
	//				Tools
	// -------------------------------------
	/** Current selected tool */
	private Tools mToolType = Tools.STATIC_TERRAIN;
	/** All the available tools */
	private TouchTool[] mTouchTools = new TouchTool[Tools.values().length];


	// Temporary variables for touch detection
	/** Original point when pressing */
	private Vector2 mTouchOrigin = new Vector2();
	/** Current point when pressing */
	private Vector2 mTouchCurrent = new Vector2();
	/** Starting position for the camera scroll */
	private Vector2 mScrollCameraOrigin = new Vector2();
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
	enum Tools {
		/** Pickup tool @TODO change style*/
		PICKUP("toggle"),
		/** Terrain tool @TODO change style */
		STATIC_TERRAIN("toggle");

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
	 * Pickup tools
	 */
	enum PickupTools {
		/** Add/create health 25 @TODO change style */
		ADD_HEALTH_25("toggle"),
		/** Add/Create health 50 @TODO change style */
		ADD_HEALTH_50("toggle"),
		/** Moves a pickup @TODO change style */
		MOVE("toggle"),
		/** Remove a pickup @TODO change style */
		REMOVE("toggle");

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
		private PickupTools(String style) {
			mStyleName = style;
		}

		/** Name of the style the tool shall use */
		private String mStyleName;
	}



	/** Tool for pickups */
	private PickupTool mPickupTool = new PickupTool();

	/**
	 * Common interface for all event handlers
	 */
	private abstract class Tool {
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
	}

	/**
	 * Handles all pickup things in the editor
	 * 
	 * @author Matteus Magnusson <senth.wallace@gmail.com>
	 */
	private class PickupTool extends Tool {
		@Override
		public void down() {
			Collectibles collectibleToAdd = null;
			switch (mTool) {
			case ADD_HEALTH_25:
				collectibleToAdd = Collectibles.HEALTH_25;
				break;


			case ADD_HEALTH_50:
				collectibleToAdd = Collectibles.HEALTH_50;
				break;


			case MOVE:
				//				testPick(mCallback);
				if (mHitBody != null && mSelectedActor != null) {
					mMoving = true;
					mDragOrigin.set(mHitBody.getPosition());
				}
				break;


			case REMOVE:
				//				testPick(mCallback);
				if (mHitBody != null && mSelectedActor != null) {
					//					mLevelInvoker.execute(new ClActorRemove(mSelectedActor, true));
					//					mLevelInvoker.execute(new ClActorSelect(null, true));
				}
				break;
			}


			// Add a new pickup at the specific location
			if (collectibleToAdd != null) {
				PickupActorDef pickupActorDef = new PickupActorDef();
				pickupActorDef.setCollectible(collectibleToAdd);
				mActorAdding = new PickupActor();
				mActorAdding.setDef(pickupActorDef);
				mActorAdding.setPosition(mTouchOrigin);

				// Add temporary actor
				mLevel.addActor(mActorAdding);
			}
		}

		@Override
		public void dragged() {

			switch (mTool) {
			case ADD_HEALTH_25:
			case ADD_HEALTH_50:
				mActorAdding.setPosition(mTouchCurrent);
				break;

			case MOVE:
				if (mMoving) {
					mSelectedActor.setPosition(mTouchCurrent);
				}
				break;


			case REMOVE:
				// Does nothing
				break;
			}
		}

		@Override
		public void up() {
			boolean addActor = false;

			switch (mTool) {
			case ADD_HEALTH_25:
			case ADD_HEALTH_50:
				addActor = true;

			case MOVE:
				if (mMoving) {
					// Reset position of the selected actor
					mSelectedActor.setPosition(mDragOrigin);

					// Move actor through command
					//					mLevelInvoker.execute(new ClActorMove(mSelectedActor, mTouchCurrent, true));
				}
				break;


			case REMOVE:
				// Does nothing
				break;
			}

			if (addActor) {
				mLevel.removeActor(mActorAdding.getId());
				//				mLevelInvoker.execute(new ClActorAdd(mActorAdding));
				//				mLevelInvoker.execute(new ClActorSelect(mActorAdding, true));
			}
		}

		@Override
		public void filterPicks() {
			Actor newActor = mSelectedActor;
			Actor oldActor = mSelectedActor;
			for (Body body : mHitBodies) {
				if (body.getUserData() instanceof PickupActor) {
					mHitBody = body;
					newActor = (Actor) mHitBody.getUserData();
					break;
				}
			}

			if (newActor != oldActor) {
				//				mLevelInvoker.execute(new ClActorSelect(newActor, false));
			}
		}

		@Override
		public void setActor() {
			// Does nothing
		}

		/**
		 * Sets the current active pickup tool
		 * @param tool new active pickup tool
		 */
		public void setTool(PickupTools tool) {
			mTool = tool;
		}

		/** Picking for static terrains */
		private QueryCallback mCallback = new QueryCallback() {
			@Override
			public boolean reportFixture(Fixture fixture) {
				if (fixture.testPoint(mTouchCurrent)) {
					// Hit a terrain actor directly
					if (fixture.getBody().getUserData() instanceof PickupActor) {
						mHitBodies.add(fixture.getBody());
					}
				}
				return true;
			}
		};

		/** Current tool for pickup */
		private PickupTools mTool = PickupTools.ADD_HEALTH_25;
		/** Actor we're currently adding */
		private PickupActor mActorAdding = null;
		/** Original position of a dragging actor */
		private Vector2 mDragOrigin = new Vector2();
		/** True if we're moving an actor */
		private boolean mMoving = false;
	}



}
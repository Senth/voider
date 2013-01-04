package com.spiddekauga.voider.editor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.Scene;
import com.spiddekauga.voider.game.Actor;
import com.spiddekauga.voider.game.Level;
import com.spiddekauga.voider.game.actors.StaticTerrainActor;
import com.spiddekauga.voider.ui.UiEvent;

/**
 * The level editor scene
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class LevelEditor extends Scene {
	/**
	 * Constructor for the level editor
	 */
	public LevelEditor() {
		/** @TODO fix aspect ratio */
		mWorld = new World(new Vector2(), true);
		mCamera = new OrthographicCamera(80, 48);
		Actor.setWorld(mWorld);
		Actor.setEditorActive(true);

		/** @TODO remove active tool */
		mToolActive = Tools.STATIC_TERRAIN;
		mEventHandlerCurrent = mStaticTerrainHandler;
	}

	@Override
	public void update() {
		mWorld.step(1/60f, 6, 2);
		mLevel.update(false);
	}

	@Override
	public void render() {
		if (Config.Graphics.USE_DEBUG_RENDERER) {
			mDebugRenderer.render(mWorld, mCamera.combined);
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
		mLevelInvoker.setLevel(level);
	}

	// --------------------------------
	//				EVENTS
	// --------------------------------
	@Override
	public void onUiEvent(UiEvent event) {
		/** @TODO handle ui events */
	}

	@Override
	public boolean touchDown(int x, int y, int pointer, int button) {
		// Special case, scrolling the map. This is the case if pressed
		// middle mouse button, or two fingers are on the screen
		if (button == 3 || (Gdx.app.getInput().isTouched(0) && Gdx.app.getInput().isTouched(1))) {
			mScrolling = true;
			return true;
		}

		// Only do something for the first pointer
		if (pointer == 0) {
			mTestPoint.set(x, y, 0);
			mCamera.unproject(mTestPoint);
			mTouchCurrent.x = mTestPoint.x;
			mTouchCurrent.y = mTestPoint.y;
			mTouchOrigin.set(mTouchCurrent);

			mEventHandlerCurrent.down();

			return true;
		}

		// Disable GUI?

		return false;
	}

	@Override
	public boolean touchDragged(int x, int y, int pointer) {
		// Only do something for the first pointer
		if (pointer == 0) {
			mTestPoint.set(x, y, 0);
			mCamera.unproject(mTestPoint);
			mTouchCurrent.x = mTestPoint.x;
			mTouchCurrent.y = mTestPoint.y;

			mEventHandlerCurrent.dragged();

			return true;
		}
		return false;
	}

	@Override
	public boolean touchUp(int x, int y, int pointer, int button) {
		// Not scrolling any more
		if (button == 3 || !Gdx.app.getInput().isTouched(0) || !Gdx.app.getInput().isTouched(1)) {
			mScrolling = false;
			return true;
		}

		// Only do something for the first pointer
		if (pointer == 0) {
			mTestPoint.set(x, y, 0);
			mCamera.unproject(mTestPoint);
			mTouchCurrent.x = mTestPoint.x;
			mTouchCurrent.y = mTestPoint.y;

			mEventHandlerCurrent.up();

			return true;
		}

		// Enable GUI?

		return false;
	}

	/**
	 * Runs a test for picking
	 * @param callback the callback function to use
	 */
	public void testPick(QueryCallback callback) {
		mHitBody = null;
		mWorld.QueryAABB(callback, mTouchCurrent.x - 0.0001f, mTouchCurrent.y - 0.0001f, mTouchCurrent.x + 0.0001f, mTouchCurrent.y + 0.0001f);
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

	// Event stuff
	/** If we're scrolling the map */
	private boolean mScrolling = false;
	/** The active tool */
	private Tools mToolActive = Tools.NONE;


	// Temporary variables for touch detection
	/** Original point when pressing */
	private Vector2 mTouchOrigin = new Vector2();
	/** Current point when pressing */
	private Vector2 mTouchCurrent = new Vector2();
	/** For ray testing on player ship when touching it */
	private Vector3 mTestPoint = new Vector3();
	/** Body that was hit */
	private Body mHitBody = null;

	/**
	 * All tools in the level editor
	 */
	enum Tools {
		/** Creating static terrain */
		STATIC_TERRAIN,
		/** No tool active */
		NONE,
	}


	// -------------------------------------
	//		EVENT HANDLING FOR TOOLS
	// -------------------------------------
	/** Event handler for the current tool */
	private EventHandler mEventHandlerCurrent = null;

	/** Event handler for static terrain tool */
	private StaticTerrainHandler mStaticTerrainHandler = new StaticTerrainHandler();
	/** Event handler for when no tool is active */
	private NoneHandler mNoneHandler = new NoneHandler();

	/**
	 * Common interface for all event handlers
	 */
	private interface EventHandler {
		/**
		 * Handles touch down events
		 */
		public void down();

		/**
		 * Handles touch dragged events
		 */
		public void dragged();

		/**
		 * Handles touch up events
		 */
		public void up();
	}

	/**
	 * Handles all events when static terrain tool is active
	 * 
	 * @author Matteus Magnusson <senth.wallace@gmail.com>
	 */
	private class StaticTerrainHandler implements EventHandler {
		@Override
		public void down() {
			// Create Terrain
			if (mActor == null) {
				mActor = new StaticTerrainActor();
				mLevel.addActor(mActor);
			}

			/** @TODO two double hits on the actor (not corners) completes the terrain
			 * This will make it possible to start another terrain */

			// Test if we hit a corner...
			testPick(mCallback);
			if (mHitBody != null) {
				mCornerCurrentIndex = mActor.getCornerIndex(mHitBody.getPosition());
			}
			// Else create a new corner
			else {
				mCornerCurrentIndex = mActor.addCorner(mTouchOrigin);
			}
		}

		@Override
		public void dragged() {
			if (mCornerCurrentIndex != -1) {
				mActor.moveCorner(mCornerCurrentIndex, mTouchCurrent);
			}
		}

		@Override
		public void up() {

		}

		/** Index of the current corner */
		private int mCornerCurrentIndex = -1;
		/** Current Static terrain actor */
		private StaticTerrainActor mActor = null;

		/**
		 * Picking for static terrains
		 */
		private QueryCallback mCallback = new QueryCallback() {
			@Override
			public boolean reportFixture(Fixture fixture) {
				if (fixture.testPoint(mTouchCurrent)) {
					if (fixture.getBody().getUserData() instanceof StaticTerrainActor) {
						mHitBody = fixture.getBody();
						mActor = (StaticTerrainActor) mHitBody.getUserData();
						return false;
					}
				}
				return true;
			}
		};
	}

	/**
	 * Handles all events for when no tool is active
	 */
	public class NoneHandler implements EventHandler {
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
	}
}

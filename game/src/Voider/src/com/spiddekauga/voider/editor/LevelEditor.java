package com.spiddekauga.voider.editor;

import java.util.LinkedList;

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
import com.badlogic.gdx.utils.Pools;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.Scene;
import com.spiddekauga.voider.editor.commands.ClAddActor;
import com.spiddekauga.voider.editor.commands.ClTerrainActorAddCorner;
import com.spiddekauga.voider.editor.commands.ClTerrainActorMoveCorner;
import com.spiddekauga.voider.editor.commands.LevelCommand;
import com.spiddekauga.voider.game.Actor;
import com.spiddekauga.voider.game.GameTime;
import com.spiddekauga.voider.game.Level;
import com.spiddekauga.voider.game.actors.StaticTerrainActor;
import com.spiddekauga.voider.game.actors.StaticTerrainActor.PolygonComplexException;
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

		if (mClickTimeLast + Config.Input.DOUBLE_CLICK_TIME > GameTime.getTotalTimeElapsed()) {
			mClickTimeLast = 0f;
			mDoubleClick = true;
		} else {
			mDoubleClick = false;
			mClickTimeLast = GameTime.getTotalTimeElapsed();
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
		if (!mScrolling && pointer == 0) {
			mTestPoint.set(x, y, 0);
			mCamera.unproject(mTestPoint);
			mTouchCurrent.x = mTestPoint.x;
			mTouchCurrent.y = mTestPoint.y;

			/** @TODO check long click */

			mEventHandlerCurrent.dragged();

			return true;
		}
		return false;
	}

	@Override
	public boolean touchUp(int x, int y, int pointer, int button) {
		// Not scrolling any more
		if (mScrolling && (button == 3 || !Gdx.app.getInput().isTouched(0) || !Gdx.app.getInput().isTouched(1))) {
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
		mHitBodies.clear();
		mHitBody = null;
		mWorld.QueryAABB(callback, mTouchCurrent.x - 0.0001f, mTouchCurrent.y - 0.0001f, mTouchCurrent.x + 0.0001f, mTouchCurrent.y + 0.0001f);
		mEventHandlerCurrent.filterPicks();
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
	/** True if double clicked */
	private boolean mDoubleClick = false;
	/** Last click time */
	private float mClickTimeLast = 0f;


	// Temporary variables for touch detection
	/** Original point when pressing */
	private Vector2 mTouchOrigin = new Vector2();
	/** Current point when pressing */
	private Vector2 mTouchCurrent = new Vector2();
	/** For ray testing on player ship when touching it */
	private Vector3 mTestPoint = new Vector3();
	/** Body that was hit (and prioritized) */
	private Body mHitBody = null;
	/** Bodies that were hit and selected for filtering, before mHitBody is set */
	private LinkedList<Body> mHitBodies = new LinkedList<Body>();

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

		/**
		 * Filter picks
		 */
		public void filterPicks();
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
				ClAddActor command = new ClAddActor(mActor);
				mLevelInvoker.execute(command);
			}

			// Double click inside current actor finishes closes it
			if (mDoubleClick && mHitBody != null && mHitBody.getUserData() == mActor) {
				// Remove the last corner if we accidently added one when double clicking
				if (mCornerLastIndex != -1) {
					mActor.removeCorner(mCornerLastIndex);
				}
				mActor = null;
				return;
			}

			// Test if we hit a corner...
			testPick(mCallback);
			if (mHitBody != null) {
				// Hit the terrain body (no corner), create corner
				if (mHitBody.getUserData() == mActor) {
					createCorner();
				}
				// Else - Hit a corner, start moving it
				else {
					mCornerCurrentIndex = mActor.getCornerIndex(mHitBody.getPosition());
					mCornerCurrentOrigin.set(mHitBody.getPosition());
					mCornerCurrentAddedNow = false;
				}
			}
			// Else create a new corner
			else {
				createCorner();
			}
		}

		@Override
		public void dragged() {
			if (mCornerCurrentIndex != -1) {
				try {
					mActor.moveCorner(mCornerCurrentIndex, mTouchCurrent);
				} catch (PolygonComplexException e) {
					// Does nothing
				}
			}
		}

		@Override
		public void up() {
			// ACTIONS VIA COMMANDS INSTEAD
			// MOVE CORNER
			if (mCornerCurrentIndex != -1 && mActor != null) {
				// Reset to original position
				Vector2 newPos = Pools.obtain(Vector2.class);
				newPos.set(mActor.getCorner(mCornerCurrentIndex));
				try {
					mActor.moveCorner(mCornerCurrentIndex, mCornerCurrentOrigin);
					LevelCommand command = new ClTerrainActorMoveCorner(mActor, mCornerCurrentIndex, newPos);
					mLevelInvoker.execute(command);
				} catch (PolygonComplexException e) {
					// Does nothing
				}
				Pools.free(newPos);
			}

			mCornerLastIndex = mCornerCurrentIndex;
			mCornerCurrentIndex = -1;
		}

		/**
		 * This will filter so that corners of the active terrain will
		 * be selected first, then any click on terrain
		 */
		@Override
		public void filterPicks() {
			StaticTerrainActor oldActor = mActor;
			for (Body body : mHitBodies) {
				// Only set hit terrain if no hit body has been set
				if (body.getUserData() instanceof StaticTerrainActor) {
					if (mHitBody == null) {
						mHitBody = body;
						mActor = (StaticTerrainActor) mHitBody.getUserData();
					}
				}
				// A corner - select it, and quick return
				else if (body.getUserData() instanceof HitWrapper) {
					mHitBody = body;
					// Still same actor
					mActor = oldActor;
					return;
				}
			}
		}

		/**
		 * Tries to create a new corner. Will print out
		 * an error message if it didn't work
		 */
		private void createCorner() {
			ClTerrainActorAddCorner command = new ClTerrainActorAddCorner(mActor, mTouchOrigin);
			boolean added = mLevelInvoker.execute(command);
			if (added) {
				mCornerCurrentIndex = mActor.getLastAddedCornerIndex();
				mCornerCurrentOrigin.set(mTouchOrigin);
				mCornerCurrentAddedNow = true;
			} else {
				/** @TODO print some error message on screen, cannot add corner here */
			}
		}

		/** Origin of the corner, before dragging it */
		private Vector2 mCornerCurrentOrigin = new Vector2();
		/** Index of the current corner */
		private int mCornerCurrentIndex = -1;
		/** Last corner index */
		private int mCornerLastIndex = -1;
		/** True if the current corner was added now */
		private boolean mCornerCurrentAddedNow = false;
		/** Current Static terrain actor */
		private StaticTerrainActor mActor = null;

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

		@Override
		public void filterPicks() {
			// TODO Auto-generated method stub
		}
	}
}

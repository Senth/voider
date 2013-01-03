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

		// What tool is active?

		// Create Terrain
		if (mActor == null) {
			mActor = new StaticTerrainActor();
			mLevel.addActor(mActor);
		}


		mTestPoint.set(x, y, 0);
		mCamera.unproject(mTestPoint);


		mActor.addCorner(new Vector2(mTestPoint.x, mTestPoint.y));


		// Disable GUI?

		return false;
	}

	@Override
	public boolean touchDragged(int x, int y, int pointer) {

		return false;
	}

	@Override
	public boolean touchUp(int x, int y, int pointer, int button) {
		// Not scrolling any more
		if (button == 3 || !Gdx.app.getInput().isTouched(0) || !Gdx.app.getInput().isTouched(1)) {
			mScrolling = false;
			return true;
		}

		// Enabel GUI?

		return false;
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

	/** If we're scrolling the map */
	private boolean mScrolling = false;
	/** Active actor, this actor gets the transformation etc */
	private StaticTerrainActor mActor = null;


	// Temporary variables for touch detection
	/** Original point when pressing */
	private Vector2 mTouchOrigin = new Vector2();
	/** Current point when pressing */
	private Vector2 mTouchCurrent = new Vector2();
	/** For ray testing on player ship when touching it */
	private Vector3 mTestPoint = new Vector3();
	/** Body that was hit */
	private Body mHitBody = null;
	/** Callback for "ray testing" */
	private QueryCallback mCallback = new QueryCallback() {
		@Override
		public boolean reportFixture(Fixture fixture) {
			if (fixture.testPoint(mTestPoint.x, mTestPoint.y)) {
				mHitBody = fixture.getBody();
				return false;
			} else {
				return true;
			}
		}
	};
}

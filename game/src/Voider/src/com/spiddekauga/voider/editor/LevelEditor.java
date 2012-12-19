package com.spiddekauga.voider.editor;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.Scene;
import com.spiddekauga.voider.game.Actor;
import com.spiddekauga.voider.game.Level;
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
	LevelEditor() {
		/** @TODO fix aspect ratio */
		mWorld = new World(new Vector2(), true);
		mCamera = new OrthographicCamera(80, 48);
		Actor.setWorld(mWorld);
	}

	/* (non-Javadoc)
	 * @see com.spiddekauga.voider.ui.IUiListener#onUiEvent(com.spiddekauga.voider.ui.UiEvent)
	 */
	@Override
	public void onUiEvent(UiEvent event) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.spiddekauga.voider.Scene#update()
	 */
	@Override
	public void update() {
		mWorld.step(1/60f, 6, 2);
		mLevel.update(true);
	}

	@Override
	public void render() {
		if (Config.Graphics.USE_DEBUG_RENDERER) {
			mDebugRenderer.render(mWorld, mCamera.combined);

		} else {
			mLevel.render(mSpriteBatch);
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

	/** Physics world */
	private World mWorld = null;
	/** Camera for the editor */
	private Camera mCamera = null;
	/** Debug renderer */
	private Box2DDebugRenderer mDebugRenderer = new Box2DDebugRenderer();
	/** Level we're currently editing */
	private Level mLevel = null;
	/** Level invoker, sends all editing commands through this */
	private LevelInvoker mLevelInvoker = null;
}

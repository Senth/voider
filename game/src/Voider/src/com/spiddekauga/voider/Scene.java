package com.spiddekauga.voider;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.spiddekauga.voider.ui.IUiListener;

/**
 * Base class for all scenes that should be rendered. Examples of scenes:
 * Game, Menus, Editors. It extends InputAdapter which makes it able to
 * handle all input events through derived classes (if necessary).
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public abstract class Scene extends InputAdapter implements IUiListener {
	/**
	 * Default constructor. Creates the input multiplexer. UI always has
	 * priority over everything else.
	 */
	public Scene() {
		mInputMultiplexer.addProcessor(0, mUi);
		mInputMultiplexer.addProcessor(1, this);
	}

	/**
	 * Runs the scene. Don't Override this method as this method clears the screen,
	 * renders it, and updates the scene elements.
	 */
	public final void run() {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		render();
		update();
	}

	/**
	 * Updates the scene
	 */
	public abstract void update();

	/**
	 * Called just before the scene reactivates. I.e. this scene has been activated
	 * before and in turn activated another scene on the stack. Now this scene has
	 * been reactivated due to some actions.
	 * @param outcome the outcome of the previous scene that was on the stack.
	 * @param message the outcome message provided with the outcome, null if none
	 * was provided.
	 */
	public void onReActivate(Outcomes outcome, String message) {
		// Does nothing
	}

	/**
	 * Called when the scene deactivates (another one is activated)
	 */
	public void onDeactivate() {
		// Does nothing
	}

	/**
	 * Called when the scene is deleted
	 */
	public void onDisposed() {
		// Does nothing
	}

	/**
	 * Renders the scene
	 */
	public void render() {
		mUi.act(Gdx.graphics.getDeltaTime());
		mUi.draw();
	}

	/**
	 * @return true if the scene is done with it work, i.e. it should be popped
	 * from the stack.
	 */
	public final boolean isDone() {
		return mOutcome != null;
	}

	/**
	 * @return outcome of the scene, returns null if no outcome has been set (when
	 * #isDone() returns true.
	 */
	public final Outcomes getOutcome() {
		return mOutcome;
	}

	/**
	 * @return outcome message of the scene, if none exist it will return null.
	 */
	public final String getOutcomeMessage() {
		return mOutcomeMessage;
	}

	/**
	 * Sets the outcome of the scene, this also "completes" the scene making #isDone()
	 * return true. Outcomes can only be set once, this to prevent another later
	 * outcome to overwrite the original outcome.
	 * @param outcome the outcome of the scene
	 */
	public final void setOutcome(Outcomes outcome) {
		if (mOutcome == null) {
			mOutcome = outcome;
		}
	}

	/**
	 * @return the input multiplexer, this determines who shall get the input
	 */
	public final InputMultiplexer getInputMultiplexer() {
		return mInputMultiplexer;
	}

	/**
	 * Sets the outcome of the scene, including a description of the outcome. E.g.
	 * LOADING_FAILED_MISSING_FILE can specify the missing file in this message.
	 * Outcomes can only be set once, this to prevent another later outcome
	 * to overwrite the original outcome.
	 * @param outcome the outcome of the scene
	 * @param message a descriptive outcome message.
	 */
	public void setOutcome(Outcomes outcome, String message) {
		if (mOutcome == null) {
			setOutcome(outcome);
			mOutcomeMessage = message;
		}
	}

	/**
	 * Different outcomes a scene can have
	 */
	public enum Outcomes {
		/** Level was successfully completed */
		LEVEL_COMPLETED,
		/** Player died during the level */
		LEVEL_PLAYER_DIED,
		/** Player decided to quit the level */
		LEVEL_QUIT,
		/** Loading succeeded */
		LOADING_SUCCEEDED,
		/** Loading failed, missing file */
		LOADING_FAILED_MISSING_FILE,
		/** Loading failed, corrupt file */
		LOADING_FAILED_CORRUPT_FILE,
	}

	/** Handles user interfaces for the scene */
	protected Stage mUi = new Stage();
	/** Sprite Batch used for rendering stuff */
	protected SpriteBatch mSpriteBatch = new SpriteBatch();
	/** Input multiplexer */
	protected InputMultiplexer mInputMultiplexer = new InputMultiplexer();

	/** Outcome of scene, this is set when a derived class calls setOutcome */
	private Outcomes mOutcome = null;
	/** Message of the outcome */
	private String mOutcomeMessage = null;
}
package com.spiddekauga.voider.editor;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Vector2;
import com.spiddekauga.utils.Invoker;
import com.spiddekauga.utils.ShapeRendererEx.ShapeType;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.game.actors.Actor;
import com.spiddekauga.voider.game.actors.ActorDef;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.resources.ResourceNames;
import com.spiddekauga.voider.scene.Gui;
import com.spiddekauga.voider.scene.SceneSwitcher;
import com.spiddekauga.voider.scene.WorldScene;
import com.spiddekauga.voider.utils.Pools;

/**
 * Common class for all editors
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public abstract class Editor extends WorldScene implements IEditor {

	/**
	 * @param gui GUI to be used with the editor
	 * @param pickRadius picking radius of the editor
	 */
	public Editor(Gui gui, float pickRadius) {
		super(gui, pickRadius);
	}

	/**
	 * @return true if the editor shall try to auto-save the current file
	 */
	protected boolean shallAutoSave() {
		if (!mSaved && !isDrawing()) {

			float totalTimeElapsed = getGameTime().getTotalTimeElapsed();
			// Save after X seconds of inactivity or always save after Y minutes
			// regardless.
			if (totalTimeElapsed - mActivityTimeLast >= Config.Editor.AUTO_SAVE_TIME_ON_INACTIVITY) {
				return true;
			} else if (totalTimeElapsed - mUnsavedTime >= Config.Editor.AUTO_SAVE_TIME_FORCED) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected void loadResources() {
		super.loadResources();
		ResourceCacheFacade.load(ResourceNames.UI_EDITOR_BUTTONS);
		ResourceCacheFacade.load(ResourceNames.UI_GENERAL);
		ResourceCacheFacade.load(ResourceNames.SHADER_DEFAULT);
		ResourceCacheFacade.load(ResourceNames.UI_EDITOR_TOOLTIPS);
	}

	@Override
	protected void unloadResources() {
		super.unloadResources();
		ResourceCacheFacade.unload(ResourceNames.UI_EDITOR_BUTTONS);
		ResourceCacheFacade.unload(ResourceNames.UI_GENERAL);
		ResourceCacheFacade.unload(ResourceNames.SHADER_DEFAULT);
		ResourceCacheFacade.unload(ResourceNames.UI_EDITOR_TOOLTIPS);
	}

	/**
	 * Checks if the resource needs saving and then saves it if that's the case
	 * @param deltaTime time elapsed since last frame
	 */
	@Override
	protected void update(float deltaTime) {
		super.update(deltaTime);

		if (shallAutoSave()) {
			saveDef();
		}
	}

	@Override
	public boolean isSaved() {
		return mSaved;
	}

	/**
	 * Set the editor as saved
	 */
	protected void setSaved() {
		mSaved = true;
	}

	/**
	 * Set the editor as unsaved
	 */
	protected void setUnsaved() {
		mSaved = false;
		mUnsavedTime = getGameTime().getTotalTimeElapsed();
		mActivityTimeLast = getGameTime().getTotalTimeElapsed();
	}

	/**
	 * @return invoker for the editor
	 */
	@Override
	public Invoker getInvoker() {
		return mInvoker;
	}

	/**
	 * Creates a texture out of the specified actor definition and sets it
	 * for the actor definition.
	 * @param actorDef the actor definition to create an image for
	 * @param actor the actual actor to render and take a screenshot of, should be empty
	 */
	protected void createActorDefTexture(ActorDef actorDef, Actor actor) {
		float width = actorDef.getWidth();
		float height = actorDef.getHeight();

		// Create duplicate
		ActorDef copy = actorDef.copyNewResource();

		// Calculate how many world coordinates 200px is
		float worldScreenRatio = SceneSwitcher.getWorldScreenRatio();

		// Calculate normalize
		float normalizeLength = Config.Actor.SAVE_TEXTURE_SIZE * worldScreenRatio;
		if (width > height) {
			normalizeLength /= width;
		} else {
			normalizeLength /= height;
		}

		// Normalize width and height vertices to use 200px
		copy.getVisualVars().setCenterOffset(0,0);
		ArrayList<Vector2> vertices = copy.getVisualVars().getTriangleVertices();
		for (Vector2 vertex : vertices) {
			vertex.scl(normalizeLength);
		}

		// Calculate where to move it
		Vector2 offset = Pools.vector2.obtain();
		offset.set(Float.MAX_VALUE, Float.MAX_VALUE);
		for (Vector2 vertex : vertices) {
			if (vertex.x < offset.x) {
				offset.x = vertex.x;
			}
			if (vertex.y < offset.y) {
				offset.y = vertex.y;
			}
		}

		// Offset with world coordinates
		Vector2 minScreenPos = SceneSwitcher.getWorldMinCoordinates();
		offset.add(minScreenPos);

		// Set actor def
		actor.setDef(copy);

		// Set position
		actor.setPosition(offset);

		// Clear screen
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		// Render actor
		mShapeRenderer.push(ShapeType.Filled);
		actor.render(mShapeRenderer);
		mShapeRenderer.pop();

		// Take a 200x200 screen shot

		// Save the texture from the screen shot

		// Make black color to alpha

		// Save texture to original definition

	}

	/** Invoker */
	protected Invoker mInvoker = new Invoker();
	/** Is the resource currently saved? */
	private boolean mSaved = false;
	/** Is the resource currently saving, this generally means it takes a screenshot of the image */
	private boolean mSaving = false;
	/** When the resource became unsaved */
	private float mUnsavedTime = 0;
	/** Last time the player did some activity */
	private float mActivityTimeLast = 0;
}

package com.spiddekauga.voider.editor;

import com.spiddekauga.utils.Invoker;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.game.actors.ActorDef;
import com.spiddekauga.voider.resources.ResourceCacheFacade;
import com.spiddekauga.voider.resources.ResourceNames;
import com.spiddekauga.voider.scene.Gui;
import com.spiddekauga.voider.scene.WorldScene;

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
	 */
	protected void createActorDefTexture(ActorDef actorDef) {
		float width = actorDef.getWidth();
		float height = actorDef.getHeight();

		// Create duplicate

		// Calculate how many world coordinates 200px is

		// Normalize width and height vertices to use 200px

		// Create a new actor and place it in a window corner

		// Clear screen

		// Render actor

		// Take a 200x200 screen shot

		// Save the texture from the screen shot

		// Make black color to alpha

		// Save texture to original definition

	}

	/** Invoker */
	protected Invoker mInvoker = new Invoker();
	/** Is the resource currently saved? */
	private boolean mSaved = false;
	/** When the resource became unsaved */
	private float mUnsavedTime = 0;
	/** Last time the player did some activity */
	private float mActivityTimeLast = 0;
}

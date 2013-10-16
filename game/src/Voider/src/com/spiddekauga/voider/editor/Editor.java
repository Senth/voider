package com.spiddekauga.voider.editor;

import com.spiddekauga.voider.Config;
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
	public Editor(
			Gui gui, float pickRadius) {
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

	/** Is the resource currently saved? */
	private boolean mSaved = false;
	/** When the resource became unsaved */
	private float mUnsavedTime = 0;
	/** Last time the player did some activity */
	private float mActivityTimeLast = 0;
}

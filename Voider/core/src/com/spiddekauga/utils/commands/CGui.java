package com.spiddekauga.utils.commands;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.spiddekauga.utils.GameTime;
import com.spiddekauga.voider.Config;
import com.spiddekauga.voider.Config.Gui;

/**
 * Common class for GUI objects
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public abstract class CGui extends Command {

	/**
	 * Sets a new temporary name for the GUI object
	 * @param object the actor to change the name on
	 * @return true if the temporary name was set successfully, false if the actor already
	 *         uses the temporary name
	 */
	protected boolean setTemporaryName(Actor object) {
		mOriginalName = object.getName();
		if (!Gui.GUI_INVOKER_TEMP_NAME.equals(mOriginalName)) {
			object.setName(Gui.GUI_INVOKER_TEMP_NAME);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Resets the actor to the original name
	 * @param actor the actor to change the name
	 */
	protected void setOriginalName(Actor actor) {
		actor.setName(mOriginalName);
	}

	/**
	 * Set execution time
	 */
	protected void setExecuteTime() {
		mExecuteTime = GameTime.getTotalGlobalTimeElapsed();
	}

	/**
	 * Check if an GUI element can be combined (i.e. hasn't been too long since the values
	 * were change)
	 * @return true if this GUI element can be combined with a newer one, false if too
	 *         long time elapsed since this command was executed
	 */
	protected boolean isCombinable() {
		return GameTime.getTotalGlobalTimeElapsed() <= mExecuteTime + Config.Gui.COMMAND_COMBINABLE_WITHIN;
	}

	/** Original name for the actor */
	private String mOriginalName = null;
	/** Execute time */
	private float mExecuteTime = 0;
}

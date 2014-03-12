package com.spiddekauga.utils.commands;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.spiddekauga.utils.commands.Command;
import com.spiddekauga.voider.Config.Gui;

/**
 * Common class for GUI objects
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public abstract class CGui extends Command {

	/**
	 * Sets a new temporary name for the GUI object
	 * @param actor the actor to change the name on
	 * @return true if the temporary name was set successfully, false if the
	 * actor already uses the temporary name
	 */
	protected boolean setTemporaryName(Actor actor) {
		mOriginalName = actor.getName();
		if (!Gui.GUI_INVOKER_TEMP_NAME.equals(mOriginalName)) {
			actor.setName(Gui.GUI_INVOKER_TEMP_NAME);
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

	/** Original name for the actor */
	private String mOriginalName = null;
}

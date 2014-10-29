package com.spiddekauga.utils.commands;

import com.badlogic.gdx.Gdx;

/**
 * Forcibly quit the game
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class CGameQuit extends Command {

	@Override
	public boolean execute() {
		Gdx.app.exit();
		return true;
	}

	@Override
	public boolean undo() {
		// Does nothing
		return true;
	}

}

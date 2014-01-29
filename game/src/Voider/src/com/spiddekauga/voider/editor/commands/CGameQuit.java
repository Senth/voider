package com.spiddekauga.voider.editor.commands;

import com.badlogic.gdx.Gdx;
import com.spiddekauga.utils.commands.Command;

/**
 * Forcibly quit the game
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
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

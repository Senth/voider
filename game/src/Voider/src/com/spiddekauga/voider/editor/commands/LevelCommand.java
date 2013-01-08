package com.spiddekauga.voider.editor.commands;

import com.badlogic.gdx.utils.Disposable;
import com.spiddekauga.voider.game.Level;

/**
 * Base class for all level commands
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public abstract class LevelCommand implements Disposable {
	/**
	 * Executes the command
	 * @param level the level to execute the command on
	 * @return true if the command was successfully executed
	 */
	public abstract boolean execute(Level level);

	/**
	 * Executes the undo command, i.e. reverses the effect of the execute command
	 * @param level the level to execute the command on
	 * @return true if the command was successfully undone
	 */
	public abstract boolean undo(Level level);

	@Override
	public void dispose() {
		// Does nothing
	}
}

package com.spiddekauga.voider.editor.commands;

import com.spiddekauga.voider.game.Level;

/**
 * Base class for all level commands
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public abstract class LevelCommand {
	/**
	 * Executes the command
	 * @param level the level to execute the command on
	 */
	public abstract void execute(Level level);

	/**
	 * Executes the undo command, i.e. reverses the effect of the execute command
	 * @param level the level to execute the command on
	 */
	public abstract void undo(Level level);
}

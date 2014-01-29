package com.spiddekauga.utils;

import com.spiddekauga.utils.commands.Command;


/**
 * Only delimits two commands. Useful when commands are made to be combined.
 * Such as sliders, a delimiter could be added whenever the player lifts the mouse
 * button.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class CDelimiter extends Command {

	@Override
	public boolean execute() {
		return true;
	}

	@Override
	public boolean undo() {
		return true;
	}

}

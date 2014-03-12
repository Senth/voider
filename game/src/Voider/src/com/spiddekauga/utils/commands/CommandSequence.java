package com.spiddekauga.utils.commands;

/**
 * Executes the command in sequence.
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class CommandSequence extends Command {
	/**
	 * Creates a command sequence with the specified commands
	 * @param commands the commands to be executed in order
	 */
	public CommandSequence(Command... commands) {
		mCommands = commands;
	}

	@Override
	public boolean execute() {
		for (Command command : mCommands) {
			if (!command.execute()) {
				return false;
			}
		}

		return true;
	}

	@Override
	public boolean undo() {
		// Execute the commands in the reverse order
		for (int i = mCommands.length - 1; i >= 0; --i) {
			if (!mCommands[i].undo()) {
				return false;
			}
		}

		return true;
	}

	/** All commands that shall be run in sequence */
	Command[] mCommands;
}

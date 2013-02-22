package com.spiddekauga.utils;

/**
 * Interface for a command that is combinable. This means that two commands of
 * the same type can be put together as one—useful when executing multiple commands
 * of the same type (such as changing a value via a slider).
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public interface ICommandCombinable {
	/**
	 * Combines the parameter command with the this command. Only this command
	 * will be applicable. This method will also call execute on the specified command.
	 * @param otherCommand the other command to combine with this command
	 * @return true if otherCommand executed successfully (and thus was combined
	 * with this command)
	 */
	boolean combine(ICommandCombinable otherCommand);
}

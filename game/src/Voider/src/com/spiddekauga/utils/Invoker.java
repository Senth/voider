package com.spiddekauga.utils;

import java.util.Iterator;
import java.util.LinkedList;

import com.badlogic.gdx.utils.Disposable;

/**
 * Invokes commands, can undo/redo commands as they are stored in
 * a list
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class Invoker implements Disposable {

	/**
	 * Can be used as a clear command.
	 */
	@Override
	public void dispose() {
		disposeRedo();
		disposeUndo();
	}

	/**
	 * Executes the specified action. Only pushes this command onto the undo
	 * stack if it was successful.
	 * @param command the command to execute
	 * @return true if the command was executed successfully
	 * @see #execute(Command,boolean) if you want to make the command chained
	 */
	public boolean execute(Command command) {
		return execute(command, false);
	}

	/**
	 * Executes the specified action. Only pushes this command onto the undo
	 * stack if it was successful. Can set the commandn as chained. Chained
	 * commands will also execute the previous command on an undo(), and if there
	 * exist a chained command directly after a redo() that will also be redone until
	 * the next redo isn't a chained command. If this command is an instance of
	 * #ICommandStackable and is the same type of command as the last executed command
	 * it will combine these two.
	 * @param command the command to execute
	 * @param chained true if the command shall be chained
	 * @return true if the command was executed successfully
	 */
	public boolean execute(Command command, boolean chained) {
		// Same command type and combinable?
		boolean combined = false;
		if (command instanceof ICommandCombinable && !mUndoCommands.isEmpty()) {
			// Try last command, continue if last command is chained
			boolean lastChained = true;
			Iterator<Command> iterator = mUndoCommands.iterator();
			Command combinedCommand = null;
			while (iterator.hasNext() && lastChained && !combined) {
				combinedCommand = iterator.next();

				if (command.getClass() == combinedCommand.getClass()) {
					combined = ((ICommandCombinable)combinedCommand).combine((ICommandCombinable) command);
				}

				// Shall we check next?
				if (combinedCommand.isChained()) {
					lastChained = true;
				} else {
					lastChained = false;
				}
			}
		}

		boolean success = combined;

		if (!combined) {
			success = command.execute();
			if (success) {
				if (chained) {
					command.setAsChanied();
				}
				mUndoCommands.push(command);
				disposeRedo();

				/** @TODO maybe set a limit on 100 undo commands? */
			} else {
				command.dispose();
			}
		}

		return success;
	}

	/**
	 * Undoes the last executed command. The commands are on a stack so this can
	 * be called multiple times in a row. This adds the command to the redo stack
	 * @see #undo(boolean) for undoing a command and not add it to the redo stack
	 */
	public void undo() {
		undo(true);
	}

	/**
	 * Undoes the last command.
	 * @param addToRedoStack if set to false the command will NOT be added to the
	 * redo stack.
	 */
	public void undo(boolean addToRedoStack) {
		boolean chained = true;
		while (canUndo() && chained) {
			Command undoCommand = mUndoCommands.pop();
			chained = undoCommand.isChained();

			// Special case for delimiter. Always treat it as chained
			if (undoCommand instanceof CDelimiter) {
				chained = true;
			}

			undoCommand.undo();
			if (addToRedoStack) {
				mRedoCommands.push(undoCommand);
			}
		}
	}

	/**
	 * Redoes the last undone command. The commands are on a stack so this can
	 * be called multiple times in a row.
	 */
	public void redo() {
		boolean chained = true;
		while (canRedo() && chained) {
			Command redoCommand = mRedoCommands.pop();
			redoCommand.execute();
			mUndoCommands.push(redoCommand);

			// Is next redo chained? -> Execute it too
			if (canRedo()) {
				Command nextRedo = mRedoCommands.getFirst();

				// Special case for delimiter. Always treat it as chained
				if (nextRedo.isChained() || nextRedo instanceof CDelimiter) {
					chained = true;
				}
			}
		}
	}

	/**
	 * @return true if the invoker can undo a command
	 */
	public boolean canUndo() {
		return !mUndoCommands.isEmpty();
	}

	/**
	 * @return true if the invoker can redo a command
	 */
	public boolean canRedo() {
		return !mRedoCommands.isEmpty();
	}

	/**
	 * Disposes all undo commands
	 */
	private void disposeUndo() {
		for (Command command : mUndoCommands) {
			command.dispose();
		}
		mUndoCommands.clear();
	}

	/**
	 * Disposes all redo commands
	 */
	private void disposeRedo() {
		for (Command command : mRedoCommands) {
			command.dispose();
		}
		mRedoCommands.clear();
	}

	/** The stack with undo commands */
	private LinkedList<Command> mUndoCommands = new LinkedList<Command>();
	/** Stack with redo commands */
	private LinkedList<Command> mRedoCommands = new LinkedList<Command>();
}

package com.spiddekauga.utils.commands;

import java.util.Iterator;
import java.util.LinkedList;

import com.badlogic.gdx.utils.Disposable;

/**
 * Invokes commands, can undo/redo commands as they are stored in a list
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
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
	 * Execute all the specified commands
	 * @param commands all commands to be executed
	 * @param chained true if all commands (except the first one) should be chained
	 * @param firstChained true if the first command should be chained
	 */
	public void execute(Iterable<Command> commands, boolean chained, boolean firstChained) {
		boolean chain = firstChained;

		for (Command command : commands) {
			execute(command, chain);
			chain = chained;
		}
	}

	/**
	 * Executes the specified action. Only pushes this command onto the undo stack if it
	 * was successful.
	 * @param command the command to execute
	 * @return true if the command was executed successfully
	 * @see #execute(Command,boolean) if you want to make the command chained
	 */
	public boolean execute(Command command) {
		return execute(command, false);
	}

	/**
	 * Executes the specified action. Only pushes this command onto the undo stack if it
	 * was successful. Can set the commandn as chained. Chained commands will also execute
	 * the previous command on an undo(), and if there exist a chained command directly
	 * after a redo() that will also be redone until the next redo isn't a chained
	 * command. If this command is an instance of #ICommandStackable and is the same type
	 * of command as the last executed command it will combine these two.
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
					combined = ((ICommandCombinable) combinedCommand).combine((ICommandCombinable) command);
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
			} else {
				command.dispose();
			}
		}

		return success;
	}

	/**
	 * Push a delimiter onto the execute (undo) stack
	 */
	public void pushDelimiter() {
		mUndoCommands.push(new CDelimiter());
	}

	/**
	 * Push a delimiter onto the execute (undo) stack
	 * @param name the name of the delimiter. Useful when undoing to a specific delimiter
	 *        using
	 */
	public void pushDelimiter(String name) {
		mUndoCommands.push(new CDelimiter(name));
	}

	/**
	 * Undo all commands to and including the specified delimiter. This method adds all
	 * undone commands to the redo stack.
	 * @param name delimiter name to undo to and including
	 */
	public void undoToDelimiter(String name) {
		undoToDelimiter(name, true);
	}

	/**
	 * Undo all commands to and including the specified delimiter.
	 * @param name delimiter name to undo to and including
	 * @param addToRedoStack set to true to add all undone commands to the redo stack
	 */
	public void undoToDelimiter(String name, boolean addToRedoStack) {
		boolean foundDelimiter = false;
		while (canUndo() && !foundDelimiter) {
			Command undoCommand = mUndoCommands.pop();

			boolean success = undoCommand.undo();
			if (addToRedoStack && success) {
				mRedoCommands.push(undoCommand);
			} else {
				undoCommand.dispose();
			}

			// Check if found
			if (undoCommand instanceof CDelimiter) {
				foundDelimiter = ((CDelimiter) undoCommand).getDelimeterName().equals(name);
			}
		}
	}

	/**
	 * Undoes the last executed command. The commands are on a stack so this can be called
	 * multiple times in a row. This adds the command to the redo stack
	 * @see #undo(boolean) for undoing a command and not add it to the redo stack
	 */
	public void undo() {
		undo(true);
	}

	/**
	 * Undoes the last command.
	 * @param addToRedoStack set to true to add all undone commands to the redo stack
	 */
	public void undo(boolean addToRedoStack) {
		boolean chained = true;
		while (canUndo() && chained) {
			Command undoCommand = mUndoCommands.pop();
			chained = undoCommand.isChained();

			boolean success = undoCommand.undo();
			if (addToRedoStack && success) {
				mRedoCommands.push(undoCommand);
			} else {
				undoCommand.dispose();
			}
		}
	}

	/**
	 * Redoes the last undone command. The commands are on a stack so this can be called
	 * multiple times in a row.
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

				chained = nextRedo.isChained();
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
	 * Clears the redo stack
	 */
	public void clearRedo() {
		disposeRedo();
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

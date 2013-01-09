package com.spiddekauga.voider.editor;

import java.util.LinkedList;

import com.badlogic.gdx.utils.Disposable;
import com.spiddekauga.voider.editor.commands.LevelCommand;
import com.spiddekauga.voider.game.Level;

/**
 * Executes level commands on the level associated with the invoker.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class LevelInvoker implements Disposable {
	/**
	 * Default constructor, a level needs to be set before this invoker can be used
	 */
	public LevelInvoker() {
		// Does nothing
	}

	@Override
	public void dispose() {
		disposeRedo();
		disposeUndo();
	}

	/**
	 * Sets a new level to invoke commands on.
	 * @note entire undo and redo stack will be cleared
	 * @param level the new level to execute the actions on
	 * @param levelEditor the editor of the level
	 */
	public void setLevel(Level level, LevelEditor levelEditor) {
		mLevel = level;
		mLevelEditor = levelEditor;
		mUndoCommands.clear();
		mRedoCommands.clear();
	}

	/**
	 * Executes the specified action on the level contained in this invoker.
	 * This also clears the redo stack.
	 * @param command the command to execute
	 * @return true if the command was executed successfully
	 */
	public boolean execute(LevelCommand command) {
		boolean success = command.execute(mLevel, mLevelEditor);
		if (success) {
			mUndoCommands.push(command);
			disposeRedo();

			/** @TODO maybe set a limit on 100 undo commands? */
		}

		return success;
	}

	/**
	 * Undoes the last executed command. The commands are on a stack so this can
	 * be called multiple times in a row.
	 */
	public void undo() {
		boolean chained = true;
		while (canUndo() && chained) {
			LevelCommand undoCommand = mUndoCommands.pop();
			chained = undoCommand.isChained();
			undoCommand.undo(mLevel, mLevelEditor);
			mRedoCommands.push(undoCommand);
		}
	}

	/**
	 * Redoes the last undone command. The commands are on a stack so this can
	 * be called multiple times in a row.
	 */
	public void redo() {
		boolean chained = true;
		while (canRedo() && chained) {
			LevelCommand redoCommand = mRedoCommands.pop();
			redoCommand.execute(mLevel, mLevelEditor);
			mUndoCommands.push(redoCommand);

			// Is next redo chained? -> Execute it too
			if (canRedo()) {
				chained = mRedoCommands.getFirst().isChained();
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
		for (LevelCommand command : mUndoCommands) {
			command.dispose();
		}
		mUndoCommands.clear();
	}

	/**
	 * Disposes all redo commands
	 */
	private void disposeRedo() {
		for (LevelCommand command : mRedoCommands) {
			command.dispose();
		}
		mRedoCommands.clear();
	}

	/** LevelEditor to invoke some commands on */
	private LevelEditor mLevelEditor = null;
	/** Level to invoke commands on */
	private Level mLevel = null;
	/** The stack with undo commands */
	private LinkedList<LevelCommand> mUndoCommands = new LinkedList<LevelCommand>();
	/** Stack with redo commands */
	private LinkedList<LevelCommand> mRedoCommands = new LinkedList<LevelCommand>();
}

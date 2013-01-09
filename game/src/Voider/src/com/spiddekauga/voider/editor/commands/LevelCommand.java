package com.spiddekauga.voider.editor.commands;

import com.badlogic.gdx.utils.Disposable;
import com.spiddekauga.voider.editor.LevelEditor;
import com.spiddekauga.voider.game.Level;

/**
 * Base class for all level commands
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public abstract class LevelCommand implements Disposable {
	/**
	 * Default constructor which creates a non-chained command
	 */
	public LevelCommand() {
		this(false);
	}

	/**
	 * Constructor which makes the command chained
	 * @param chained true if chained
	 */
	public LevelCommand(boolean chained) {
		mChained = chained;
	}

	/**
	 * Executes the command
	 * @param level the level to execute the command on
	 * @param levelEditor the level editor to execute the command on
	 * @return true if the command was successfully executed
	 */
	public abstract boolean execute(Level level, LevelEditor levelEditor);

	/**
	 * Executes the undo command, i.e. reverses the effect of the execute command
	 * @param level the level to execute the command on
	 * @param levelEditor teh level edito to execute the command on
	 * @return true if the command was successfully undone
	 */
	public abstract boolean undo(Level level, LevelEditor levelEditor);

	@Override
	public void dispose() {
		// Does nothing
	}

	/**
	 * A command is chained if it should be treated as one combined command with
	 * the previous command. An example would be ClAddActor(actor) and then ClSelectActor(actor).
	 * This will make the undo command undo both ClSelectActor and ClAddActor, likewise when using
	 * redo, when doing redo on ClAddActor and the next redo is chained (i.e. ClSelectActor) it will
	 * execute that command too.
	 * @return true if the command is chained.
	 */
	public boolean isChained() {
		return mChained;
	}

	/** If the command is chained (combined) with the previous command. */
	private boolean mChained;
}

package com.spiddekauga.utils.commands;


/**
 * Only delimits two commands. Useful when commands are made to be combined. Such as
 * sliders, a delimiter could be added whenever the player lifts the mouse button.
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
class CDelimiter extends Command {
	/**
	 * Creates a default delimiter without a name
	 */
	public CDelimiter() {
		mName = "";
		setAsChanied();
	}

	/**
	 * Create a delimiter with a name
	 * @param name delimiter namae
	 */
	public CDelimiter(String name) {
		mName = name;
		setAsChanied();
	}

	@Override
	public boolean execute() {
		return true;
	}

	@Override
	public boolean undo() {
		return true;
	}

	/**
	 * @return the delimiter name
	 */
	String getDelimeterName() {
		return mName;
	}

	/** Delimiter name */
	private String mName;
}

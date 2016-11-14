package com.spiddekauga.utils.commands;


/**
 * Only delimits two commands. Useful when commands are made to be combined. Such as sliders, a
 * delimiter could be added whenever the player lifts the mouse button.
 */
class CDelimiter extends Command {
/** Delimiter name */
private String mName;

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
}

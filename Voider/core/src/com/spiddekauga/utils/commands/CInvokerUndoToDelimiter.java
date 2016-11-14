package com.spiddekauga.utils.commands;

/**
 * A command that undoes an specified invoker until the delimiter name is found. I.e. it calls
 * Invoker.undoToDelimiter(String,boolean)
 */
public class CInvokerUndoToDelimiter extends Command {
/** Invoker to call undoToDelimiter() in */
private Invoker mInvoker;
/** Name of the delimiter to undo to */
private String mDelimiterName;
/** If the undone commands should be added to the redo stack */
private boolean mAddToRedoStack;

/**
 * @param invoker the invoker to undo
 * @param name delimiter name to undo to (and including)
 * @param addToRedoStack set to true if the undone commands should be added to the redo stack
 */
public CInvokerUndoToDelimiter(Invoker invoker, String name, boolean addToRedoStack) {
	mInvoker = invoker;
	mDelimiterName = name;
	mAddToRedoStack = addToRedoStack;
}

@Override
public boolean execute() {
	mInvoker.undoToDelimiter(mDelimiterName, mAddToRedoStack);
	return true;
}

@Override
public boolean undo() {
	return false;
}
}

package com.spiddekauga.utils;


/**
 * Improved version of the observable. method notifyObservers() automatically sets this
 * object as changed using setChanged() before calling java's implementation of
 * notifyObservers()
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public class ObservableExt extends java.util.Observable {

	@Override
	public void notifyObservers() {
		setChanged();
		super.notifyObservers();
	}

	@Override
	public void notifyObservers(Object arg) {
		setChanged();
		super.notifyObservers(arg);
	}
}

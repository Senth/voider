package com.spiddekauga.voider.server.util;

import java.io.Serializable;

/**
 * Abstract class for all session variables
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public abstract class SessionVariable implements Serializable {

	/**
	 * Call this when a session variable has been changed
	 */
	protected void setChanged() {
		mChanged = true;
	}

	/**
	 * @return true if the session variable has been changed since
	 * it was read from the session
	 */
	public boolean isChanged() {
		return mChanged;
	}

	/**
	 * Sets the session variable as saved, i.e. changed to false.
	 * @note Do this BEFORE saving the actual variable, otherwise
	 * this value won't be saved correctly.
	 */
	public void setSaved() {
		mChanged = false;
	}

	/** If the variable has been changed since last session read */
	private boolean mChanged = true;
	/** Serialized serial version */
	private static final long serialVersionUID = -5396331642856090956L;
}

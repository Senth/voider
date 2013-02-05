package com.spiddekauga.voider.utils;

import com.badlogic.gdx.math.Vector2;

/**
 * Wrapper for a position and time
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class TimePos {

	//	/**
	//	 * Sets the position and time.
	//	 * @param position this variable is deep copied
	//	 * @param time
	//	 */
	//	public void set(Vector2 position, float time) {
	//		mPosition.set(position);
	//		mTime = time;
	//	}
	//
	//	/**
	//	 * @return time of the time position
	//	 */
	//	public float getTime() {
	//		return mTime;
	//	}
	//
	//	/**
	//	 * @return position of the time position
	//	 */
	//	public Vector2 getPosition() {
	//		return mPosition;
	//	}
	//
	//
	//	@Override
	//	public void dispose() {
	//		Pools.free(mPosition);
	//	}

	/** Position */
	public Vector2 position = new Vector2();
	/** Time */
	public float time = 0;
}

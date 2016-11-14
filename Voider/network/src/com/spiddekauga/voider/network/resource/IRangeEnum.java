package com.spiddekauga.voider.network.resource;

import com.spiddekauga.utils.ISearchStore;

/**
 * Range enumeration / category for searches

 */
public interface IRangeEnum extends ISearchStore {
	/**
	 * @return lowest possible value in this range category
	 */
	float getLow();

	/**
	 * @return highest possible value in this range category
	 */
	float getHigh();
}

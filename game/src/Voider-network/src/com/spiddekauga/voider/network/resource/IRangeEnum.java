package com.spiddekauga.voider.network.resource;

import com.spiddekauga.voider.network.util.ISearchStore;

/**
 * Range enumeration / category for searches
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
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

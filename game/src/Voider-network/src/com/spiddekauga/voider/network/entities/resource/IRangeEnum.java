package com.spiddekauga.voider.network.entities.resource;

/**
 * Range enumeration / category for searches
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public interface IRangeEnum {
	/**
	 * @return human-readable display name
	 */
	String getDisplayName();

	/**
	 * @return internal storage name
	 */
	String getInternalName();

	/**
	 * @return lowest possible value in this range category
	 */
	float getLow();

	/**
	 * @return highest possible value in this range category
	 */
	float getHigh();
}

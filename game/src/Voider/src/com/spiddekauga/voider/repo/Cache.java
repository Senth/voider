package com.spiddekauga.voider.repo;

import java.util.Date;

/**
 * Base class for caches
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
abstract class Cache {
	/** Last updated cache */
	public Date lastUpdated = new Date();
	/** Creation time of this cache */
	public Date created = new Date();
}

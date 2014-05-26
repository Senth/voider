package com.spiddekauga.voider.resources;

/**
 * If the resource has a definition
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public interface IResourceHasDef {
	/**
	 * @param <DefType> type of definition
	 * @return definition of the resource
	 */
	<DefType extends Def> DefType getDef();
}

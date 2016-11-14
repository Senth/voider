package com.spiddekauga.voider.resources;

/**
 * If the resource has a definition

 */
public interface IResourceHasDef {
	/**
	 * @param <DefType> type of definition
	 * @return definition of the resource
	 */
	<DefType extends Def> DefType getDef();
}

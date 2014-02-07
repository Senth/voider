package com.spiddekauga.voider.network.entities;


/**
 * Abstract class for all method entities
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public abstract interface IMethodEntity extends IEntity {
	/**
	 * @return method url
	 */
	String getMethodName();
}

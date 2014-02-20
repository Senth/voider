package com.spiddekauga.voider.network.entities.method;

import com.spiddekauga.voider.network.entities.IEntity;

/**
 * Response of publish method.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
@SuppressWarnings("serial")
public class PublishMethodResponse implements IEntity {
	/** If publish was successful */
	public boolean success = false;
}
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
	public Statuses status = null;

	/**
	 * Return statuses of the method
	 */
	public enum Statuses {
		/** Successfully publish the resources */
		SUCCESS,
		/** Failed internal server error */
		FAILED_SERVER_ERROR,
		/** Failed could not connect to the server */
		FAILED_SERVER_CONNECTION,
	}
}
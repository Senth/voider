package com.spiddekauga.voider.network.entities.method;

import com.spiddekauga.voider.network.entities.IEntity;



/**
 * Abstract class for all method entities
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
 */
public abstract interface IMethodEntity extends IEntity {
	/**
	 * @return method url
	 */
	String getMethodName();

	/**
	 * Helper method names
	 */
	public enum MethodNames {
		/** Login the user */
		LOGIN,
		/** Register new user */
		REGISTER_USER,

		;
		/**
		 * Creates the enumeration with the correct url
		 */
		private MethodNames() {
			mUrl = name().toLowerCase().replace('_', '-');
		}

		@Override
		public String toString() {
			return mUrl;
		}

		/** The actual url of the method */
		private String mUrl;
	}
}

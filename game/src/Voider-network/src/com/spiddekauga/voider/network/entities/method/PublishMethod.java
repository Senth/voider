package com.spiddekauga.voider.network.entities.method;

import java.util.ArrayList;

import com.spiddekauga.voider.network.entities.DefEntity;

/**
 * Publishes resources
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
@SuppressWarnings("serial")
public class PublishMethod implements IMethodEntity {
	@Override
	public String getMethodName() {
		return "publish";
	}

	/** All definitions to publish */
	public ArrayList<DefEntity> defs = new ArrayList<>();
}

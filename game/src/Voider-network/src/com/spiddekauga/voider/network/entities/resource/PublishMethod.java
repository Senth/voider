package com.spiddekauga.voider.network.entities.resource;

import java.util.ArrayList;

import com.spiddekauga.voider.network.entities.IMethodEntity;

/**
 * Publishes resources
 * 
 * @author Matteus Magnusson <matteus.magnusson@spiddekauga.com>
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

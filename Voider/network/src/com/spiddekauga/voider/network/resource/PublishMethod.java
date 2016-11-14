package com.spiddekauga.voider.network.resource;

import java.util.ArrayList;

import com.spiddekauga.voider.network.entities.IMethodEntity;

/**
 * Publishes resources

 */
public class PublishMethod implements IMethodEntity {
	@Override
	public MethodNames getMethodName() {
		return MethodNames.PUBLISH;
	}

	/** All definitions to publish */
	public ArrayList<DefEntity> defs = new ArrayList<>();
}

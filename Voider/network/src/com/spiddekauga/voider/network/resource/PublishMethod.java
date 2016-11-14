package com.spiddekauga.voider.network.resource;

import com.spiddekauga.voider.network.entities.IMethodEntity;

import java.util.ArrayList;

/**
 * Publishes resources
 */
public class PublishMethod implements IMethodEntity {
/** All definitions to publish */
public ArrayList<DefEntity> defs = new ArrayList<>();

@Override
public MethodNames getMethodName() {
	return MethodNames.PUBLISH;
}
}

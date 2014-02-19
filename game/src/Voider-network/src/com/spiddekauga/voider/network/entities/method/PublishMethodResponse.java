package com.spiddekauga.voider.network.entities.method;

import java.util.HashMap;
import java.util.UUID;

import com.spiddekauga.voider.network.entities.IEntity;

/**
 * Response of publish method.
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
@SuppressWarnings("serial")
public class PublishMethodResponse implements IEntity {
	/** Map with all blob/file keys */
	HashMap<UUID, String> keys = new HashMap<>();
}
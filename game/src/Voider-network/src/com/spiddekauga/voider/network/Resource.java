package com.spiddekauga.voider.network;

import java.util.UUID;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;

/**
 * Resource wrapper for network
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class Resource {
	/** ID of the resource */
	@Tag(10) public UUID id;

	@Override
	public String toString() {
		return "Resource: {"
				+ "\n\tid: " + id
				+ "\n}";
	}
}

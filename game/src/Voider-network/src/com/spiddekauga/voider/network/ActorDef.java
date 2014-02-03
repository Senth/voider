package com.spiddekauga.voider.network;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;

/**
 * ActorDef kryo wrapper for network
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class ActorDef extends Def {
	/** Maximum lif of the actor */
	@Tag(17) public float maxLife;
	/** PNG image of actor */
	@Tag(18) public byte[] pngBytes = null;
}

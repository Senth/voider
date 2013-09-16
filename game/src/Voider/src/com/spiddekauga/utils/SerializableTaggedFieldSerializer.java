package com.spiddekauga.utils;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer;

/**
 * A serializer which first serializes all the tags in the class. Then
 * calls #com.esotericsoftware.kryo.KryoSerializable.write(Kryo,Output)
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class SerializableTaggedFieldSerializer extends TaggedFieldSerializer<KryoSerializable> {

	/**
	 * Creates the serializable tagged field serializer.
	 * @param kryo the kryo object to serialize the class in
	 * @param type the type to serialize
	 */
	public SerializableTaggedFieldSerializer(Kryo kryo, Class<? extends KryoSerializable> type) {
		super(kryo, type);
	}

	@Override
	public void write(Kryo kryo, Output output, KryoSerializable object) {
		super.write(kryo, output, object);
		object.write(kryo, output);
	}

	@Override
	public KryoSerializable read(Kryo kryo, Input input, Class<KryoSerializable> type) {
		KryoSerializable object = super.read(kryo, input, type);
		object.read(kryo, input);
		return object;
	}
}
